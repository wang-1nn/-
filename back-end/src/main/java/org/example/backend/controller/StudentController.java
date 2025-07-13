package org.example.backend.controller;

import jakarta.annotation.Resource;
import org.example.backend.entity.dto.CourseDTO;
import org.example.backend.entity.dto.CourseDetailDTO;
import org.example.backend.entity.dto.CourseResourceDTO;
import org.example.backend.entity.dto.DiscussionDTO;
import org.example.backend.entity.dto.QuizDTO;
import org.example.backend.entity.dto.LearningNoteDTO;
import org.example.backend.entity.dto.ChapterRatingDTO;
import org.example.backend.entity.dto.LearningStatisticsDTO;
import org.example.backend.entity.pojo.TodoTask;
import org.example.backend.entity.pojo.LearningNote;
import org.example.backend.entity.pojo.ChapterRating;
import org.example.backend.entity.vo.DashboardDataVO;
import org.example.backend.entity.vo.DashboardWidgetsVO.ScoreOverviewVO;
import org.example.backend.entity.vo.DashboardWidgetsVO.StudyTimeVO;
import org.example.backend.entity.vo.DashboardWidgetsVO.TodoTasksVO;
import org.example.backend.service.StudentService;
import org.example.backend.service.TeacherService;
import org.example.backend.util.RestBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * 学生相关接口控制器
 */
@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Resource
    TeacherService teacherService;

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    @Resource
    private StudentService studentService;

    /**
     * 获取学生仪表盘数据
     *
     * @param userId 学生ID
     * @return 仪表盘数据
     */
    @GetMapping("/dashboard")
    public RestBean<DashboardDataVO> getDashboardData(@RequestParam String userId) {
        logger.info("获取仪表盘数据, userId={}", userId);
        DashboardDataVO dashboardData = studentService.getDashboardData(userId);
        // 始终返回成功，即使数据为空也会返回一个空的DashboardDataVO对象
        return RestBean.success("获取仪表盘数据成功", dashboardData != null ? dashboardData : new DashboardDataVO());
    }

    /**
     * 获取学习统计数据
     *
     * @param userId 学生ID
     * @return 学习统计数据
     */
    @GetMapping("/dashboard/stats")
    public RestBean<Object> getLearningStats(@RequestParam String userId) {
        return RestBean.success("获取学习统计数据成功", studentService.getLearningStats(userId));
    }

    /**
     * 获取课程学习进度
     *
     * @param userId 学生ID
     * @return 课程学习进度
     */
    @GetMapping("/dashboard/courses")
    public RestBean<Object> getCourseProgress(@RequestParam String userId) {
        return RestBean.success("获取课程进度成功", studentService.getCourseProgress(userId));
    }

    /**
     * 获取个性化学习建议
     *
     * @param userId 学生ID
     * @return 学习建议列表
     */
    @GetMapping("/dashboard/recommendations")
    public RestBean<Object> getLearningRecommendations(@RequestParam String userId) {
        return RestBean.success("获取学习建议成功", studentService.getLearningRecommendations(userId));
    }
    
    /**
     * 获取成绩总览数据
     *
     * @param userId 学生ID
     * @return 成绩总览数据
     */
    @GetMapping("/dashboard/score-overview")
    public RestBean<ScoreOverviewVO> getScoreOverview(@RequestParam String userId) {
        logger.info("获取成绩总览数据, userId={}", userId);
        ScoreOverviewVO scoreOverview = studentService.getScoreOverview(userId);
        return RestBean.success("获取成绩总览数据成功", scoreOverview);
    }
    
    /**
     * 获取学习时长分析数据
     *
     * @param userId 学生ID
     * @return 学习时长分析数据
     */
    @GetMapping("/dashboard/study-time")
    public RestBean<StudyTimeVO> getStudyTime(@RequestParam String userId) {
        logger.info("获取学习时长分析数据, userId={}", userId);
        StudyTimeVO studyTime = studentService.getStudyTime(userId);
        return RestBean.success("获取学习时长分析数据成功", studyTime);
    }
    
    /**
     * 获取待办任务列表
     *
     * @param userId 学生ID
     * @return 待办任务数据
     */
    @GetMapping("/dashboard/todo-tasks")
    public RestBean<TodoTasksVO> getTodoTasks(@RequestParam String userId) {
        logger.info("获取待办任务列表, userId={}", userId);
        TodoTasksVO todoTasks = studentService.getTodoTasks(userId);
        return RestBean.success("获取待办任务列表成功", todoTasks);
    }
    
    /**
     * 添加待办任务
     *
     * @param task 待办任务对象
     * @return 操作结果
     */
    @PostMapping("/dashboard/todo-tasks")
    public RestBean<Object> addTodoTask(@RequestBody TodoTask task) {
        logger.info("添加待办任务, userId={}, title={}", task.getUserId(), task.getTitle());
        
        // 验证必要参数
        if (task.getUserId() == null || task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            return RestBean.failure(400, "缺少必要参数");
        }
        
        // 添加任务
        Long taskId = studentService.addTodoTask(task);
        if (taskId != null) {
            // 添加成功，返回最新任务列表
            TodoTasksVO todoTasks = studentService.getTodoTasks(task.getUserId().toString());
            return RestBean.success("添加待办任务成功", todoTasks);
        } else {
            return RestBean.failure(500, "添加待办任务失败");
        }
    }
    
    /**
     * 更新待办任务状态
     * 同时支持PUT请求和带有_method=PUT参数的POST请求
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @param completed 是否完成
     * @param method 请求方法（用于POST请求模拟PUT）
     * @return 操作结果
     */
    @RequestMapping(value = "/dashboard/todo-tasks/{taskId}", method = {RequestMethod.PUT, RequestMethod.POST})
    public RestBean<Object> updateTodoTaskStatus(
            @PathVariable Long taskId,
            @RequestParam String userId,
            @RequestParam Boolean completed,
            @RequestParam(value = "_method", required = false) String method) {
        
        logger.info("更新待办任务状态, taskId={}, completed={}, method={}", taskId, completed, method);
        
        // 如果是POST请求但_method不是PUT，则拒绝处理
        if (method != null && !method.equalsIgnoreCase("PUT")) {
            return RestBean.failure(400, "不支持的请求方法");
        }
        
        boolean success = studentService.updateTodoTaskStatus(taskId, completed);
        if (success) {
            // 更新成功，返回最新任务列表
            TodoTasksVO todoTasks = studentService.getTodoTasks(userId);
            return RestBean.success("更新待办任务状态成功", todoTasks);
        } else {
            return RestBean.failure(500, "更新待办任务状态失败");
        }
    }

    /**
     * 获取学生课程列表
     *
     * @param userId 学生ID
     * @return 课程列表
     */
    @GetMapping("/courses")
    public RestBean<List<CourseDTO>> getStudentCourses(@RequestParam Long userId) {
        logger.info("获取学生课程列表, userId={}", userId);
        List<CourseDTO> courses = studentService.getStudentCourses(userId);
        return RestBean.success("获取课程列表成功", courses);
    }
    
    /**
     * 获取课程详情
     *
     * @param userId 学生ID
     * @param courseId 课程ID
     * @return 课程详情
     */
    @GetMapping("/courses/{courseId}")
    public RestBean<CourseDetailDTO> getCourseDetail(@RequestParam Long userId, @PathVariable Long courseId) {
        logger.info("获取课程详情, userId={}, courseId={}", userId, courseId);
        CourseDetailDTO courseDetail = studentService.getCourseDetail(userId, courseId);
        if (courseDetail != null) {
            return RestBean.success("获取课程详情成功", courseDetail);
        } else {
            return RestBean.failure(404, "课程不存在或无权访问");
        }
    }
    
    /**
     * 获取课程资源列表
     *
     * @param courseId 课程ID
     * @return 资源列表
     */
    @GetMapping("/courses/{courseId}/resources")
    public RestBean<List<CourseResourceDTO>> getCourseResources(@PathVariable String courseId) {
        logger.info("获取课程资源列表, courseId={}", courseId);
        List<CourseResourceDTO> resources = studentService.getCourseResources(courseId);
        return RestBean.success("获取课程资源列表成功", resources);
    }
    
    /**
     * 获取课程讨论列表
     *
     * @param courseId 课程ID
     * @return 讨论列表
     */
    @GetMapping("/courses/{courseId}/discussions")
    public RestBean<List<DiscussionDTO>> getCourseDiscussions(@PathVariable Long courseId) {
        logger.info("获取课程讨论列表, courseId={}", courseId);
        List<DiscussionDTO> discussions = studentService.getCourseDiscussions(courseId);
        return RestBean.success("获取课程讨论列表成功", discussions);
    }
    
    /**
     * 获取讨论详情和回复列表
     *
     * @param discussionId 讨论ID
     * @return 讨论详情，包含回复列表
     */
    @GetMapping("/discussions/{discussionId}")
    public RestBean<DiscussionDTO> getDiscussionDetail(@PathVariable Long discussionId) {
        logger.info("获取讨论详情, discussionId={}", discussionId);
        DiscussionDTO discussion = studentService.getDiscussionDetail(discussionId);
        if (discussion != null) {
            return RestBean.success("获取讨论详情成功", discussion);
        } else {
            return RestBean.failure(404, "讨论不存在或无权访问");
        }
    }

    /**
     * 发布新讨论
     *
     * @param courseId 课程ID
     * @param requestData 请求数据，包含讨论标题和内容
     * @return 操作结果
     */
    @PostMapping("/courses/{courseId}/discussions")
    public RestBean<Object> createDiscussion(@PathVariable Long courseId, @RequestBody Map<String, Object> requestData) {
        logger.info("发布新讨论, courseId={}", courseId);
        
        try {
            Long userId = Long.valueOf(requestData.get("userId").toString());
            String title = (String) requestData.get("title");
            String content = (String) requestData.get("content");
            
            boolean success = studentService.createDiscussion(userId, courseId, title, content);
            
            if (success) {
                return RestBean.success("发布讨论成功", null);
            } else {
                return RestBean.failure(500, "发布讨论失败");
            }
        } catch (Exception e) {
            logger.error("发布讨论参数错误", e);
            return RestBean.failure(400, "参数格式错误: " + e.getMessage());
        }
    }
    
    /**
     * 回复讨论
     *
     * @param discussionId 讨论ID
     * @param requestData 请求数据，包含回复内容
     * @return 操作结果
     */
    @PostMapping("/discussions/{discussionId}/replies")
    public RestBean<Object> replyToDiscussion(@PathVariable Long discussionId, @RequestBody Map<String, Object> requestData) {
        logger.info("回复讨论, discussionId={}", discussionId);
        
        try {
            Long userId = Long.valueOf(requestData.get("userId").toString());
            String content = (String) requestData.get("content");
            Long parentId = requestData.get("parentId") != null ? Long.valueOf(requestData.get("parentId").toString()) : null;
            
            boolean success = studentService.replyToDiscussion(userId, discussionId, content, parentId);
            
            if (success) {
                return RestBean.success("回复成功", null);
            } else {
                return RestBean.failure(500, "回复失败");
            }
        } catch (Exception e) {
            logger.error("回复讨论参数错误", e);
            return RestBean.failure(400, "参数格式错误: " + e.getMessage());
        }
    }
    
    /**
     * 点赞讨论
     *
     * @param discussionId 讨论ID
     * @param requestData 请求数据，包含用户ID
     * @return 操作结果
     */
    @PostMapping("/discussions/{discussionId}/like")
    public RestBean<Object> likeDiscussion(@PathVariable Long discussionId, @RequestBody Map<String, Object> requestData) {
        logger.info("点赞讨论, discussionId={}", discussionId);
        
        try {
            Long userId = Long.valueOf(requestData.get("userId").toString());
            
            boolean success = studentService.likeDiscussion(userId, discussionId);
            
            if (success) {
                return RestBean.success("点赞成功", null);
            } else {
                return RestBean.failure(500, "点赞失败");
            }
        } catch (Exception e) {
            logger.error("点赞讨论参数错误", e);
            return RestBean.failure(400, "参数格式错误: " + e.getMessage());
        }
    }
    
    /**
     * 获取课程测验列表
     *
     * @param userId 学生ID
     * @param courseId 课程ID
     * @return 测验列表
     */
    @GetMapping("/courses/{courseId}/quizzes")
    public RestBean<List<QuizDTO>> getCourseQuizzes(@RequestParam Long userId, @PathVariable Long courseId) {
        logger.info("获取课程测验列表, userId={}, courseId={}", userId, courseId);
        List<QuizDTO> quizzes = studentService.getCourseQuizzes(userId, courseId);
        return RestBean.success("获取课程测验列表成功", quizzes);
    }
    
    /**
     * 获取测验详情
     *
     * @param userId 学生ID
     * @param quizId 测验ID
     * @return 测验详情，包含问题列表
     */
    @GetMapping("/quizzes/{quizId}")
    public RestBean<QuizDTO> getQuizDetail(@RequestParam Long userId, @PathVariable Long quizId) {
        logger.info("获取测验详情, userId={}, quizId={}", userId, quizId);
        QuizDTO quiz = studentService.getQuizDetail(userId, quizId);
        if (quiz != null) {
            return RestBean.success("获取测验详情成功", quiz);
        } else {
            return RestBean.failure(404, "测验不存在或无权访问");
        }
    }
    
    /**
     * 更新学生课程学习进度
     *
     * @param courseId 课程ID
     * @param requestData 请求数据，包含学生ID、章节ID、当前位置和完成百分比
     * @return 操作结果
     */
    @PostMapping("/courses/{courseId}/progress")
    public RestBean<Object> updateCourseProgress(
            @PathVariable Long courseId, 
            @RequestBody Map<String, Object> requestData) {
        
        try {
            Long studentId = Long.valueOf(requestData.get("studentId").toString());
            Long chapterId = Long.valueOf(requestData.get("chapterId").toString());
            String lastPosition = (String) requestData.get("lastPosition");
            BigDecimal completionPercentage = new BigDecimal(requestData.get("completionPercentage").toString());
            
            logger.info("更新学生课程学习进度, studentId={}, courseId={}, chapterId={}, progress={}", 
                    studentId, courseId, chapterId, completionPercentage);
            
            boolean success = studentService.updateCourseProgress(
                    studentId, courseId, chapterId, lastPosition, completionPercentage);
            
            if (success) {
                return RestBean.success("更新学习进度成功", null);
            } else {
                return RestBean.failure(500, "更新学习进度失败");
            }
        } catch (Exception e) {
            logger.error("更新学习进度参数错误", e);
            return RestBean.failure(400, "参数格式错误: " + e.getMessage());
        }
    }
    
    /**
     * 添加学习记录
     *
     * @param requestData 请求数据，包含学习记录相关信息
     * @return 操作结果
     */
    @PostMapping("/learning-records")
    public RestBean<Object> addLearningRecord(@RequestBody Map<String, Object> requestData) {
        try {
            // 处理userId可能是Integer或String类型的情况
            String userId;
            Object userIdObj = requestData.get("userId");
            if (userIdObj instanceof Integer) {
                userId = String.valueOf(userIdObj);
            } else {
                userId = (String) userIdObj;
            }
            
            // 处理courseId可能是Integer或String类型的情况
            String courseId;
            Object courseIdObj = requestData.get("courseId");
            if (courseIdObj instanceof Integer) {
                courseId = String.valueOf(courseIdObj);
            } else {
                courseId = (String) courseIdObj;
            }
            
            String contentType = (String) requestData.get("contentType");
            
            // 处理contentId可能是Integer或String类型的情况
            String contentId;
            Object contentIdObj = requestData.get("contentId");
            if (contentIdObj instanceof Integer) {
                contentId = String.valueOf(contentIdObj);
            } else {
                contentId = (String) contentIdObj;
            }
            
            // 解析时间字符串
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            LocalDateTime startTime = LocalDateTime.parse((String) requestData.get("startTime"), formatter);
            LocalDateTime endTime = LocalDateTime.parse((String) requestData.get("endTime"), formatter);
            
            Integer duration = Integer.valueOf(requestData.get("duration").toString());
            BigDecimal progress = new BigDecimal(requestData.get("progress").toString());
            
            logger.info("添加学习记录, userId={}, courseId={}, contentType={}", userId, courseId, contentType);
            
            boolean success = studentService.addLearningRecord(
                    userId, courseId, contentType, contentId, startTime, endTime, duration, progress);
            
            if (success) {
                return RestBean.success("添加学习记录成功", null);
            } else {
                return RestBean.failure(500, "添加学习记录失败");
            }
        } catch (DateTimeParseException e) {
            logger.error("解析时间格式错误", e);
            return RestBean.failure(400, "时间格式错误，请使用ISO标准格式: " + e.getMessage());
        } catch (Exception e) {
            logger.error("添加学习记录参数错误", e);
            return RestBean.failure(400, "参数格式错误: " + e.getMessage());
        }
    }
    
    /**
     * 获取课程章节列表
     *
     * @param courseId 课程ID
     * @return 章节列表
     */
    @GetMapping("/courses/{courseId}/chapters")
    public RestBean<List<Map<String, Object>>> getCourseChapters(@PathVariable Long courseId) {
        logger.info("获取课程章节列表, courseId={}", courseId);
        List<Map<String, Object>> chapters = studentService.getCourseChapters(courseId);
        return RestBean.success("获取课程章节列表成功", chapters);
    }
    
    /**
     * 获取章节内容
     *
     * @param courseId 课程ID
     * @param chapterId 章节ID
     * @return 章节内容
     */
    @GetMapping("/courses/{courseId}/chapters/{chapterId}")
    public RestBean<Map<String, Object>> getChapterContent(
            @PathVariable Long courseId, 
            @PathVariable Long chapterId,
            @RequestParam(required = false) Long userId) {
        
        logger.info("获取章节内容, courseId={}, chapterId={}, userId={}", courseId, chapterId, userId);
        
        if (courseId == null || chapterId == null) {
            logger.warn("获取章节内容失败: 缺少必要参数");
            return RestBean.failure(400, "缺少必要参数");
        }
        
        try {
            Map<String, Object> chapterContent = studentService.getChapterContent(courseId, chapterId, userId);
        
        if (chapterContent != null) {
            // 如果提供了用户ID，记录访问
            if (userId != null) {
                    boolean recorded = studentService.recordChapterAccess(userId, courseId, chapterId);
                    if (!recorded) {
                        logger.warn("记录章节访问失败: userId={}, courseId={}, chapterId={}", userId, courseId, chapterId);
                    }
                }
                
                if (chapterContent.get("contents") != null) {
                    List<?> contents = (List<?>) chapterContent.get("contents");
                    logger.info("获取章节内容成功, 内容数量: {}", contents.size());
                } else {
                    logger.warn("获取章节内容成功，但内容列表为空");
            }
            
            return RestBean.success("获取章节内容成功", chapterContent);
        } else {
                logger.warn("未找到章节内容: courseId={}, chapterId={}", courseId, chapterId);
            return RestBean.failure(404, "章节不存在或无权访问");
            }
        } catch (Exception e) {
            logger.error("获取章节内容异常: courseId={}, chapterId={}", courseId, chapterId, e);
            return RestBean.failure(500, "获取章节内容失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生的学习笔记列表
     *
     * @param studentId 学生ID
     * @param courseId 课程ID (可选)
     * @param chapterId 章节ID (可选)
     * @return 笔记列表
     */
    @GetMapping("/learning-notes")
    public RestBean<List<LearningNoteDTO>> getLearningNotes(
            @RequestParam Long studentId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long chapterId) {
        
        logger.info("获取学习笔记列表, userId={}, courseId={}, chapterId={}", studentId, courseId, chapterId);
        List<LearningNoteDTO> notes = studentService.getLearningNotes(studentId, courseId, chapterId);
        return RestBean.success("获取学习笔记列表成功", notes);
    }

    /**
     * 添加学习笔记
     *
     * @param note 笔记对象
     * @return 操作结果
     */
    @PostMapping("/learning-notes/add")
    public RestBean<Object> addLearningNote(@RequestBody LearningNote note) {
        logger.info("添加学习笔记, userId={}, courseId={}, chapterId={}", note.getStudentId(), note.getCourseId(), note.getChapterId());
        
        if (note.getStudentId() == null || note.getCourseId() == null || note.getNoteContent() == null || note.getNoteContent().trim().isEmpty()) {
            return RestBean.failure(400, "缺少必要参数");
        }
        
        boolean success = studentService.addLearningNote(note);
        if (success) {
            return RestBean.success("添加学习笔记成功", null);
        } else {
            return RestBean.failure(500, "添加学习笔记失败");
        }
    }


    @PostMapping(value = "/chat",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> Chat(@RequestParam String message, @RequestParam String uid){
        try {
            int id = Integer.parseInt(uid);
            return teacherService.Chat(message,id);
        } catch (Exception e) {
            e.printStackTrace();
            return Flux.just("发生错误: " + e.getMessage());
        }
    }

    /**
     * 更新学习笔记
     *
     * @param noteId 笔记ID
     * @param note 笔记对象
     * @return 操作结果
     */
    @PostMapping("/learning-notes/update/{noteId}")
    public RestBean<Object> updateLearningNote(@PathVariable Long noteId, @RequestBody LearningNote note) {
        logger.info("更新学习笔记, noteId={}, userId={}", noteId, note.getStudentId());
        
        if (note.getStudentId() == null || note.getNoteContent() == null || note.getNoteContent().trim().isEmpty()) {
            return RestBean.failure(400, "缺少必要参数");
        }
        
        note.setId(noteId);
        boolean success = studentService.updateLearningNote(note);
        if (success) {
            return RestBean.success("更新学习笔记成功", null);
        } else {
            return RestBean.failure(500, "更新学习笔记失败");
        }
    }

    /**
     * 删除学习笔记
     *
     * @param noteId 笔记ID
     * @param studentId 学生ID
     * @return 操作结果
     */
    @PostMapping("/learning-notes/delete/{noteId}")
    public RestBean<Object> deleteLearningNote(@PathVariable Long noteId, @RequestParam Long studentId) {
        logger.info("删除学习笔记, noteId={}, studentId={}", noteId, studentId);
        
        boolean success = studentService.deleteLearningNote(noteId, studentId);
        if (success) {
            return RestBean.success("删除学习笔记成功", null);
        } else {
            return RestBean.failure(500, "删除学习笔记失败");
        }
    }

    /**
     * 获取章节评价
     *
     * @param studentId 学生ID
     * @param chapterId 章节ID
     * @return 评价对象
     */
    @GetMapping("/chapter-ratings/user")
    public RestBean<ChapterRating> getChapterRating(@RequestParam Long studentId, @RequestParam Long chapterId) {
        logger.info("获取章节评价, userId={}, chapterId={}", studentId, chapterId);
        
        ChapterRating rating = studentService.getChapterRating(studentId, chapterId);
        if (rating != null) {
            return RestBean.success("获取章节评价成功", rating);
        } else {
            return RestBean.success("未找到章节评价", null);
        }
    }

    /**
     * 保存章节评价
     *
     * @param rating 评价对象
     * @return 操作结果
     */
    @PostMapping("/chapter-ratings")
    public RestBean<Object> saveChapterRating(@RequestBody ChapterRating rating) {
        logger.info("保存章节评价, userId={}, chapterId={}, rating={}", rating.getStudentId(), rating.getChapterId(), rating.getRating());
        
        if (rating.getStudentId() == null || rating.getCourseId() == null || rating.getChapterId() == null || rating.getRating() == null) {
            return RestBean.failure(400, "缺少必要参数");
        }
        
        if (rating.getRating() < 1 || rating.getRating() > 5) {
            return RestBean.failure(400, "评分必须在1-5之间");
        }
        
        boolean success = studentService.saveChapterRating(rating);
        if (success) {
            return RestBean.success("保存章节评价成功", null);
        } else {
            return RestBean.failure(500, "保存章节评价失败");
        }
    }

    /**
     * 获取章节评价列表
     *
     * @param chapterId 章节ID
     * @return 评价列表
     */
    @GetMapping("/chapter-ratings/{chapterId}")
    public RestBean<List<ChapterRatingDTO>> getChapterRatings(@PathVariable Long chapterId) {
        logger.info("获取章节评价列表, chapterId={}", chapterId);
        
        List<ChapterRatingDTO> ratings = studentService.getChapterRatings(chapterId);
        return RestBean.success("获取章节评价列表成功", ratings);
    }

    /**
     * 获取章节平均评分
     *
     * @param chapterId 章节ID
     * @return 平均评分
     */
    @GetMapping("/chapter-ratings/{chapterId}/average")
    public RestBean<Integer> getChapterAverageRating(@PathVariable Long chapterId) {
        logger.info("获取章节平均评分, chapterId={}", chapterId);
        
        Integer averageRating = studentService.getChapterAverageRating(chapterId);
        return RestBean.success("获取章节平均评分成功", averageRating);
    }

    /**
     * 获取课程学习统计
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 学习统计数据
     */
    @GetMapping("/courses/{courseId}/statistics")
    public RestBean<LearningStatisticsDTO> getLearningStatistics(@RequestParam Long studentId, @PathVariable Long courseId) {
        logger.info("获取课程学习统计, studentId={}, courseId={}", studentId, courseId);
        
        LearningStatisticsDTO statistics = studentService.getLearningStatistics(studentId, courseId);
        return RestBean.success("获取课程学习统计成功", statistics);
    }
} 