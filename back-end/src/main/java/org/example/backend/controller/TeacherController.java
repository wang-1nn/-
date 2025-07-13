package org.example.backend.controller;


import org.example.backend.entity.pojo.AiChatMemory;
import org.example.backend.entity.pojo.QGRequest;
import org.example.backend.entity.pojo.Question;
import org.example.backend.entity.pojo.QuestionBatch;
import org.example.backend.entity.vo.*;
import org.example.backend.service.TeacherService;
import org.example.backend.service.impl.ai.TeacherAi.CreateLesson;
import org.example.backend.service.impl.ai.TeacherAi.OptimizateOutline;
import org.example.backend.service.impl.ai.TeacherAi.QuestionAiService;
import org.example.backend.util.Ingestor;
import org.example.backend.util.RestBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/teacher")

public class TeacherController {



    @Autowired
    TeacherService teacherService;

    @Autowired
    CreateLesson createLesson;

    @Autowired
    Ingestor ingestor;
    @Autowired
    OptimizateOutline optimizateOutline;

    @Autowired
    QuestionAiService questionAiService;

    private int getTeacherIdFromUid(String uid) {
        // In a real application, you would get the user ID from the security context (e.g., JWT token)
        // For now, we parse it from the request parameter.
        return Integer.parseInt(uid);
    }

    @GetMapping("/dashboard/summary")
    public RestBean<DashboardSummaryVO> getSummary(@RequestParam String uid) {
        try {
            return RestBean.success(teacherService.getSummary(getTeacherIdFromUid(uid)));
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        }
    }

    @GetMapping("/dashboard/announcements")
    public RestBean<List<AnnouncementVO>> getAnnouncements(@RequestParam String uid) {
        try {
            return RestBean.success(teacherService.getAnnouncements(getTeacherIdFromUid(uid)));
        } catch (NumberFormatException e) {
            return RestBean.failure(400, "无效的用户ID格式");
        }
    }

    @GetMapping("/dashboard/pending-tasks")
    public RestBean<List<PendingTaskVO>> getPendingTasks(@RequestParam String uid) {
        try {
            return RestBean.success(teacherService.getPendingTasks(getTeacherIdFromUid(uid)));
        } catch (NumberFormatException e) {
            return RestBean.success(Collections.emptyList());
        }
    }

    @PostMapping("/dashboard/tasks")
    public RestBean<PendingTaskVO> createTask(@RequestBody NewTaskVO task) {
        try {
            PendingTaskVO createdTask = teacherService.createTask(task);
            return RestBean.success(createdTask);
        } catch (Exception e) {
            return RestBean.failure(400, e.getMessage());
        }
    }

    @PutMapping("/dashboard/tasks/{taskId}")
    public RestBean<Void> updateTaskStatus(@PathVariable long taskId, @RequestBody Map<String, Boolean> payload) {
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
}
