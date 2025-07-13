package org.example.backend.controller;

import org.example.backend.entity.dto.ChapterRatingDTO;
import org.example.backend.entity.pojo.ChapterRating;
import org.example.backend.service.ChapterRatingService;
import org.example.backend.util.RestBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 章节评价控制器
 */
@RestController
@RequestMapping("/api/rating")
public class ChapterRatingController {
    
    @Autowired
    private ChapterRatingService chapterRatingService;
    
    /**
     * 提交章节评价
     * 
     * @param rating 评价信息
     * @return 结果
     */
    @PostMapping("/submit")
    public RestBean<Long> submitRating(@RequestBody ChapterRating rating) {
        Long ratingId = chapterRatingService.submitRating(rating);
        return RestBean.success("提交成功", ratingId);
    }
    
    /**
     * 更新章节评价
     * 
     * @param rating 评价信息
     * @return 结果
     */
    @PutMapping("/update")
    public RestBean<Void> updateRating(@RequestBody ChapterRating rating) {
        boolean success = chapterRatingService.updateRating(rating);
        return success ? RestBean.success("更新评价成功") : RestBean.failure(400, "更新评价失败");
    }
    
    /**
     * 删除章节评价
     * 
     * @param ratingId 评价ID
     * @return 结果
     */
    @DeleteMapping("/delete/{ratingId}")
    public RestBean<Void> deleteRating(@PathVariable Long ratingId) {
        boolean success = chapterRatingService.deleteRating(ratingId);
        return success ? RestBean.success("删除评价成功") : RestBean.failure(400, "删除评价失败");
    }
    
    /**
     * 获取学生对章节的评价
     * 
     * @param studentId 学生ID
     * @param chapterId 章节ID
     * @return 章节评价
     */
    @GetMapping("/student/chapter")
    public RestBean<ChapterRating> getStudentRating(
            @RequestParam Long studentId, @RequestParam Long chapterId) {
        ChapterRating rating = chapterRatingService.getStudentRating(studentId, chapterId);
        return rating != null ? RestBean.success("获取成功", rating) : RestBean.success("未找到评价", null);
    }
    
    /**
     * 根据章节ID获取评价列表
     * 
     * @param chapterId 章节ID
     * @return 评价列表
     */
    @GetMapping("/list/{chapterId}")
    public RestBean<List<ChapterRating>> getRatingsByChapter(@PathVariable Long chapterId) {
        List<ChapterRating> ratings = chapterRatingService.getRatingsByChapter(chapterId);
        return RestBean.success("获取成功", ratings);
    }
    
    /**
     * 根据章节ID获取评价详情列表（包含学生信息）
     * 
     * @param chapterId 章节ID
     * @return 评价DTO列表
     */
    @GetMapping("/detail/chapter/{chapterId}")
    public RestBean<List<ChapterRatingDTO>> getChapterRatingDetails(@PathVariable Long chapterId) {
        List<ChapterRatingDTO> ratings = chapterRatingService.getChapterRatingDetails(chapterId);
        return RestBean.success("获取成功", ratings);
    }
    
    /**
     * 获取章节的平均评分
     * 
     * @param chapterId 章节ID
     * @return 平均评分
     */
    @GetMapping("/average/{chapterId}")
    public RestBean<Double> getChapterAverageRating(@PathVariable Long chapterId) {
        Double averageRating = chapterRatingService.getChapterAverageRating(chapterId);
        return RestBean.success("获取成功", averageRating != null ? averageRating : 0.0);
    }
    
    /**
     * 根据学生ID获取该学生的所有评价
     * 
     * @param studentId 学生ID
     * @return 评价DTO列表
     */
    @GetMapping("/student/{studentId}")
    public RestBean<List<ChapterRatingDTO>> getStudentRatings(@PathVariable Long studentId) {
        List<ChapterRatingDTO> ratings = chapterRatingService.getStudentRatings(studentId);
        return RestBean.success("获取成功", ratings);
    }
} 