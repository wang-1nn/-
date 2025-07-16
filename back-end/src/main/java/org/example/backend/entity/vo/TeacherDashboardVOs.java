package org.example.backend.entity.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 教师仪表盘相关的VO类集合
 */
public class TeacherDashboardVOs {

    /**
     * 通知公告VO
     */
    @Data
    public static class AnnouncementVO {
        private Long id;
        private String title;
        private String content;
        private String type;
        private Boolean isImportant;
        private LocalDateTime publishTime;
        private LocalDateTime expireTime;
        private String status;
        private String course; // 关联课程名称
    }

    /**
     * 任务VO
     */
    @Data
    public static class TaskVO {
        private Long id;
        private String title;
        private String description;
        private String priority;
        private LocalDateTime dueDate;
        private String status;
        private Boolean completed;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    /**
     * 图表数据VO
     */
    @Data
    public static class ChartDataVO<T, V> {
        private List<T> labels;
        private List<V> values;
        private String title;
        private String type;

        public ChartDataVO() {}

        public ChartDataVO(List<T> labels, List<V> values) {
            this.labels = labels;
            this.values = values;
        }

        public ChartDataVO(List<T> labels, List<V> values, String title, String type) {
            this.labels = labels;
            this.values = values;
            this.title = title;
            this.type = type;
        }
    }

    /**
     * 课程统计VO
     */
    @Data
    public static class CourseStatsVO {
        private Integer totalCourses;
        private Integer activeCourses;
        private Integer publishedCourses;
        private Double avgStudentsPerCourse;
        private Integer totalStudents;
        private Integer totalChapters;
    }

    /**
     * 待批改作业VO
     */
    @Data
    public static class PendingGradingVO {
        private Long assignmentId;
        private String assignmentTitle;
        private String courseName;
        private LocalDateTime submitTime;
        private String studentName;
        private Long submissionId;
        private String type;
    }

    /**
     * 学生表现VO
     */
    @Data
    public static class StudentPerformanceVO {
        private String studentName;
        private String courseName;
        private Double progress;
        private LocalDateTime lastAccessTime;
        private Double averageScore;
        private String status;
    }

    /**
     * 日历事件VO
     */
    @Data
    public static class CalendarEventVO {
        private String type;
        private String title;
        private LocalDateTime eventDate;
        private String courseName;
        private Long relatedId;
        private String description;
        private String priority;
    }

    /**
     * 创建任务请求VO
     */
    @Data
    public static class CreateTaskRequest {
        private Long teacherId;
        private String title;
        private String description;
        private String priority;
        private LocalDateTime dueDate;
        private String taskType;
    }

    /**
     * 更新任务请求VO
     */
    @Data
    public static class UpdateTaskRequest {
        private String status;
        private Boolean completed;
        private String description;
        private String priority;
        private LocalDateTime dueDate;
    }

    /**
     * 班级快照VO
     */
    @Data
    public static class ClassSnapshotVO {
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
     * 分数分布VO
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
     * 学生活动数据VO
     */
    @Data
    public static class StudentActivityData {
        private String date;
        private Integer count;
        private String activityType;
    }

    /**
     * 资源访问数据VO
     */
    @Data
    public static class ResourceAccessData {
        private String resourceType;
        private Integer accessCount;
        private String resourceName;
    }

    /**
     * 教学进度VO
     */
    @Data
    public static class TeachingProgressVO {
        private Long courseId;
        private String courseName;
        private Integer totalChapters;
        private Integer completedChapters;
        private Double progressPercentage;
        private LocalDateTime lastUpdate;
    }

    /**
     * 快速统计VO
     */
    @Data
    public static class QuickStatsVO {
        private String label;
        private Object value;
        private String trend;
        private String icon;
        private String color;
        private String description;
    }

    /**
     * 最近活动VO
     */
    @Data
    public static class RecentActivityVO {
        private String type;
        private String title;
        private String description;
        private LocalDateTime timestamp;
        private String relatedUser;
        private String relatedCourse;
        private String status;
    }

    /**
     * 教师工作负载VO
     */
    @Data
    public static class WorkloadVO {
        private Integer totalCourses;
        private Integer totalStudents;
        private Integer pendingTasks;
        private Integer upcomingDeadlines;
        private Double workloadScore; // 工作负载评分 (0-100)
        private String workloadLevel; // low, medium, high
        private List<String> suggestions; // 工作建议
    }

    /**
     * 学期概览VO
     */
    @Data
    public static class SemesterOverviewVO {
        private Integer currentWeek;
        private Integer totalWeeks;
        private Double semesterProgress;
        private LocalDateTime semesterStart;
        private LocalDateTime semesterEnd;
        private List<String> upcomingEvents;
    }
}
