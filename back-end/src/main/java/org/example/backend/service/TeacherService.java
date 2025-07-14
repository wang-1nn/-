package org.example.backend.service;


import org.example.backend.entity.pojo.AiChatMemory;
import org.example.backend.entity.pojo.Question;
import org.example.backend.entity.pojo.QuestionBatch;
import org.example.backend.entity.vo.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

public interface TeacherService {

    // @Autowired
    // private DashboardMapper dashboardMapper;

    // @Autowired
    // private UserMapper userMapper;

    DashboardSummaryVO getSummary(int teacherId);
    // Long teacherId = // 从Spring Security获取当前用户ID

    // 1. 获取教师姓名
    // String teacherName = userMapper.findNameById(teacherId);

    // 2. 获取课程数
    // Integer courseCount = dashboardMapper.countCoursesByTeacherId(teacherId);

    // 3. 获取学生总数
    // Integer studentCount = dashboardMapper.countStudentsByTeacherId(teacherId);

    // 4. 获取待办任务数
    // Integer pendingTasksCount = dashboardMapper.countPendingTasksByTeacherId(teacherId);

    // 组装VO对象
//        DashboardSummaryVO summary = new DashboardSummaryVO();
//        // summary.setTeacherName(teacherName);
//        // summary.setCourseCount(courseCount);
//        // summary.setStudentCount(studentCount);
//        // summary.setPendingTasksCount(pendingTasksCount);

    // 暂时返回模拟数据
//        summary.setTeacherName("王老师 (模拟)");
//        summary.setCourseCount(10);
//        summary.setStudentCount(230);
//        summary.setPendingTasksCount(8);

    List<AnnouncementVO> getAnnouncements(int teacherId);

    List<PendingTaskVO> getPendingTasks(int teacherId);

    PendingTaskVO createTask(NewTaskVO task);

    void updateTaskStatus(long taskId, boolean isCompleted);

    void deleteTask(long taskId);

    ChartDataVO<String, Long> getStudentActivity(int teacherId);

    ChartDataVO<String, Long> getResourceAccess(int teacherId);

    Flux<String> Chat(String message,int id);

    List<AiChatMemory> getChatHistory(String uid);

    Flux<String> OptimizateOutline(String message, String conversationId);

    Flux<String> CreatLesson(String message, String conversationId);

    List<Question> listQuestions();

    List<Question>GetQuestionById(String questionId);

    List<QuestionBatch> listBatches();

    void deleteQuestion(long id);
}
