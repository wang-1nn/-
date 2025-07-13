package org.example.backend.controller;

import org.example.backend.service.CourseFavoriteService;
import org.example.backend.util.RestBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 课程收藏Controller
 */
@RestController
@RequestMapping("/api/favorites")
public class CourseFavoriteController {

    @Autowired
    private CourseFavoriteService courseFavoriteService;

    /**
     * 切换收藏状态
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 当前收藏状态
     */
    @PostMapping("/toggle")
    public RestBean<Map<String, Object>> toggleFavorite(@RequestParam Long userId, @RequestParam Long courseId) {
        boolean isFavorite = courseFavoriteService.toggleFavorite(userId, courseId);
        Map<String, Object> result = new HashMap<>();
        result.put("isFavorite", isFavorite);
        return RestBean.success(isFavorite ? "收藏成功" : "取消收藏成功", result);
    }

    /**
     * 检查是否已收藏
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 是否已收藏
     */
    @GetMapping("/check")
    public RestBean<Map<String, Object>> checkFavorite(@RequestParam Long userId, @RequestParam Long courseId) {
        boolean isFavorite = courseFavoriteService.isFavorite(userId, courseId);
        Map<String, Object> result = new HashMap<>();
        result.put("isFavorite", isFavorite);
        return RestBean.success(result);
    }

    /**
     * 获取用户收藏的所有课程ID
     *
     * @param userId 用户ID
     * @return 课程ID列表
     */
    @GetMapping("/list")
    public RestBean<Map<String, Object>> getFavoriteCourses(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("courseIds", courseFavoriteService.getFavoriteCourseIds(userId));
        return RestBean.success("获取收藏课程列表成功", result);
    }
} 