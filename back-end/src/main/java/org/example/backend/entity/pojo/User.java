package org.example.backend.entity.pojo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 用户实体类，对应数据库中的users表
 */
@Data
public class User {
    public String Token;
    private String userId;        // 用户唯一标识
    private String username;      // 用户登录名
    private String password;  // 密码哈希值

    public String getToken() {
        return Token;
    }

    public void setToken(String token) {
        Token = token;
    }

    private String realName;      // 真实姓名
    private String email;         // 电子邮箱
    private String phone;         // 电话号码
    private Integer roleId;       // 角色ID: 1-管理员，2-教师，3-学生
    private String avatar;        // 头像图片URL
    private Date createdAt;  // 账户创建时间
    private LocalDateTime lastLogin;  // 最后登录时间
    private Integer status;       // 账户状态: 1-正常，0-禁用
    private Long classId;         // 班级ID

    // 默认构造函数

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
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
    
    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
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
                ", classId=" + classId +
                '}';
    }
} 