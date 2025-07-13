package org.example.backend.entity.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MarkdownResponse {
    private String markdown;   // 必填：整份教学大纲 (Markdown)
    private String status;     // 可选
    private String message;    // 可选
}
