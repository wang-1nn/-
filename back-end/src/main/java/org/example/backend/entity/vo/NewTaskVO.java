package org.example.backend.entity.vo;

import lombok.Data;

@Data
public class NewTaskVO {
    private String uid;
    private String title;
    private String deadline;
    private String priority;
} 