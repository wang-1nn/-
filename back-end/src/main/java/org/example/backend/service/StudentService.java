package org.example.backend.service;

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
import org.example.backend.entity.vo.DashboardDataVO.StatItemVO;
import org.example.backend.entity.vo.DashboardDataVO.CourseProgressVO;
import org.example.backend.entity.vo.DashboardDataVO.RecommendationVO;
import org.example.backend.entity.vo.DashboardWidgetsVO.ScoreOverviewVO;
import org.example.backend.entity.vo.DashboardWidgetsVO.StudyTimeVO;
import org.example.backend.entity.vo.DashboardWidgetsVO.TodoTasksVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface StudentService {
    /**
     * 获取学生仪表盘数据
     *
     * @param userId 学生ID
     * @return 仪表盘数据
     */
    DashboardDataVO getDashboardData(String userId);
    
    /**
     * 获取学习统计数据
     *
     * @param userId 学生ID
     * @return 学习统计数据
     */
    List<StatItemVO> getLearningStats(String userId);
    
    /**
     * 获取课程学习进度
     *
     * @param userId 学生ID
     * @return 课程进度列表
     */
    List<CourseProgressVO> getCourseProgress(String userId);
    
    /**
     * 获取学习建议
     *
     * @param userId 学生ID
     * @return 学习建议列表
     */
    List<RecommendationVO> getLearningRecommendations(String userId);

    /**
     * 获取成绩总览数据
     *
     * @param userId 学生ID
     * @return 成绩总览数据
     */
    ScoreOverviewVO getScoreOverview(String userId);

    /**
     * 获取学习时长分析数据
     *
     * @param userId 学生ID
     * @return 学习时长分析数据
     */
    StudyTimeVO getStudyTime(String userId);

    /**
     * 获取待办任务列表
     *
     * @param userId 学生ID
     * @return 待办任务数据
     */
    TodoTasksVO getTodoTasks(String userId);

    /**
     * 添加待办任务
     *
     * @param task 待办任务对象
     * @return 添加后的任务ID，失败返回null
     */
    Long addTodoTask(TodoTask task);

    /**
     * 更新待办任务状态
     *
     * @param taskId 任务ID
     * @param completed 是否完成
     * @return 是否更新成功
     */
    boolean updateTodoTaskStatus(Long taskId, boolean completed);
    
    /**
     * 获取仪表盘小部件数据
     *
     * @param userId 学生ID
     * @param widgetType 小部件类型
     * @return 小部件数据
     */
    Object getDashboardWidget(String userId, String widgetType);
    
    /**
     * 更新待办任务状态
     *
     * @param userId 学生ID
     * @param taskId 任务ID
     * @param status 任务状态
     * @return 是否更新成功
     */
    boolean updateTaskStatus(Long userId, Long taskId, Integer status);
    
    /**
     * 记录学习时长
     *
     * @param userId 学生ID
     * @param courseId 课程ID
     * @param startTime 开始时间
     * @param duration 学习时长（分钟）
     * @param progress 学习进度（0-100）
     * @return 是否记录成功
     */
    boolean recordStudyTime(Long userId, Long courseId, LocalDateTime startTime, Integer duration, BigDecimal progress);
    
    /**
     * 获取学生课程列表
     *
     * @param userId 学生ID
     * @return 课程列表
     */
    List<CourseDTO> getStudentCourses(Long userId);
    
    /**
     * 获取课程详情
     *
     * @param userId 学生ID
     * @param courseId 课程ID
     * @return 课程详情
     */
    CourseDetailDTO getCourseDetail(Long userId, Long courseId);
    
    /**
     * 获取课程资源列表
     *
     * @param courseId 课程ID
     * @return 资源列表
     */
    List<CourseResourceDTO> getCourseResources(String courseId);
    
    /**
     * 获取课程讨论列表
     *
     * @param courseId 课程ID
     * @return 讨论列表
     */
    List<DiscussionDTO> getCourseDiscussions(Long courseId);
    
    /**
     * 获取讨论详情和回复列表
     *
     * @param discussionId 讨论ID
     * @return 讨论详情，包含回复列表
     */
    DiscussionDTO getDiscussionDetail(Long discussionId);
    
    /**
     * 获取课程测验列表
     *
     * @param userId 学生ID
     * @param courseId 课程ID
     * @return 测验列表
     */
    List<QuizDTO> getCourseQuizzes(Long userId, Long courseId);
    
    /**
     * 获取测验详情
     *
     * @param userId 学生ID
     * @param quizId 测验ID
     * @return 测验详情，包含问题列表
     */
    QuizDTO getQuizDetail(Long userId, Long quizId);
    
    /**
     * 获取测验问题列表
     *
     * @param quizId 测验ID
     * @return 问题列表
     */
    List<QuizDTO.QuizQuestionDTO> getQuizQuestions(Long quizId);
    
    /**
     * 更新学生课程学习进度
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param chapterId 章节ID
     * @param lastPosition 当前学习位置
     * @param completionPercentage 完成百分比
     * @return 是否更新成功
     */
    boolean updateCourseProgress(Long studentId, Long courseId, Long chapterId, String lastPosition, BigDecimal completionPercentage);
    
    /**
     * 添加学习记录
     *
     * @param userId 学生ID
     * @param courseId 课程ID
     * @param contentType 内容类型
     * @param contentId 内容ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param duration 时长（秒）
     * @param progress 进度（百分比）
     * @return 是否添加成功
     */
    boolean addLearningRecord(String userId, String courseId, String contentType, String contentId, 
                             LocalDateTime startTime, LocalDateTime endTime, Integer duration, BigDecimal progress);

    /**
     * 创建新讨论
     *
     * @param userId 用户ID
     * @param courseId 课程ID
     * @param title 讨论标题
     * @param content 讨论内容
     * @return 是否创建成功
     */
    boolean createDiscussion(Long userId, Long courseId, String title, String content);

    /**
     * 回复讨论
     *
     * @param userId 用户ID
     * @param discussionId 讨论ID
     * @param content 回复内容
     * @param parentId 父回复ID（可为null）
     * @return 是否回复成功
     */
    boolean replyToDiscussion(Long userId, Long discussionId, String content, Long parentId);

    /**
     * 点赞讨论
     *
     * @param userId 用户ID
     * @param discussionId 讨论ID
     * @return 是否点赞成功
     */
    boolean likeDiscussion(Long userId, Long discussionId);
    
    /**
     * 切换讨论点赞状态
     *
     * @param userId 用户ID
     * @param discussionId 讨论ID
     * @return 当前点赞状态
     */
    boolean toggleDiscussionLike(Long userId, Long discussionId);
    
    /**
     * 检查是否已点赞讨论
     *
     * @param userId 用户ID
     * @param discussionId 讨论ID
     * @return 是否已点赞
     */
    boolean checkDiscussionLike(Long userId, Long discussionId);
    
    /**
     * 获取课程章节列表
     *
     * @param courseId 课程ID
     * @return 章节列表
     */
    List<Map<String, Object>> getCourseChapters(Long courseId);
    
    /**
     * 获取章节内容
     *
     * @param courseId 课程ID
     * @param chapterId 章节ID
     * @param studentId 学生ID (可为null)
     * @return 章节内容，包含章节信息和内容列表
     */
    Map<String, Object> getChapterContent(Long courseId, Long chapterId, Long studentId);
    
    /**
     * 记录章节访问
     *
     * @param userId 用户ID
     * @param courseId 课程ID
     * @param chapterId 章节ID
     * @return 是否记录成功
     */
    boolean recordChapterAccess(Long userId, Long courseId, Long chapterId);

    /**
     * 获取学生的学习笔记列表
     *
     * @param studentId 学生ID
     * @param courseId 课程ID (可选)
     * @param chapterId 章节ID (可选)
     * @return 笔记列表
     */
    List<LearningNoteDTO> getLearningNotes(Long studentId, Long courseId, Long chapterId);

    /**
     * 添加学习笔记
     *
     * @param note 笔记对象
     * @return 是否添加成功
     */
    boolean addLearningNote(LearningNote note);

    /**
     * 更新学习笔记
     *
     * @param note 笔记对象
     * @return 是否更新成功
     */
    boolean updateLearningNote(LearningNote note);

    /**
     * 删除学习笔记
     *
     * @param noteId 笔记ID
     * @param studentId 学生ID
     * @return 是否删除成功
     */
    boolean deleteLearningNote(Long noteId, Long studentId);

    /**
     * 获取章节评价
     *
     * @param studentId 学生ID
     * @param chapterId 章节ID
     * @return 评价对象
     */
    ChapterRating getChapterRating(Long studentId, Long chapterId);

    /**
     * 保存章节评价
     *
     * @param rating 评价对象
     * @return 是否保存成功
     */
    boolean saveChapterRating(ChapterRating rating);

    /**
     * 获取章节评价列表
     *
     * @param chapterId 章节ID
     * @return 评价列表
     */
    List<ChapterRatingDTO> getChapterRatings(Long chapterId);

    /**
     * 获取章节平均评分
     *
     * @param chapterId 章节ID
     * @return 平均评分
     */
    Integer getChapterAverageRating(Long chapterId);

    /**
     * 获取课程学习统计
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 学习统计数据
     */
    LearningStatisticsDTO getLearningStatistics(Long studentId, Long courseId);
} 