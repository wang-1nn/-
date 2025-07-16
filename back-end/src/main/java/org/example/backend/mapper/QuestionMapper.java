package org.example.backend.mapper;


import org.apache.ibatis.annotations.*;
import org.example.backend.entity.pojo.Question;

import java.util.List;

/**
 * Mapper for questions table.
 */
@Mapper
public interface QuestionMapper {

    @Insert("""
            INSERT INTO questions(question_id, exam_id, question_type, content, options, answer, analysis, score, difficulty, subject, created_by, is_ai_generated, created_at)
            VALUES(#{questionId}, #{examId}, #{questionType}, #{content}, #{options}, #{answer}, #{analysis}, #{score}, #{difficulty}, #{subject}, #{createdBy}, #{isAiGenerated}, NOW())
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

    /**
     * 根据题型查询题目
     */
    @Select("SELECT * FROM questions WHERE question_type = #{questionType} ORDER BY created_at DESC")
    List<Question> findQuestionsByType(String questionType);

    /**
     * 根据难度查询题目
     */
    @Select("SELECT * FROM questions WHERE difficulty = #{difficulty} ORDER BY created_at DESC")
    List<Question> findQuestionsByDifficulty(Integer difficulty);

    /**
     * 根据创建者查询题目
     */
    @Select("SELECT * FROM questions WHERE created_by = #{createdBy} ORDER BY created_at DESC")
    List<Question> findQuestionsByCreator(String createdBy);

    /**
     * 根据是否AI生成查询题目
     */
    @Select("SELECT * FROM questions WHERE is_ai_generated = #{isAiGenerated} ORDER BY created_at DESC")
    List<Question> findQuestionsByAiGenerated(Boolean isAiGenerated);

    /**
     * 删除题目（根据questionId）
     */
    @Delete("DELETE FROM questions WHERE question_id = #{questionId}")
    int deleteQuestion(String questionId);

    /**
     * 更新题目
     */
    @Update("""
            UPDATE questions SET
                question_type = #{questionType},
                content = #{content},
                options = #{options},
                answer = #{answer},
                analysis = #{analysis},
                score = #{score},
                difficulty = #{difficulty}
            WHERE question_id = #{questionId}
            """)
    int updateQuestion(Question question);

    /**
     * 根据多个条件查询题目
     */
    @Select("""
            <script>
            SELECT * FROM questions
            WHERE 1=1
            <if test="questionType != null and questionType != ''">
                AND question_type = #{questionType}
            </if>
            <if test="difficulty != null">
                AND difficulty = #{difficulty}
            </if>
            <if test="createdBy != null and createdBy != ''">
                AND created_by = #{createdBy}
            </if>
            <if test="isAiGenerated != null">
                AND is_ai_generated = #{isAiGenerated}
            </if>
            ORDER BY created_at DESC
            <if test="limit != null and limit > 0">
                LIMIT #{limit}
            </if>
            </script>
            """)
    List<Question> findQuestionsByConditions(@Param("questionType") String questionType,
                                             @Param("difficulty") Integer difficulty,
                                             @Param("createdBy") String createdBy,
                                             @Param("isAiGenerated") Boolean isAiGenerated,
                                             @Param("limit") Integer limit);
}
