package org.example.backend.entity.pojo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StudentAnswers {
    private Long id;
    private Long submissionId;
    private Long questionId;
    private String answerContent;
    private BigDecimal score;
} 