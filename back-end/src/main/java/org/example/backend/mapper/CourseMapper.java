package org.example.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface CourseMapper {
    
    /**
     * 获取所有去重的课程标题
     * @return 课程标题列表
     */
    @Select("SELECT DISTINCT title FROM courses ORDER BY title")
    List<String> findDistinctTitles();
    
    /**
     * 获取所有去重的课程标题，包含ID和描述信息
     * @return 课程信息列表
     */
    @Select("SELECT DISTINCT title as name, " +
            "'课程基础知识与应用' as description " +
            "FROM courses ORDER BY title")
    List<Map<String, Object>> findDistinctSubjects();
} 