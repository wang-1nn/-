package org.example.backend.entity.pojo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Submissions {
    private Long id;
    private Long examId;
    private Long studentId;
    private Integer status;
    private LocalDateTime submittedAt;
    private BigDecimal totalScore;
} 