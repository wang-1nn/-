package org.example.backend.mapper;

import org.apache.ibatis.annotations.*;
import org.example.backend.entity.CourseFavorite;

import java.util.List;

/**
 * 课程收藏Mapper接口
 */
@Mapper
public interface CourseFavoriteMapper {

    /**
     * 添加收藏
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 影响行数
     */
    @Insert({
            "INSERT INTO course_favorites (user_id, course_id) ",
            "VALUES (#{userId}, #{courseId})"
    })
    int addFavorite(@Param("userId") Long userId, @Param("courseId") Long courseId);

    /**
     * 取消收藏
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 影响行数
     */
    @Delete({
            "DELETE FROM course_favorites ",
            "WHERE user_id = #{userId} AND course_id = #{courseId}"
    })
    int removeFavorite(@Param("userId") Long userId, @Param("courseId") Long courseId);

    /**
     * 检查是否已收藏
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 是否已收藏
     */
    @Select({
            "SELECT COUNT(*) FROM course_favorites ",
            "WHERE user_id = #{userId} AND course_id = #{courseId}"
    })
    boolean isFavorite(@Param("userId") Long userId, @Param("courseId") Long courseId);

    /**
     * 获取用户收藏的所有课程ID
     *
     * @param userId 用户ID
     * @return 课程ID列表
     */
    @Select({
            "SELECT course_id FROM course_favorites ",
            "WHERE user_id = #{userId}"
    })
    List<Long> getFavoriteCourseIds(@Param("userId") Long userId);
} 