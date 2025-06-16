package org.example.backend.entity;

/**
 * 用户与班级的关联实体类，对应数据库中的user_class_relation表
 */
public class UserClassRelation {
    private Integer id;        // 关系ID
    private String userId;     // 用户ID
    private String classId;    // 班级ID

    // 默认构造函数
    public UserClassRelation() {
    }

    // 带参构造函数
    public UserClassRelation(String userId, String classId) {
        this.userId = userId;
        this.classId = classId;
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

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    @Override
    public String toString() {
        return "UserClassRelation{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", classId='" + classId + '\'' +
                '}';
    }
} 