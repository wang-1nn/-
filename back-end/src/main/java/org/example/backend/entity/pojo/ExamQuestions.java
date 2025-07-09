package org.example.backend.entity.pojo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ExamQuestions {
    private Long id;
    private Long examId;
    private Long questionId;
    private BigDecimal score;
    private Integer displayOrder;
} 