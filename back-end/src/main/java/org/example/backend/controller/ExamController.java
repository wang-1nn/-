package org.example.backend.controller;

import jakarta.annotation.Resource;
import org.example.backend.entity.dto.ExamDTO;
import org.example.backend.entity.dto.ExamDetailDTO;
import org.example.backend.entity.dto.SubmissionDTO;
import org.example.backend.entity.pojo.Answer;
import org.example.backend.service.ExamService;
import org.example.backend.util.RestBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 考试相关接口控制器
 */
@RestController
@RequestMapping("/api/student/exams")
public class ExamController {
    
    private static final Logger logger = LoggerFactory.getLogger(ExamController.class);
    
    @Resource
    private ExamService examService;
    
    /**
     * 获取学生可参加的考试列表
     *
     * @param studentId 学生ID
     * @return 考试列表
     */
    @GetMapping("/list")
    public RestBean<List<ExamDTO>> getStudentExams(@RequestParam Long studentId) {
        logger.info("获取学生可参加的考试列表, studentId={}", studentId);
        List<ExamDTO> exams = examService.getStudentExams(studentId);
        return RestBean.success("获取考试列表成功", exams);
    }
    
    /**
     * 获取考试详情，包含题目列表
     *
     * @param examId 考试ID
     * @param studentId 学生ID
     * @return 考试详情
     */
    @GetMapping("/{examId}")
    public RestBean<ExamDetailDTO> getExamDetail(@PathVariable Long examId, @RequestParam Long studentId) {
        logger.info("获取考试详情, examId={}, studentId={}", examId, studentId);
        ExamDetailDTO exam = examService.getExamDetail(examId, studentId);
        if (exam != null) {
            return RestBean.success("获取考试详情成功", exam);
        } else {
            return RestBean.failure(404, "考试不存在或无权访问");
        }
    }
    
    /**
     * 开始考试，创建提交记录
     *
     * @param examId 考试ID
     * @param studentId 学生ID
     * @return 提交记录ID
     */
    @PostMapping("/{examId}/start")
    public RestBean<Long> startExam(@PathVariable Long examId, @RequestParam Long studentId) {
        logger.info("开始考试, examId={}, studentId={}", examId, studentId);
        Long submissionId = examService.startExam(examId, studentId);
        if (submissionId != null) {
            return RestBean.success("开始考试成功", submissionId);
        } else {
            return RestBean.failure(400, "无法开始考试，可能考试不在进行中或已结束");
        }
    }
    
    /**
     * 保存学生答案
     *
     * @param submissionId 提交记录ID
     * @param questionId 题目ID
     * @param answer 答案
     * @return 操作结果
     */
    @PostMapping("/submissions/{submissionId}/answers")
    public RestBean<Boolean> saveAnswer(
            @PathVariable Long submissionId,
            @RequestParam Long questionId,
            @RequestParam String answer) {
        logger.info("保存学生答案, submissionId={}, questionId={}", submissionId, questionId);
        boolean success = examService.saveAnswer(submissionId, questionId, answer);
        if (success) {
            return RestBean.success("保存答案成功", true);
        } else {
            return RestBean.failure(500, "保存答案失败");
        }
    }
    
    /**
     * 提交考试
     *
     * @param submissionId 提交记录ID
     * @return 操作结果
     */
    @PostMapping("/submissions/{submissionId}/submit")
    public RestBean<Boolean> submitExam(@PathVariable Long submissionId) {
        logger.info("提交考试, submissionId={}", submissionId);
        boolean success = examService.submitExam(submissionId);
        if (success) {
            return RestBean.success("提交考试成功", true);
        } else {
            return RestBean.failure(500, "提交考试失败");
        }
    }
    
    /**
     * 获取学生的成绩列表
     *
     * @param studentId 学生ID
     * @return 成绩列表
     */
    @GetMapping("/scores")
    public RestBean<List<SubmissionDTO>> getStudentScores(@RequestParam Long studentId) {
        logger.info("获取学生的成绩列表, studentId={}", studentId);
        List<SubmissionDTO> scores = examService.getStudentScores(studentId);
        return RestBean.success("获取成绩列表成功", scores);
    }
    
    /**
     * 获取成绩详情
     *
     * @param submissionId 提交记录ID
     * @return 成绩详情
     */
    @GetMapping("/scores/{submissionId}")
    public RestBean<Map<String, Object>> getScoreDetail(@PathVariable Long submissionId) {
        logger.info("获取成绩详情, submissionId={}", submissionId);
        Map<String, Object> scoreDetail = examService.getScoreDetail(submissionId);
        if (!scoreDetail.isEmpty()) {
            return RestBean.success("获取成绩详情成功", scoreDetail);
        } else {
            return RestBean.failure(404, "未找到成绩详情");
        }
    }
    
    /**
     * 获取学生在某次考试中的答案列表
     *
     * @param submissionId 提交记录ID
     * @return 答案列表
     */
    @GetMapping("/submissions/{submissionId}/answers")
    public RestBean<List<Answer>> getStudentAnswers(@PathVariable Long submissionId) {
        logger.info("获取学生在某次考试中的答案列表, submissionId={}", submissionId);
        List<Answer> answers = examService.getStudentAnswers(submissionId);
        return RestBean.success("获取答案列表成功", answers);
    }
    
    /**
     * 获取学生的考试统计数据
     *
     * @param studentId 学生ID
     * @return 统计数据
     */
    @GetMapping("/stats")
    public RestBean<Map<String, Object>> getStudentExamStats(@RequestParam Long studentId) {
        logger.info("获取学生的考试统计数据, studentId={}", studentId);
        Map<String, Object> stats = examService.getStudentExamStats(studentId);
        return RestBean.success("获取统计数据成功", stats);
    }
    
    /**
     * 获取学生的成绩趋势数据
     *
     * @param studentId 学生ID
     * @param limit 限制返回的记录数，默认为最近10次考试
     * @return 成绩趋势数据
     */
    @GetMapping("/score-trend")
    public RestBean<List<Map<String, Object>>> getStudentScoreTrend(
            @RequestParam Long studentId,
            @RequestParam(required = false) Integer limit) {
        logger.info("获取学生的成绩趋势数据, studentId={}, limit={}", studentId, limit);
        List<Map<String, Object>> trend = examService.getStudentScoreTrend(studentId, limit);
        return RestBean.success("获取成绩趋势数据成功", trend);
    }
} 