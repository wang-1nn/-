package org.example.backend.entity;

import java.time.LocalDateTime;

/**
 * 使用统计实体类，对应数据库中的usage_statistics表
 */
public class UsageStatistic {
    private Integer id;           // 记录ID
    private String userId;        // 用户ID
    private String module;        // 使用模块: 备课系统、考核系统、学习助手等
    private String action;        // 具体操作
    private LocalDateTime startTime; // 开始时间
    private LocalDateTime endTime;   // 结束时间
    private Integer duration;     // 使用时长(秒)
    private Integer requestCount; // 请求次数

    // 默认构造函数
    public UsageStatistic() {
    }

    // 带参构造函数
    public UsageStatistic(String userId, String module, String action, LocalDateTime startTime) {
        this.userId = userId;
        this.module = module;
        this.action = action;
        this.startTime = startTime;
        this.requestCount = 1;
    }

    // Getter和Setter方法
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(Integer requestCount) {
        this.requestCount = requestCount;
    }

    @Override
    public String toString() {
        return "UsageStatistic{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", module='" + module + '\'' +
                ", action='" + action + '\'' +
                ", duration=" + duration +
                ", requestCount=" + requestCount +
                '}';
    }
} 