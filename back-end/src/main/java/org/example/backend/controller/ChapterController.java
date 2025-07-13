package org.example.backend.controller;

import org.example.backend.entity.dto.ChapterContentDTO;
import org.example.backend.entity.dto.CourseChapterDTO;
import org.example.backend.entity.pojo.ChapterContent;
import org.example.backend.entity.pojo.CourseChapter;
import org.example.backend.service.ChapterService;
import org.example.backend.util.RestBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.backend.mapper.ChapterContentMapper;

/**
 * 章节控制器
 */
@RestController
@RequestMapping("/api/chapter")
public class ChapterController {
    
    private static final Logger logger = LoggerFactory.getLogger(ChapterController.class);

    @Autowired
    private ChapterService chapterService;
    
    @Autowired
    private ChapterContentMapper chapterContentMapper;
    
    /**
     * 创建章节
     * 
     * @param chapter 章节信息
     * @return 结果
     */
    @PostMapping("/create")
    public RestBean<Long> createChapter(@RequestBody CourseChapter chapter) {
        Long chapterId = chapterService.createChapter(chapter);
        return RestBean.success("创建成功", chapterId);
    }
    
    /**
     * 更新章节
     * 
     * @param chapter 章节信息
     * @return 结果
     */
    @PutMapping("/update")
    public RestBean<Void> updateChapter(@RequestBody CourseChapter chapter) {
        boolean success = chapterService.updateChapter(chapter);
        return success ? RestBean.success("更新章节成功") : RestBean.failure(400, "更新章节失败");
    }
    
    /**
     * 删除章节
     * 
     * @param chapterId 章节ID
     * @return 结果
     */
    @DeleteMapping("/delete/{chapterId}")
    public RestBean<Void> deleteChapter(@PathVariable Long chapterId) {
        boolean success = chapterService.deleteChapter(chapterId);
        return success ? RestBean.success("删除章节成功") : RestBean.failure(400, "删除章节失败");
    }
    
    /**
     * 获取章节详情
     * 
     * @param chapterId 章节ID
     * @return 章节详情
     */
    @GetMapping("/detail/{chapterId}")
    public RestBean<CourseChapterDTO> getChapterDetail(@PathVariable Long chapterId) {
        CourseChapterDTO chapter = chapterService.getChapterDetail(chapterId);
        return chapter != null ? RestBean.success("获取成功", chapter) : RestBean.failure(404, "章节不存在");
    }
    
    /**
     * 根据课程ID获取章节列表
     * 
     * @param courseId 课程ID
     * @return 章节列表
     */
    @GetMapping("/list/{courseId}")
    public RestBean<List<CourseChapter>> getChaptersByCourse(@PathVariable Long courseId) {
        List<CourseChapter> chapters = chapterService.getChaptersByCourse(courseId);
        return RestBean.success("获取成功", chapters);
    }
    
    /**
     * 根据课程ID获取章节详情列表
     * 
     * @param courseId 课程ID
     * @return 章节详情列表
     */
    @GetMapping("/detail/course/{courseId}")
    public RestBean<List<CourseChapterDTO>> getChapterDetailsByCourse(@PathVariable Long courseId) {
        List<CourseChapterDTO> chapters = chapterService.getChapterDetailsByCourse(courseId);
        return RestBean.success("获取成功", chapters);
    }
    
    /**
     * 获取学生在课程中的章节学习进度
     * 
     * @param courseId 课程ID
     * @param studentId 学生ID
     * @return 章节学习进度列表
     */
    @GetMapping("/progress/{courseId}/{studentId}")
    public RestBean<List<CourseChapterDTO>> getStudentChapterProgress(
            @PathVariable Long courseId, @PathVariable Long studentId) {
        List<CourseChapterDTO> progress = chapterService.getStudentChapterProgress(courseId, studentId);
        return RestBean.success("获取成功", progress);
    }
    
    /**
     * 添加章节内容
     * 
     * @param content 内容信息
     * @return 结果
     */
    @PostMapping("/content/add")
    public RestBean<Long> addChapterContent(@RequestBody ChapterContent content) {
        Long contentId = chapterService.addChapterContent(content);
        return RestBean.success("添加成功", contentId);
    }
    
    /**
     * 更新章节内容
     * 
     * @param content 内容信息
     * @return 结果
     */
    @PutMapping("/content/update")
    public RestBean<Void> updateChapterContent(@RequestBody ChapterContent content) {
        boolean success = chapterService.updateChapterContent(content);
        return success ? RestBean.success("更新内容成功") : RestBean.failure(400, "更新内容失败");
    }
    
    /**
     * 删除章节内容
     * 
     * @param contentId 内容ID
     * @return 结果
     */
    @DeleteMapping("/content/delete/{contentId}")
    public RestBean<Void> deleteChapterContent(@PathVariable Long contentId) {
        boolean success = chapterService.deleteChapterContent(contentId);
        return success ? RestBean.success("删除内容成功") : RestBean.failure(400, "删除内容失败");
    }
    
    /**
     * 根据章节ID获取内容列表
     * 
     * @param chapterId 章节ID
     * @return 内容列表
     */
    @GetMapping("/content/list/{chapterId}")
    public RestBean<List<ChapterContent>> getContentsByChapter(@PathVariable Long chapterId) {
        List<ChapterContent> contents = chapterService.getContentsByChapter(chapterId);
        return RestBean.success("获取成功", contents);
    }
    
    /**
     * 获取章节内容详情
     * 
     * @param contentId 内容ID
     * @return 内容详情
     */
    @GetMapping("/content/detail/{contentId}")
    public RestBean<ChapterContentDTO> getContentDetail(@PathVariable Long contentId) {
        ChapterContentDTO content = chapterService.getContentDetail(contentId);
        return content != null ? RestBean.success("获取成功", content) : RestBean.failure(404, "内容不存在");
    }
    
    /**
     * 获取学生对章节内容的学习进度
     * 
     * @param chapterId 章节ID
     * @param studentId 学生ID
     * @return 内容学习进度列表
     */
    @GetMapping("/content/progress/{chapterId}/{studentId}")
    public RestBean<List<ChapterContentDTO>> getStudentContentProgress(
            @PathVariable Long chapterId, @PathVariable Long studentId) {
        List<ChapterContentDTO> progress = chapterService.getStudentContentProgress(chapterId, studentId);
        return RestBean.success("获取成功", progress);
    }
    
    /**
     * 记录学生学习章节的行为
     * 
     * @param studentId 学生ID
     * @param chapterId 章节ID
     * @param contentId 内容ID
     * @param duration 学习时长（秒）
     * @return 结果
     */
    @PostMapping("/learning/record")
    public RestBean<Void> recordLearningBehavior(
            @RequestParam Long studentId,
            @RequestParam Long chapterId,
            @RequestParam Long contentId,
            @RequestParam Integer duration) {
        boolean success = chapterService.recordLearningBehavior(studentId, chapterId, contentId, duration);
        return success ? RestBean.success("记录学习行为成功") : RestBean.failure(400, "记录学习行为失败");
    }
    
    /**
     * 标记章节为已完成
     * 
     * @param studentId 学生ID
     * @param chapterId 章节ID
     * @return 结果
     */
    @PostMapping("/learning/complete")
    public RestBean<Void> markChapterAsCompleted(
            @RequestParam Long studentId, @RequestParam Long chapterId) {
        boolean success = chapterService.markChapterAsCompleted(studentId, chapterId);
        return success ? RestBean.success("标记章节完成成功") : RestBean.failure(400, "标记章节完成失败");
    }

    /**
     * 检查章节内容 - 用于测试和调试
     * 
     * @param chapterId 章节ID
     * @return 章节内容
     */
    @GetMapping("/debug/chapter-contents/{chapterId}")
    public RestBean<Object> checkChapterContents(@PathVariable Long chapterId) {
        logger.info("DEBUG: 检查章节内容, chapterId={}", chapterId);
        
        try {
            if (chapterId == null) {
                return RestBean.failure(400, "章节ID不能为空");
            }
            
            // 直接从Mapper层获取数据，跳过Service层的业务逻辑
            List<Map<String, Object>> contents = chapterContentMapper.selectChapterContents(chapterId);
            
            if (contents == null || contents.isEmpty()) {
                return RestBean.success("章节内容不存在", Map.of(
                    "chapterId", chapterId,
                    "exists", false,
                    "message", "没有找到该章节的内容，请检查数据库"
                ));
            } else {
                return RestBean.success("获取章节内容成功", Map.of(
                    "chapterId", chapterId,
                    "exists", true,
                    "count", contents.size(),
                    "contents", contents
                ));
            }
        } catch (Exception e) {
            logger.error("DEBUG: 检查章节内容异常", e);
            return RestBean.failure(500, "检查章节内容异常: " + e.getMessage());
        }
    }
} 