package org.example.backend.entity.pojo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 题目实体类，对应数据库中的questions表


import java.math.BigDecimal;

/**
 * 题目实体类，对应数据库中的questions表
 */
@Data
public class Question {
        String UserAnswer;
        BigDecimal UserScore;
        Boolean IsCorrect;
        private Long id;             // 自增主键，对应数据库questions表的id
        private String questionId;     // 题目唯一标识
        private String examId;         // 所属考核ID
        private String questionType;   // 题目类型: 选择题、填空题、问答题、编程题
        private String content;        // 题目内容
        private String options;        // 选择题选项，JSON格式
        private String answer;         // 参考答案
        private String analysis;       // 解析
        private BigDecimal score;      // 分值
        private Integer difficulty;    // 难度级别: 1-5
        private String createdBy;      // 创建者ID
        private Boolean isAiGenerated; // 是否AI生成

        // 默认构造函数
        public Question() {
        }

        // 带参构造函数
        public Question(String questionId, String questionType, String content, String createdBy) {
                this.questionId = questionId;
                this.questionType = questionType;
                this.content = content;
                this.createdBy = createdBy;
        }

        // Getter和Setter方法

        public Long getId() {
                return id;
        }

        public void setId(Long id) {
                this.id = id;
        }

        public String getQuestionId() {
                return questionId;
        }

        public void setQuestionId(String questionId) {
                this.questionId = questionId;
        }

        public String getExamId() {
                return examId;
        }

        public void setExamId(String examId) {
                this.examId = examId;
        }

        public String getQuestionType() {
                return questionType;
        }

        public void setQuestionType(String questionType) {
                this.questionType = questionType;
        }

        public String getContent() {
                return content;
        }

        public void setContent(String content) {
                this.content = content;
        }

        public String getOptions() {
                return options;
        }

        public void setOptions(String options) {
                this.options = options;
        }

        public String getAnswer() {
                return answer;
        }

        public void setAnswer(String answer) {
                this.answer = answer;
        }

        public String getAnalysis() {
                return analysis;
        }

        public void setAnalysis(String analysis) {
                this.analysis = analysis;
        }

        public BigDecimal getScore() {
                return score;
        }

        public void setScore(BigDecimal score) {
                this.score = score;
        }

        public Integer getDifficulty() {
                return difficulty;
        }

        public void setDifficulty(Integer difficulty) {
                this.difficulty = difficulty;
        }

        public String getCreatedBy() {
                return createdBy;
        }

        public void setCreatedBy(String createdBy) {
                this.createdBy = createdBy;
        }

        public Boolean getIsAiGenerated() {
                return isAiGenerated;
        }

        public void setIsAiGenerated(Boolean aiGenerated) {
                isAiGenerated = aiGenerated;
        }

        @Override
        public String toString() {
                return "Question{" +
                        "questionId='" + questionId + '\'' +
                        ", examId='" + examId + '\'' +
                        ", questionType='" + questionType + '\'' +
                        ", content='" + content + '\'' +
                        ", difficulty=" + difficulty +
                        '}';
        }
} 