package org.example.backend.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI 生成的一道题目结构。对应 LLM 输出的 JSON 字段。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenQuestion {
    /** 题干 */
    private String question;

    /**
     * 选项列表。仅当题型为选择题时有值，顺序固定 A-D
     */
    private List<String> options;

    /** 正确答案（选项的字母或填空/判断/简答内容） */
    private String answer;

    /** 答案解析 */
    private String explain;
} 