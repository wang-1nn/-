package org.example.backend.entity.vo;

import lombok.Data;

@Data
public class DashboardSummaryVO {
    private String teacherName;
    private CourseStatVO courses;
    private StudentStatVO students;
    private PlanStatVO plans;
    private ResourceStatVO resources;
    private Integer currentWeek;
    private Integer totalWeeks;
} 