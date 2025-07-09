package org.example.backend.entity.dto;

/**
 * 题型枚举。
 */
public enum QuestionType {
    CHOICE("选择题"),
    FILL("填空题"),
    SHORT("简答题"),
    JUDGE("判断题");

    private final String cn;

    QuestionType(String cn) {
        this.cn = cn;
    }

    public String cn() {
        return cn;
    }
} 