package org.example.backend.entity.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建考试请求DTO
 */
@Data
public class ExamCreateRequest {
    
    @NotBlank(message = "考试标题不能为空")
    @Size(max = 200, message = "考试标题长度不能超过200个字符")
    private String title;
    
    @Size(max = 1000, message = "考试描述长度不能超过1000个字符")
    private String description;
    
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
    
    @NotEmpty(message = "班级列表不能为空")
    private List<Long> classIds;
    
    @NotBlank(message = "考试类型不能为空")
    private String examType; // 作业、测验、考试
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    @Min(value = 1, message = "考试时长至少1分钟")
    @Max(value = 600, message = "考试时长不能超过600分钟")
    private Integer duration; // 考试时长（分钟）
    
    @Min(value = 0, message = "及格分数不能小于0")
    @Max(value = 100, message = "及格分数不能大于100")
    private Double passingScore = 60.0;
    
    private Boolean showResults = true;
    
    @NotEmpty(message = "题目列表不能为空")
    private List<ExamQuestionRequest> questions;
    
    /**
     * 考试题目请求
     */
    @Data
    public static class ExamQuestionRequest {
        @NotBlank(message = "题目ID不能为空")
        private String questionId;
        
        @Min(value = 1, message = "题目分数至少1分")
        @Max(value = 100, message = "题目分数不能超过100分")
        private Double score;
        
        @Min(value = 1, message = "题目顺序至少为1")
        private Integer orderNum;
    }
}
