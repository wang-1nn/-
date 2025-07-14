package org.example.backend.service.impl;



import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.entity.pojo.AiChatMemory;


import org.example.backend.entity.pojo.QuestionBatch;
import org.example.backend.entity.pojo.TodoTask;
import org.example.backend.entity.vo.*;
import org.example.backend.mapper.BatchMapper;
import org.example.backend.mapper.TeacherMapper;
import org.example.backend.service.TeacherService;
import org.example.backend.service.impl.ai.TeacherAi.CreateLesson;
import org.example.backend.service.impl.ai.TeacherAi.OptimizateOutline;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import org.example.backend.mapper.QuestionMapper;
import org.example.backend.entity.pojo.Question;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {

    private final TeacherMapper teacherMapper;

    @Autowired
    OpenAiChatModel chatModel;

    @Autowired
    ChatMemory chatMemory;

    @Autowired
    JdbcChatMemoryRepository chatMemoryRepository;

    @Autowired
    BatchMapper batchMapper;

    @Autowired
    CreateLesson createLesson;

    @Autowired
    OptimizateOutline optimizateOutline;

    @Autowired
    QuestionMapper questionMapper;

    private ChatClient chatClient;
    @PostConstruct
    void init() {
        // 1. 内存（窗口大小 10）
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();

        // 2. ChatClient，默认挂载记忆顾问
        this.chatClient = ChatClient.builder(chatModel)// 挂载mcp至chatclient
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultSystem("你是一个自定义的rag应用请始终保持用中文回答问")
                .build();

    }

    @Override
    public DashboardSummaryVO getSummary(int teacherId) {
        DashboardSummaryVO summary = new DashboardSummaryVO();
        summary.setTeacherName(teacherMapper.findUsernameByUserId(teacherId));

        CourseStatVO courses = new CourseStatVO();
        courses.setTotal(teacherMapper.countCoursesByTeacherId(teacherId));
        courses.setActive(teacherMapper.countActiveCoursesByTeacherId(teacherId));
        courses.setTrend(teacherMapper.countCoursesTrendByTeacherId(teacherId));
        summary.setCourses(courses);

        StudentStatVO students = new StudentStatVO();
        students.setTotal(teacherMapper.countStudentsByTeacherId(teacherId));
        students.setActive(teacherMapper.countActiveStudentsByTeacherId(teacherId));
        summary.setStudents(students);

        PlanStatVO plans = new PlanStatVO();
        plans.setTotal(teacherMapper.countPlansByTeacherId(teacherId));
        plans.setTrend(teacherMapper.countPlansTrendByTeacherId(teacherId));
        summary.setPlans(plans);

        ResourceStatVO resources = new ResourceStatVO();
        resources.setTotal(teacherMapper.countResourcesByTeacherId(teacherId));
        resources.setTrend(teacherMapper.countResourcesTrendByTeacherId(teacherId));
        summary.setResources(resources);

        summary.setCurrentWeek(10);
        summary.setTotalWeeks(16);

        return summary;
    }

    @Override
    public List<AnnouncementVO> getAnnouncements(int teacherId) {
        return teacherMapper.findAnnouncementsByTeacherId(teacherId);
    }

    @Override
    public List<PendingTaskVO> getPendingTasks(int teacherId) {
        return teacherMapper.findTasksByTeacherId(teacherId)
                .stream()
                .map(this::convertToPendingTaskVO)
                .collect(Collectors.toList());
    }

    @Override
    public PendingTaskVO createTask(NewTaskVO vo) {
        TodoTask task = new TodoTask();
        task.setUserId(Long.parseLong(vo.getUid()));
        task.setTitle(vo.getTitle());
        task.setTaskType(TodoTask.getPriorityValue(vo.getPriority()));
        task.setStatus("PENDING");

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = dateFormat.parse(vo.getDeadline());
            LocalDateTime localDateTime = date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            task.setDueDate(localDateTime);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format for deadline", e);
        }

        task.setCreatedAt(LocalDateTime.now());

        teacherMapper.insertTask(task);
        return convertToPendingTaskVO(task);
    }

    @Override
    public void updateTaskStatus(long taskId, boolean isCompleted) {
        teacherMapper.updateTaskStatus(taskId, isCompleted ? "COMPLETED" : "PENDING");
    }

    @Override
    public void deleteTask(long taskId) {
        teacherMapper.deleteTask(taskId);
    }

    private PendingTaskVO convertToPendingTaskVO(TodoTask task) {
        return new PendingTaskVO(
                String.valueOf(task.getId()),
                task.getTitle(),
                task.getDueDate(),
                TodoTask.getPriorityString(task.getTaskType()),
                "COMPLETED".equals(task.getStatus())
        );
    }

    @Override
    public ChartDataVO<String, Long> getStudentActivity(int teacherId) {
        List<Map<String, Object>> rawData = teacherMapper.getStudentActivityLast7Days(teacherId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        Map<String, Long> activityByDate = rawData.stream()
                .collect(Collectors.toMap(
                        row -> {
                            Object dateObj = row.get("activity_date");
                            return formatter.format(LocalDate.parse(dateObj.toString()));
                        },
                        row -> ((Number) row.get("activity_count")).longValue()
                ));

        List<String> labels = IntStream.range(0, 7)
                .mapToObj(i -> LocalDate.now().minusDays(6 - i))
                .map(formatter::format)
                .collect(Collectors.toList());

        List<Long> values = labels.stream()
                .map(label -> activityByDate.getOrDefault(label, 0L))
                .collect(Collectors.toList());

        return new ChartDataVO<>(labels, values);
    }

    @Override
    public ChartDataVO<String, Long> getResourceAccess(int teacherId) {
        List<Map<String, Object>> rawData = teacherMapper.getResourceAccessCountByModule(teacherId);

        if (rawData == null || rawData.isEmpty()) {
            return new ChartDataVO<>(Collections.emptyList(), Collections.emptyList());
        }

        List<String> labels = rawData.stream()
                .map(row -> (String) row.get("module"))
                .collect(Collectors.toList());

        List<Long> values = rawData.stream()
                .map(row -> {
                    Object count = row.get("access_count");
                    if (count instanceof BigInteger) {
                        return ((BigInteger) count).longValue();
                    } else if (count instanceof Long) {
                        return (Long) count;
                    } else if (count instanceof Integer) {
                        return ((Integer) count).longValue();
                    }
                    return 0L;
                })
                .collect(Collectors.toList());

        return new ChartDataVO<>(labels, values);
    }

    @Override
    public Flux<String> Chat(String message, int id) {

        return chatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, id))          // 将 QA 顾问加入当前调用
                .stream()
                .content();
    }

    @Override
    public List<AiChatMemory> getChatHistory(String uid) {

        return teacherMapper.getChatHistory(uid);
    }

    @Override
    public Flux<String> OptimizateOutline(String message, String conversationId) {

        return optimizateOutline.chat(message, conversationId);
    }

    @Override
    public Flux<String> CreatLesson(String message, String conversationId) {
        return createLesson.chat(message,conversationId);
    }

    @Override
    public List<Question> listQuestions() {
        return questionMapper.listAll();
    }

    @Override
    public List<Question>GetQuestionById(String questionId){
        return questionMapper.findQuestionById(questionId);
    }

    @Override
    public void deleteQuestion(long id) {
        questionMapper.deleteQuestionById(id);
    }

    @Override
    public List<QuestionBatch> listBatches() {

        return batchMapper.listAll();
    }


}
