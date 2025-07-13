package org.example.backend.service.impl;

import jakarta.annotation.Resource;
import org.example.backend.entity.dto.*;
import org.example.backend.entity.pojo.*;
import org.example.backend.entity.vo.DashboardDataVO;
import org.example.backend.entity.vo.DashboardDataVO.CourseProgressVO;
import org.example.backend.entity.vo.DashboardDataVO.RecommendationVO;
import org.example.backend.entity.vo.DashboardDataVO.StatItemVO;
import org.example.backend.entity.vo.DashboardWidgetsVO.*;
import org.example.backend.mapper.StudentMapper;
import org.example.backend.mapper.UserMapper;
import org.example.backend.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生服务实现类
 */
@Service
public class StudentServiceImpl implements StudentService {
    
    private static final Logger logger = LoggerFactory.getLogger(StudentServiceImpl.class);
    
    @Resource
    private StudentMapper studentMapper;
    
    @Resource
    private UserMapper userMapper;
    
    @Override
    public DashboardDataVO getDashboardData(String userId) {
        try {
            logger.info("获取学生仪表盘数据, userId={}", userId);
            DashboardDataVO dashboardData = new DashboardDataVO();
            
            // 获取学生信息
            User student = userMapper.findUserById(userId);
            if (student != null) {
                dashboardData.setUsername(student.getRealName());
                logger.info("学生信息获取成功: {}", student.getRealName());
            } else {
                dashboardData.setUsername("同学");
                logger.warn("未找到学生信息, userId={}", userId);
            }
            
            // 填充统计数据
            dashboardData.setStats(getLearningStats(userId));
            
            // 填充课程进度
            dashboardData.setCourses(getCourseProgress(userId));
            
            // 填充学习建议
            dashboardData.setRecommendations(getLearningRecommendations(userId));
            
            return dashboardData;
        } catch (Exception e) {
            logger.error("获取仪表盘数据异常", e);
            return new DashboardDataVO(); // 返回空对象而不是null，避免前端报错
        }
    }
    
    @Override
    public List<StatItemVO> getLearningStats(String userId) {
        List<StatItemVO> stats = new ArrayList<>();
        
        try {
            logger.info("获取学习统计数据, userId={}", userId);
            
            // 已学课程数量
            Integer courseCount = studentMapper.countEnrolledCourses(userId);
            logger.info("已学课程数量: {}", courseCount);
            StatItemVO coursesStat = new StatItemVO();
            coursesStat.setId(1);
            coursesStat.setTitle("已学课程");
            coursesStat.setValue(courseCount != null ? courseCount.toString() : "0");
            coursesStat.setColor("bg-blue-100 text-blue-600");
            stats.add(coursesStat);
            
            // 学习总时长（小时）
            Float totalHours = studentMapper.calculateLearningHours(userId);
            logger.info("学习总时长: {}", totalHours);
            StatItemVO hoursStat = new StatItemVO();
            hoursStat.setId(2);
            hoursStat.setTitle("学习时长");
            hoursStat.setValue((totalHours != null ? String.format("%.1f", totalHours) : "0") + "h");
            hoursStat.setColor("bg-indigo-100 text-indigo-600");
            stats.add(hoursStat);
            
            // 连续学习天数
            Integer streakDays = studentMapper.calculateStreakDays(userId);
            logger.info("连续学习天数: {}", streakDays);
            StatItemVO streakStat = new StatItemVO();
            streakStat.setId(3);
            streakStat.setTitle("连续学习");
            streakStat.setValue((streakDays != null ? streakDays : 0) + "天");
            streakStat.setColor("bg-amber-100 text-amber-600");
            stats.add(streakStat);
            
            // 平均成绩
            Integer avgScore = studentMapper.calculateAverageScore(userId);
            logger.info("平均成绩: {}", avgScore);
            StatItemVO scoreStat = new StatItemVO();
            scoreStat.setId(4);
            scoreStat.setTitle("平均成绩");
            scoreStat.setValue(avgScore != null ? avgScore.toString() : "0");
            scoreStat.setColor("bg-green-100 text-green-600");
            stats.add(scoreStat);
            
        } catch (Exception e) {
            logger.error("获取学习统计数据异常", e);
            // 如果发生错误，返回空列表
        }
        
        return stats;
    }
    
    @Override
    public List<CourseProgressVO> getCourseProgress(String userId) {
        try {
            return studentMapper.findCourseProgress(userId);
        } catch (Exception e) {
            logger.error("获取课程进度异常", e);
            // 返回空列表
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<RecommendationVO> getLearningRecommendations(String userId) {
        try {
            return studentMapper.findLearningRecommendations(userId);
        } catch (Exception e) {
            logger.error("获取学习建议异常", e);
            // 返回空列表
            return new ArrayList<>();
        }
    }

    @Override
    public ScoreOverviewVO getScoreOverview(String userId) {
        try {
            logger.info("获取成绩总览数据, userId={}", userId);
            ScoreOverviewVO scoreOverview = new ScoreOverviewVO();

            // 获取最近一次考试成绩
            Integer latestScore = studentMapper.findLatestExamScore(userId);
            scoreOverview.setScore(latestScore != null ? latestScore : 0);
            logger.info("最近一次考试成绩: {}", scoreOverview.getScore());

            // 获取成绩变化趋势
            Integer scoreTrend = studentMapper.findScoreTrend(userId);
            scoreOverview.setScoreTrend(scoreTrend != null ? scoreTrend : 0);
            logger.info("成绩变化趋势: {}", scoreOverview.getScoreTrend());

            // 获取近期考试成绩历史
            List<ExamScoreVO> scoreHistory = studentMapper.findRecentExamScores(userId);
            scoreOverview.setScoreHistory(scoreHistory);
            logger.info("获取近期考试成绩历史: {} 条记录", scoreHistory != null ? scoreHistory.size() : 0);

            return scoreOverview;
        } catch (Exception e) {
            logger.error("获取成绩总览数据异常", e);
            // 返回空对象
            ScoreOverviewVO emptyResult = new ScoreOverviewVO();
            emptyResult.setScore(0);
            emptyResult.setScoreTrend(0);
            emptyResult.setScoreHistory(new ArrayList<>());
            return emptyResult;
        }
    }

    @Override
    public StudyTimeVO getStudyTime(String userId) {
        try {
            logger.info("获取学习时长分析数据, userId={}", userId);
            StudyTimeVO studyTime = new StudyTimeVO();

            // 获取本周学习时长
            Float weeklyHours = studentMapper.calculateWeeklyLearningHours(userId);
            studyTime.setWeeklyHours(weeklyHours != null ? weeklyHours : 0f);
            logger.info("本周学习时长: {}", studyTime.getWeeklyHours());

            // 获取上周学习时长，计算变化趋势
            Float lastWeekHours = studentMapper.calculateLastWeekLearningHours(userId);
            float trend = (weeklyHours != null && lastWeekHours != null) ? weeklyHours - lastWeekHours : 0f;
            studyTime.setWeeklyTrend(trend);
            logger.info("较上周变化: {}", studyTime.getWeeklyTrend());

            // 获取每日学习时长数据
            List<DailyStudyVO> dailyStudy = studentMapper.findWeeklyStudyTime(userId);
            
            // 计算每天的学习时长百分比（用于柱状图高度）
            if (dailyStudy != null && !dailyStudy.isEmpty()) {
                // 找出最大的学习时长，用于计算百分比
                float maxHours = dailyStudy.stream()
                        .map(DailyStudyVO::getHours)
                        .max(Float::compare)
                        .orElse(1.0f);
                
                // 设置每天的百分比高度（最高为100%）
                dailyStudy.forEach(day -> {
                    int percentage = (int) (day.getHours() / maxHours * 100);
                    day.setPercentage(percentage);
                });
                
                // 确保周一到周日的顺序是正确的，如果缺失某天则补充
                Map<String, DailyStudyVO> dailyMap = dailyStudy.stream()
                        .collect(Collectors.toMap(DailyStudyVO::getDay, d -> d));
                
                List<DailyStudyVO> orderedDailyStudy = new ArrayList<>();
                String[] weekdays = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
                
                for (String day : weekdays) {
                    if (dailyMap.containsKey(day)) {
                        orderedDailyStudy.add(dailyMap.get(day));
                    } else {
                        DailyStudyVO emptyDay = new DailyStudyVO();
                        emptyDay.setDay(day);
                        emptyDay.setHours(0.0f);
                        emptyDay.setPercentage(0);
                        orderedDailyStudy.add(emptyDay);
                    }
                }
                
                studyTime.setDailyStudy(orderedDailyStudy);
            } else {
                // 如果没有数据，创建默认的空数据
                List<DailyStudyVO> emptyDailyStudy = new ArrayList<>();
                String[] weekdays = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
                
                for (String day : weekdays) {
                    DailyStudyVO emptyDay = new DailyStudyVO();
                    emptyDay.setDay(day);
                    emptyDay.setHours(0.0f);
                    emptyDay.setPercentage(0);
                    emptyDailyStudy.add(emptyDay);
                }
                
                studyTime.setDailyStudy(emptyDailyStudy);
            }

            return studyTime;
        } catch (Exception e) {
            logger.error("获取学习时长分析数据异常", e);
            // 返回空对象
            StudyTimeVO emptyResult = new StudyTimeVO();
            emptyResult.setWeeklyHours(0.0f);
            emptyResult.setWeeklyTrend(0.0f);
            
            // 创建默认的空数据
            List<DailyStudyVO> emptyDailyStudy = new ArrayList<>();
            String[] weekdays = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
            
            for (String day : weekdays) {
                DailyStudyVO emptyDay = new DailyStudyVO();
                emptyDay.setDay(day);
                emptyDay.setHours(0.0f);
                emptyDay.setPercentage(0);
                emptyDailyStudy.add(emptyDay);
            }
            
            emptyResult.setDailyStudy(emptyDailyStudy);
            return emptyResult;
        }
    }

    @Override
    public TodoTasksVO getTodoTasks(String userId) {
        try {
            logger.info("获取待办任务列表, userId={}", userId);
            TodoTasksVO todoTasks = new TodoTasksVO();

            // 获取待办任务列表
            List<TaskItemVO> tasks = studentMapper.findTodoTasks(userId);
            todoTasks.setTasks(tasks);
            logger.info("获取待办任务列表: {} 条记录", tasks != null ? tasks.size() : 0);

            return todoTasks;
        } catch (Exception e) {
            logger.error("获取待办任务列表异常", e);
            // 返回空对象
            TodoTasksVO emptyResult = new TodoTasksVO();
            emptyResult.setTasks(new ArrayList<>());
            return emptyResult;
        }
    }
    
    @Override
    public Long addTodoTask(TodoTask task) {
        try {
            logger.info("添加待办任务, userId={}, title={}", task.getUserId(), task.getTitle());
            
            // 设置初始状态为待办
            task.setStatus("PENDING");
            
            // 如果没有设置创建时间，则使用当前时间
            if (task.getCreatedAt() == null) {
                task.setCreatedAt(LocalDateTime.now());
            }
            
            int result = studentMapper.addTodoTask(task);
            if (result > 0) {
                logger.info("待办任务添加成功, id={}", task.getId());
                return task.getId();
            } else {
                logger.warn("待办任务添加失败");
                return null;
            }
        } catch (Exception e) {
            logger.error("添加待办任务异常", e);
            return null;
        }
    }
    
    @Override
    public boolean updateTodoTaskStatus(Long taskId, boolean completed) {
        try {
            logger.info("更新待办任务状态, taskId={}, completed={}", taskId, completed);
            
            // 先查询任务是否存在
            TodoTask task = studentMapper.findTodoTaskById(taskId);
            if (task == null) {
                logger.warn("待办任务不存在, taskId={}", taskId);
                return false;
            }
            
            // 更新任务状态
            String status = completed ? "COMPLETED" : "PENDING";
            int result = studentMapper.updateTodoTaskStatus(taskId, status);
            
            if (result > 0) {
                logger.info("待办任务状态更新成功, taskId={}, status={}", taskId, status);
                return true;
            } else {
                logger.warn("待办任务状态更新失败, taskId={}", taskId);
                return false;
            }
        } catch (Exception e) {
            logger.error("更新待办任务状态异常", e);
            return false;
        }
    }
    
    /**
     * 获取学生课程列表
     *
     * @param userId 学生ID
     * @return 课程列表
     */
    @Override
    public List<CourseDTO> getStudentCourses(Long userId) {
        try {
            logger.info("获取学生课程列表，学生ID: {}", userId);
            return studentMapper.findStudentCourses(userId);
        } catch (Exception e) {
            logger.error("获取学生课程列表失败，学生ID: {}", userId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取课程详情
     *
     * @param userId 学生ID
     * @param courseId 课程ID
     * @return 课程详情
     */
    @Override
    public CourseDetailDTO getCourseDetail(Long userId, Long courseId) {
        try {
            logger.info("获取课程详情，学生ID: {}, 课程ID: {}", userId, courseId);
            CourseDetailDTO courseDetail = studentMapper.findCourseDetail(userId, courseId);
            
            if (courseDetail != null) {
                // 获取课程章节列表
                List<Map<String, Object>> chapters = studentMapper.findCourseChapters(courseId, null);
                courseDetail.setChapters(chapters);
                
                // 获取课程公告列表（从course_discussions表中获取公告数据）
                List<CourseDetailDTO.AnnouncementDTO> announcements = new ArrayList<>();
                // 从数据库中获取公告数据
                getCourseAnnouncements(courseId, announcements);
                
                courseDetail.setAnnouncements(announcements);
            }
            
            return courseDetail;
        } catch (Exception e) {
            logger.error("获取课程详情失败，学生ID: {}, 课程ID: {}", userId, courseId, e);
            return null;
        }
    }
    
    /**
     * 获取课程公告列表
     * 从course_discussions表中获取教师发布的置顶讨论或带有公告标签的讨论作为公告
     * 
     * @param courseId 课程ID
     * @param announcements 公告列表（传出参数）
     */
    private void getCourseAnnouncements(Long courseId, List<CourseDetailDTO.AnnouncementDTO> announcements) {
        try {
            logger.info("获取课程公告，课程ID: {}", courseId);
            List<CourseDetailDTO.AnnouncementDTO> dbAnnouncements = studentMapper.findCourseAnnouncements(courseId);
            if (dbAnnouncements != null && !dbAnnouncements.isEmpty()) {
                announcements.addAll(dbAnnouncements);
                logger.info("从数据库获取到 {} 条课程公告", dbAnnouncements.size());
            } else {
                logger.info("未找到课程公告，课程ID: {}", courseId);
            }
        } catch (Exception e) {
            logger.error("获取课程公告失败，课程ID: {}", courseId, e);
            // 如果查询失败，添加一条默认公告
            CourseDetailDTO.AnnouncementDTO defaultAnnouncement = new CourseDetailDTO.AnnouncementDTO();
            defaultAnnouncement.setId(0L);
            defaultAnnouncement.setTitle("欢迎加入课程");
            defaultAnnouncement.setContent("欢迎加入本课程学习，请按计划完成课程内容。");
            defaultAnnouncement.setPublishTime(LocalDateTime.now());
            defaultAnnouncement.setPublisherName("系统");
            announcements.add(defaultAnnouncement);
        }
    }
    
    /**
     * 获取课程资源列表
     *
     * @param courseId 课程ID
     * @return 资源列表
     */
    @Override
    public List<CourseResourceDTO> getCourseResources(String courseId) {
        try {
            Long courseIdLong = Long.parseLong(courseId);
            logger.info("获取课程资源列表，课程ID: {}", courseIdLong);
            return studentMapper.findCourseResources(courseIdLong);
        } catch (Exception e) {
            logger.error("获取课程资源列表失败，课程ID: {}", courseId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取课程讨论列表
     *
     * @param courseId 课程ID
     * @return 讨论列表
     */
    @Override
    public List<DiscussionDTO> getCourseDiscussions(Long courseId) {
        try {
            logger.info("获取课程讨论列表，课程ID: {}", courseId);
            List<DiscussionDTO> discussions = studentMapper.findCourseDiscussions(courseId);
            
            // 添加日志输出讨论列表内容
            logger.info("获取到的讨论数量: {}", discussions != null ? discussions.size() : 0);
            if (discussions == null || discussions.isEmpty()) {
                logger.info("未找到课程讨论，返回空列表");
                return new ArrayList<>();
            }
            
            // 获取每个讨论的回复列表
            for (DiscussionDTO discussion : discussions) {
                // 获取讨论回复列表
                List<DiscussionDTO.ReplyDTO> replies = studentMapper.findDiscussionReplies(discussion.getId());
                discussion.setReplies(replies);
                
                // 处理标签
                if (discussion.getTags() != null && !discussion.getTags().isEmpty()) {
                    discussion.setTagList(Arrays.asList(discussion.getTags().split(",")));
                } else {
                    discussion.setTagList(new ArrayList<>());
                }
            }
            
            return discussions;
        } catch (Exception e) {
            logger.error("获取课程讨论列表失败，课程ID: {}", courseId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取讨论详情和回复列表
     *
     * @param discussionId 讨论ID
     * @return 讨论详情，包含回复列表
     */
    @Override
    public DiscussionDTO getDiscussionDetail(Long discussionId) {
        try {
            logger.info("获取讨论详情，讨论ID: {}", discussionId);
            DiscussionDTO discussion = studentMapper.findDiscussionDetail(discussionId);
            
            if (discussion != null) {
                // 增加浏览量
                studentMapper.incrementDiscussionViewCount(discussionId);
                
                // 获取讨论回复列表
                List<DiscussionDTO.ReplyDTO> replies = studentMapper.findDiscussionReplies(discussionId);
                discussion.setReplies(replies);
                
                // 处理标签
                if (discussion.getTags() != null && !discussion.getTags().isEmpty()) {
                    discussion.setTagList(Arrays.asList(discussion.getTags().split(",")));
                } else {
                    discussion.setTagList(new ArrayList<>());
                }
            }
            
            return discussion;
        } catch (Exception e) {
            logger.error("获取讨论详情失败，讨论ID: {}", discussionId, e);
            return null;
        }
    }
    
    /**
     * 获取课程测验列表
     *
     * @param userId 学生ID
     * @param courseId 课程ID
     * @return 测验列表
     */
    @Override
    public List<QuizDTO> getCourseQuizzes(Long userId, Long courseId) {
        try {
            logger.info("获取课程测验列表，学生ID: {}, 课程ID: {}", userId, courseId);
            return studentMapper.findCourseQuizzes(userId, courseId);
        } catch (Exception e) {
            logger.error("获取课程测验列表失败，学生ID: {}, 课程ID: {}", userId, courseId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取测验详情
     *
     * @param userId 学生ID
     * @param quizId 测验ID
     * @return 测验详情，包含问题列表
     */
    @Override
    public QuizDTO getQuizDetail(Long userId, Long quizId) {
        try {
            logger.info("获取测验详情，学生ID: {}, 测验ID: {}", userId, quizId);
            QuizDTO quiz = studentMapper.findQuizDetail(userId, quizId);
            
            if (quiz != null) {
                // 获取测验问题列表
                List<QuizDTO.QuizQuestionDTO> questions = studentMapper.findQuizQuestions(quizId);
                quiz.setQuestions(questions);
            }
            
            return quiz;
        } catch (Exception e) {
            logger.error("获取测验详情失败，学生ID: {}, 测验ID: {}", userId, quizId, e);
            return null;
        }
    }
    
    /**
     * 获取课程章节列表
     *
     * @param courseId 课程ID
     * @return 章节列表
     */
    @Override
    public List<Map<String, Object>> getCourseChapters(Long courseId) {
        try {
            logger.info("获取课程章节列表, courseId={}", courseId);
            // 使用null作为studentId，因为此处不需要学生特定的信息
            return studentMapper.findCourseChapters(courseId, null);
        } catch (Exception e) {
            logger.error("获取课程章节列表失败, courseId={}", courseId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取章节内容
     *
     * @param courseId 课程ID
     * @param chapterId 章节ID
     * @param studentId 学生ID (可为null)
     * @return 章节内容，包含章节信息和内容列表
     */
    @Override
    public Map<String, Object> getChapterContent(Long courseId, Long chapterId, Long studentId) {
        try {
            logger.info("获取章节内容, courseId={}, chapterId={}, studentId={}", courseId, chapterId, studentId);
            
            // 获取章节信息
            List<Map<String, Object>> chapters = studentMapper.findCourseChapters(courseId, studentId);
            System.out.println("查询结果："+chapters);
            if (chapters == null || chapters.isEmpty()) {
                logger.warn("未找到课程章节: courseId={}", courseId);
                return null;
            }
            
            logger.info("找到课程章节数量: {}", chapters.size());
            
            Map<String, Object> chapterInfo = null;
            
            // 查找指定章节
            for (Map<String, Object> chapter : chapters) {
                if (chapter.get("id").equals(chapterId)) {
                    chapterInfo = chapter;
                    break;
                }
            }
            
            if (chapterInfo == null) {
                logger.warn("未找到指定章节信息, courseId={}, chapterId={}", courseId, chapterId);
                return null;
            }
            
            // 获取章节内容列表
            List<Map<String, Object>> contents = studentMapper.findChapterContents(chapterId);
            System.out.println("查询结果："+contents);
            if (contents == null) {
                contents = new ArrayList<>();
                logger.warn("获取章节内容列表失败, chapterId={}", chapterId);
            } else {
                logger.info("获取章节内容列表成功, 内容数量: {}", contents.size());
                
                // 检查内容列表是否为空
                if (contents.isEmpty()) {
                    logger.warn("章节内容列表为空, chapterId={}", chapterId);
                }
            }
            
            // 组装结果
            Map<String, Object> result = new HashMap<>();
            result.put("id", chapterId);
            result.put("courseId", courseId);
            result.put("title", chapterInfo.get("title"));
            result.put("description", chapterInfo.get("description"));
            result.put("orderNum", chapterInfo.get("orderNum"));
            result.put("duration", chapterInfo.get("duration"));
            result.put("contents", contents);
            
            return result;
        } catch (Exception e) {
            logger.error("获取章节内容异常, courseId={}, chapterId={}", courseId, chapterId, e);
            return null;
        }
    }
    
    @Override
    public List<LearningNoteDTO> getLearningNotes(Long studentId, Long courseId, Long chapterId) {
        try {
            logger.info("获取学习笔记列表, studentId={}, courseId={}, chapterId={}", studentId, courseId, chapterId);
            return studentMapper.findLearningNotes(studentId, courseId, chapterId);
        } catch (Exception e) {
            logger.error("获取学习笔记列表异常", e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean addLearningNote(LearningNote note) {
        try {
            logger.info("添加学习笔记, userId={}, courseId={}, chapterId={}", note.getStudentId(), note.getCourseId(), note.getChapterId());
            return studentMapper.addLearningNote(note) > 0;
        } catch (Exception e) {
            logger.error("添加学习笔记异常", e);
            return false;
        }
    }

    @Override
    public boolean updateLearningNote(LearningNote note) {
        try {
            logger.info("更新学习笔记, noteId={}, userId={}", note.getId(), note.getStudentId());
            return studentMapper.updateLearningNote(note) > 0;
        } catch (Exception e) {
            logger.error("更新学习笔记异常", e);
            return false;
        }
    }

    @Override
    public boolean deleteLearningNote(Long noteId, Long studentId) {
        try {
            logger.info("删除学习笔记, noteId={}, studentId={}", noteId, studentId);
            return studentMapper.deleteLearningNote(noteId, studentId) > 0;
        } catch (Exception e) {
            logger.error("删除学习笔记异常", e);
            return false;
        }
    }

    @Override
    public ChapterRating getChapterRating(Long studentId, Long chapterId) {
        try {
            logger.info("获取章节评价, studentId={}, chapterId={}", studentId, chapterId);
            return studentMapper.findChapterRating(studentId, chapterId);
        } catch (Exception e) {
            logger.error("获取章节评价异常", e);
            return null;
        }
    }

    @Override
    public boolean saveChapterRating(ChapterRating rating) {
        try {
            logger.info("保存章节评价, userId={}, chapterId={}, rating={}", rating.getStudentId(), rating.getChapterId(), rating.getRating());
            return studentMapper.saveChapterRating(rating) > 0;
        } catch (Exception e) {
            logger.error("保存章节评价异常", e);
            return false;
        }
    }

    @Override
    public List<ChapterRatingDTO> getChapterRatings(Long chapterId) {
        try {
            logger.info("获取章节评价列表, chapterId={}", chapterId);
            return studentMapper.findChapterRatings(chapterId);
        } catch (Exception e) {
            logger.error("获取章节评价列表异常", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Integer getChapterAverageRating(Long chapterId) {
        try {
            logger.info("获取章节平均评分, chapterId={}", chapterId);
            return studentMapper.getChapterAverageRating(chapterId);
        } catch (Exception e) {
            logger.error("获取章节平均评分异常", e);
            return 0;
        }
    }

    @Override
    public LearningStatisticsDTO getLearningStatistics(Long studentId, Long courseId) {
        try {
            logger.info("获取课程学习统计, studentId={}, courseId={}", studentId, courseId);
            
            // 获取课程统计基本信息
            LearningStatisticsDTO statistics = studentMapper.getCourseStatistics(studentId, courseId);
            
            if (statistics != null) {
                // 获取章节详细统计
                List<LearningStatisticsDTO.ChapterStatDTO> chapterStats = studentMapper.getChapterStatistics(studentId, courseId);
                statistics.setChapterStats(chapterStats);
                
                logger.info("获取课程学习统计成功, 总章节数: {}, 已完成章节数: {}", 
                        statistics.getTotalChapters(), statistics.getCompletedChapters());
            } else {
                statistics = new LearningStatisticsDTO();
                statistics.setChapterStats(new ArrayList<>());
                statistics.setCourseId(courseId);
                statistics.setTotalChapters(0);
                statistics.setCompletedChapters(0);
                statistics.setTotalTime(0);
                statistics.setAverageRating(0);
            }
            
            return statistics;
        } catch (Exception e) {
            logger.error("获取课程学习统计异常", e);
            LearningStatisticsDTO emptyStats = new LearningStatisticsDTO();
            emptyStats.setChapterStats(new ArrayList<>());
            return emptyStats;
        }
    }

    // 更新现有的recordChapterAccess方法，使其同时更新学习统计
    @Override
    public boolean recordChapterAccess(Long userId, Long courseId, Long chapterId) {
        try {
            logger.info("记录章节访问, userId={}, courseId={}, chapterId={}", userId, courseId, chapterId);
            
            // 创建学习统计记录
            LearningStatisticsDetail stats = new LearningStatisticsDetail();
            stats.setStudentId(userId);
            stats.setCourseId(courseId);
            stats.setChapterId(chapterId);
            stats.setTotalTime(0); // 初始访问时不累计时间
            stats.setVisitCount(1); // 增加访问次数
            stats.setLastVisitTime(new Date()); // 使用当前时间，转换为Date类型
            stats.setCompletionStatus(0); // 默认未完成
            
            return studentMapper.updateLearningStatistics(stats) > 0;
        } catch (Exception e) {
            logger.error("记录章节访问异常", e);
            return false;
        }
    }
    
    @Override
    public boolean updateCourseProgress(Long studentId, Long courseId, Long chapterId, String lastPosition, BigDecimal completionPercentage) {
        try {
            logger.info("更新课程进度, studentId={}, courseId={}, chapterId={}, progress={}", 
                    studentId, courseId, chapterId, completionPercentage);
            
            // 更新课程进度
            boolean success = studentMapper.updateCourseProgress(studentId, courseId, chapterId, lastPosition, completionPercentage) > 0;
            
            // 如果进度更新成功且完成百分比达到100%，则更新学习统计中的完成状态
            if (success && completionPercentage != null && completionPercentage.compareTo(new BigDecimal("100")) >= 0) {
                LearningStatisticsDetail stats = new LearningStatisticsDetail();
                stats.setStudentId(studentId);
                stats.setCourseId(courseId);
                stats.setChapterId(chapterId);
                stats.setTotalTime(0); // 不更新时间
                stats.setVisitCount(0); // 不更新访问次数
                stats.setLastVisitTime(new Date()); // 使用当前时间，转换为Date类型
                stats.setCompletionStatus(1); // 标记为已完成
                
                studentMapper.updateLearningStatistics(stats);
            }
            
            return success;
        } catch (Exception e) {
            logger.error("更新课程进度异常", e);
            return false;
        }
    }
    
    @Override
    public boolean addLearningRecord(String userId, String courseId, String contentType, String contentId,
                                     LocalDateTime startTime, LocalDateTime endTime, Integer duration, BigDecimal progress) {
        try {
            logger.info("添加学习记录, userId={}, courseId={}, contentType={}, duration={}", userId, courseId, contentType, duration);
            
            // 添加学习记录
            boolean success = studentMapper.addLearningRecord(userId, courseId, contentType, contentId, startTime, endTime, duration, progress) > 0;
            
            // 如果是章节内容的学习记录，更新学习统计
            if (success && "CHAPTER".equals(contentType) && contentId != null) {
                try {
                    Long userIdLong = Long.valueOf(userId);
                    Long courseIdLong = Long.valueOf(courseId);
                    Long chapterIdLong = Long.valueOf(contentId);
                    
                    LearningStatisticsDetail stats = new LearningStatisticsDetail();
                    stats.setStudentId(userIdLong);
                    stats.setCourseId(courseIdLong);
                    stats.setChapterId(chapterIdLong);
                    stats.setTotalTime(duration); // 累计学习时间
                    stats.setVisitCount(0); // 不更新访问次数
                    stats.setLastVisitTime(new Date()); // 使用当前时间，转换为Date类型
                    stats.setCompletionStatus(progress != null && progress.compareTo(new BigDecimal("100")) >= 0 ? 1 : 0);
                    
                    studentMapper.updateLearningStatistics(stats);
                } catch (NumberFormatException e) {
                    logger.error("转换ID类型异常", e);
                }
            }
            
            return success;
        } catch (Exception e) {
            logger.error("添加学习记录异常", e);
            return false;
        }
    }

    /**
     * 获取仪表盘小部件数据
     *
     * @param userId 学生ID
     * @param widgetType 小部件类型
     * @return 小部件数据
     */
    @Override
    public Object getDashboardWidget(String userId, String widgetType) {
        try {
            logger.info("获取仪表盘小部件数据, userId={}, widgetType={}", userId, widgetType);
            
            switch (widgetType.toLowerCase()) {
                case "score-overview":
                    return getScoreOverview(userId);
                case "study-time":
                    return getStudyTime(userId);
                case "todo-tasks":
                    return getTodoTasks(userId);
                case "learning-stats":
                    return getLearningStats(userId);
                case "course-progress":
                    return getCourseProgress(userId);
                case "recommendations":
                    return getLearningRecommendations(userId);
                default:
                    logger.warn("未知的小部件类型: {}", widgetType);
                    return null;
            }
        } catch (Exception e) {
            logger.error("获取仪表盘小部件数据失败, userId={}, widgetType={}", userId, widgetType, e);
            return null;
        }
    }
    
    /**
     * 更新待办任务状态
     *
     * @param userId 学生ID
     * @param taskId 任务ID
     * @param status 任务状态
     * @return 是否更新成功
     */
    @Override
    public boolean updateTaskStatus(Long userId, Long taskId, Integer status) {
        try {
            logger.info("更新待办任务状态, userId={}, taskId={}, status={}", userId, taskId, status);
            
            // 这里简化为调用updateTodoTaskStatus方法
            boolean completed = (status == 1); // 假设1代表完成
            return updateTodoTaskStatus(taskId, completed);
        } catch (Exception e) {
            logger.error("更新待办任务状态失败, userId={}, taskId={}, status={}", userId, taskId, status, e);
            return false;
        }
    }
    
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
    @Override
    public boolean recordStudyTime(Long userId, Long courseId, LocalDateTime startTime, Integer duration, BigDecimal progress) {
        try {
            logger.info("记录学习时长, userId={}, courseId={}, duration={}, progress={}", userId, courseId, duration, progress);
            
            LocalDateTime endTime = startTime.plusMinutes(duration);
            
            // 调用现有的学习记录方法，转换参数类型
            return addLearningRecord(
                    userId.toString(),
                    courseId.toString(),
                    "course", // 内容类型为课程
                    null, // 不指定具体内容ID
                    startTime,
                    endTime,
                    duration * 60, // 转换为秒
                    progress
            );
        } catch (Exception e) {
            logger.error("记录学习时长失败, userId={}, courseId={}, duration={}", userId, courseId, duration, e);
            return false;
        }
    }
    
    /**
     * 获取测验问题列表
     *
     * @param quizId 测验ID
     * @return 问题列表
     */
    @Override
    public List<QuizDTO.QuizQuestionDTO> getQuizQuestions(Long quizId) {
        try {
            logger.info("获取测验问题列表, quizId={}", quizId);
            
            List<QuizDTO.QuizQuestionDTO> questions = studentMapper.findQuizQuestions(quizId);
            
            // 处理问题选项，将JSON字符串转换为数组
            if (questions != null) {
                for (QuizDTO.QuizQuestionDTO question : questions) {
                    if (question.getOptions() != null) {
                        // 如果需要将选项JSON转换为对象，可以在这里处理
                    }
                }
            }
            
            return questions;
        } catch (Exception e) {
            logger.error("获取测验问题列表失败, quizId={}", quizId, e);
            return null;
        }
    }

    /**
     * 创建新讨论
     *
     * @param userId 用户ID
     * @param courseId 课程ID
     * @param title 讨论标题
     * @param content 讨论内容
     * @return 是否创建成功
     */
    @Override
    public boolean createDiscussion(Long userId, Long courseId, String title, String content) {
        try {
            logger.info("创建新讨论，用户ID: {}, 课程ID: {}", userId, courseId);
            int result = studentMapper.createDiscussion(userId, courseId, title, content);
            return result > 0;
        } catch (Exception e) {
            logger.error("创建新讨论失败，用户ID: {}, 课程ID: {}", userId, courseId, e);
            return false;
        }
    }

    /**
     * 回复讨论
     *
     * @param userId 用户ID
     * @param discussionId 讨论ID
     * @param content 回复内容
     * @param parentId 父回复ID（可为null）
     * @return 是否回复成功
     */
    @Override
    public boolean replyToDiscussion(Long userId, Long discussionId, String content, Long parentId) {
        try {
            logger.info("回复讨论，用户ID: {}, 讨论ID: {}", userId, discussionId);
            int result = studentMapper.replyToDiscussion(userId, discussionId, content, parentId);
            
            if (result > 0) {
                // 更新讨论的回复数量和最后回复时间
                studentMapper.updateDiscussionReplyInfo(discussionId);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("回复讨论失败，用户ID: {}, 讨论ID: {}", userId, discussionId, e);
            return false;
        }
    }

    /**
     * 点赞讨论
     *
     * @param userId 用户ID
     * @param discussionId 讨论ID
     * @return 是否点赞成功
     */
    @Override
    public boolean likeDiscussion(Long userId, Long discussionId) {
        try {
            logger.info("点赞讨论，用户ID: {}, 讨论ID: {}", userId, discussionId);
            
            // 未点赞，添加点赞
            int result = studentMapper.addDiscussionLike(userId, discussionId);
            if (result > 0) {
                // 增加点赞数
                studentMapper.increaseDiscussionLikeCount(discussionId);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("点赞讨论失败，用户ID: {}, 讨论ID: {}", userId, discussionId, e);
            return false;
        }
    }
    
    /**
     * 切换讨论点赞状态
     *
     * @param userId 用户ID
     * @param discussionId 讨论ID
     * @return 当前点赞状态
     */
    @Override
    public boolean toggleDiscussionLike(Long userId, Long discussionId) {
        try {
            logger.info("切换讨论点赞状态，用户ID: {}, 讨论ID: {}", userId, discussionId);
            
            // 先检查是否已点赞
            boolean exists = studentMapper.checkDiscussionLike(userId, discussionId);
            if (exists) {
                // 已点赞，取消点赞
                studentMapper.removeDiscussionLike(userId, discussionId);
                // 减少点赞数
                studentMapper.decreaseDiscussionLikeCount(discussionId);
                return false;
            } else {
                // 未点赞，添加点赞
                int result = studentMapper.addDiscussionLike(userId, discussionId);
                if (result > 0) {
                    // 增加点赞数
                    studentMapper.increaseDiscussionLikeCount(discussionId);
                    return true;
                }
                return false;
            }
        } catch (Exception e) {
            logger.error("切换讨论点赞状态失败，用户ID: {}, 讨论ID: {}", userId, discussionId, e);
            return false;
        }
    }
    
    /**
     * 检查是否已点赞讨论
     *
     * @param userId 用户ID
     * @param discussionId 讨论ID
     * @return 是否已点赞
     */
    @Override
    public boolean checkDiscussionLike(Long userId, Long discussionId) {
        try {
            logger.info("检查讨论点赞状态，用户ID: {}, 讨论ID: {}", userId, discussionId);
            return studentMapper.checkDiscussionLike(userId, discussionId);
        } catch (Exception e) {
            logger.error("检查讨论点赞状态失败，用户ID: {}, 讨论ID: {}", userId, discussionId, e);
            return false;
        }
    }
} 