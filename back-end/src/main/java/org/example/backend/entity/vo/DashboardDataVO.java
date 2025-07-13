package org.example.backend.entity.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 学生仪表盘数据视图对象
 */
@Data
public class DashboardDataVO {
    
    /**
     * 用户名称
     */
    private String username;
    
    /**
     * 学习统计数据
     */
    private List<StatItemVO> stats;
    
    /**
     * 课程学习进度
     */
    private List<CourseProgressVO> courses;
    
    /**
     * 个性化学习建议
     */
    private List<RecommendationVO> recommendations;

    /**
     * 统计项
     */
    @Data
    public static class StatItemVO {
        private Integer id;
        private String title;
        private String value;
        private String color;
    }

    /**
     * 课程进度项
     */
    @Data
    public static class CourseProgressVO {
        private Long id;
        private String name;
        private Integer progress;
        private String lastStudy;
        private String color;
    }

    /**
     * 学习建议项
     */
    @Data
    public static class RecommendationVO {
        private Integer id;
        private String title;
        private String description;
        private Integer progress;
    }
} 