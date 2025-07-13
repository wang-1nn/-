package org.example.backend.service;

import org.example.backend.entity.dto.ExamDTO;
import org.example.backend.entity.dto.ExamDetailDTO;
import org.example.backend.entity.dto.SubmissionDTO;
import org.example.backend.entity.pojo.Answer;
import org.example.backend.entity.pojo.Question;

import java.util.List;
import java.util.Map;

/**
 * 考试服务接口
 */
public interface ExamService {
    
    /**
     * 获取学生可参加的考试列表
     *
     * @param studentId 学生ID
     * @return 考试列表
     */
    List<ExamDTO> getStudentExams(Long studentId);
    
    /**
     * 获取考试详情，包含题目列表
     *
     * @param examId 考试ID
     * @param studentId 学生ID
     * @return 考试详情
     */
    ExamDetailDTO getExamDetail(Long examId, Long studentId);
    
    /**
     * 开始考试，创建提交记录
     *
     * @param examId 考试ID
     * @param studentId 学生ID
     * @return 提交记录ID
     */
    Long startExam(Long examId, Long studentId);
    
    /**
     * 保存学生答案
     *
     * @param submissionId 提交记录ID
     * @param questionId 题目ID
     * @param answer 答案
     * @return 是否保存成功
     */
    boolean saveAnswer(Long submissionId, Long questionId, String answer);
    
    /**
     * 提交考试
     *
     * @param submissionId 提交记录ID
     * @return 是否提交成功
     */
    boolean submitExam(Long submissionId);
    
    /**
     * 获取学生的成绩列表
     *
     * @param studentId 学生ID
     * @return 成绩列表
     */
    List<SubmissionDTO> getStudentScores(Long studentId);
    
    /**
     * 获取成绩详情
     *
     * @param submissionId 提交记录ID
     * @return 成绩详情
     */
    Map<String, Object> getScoreDetail(Long submissionId);
    
    /**
     * 获取考试的题目列表
     *
     * @param examId 考试ID
     * @return 题目列表
     */
    List<Question> getExamQuestions(Long examId);
    
    /**
     * 获取学生在某次考试中的答案列表
     *
     * @param submissionId 提交记录ID
     * @return 答案列表
     */
    List<Answer> getStudentAnswers(Long submissionId);
    
    /**
     * 获取学生的考试统计数据
     *
     * @param studentId 学生ID
     * @return 统计数据，包含总考试数、已完成数、平均分、最高分、最低分、及格率等
     */
    Map<String, Object> getStudentExamStats(Long studentId);
    
    /**
     * 获取学生的成绩趋势数据
     *
     * @param studentId 学生ID
     * @param limit 限制返回的记录数，默认为最近10次考试
     * @return 成绩趋势数据，包含考试日期、分数等
     */
    List<Map<String, Object>> getStudentScoreTrend(Long studentId, Integer limit);
}