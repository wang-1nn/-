package org.example.backend.entity.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * AI生成的题目DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedQuestionDTO {

    /**
     * 题目ID（生成后分配）
     */
    private String id;

    /**
     * 题目类型
     */
    private String type;

    /**
     * 题目内容
     */
    private String content;

    /**
     * 选项（仅选择题有）
     */
    private List<Map<String, String>> options;

    /**
     * 正确答案
     */
    private String answer;

    /**
     * 解析
     */
    private String analysis;

    /**
     * 难度等级
     */
    private Integer difficulty;

    /**
     * 知识点
     */
    private String topic;

    /**
     * 是否被选中（前端使用）
     */
    private Boolean selected = false;

    /**
     * 是否正在编辑（前端使用）
     */
    private Boolean isEditing = false;

    /**
     * 生成状态
     */
    private String status = "generated";
}
