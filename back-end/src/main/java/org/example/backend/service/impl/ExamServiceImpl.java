package org.example.backend.service.impl;

import jakarta.annotation.Resource;
import org.example.backend.entity.dto.ExamDTO;
import org.example.backend.entity.dto.ExamDetailDTO;
import org.example.backend.entity.dto.SubmissionDTO;
import org.example.backend.entity.pojo.Answer;
import org.example.backend.entity.pojo.Exam;
import org.example.backend.entity.pojo.Question;
import org.example.backend.entity.pojo.Submission;
import org.example.backend.mapper.ExamMapper;
import org.example.backend.service.ExamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 考试服务实现类
 */
@Service
public class ExamServiceImpl implements ExamService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExamServiceImpl.class);
    
    @Resource
    private ExamMapper examMapper;
    
    @Override
    public List<ExamDTO> getStudentExams(Long studentId) {
        logger.info("获取学生可参加的考试列表: studentId={}", studentId);
        
        List<Exam> exams = examMapper.findExamsByStudentId(studentId);
        List<ExamDTO> examDTOs = new ArrayList<>();
        
        for (Exam exam : exams) {
            ExamDTO dto = new ExamDTO();
            BeanUtils.copyProperties(exam, dto);
            
            // 获取学生的提交记录
            Submission submission = examMapper.findSubmissionByStudentAndExam(studentId, exam.getId());
            if (submission != null) {
                dto.setIsSubmitted(submission.getStatus() > 0);
                dto.setUserScore(submission.getTotalScore() != null ? submission.getTotalScore().intValue() : null);
                dto.setSubmissionStatus(submission.getStatusText());
            } else {
                dto.setIsSubmitted(false);
                dto.setSubmissionStatus("未开始");
            }
            
            examDTOs.add(dto);
        }
        
        return examDTOs;
    }
    
    @Override
    public ExamDetailDTO getExamDetail(Long examId, Long studentId) {
        logger.info("获取考试详情: examId={}, studentId={}", examId, studentId);
        
        Exam exam = examMapper.findExamById(examId, studentId);
        if (exam == null) {
            logger.warn("未找到考试: examId={}", examId);
            return null;
        }
        
        ExamDetailDTO dto = new ExamDetailDTO();
        BeanUtils.copyProperties(exam, dto);
        
        // 获取学生的提交记录
        Submission submission = examMapper.findSubmissionByStudentAndExam(studentId, examId);
        if (submission != null) {
            dto.setSubmissionId(submission.getId());
            dto.setIsSubmitted(submission.getStatus() > 0);
            dto.setUserScore(submission.getTotalScore() != null ? submission.getTotalScore().intValue() : null);
            dto.setSubmissionStatus(submission.getStatusText());
            dto.setSubmittedAt(submission.getSubmittedAt());
        } else {
            dto.setIsSubmitted(false);
            dto.setSubmissionStatus("未开始");
        }
        
        // 设置考试规则
        dto.setAllowReview(true); // 默认允许查看答案
        dto.setShuffleQuestions(false); // 默认不打乱题目顺序
        
        // 获取题目列表，无论考试状态如何
        List<Question> questions = examMapper.findQuestionsByExamId(examId);
        System.out.println("题目列表"+questions);
        logger.info("查询到的题目数量: examId={}, count={}", examId, questions.size());
        
        // 如果已提交，获取学生的答案
        if (submission != null && submission.getStatus() > 0) {
            List<Answer> answers = examMapper.findAnswersBySubmissionId(submission.getId());
            logger.info("查询到的答案数量: submissionId={}, count={}", submission.getId(), answers.size());
            Map<Long, Answer> answerMap = answers.stream()
                    .collect(Collectors.toMap(Answer::getQuestionId, a -> a));
            
            // 将答案关联到题目
            for (Question question : questions) {
                Answer answer = answerMap.get(question.getId().longValue());
                if (answer != null) {
                    question.setUserAnswer(answer.getUserAnswer());
                    question.setUserScore(answer.getScore());
                    question.setIsCorrect(answer.getIsCorrect());
                }
            }
        }
        
        dto.setQuestions(questions);
        
        return dto;
    }
    
    @Override
    @Transactional
    public Long startExam(Long examId, Long studentId) {
        logger.info("开始考试: examId={}, studentId={}", examId, studentId);
        
        // 检查是否已有提交记录
        Submission existingSubmission = examMapper.findSubmissionByStudentAndExam(studentId, examId);
        if (existingSubmission != null) {
            logger.info("学生已有提交记录: submissionId={}", existingSubmission.getId());
            return existingSubmission.getId();
        }
        
        // 检查考试是否可以开始
        Exam exam = examMapper.findExamById(examId, studentId);
        if (exam == null) {
            logger.warn("未找到考试: examId={}", examId);
            return null;
        }
        
        if (!"进行中".equals(exam.getStatus())) {
            logger.warn("考试不在进行中: examId={}, status={}", examId, exam.getStatus());
            return null;
        }
        
        // 创建提交记录
        Submission submission = new Submission();
        submission.setExamId(examId);
        submission.setStudentId(studentId);
        submission.setStatus(0); // 进行中
        submission.setSubmittedAt(null);
        submission.setTotalScore(null);
        
        int rows = examMapper.createSubmission(submission);
        if (rows > 0) {
            logger.info("创建提交记录成功: submissionId={}", submission.getId());
            return submission.getId();
        } else {
            logger.error("创建提交记录失败");
            return null;
        }
    }
    
    @Override
    @Transactional
    public boolean saveAnswer(Long submissionId, Long questionId, String answer) {
        logger.info("保存学生答案: submissionId={}, questionId={}", submissionId, questionId);
        
        // 查找是否已有答案
        List<Answer> answers = examMapper.findAnswersBySubmissionId(submissionId);
        Answer existingAnswer = answers.stream()
                .filter(a -> a.getQuestionId().equals(questionId))
                .findFirst()
                .orElse(null);
        
        if (existingAnswer != null) {
            // 更新答案
            existingAnswer.setUserAnswer(answer);
            existingAnswer.setAnsweredAt(LocalDateTime.now());
            return examMapper.updateAnswer(existingAnswer) > 0;
        } else {
            // 创建新答案
            Answer newAnswer = new Answer();
            newAnswer.setSubmissionId(submissionId);
            newAnswer.setQuestionId(questionId);
            newAnswer.setUserAnswer(answer);
            newAnswer.setAnsweredAt(LocalDateTime.now());
            return examMapper.saveAnswer(newAnswer) > 0;
        }
    }
    
    @Override
    @Transactional
    public boolean submitExam(Long submissionId) {
        logger.info("提交考试: submissionId={}", submissionId);
        
        // 获取提交记录
        List<Answer> answers = examMapper.findAnswersBySubmissionId(submissionId);
        if (answers.isEmpty()) {
            logger.warn("未找到答案: submissionId={}", submissionId);
            return false;
        }
        
        // 获取提交记录
        Submission submission = null;
        for (Answer answer : answers) {
            if (submission == null) {
                // 通过第一个答案获取提交记录和考试信息
                Map<String, Object> submissionMap = examMapper.findScoreDetailBySubmissionId(submissionId);
                if (submissionMap == null) {
                    logger.warn("未找到提交记录: submissionId={}", submissionId);
                    return false;
                }
                
                submission = new Submission();
                submission.setId(submissionId);
                submission.setStatus(1); // 已提交
                submission.setSubmittedAt(LocalDateTime.now());
                
                // 自动批改选择题和判断题
                Long examId = (Long) submissionMap.get("examId");
                List<Question> questions = examMapper.findQuestionsByExamId(examId);
                Map<Long, Question> questionMap = questions.stream()
                        .collect(Collectors.toMap(q -> q.getId().longValue(), q -> q));
                
                // 计算总分
                BigDecimal totalScore = BigDecimal.ZERO;
                for (Answer ans : answers) {
                    Question question = questionMap.get(ans.getQuestionId());
                    if (question != null && 
                        ("单选题".equals(question.getQuestionType()) || 
                         "多选题".equals(question.getQuestionType()) || 
                         "判断题".equals(question.getQuestionType()))) {
                        
                        // 自动批改
                        boolean isCorrect = question.getAnswer().equals(ans.getUserAnswer());
                        ans.setIsCorrect(isCorrect);
                        ans.setScore(isCorrect ? question.getScore() : BigDecimal.ZERO);
                        ans.setGradedAt(LocalDateTime.now());
                        ans.setGradedBy(0L); // 系统批改
                        
                        // 更新答案
                        examMapper.updateAnswer(ans);
                        
                        // 累加分数
                        if (ans.getScore() != null) {
                            totalScore = totalScore.add(ans.getScore());
                        }
                    }
                }
                
                submission.setTotalScore(totalScore);
                break;
            }
        }
        
        if (submission != null) {
            return examMapper.updateSubmission(submission) > 0;
        }
        
        return false;
    }
    
    @Override
    public List<SubmissionDTO> getStudentScores(Long studentId) {
        logger.info("获取学生的成绩列表: studentId={}", studentId);
        
        List<Map<String, Object>> scores = examMapper.findScoresByStudentId(studentId);
        List<SubmissionDTO> submissionDTOs = new ArrayList<>();
        
        for (Map<String, Object> score : scores) {
            SubmissionDTO dto = new SubmissionDTO();
            dto.setId((Long) score.get("submissionId"));
            dto.setExamId((Long) score.get("examId"));
            dto.setExamTitle((String) score.get("examTitle"));
            dto.setExamType((String) score.get("examType"));
            dto.setCourseName((String) score.get("courseName"));
            dto.setClassName((String) score.get("className"));
            dto.setSubmittedAt((LocalDateTime) score.get("submittedAt"));
            dto.setTotalScore((BigDecimal) score.get("score"));
            dto.setStatus((String) score.get("status"));
            
            submissionDTOs.add(dto);
        }
        
        return submissionDTOs;
    }
    
    @Override
    public Map<String, Object> getScoreDetail(Long submissionId) {
        logger.info("获取成绩详情: submissionId={}", submissionId);
        
        Map<String, Object> scoreDetail = examMapper.findScoreDetailBySubmissionId(submissionId);
        if (scoreDetail == null) {
            logger.warn("未找到成绩详情: submissionId={}", submissionId);
            return new HashMap<>();
        }
        
        // 获取答案详情
        List<Answer> answers = examMapper.findAnswersBySubmissionId(submissionId);
        List<Map<String, Object>> answerDetails = new ArrayList<>();
        
        for (Answer answer : answers) {
            Map<String, Object> answerDetail = new HashMap<>();
            answerDetail.put("questionId", answer.getQuestionId());
            answerDetail.put("userAnswer", answer.getUserAnswer());
            answerDetail.put("score", answer.getScore());
            answerDetail.put("isCorrect", answer.getIsCorrect());
            answerDetail.put("comment", answer.getComment());
            
            answerDetails.add(answerDetail);
        }
        
        scoreDetail.put("answers", answerDetails);
        return scoreDetail;
    }
    
    @Override
    public List<Question> getExamQuestions(Long examId) {
        logger.info("获取考试的题目列表: examId={}", examId);
        return examMapper.findQuestionsByExamId(examId);
    }
    
    @Override
    public List<Answer> getStudentAnswers(Long submissionId) {
        logger.info("获取学生在某次考试中的答案列表: submissionId={}", submissionId);
        return examMapper.findAnswersBySubmissionId(submissionId);
    }
    
    @Override
    public Map<String, Object> getStudentExamStats(Long studentId) {
        logger.info("获取学生的考试统计数据: studentId={}", studentId);
        
        // 获取学生所有的考试提交记录
        List<Map<String, Object>> submissions = examMapper.findScoresByStudentId(studentId);
        
        // 统计数据
        int totalExams = submissions.size();
        int completedExams = 0;
        int passedExams = 0;
        double totalScore = 0;
        double highestScore = 0;
        double lowestScore = 100;
        
        for (Map<String, Object> submission : submissions) {
            String status = (String) submission.get("status");
            
            // 只统计已批改的考试
            if ("已批改".equals(status) || "已提交".equals(status)) {
                completedExams++;
                
                // 获取分数
                Object scoreObj = submission.get("score");
                if (scoreObj != null) {
                    double score = 0;
                    if (scoreObj instanceof Number) {
                        score = ((Number) scoreObj).doubleValue();
                    } else if (scoreObj instanceof String) {
                        try {
                            score = Double.parseDouble((String) scoreObj);
                        } catch (NumberFormatException e) {
                            logger.warn("无法解析分数: {}", scoreObj);
                        }
                    }
                    
                    // 累加总分
                    totalScore += score;
                    
                    // 更新最高分和最低分
                    if (score > highestScore) {
                        highestScore = score;
                    }
                    if (score < lowestScore) {
                        lowestScore = score;
                    }
                    
                    // 统计及格数量（默认60分及格）
                    if (score >= 60) {
                        passedExams++;
                    }
                }
            }
        }
        
        // 计算平均分和及格率
        double averageScore = completedExams > 0 ? totalScore / completedExams : 0;
        double passRate = completedExams > 0 ? (double) passedExams / completedExams * 100 : 0;
        
        // 如果没有完成的考试，最低分设为0
        if (completedExams == 0) {
            lowestScore = 0;
        }
        
        // 组装结果
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalExams", totalExams);
        stats.put("completedExams", completedExams);
        stats.put("passedExams", passedExams);
        stats.put("averageScore", Math.round(averageScore * 10) / 10.0);  // 保留一位小数
        stats.put("highestScore", highestScore);
        stats.put("lowestScore", lowestScore);
        stats.put("passRate", Math.round(passRate * 10) / 10.0);  // 保留一位小数
        stats.put("upcomingExams", totalExams - completedExams);
        
        // 获取正在进行中的考试数量
        List<Exam> ongoingExams = examMapper.findOngoingExamsByStudentId(studentId);
        stats.put("ongoingExams", ongoingExams.size());
        
        return stats;
    }
    
    @Override
    public List<Map<String, Object>> getStudentScoreTrend(Long studentId, Integer limit) {
        logger.info("获取学生的成绩趋势数据: studentId={}, limit={}", studentId, limit);
        
        // 如果没有指定limit，默认返回最近10次考试
        int actualLimit = limit != null ? limit : 10;
        
        // 获取学生已完成的考试记录，按提交时间排序
        List<Map<String, Object>> submissions = examMapper.findCompletedScoresByStudentId(studentId, actualLimit);
        List<Map<String, Object>> trend = new ArrayList<>();
        
        for (Map<String, Object> submission : submissions) {
            Map<String, Object> item = new HashMap<>();
            item.put("examId", submission.get("examId"));
            item.put("examTitle", submission.get("examTitle"));
            item.put("submissionId", submission.get("submissionId"));
            item.put("submittedAt", submission.get("submittedAt"));
            item.put("score", submission.get("score"));
            item.put("courseName", submission.get("courseName"));
            
            trend.add(item);
        }
        
        return trend;
    }
} 