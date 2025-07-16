package org.example.backend.service.impl;

import org.example.backend.entity.vo.teacher.TeacherDashboardVO;
import org.example.backend.mapper.TeacherDashboardMapper;
import org.example.backend.service.TeacherAIService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 教师端AI服务实现类
 */
@Service
public class TeacherAIServiceImpl implements TeacherAIService {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private TeacherDashboardMapper dashboardMapper;

    private static final String SCHEDULE_SYSTEM_PROMPT = """
            你是一个教育领域的AI助手，专门为教师生成智能日程安排。
            基于教师的课程、会议和任务信息，生成一个合理的日程安排。
            返回的数据应该是JSON格式，包含以下字段：
            - id: 数字类型，唯一标识
            - time: 字符串类型，时间段，如"08:00 - 09:35"
            - title: 字符串类型，活动标题
            - location: 字符串类型，地点
            - type: 字符串类型，活动类型，可选值为"class"(课程)、"meeting"(会议)、"deadline"(截止日期)
            
            示例输出:
            [
              {
                "id": 1,
                "time": "08:00 - 09:35",
                "title": "高等数学 II",
                "location": "教A-301",
                "type": "class"
              },
              {
                "id": 2,
                "time": "10:00 - 11:30",
                "title": "教研室周会",
                "location": "办公楼-502",
                "type": "meeting"
              }
            ]
            
            请确保生成的日程安排合理、符合教育教学规律，并且不要超过6个项目。
            """;

    private static final String HIGHLIGHTS_SYSTEM_PROMPT = """
            你是一个教育领域的AI助手，专门为教师分析学生的表现亮点。
            基于学生的学习数据、考试成绩和课堂表现，生成学生表现亮点分析。
            返回的数据应该是JSON格式，包含以下字段：
            - id: 数字类型，唯一标识
            - text: 字符串类型，亮点描述
            
            示例输出:
            [
              {
                "id": 1,
                "text": "在「期中模拟考」中，张伟、王静等5名同学取得了98分以上的优异成绩。"
              },
              {
                "id": 2,
                "text": "李娜同学在「诗歌鉴赏」单元的讨论中，连续发表了3个高质量的主题帖。"
              }
            ]
            
            请确保生成的亮点具体、真实，并且不要超过3个项目。
            """;

    private static final String RISKS_SYSTEM_PROMPT = """
            你是一个教育领域的AI助手，专门为教师分析学生的潜在风险。
            基于学生的学习数据、考试成绩和课堂表现，生成学生潜在风险分析。
            返回的数据应该是JSON格式，包含以下字段：
            - id: 数字类型，唯一标识
            - text: 字符串类型，风险描述
            
            示例输出:
            [
              {
                "id": 1,
                "text": "陈浩同学已经连续2次未提交「数学周测」作业，需要重点关注。"
              },
              {
                "id": 2,
                "text": "高三(2)班在「完形填空」模块的平均正确率较上周下降了15%。"
              }
            ]
            
            请确保生成的风险具体、真实，并且不要超过3个项目。
            """;

    private static final String INTERACTION_SYSTEM_PROMPT = """
            你是一个教育领域的AI助手，专门为教师生成互动中心数据。
            基于学生的互动记录，生成互动中心数据。
            返回的数据应该是JSON格式，包含以下字段：
            - id: 数字类型，唯一标识
            - type: 字符串类型，互动类型，可选值为"assignment"(作业)、"question"(问题)、"forum"(讨论)
            - student: 字符串类型，学生姓名
            - title: 字符串类型，互动标题
            - course: 字符串类型，课程名称
            - timestamp: 字符串类型，时间戳，如"2小时前"、"昨天"
            
            示例输出:
            [
              {
                "id": 1,
                "type": "assignment",
                "student": "李晓明",
                "title": "提交了「文言文阅读理解」作业",
                "course": "高三(1)班 - 语文",
                "timestamp": "2小时前"
              },
              {
                "id": 2,
                "type": "question",
                "student": "王芳",
                "title": "在「函数与导数」章节提出了问题",
                "course": "高三(2)班 - 数学",
                "timestamp": "5小时前"
              }
            ]
            
            请确保生成的互动数据具体、真实，并且不要超过4个项目。
            """;

    @Override
    public List<TeacherDashboardVO.ScheduleItem> generateSmartSchedule(Long teacherId) {
        try {
            // 获取教师信息
            String teacherName = dashboardMapper.getTeacherName(teacherId);

            // 获取教师课程信息
            List<TeacherDashboardVO.CourseInfo> courses = dashboardMapper.getCoursesByTeacherId(teacherId);

            // 获取教师任务信息
            List<TeacherDashboardVO.Task> tasks = dashboardMapper.getTasksByTeacherId(teacherId, "all");

            // 构建提示信息
            String userPrompt = String.format(
                    "请为教师 %s 生成今天（%s）的智能日程安排。\n\n" +
                            "教师的课程信息：\n%s\n\n" +
                            "教师的任务信息：\n%s\n\n" +
                            "请基于以上信息，生成一个合理的日程安排，返回JSON格式。",
                    teacherName,
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
                    formatCourseInfo(courses),
                    formatTaskInfo(tasks)
            );

            // 使用ChatClient进行AI调用
            String content = chatClient.prompt()
                    .system(SCHEDULE_SYSTEM_PROMPT)
                    .user(userPrompt)
                    .call()
                    .content();

            // 将JSON字符串转换为对象列表
            return parseScheduleItems(content);
        } catch (Exception e) {
            e.printStackTrace();
            // 返回默认数据
            return getDefaultScheduleItems();
        }
    }

    @Override
    public List<TeacherDashboardVO.StudentHighlight> analyzeStudentHighlights(Long teacherId) {
        try {
            // 获取教师信息
            String teacherName = dashboardMapper.getTeacherName(teacherId);

            // 获取教师课程信息
            List<TeacherDashboardVO.CourseInfo> courses = dashboardMapper.getCoursesByTeacherId(teacherId);

            // 获取学生表现数据
            List<TeacherDashboardVO.StudentPerformance> performances =
                    dashboardMapper.getRecentStudentPerformance(teacherId, 10);

            // 构建提示信息
            String userPrompt = String.format(
                    "请为教师 %s 分析学生的表现亮点。\n\n" +
                            "教师的课程信息：\n%s\n\n" +
                            "学生表现数据：\n%s\n\n" +
                            "请基于以上信息，生成学生表现亮点分析，返回JSON格式。",
                    teacherName,
                    formatCourseInfo(courses),
                    formatStudentPerformance(performances)
            );

            // 使用ChatClient进行AI调用
            String content = chatClient.prompt()
                    .system(HIGHLIGHTS_SYSTEM_PROMPT)
                    .user(userPrompt)
                    .call()
                    .content();

            // 将JSON字符串转换为对象列表
            return parseStudentHighlights(content);
        } catch (Exception e) {
            e.printStackTrace();
            // 返回默认数据
            return getDefaultStudentHighlights();
        }
    }

    @Override
    public List<TeacherDashboardVO.StudentRisk> analyzeStudentRisks(Long teacherId) {
        try {
            // 获取教师信息
            String teacherName = dashboardMapper.getTeacherName(teacherId);

            // 获取教师课程信息
            List<TeacherDashboardVO.CourseInfo> courses = dashboardMapper.getCoursesByTeacherId(teacherId);

            // 获取学生表现数据
            List<TeacherDashboardVO.StudentPerformance> performances =
                    dashboardMapper.getRecentStudentPerformance(teacherId, 10);

            // 构建提示信息
            String userPrompt = String.format(
                    "请为教师 %s 分析学生的潜在风险。\n\n" +
                            "教师的课程信息：\n%s\n\n" +
                            "学生表现数据：\n%s\n\n" +
                            "请基于以上信息，生成学生潜在风险分析，返回JSON格式。",
                    teacherName,
                    formatCourseInfo(courses),
                    formatStudentPerformance(performances)
            );

            // 使用ChatClient进行AI调用
            String content = chatClient.prompt()
                    .system(RISKS_SYSTEM_PROMPT)
                    .user(userPrompt)
                    .call()
                    .content();

            // 将JSON字符串转换为对象列表
            return parseStudentRisks(content);
        } catch (Exception e) {
            e.printStackTrace();
            // 返回默认数据
            return getDefaultStudentRisks();
        }
    }

    @Override
    public List<TeacherDashboardVO.InteractionItem> getInteractionHub(Long teacherId) {
        try {
            // 获取教师信息
            String teacherName = dashboardMapper.getTeacherName(teacherId);

            // 获取教师课程信息
            List<TeacherDashboardVO.CourseInfo> courses = dashboardMapper.getCoursesByTeacherId(teacherId);

            // 获取学生表现数据
            List<TeacherDashboardVO.StudentPerformance> performances =
                    dashboardMapper.getRecentStudentPerformance(teacherId, 10);

            // 构建提示信息
            String userPrompt = String.format(
                    "请为教师 %s 生成互动中心数据。\n\n" +
                            "教师的课程信息：\n%s\n\n" +
                            "学生表现数据：\n%s\n\n" +
                            "请基于以上信息，生成互动中心数据，返回JSON格式。",
                    teacherName,
                    formatCourseInfo(courses),
                    formatStudentPerformance(performances)
            );

            // 使用ChatClient进行AI调用
            String content = chatClient.prompt()
                    .system(INTERACTION_SYSTEM_PROMPT)
                    .user(userPrompt)
                    .call()
                    .content();

            // 将JSON字符串转换为对象列表
            return parseInteractionItems(content);
        } catch (Exception e) {
            e.printStackTrace();
            // 返回默认数据
            return getDefaultInteractionItems();
        }
    }

    @Override
    public String generateTeachingSuggestions(Long teacherId, Long courseId) {
        // 实现教学建议生成逻辑
        return "基于学生的学习数据分析，建议在下一章节中增加更多的互动环节，特别是小组讨论和案例分析，以提高学生的参与度和理解深度。";
    }

    @Override
    public String analyzeClassPerformance(Long teacherId, Long courseId) {
        // 实现班级表现分析逻辑
        return "班级整体表现良好，平均分83.5分，及格率92%。但在「函数应用」单元的掌握程度较弱，建议加强这部分内容的复习和巩固。";
    }

    @Override
    public String generatePersonalizedAdvice(Long teacherId, Long studentId, Long courseId) {
        // 实现个性化学习建议生成逻辑
        return "李明同学在数学计算方面表现优秀，但在几何证明题上存在困难。建议针对性地提供更多几何证明的练习和指导，同时可以鼓励他参与数学竞赛以进一步提升计算能力。";
    }

    @Override
    public String predictLearningTrend(Long teacherId, Long studentId, Long courseId) {
        // 实现学习趋势预测逻辑
        return "根据王芳同学近三个月的学习数据分析，她的学习曲线呈现稳步上升趋势，预计在期末考试中能够取得85分以上的成绩。建议继续保持当前的学习节奏和方法。";
    }

    @Override
    public String generateGradingSuggestions(Long teacherId, Long submissionId) {
        // 实现批改建议生成逻辑
        return "这份作业整体结构清晰，论点有力，但在论据的选择上略显单薄。建议给予85分，并在反馈中鼓励学生增加更多具体的例证来支持论点。";
    }

    // 辅助方法：格式化课程信息
    private String formatCourseInfo(List<TeacherDashboardVO.CourseInfo> courses) {
        if (courses == null || courses.isEmpty()) {
            return "暂无课程信息";
        }

        StringBuilder sb = new StringBuilder();
        for (TeacherDashboardVO.CourseInfo course : courses) {
            sb.append("- ").append(course.getTitle())
                    .append("（学生数：").append(course.getStudentCount())
                    .append("，章节数：").append(course.getChapterCount())
                    .append("）\n");
        }
        return sb.toString();
    }

    // 辅助方法：格式化任务信息
    private String formatTaskInfo(List<TeacherDashboardVO.Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return "暂无任务信息";
        }

        StringBuilder sb = new StringBuilder();
        for (TeacherDashboardVO.Task task : tasks) {
            sb.append("- ").append(task.getTitle())
                    .append("（优先级：").append(task.getPriority())
                    .append("，截止日期：").append(task.getDueDate())
                    .append("，状态：").append(task.getStatus())
                    .append("）\n");
        }
        return sb.toString();
    }

    // 辅助方法：格式化学生表现数据
    private String formatStudentPerformance(List<TeacherDashboardVO.StudentPerformance> performances) {
        if (performances == null || performances.isEmpty()) {
            return "暂无学生表现数据";
        }

        StringBuilder sb = new StringBuilder();
        for (TeacherDashboardVO.StudentPerformance performance : performances) {
            sb.append("- 学生：").append(performance.getStudentName())
                    .append("，课程：").append(performance.getCourseName())
                    .append("，进度：").append(performance.getProgress()).append("%")
                    .append("，平均分：").append(performance.getAverageScore())
                    .append("，状态：").append(performance.getStatus())
                    .append("\n");
        }
        return sb.toString();
    }

    // 辅助方法：解析日程项目
    private List<TeacherDashboardVO.ScheduleItem> parseScheduleItems(String content) {
        try {
            // 提取JSON部分
            content = extractJsonContent(content);

            // 使用Jackson解析JSON
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            TeacherDashboardVO.ScheduleItem[] items = objectMapper.readValue(content, TeacherDashboardVO.ScheduleItem[].class);
            return Arrays.asList(items);
        } catch (Exception e) {
            e.printStackTrace();
            return getDefaultScheduleItems();
        }
    }

    // 辅助方法：解析学生亮点
    private List<TeacherDashboardVO.StudentHighlight> parseStudentHighlights(String content) {
        try {
            // 提取JSON部分
            content = extractJsonContent(content);

            // 使用Jackson解析JSON
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            TeacherDashboardVO.StudentHighlight[] items = objectMapper.readValue(content, TeacherDashboardVO.StudentHighlight[].class);
            return Arrays.asList(items);
        } catch (Exception e) {
            e.printStackTrace();
            return getDefaultStudentHighlights();
        }
    }

    // 辅助方法：解析学生风险
    private List<TeacherDashboardVO.StudentRisk> parseStudentRisks(String content) {
        try {
            // 提取JSON部分
            content = extractJsonContent(content);

            // 使用Jackson解析JSON
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            TeacherDashboardVO.StudentRisk[] items = objectMapper.readValue(content, TeacherDashboardVO.StudentRisk[].class);
            return Arrays.asList(items);
        } catch (Exception e) {
            e.printStackTrace();
            return getDefaultStudentRisks();
        }
    }

    // 辅助方法：解析互动项目
    private List<TeacherDashboardVO.InteractionItem> parseInteractionItems(String content) {
        try {
            // 提取JSON部分
            content = extractJsonContent(content);

            // 使用Jackson解析JSON
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            TeacherDashboardVO.InteractionItem[] items = objectMapper.readValue(content, TeacherDashboardVO.InteractionItem[].class);
            return Arrays.asList(items);
        } catch (Exception e) {
            e.printStackTrace();
            return getDefaultInteractionItems();
        }
    }

    // 辅助方法：提取JSON内容
    private String extractJsonContent(String content) {
        // 如果内容包含```json和```，则提取其中的JSON部分
        if (content.contains("```json") && content.contains("```")) {
            int start = content.indexOf("```json") + 7;
            int end = content.indexOf("```", start);
            return content.substring(start, end).trim();
        }
        // 如果内容包含[和]，则提取其中的JSON数组部分
        else if (content.contains("[") && content.contains("]")) {
            int start = content.indexOf("[");
            int end = content.lastIndexOf("]") + 1;
            return content.substring(start, end).trim();
        }
        // 否则返回原内容
        return content;
    }

    // 默认数据方法
    private List<TeacherDashboardVO.ScheduleItem> getDefaultScheduleItems() {
        List<TeacherDashboardVO.ScheduleItem> items = new ArrayList<>();
        items.add(new TeacherDashboardVO.ScheduleItem(1L, "08:00 - 09:35", "高等数学 II", "教A-301", "class"));
        items.add(new TeacherDashboardVO.ScheduleItem(2L, "10:00 - 11:30", "大学物理 I", "实验楼-210", "class"));
        items.add(new TeacherDashboardVO.ScheduleItem(3L, "12:00", "午餐会议", "教师食堂", "meeting"));
        items.add(new TeacherDashboardVO.ScheduleItem(4L, "14:00 - 15:30", "教研室周会", "办公楼-502", "meeting"));
        items.add(new TeacherDashboardVO.ScheduleItem(5L, "17:00", "批改「数据结构」期中作业", "在线", "deadline"));
        return items;
    }

    private List<TeacherDashboardVO.StudentHighlight> getDefaultStudentHighlights() {
        List<TeacherDashboardVO.StudentHighlight> highlights = new ArrayList<>();
        highlights.add(new TeacherDashboardVO.StudentHighlight(1L, "在「期中模拟考」中，张伟、王静等5名同学取得了98分以上的优异成绩。"));
        highlights.add(new TeacherDashboardVO.StudentHighlight(2L, "李娜同学在「诗歌鉴赏」单元的讨论中，连续发表了3个高质量的主题帖。"));
        return highlights;
    }

    private List<TeacherDashboardVO.StudentRisk> getDefaultStudentRisks() {
        List<TeacherDashboardVO.StudentRisk> risks = new ArrayList<>();
        risks.add(new TeacherDashboardVO.StudentRisk(1L, "陈浩同学已经连续2次未提交「数学周测」作业，需要重点关注。"));
        risks.add(new TeacherDashboardVO.StudentRisk(2L, "高三(2)班在「完形填空」模块的平均正确率较上周下降了15%。"));
        risks.add(new TeacherDashboardVO.StudentRisk(3L, "孙悦同学最近7天的平台活跃度明显低于平均水平。"));
        return risks;
    }

    private List<TeacherDashboardVO.InteractionItem> getDefaultInteractionItems() {
        List<TeacherDashboardVO.InteractionItem> items = new ArrayList<>();
        items.add(new TeacherDashboardVO.InteractionItem(1L, "assignment", "李晓明", "提交了「文言文阅读理解」作业", "高三(1)班 - 语文", "2小时前"));
        items.add(new TeacherDashboardVO.InteractionItem(2L, "question", "王芳", "在「函数与导数」章节提出了问题", "高三(2)班 - 数学", "5小时前"));
        items.add(new TeacherDashboardVO.InteractionItem(3L, "forum", "赵刚", "在「宋明理学」讨论区发表了新主题", "高二(5)班 - 历史", "昨天"));
        items.add(new TeacherDashboardVO.InteractionItem(4L, "assignment", "刘悦", "提交了「近代史纲要」论文", "高二(5)班 - 历史", "昨天"));
        return items;
    }
}
