package org.example.backend.mapper;

import org.apache.ibatis.annotations.*;
import org.example.backend.entity.pojo.AdaptiveExam;
import org.example.backend.entity.pojo.AdaptiveQuestion;
import org.example.backend.entity.pojo.AdaptiveExamResult;

import java.util.List;

@Mapper
public interface AdaptiveExamMapper {
    
    // 插入新的自适应测试
    @Insert("INSERT INTO adaptive_exams(user_id, subject, difficulty, starting_difficulty, question_count, " +
            "current_question, time_limit, topics, status, created_at) " +
            "VALUES(#{userId}, #{subject}, #{difficulty}, #{startingDifficulty}, #{questionCount}, " +
            "#{currentQuestion}, #{timeLimit}, #{topics}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertExam(AdaptiveExam exam);
    
    // 更新自适应测试状态
    @Update("UPDATE adaptive_exams SET status = #{status}, difficulty = #{difficulty}, " +
            "current_question = #{currentQuestion}, score = #{score}, ability_level = #{abilityLevel}, " +
            "start_time = #{startTime}, end_time = #{endTime} WHERE id = #{id}")
    void updateExam(AdaptiveExam exam);
    
    // 根据ID查询自适应测试
    @Select("SELECT * FROM adaptive_exams WHERE id = #{id}")
    AdaptiveExam findById(Long id);
    
    // 根据用户ID查询自适应测试列表
    @Select("SELECT * FROM adaptive_exams WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<AdaptiveExam> findByUserId(Long userId);
    
    // 插入测试题目
    @Insert("INSERT INTO adaptive_questions(exam_id, question_type, content, options, answer, topic, difficulty, " +
            "score, created_at) VALUES(#{examId}, #{questionType}, #{content}, #{options}, #{answer}, #{topic}, " +
            "#{difficulty}, #{score}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertQuestion(AdaptiveQuestion question);
    
    // 更新题目作答情况
    @Update("UPDATE adaptive_questions SET user_answer = #{userAnswer}, is_correct = #{isCorrect}, " +
            "answered_at = NOW() WHERE id = #{id}")
    void updateQuestionAnswer(AdaptiveQuestion question);
    
    // 根据测试ID查询题目列表
    @Select("SELECT * FROM adaptive_questions WHERE exam_id = #{examId} ORDER BY id")
    List<AdaptiveQuestion> findQuestionsByExamId(Long examId);
    
    // 根据ID查询题目
    @Select("SELECT * FROM adaptive_questions WHERE id = #{id}")
    AdaptiveQuestion findQuestionById(Long id);
    
    // 查找下一个预生成的题目
    @Select("SELECT * FROM adaptive_questions WHERE exam_id = #{examId} AND user_answer IS NULL " +
            "ORDER BY id LIMIT 1 OFFSET #{questionIndex}")
    AdaptiveQuestion findNextGeneratedQuestion(Long examId, Integer questionIndex);
    
    // 插入测试结果
    @Insert("INSERT INTO adaptive_exam_results(exam_id, user_id, score, ability_level, total_questions, " +
            "correct_count, topic_scores, strengths, weaknesses, analysis, recommendations, created_at) " +
            "VALUES(#{examId}, #{userId}, #{score}, #{abilityLevel}, #{totalQuestions}, #{correctCount}, " +
            "#{topicScores}, #{strengths}, #{weaknesses}, #{analysis}, #{recommendations}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertResult(AdaptiveExamResult result);
    
    // 根据测试ID查询结果
    @Select("SELECT * FROM adaptive_exam_results WHERE exam_id = #{examId}")
    AdaptiveExamResult findResultByExamId(Long examId);
} 