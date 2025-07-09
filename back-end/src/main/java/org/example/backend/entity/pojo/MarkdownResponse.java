package org.example.backend.entity.pojo;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * AI 优化结果 —— 仅保留 markdown 字段，其余可选
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MarkdownResponse {
    private String markdown;   // 必填：整份教学大纲 (Markdown)
    private String status;     // 可选
    private String message;    // 可选
}
