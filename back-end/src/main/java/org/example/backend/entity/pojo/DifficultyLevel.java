package org.example.backend.entity.pojo;

/**
 * 题目难度枚举
 */
public enum DifficultyLevel {
    EASY("简单"),
    MEDIUM("中等"),
    HARD("困难");

    private final String cn;
    DifficultyLevel(String cn) { this.cn = cn; }
    public String cn() { return cn; }
} 