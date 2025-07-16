package org.example.backend.controller;


import lombok.extern.slf4j.Slf4j;
import org.example.backend.entity.pojo.AiChatMemory;
import org.example.backend.entity.pojo.QGRequest;
import org.example.backend.entity.pojo.Question;
import org.example.backend.entity.pojo.QuestionBatch;
import org.example.backend.entity.dto.QuestionGenerationRequest;
import org.example.backend.entity.dto.GeneratedQuestionDTO;
import org.example.backend.entity.dto.ExamCreateRequest;
import org.example.backend.entity.vo.*;
import org.example.backend.entity.vo.ExamVO;
import org.example.backend.entity.vo.ExamListVO;
import org.example.backend.entity.vo.QuestionSelectionVO;
import org.example.backend.entity.vo.CourseClassVO;
import org.example.backend.entity.vo.teacher.TeacherDashboardVO;
import org.example.backend.entity.pojo.LessonPlan;
import org.example.backend.entity.pojo.LessonTemplate;
import org.example.backend.entity.pojo.TeachingResource;
import org.example.backend.entity.vo.LessonPlanVO;
import org.example.backend.service.TeacherService;
import org.example.backend.service.TeacherDashboardService;
import org.example.backend.service.TeacherAIService;
import org.example.backend.service.LessonPlanService;
import org.example.backend.service.QuestionBankService;
import org.example.backend.service.ExamManagementService;
import org.example.backend.service.impl.ai.TeacherAi.CreateLesson;
import org.example.backend.service.impl.ai.TeacherAi.OptimizateOutline;
import org.example.backend.service.impl.ai.TeacherAi.QuestionAiService;
import org.example.backend.util.Ingestor;
import org.example.backend.util.RestBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.*;


@Slf4j
@RestController
@RequestMapping("/api/teacher")

public class TeacherController {



    @Autowired
    TeacherService teacherService;

    @Autowired
    TeacherDashboardService teacherDashboardService;

    @Autowired
    TeacherAIService teacherAIService;

    @Autowired
    LessonPlanService lessonPlanService;

    @Autowired
    CreateLesson createLesson;

    @Autowired
    Ingestor ingestor;
    @Autowired
    OptimizateOutline optimizateOutline;

    @Autowired
    QuestionAiService questionAiService;

    @Autowired
    QuestionBankService questionBankService;

    @Autowired
    ExamManagementService examManagementService;

    private int getTeacherIdFromUid(String uid) {
        // In a real application, you would get the user ID from the security context (e.g., JWT token)
        // For now, we parse it from the request parameter.
        return Integer.parseInt(uid);
    }

    @GetMapping("/dashboard/summary/legacy")
    public RestBean<DashboardSummaryVO> getSummaryLegacy(@RequestParam String uid) {
        try {
            return RestBean.success(teacherService.getSummary(getTeacherIdFromUid(uid)));
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        }
    }

    @GetMapping("/dashboard/announcements/legacy")
    public RestBean<List<AnnouncementVO>> getAnnouncementsLegacy(@RequestParam String uid) {
        try {
            return RestBean.success(teacherService.getAnnouncements(getTeacherIdFromUid(uid)));
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        }
    }

    @GetMapping("/dashboard/pending-tasks/legacy")
    public RestBean<List<PendingTaskVO>> getPendingTasksLegacy(@RequestParam String uid) {
        try {
            return RestBean.success(teacherService.getPendingTasks(getTeacherIdFromUid(uid)));
        } catch (NumberFormatException e) {
            return RestBean.success(Collections.emptyList());
        }
    }

    @PostMapping("/dashboard/tasks/legacy")
    public RestBean<PendingTaskVO> createTaskLegacy(@RequestBody NewTaskVO task) {
        try {
            PendingTaskVO createdTask = teacherService.createTask(task);
            return RestBean.success(createdTask);
        } catch (Exception e) {
            return RestBean.failure(400, e.getMessage());
        }
    }

    @PutMapping("/dashboard/tasks/{taskId}/legacy")
    public RestBean<Void> updateTaskStatusLegacy(@PathVariable long taskId, @RequestBody Map<String, Boolean> payload) {
        try {
            Boolean isCompleted = payload.get("completed");
            if (isCompleted == null) {
                return RestBean.failure(400, "请求体缺少 'completed' 字段");
            }
            teacherService.updateTaskStatus(taskId, isCompleted);
            return RestBean.success(null);
        } catch (Exception e) {
            return RestBean.failure(500, e.getMessage());
        }
    }


    @GetMapping("/dashboard/student-activity")
    public RestBean<ChartDataVO<String, Long>> getStudentActivity(@RequestParam String uid) {
        try {
            return RestBean.success(teacherService.getStudentActivity(getTeacherIdFromUid(uid)));
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        }
    }

    @GetMapping("/dashboard/resource-access")
    public RestBean<ChartDataVO<String, Long>> getResourceAccess(@RequestParam String uid) {
        try {
            return RestBean.success(teacherService.getResourceAccess(getTeacherIdFromUid(uid)));
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        }
    }

    /**
     * 完成任务接口 - 专门用于标记任务为已完成
     * 此接口是为了适配前端通过POST请求完成任务的需求
     * @param taskId 任务ID
     * @return 操作结果
     */
    @PostMapping("/dashboard/tasks/{taskId}/complete")
    public RestBean<Void> completeTask(@PathVariable long taskId) {
        try {
            teacherService.updateTaskStatus(taskId, true);
            return RestBean.success("任务已标记为完成");
        } catch (Exception e) {
            return RestBean.failure(500, e.getMessage());
        }
    }

    /**
     * 取消完成任务接口 - 专门用于标记任务为未完成
     * 此接口是为了适配前端通过POST请求取消完成任务的需求
     * @param taskId 任务ID
     * @return 操作结果
     */
    @PostMapping("/dashboard/tasks/{taskId}/uncomplete")
    public RestBean<Void> uncompleteTask(@PathVariable long taskId) {
        try {
            teacherService.updateTaskStatus(taskId, false);
            return RestBean.success("任务已标记为未完成");
        } catch (Exception e) {
            return RestBean.failure(500, e.getMessage());
        }
    }

    /**
     * 删除任务接口 - 通过POST请求实现，为了适配前端
     * 此接口是为了适配前端通过POST请求删除任务的需求
     * @param taskId 任务ID
     * @return 操作结果
     */
    @PostMapping("/dashboard/tasks/{taskId}/delete")
    public RestBean<Void> deleteTaskByPost(@PathVariable long taskId) {
        try {
            teacherService.deleteTask(taskId);
            return RestBean.success("任务已删除");
        } catch (Exception e) {
            return RestBean.failure(500, e.getMessage());
        }
    }

    @PostMapping(value = "/chat",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> Chat(@RequestParam String message, @RequestParam String uid){
        try {
            int id = Integer.parseInt(uid);
            return teacherService.Chat(message,id);
        } catch (Exception e) {
            e.printStackTrace();
            return Flux.just("发生错误: " + e.getMessage());
        }
    }

    @GetMapping("/getChatHistory")
    public RestBean<List<AiChatMemory>>getChatHistory(@RequestParam String uid){
        return RestBean.success("cg",teacherService.getChatHistory(uid));
    }

    @PostMapping(value = "/Ai/OptimizateOutline", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> OptimizateOutline(@RequestPart("message") String message,
                                          @RequestPart(value = "conversationId", required = false) String conversationId,
                                          @RequestPart(value = "files", required = false) MultipartFile[] files){
        String prompt = buildPrompt(message, files);

        return teacherService.OptimizateOutline(prompt,conversationId);
    }

    @PostMapping(value = "/Ai/CreateLesson", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> CreateLesson(@RequestPart("message") String message,@RequestParam("id")String id){
        return teacherService.CreatLesson(message,id);
    }

    @PostMapping(value = "/Ai/GenerateQuestions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateQuestions(@RequestBody QGRequest request) {
        return questionAiService.generate(request);
    }

    @GetMapping("/Ai/Questions")
    public RestBean<List<Question>> getQuestions(){
        return RestBean.success(teacherService.listQuestions());
    }

    @GetMapping("/Ai/GetQuestionById")
    public  RestBean<List<Question>>GetQuestionById(@RequestParam("questionId")String questionId){
        return RestBean.success(teacherService.GetQuestionById(questionId));
    }

    @GetMapping("/Ai/Batches")
    public RestBean<List<QuestionBatch>> getBatches(){
        return RestBean.success(teacherService.listBatches());
    }

    @PostMapping("/Ai/Questions/{id}/delete")
    public RestBean<Void> deleteQuestionByPost(@PathVariable long id) {
        try {
            teacherService.deleteQuestion(id);
            return RestBean.success("题目已删除");
        } catch (Exception e) {
            return RestBean.failure(500, e.getMessage());
        }
    }

    // ========================================
    // 新的题目生成相关API
    // ========================================

    /**
     * 生成题目（流式响应）- 新版本
     */
    @PostMapping(value = "/question-generator/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateQuestionsNew(@Valid @RequestBody QuestionGenerationRequest request,
                                            HttpServletRequest httpRequest) {
        try {
            // 从请求中获取用户ID
            String userId = httpRequest.getHeader("userId");
            if (userId == null || userId.isEmpty()) {
                userId = "default_teacher";
            }
            request.setCreatedBy(userId);

            return questionBankService.generateQuestions(request);

        } catch (Exception e) {
            return Flux.error(new RuntimeException("题目生成失败：" + e.getMessage()));
        }
    }

    /**
     * 解析并保存生成的题目
     */
    @PostMapping("/question-generator/parse-and-save")
    public RestBean<List<GeneratedQuestionDTO>> parseAndSaveQuestions(
            @RequestBody Map<String, Object> requestData,
            HttpServletRequest httpRequest) {
        try {
            log.info("收到解析保存请求，数据: {}", requestData);

            // 验证必要参数
            if (requestData == null) {
                return RestBean.failure(400, "请求数据不能为空");
            }

            String aiResponse = (String) requestData.get("aiResponse");
            if (aiResponse == null || aiResponse.trim().isEmpty()) {
                return RestBean.failure(400, "AI响应内容不能为空");
            }

            Object originalRequestObj = requestData.get("originalRequest");
            if (originalRequestObj == null) {
                return RestBean.failure(400, "原始请求数据不能为空");
            }

            Map<String, Object> requestMap = (Map<String, Object>) originalRequestObj;

            // 安全地重构请求对象
            QuestionGenerationRequest request = new QuestionGenerationRequest();
            request.setTopic(getStringFromMap(requestMap, "topic"));
            request.setSelectedTypes(getListFromMap(requestMap, "selectedTypes"));
            request.setDifficulty(getIntegerFromMap(requestMap, "difficulty"));
            request.setCount(getIntegerFromMap(requestMap, "count"));
            request.setRequirements(getStringFromMap(requestMap, "requirements"));

            String userId = httpRequest.getHeader("userId");
            if (userId == null || userId.isEmpty()) {
                userId = "default_teacher";
            }
            request.setCreatedBy(userId);

            log.info("解析请求对象: {}", request);

            List<GeneratedQuestionDTO> questions = questionBankService.parseAndSaveQuestions(aiResponse, request);

            log.info("成功解析并保存{}道题目", questions.size());
            return RestBean.success("题目保存成功", questions);

        } catch (Exception e) {
            log.error("解析保存题目失败", e);
            return RestBean.failure(500, "解析保存题目失败：" + e.getMessage());
        }
    }

    /**
     * 安全地从Map中获取字符串值
     */
    private String getStringFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 安全地从Map中获取整数值
     */
    private Integer getIntegerFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }

        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                log.warn("无法解析整数值: {}", value);
                return null;
            }
        }

        return null;
    }

    /**
     * 安全地从Map中获取列表值
     */
    private List<String> getListFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }

        if (value instanceof List) {
            List<?> list = (List<?>) value;
            List<String> stringList = new ArrayList<>();
            for (Object item : list) {
                if (item != null) {
                    stringList.add(item.toString());
                }
            }
            return stringList;
        }

        return null;
    }

    /**
     * 批量保存题目到题库
     */
    @PostMapping("/question-bank/batch-save")
    public RestBean<String> batchSaveQuestionsToBank(@RequestBody List<GeneratedQuestionDTO> questions,
                                                    HttpServletRequest httpRequest) {
        try {
            String userId = httpRequest.getHeader("userId");
            if (userId == null || userId.isEmpty()) {
                userId = "default_teacher";
            }

            boolean success = questionBankService.batchSaveQuestionsToBank(questions, userId);
            if (success) {
                return RestBean.success("批量保存成功");
            } else {
                return RestBean.failure(500, "批量保存失败");
            }
        } catch (Exception e) {
            return RestBean.failure(500, "批量保存题目失败：" + e.getMessage());
        }
    }

    /**
     * 根据题型筛选题目
     */
    @GetMapping("/question-bank/filter/type")
    public RestBean<List<Question>> getQuestionsByType(@RequestParam String questionType) {
        try {
            List<Question> questions = questionBankService.getQuestionsByType(questionType);
            return RestBean.success("筛选题目成功", questions);
        } catch (Exception e) {
            return RestBean.failure(500, "筛选题目失败：" + e.getMessage());
        }
    }

    /**
     * 根据难度筛选题目
     */
    @GetMapping("/question-bank/filter/difficulty")
    public RestBean<List<Question>> getQuestionsByDifficulty(@RequestParam Integer difficulty) {
        try {
            List<Question> questions = questionBankService.getQuestionsByDifficulty(difficulty);
            return RestBean.success("筛选题目成功", questions);
        } catch (Exception e) {
            return RestBean.failure(500, "筛选题目失败：" + e.getMessage());
        }
    }

    /**
     * 获取题库统计信息
     */
    @GetMapping("/question-bank/statistics")
    public RestBean<Map<String, Object>> getQuestionBankStatistics() {
        try {
            List<Question> allQuestions = questionBankService.getAllQuestions();

            // 统计各种信息
            long totalCount = allQuestions.size();
            long aiGeneratedCount = allQuestions.stream()
                    .filter(q -> q.getIsAiGenerated() != null && q.getIsAiGenerated())
                    .count();

            // 按题型统计
            Map<String, Long> typeStats = allQuestions.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            q -> q.getQuestionType() != null ? q.getQuestionType() : "未知",
                            java.util.stream.Collectors.counting()));

            // 按难度统计
            Map<Integer, Long> difficultyStats = allQuestions.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            q -> q.getDifficulty() != null ? q.getDifficulty() : 0,
                            java.util.stream.Collectors.counting()));

            Map<String, Object> statistics = Map.of(
                    "totalCount", totalCount,
                    "aiGeneratedCount", aiGeneratedCount,
                    "manualCount", totalCount - aiGeneratedCount,
                    "typeStatistics", typeStats,
                    "difficultyStatistics", difficultyStats
            );

            return RestBean.success("获取统计信息成功", statistics);
        } catch (Exception e) {
            return RestBean.failure(500, "获取统计信息失败：" + e.getMessage());
        }
    }

    /**
     * 调试AI响应解析（开发测试用）
     */
    @PostMapping("/question-generator/debug-parse")
    public RestBean<Map<String, Object>> debugParseResponse(@RequestBody Map<String, String> requestData) {
        try {
            String aiResponse = requestData.get("aiResponse");

            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("originalResponse", aiResponse);
            debugInfo.put("responseLength", aiResponse.length());
            debugInfo.put("containsData", aiResponse.contains("data:"));
            debugInfo.put("containsJson", aiResponse.contains("{") || aiResponse.contains("["));

            // 尝试解析（不保存到数据库）
            try {
                QuestionGenerationRequest dummyRequest = new QuestionGenerationRequest();
                dummyRequest.setCreatedBy("debug_user");
                dummyRequest.setTopic("调试测试");
                dummyRequest.setDifficulty(3);
                dummyRequest.setCount(1);

                List<GeneratedQuestionDTO> questions = questionBankService.parseAndSaveQuestions(aiResponse, dummyRequest);
                debugInfo.put("parseSuccess", true);
                debugInfo.put("questionCount", questions.size());
                debugInfo.put("questions", questions);

            } catch (Exception e) {
                debugInfo.put("parseSuccess", false);
                debugInfo.put("parseError", e.getMessage());
                debugInfo.put("stackTrace", e.getStackTrace());
            }

            return RestBean.success("调试信息获取成功", debugInfo);

        } catch (Exception e) {
            return RestBean.failure(500, "调试失败：" + e.getMessage());
        }
    }

    /**
     * 简单测试题目生成和保存
     */
    @PostMapping("/question-generator/test-simple")
    public RestBean<List<GeneratedQuestionDTO>> testSimpleGeneration() {
        try {
            log.info("开始简单测试");

            // 创建一个简单的测试题目
            QuestionGenerationRequest request = new QuestionGenerationRequest();
            request.setTopic("测试题目");
            request.setDifficulty(3);
            request.setCount(1);
            request.setCreatedBy("test_user");

            // 模拟AI响应
            String mockAiResponse = """
                [
                  {
                    "type": "single",
                    "content": "Java中哪个关键字用于定义类？",
                    "options": [
                      {"key": "A", "value": "class"},
                      {"key": "B", "value": "interface"},
                      {"key": "C", "value": "enum"},
                      {"key": "D", "value": "abstract"}
                    ],
                    "answer": "A",
                    "analysis": "在Java中，class关键字用于定义类。",
                    "difficulty": 3,
                    "topic": "Java基础"
                  }
                ]
                """;

            log.info("测试请求: {}", request);
            log.info("测试AI响应: {}", mockAiResponse);

            List<GeneratedQuestionDTO> questions = questionBankService.parseAndSaveQuestions(mockAiResponse, request);

            log.info("测试成功，生成{}道题目", questions.size());
            return RestBean.success("测试成功", questions);

        } catch (Exception e) {
            log.error("测试失败", e);
            return RestBean.failure(500, "测试失败：" + e.getMessage());
        }
    }

    /**
     * 测试解析保存接口（模拟前端调用）
     */
    @PostMapping("/question-generator/test-parse-save")
    public RestBean<List<GeneratedQuestionDTO>> testParseSave() {
        try {
            log.info("开始测试解析保存接口");

            // 模拟前端发送的数据
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("aiResponse", """
                [
                  {
                    "type": "single",
                    "content": "什么是Java面向对象编程？",
                    "answer": "A",
                    "analysis": "Java是一种面向对象的编程语言",
                    "difficulty": 3,
                    "topic": "Java基础"
                  }
                ]
                """);

            Map<String, Object> originalRequest = new HashMap<>();
            originalRequest.put("topic", "java面向对象");
            originalRequest.put("difficulty", 3);
            originalRequest.put("count", 1);
            originalRequest.put("selectedTypes", List.of("single"));

            requestData.put("originalRequest", originalRequest);

            // 模拟HttpServletRequest
            String aiResponse = (String) requestData.get("aiResponse");
            Map<String, Object> requestMap = (Map<String, Object>) requestData.get("originalRequest");

            QuestionGenerationRequest request = new QuestionGenerationRequest();
            request.setTopic(getStringFromMap(requestMap, "topic"));
            request.setSelectedTypes(getListFromMap(requestMap, "selectedTypes"));
            request.setDifficulty(getIntegerFromMap(requestMap, "difficulty"));
            request.setCount(getIntegerFromMap(requestMap, "count"));
            request.setCreatedBy("test_user");

            List<GeneratedQuestionDTO> questions = questionBankService.parseAndSaveQuestions(aiResponse, request);

            return RestBean.success("测试解析保存成功", questions);

        } catch (Exception e) {
            log.error("测试解析保存失败", e);
            return RestBean.failure(500, "测试解析保存失败：" + e.getMessage());
        }
    }


    private String buildPrompt(String message, MultipartFile[] files) {
        StringBuilder sb = new StringBuilder(message);
        if (files != null) {
            for (MultipartFile file : files) {
                // 将上传文件嵌⼊向量库，并把⽂本加到 Prompt
                String fileText = ingestor.ingestMultipartFile(file);
                sb.append("\n\n--- 来⾃⽂件 ")
                        .append(file.getOriginalFilename())
                        .append(" ---\n")
                        .append(fileText);
            }
        }
        return sb.toString();
    }

    // ========================================
    // 教师仪表盘相关API
    // ========================================

    /**
     * 获取教师仪表盘概览数据
     */
    @GetMapping("/dashboard/summary")
    public RestBean<TeacherDashboardVO.DashboardSummary> getDashboardSummary(@RequestParam String uid) {
        try {
            Long teacherId = Long.parseLong(uid);
            TeacherDashboardVO.DashboardSummary summary = teacherDashboardService.getDashboardSummary(teacherId);
            return RestBean.success("获取概览数据成功", summary);
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        } catch (Exception e) {
            return RestBean.failure(500, "获取概览数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取通知公告列表
     */
    @GetMapping("/dashboard/announcements")
    public RestBean<List<TeacherDashboardVO.Announcement>> getAnnouncements(
            @RequestParam String uid,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            Long teacherId = Long.parseLong(uid);
            List<TeacherDashboardVO.Announcement> announcements = teacherDashboardService.getAnnouncements(teacherId, limit);
            return RestBean.success("获取通知公告成功", announcements);
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        } catch (Exception e) {
            return RestBean.failure(500, "获取通知公告失败: " + e.getMessage());
        }
    }

    /**
     * 获取待办任务列表
     */
    @GetMapping("/dashboard/pending-tasks")
    public RestBean<List<TeacherDashboardVO.Task>> getTasks(
            @RequestParam String uid,
            @RequestParam(defaultValue = "all") String status) {
        try {
            Long teacherId = Long.parseLong(uid);
            List<TeacherDashboardVO.Task> tasks = teacherDashboardService.getTasks(teacherId, status);
            return RestBean.success("获取任务列表成功", tasks);
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        } catch (Exception e) {
            return RestBean.failure(500, "获取任务列表失败: " + e.getMessage());
        }
    }

    /**
     * 创建新任务
     */
    @PostMapping("/dashboard/tasks")
    public RestBean<TeacherDashboardVO.Task> createTask(@RequestBody TeacherDashboardVO.CreateTaskRequest request) {
        try {
            TeacherDashboardVO.Task task = teacherDashboardService.createTask(request);
            return RestBean.success("创建任务成功", task);
        } catch (Exception e) {
            return RestBean.failure(500, "创建任务失败: " + e.getMessage());
        }
    }

    /**
     * 更新任务状态
     */
    @PutMapping("/dashboard/tasks/{taskId}")
    public RestBean<Void> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestBody TeacherDashboardVO.UpdateTaskRequest request) {
        try {
            teacherDashboardService.updateTaskStatus(taskId, request);
            return RestBean.success("更新任务状态成功");
        } catch (Exception e) {
            return RestBean.failure(500, "更新任务状态失败: " + e.getMessage());
        }
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/dashboard/tasks/{taskId}")
    public RestBean<Void> deleteTask(@PathVariable Long taskId) {
        try {
            teacherDashboardService.deleteTask(taskId);
            return RestBean.success("删除任务成功");
        } catch (Exception e) {
            return RestBean.failure(500, "删除任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取待批改作业列表
     */
    @GetMapping("/dashboard/pending-grading")
    public RestBean<List<TeacherDashboardVO.PendingGrading>> getPendingGrading(
            @RequestParam String uid,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            Long teacherId = Long.parseLong(uid);
            List<TeacherDashboardVO.PendingGrading> pendingList = teacherDashboardService.getPendingGrading(teacherId, limit);
            return RestBean.success("获取待批改作业成功", pendingList);
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        } catch (Exception e) {
            return RestBean.failure(500, "获取待批改作业失败: " + e.getMessage());
        }
    }

    /**
     * 获取班级快照数据
     */
    @GetMapping("/dashboard/class-snapshots")
    public RestBean<List<TeacherDashboardVO.ClassSnapshot>> getClassSnapshots(@RequestParam String uid) {
        try {
            Long teacherId = Long.parseLong(uid);
            List<TeacherDashboardVO.ClassSnapshot> snapshots = teacherDashboardService.getClassSnapshots(teacherId);
            return RestBean.success("获取班级快照成功", snapshots);
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        } catch (Exception e) {
            return RestBean.failure(500, "获取班级快照失败: " + e.getMessage());
        }
    }

    /**
     * 标记通知为已读
     */
    @PostMapping("/dashboard/announcements/{announcementId}/read")
    public RestBean<Void> markAnnouncementAsRead(
            @PathVariable Long announcementId,
            @RequestParam String uid) {
        try {
            Long teacherId = Long.parseLong(uid);
            teacherDashboardService.markAnnouncementAsRead(announcementId, teacherId);
            return RestBean.success("标记通知已读成功");
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        } catch (Exception e) {
            return RestBean.failure(500, "标记通知已读失败: " + e.getMessage());
        }
    }

    // ========================================
    // AI功能相关API
    // ========================================

    /**
     * 获取智能日程安排
     */
    @GetMapping("/dashboard/smart-schedule")
    public RestBean<List<TeacherDashboardVO.ScheduleItem>> getSmartSchedule(@RequestParam String uid) {
        try {
            Long teacherId = Long.parseLong(uid);
            List<TeacherDashboardVO.ScheduleItem> schedule = teacherAIService.generateSmartSchedule(teacherId);
            return RestBean.success("获取智能日程成功", schedule);
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        } catch (Exception e) {
            return RestBean.failure(500, "获取智能日程失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生表现亮点
     */
    @GetMapping("/dashboard/student-highlights")
    public RestBean<List<TeacherDashboardVO.StudentHighlight>> getStudentHighlights(@RequestParam String uid) {
        try {
            Long teacherId = Long.parseLong(uid);
            List<TeacherDashboardVO.StudentHighlight> highlights = teacherAIService.analyzeStudentHighlights(teacherId);
            return RestBean.success("获取学生亮点成功", highlights);
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        } catch (Exception e) {
            return RestBean.failure(500, "获取学生亮点失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生潜在风险
     */
    @GetMapping("/dashboard/student-risks")
    public RestBean<List<TeacherDashboardVO.StudentRisk>> getStudentRisks(@RequestParam String uid) {
        try {
            Long teacherId = Long.parseLong(uid);
            List<TeacherDashboardVO.StudentRisk> risks = teacherAIService.analyzeStudentRisks(teacherId);
            return RestBean.success("获取学生风险成功", risks);
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        } catch (Exception e) {
            return RestBean.failure(500, "获取学生风险失败: " + e.getMessage());
        }
    }

    /**
     * 获取互动中心数据
     */
    @GetMapping("/dashboard/interaction-hub")
    public RestBean<List<TeacherDashboardVO.InteractionItem>> getInteractionHub(@RequestParam String uid) {
        try {
            Long teacherId = Long.parseLong(uid);
            List<TeacherDashboardVO.InteractionItem> interactions = teacherAIService.getInteractionHub(teacherId);
            return RestBean.success("获取互动数据成功", interactions);
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        } catch (Exception e) {
            return RestBean.failure(500, "获取互动数据失败: " + e.getMessage());
        }
    }

    /**
     * 生成教学建议
     */
    @PostMapping("/ai/teaching-suggestions")
    public RestBean<String> generateTeachingSuggestions(
            @RequestParam String uid,
            @RequestParam Long courseId) {
        try {
            Long teacherId = Long.parseLong(uid);
            String suggestions = teacherAIService.generateTeachingSuggestions(teacherId, courseId);
            return RestBean.success("生成教学建议成功", suggestions);
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        } catch (Exception e) {
            return RestBean.failure(500, "生成教学建议失败: " + e.getMessage());
        }
    }

    /**
     * 分析班级表现
     */
    @PostMapping("/ai/class-performance")
    public RestBean<String> analyzeClassPerformance(
            @RequestParam String uid,
            @RequestParam Long courseId) {
        try {
            Long teacherId = Long.parseLong(uid);
            String analysis = teacherAIService.analyzeClassPerformance(teacherId, courseId);
            return RestBean.success("分析班级表现成功", analysis);
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        } catch (Exception e) {
            return RestBean.failure(500, "分析班级表现失败: " + e.getMessage());
        }
    }

    /**
     * 生成个性化学习建议
     */
    @PostMapping("/ai/personalized-advice")
    public RestBean<String> generatePersonalizedAdvice(
            @RequestParam String uid,
            @RequestParam Long studentId,
            @RequestParam Long courseId) {
        try {
            Long teacherId = Long.parseLong(uid);
            String advice = teacherAIService.generatePersonalizedAdvice(teacherId, studentId, courseId);
            return RestBean.success("生成个性化建议成功", advice);
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        } catch (Exception e) {
            return RestBean.failure(500, "生成个性化建议失败: " + e.getMessage());
        }
    }

    /**
     * 预测学习趋势
     */
    @PostMapping("/ai/learning-trend")
    public RestBean<String> predictLearningTrend(
            @RequestParam String uid,
            @RequestParam Long studentId,
            @RequestParam Long courseId) {
        try {
            Long teacherId = Long.parseLong(uid);
            String trend = teacherAIService.predictLearningTrend(teacherId, studentId, courseId);
            return RestBean.success("预测学习趋势成功", trend);
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        } catch (Exception e) {
            return RestBean.failure(500, "预测学习趋势失败: " + e.getMessage());
        }
    }

    /**
     * 生成作业批改建议
     */
    @PostMapping("/ai/grading-suggestions")
    public RestBean<String> generateGradingSuggestions(
            @RequestParam String uid,
            @RequestParam Long submissionId) {
        try {
            Long teacherId = Long.parseLong(uid);
            String suggestions = teacherAIService.generateGradingSuggestions(teacherId, submissionId);
            return RestBean.success("生成批改建议成功", suggestions);
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        } catch (Exception e) {
            return RestBean.failure(500, "生成批改建议失败: " + e.getMessage());
        }
    }

    // ========================================
    // 智能备课工作台API
    // ========================================

    /**
     * 保存教案
     */
    @PostMapping("/lesson-plans")
    public RestBean<Long> saveLessonPlan(@RequestBody LessonPlan lessonPlan, @RequestParam String uid) {
        try {
            Long teacherId = Long.parseLong(uid);
            lessonPlan.setTeacherId(teacherId);
            Long lessonPlanId = lessonPlanService.saveLessonPlan(lessonPlan);
            return RestBean.success("保存教案成功", lessonPlanId);
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        } catch (Exception e) {
            return RestBean.failure(500, "保存教案失败: " + e.getMessage());
        }
    }

    /**
     * 更新教案
     */
    @PostMapping("/lesson-plans/{id}/update")
    public RestBean<Void> updateLessonPlan(@PathVariable Long id, @RequestBody LessonPlan lessonPlan, @RequestParam String uid) {
        try {
            Long teacherId = Long.parseLong(uid);
            lessonPlan.setId(id);
            lessonPlan.setTeacherId(teacherId);
            boolean success = lessonPlanService.updateLessonPlan(lessonPlan);
            if (success) {
                return RestBean.success("更新教案成功");
            } else {
                return RestBean.failure(404, "教案不存在或无权限");
            }
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        } catch (Exception e) {
            return RestBean.failure(500, "更新教案失败: " + e.getMessage());
        }
    }

    /**
     * 获取教案详情
     */
    @GetMapping("/lesson-plans/{id}")
    public RestBean<LessonPlanVO> getLessonPlan(@PathVariable Long id, @RequestParam String uid) {
        try {
            Long teacherId = Long.parseLong(uid);
            LessonPlanVO lessonPlan = lessonPlanService.getLessonPlanById(id, teacherId);
            if (lessonPlan != null) {
                return RestBean.success("获取教案成功", lessonPlan);
            } else {
                return RestBean.failure(404, "教案不存在");
            }
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        } catch (Exception e) {
            return RestBean.failure(500, "获取教案失败: " + e.getMessage());
        }
    }

    /**
     * 获取教案列表
     */
    @GetMapping("/lesson-plans")
    public RestBean<Map<String, Object>> getLessonPlans(
            @RequestParam String uid,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "") String subject,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Long teacherId = Long.parseLong(uid);
            List<LessonPlanVO> lessonPlans = lessonPlanService.getLessonPlansByTeacher(teacherId, status, subject, page, size);
            int total = lessonPlanService.countLessonPlansByTeacher(teacherId, status, subject);

            Map<String, Object> result = Map.of(
                "list", lessonPlans,
                "total", total,
                "page", page,
                "size", size
            );
            return RestBean.success("获取教案列表成功", result);
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        } catch (Exception e) {
            return RestBean.failure(500, "获取教案列表失败: " + e.getMessage());
        }
    }

    /**
     * 删除教案
     */
    @PostMapping("/lesson-plans/{id}/delete")
    public RestBean<Void> deleteLessonPlan(@PathVariable Long id, @RequestParam String uid) {
        try {
            Long teacherId = Long.parseLong(uid);
            boolean success = lessonPlanService.deleteLessonPlan(id, teacherId);
            if (success) {
                return RestBean.success("删除教案成功");
            } else {
                return RestBean.failure(404, "教案不存在或无权限");
            }
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        } catch (Exception e) {
            return RestBean.failure(500, "删除教案失败: " + e.getMessage());
        }
    }

    /**
     * 获取可用模板列表
     */
    @GetMapping("/lesson-templates")
    public RestBean<List<LessonTemplate>> getTemplates(@RequestParam String uid) {
        try {
            Long teacherId = Long.parseLong(uid);
            List<LessonTemplate> templates = lessonPlanService.getAvailableTemplates(teacherId);
            return RestBean.success("获取模板列表成功", templates);
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        } catch (Exception e) {
            return RestBean.failure(500, "获取模板列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据类型获取模板
     */
    @GetMapping("/lesson-templates/{templateType}")
    public RestBean<LessonTemplate> getTemplateByType(@PathVariable String templateType, @RequestParam String uid) {
        try {
            Long teacherId = Long.parseLong(uid);
            LessonTemplate template = lessonPlanService.getTemplateByType(templateType, teacherId);
            if (template != null) {
                return RestBean.success("获取模板成功", template);
            } else {
                return RestBean.failure(404, "模板不存在");
            }
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        } catch (Exception e) {
            return RestBean.failure(500, "获取模板失败: " + e.getMessage());
        }
    }

    /**
     * 保存自定义模板
     */
    @PostMapping("/lesson-templates")
    public RestBean<Long> saveTemplate(@RequestBody LessonTemplate template, @RequestParam String uid) {
        try {
            Long teacherId = Long.parseLong(uid);
            template.setTeacherId(teacherId);
            Long templateId = lessonPlanService.saveTemplate(template);
            return RestBean.success("保存模板成功", templateId);
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        } catch (Exception e) {
            return RestBean.failure(500, "保存模板失败: " + e.getMessage());
        }
    }

    /**
     * 获取推荐的教学资源
     */
    @GetMapping("/teaching-resources/recommend")
    public RestBean<List<TeachingResource>> getRecommendedResources(
            @RequestParam(defaultValue = "") String subject,
            @RequestParam(defaultValue = "") String grade,
            @RequestParam(defaultValue = "") String resourceType,
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<TeachingResource> resources = lessonPlanService.getRecommendedResources(subject, grade, resourceType, limit);
            return RestBean.success("获取推荐资源成功", resources);
        } catch (Exception e) {
            return RestBean.failure(500, "获取推荐资源失败: " + e.getMessage());
        }
    }

    // ==================== 考试管理相关接口 ====================
    @GetMapping("/exams")
    public RestBean<List<ExamListVO>> getTeacherExams(
            @RequestParam String uid,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            Long teacherId = Long.parseLong(uid);
            List<ExamListVO> exams = examManagementService.getTeacherExams(teacherId, status, keyword, page, size);
            Integer total = examManagementService.countTeacherExams(teacherId, status, keyword);

            // 可以考虑返回分页信息
            return RestBean.success("获取考试列表成功", exams);
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        } catch (Exception e) {
            return RestBean.failure(500, "获取考试列表失败: " + e.getMessage());
        }
    }
    /**
     * 获取教师的考试列表
     */
//    @GetMapping("/exams")
//    public RestBean<List<ExamVO>> getTeacherExams(@RequestParam String uid) {
//        try {
//            Long teacherId = Long.parseLong(uid);
//            List<ExamVO> exams = examManagementService.getTeacherExams(teacherId);
//            return RestBean.success("获取考试列表成功", exams);
//        } catch (NumberFormatException e) {
//            return RestBean.failure(400, "无效的用户ID格式");
//        } catch (Exception e) {
//            return RestBean.failure(500, "获取考试列表失败: " + e.getMessage());
//        }
//    }

    /**
     * 获取题目列表供选择
     */
    @GetMapping("/exams/questions")
    public RestBean<List<QuestionSelectionVO>> getQuestionsForSelection(
            @RequestParam String uid,
            @RequestParam String subject,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        try {
            if (subject == null || subject.trim().isEmpty()) {
                return RestBean.failure(400, "科目参数不能为空");
            }

            Long teacherId = Long.parseLong(uid);
            List<QuestionSelectionVO> questions = examManagementService.getQuestionsForSelection(teacherId, subject, page, size);
            Integer total = examManagementService.countQuestions(teacherId, subject.trim());

            return RestBean.success("获取题目列表成功", questions);
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        } catch (Exception e) {
            return RestBean.failure(500, "获取题目列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取教师创建的题目的所有学科列表
     */
    @GetMapping("/exams/question-subjects")
    public RestBean<List<String>> getQuestionSubjects(@RequestParam String uid) {
        try {
            Long teacherId = Long.parseLong(uid);
            List<String> subjects = examManagementService.getQuestionSubjects(teacherId);
            return RestBean.success("获取学科列表成功", subjects);
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        } catch (Exception e) {
            return RestBean.failure(500, "获取学科列表失败: " + e.getMessage());
        }
    }



    /**
     * 获取考试详情
     */
    @GetMapping("/exams/{examId}")
    public RestBean<ExamVO> getExamDetail(
            @PathVariable Long examId,
            @RequestParam String uid) {
        try {
            Long teacherId = Long.parseLong(uid);
            ExamVO examDetail = examManagementService.getExamDetail(examId, teacherId);

            if (examDetail == null) {
                return RestBean.failure(404, "考试不存在或无权限访问");
            }

            return RestBean.success("获取考试详情成功", examDetail);
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        } catch (Exception e) {
            return RestBean.failure(500, "获取考试详情失败: " + e.getMessage());
        }
    }

    /**
     * 删除考试
     */
    @PostMapping("/exams/{examId}/delete")
    public RestBean<String> deleteExam(
            @PathVariable Long examId,
            @RequestParam String uid) {
        try {
            Long teacherId = Long.parseLong(uid);
            Boolean result = examManagementService.deleteExam(examId, teacherId);

            if (result) {
                return RestBean.success("删除考试成功");
            } else {
                return RestBean.failure(400, "删除考试失败，考试不存在或无权限");
            }
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        } catch (Exception e) {
            return RestBean.failure(500, "删除考试失败: " + e.getMessage());
        }
    }

    /**
     * 获取教师的考试列表
     */


    /**
     * 获取教师所教的课程和班级
     */
    @GetMapping("/exams/courses-classes")
    public RestBean<List<CourseClassVO>> getTeacherCoursesAndClasses(@RequestParam String uid) {
        try {
            Long teacherId = Long.parseLong(uid);
            List<CourseClassVO> coursesAndClasses = examManagementService.getTeacherCoursesAndClasses(teacherId);
            return RestBean.success("获取课程班级列表成功", coursesAndClasses);
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        } catch (Exception e) {
            return RestBean.failure(500, "获取课程班级列表失败: " + e.getMessage());
        }
    }

    /**
     * 创建考试
     */
    @PostMapping("/exams")
    public RestBean<Long> createExam(@RequestBody @Valid ExamCreateRequest request, @RequestParam String uid) {
        try {
            Long teacherId = Long.parseLong(uid);
            Long examId = examManagementService.createExam(request, teacherId);
            return RestBean.success("创建考试成功", examId);
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        } catch (Exception e) {
            return RestBean.failure(500, "创建考试失败: " + e.getMessage());
        }
    }


}
