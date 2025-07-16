package org.example.backend.entity.vo;

import lombok.Data;
import java.util.List;

/**
 * 课程班级VO
 */
@Data
public class CourseClassVO {
    private Long courseId;
    private String courseName;
    private String subject;
    private List<ClassInfo> classes;
    
    @Data
    public static class ClassInfo {
        private Long classId;
        private String className;
        private String grade;
        private String major;
        private Integer studentCount;
    }
}
