package org.example.backend.service.impl;

import org.example.backend.mapper.CourseFavoriteMapper;
import org.example.backend.service.CourseFavoriteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 课程收藏Service实现类
 */
@Service
public class CourseFavoriteServiceImpl implements CourseFavoriteService {

    private static final Logger logger = LoggerFactory.getLogger(CourseFavoriteServiceImpl.class);

    @Autowired
    private CourseFavoriteMapper courseFavoriteMapper;

    @Override
    public boolean addFavorite(Long userId, Long courseId) {
        try {
            logger.info("添加收藏，用户ID: {}, 课程ID: {}", userId, courseId);
            return courseFavoriteMapper.addFavorite(userId, courseId) > 0;
        } catch (Exception e) {
            logger.error("添加收藏失败，用户ID: {}, 课程ID: {}", userId, courseId, e);
            return false;
        }
    }

    @Override
    public boolean removeFavorite(Long userId, Long courseId) {
        try {
            logger.info("取消收藏，用户ID: {}, 课程ID: {}", userId, courseId);
            return courseFavoriteMapper.removeFavorite(userId, courseId) > 0;
        } catch (Exception e) {
            logger.error("取消收藏失败，用户ID: {}, 课程ID: {}", userId, courseId, e);
            return false;
        }
    }

    @Override
    public boolean toggleFavorite(Long userId, Long courseId) {
        try {
            boolean isFavorite = isFavorite(userId, courseId);
            logger.info("切换收藏状态，用户ID: {}, 课程ID: {}, 当前状态: {}", userId, courseId, isFavorite);
            
            if (isFavorite) {
                removeFavorite(userId, courseId);
                return false;
            } else {
                addFavorite(userId, courseId);
                return true;
            }
        } catch (Exception e) {
            logger.error("切换收藏状态失败，用户ID: {}, 课程ID: {}", userId, courseId, e);
            return false;
        }
    }

    @Override
    public boolean isFavorite(Long userId, Long courseId) {
        try {
            return courseFavoriteMapper.isFavorite(userId, courseId);
        } catch (Exception e) {
            logger.error("检查收藏状态失败，用户ID: {}, 课程ID: {}", userId, courseId, e);
            return false;
        }
    }

    @Override
    public List<Long> getFavoriteCourseIds(Long userId) {
        try {
            logger.info("获取用户收藏课程列表，用户ID: {}", userId);
            return courseFavoriteMapper.getFavoriteCourseIds(userId);
        } catch (Exception e) {
            logger.error("获取用户收藏课程列表失败，用户ID: {}", userId, e);
            return Collections.emptyList();
        }
    }
} 