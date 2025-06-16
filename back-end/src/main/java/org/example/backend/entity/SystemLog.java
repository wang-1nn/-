package org.example.backend.entity;

import java.time.LocalDateTime;

/**
 * 系统日志实体类，对应数据库中的system_logs表
 */
public class SystemLog {
    private Integer id;            // 日志ID
    private String logType;        // 日志类型: 登录、操作、异常等
    private String userId;         // 相关用户ID
    private String ipAddress;      // IP地址
    private String operation;      // 操作描述
    private String details;        // 详细信息
    private LocalDateTime createdAt; // 创建时间
    private String module;         // 所属模块
    private Integer status;        // 状态: 0-失败，1-成功

    // 默认构造函数
    public SystemLog() {
    }

    // 带参构造函数
    public SystemLog(String logType, String operation, LocalDateTime createdAt, Integer status) {
        this.logType = logType;
        this.operation = operation;
        this.createdAt = createdAt;
        this.status = status;
    }

    // Getter和Setter方法
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "SystemLog{" +
                "id=" + id +
                ", logType='" + logType + '\'' +
                ", userId='" + userId + '\'' +
                ", operation='" + operation + '\'' +
                ", createdAt=" + createdAt +
                ", status=" + status +
                '}';
    }
} 