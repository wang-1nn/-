package org.example.backend.controller;

import org.example.backend.entity.dto.DiscussionDTO;
import org.example.backend.service.StudentService;
import org.example.backend.util.RestBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 课程讨论Controller
 */
@RestController
@RequestMapping("/api/discussions")
public class DiscussionController {

    private static final Logger logger = LoggerFactory.getLogger(DiscussionController.class);

    @Autowired
    private StudentService studentService;

    /**
     * 获取课程讨论列表
     *
     * @param courseId 课程ID
     * @return 讨论列表
     */
    @GetMapping("/list")
    public RestBean<List<DiscussionDTO>> getDiscussions(@RequestParam Long courseId) {
        try {
            List<DiscussionDTO> discussions = studentService.getCourseDiscussions(courseId);
            return RestBean.success("获取讨论列表成功", discussions);
        } catch (Exception e) {
            logger.error("获取讨论列表失败", e);
            return RestBean.failure(500, "获取讨论列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取讨论详情
     *
     * @param discussionId 讨论ID
     * @return 讨论详情
     */
    @GetMapping("/detail")
    public RestBean<DiscussionDTO> getDiscussionDetail(@RequestParam Long discussionId) {
        try {
            DiscussionDTO discussion = studentService.getDiscussionDetail(discussionId);
            return RestBean.success("获取讨论详情成功", discussion);
        } catch (Exception e) {
            logger.error("获取讨论详情失败", e);
            return RestBean.failure(500, "获取讨论详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建讨论
     *
     * @param userId    用户ID
     * @param courseId  课程ID
     * @param title     标题
     * @param content   内容
     * @return 创建结果
     */
    @PostMapping("/create")
    public RestBean<Map<String, Object>> createDiscussion(
            @RequestParam Long userId,
            @RequestParam Long courseId,
            @RequestParam String title,
            @RequestParam String content) {
        try {
            boolean success = studentService.createDiscussion(userId, courseId, title, content);
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            return RestBean.success("创建讨论成功", result);
        } catch (Exception e) {
            logger.error("创建讨论失败", e);
            return RestBean.failure(500, "创建讨论失败: " + e.getMessage());
        }
    }

    /**
     * 回复讨论
     *
     * @param userId       用户ID
     * @param discussionId 讨论ID
     * @param content      回复内容
     * @param parentId     父回复ID（可选）
     * @return 回复结果
     */
    @PostMapping("/reply")
    public RestBean<Map<String, Object>> replyToDiscussion(
            @RequestParam Long userId,
            @RequestParam Long discussionId,
            @RequestParam String content,
            @RequestParam(required = false) Long parentId) {
        try {
            boolean success = studentService.replyToDiscussion(userId, discussionId, content, parentId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            return RestBean.success("回复成功", result);
        } catch (Exception e) {
            logger.error("回复失败", e);
            return RestBean.failure(500, "回复失败: " + e.getMessage());
        }
    }

    /**
     * 点赞讨论
     *
     * @param userId       用户ID
     * @param discussionId 讨论ID
     * @return 点赞结果
     */
    @PostMapping("/like")
    public RestBean<Map<String, Object>> likeDiscussion(
            @RequestParam Long userId,
            @RequestParam Long discussionId) {
        try {
            boolean isLiked = studentService.toggleDiscussionLike(userId, discussionId);
            Map<String, Object> result = new HashMap<>();
            result.put("isLiked", isLiked);
            return RestBean.success(isLiked ? "点赞成功" : "取消点赞成功", result);
        } catch (Exception e) {
            logger.error("点赞操作失败", e);
            return RestBean.failure(500, "点赞操作失败: " + e.getMessage());
        }
    }

    /**
     * 检查是否已点赞
     *
     * @param userId       用户ID
     * @param discussionId 讨论ID
     * @return 是否已点赞
     */
    @GetMapping("/check-like")
    public RestBean<Map<String, Object>> checkLike(
            @RequestParam Long userId,
            @RequestParam Long discussionId) {
        try {
            boolean isLiked = studentService.checkDiscussionLike(userId, discussionId);
            Map<String, Object> result = new HashMap<>();
            result.put("isLiked", isLiked);
            return RestBean.success("查询点赞状态成功", result);
        } catch (Exception e) {
            logger.error("查询点赞状态失败", e);
            return RestBean.failure(500, "查询点赞状态失败: " + e.getMessage());
        }
    }
} 