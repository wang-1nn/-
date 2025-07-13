package org.example.backend.controller;

import org.example.backend.entity.dto.LearningNoteDTO;
import org.example.backend.entity.pojo.LearningNote;
import org.example.backend.service.LearningNoteService;
import org.example.backend.util.RestBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学习笔记控制器
 */
@RestController
@RequestMapping("/api/note")
public class LearningNoteController {
    
    @Autowired
    private LearningNoteService learningNoteService;
    
    /**
     * 创建学习笔记
     * 
     * @param note 笔记信息
     * @return 结果
     */
    @PostMapping("/create")
    public RestBean<Long> createNote(@RequestBody LearningNote note) {
        Long noteId = learningNoteService.createNote(note);
        return RestBean.success("创建成功", noteId);
    }
    
    /**
     * 更新学习笔记
     * 
     * @param note 笔记信息
     * @return 结果
     */
    @PutMapping("/update")
    public RestBean<Void> updateNote(@RequestBody LearningNote note) {
        boolean success = learningNoteService.updateNote(note);
        return success ? RestBean.success("更新笔记成功") : RestBean.failure(400, "更新笔记失败");
    }
    
    /**
     * 删除学习笔记
     * 
     * @param noteId 笔记ID
     * @return 结果
     */
    @DeleteMapping("/delete/{noteId}")
    public RestBean<Void> deleteNote(@PathVariable Long noteId) {
        boolean success = learningNoteService.deleteNote(noteId);
        return success ? RestBean.success("删除笔记成功") : RestBean.failure(400, "删除笔记失败");
    }
    
    /**
     * 获取笔记详情
     * 
     * @param noteId 笔记ID
     * @return 笔记详情
     */
    @GetMapping("/detail/{noteId}")
    public RestBean<LearningNoteDTO> getNoteDetail(@PathVariable Long noteId) {
        LearningNoteDTO note = learningNoteService.getNoteDetail(noteId);
        return note != null ? RestBean.success("获取成功", note) : RestBean.failure(404, "笔记不存在");
    }
    
    /**
     * 根据学生ID和内容ID获取笔记列表
     * 
     * @param studentId 学生ID
     * @param contentId 内容ID
     * @return 笔记列表
     */
    @GetMapping("/list/content")
    public RestBean<List<LearningNote>> getNotesByStudentAndContent(
            @RequestParam Long studentId, @RequestParam Long contentId) {
        List<LearningNote> notes = learningNoteService.getNotesByStudentAndContent(studentId, contentId);
        return RestBean.success("获取成功", notes);
    }
    
    /**
     * 根据学生ID和章节ID获取笔记列表
     * 
     * @param studentId 学生ID
     * @param chapterId 章节ID
     * @return 笔记列表
     */
    @GetMapping("/list/chapter")
    public RestBean<List<LearningNote>> getNotesByStudentAndChapter(
            @RequestParam Long studentId, @RequestParam Long chapterId) {
        List<LearningNote> notes = learningNoteService.getNotesByStudentAndChapter(studentId, chapterId);
        return RestBean.success("获取成功", notes);
    }
    
    /**
     * 根据学生ID和课程ID获取笔记列表
     * 
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 笔记列表
     */
    @GetMapping("/list/course")
    public RestBean<List<LearningNoteDTO>> getStudentNotesByCourse(
            @RequestParam Long studentId, @RequestParam Long courseId) {
        List<LearningNoteDTO> notes = learningNoteService.getStudentNotesByCourse(studentId, courseId);
        return RestBean.success("获取成功", notes);
    }
    
    /**
     * 获取学生的所有笔记
     * 
     * @param studentId 学生ID
     * @return 笔记列表
     */
    @GetMapping("/list/student/{studentId}")
    public RestBean<List<LearningNoteDTO>> getStudentAllNotes(@PathVariable Long studentId) {
        List<LearningNoteDTO> notes = learningNoteService.getStudentAllNotes(studentId);
        return RestBean.success("获取成功", notes);
    }
} 