package org.example.backend.entity.vo;

import lombok.Data;
import java.util.List;

/**
 * 仪表盘小组件数据视图对象
 */
@Data
public class DashboardWidgetsVO {

    /**
     * 成绩总览数据
     */
    @Data
    public static class ScoreOverviewVO {
        private Integer score;          // 最近一次考试分数
        private Integer scoreTrend;     // 分数变化趋势，正数表示上升，负数表示下降
        private List<ExamScoreVO> scoreHistory; // 近期考试成绩记录
    }

    /**
     * 考试成绩记录
     */
    @Data
    public static class ExamScoreVO {
        private String name;    // 考试名称
        private String short_name; // 短名称（用于显示）
        private Integer score;   // 分数
    }

    /**
     * 学习时长分析数据
     */
    @Data
    public static class StudyTimeVO {
        private Float weeklyHours;       // 本周学习时长（小时）
        private Float weeklyTrend;       // 较上周变化（小时），正数表示增加，负数表示减少
        private List<DailyStudyVO> dailyStudy;  // 每日学习时长
    }

    /**
     * 每日学习记录
     */
    @Data
    public static class DailyStudyVO {
        private String day;        // 星期几（周一到周日）
        private Float hours;       // 学习时长（小时）
        private Integer percentage; // 百分比高度（用于显示）
    }

    /**
     * 待办任务数据
     */
    @Data
    public static class TodoTasksVO {
        private List<TaskItemVO> tasks; // 待办任务列表
    }

    /**
     * 待办任务项
     */
    @Data
    public static class TaskItemVO {
        private Long id;            // 任务ID
        private String title;       // 任务标题
        private String due;         // 截止时间（友好显示，如"今天 23:59"）
        private Boolean completed;  // 是否已完成
        private Boolean urgent;     // 是否紧急
    }
} 