package org.example.backend.entity.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.util.List;

/**
 * 题目生成请求DTO
 */
@Data
public class QuestionGenerationRequest {

    /**
     * 学科/科目
     */
    @Size(max = 100, message = "学科名称长度不能超过100个字符")
    private String subject;

    /**
     * 知识点
     */
    @NotBlank(message = "知识点不能为空")
    @Size(max = 255, message = "知识点长度不能超过255个字符")
    private String topic;

    /**
     * 题型列表
     */
    @NotEmpty(message = "题型不能为空")
    @Size(min = 1, max = 10, message = "题型数量必须在1-10之间")
    private List<String> selectedTypes;

    /**
     * 难度等级(1-5)
     */
    @NotNull(message = "难度等级不能为空")
    @Min(value = 1, message = "难度等级最小为1")
    @Max(value = 5, message = "难度等级最大为5")
    private Integer difficulty;

    /**
     * 生成数量
     */
    @NotNull(message = "生成数量不能为空")
    @Min(value = 1, message = "生成数量最小为1")
    @Max(value = 20, message = "生成数量最大为20")
    private Integer count;

    /**
     * 额外要求
     */
    @Size(max = 1000, message = "额外要求长度不能超过1000个字符")
    private String requirements;

    /**
     * 创建者ID
     */
    private String createdBy;

    /**
     * 获取难度描述
     */
    public String getDifficultyDescription() {
        switch (difficulty) {
            case 1: return "简单";
            case 2: return "较简单";
            case 3: return "中等";
            case 4: return "较难";
            case 5: return "困难";
            default: return "中等";
        }
    }
}
