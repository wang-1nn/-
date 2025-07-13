package org.example.backend.service;

import java.util.List;

/**
 * 课程收藏Service接口
 */
public interface CourseFavoriteService {

    /**
     * 添加收藏
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 是否成功
     */
    boolean addFavorite(Long userId, Long courseId);

    /**
     * 取消收藏
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 是否成功
     */
    boolean removeFavorite(Long userId, Long courseId);

    /**
     * 切换收藏状态
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 当前收藏状态
     */
    boolean toggleFavorite(Long userId, Long courseId);

    /**
     * 检查是否已收藏
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 是否已收藏
     */
    boolean isFavorite(Long userId, Long courseId);

    /**
     * 获取用户收藏的所有课程ID
     *
     * @param userId 用户ID
     * @return 课程ID列表
     */
    List<Long> getFavoriteCourseIds(Long userId);
} 