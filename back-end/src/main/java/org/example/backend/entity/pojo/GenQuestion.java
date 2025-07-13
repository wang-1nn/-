package org.example.backend.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI 生成的题目信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenQuestion {
    /** 题干 */
    private String question;
    /** 选项，仅选择题有 */
    private List<String> options;
    /** 正确答案 */
    private String answer;
    /** 解析/说明 */
    private String explain;
}