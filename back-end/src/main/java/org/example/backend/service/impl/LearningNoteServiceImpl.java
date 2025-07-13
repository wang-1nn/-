package org.example.backend.service.impl;

import org.example.backend.entity.dto.LearningNoteDTO;
import org.example.backend.entity.pojo.LearningNote;
import org.example.backend.mapper.LearningNoteMapper;
import org.example.backend.service.LearningNoteService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 学习笔记服务实现类
 */
@Service
public class LearningNoteServiceImpl implements LearningNoteService {
    
    @Autowired
    private LearningNoteMapper learningNoteMapper;
    
    @Override
    @Transactional
    public Long createNote(LearningNote note) {
        // 设置创建和更新时间
        note.setCreatedAt(new Date());
        note.setUpdatedAt(new Date());
        
        // 保存笔记信息
        learningNoteMapper.insert(note);
        
        return note.getId();
    }
    
    @Override
    @Transactional
    public boolean updateNote(LearningNote note) {
        // 设置更新时间
        note.setUpdatedAt(new Date());
        
        // 更新笔记信息
        return learningNoteMapper.update(note) > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteNote(Long noteId) {
        // 删除笔记
        return learningNoteMapper.deleteById(noteId) > 0;
    }
    
    @Override
    public LearningNoteDTO getNoteDetail(Long noteId) {
        // 查询笔记基本信息
        LearningNote note = learningNoteMapper.selectById(noteId);
        
        if (note == null) {
            return null;
        }
        
        // 转换为DTO
        LearningNoteDTO noteDTO = new LearningNoteDTO();
        BeanUtils.copyProperties(note, noteDTO);
        
        // 这里可以查询关联信息，例如学生姓名、课程标题、章节标题等
        // 为了简化，这里暂不实现，实际开发中可通过JOIN查询或单独查询来填充这些字段
        
        return noteDTO;
    }
    
    @Override
    public List<LearningNote> getNotesByStudentAndContent(Long studentId, Long contentId) {
        // 查询学生对特定内容的笔记列表
        return learningNoteMapper.findByStudentAndContent(studentId, contentId);
    }
    
    @Override
    public List<LearningNote> getNotesByStudentAndChapter(Long studentId, Long chapterId) {
        // 查询学生对特定章节的笔记列表
        return learningNoteMapper.findByStudentAndChapter(studentId, chapterId);
    }
    
    @Override
    public List<LearningNoteDTO> getStudentNotesByCourse(Long studentId, Long courseId) {
        // 查询学生在特定课程中的笔记列表（带详细信息）
        return learningNoteMapper.getStudentNotesByCourse(studentId, courseId);
    }
    
    @Override
    public List<LearningNoteDTO> getStudentAllNotes(Long studentId) {
        // 查询学生的所有笔记列表（带详细信息）
        return learningNoteMapper.getStudentAllNotes(studentId);
    }
} 