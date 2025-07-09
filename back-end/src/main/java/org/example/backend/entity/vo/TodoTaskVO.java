package org.example.backend.entity.vo;

import lombok.Data;

import java.util.Date;

@Data
public class TodoTaskVO {
    private long id;
    private String task;
    private String type;
    private Date dueDate;
    private boolean completed;
} 