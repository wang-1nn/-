package org.example.backend.service.impl;

import org.example.backend.entity.dto.ChapterRatingDTO;
import org.example.backend.entity.pojo.ChapterRating;
import org.example.backend.mapper.ChapterRatingMapper;
import org.example.backend.mapper.CourseChapterMapper;
import org.example.backend.service.ChapterRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 章节评价服务实现类
 */
@Service
public class ChapterRatingServiceImpl implements ChapterRatingService {
    
    @Autowired
    private ChapterRatingMapper chapterRatingMapper;
    
    @Autowired
    private CourseChapterMapper courseChapterMapper;
    
    @Override
    @Transactional
    public Long submitRating(ChapterRating rating) {
        // 检查该学生是否已经评价过该章节
        ChapterRating existingRating = chapterRatingMapper.findByStudentAndChapter(rating.getStudentId(), rating.getChapterId());
        
        if (existingRating != null) {
            // 如果已经评价过，则更新评价
            existingRating.setRating(rating.getRating());
            existingRating.setUpdatedAt(new Date());
            chapterRatingMapper.update(existingRating);
            return existingRating.getId();
        } else {
            // 设置创建和更新时间
            rating.setCreatedAt(new Date());
            rating.setUpdatedAt(new Date());
            
            // 保存评价信息
            chapterRatingMapper.insert(rating);
            return rating.getId();
        }
    }
    
    @Override
    @Transactional
    public boolean updateRating(ChapterRating rating) {
        // 设置更新时间
        rating.setUpdatedAt(new Date());
        
        // 更新评价信息
        return chapterRatingMapper.update(rating) > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteRating(Long ratingId) {
        // 删除评价
        return chapterRatingMapper.deleteById(ratingId) > 0;
    }
    
    @Override
    public ChapterRating getStudentRating(Long studentId, Long chapterId) {
        // 查询学生对章节的评价
        return chapterRatingMapper.findByStudentAndChapter(studentId, chapterId);
    }
    
    @Override
    public List<ChapterRating> getRatingsByChapter(Long chapterId) {
        // 查询章节的所有评价
        return chapterRatingMapper.findByChapterId(chapterId);
    }
    
    @Override
    public List<ChapterRatingDTO> getChapterRatingDetails(Long chapterId) {
        // 查询章节的评价详情（包含学生信息）
        return chapterRatingMapper.getChapterRatingDetails(chapterId);
    }
    
    @Override
    public Double getChapterAverageRating(Long chapterId) {
        // 查询章节的平均评分
        return courseChapterMapper.getAverageRating(chapterId);
    }
    
    @Override
    public List<ChapterRatingDTO> getStudentRatings(Long studentId) {
        // 查询学生的所有评价
        return chapterRatingMapper.getStudentRatings(studentId);
    }
} 