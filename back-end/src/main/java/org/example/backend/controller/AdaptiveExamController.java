package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.dto.AnswerResultDTO;
import org.example.backend.entity.dto.AnswerSubmitDTO;
import org.example.backend.entity.dto.ExamCreateRequestDTO;
import org.example.backend.entity.dto.QuestionDTO;
import org.example.backend.service.AdaptiveExamService;
import org.example.backend.util.RestBean;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自适应测试控制器
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdaptiveExamController {
    
    private static final Logger log = LoggerFactory.getLogger(AdaptiveExamController.class);
    
    private final AdaptiveExamService adaptiveExamService;
    
    /**
     * 获取所有可用科目
     */
    @GetMapping("/adaptive-exams/subjects")
    public RestBean<List<Map<String, Object>>> getAllSubjects() {
        try {
            List<Map<String, Object>> subjects = adaptiveExamService.getAllSubjects();
            return RestBean.success("获取科目列表成功", subjects);
        } catch (Exception e) {
            return RestBean.failure(500, "获取科目列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建新的自适应测试
     */
    @PostMapping("/adaptive-exams")
    public RestBean<Map<String, Object>> createExam(@RequestBody ExamCreateRequestDTO request) {
        try {
            // 假设已登录，获取当前用户ID
            // 实际情况下，应该通过认证系统获取，这里简化处理
            Long userId = 1L;  // 模拟用户ID
            
            var exam = adaptiveExamService.createExam(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", exam.getId());
            response.put("subject", exam.getSubject());
            
            // 如果有生成的题目，也返回
            if (exam.getQuestions() != null && !exam.getQuestions().isEmpty()) {
                response.put("questions", exam.getQuestions());
                response.put("questionCount", exam.getQuestions().size());
            }
            
            return RestBean.success("创建测试成功", response);
        } catch (Exception e) {
            return RestBean.failure(500, "创建测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取测试信息
     */
    @GetMapping("/adaptive-exams/{examId}")
    public RestBean<Map<String, Object>> getExamInfo(@PathVariable Long examId) {
        try {
            var exam = adaptiveExamService.getExamById(examId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", exam.getId());
            response.put("subject", exam.getSubject());
            response.put("questionCount", exam.getQuestionCount());
            response.put("currentQuestion", exam.getCurrentQuestion());
            response.put("timeLimit", exam.getTimeLimit());
            response.put("status", exam.getStatus());
            response.put("startTime", exam.getStartTime());
            
            // 获取题目列表
            if (exam.getQuestions() != null && !exam.getQuestions().isEmpty()) {
                response.put("questions", exam.getQuestions());
            } else {
                // 如果测试已经开始但没有题目数据，尝试获取题目
                if ("IN_PROGRESS".equals(exam.getStatus())) {
                    List<QuestionDTO> questions = adaptiveExamService.getExamQuestions(examId);
                    if (questions != null && !questions.isEmpty()) {
                        response.put("questions", questions);
                    }
                }
            }
            
            return RestBean.success("获取测试信息成功", response);
        } catch (Exception e) {
            return RestBean.failure(500, "获取测试信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 开始测试
     */
    @PostMapping("/adaptive-exams/{examId}/start")
    public RestBean<Map<String, Object>> startExam(@PathVariable Long examId) {
        try {
            var exam = adaptiveExamService.startExam(examId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", exam.getId());
            response.put("status", exam.getStatus());
            response.put("startTime", exam.getStartTime());
            
            return RestBean.success("开始测试成功", response);
        } catch (Exception e) {
            return RestBean.failure(500, "开始测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取下一题
     */
    @GetMapping("/adaptive-exams/{examId}/next-question")
    public RestBean<QuestionDTO> getNextQuestion(
            @PathVariable Long examId,
            @RequestParam(required = false) Long lastQuestionId,
            @RequestParam(required = false, defaultValue = "false") Boolean preload) {
        try {
            QuestionDTO question;
            
            if (preload) {
                // 预加载模式，不更新考试状态
                question = adaptiveExamService.preloadNextQuestion(examId);
            } else {
                // 正常模式，更新考试状态
                question = adaptiveExamService.getNextQuestion(examId, lastQuestionId);
            }
            
            return RestBean.success("获取题目成功", question);
        } catch (Exception e) {
            return RestBean.failure(500, "获取题目失败: " + e.getMessage());
        }
    }
    
    /**
     * 提交答案
     */
    @PostMapping("/adaptive-exams/{examId}/questions/{questionId}/submit")
    public RestBean<AnswerResultDTO> submitAnswer(
            @PathVariable Long examId,
            @PathVariable Long questionId,
            @RequestBody AnswerSubmitDTO answer) {
        try {
            log.info("提交答案: examId={}, questionId={}, answer={}", examId, questionId, answer.getAnswer());
            AnswerResultDTO result = adaptiveExamService.submitAnswer(examId, questionId, answer);
            log.info("答案评估结果: isCorrect={}, score={}", result.getIsCorrect(), result.getScore());
            return RestBean.success("提交答案成功", result);
        } catch (Exception e) {
            log.error("提交答案失败: {}", e.getMessage(), e);
            return RestBean.failure(500, "提交答案失败: " + e.getMessage());
        }
    }
    
    /**
     * 完成测试
     */
    @PostMapping("/adaptive-exams/{examId}/complete")
    public RestBean<Map<String, Object>> completeExam(@PathVariable Long examId) {
        try {
            Map<String, Object> result = adaptiveExamService.completeExam(examId);
            return RestBean.success("完成测试成功", result);
        } catch (Exception e) {
            return RestBean.failure(500, "完成测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取测试结果
     */
    @GetMapping("/adaptive-exams/{examId}/result")
    public RestBean<Map<String, Object>> getExamResult(@PathVariable Long examId) {
        try {
            Map<String, Object> result = adaptiveExamService.getExamResult(examId);
            return RestBean.success("获取测试结果成功", result);
        } catch (Exception e) {
            return RestBean.failure(500, "获取测试结果失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取测试历史
     */
    @GetMapping("/adaptive-exams/history")
    public RestBean<List<Map<String, Object>>> getExamHistory() {
        try {
            // 假设已登录，获取当前用户ID
            Long userId = 1L;  // 模拟用户ID
            
            var examList = adaptiveExamService.getExamHistoryByUserId(userId);
            
            // 转换为前端友好的格式
            List<Map<String, Object>> response = examList.stream()
                    .map(exam -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", exam.getId());
                        item.put("subject", exam.getSubject());
                        item.put("questionCount", exam.getQuestionCount());
                        item.put("status", exam.getStatus());
                        item.put("score", exam.getScore());
                        item.put("startTime", exam.getStartTime());
                        item.put("endTime", exam.getEndTime());
                        return item;
                    })
                    .toList();
            
            return RestBean.success("获取测试历史成功", response);
        } catch (Exception e) {
            return RestBean.failure(500, "获取测试历史失败: " + e.getMessage());
        }
    }
} 