package org.example.backend.entity.dto;

import lombok.Data;
import java.util.List;

/**
 * 测试创建请求DTO
 */
@Data
public class ExamCreateRequestDTO {
    private String subjectName;
    private ExamSettingsDTO settings;
    
    @Data
    public static class ExamSettingsDTO {
        private Integer questionCount;
        private Integer timeLimit;
        private String startingDifficulty;
        private List<Integer> topics;
        private List<String> customTopics;
        private Boolean showHints;
        private Boolean showFeedback;
        private Boolean adaptiveDifficulty;
    }
} 