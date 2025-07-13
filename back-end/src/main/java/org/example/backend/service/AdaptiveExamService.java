package org.example.backend.service;

import org.example.backend.entity.dto.*;

import java.util.List;
import java.util.Map;

/**
 * 自适应测试服务接口
 */
public interface AdaptiveExamService {

    /**
     * 获取所有可用科目
     * @return 科目列表
     */
    List<Map<String, Object>> getAllSubjects();
    
    /**
     * 创建新的自适应测试
     * @param request 测试创建请求
     * @return 测试DTO
     */
    AdaptiveExamDTO createExam(ExamCreateRequestDTO request);
    
    /**
     * 获取测试信息
     * @param examId 测试ID
     * @return 测试DTO
     */
    AdaptiveExamDTO getExamById(Long examId);
    
    /**
     * 开始测试
     * @param examId 测试ID
     * @return 测试DTO
     */
    AdaptiveExamDTO startExam(Long examId);
    
    /**
     * 获取下一题
     * @param examId 测试ID
     * @param lastAnswerId 上一题ID，首题为null
     * @return 题目DTO
     */
    QuestionDTO getNextQuestion(Long examId, Long lastAnswerId);
    
    /**
     * 预加载下一题（不更新考试状态）
     * @param examId 测试ID
     * @return 题目DTO
     */
    QuestionDTO preloadNextQuestion(Long examId);
    
    /**
     * 提交答案
     * @param examId 测试ID
     * @param questionId 题目ID
     * @param answer 答案提交DTO
     * @return 答案结果DTO
     */
    AnswerResultDTO submitAnswer(Long examId, Long questionId, AnswerSubmitDTO answer);
    
    /**
     * 完成测试
     * @param examId 测试ID
     * @return 测试结果DTO
     */
    Map<String, Object> completeExam(Long examId);
    
    /**
     * 获取学生的测试历史
     * @param userId 学生ID
     * @return 测试列表
     */
    List<AdaptiveExamDTO> getExamHistoryByUserId(Long userId);
    
    /**
     * 获取测试详细结果
     * @param examId 测试ID
     * @return 详细结果
     */
    Map<String, Object> getExamResult(Long examId);
    
    /**
     * 获取测试的所有题目
     * @param examId 测试ID
     * @return 题目列表
     */
    List<QuestionDTO> getExamQuestions(Long examId);
} 