package org.example.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.backend.entity.pojo.Answer;
import org.example.backend.entity.pojo.Exam;
import org.example.backend.entity.pojo.Question;
import org.example.backend.entity.pojo.Submission;

import java.util.List;
import java.util.Map;

/**
 * 考试相关数据库操作接口
 */
@Mapper
public interface ExamMapper {
    
    /**
     * 获取学生可参加的考试列表
     *
     * @param studentId 学生ID
     * @return 考试列表
     */
    List<Exam> findExamsByStudentId(Long studentId);
    
    /**
     * 获取考试详情
     *
     * @param examId 考试ID
     * @return 考试详情
     */
    Exam findExamById(Long examId);
    
    /**
     * 获取考试的题目列表
     *
     * @param examId 考试ID
     * @return 题目列表
     */
    List<Question> findQuestionsByExamId(Long examId);
    
    /**
     * 获取学生的考试提交记录
     *
     * @param studentId 学生ID
     * @param examId 考试ID
     * @return 提交记录
     */
    Submission findSubmissionByStudentAndExam(@Param("studentId") Long studentId, @Param("examId") Long examId);
    
    /**
     * 创建考试提交记录
     *
     * @param submission 提交记录
     * @return 影响行数
     */
    int createSubmission(Submission submission);
    
    /**
     * 更新考试提交记录
     *
     * @param submission 提交记录
     * @return 影响行数
     */
    int updateSubmission(Submission submission);
    
    /**
     * 获取学生的答案列表
     *
     * @param submissionId 提交记录ID
     * @return 答案列表
     */
    List<Answer> findAnswersBySubmissionId(Long submissionId);
    
    /**
     * 保存学生答案
     *
     * @param answer 答案
     * @return 影响行数
     */
    int saveAnswer(Answer answer);
    
    /**
     * 更新学生答案
     *
     * @param answer 答案
     * @return 影响行数
     */
    int updateAnswer(Answer answer);
    
    /**
     * 获取学生的成绩列表
     *
     * @param studentId 学生ID
     * @return 成绩列表
     */
    List<Map<String, Object>> findScoresByStudentId(Long studentId);
    
    /**
     * 获取学生的成绩详情
     *
     * @param submissionId 提交记录ID
     * @return 成绩详情
     */
    Map<String, Object> findScoreDetailBySubmissionId(Long submissionId);
    
    /**
     * 获取学生正在进行中的考试
     *
     * @param studentId 学生ID
     * @return 考试列表
     */
    List<Exam> findOngoingExamsByStudentId(Long studentId);
    
    /**
     * 获取学生已完成的考试成绩，按提交时间排序
     *
     * @param studentId 学生ID
     * @param limit 限制返回的记录数
     * @return 成绩列表
     */
    List<Map<String, Object>> findCompletedScoresByStudentId(@Param("studentId") Long studentId, @Param("limit") int limit);
} 