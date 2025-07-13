package org.example.backend.entity.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 自适应考试题目实体类
 */
@Data
public class AdaptiveQuestion {
    private Long id;
    private Long examId;
    private String questionId;
    private String questionType; // 题目类型：SINGLE_CHOICE, MULTIPLE_CHOICE, TRUE_FALSE, FILL_BLANK, SHORT_ANSWER
    private String content; // 题目内容
    private String options; // 选项，如{"A": "选项A", "B": "选项B"}
    private String answer; // 正确答案
    private String analysis; // 解析
    private double score; // 分值
    private Integer difficulty; // 难度等级：1-5
    private String userAnswer; // 用户答案
    private Integer userScore; // 用户得分
    private Integer sequenceNumber; // 题目序号

    LocalDateTime AnsweredAt;
    Boolean IsCorrect;

    LocalDateTime CreatedAt;
    String Topic;



} 