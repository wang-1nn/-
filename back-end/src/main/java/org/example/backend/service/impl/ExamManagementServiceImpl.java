package org.example.backend.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.backend.entity.dto.ExamCreateRequest;
import org.example.backend.entity.pojo.Exam;
import org.example.backend.entity.pojo.Question;
import org.example.backend.entity.vo.ExamVO;
import org.example.backend.entity.vo.ExamListVO;
import org.example.backend.entity.vo.QuestionSelectionVO;
import org.example.backend.entity.vo.CourseClassVO;
import org.example.backend.entity.pojo.Question;
import java.math.BigDecimal;
import org.example.backend.mapper.ExamMapper;
import org.example.backend.service.ExamManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 考试管理服务实现
 */
@Slf4j
@Service
public class ExamManagementServiceImpl implements ExamManagementService {

    @Autowired
    private ExamMapper examMapper;

    @Override
    public List<ExamVO> getTeacherExams(Long teacherId) {
        try {
            List<ExamVO> exams = examMapper.getExamsByTeacher(teacherId);
            
            // 设置考试状态
            LocalDateTime now = LocalDateTime.now();
            for (ExamVO exam : exams) {
                exam.setStatus(determineExamStatus(exam, now));
            }
            
            return exams;
        } catch (Exception e) {
            log.error("获取教师考试列表失败，teacherId: {}", teacherId, e);
            throw new RuntimeException("获取考试列表失败");
        }
    }

    @Override
    public List<QuestionSelectionVO> getQuestionsForSelection(Long teacherId, String subject, Integer page, Integer size) {
        try {
            Integer offset = (page - 1) * size;

            return examMapper.getQuestionsForSelection(subject, size, offset);
        } catch (Exception e) {
            log.error("获取题目列表失败，teacherId: {}, subject: {}", teacherId, subject, e);
            throw new RuntimeException("获取题目列表失败");
        }
    }

    @Override
    public Integer countQuestions(Long teacherId, String subject) {
        try {
            String createdBy = teacherId.toString();
            return examMapper.countQuestions(createdBy, subject);
        } catch (Exception e) {
            log.error("统计题目数量失败，teacherId: {}, subject: {}", teacherId, subject, e);
            return 0;
        }
    }



    @Override
    public List<CourseClassVO> getTeacherCoursesAndClasses(Long teacherId) {
        try {
            // 获取教师的课程
            List<CourseClassVO> courses = examMapper.getTeacherCourses(teacherId);
            
            // 为每个课程获取班级列表
            for (CourseClassVO course : courses) {
                List<CourseClassVO.ClassInfo> classes = examMapper.getClassesByCourse(course.getCourseId());
                course.setClasses(classes);
            }
            
            return courses;
        } catch (Exception e) {
            log.error("获取教师课程班级列表失败，teacherId: {}", teacherId, e);
            throw new RuntimeException("获取课程班级列表失败");
        }
    }

    @Override
    @Transactional
    public Long createExam(ExamCreateRequest request, Long teacherId) {
        try {
            // 为每个班级创建考试
            Long firstExamId = null;
            
            for (Long classId : request.getClassIds()) {
                // 获取开课ID
                Long offeringId = examMapper.getOfferingId(request.getCourseId(), classId);
                if (offeringId == null) {
                    throw new RuntimeException("课程和班级的开课记录不存在");
                }
                
                // 创建考试记录
                Exam exam = new Exam();
                exam.setOfferingId(offeringId);
                exam.setCreatorId(teacherId);
                exam.setTitle(request.getTitle());
                exam.setExamType(request.getExamType());
                exam.setStartTime(request.getStartTime());
                exam.setEndTime(request.getEndTime());
                
                examMapper.insertExam(exam);
                
                if (firstExamId == null) {
                    firstExamId = exam.getId();
                }
                
                log.info("创建考试成功，examId: {}, offeringId: {}", exam.getId(), offeringId);
            }
            
            return firstExamId;
        } catch (Exception e) {
            log.error("创建考试失败，teacherId: {}, request: {}", teacherId, request, e);
            throw new RuntimeException("创建考试失败：" + e.getMessage());
        }
    }

    @Override
    public Boolean updateExam(Long examId, ExamCreateRequest request, Long teacherId) {
        // TODO: 实现更新考试
        throw new UnsupportedOperationException("暂未实现");
    }

//    @Override
//    public List<String> getQuestionSubjects(Long teacherId) {
//        try {
//            String createdBy = teacherId.toString();
//            return examMapper.getQuestionSubjects(createdBy);
//        } catch (Exception e) {
//            log.error("获取题目学科列表失败，teacherId: {}", teacherId, e);
//            return new ArrayList<>();
//        }
//    }
    @Override
    public List<String> getQuestionSubjects(Long teacherId) {
        try {
            String createdBy = teacherId.toString();
            return examMapper.getQuestionSubjects(createdBy);
        } catch (Exception e) {
            log.error("获取题目学科列表失败，teacherId: {}", teacherId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<ExamListVO> getTeacherExams(Long teacherId, String status, String keyword, Integer page, Integer size) {
        try {
            Integer offset = (page - 1) * size;
            List<ExamListVO> exams = examMapper.getTeacherExams(teacherId, status, keyword, size, offset);

            // 计算参与人数（这里可以根据实际需求调整）
            for (ExamListVO exam : exams) {
                // 暂时设置为0，后续可以通过查询班级学生数量来设置
                exam.setParticipantCount(0);
                if (exam.getSubmissionCount() == null) {
                    exam.setSubmissionCount(0);
                }
            }

            return exams;
        } catch (Exception e) {
            log.error("获取教师考试列表失败，teacherId: {}, status: {}, keyword: {}", teacherId, status, keyword, e);
            return new ArrayList<>();
        }
    }

    @Override
    public Integer countTeacherExams(Long teacherId, String status, String keyword) {
        try {
            return examMapper.countTeacherExams(teacherId, status, keyword);
        } catch (Exception e) {
            log.error("统计教师考试数量失败，teacherId: {}, status: {}, keyword: {}", teacherId, status, keyword, e);
            return 0;
        }
    }

    @Override
    public ExamVO getExamDetail(Long examId, Long teacherId) {
        try {
            ExamVO examDetail = examMapper.getExamDetail(examId, teacherId);
            if (examDetail != null) {
                // 获取考试题目
                List<Question> questions = examMapper.getExamQuestions(examId.toString());
                examDetail.setQuestions(questions);

                // 计算总分和题目数量
                if (questions != null && !questions.isEmpty()) {
                    examDetail.setQuestionCount(questions.size());
                    BigDecimal totalScore = questions.stream()
                            .map(q -> q.getScore() != null ? q.getScore() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    examDetail.setTotalScore(totalScore.intValue());
                } else {
                    examDetail.setQuestionCount(0);
                    examDetail.setTotalScore(0);
                }
            }
            return examDetail;
        } catch (Exception e) {
            log.error("获取考试详情失败，examId: {}, teacherId: {}", examId, teacherId, e);
            return null;
        }
    }

    @Override
    @Transactional
    public Boolean deleteExam(Long examId, Long teacherId) {
        try {
            // 先删除考试相关的题目
            examMapper.deleteExamQuestions(examId.toString());

            // 再删除考试记录
            Integer result = examMapper.deleteExam(examId, teacherId);

            return result != null && result > 0;
        } catch (Exception e) {
            log.error("删除考试失败，examId: {}, teacherId: {}", examId, teacherId, e);
            return false;
        }
    }

    /**
     * 确定考试状态
     */
    private String determineExamStatus(ExamVO exam, LocalDateTime now) {
        if (exam.getStartTime() == null || exam.getEndTime() == null) {
            return "draft";
        }

        if (now.isBefore(exam.getStartTime())) {
            return "upcoming";
        } else if (now.isAfter(exam.getEndTime())) {
            return "completed";
        } else {
            return "ongoing";
        }
    }
}
