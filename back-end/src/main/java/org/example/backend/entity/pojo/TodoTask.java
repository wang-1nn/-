package org.example.backend.entity.pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TodoTask {
    private Long id;
    private Long userId;
    private String taskType;  // 任务优先级：0-紧急，1-普通，2-低
    private String title;
    private String status;    // 任务状态: PENDING, COMPLETED
    private LocalDateTime dueDate;
    private Long relatedEntityId;
    private LocalDateTime createdAt;
    
    /**
     * 根据前端传入的优先级字符串获取对应的数值表示
     * @param priorityStr 优先级字符串（high, medium, low）
     * @return 优先级数值（0-紧急，1-普通，2-低）
     */
    public static String getPriorityValue(String priorityStr) {
        if (priorityStr == null) return "1"; // 默认普通优先级
        
        switch (priorityStr.toLowerCase()) {
            case "high": return "0"; // 紧急
            case "medium": return "1"; // 普通
            case "low": return "2"; // 低
            default: return "1"; // 默认普通优先级
        }
    }
    
    /**
     * 根据优先级数值获取对应的字符串表示
     * @param priority 优先级数值（0-紧急，1-普通，2-低）
     * @return 优先级字符串（high, medium, low）
     */
    public static String getPriorityString(String priority) {
        if (priority == null) return "medium"; // 默认普通优先级
        
        switch (priority) {
            case "0": return "high"; // 紧急
            case "1": return "medium"; // 普通
            case "2": return "low"; // 低
            default: return "medium"; // 默认普通优先级
        }
    }
} 