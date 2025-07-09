package org.example.backend.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.backend.entity.pojo.QuestionBatch;

import java.util.List;

@Mapper
public interface BatchMapper {

    @Insert("INSERT INTO question_batch(question_id, batch_name) VALUES(#{qb.questionId}, #{qb.batchName}) ON DUPLICATE KEY UPDATE batch_name = VALUES(batch_name)")
    void insertBatch(@Param("qb") QuestionBatch qb);

    /**
     * 查询全部批次信息，并统计每个批次下题目的数量。
     */
    @Select("""
            SELECT qb.question_id  AS questionId,
                   qb.batch_name   AS batchName,
                   COUNT(q.id)     AS count
            FROM question_batch qb
            LEFT JOIN questions q ON qb.question_id = q.question_id
            GROUP BY qb.question_id, qb.batch_name
            ORDER BY MAX(q.created_at) DESC
            """)
    List<QuestionBatch> listAll();
} 