package org.example.backend.service;

import org.example.backend.entity.dto.ExamCreateRequest;
import org.example.backend.entity.vo.ExamVO;
import org.example.backend.entity.vo.ExamListVO;
import org.example.backend.entity.vo.QuestionSelectionVO;
import org.example.backend.entity.vo.CourseClassVO;

import java.util.List;

/**
 * 考试管理服务接口
 */
public interface ExamManagementService {

    /**
     * 获取教师的考试列表
     * @param teacherId 教师ID
     * @return 考试列表
     */
    List<ExamVO> getTeacherExams(Long teacherId);

    /**
     * 获取题目列表供选择
     * @param teacherId 教师ID
     * @param subject 学科（必填）
     * @param page 页码
     * @param size 每页大小
     * @return 题目列表
     */
    List<QuestionSelectionVO> getQuestionsForSelection(Long teacherId, String subject, Integer page, Integer size);

    /**
     * 统计题目数量
     * @param teacherId 教师ID
     * @param subject 学科（必填）
     * @return 题目总数
     */
    Integer countQuestions(Long teacherId, String subject);

    /**
     * 获取教师创建的题目的所有学科列表
     * @param teacherId 教师ID
     * @return 学科列表
     */
    List<String> getQuestionSubjects(Long teacherId);

    /**
     * 获取教师的考试列表
     * @param teacherId 教师ID
     * @param status 考试状态（可选）：即将开始、进行中、已结束、草稿
     * @param keyword 搜索关键字（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 考试列表
     */
    List<ExamListVO> getTeacherExams(Long teacherId, String status, String keyword, Integer page, Integer size);

    /**
     * 统计教师的考试数量
     * @param teacherId 教师ID
     * @param status 考试状态（可选）
     * @param keyword 搜索关键字（可选）
     * @return 考试总数
     */
    Integer countTeacherExams(Long teacherId, String status, String keyword);


    /**
     * 获取教师所教的课程和班级
     * @param teacherId 教师ID
     * @return 课程班级列表
     */
    List<CourseClassVO> getTeacherCoursesAndClasses(Long teacherId);

    /**
     * 创建考试
     * @param request 创建考试请求
     * @param teacherId 教师ID
     * @return 创建的考试ID
     */
    Long createExam(ExamCreateRequest request, Long teacherId);

    /**
     * 获取考试详情
     * @param examId 考试ID
     * @param teacherId 教师ID
     * @return 考试详情
     */
    ExamVO getExamDetail(Long examId, Long teacherId);

    /**
     * 更新考试
     * @param examId 考试ID
     * @param request 更新请求
     * @param teacherId 教师ID
     * @return 是否成功
     */
    Boolean updateExam(Long examId, ExamCreateRequest request, Long teacherId);

    /**
     * 删除考试
     * @param examId 考试ID
     * @param teacherId 教师ID
     * @return 是否成功
     */
    Boolean deleteExam(Long examId, Long teacherId);
}
