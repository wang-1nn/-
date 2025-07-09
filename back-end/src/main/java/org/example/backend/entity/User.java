package org.example.backend.entity;

import java.time.LocalDateTime;

/**
 * 用户实体类，对应数据库中的users表
 */
public class User {
    private String userId;        // 用户唯一标识
    private String username;      // 用户登录名
    private String password;  // 密码哈希值
    private String realName;      // 真实姓名
    private String email;         // 电子邮箱
    private String phone;         // 电话号码
    private Integer roleId;       // 角色ID: 1-管理员，2-教师，3-学生
    private String avatar;        // 头像图片URL
    private LocalDateTime createdAt;  // 账户创建时间
    private LocalDateTime lastLogin;  // 最后登录时间
    private Integer status;       // 账户状态: 1-正常，0-禁用

    // 默认构造函数
    public User() {
    }

    // 带参构造函数
    public User(String userId, String username, String passwordHash, Integer roleId, LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.roleId = roleId;
        this.createdAt = createdAt;
    }

    // Getter和Setter方法
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }



    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", realName='" + realName + '\'' +
                ", email='" + email + '\'' +
                ", roleId=" + roleId +
                ", status=" + status +
                '}';
    }
} 