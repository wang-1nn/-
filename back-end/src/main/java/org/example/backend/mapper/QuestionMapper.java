package org.example.backend.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.example.backend.entity.pojo.Question;

import java.util.List;

/**
 * Mapper for questions table.
 */
@Mapper
public interface QuestionMapper {

    @Insert("""
            INSERT INTO questions(question_id, exam_id, question_type, content, options, answer, analysis, score, difficulty, created_by, is_ai_generated, created_at)
            VALUES(#{questionId}, #{examId}, #{questionType}, #{content}, #{options}, #{answer}, #{analysis}, #{score}, #{difficulty}, #{createdBy}, #{isAiGenerated}, NOW())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertQuestion(Question question);

    @Select("SELECT * FROM questions ORDER BY created_at DESC")
    List<Question> listAll();

    @Select("SELECT * from questions where question_id = #{questionId}")
    List<Question>findQuestionById(String questionId);

    @Delete("DELETE FROM questions WHERE id = #{id}")
    int deleteQuestionById(long id);


    @Select("SELECT count(*)  FROM questions GROUP BY question_id")
    List<Integer> getQuestionCount();
} 