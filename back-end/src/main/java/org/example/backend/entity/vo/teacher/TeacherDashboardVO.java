package org.example.backend.entity.vo.teacher;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 教师仪表盘相关的VO类
 */
public class TeacherDashboardVO {

    /**
     * 仪表盘概览数据
     */
    @Data
    public static class DashboardSummary {
        private String teacherName;
        private Integer courseCount;
        private Integer studentCount;
        private Integer pendingGradingCount;
        private Integer upcomingDeadlines;
        private Integer activeCourses;
        private Integer newStudentsThisWeek;
        private Integer currentWeek;
        private Integer totalWeeks;
    }

    /**
     * 通知公告
     */
    @Data
    public static class Announcement {
        private Long id;
        private String title;
        private String content;
        private String type;
        private Boolean isImportant;
        private LocalDateTime publishTime;
        private LocalDateTime expireTime;
        private String status;
        private String course;
        private Boolean isRead;
    }

    /**
     * 待办任务
     */
    @Data
    public static class Task {
        private Long id;
        private String title;
        private String description;
        private String taskType;
        private String priority;
        private LocalDateTime dueDate;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    /**
     * 图表数据
     */
    @Data
    public static class ChartData<T, V> {
        private List<T> labels;
        private List<V> values;
        private String title;
        private String type;

        public ChartData() {}

        public ChartData(List<T> labels, List<V> values) {
            this.labels = labels;
            this.values = values;
        }

        public ChartData(List<T> labels, List<V> values, String title, String type) {
            this.labels = labels;
            this.values = values;
            this.title = title;
            this.type = type;
        }
    }

    /**
     * 课程统计
     */
    @Data
    public static class CourseStats {
        private Integer totalCourses;
        private Integer activeCourses;
        private Integer publishedCourses;
        private Double avgStudentsPerCourse;
        private Integer totalStudents;
        private Integer totalChapters;
    }

    /**
     * 待批改作业
     */
    @Data
    public static class PendingGrading {
        private Long assignmentId;
        private String assignmentTitle;
        private String courseName;
        private LocalDateTime submitTime;
        private String studentName;
        private Long submissionId;
        private String type;
        private Boolean isLate;
    }

    /**
     * 学生表现
     */
    @Data
    public static class StudentPerformance {
        private String studentName;
        private String courseName;
        private Double progress;
        private LocalDateTime lastAccessTime;
        private Double averageScore;
        private String status;
        private Integer studyTime;
    }

    /**
     * 班级快照
     */
    @Data
    public static class ClassSnapshot {
        private Long courseId;
        private String courseName;
        private Integer totalStudents;
        private Integer activeStudents;
        private Integer atRiskStudents;
        private Double averageScore;
        private Double passRate;
        private Double excellentRate;
        private ScoreDistribution scoreDistribution;
    }

    /**
     * 分数分布
     */
    @Data
    public static class ScoreDistribution {
        private Integer excellent; // 优秀 (90-100)
        private Integer good;      // 良好 (80-89)
        private Integer average;   // 中等 (70-79)
        private Integer poor;      // 需努力 (60-69)
        private Integer fail;      // 不及格 (<60)
    }

    /**
     * 教学进度
     */
    @Data
    public static class TeachingProgress {
        private Long courseId;
        private String courseName;
        private Integer totalChapters;
        private Integer publishedChapters;
        private Double progressPercentage;
        private LocalDateTime lastUpdate;
    }

    /**
     * 最近活动
     */
    @Data
    public static class RecentActivity {
        private String type;
        private String title;
        private String description;
        private LocalDateTime timestamp;
        private String relatedUser;
        private String relatedCourse;
        private String status;
    }

    /**
     * 创建任务请求
     */
    @Data
    public static class CreateTaskRequest {
        private Long teacherId;
        private String title;
        private String description;
        private String taskType;
        private String priority;
        private LocalDateTime dueDate;
    }

    /**
     * 更新任务请求
     */
    @Data
    public static class UpdateTaskRequest {
        private String status;
        private String description;
        private String priority;
        private LocalDateTime dueDate;
    }

    /**
     * 学生活动数据
     */
    @Data
    public static class StudentActivityData {
        private String date;
        private Integer count;
        private String activityType;
    }

    /**
     * 资源访问数据
     */
    @Data
    public static class ResourceAccessData {
        private String resourceType;
        private Integer accessCount;
        private String resourceName;
    }

    /**
     * 课程信息
     */
    @Data
    public static class CourseInfo {
        private Long id;
        private String title;
        private String subject;
        private String description;
        private Boolean isPublished;
        private Integer studentCount;
        private Integer chapterCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    /**
     * 学生信息
     */
    @Data
    public static class StudentInfo {
        private Long id;
        private String username;
        private String realName;
        private String email;
        private LocalDateTime enrollmentDate;
        private String status;
        private Double progress;
        private Integer studyTime;
        private LocalDateTime lastAccessTime;
    }

    /**
     * 作业信息
     */
    @Data
    public static class AssignmentInfo {
        private Long id;
        private String title;
        private String description;
        private String type;
        private Double totalScore;
        private LocalDateTime dueDate;
        private Boolean allowLateSubmission;
        private Boolean isPublished;
        private Integer submissionCount;
        private Integer gradedCount;
        private LocalDateTime createdAt;
    }

    /**
     * 成绩统计
     */
    @Data
    public static class GradeStats {
        private String courseName;
        private Double averageScore;
        private Double highestScore;
        private Double lowestScore;
        private Integer totalStudents;
        private Integer passedStudents;
        private Double passRate;
        private ScoreDistribution distribution;
    }

    // ========== AI功能相关VO ==========

    /**
     * 智能日程项目
     */
    @Data
    public static class ScheduleItem {
        private Long id;
        private String time;
        private String title;
        private String location;
        private String type; // class, meeting, deadline

        public ScheduleItem() {}

        public ScheduleItem(Long id, String time, String title, String location, String type) {
            this.id = id;
            this.time = time;
            this.title = title;
            this.location = location;
            this.type = type;
        }
    }

    /**
     * 学生表现亮点
     */
    @Data
    public static class StudentHighlight {
        private Long id;
        private String text;

        public StudentHighlight() {}

        public StudentHighlight(Long id, String text) {
            this.id = id;
            this.text = text;
        }
    }

    /**
     * 学生潜在风险
     */
    @Data
    public static class StudentRisk {
        private Long id;
        private String text;

        public StudentRisk() {}

        public StudentRisk(Long id, String text) {
            this.id = id;
            this.text = text;
        }
    }

    /**
     * 互动中心项目
     */
    @Data
    public static class InteractionItem {
        private Long id;
        private String type; // assignment, question, forum
        private String student;
        private String title;
        private String course;
        private String timestamp;

        public InteractionItem() {}

        public InteractionItem(Long id, String type, String student, String title, String course, String timestamp) {
            this.id = id;
            this.type = type;
            this.student = student;
            this.title = title;
            this.course = course;
            this.timestamp = timestamp;
        }
    }

    /**
     * AI分析结果
     */
    @Data
    public static class AIAnalysisResult {
        private String type; // teaching_suggestion, class_performance, personalized_advice, learning_trend, grading_suggestion
        private String content;
        private LocalDateTime generatedAt;
        private Long relatedId; // 相关的课程ID、学生ID或提交ID
    }
}
