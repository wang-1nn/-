package org.example.backend.service;

import org.example.backend.entity.dto.LearningNoteDTO;
import org.example.backend.entity.pojo.LearningNote;

import java.util.List;

/**
 * 学习笔记服务接口
 */
public interface LearningNoteService {
    
    /**
     * 创建学习笔记
     * 
     * @param note 笔记信息
     * @return 创建的笔记ID
     */
    Long createNote(LearningNote note);
    
    /**
     * 更新学习笔记
     * 
     * @param note 笔记信息
     * @return 是否成功
     */
    boolean updateNote(LearningNote note);
    
    /**
     * 删除学习笔记
     * 
     * @param noteId 笔记ID
     * @return 是否成功
     */
    boolean deleteNote(Long noteId);
    
    /**
     * 获取笔记详情
     * 
     * @param noteId 笔记ID
     * @return 笔记详情
     */
    LearningNoteDTO getNoteDetail(Long noteId);
    
    /**
     * 根据学生ID和内容ID获取笔记列表
     * 
     * @param studentId 学生ID
     * @param contentId 内容ID
     * @return 笔记列表
     */
    List<LearningNote> getNotesByStudentAndContent(Long studentId, Long contentId);
    
    /**
     * 根据学生ID和章节ID获取笔记列表
     * 
     * @param studentId 学生ID
     * @param chapterId 章节ID
     * @return 笔记列表
     */
    List<LearningNote> getNotesByStudentAndChapter(Long studentId, Long chapterId);
    
    /**
     * 根据学生ID和课程ID获取笔记DTO列表
     * 
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 笔记DTO列表
     */
    List<LearningNoteDTO> getStudentNotesByCourse(Long studentId, Long courseId);
    
    /**
     * 获取学生的所有笔记
     * 
     * @param studentId 学生ID
     * @return 笔记DTO列表
     */
    List<LearningNoteDTO> getStudentAllNotes(Long studentId);
} 