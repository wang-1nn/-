package org.example.backend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.entity.dto.*;
import org.example.backend.entity.pojo.AdaptiveExam;
import org.example.backend.entity.pojo.AdaptiveExamResult;
import org.example.backend.entity.pojo.AdaptiveQuestion;
import org.example.backend.mapper.AdaptiveExamMapper;
import org.example.backend.mapper.CourseMapper;
import org.example.backend.service.AdaptiveExamService;
import org.example.backend.service.ai.TeacherAi.QuestionAiService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdaptiveExamServiceImpl implements AdaptiveExamService {

    @Autowired
    CourseMapper courseMapper;
    
    @Autowired
    private final AdaptiveExamMapper adaptiveExamMapper;
    
    @Autowired
    private ChatClient chatClient;

    @Autowired
    private final ObjectMapper objectMapper;
    
    @Autowired
    private QuestionAiService questionAiService;

    // 系统提示模板
    private static final String QUESTION_GENERATION_PROMPT_TEMPLATE = """
            你是一个教育测试专家AI系统。请生成一个关于{subject}的{topic}相关的{difficulty}难度测试题目。
            
            题目要求：
            1. 题目类型: {questionType}
            2. 难度级别: {difficulty} (1-5的难度，1最简单，5最困难)
            3. 知识点: {topic}
            4. 清晰明确，没有歧义
            5. 对于选择题，提供4个选项(A,B,C,D)，其中只有一个正确答案
            6. 题目不要重复
            
            请使用以下JSON格式返回：
            ```json
            {
              "questionType": "题目类型(SINGLE_CHOICE/MULTI_CHOICE/SHORT_ANSWER)",
              "content": "题目内容",
              "options": [{"id": "A", "text": "选项A内容"}, {"id": "B", "text": "选项B内容"}, ...],
              "answer": "正确答案",
              "explanation": "答案解析",
              "difficulty": 难度系数(1-5),
              "topic": "所属知识点"
            }
            ```
            
            只返回JSON格式的内容，不要包含其他文字说明。
            """;
            
    private static final String ANSWER_EVALUATION_PROMPT_TEMPLATE = """
            你是一个教育测试评估专家AI系统。请评估学生对以下题目的回答是否正确。
            
            题目信息:
            题目类型: {questionType}
            题目内容: {questionContent}
            正确答案: {correctAnswer}
            
            学生答案: {userAnswer}
            
            请根据以下标准进行评估:
            1. 对于选择题，答案必须完全匹配才算正确
            2. 对于简答题，评估答案是否包含关键概念，关键词，是否正确理解了问题
            3. 给出评分和解析
            
            请使用以下JSON格式返回：
            ```json
            {
              "isCorrect": true/false,
              "score": 分数(0-100之间的百分比),
              "feedback": "评价反馈",
              "correctExplanation": "正确答案的详细解释"
            }
            ```
            
            只返回JSON格式的内容，不要包含其他文字说明。
            """;
            
    private static final String RESULT_ANALYSIS_PROMPT_TEMPLATE = """
            你是一个教育数据分析专家AI系统。请根据学生在自适应测试中的表现进行详细分析，并提供学习建议。
            
            测试基本信息:
            科目: {subject}
            知识点范围: {topics}
            题目数量: {questionCount}
            答对题目数: {correctCount}
            最终得分: {score}
            各知识点得分情况: {topicScores}
            
            请提供以下分析:
            1. 学生的总体能力水平评估
            2. 学生的优势知识点（至少3个）
            3. 学生的弱势知识点（至少3个）
            4. 详细的学习建议，针对弱势知识点提供改进方法
            5. 后续学习计划建议
            
            请使用以下JSON格式返回：
            ```json
            {
              "abilityLevel": "能力水平描述",
              "abilityScore": 能力水平分数(0-100),
              "strengths": ["优势知识点1", "优势知识点2", "优势知识点3"],
              "weaknesses": ["弱势知识点1", "弱势知识点2", "弱势知识点3"],
              "analysis": "详细分析报告",
              "recommendations": "学习建议"
            }
            ```
            
            只返回JSON格式的内容，不要包含其他文字说明。
            """;

    @Override
    public List<Map<String, Object>> getAllSubjects() {
        List<Map<String, Object>> subjects = courseMapper.findDistinctSubjects();
        
        // 为每个科目添加ID
        int id = 1;
        for (Map<String, Object> subject : subjects) {
            subject.put("id", id++);
        }
        
        return subjects;
    }

    @Override
    @Transactional
    public AdaptiveExamDTO createExam(ExamCreateRequestDTO request) {
        // 模拟用户ID，实际应该从登录信息中获取
        Long userId = 1L;

        AdaptiveExam exam = new AdaptiveExam();
        exam.setUserId(userId);
        exam.setSubject(request.getSubjectName());
        exam.setDifficulty(request.getSettings().getStartingDifficulty());
        exam.setStartingDifficulty(request.getSettings().getStartingDifficulty());
        exam.setQuestionCount(request.getSettings().getQuestionCount());
        exam.setCurrentQuestion(0);
        exam.setTimeLimit(request.getSettings().getTimeLimit());
        
        // 处理知识点
        List<String> allTopics = new ArrayList<>();
        if (request.getSettings().getCustomTopics() != null && !request.getSettings().getCustomTopics().isEmpty()) {
            allTopics.addAll(request.getSettings().getCustomTopics());
        }
        exam.setTopics(String.join(",", allTopics));
        exam.setStatus("CREATED");
        exam.setCreatedAt(LocalDateTime.now());

        adaptiveExamMapper.insertExam(exam);
        
        // 同步生成题目
        List<AdaptiveQuestion> generatedQuestions = new ArrayList<>();
        if (request.getSettings().getQuestionCount() > 0) {
            try {
                int questionCount = request.getSettings().getQuestionCount();
                log.info("开始生成{}道题目", questionCount);
                
                // 随机生成不同类型的题目
                QuestionType[] questionTypes = QuestionType.values();
                Random random = new Random();
                
                for (int i = 0; i < questionCount; i++) {
                    // 将ExamCreateRequestDTO转换为QGRequest
                    QGRequest qgRequest = new QGRequest();
                    qgRequest.setCount(1); // 每次生成一题
                    qgRequest.setSubject(request.getSubjectName());
                    qgRequest.setKnowledgePoints(request.getSettings().getCustomTopics());
                    
                    // 设置难度
                    switch (request.getSettings().getStartingDifficulty()) {
                        case "easy":
                            qgRequest.setLevel(DifficultyLevel.EASY);
                            break;
                        case "hard":
                            qgRequest.setLevel(DifficultyLevel.HARD);
                            break;
                        default:
                            qgRequest.setLevel(DifficultyLevel.MEDIUM);
                    }
                    
                    // 随机选择题型
                    QuestionType randomType = questionTypes[random.nextInt(questionTypes.length)];
                    qgRequest.setType(randomType);
                    
                    try {
                        // 同步调用QuestionAiService生成题目
                        String questionJson = questionAiService.generate(qgRequest)
                            .blockFirst(); // 阻塞获取第一个结果
                        
                        if (questionJson != null) {
                            // 解析JSON
                            Map<String, Object> questionMap = objectMapper.readValue(questionJson, Map.class);
                            
                            // 创建题目实体
                            AdaptiveQuestion question = new AdaptiveQuestion();
                            question.setExamId(exam.getId());
                            
                            // 根据题型设置questionType
                            String questionType;
                            switch (randomType) {
                                case CHOICE:
                                    questionType = "SINGLE_CHOICE";
                                    break;
                                case FILL:
                                    questionType = "FILL_BLANK";
                                    break;
                                case SHORT:
                                    questionType = "SHORT_ANSWER";
                                    break;
                                case JUDGE:
                                    questionType = "TRUE_FALSE";
                                    break;
                                default:
                                    questionType = "SINGLE_CHOICE";
                            }
                            question.setQuestionType(questionType);
                            
                            // 获取题目内容，兼容不同的字段名
                            String content = null;
                            if (questionMap.containsKey("question")) {
                                content = (String) questionMap.get("question");
                            } else if (questionMap.containsKey("content")) {
                                content = (String) questionMap.get("content");
                            }
                            
                            if (content == null) {
                                // 如果内容为空，使用默认内容
                                content = "题目内容生成失败，请重新尝试";
                                log.warn("题目内容为空，使用默认内容");
                            }
                            question.setContent(content);
                            
                            // 处理选项
                            List<Map<String, Object>> formattedOptions = new ArrayList<>();
                            if (randomType == QuestionType.CHOICE) {
                                // 选择题才需要处理选项
                                @SuppressWarnings("unchecked")
                                List<String> options = (List<String>) questionMap.get("options");
                                
                                if (options != null) {
                                    for (int j = 0; j < options.size(); j++) {
                                        Map<String, Object> option = new HashMap<>();
                                        option.put("id", String.valueOf((char)('A' + j)));
                                        option.put("text", options.get(j));
                                        formattedOptions.add(option);
                                    }
                                }
                            }
                            
                            question.setOptions(objectMapper.writeValueAsString(formattedOptions));
                            
                            // 处理答案
                            String answer = (String) questionMap.get("answer");
                            if (answer == null) {
                                // 如果答案为空，使用默认答案
                                answer = "答案生成失败";
                                log.warn("题目答案为空，使用默认答案");
                            }
                            question.setAnswer(answer);
                            
                            // 设置知识点
                            if (allTopics.isEmpty()) {
                                question.setTopic("综合知识点");
                            } else {
                                question.setTopic(allTopics.get(random.nextInt(allTopics.size())));
                            }
                            
                            // 设置难度
                            int difficulty;
                            switch (request.getSettings().getStartingDifficulty()) {
                                case "easy":
                                    difficulty = 1;
                                    break;
                                case "hard":
                                    difficulty = 5;
                                    break;
                                default:
                                    difficulty = 3;
                            }
                            question.setDifficulty(difficulty);
                            
                            // 根据题目难度设置分值
                            question.setScore(calculateScore(question.getDifficulty()));
                            question.setCreatedAt(LocalDateTime.now());
                            
                            // 保存题目
                            adaptiveExamMapper.insertQuestion(question);
                            generatedQuestions.add(question);
                            
                            log.info("成功生成第{}道题目", i + 1);
                        } else {
                            log.error("生成第{}道题目失败，返回为空", i + 1);
                        }
                    } catch (Exception e) {
                        log.error("生成第{}道题目出错: {}", i + 1, e.getMessage());
                    }
                }
                
                log.info("题目生成完成，成功生成{}道题目", generatedQuestions.size());
            } catch (Exception e) {
                log.error("生成题目失败", e);
            }
        }
        
        // 转换为DTO
        AdaptiveExamDTO examDTO = convertToDTO(exam);
        
        // 添加生成的题目
        List<QuestionDTO> questionDTOs = generatedQuestions.stream()
                .map(this::convertToQuestionDTO)
                .collect(Collectors.toList());
        examDTO.setQuestions(questionDTOs);
        
        return examDTO;
    }

    @Override
    public AdaptiveExamDTO getExamById(Long examId) {
        AdaptiveExam exam = adaptiveExamMapper.findById(examId);
        if (exam == null) {
            throw new RuntimeException("测试不存在");
        }
        
        AdaptiveExamDTO dto = convertToDTO(exam);
        
        // 如果测试已经开始，计算剩余时间
        if ("IN_PROGRESS".equals(exam.getStatus()) && exam.getStartTime() != null) {
            // 计算已经过去的时间（分钟）
            LocalDateTime now = LocalDateTime.now();
            long elapsedSeconds = java.time.Duration.between(exam.getStartTime(), now).getSeconds();
            
            // 计算剩余时间（秒）
            int totalSeconds = exam.getTimeLimit() * 60;
            int remainingSeconds = Math.max(0, totalSeconds - (int)elapsedSeconds);
            
            // 设置剩余时间
            dto.setRemainingTime(remainingSeconds);
        }
        
        // 获取题目列表
        List<AdaptiveQuestion> questions = adaptiveExamMapper.findQuestionsByExamId(examId);
        if (!questions.isEmpty()) {
            List<QuestionDTO> questionDTOs = questions.stream()
                    .map(this::convertToQuestionDTO)
                    .collect(Collectors.toList());
            dto.setQuestions(questionDTOs);
        }
        
        return dto;
    }

    @Override
    @Transactional
    public AdaptiveExamDTO startExam(Long examId) {
        AdaptiveExam exam = adaptiveExamMapper.findById(examId);
        if (exam == null) {
            throw new RuntimeException("测试不存在");
        }
        
        if (!"CREATED".equals(exam.getStatus())) {
            throw new RuntimeException("测试已经开始或已完成");
        }
        
        exam.setStatus("IN_PROGRESS");
        exam.setStartTime(LocalDateTime.now());
        adaptiveExamMapper.updateExam(exam);
        
        return convertToDTO(exam);
    }

    @Override
    @Transactional
    public QuestionDTO getNextQuestion(Long examId, Long lastQuestionId) {
        AdaptiveExam exam = adaptiveExamMapper.findById(examId);
        if (exam == null) {
            throw new RuntimeException("测试不存在");
        }
        
        if (!"IN_PROGRESS".equals(exam.getStatus())) {
            throw new RuntimeException("测试未开始或已完成");
        }
        
        // 检查是否已达到题目上限
        if (exam.getCurrentQuestion() >= exam.getQuestionCount()) {
            throw new RuntimeException("已完成所有题目");
        }
        
        // 如果有上一题，获取上一题的答题情况，调整难度
        if (lastQuestionId != null) {
            AdaptiveQuestion lastQuestion = adaptiveExamMapper.findQuestionById(lastQuestionId);
            if (lastQuestion != null && lastQuestion.getIsCorrect() != null) {
                // 根据答题正确与否调整难度
                adjustDifficulty(exam, lastQuestion.getIsCorrect());
            }
        }
        
        // 首先尝试查找是否有预生成的题目
        AdaptiveQuestion question = adaptiveExamMapper.findNextGeneratedQuestion(examId, exam.getCurrentQuestion());
        
        // 如果没有预生成的题目，则实时生成
        if (question == null) {
            log.info("没有找到预生成题目，开始实时生成题目");
            question = generateQuestion(exam);
            adaptiveExamMapper.insertQuestion(question);
        } else {
            log.info("使用预生成题目: {}", question.getId());
        }
        
        // 更新测试的当前题目序号
        exam.setCurrentQuestion(exam.getCurrentQuestion() + 1);
        adaptiveExamMapper.updateExam(exam);
        
        return convertToQuestionDTO(question);
    }

    @Override
    public QuestionDTO preloadNextQuestion(Long examId) {
        AdaptiveExam exam = adaptiveExamMapper.findById(examId);
        if (exam == null) {
            throw new RuntimeException("测试不存在");
        }
        
        if (!"IN_PROGRESS".equals(exam.getStatus())) {
            throw new RuntimeException("测试未开始或已完成");
        }
        
        // 检查是否已达到题目上限
        if (exam.getCurrentQuestion() >= exam.getQuestionCount()) {
            throw new RuntimeException("已完成所有题目");
        }
        
        // 生成下一题，但不保存到数据库，也不更新考试状态
        AdaptiveQuestion question = generateQuestion(exam);
        
        return convertToQuestionDTO(question);
    }

    @Override
    @Transactional
    public AnswerResultDTO submitAnswer(Long examId, Long questionId, AnswerSubmitDTO answerSubmit) {
        AdaptiveExam exam = adaptiveExamMapper.findById(examId);
        if (exam == null) {
            throw new RuntimeException("测试不存在");
        }
        
        if (!"IN_PROGRESS".equals(exam.getStatus())) {
            throw new RuntimeException("测试未开始或已完成");
        }
        
        AdaptiveQuestion question = adaptiveExamMapper.findQuestionById(questionId);
        if (question == null) {
            throw new RuntimeException("题目不存在");
        }
        
        if (question.getUserAnswer() != null) {
            throw new RuntimeException("该题目已作答");
        }
        
        // 记录用户答案
        question.setUserAnswer(answerSubmit.getAnswer());
        question.setAnsweredAt(LocalDateTime.now());
        
        // 使用AI评估答案
        Map<String, Object> evaluationResult = evaluateAnswer(question);
        
        // 更新题目评估结果
        boolean isCorrect = (Boolean) evaluationResult.get("isCorrect");
        double score = ((Number) evaluationResult.get("score")).doubleValue();
        
        question.setIsCorrect(isCorrect);
        adaptiveExamMapper.updateQuestionAnswer(question);
        
        // 创建返回DTO
        AnswerResultDTO result = new AnswerResultDTO();
        result.setQuestionId(questionId);
        result.setIsCorrect(isCorrect);
        result.setScore(score);
        result.setCorrectAnswer(question.getAnswer());
        result.setExplanation((String) evaluationResult.get("correctExplanation"));
        
        // 检查是否是最后一题
        boolean isLastQuestion = exam.getCurrentQuestion() >= exam.getQuestionCount();
        result.setIsLastQuestion(isLastQuestion);
        
        if (isLastQuestion) {
            // 如果是最后一题，完成测试
            completeExam(examId);
        }
        
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> completeExam(Long examId) {
        AdaptiveExam exam = adaptiveExamMapper.findById(examId);
        if (exam == null) {
            throw new RuntimeException("测试不存在");
        }
        
        // 如果测试已完成，直接返回结果
        if ("COMPLETED".equals(exam.getStatus())) {
            return getExamResult(examId);
        }
        
        // 获取测试题目列表
        List<AdaptiveQuestion> questions = adaptiveExamMapper.findQuestionsByExamId(examId);
        
        // 计算得分
        int totalQuestions = questions.size();
        long correctCount = questions.stream()
                .filter(q -> q.getIsCorrect() != null && q.getIsCorrect())
                .count();
        
        double score = totalQuestions > 0 ? 
                ((double) correctCount / totalQuestions) * 100 : 0;
        
        // 计算各知识点得分
        Map<String, List<AdaptiveQuestion>> questionsByTopic = questions.stream()
                .collect(Collectors.groupingBy(AdaptiveQuestion::getTopic));
        
        Map<String, Double> topicScores = new HashMap<>();
        for (Map.Entry<String, List<AdaptiveQuestion>> entry : questionsByTopic.entrySet()) {
            String topic = entry.getKey();
            List<AdaptiveQuestion> topicQuestions = entry.getValue();
            
            long topicCorrectCount = topicQuestions.stream()
                    .filter(q -> q.getIsCorrect() != null && q.getIsCorrect())
                    .count();
            
            double topicScore = topicQuestions.size() > 0 ? 
                    ((double) topicCorrectCount / topicQuestions.size()) * 100 : 0;
            
            topicScores.put(topic, topicScore);
        }
        
        // 使用AI生成分析报告
        Map<String, Object> analysisResult;
        try {
            analysisResult = analyzeResults(exam, questions, topicScores);
        } catch (Exception e) {
            log.error("AI分析失败，使用默认分析结果: {}", e.getMessage());
            // 创建默认分析结果
            analysisResult = createDefaultAnalysisResult(exam, questions, topicScores);
        }
        
        // 保存测试结果
        AdaptiveExamResult result = new AdaptiveExamResult();
        result.setExamId(examId);
        result.setUserId(exam.getUserId());
        result.setScore(score);
        result.setAbilityLevel(((Number) analysisResult.get("abilityScore")).doubleValue());
        result.setTotalQuestions(totalQuestions);
        result.setCorrectCount((int) correctCount);
        
        try {
            // 将Map和List转换为JSON字符串
            result.setTopicScores(objectMapper.writeValueAsString(topicScores));
            
            @SuppressWarnings("unchecked")
            List<String> strengths = (List<String>) analysisResult.get("strengths");
            result.setStrengths(objectMapper.writeValueAsString(strengths));
            
            @SuppressWarnings("unchecked")
            List<String> weaknesses = (List<String>) analysisResult.get("weaknesses");
            result.setWeaknesses(objectMapper.writeValueAsString(weaknesses));
        } catch (JsonProcessingException e) {
            log.error("转换JSON失败", e);
            throw new RuntimeException("系统错误");
        }
        
        result.setAnalysis((String) analysisResult.get("analysis"));
        result.setRecommendations((String) analysisResult.get("recommendations"));
        result.setCreatedAt(LocalDateTime.now());
        
        // 更新测试状态
        exam.setStatus("COMPLETED");
        exam.setEndTime(LocalDateTime.now());
        exam.setScore(score);
        exam.setAbilityLevel(((Number) analysisResult.get("abilityScore")).doubleValue());
        
        // 先保存结果再更新测试状态，确保结果存在
        try {
            adaptiveExamMapper.insertResult(result);
            adaptiveExamMapper.updateExam(exam);
        } catch (Exception e) {
            log.error("保存测试结果失败: {}", e.getMessage());
            throw new RuntimeException("保存测试结果失败: " + e.getMessage());
        }
        
        // 返回结果摘要
        Map<String, Object> resultSummary = new HashMap<>();
        resultSummary.put("examId", examId);
        resultSummary.put("score", score);
        resultSummary.put("abilityLevel", analysisResult.get("abilityLevel"));
        resultSummary.put("correctCount", correctCount);
        resultSummary.put("totalQuestions", totalQuestions);
        resultSummary.put("topicScores", topicScores);
        resultSummary.put("strengths", analysisResult.get("strengths"));
        resultSummary.put("weaknesses", analysisResult.get("weaknesses"));
        
        return resultSummary;
    }

    @Override
    public List<AdaptiveExamDTO> getExamHistoryByUserId(Long userId) {
        List<AdaptiveExam> exams = adaptiveExamMapper.findByUserId(userId);
        return exams.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getExamResult(Long examId) {
        AdaptiveExamResult result = adaptiveExamMapper.findResultByExamId(examId);
        if (result == null) {
            // 检查测试是否存在
            AdaptiveExam exam = adaptiveExamMapper.findById(examId);
            if (exam == null) {
                throw new RuntimeException("测试不存在");
            }
            
            // 如果测试存在但结果不存在，尝试重新生成结果
            if ("COMPLETED".equals(exam.getStatus())) {
                log.warn("测试已完成但结果不存在，尝试重新生成结果: examId={}", examId);
                return completeExam(examId);
            } else {
                throw new RuntimeException("测试结果不存在，测试可能尚未完成");
            }
        }
        
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("examId", result.getExamId());
        resultMap.put("score", result.getScore());
        resultMap.put("abilityLevel", result.getAbilityLevel());
        resultMap.put("totalQuestions", result.getTotalQuestions());
        resultMap.put("correctCount", result.getCorrectCount());
        
        try {
            // 从JSON字符串解析Map和List
            TypeReference<Map<String, Double>> topicScoresType = new TypeReference<>() {};
            resultMap.put("topicScores", objectMapper.readValue(result.getTopicScores(), topicScoresType));
            
            TypeReference<List<String>> stringListType = new TypeReference<>() {};
            resultMap.put("strengths", objectMapper.readValue(result.getStrengths(), stringListType));
            resultMap.put("weaknesses", objectMapper.readValue(result.getWeaknesses(), stringListType));
        } catch (JsonProcessingException e) {
            log.error("解析JSON失败", e);
            throw new RuntimeException("系统错误");
        }
        
        resultMap.put("analysis", result.getAnalysis());
        resultMap.put("recommendations", result.getRecommendations());
        
        return resultMap;
    }

    @Override
    public List<QuestionDTO> getExamQuestions(Long examId) {
        // 获取测试题目列表
        List<AdaptiveQuestion> questions = adaptiveExamMapper.findQuestionsByExamId(examId);
        
        // 转换为DTO
        return questions.stream()
                .map(this::convertToQuestionDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据答题情况调整难度
     */
    private void adjustDifficulty(AdaptiveExam exam, boolean isCorrect) {
        String currentDifficulty = exam.getDifficulty();
        
        if (isCorrect) {
            // 答对了，提高难度
            switch (currentDifficulty) {
                case "easy":
                    exam.setDifficulty("medium");
                    break;
                case "medium":
                    exam.setDifficulty("hard");
                    break;
                default:
                    // 已经是最高难度，保持不变
            }
        } else {
            // 答错了，降低难度
            switch (currentDifficulty) {
                case "hard":
                    exam.setDifficulty("medium");
                    break;
                case "medium":
                    exam.setDifficulty("easy");
                    break;
                default:
                    // 已经是最低难度，保持不变
            }
        }
    }
    
    /**
     * 生成题目
     */
    private AdaptiveQuestion generateQuestion(AdaptiveExam exam) {
        // 获取题目主题
        List<String> topicList = Arrays.asList(exam.getTopics().split(","));
        String selectedTopic = topicList.get(new Random().nextInt(topicList.size()));
        
        // 选择题目类型
        String[] questionTypes = {"SINGLE_CHOICE", "MULTI_CHOICE", "SHORT_ANSWER"};
        String questionType = questionTypes[new Random().nextInt(questionTypes.length)];
        
        // 构建QGRequest对象
        QGRequest qgRequest = new QGRequest();
        qgRequest.setCount(1); // 只生成一道题
        qgRequest.setSubject(exam.getSubject());
        qgRequest.setKnowledgePoints(Collections.singletonList(selectedTopic));
        
        // 设置难度
        switch (exam.getDifficulty()) {
            case "easy":
                qgRequest.setLevel(DifficultyLevel.EASY);
                break;
            case "hard":
                qgRequest.setLevel(DifficultyLevel.HARD);
                break;
            default:
                qgRequest.setLevel(DifficultyLevel.MEDIUM);
        }
        
        // 设置题型
        QuestionType aiQuestionType;
        switch (questionType) {
            case "SINGLE_CHOICE":
                aiQuestionType = QuestionType.CHOICE;
                break;
            case "MULTI_CHOICE":
                aiQuestionType = QuestionType.CHOICE; // 多选题也使用选择题类型
                break;
            case "SHORT_ANSWER":
                aiQuestionType = QuestionType.SHORT;
                break;
            default:
                aiQuestionType = QuestionType.CHOICE;
        }
        qgRequest.setType(aiQuestionType);
        
        try {
            // 同步调用QuestionAiService生成题目
            String questionJson = questionAiService.generate(qgRequest)
                .blockFirst(); // 阻塞获取第一个结果
            
            if (questionJson == null) {
                throw new RuntimeException("题目生成失败，返回为空");
            }
            
            // 解析JSON
            Map<String, Object> questionMap = objectMapper.readValue(questionJson, Map.class);
            
            // 创建题目实体
            AdaptiveQuestion question = new AdaptiveQuestion();
            question.setExamId(exam.getId());
            question.setQuestionType(questionType);
            
            // 获取题目内容，兼容不同的字段名
            String content = null;
            if (questionMap.containsKey("question")) {
                content = (String) questionMap.get("question");
            } else if (questionMap.containsKey("content")) {
                content = (String) questionMap.get("content");
            }
            
            if (content == null) {
                // 如果内容为空，使用默认内容
                content = "题目内容生成失败，请重新尝试";
                log.warn("题目内容为空，使用默认内容");
            }
            question.setContent(content);
            
            // 处理选项
            List<Map<String, Object>> formattedOptions = new ArrayList<>();
            if (aiQuestionType == QuestionType.CHOICE) {
                // 选择题才需要处理选项
                @SuppressWarnings("unchecked")
                List<String> options = (List<String>) questionMap.get("options");
                
                if (options != null) {
                    for (int i = 0; i < options.size(); i++) {
                        Map<String, Object> option = new HashMap<>();
                        option.put("id", String.valueOf((char)('A' + i)));
                        option.put("text", options.get(i));
                        formattedOptions.add(option);
                    }
                }
            }
            
            question.setOptions(objectMapper.writeValueAsString(formattedOptions));
            // 处理答案
            String answer = (String) questionMap.get("answer");
            if (answer == null) {
                // 如果答案为空，使用默认答案
                answer = "答案生成失败";
                log.warn("题目答案为空，使用默认答案");
            }
            question.setAnswer(answer);
            question.setTopic(selectedTopic);
            
            // 设置难度
            int difficulty;
            switch (exam.getDifficulty()) {
                case "easy":
                    difficulty = 1;
                    break;
                case "hard":
                    difficulty = 5;
                    break;
                default:
                    difficulty = 3;
            }
            question.setDifficulty(difficulty);
            
            // 根据题目难度设置分值
            question.setScore(calculateScore(question.getDifficulty()));
            question.setCreatedAt(LocalDateTime.now());
            
            return question;
        } catch (Exception e) {
            log.error("生成题目失败", e);
            
            // 如果使用QuestionAiService失败，回退到原来的方法
            log.info("回退到原始题目生成方法");
            return fallbackGenerateQuestion(exam, selectedTopic, questionType);
        }
    }
    
    /**
     * 回退的题目生成方法，使用原来的实现
     */
    private AdaptiveQuestion fallbackGenerateQuestion(AdaptiveExam exam, String selectedTopic, String questionType) {
        // 构建提示模板参数
        Map<String, Object> params = new HashMap<>();
        params.put("subject", exam.getSubject());
        params.put("topic", selectedTopic);
        params.put("difficulty", exam.getDifficulty());
        params.put("questionType", questionType);
        
        // 使用Spring AI的最新API调用方式
        PromptTemplate promptTemplate = new PromptTemplate(QUESTION_GENERATION_PROMPT_TEMPLATE);
        String renderedPrompt = promptTemplate.render(params);
        SystemMessage systemMessage = new SystemMessage(renderedPrompt);
        
        try {
            // 使用ChatClient调用AI服务
            String questionJson = chatClient.prompt()
                .messages(systemMessage)
                .call()
                .content();
            
            // 解析JSON
            Map<String, Object> questionMap = objectMapper.readValue(questionJson, Map.class);
            
            // 创建题目实体
            AdaptiveQuestion question = new AdaptiveQuestion();
            question.setExamId(exam.getId());
            question.setQuestionType((String) questionMap.get("questionType"));
            question.setContent((String) questionMap.get("content"));
            question.setOptions(objectMapper.writeValueAsString(questionMap.get("options")));
            question.setAnswer((String) questionMap.get("answer"));
            question.setTopic((String) questionMap.get("topic"));
            question.setDifficulty(((Number) questionMap.get("difficulty")).intValue());
            
            // 根据题目难度设置分值
            question.setScore(calculateScore(question.getDifficulty()));
            question.setCreatedAt(LocalDateTime.now());
            
            return question;
        } catch (Exception e) {
            log.error("回退生成题目失败", e);
            throw new RuntimeException("生成题目失败: " + e.getMessage());
        }
    }
    
    /**
     * 评估答案
     */
    private Map<String, Object> evaluateAnswer(AdaptiveQuestion question) {
        try {
            // 处理判断题的特殊情况
            if ("TRUE_FALSE".equals(question.getQuestionType())) {
                return evaluateTrueFalseAnswer(question);
            }
            
            // 处理填空题和选择题的特殊情况
            if ("FILL_BLANK".equals(question.getQuestionType()) || 
                "SINGLE_CHOICE".equals(question.getQuestionType()) ||
                "MULTI_CHOICE".equals(question.getQuestionType())) {
                return evaluateExactMatchAnswer(question);
            }
            
            // 构建提示模板参数
            Map<String, Object> params = new HashMap<>();
            params.put("questionType", question.getQuestionType());
            params.put("questionContent", question.getContent() != null ? question.getContent() : "");
            params.put("correctAnswer", question.getAnswer() != null ? question.getAnswer() : "");
            params.put("userAnswer", question.getUserAnswer() != null ? question.getUserAnswer() : "");
            
            // 使用备用方法：手动构建提示内容，避免模板渲染问题
            String renderedPrompt = "你是一个教育测试评估专家AI系统。请评估学生对以下题目的回答是否正确。\n\n" +
                    "题目信息:\n" +
                    "题目类型: " + question.getQuestionType() + "\n" +
                    "题目内容: " + (question.getContent() != null ? question.getContent() : "") + "\n" +
                    "正确答案: " + (question.getAnswer() != null ? question.getAnswer() : "") + "\n\n" +
                    "学生答案: " + (question.getUserAnswer() != null ? question.getUserAnswer() : "") + "\n\n" +
                    "请根据以下标准进行评估:\n" +
                    "1. 对于选择题，答案必须完全匹配才算正确\n" +
                    "2. 对于简答题，评估答案是否包含关键概念，是否正确理解了问题\n" +
                    "3. 给出评分和解析\n\n" +
                    "请使用以下JSON格式返回：\n" +
                    "```json\n" +
                    "{\n" +
                    "  \"isCorrect\": true/false,\n" +
                    "  \"score\": 分数(0-100之间的百分比),\n" +
                    "  \"feedback\": \"评价反馈\",\n" +
                    "  \"correctExplanation\": \"正确答案的详细解释\"\n" +
                    "}\n" +
                    "```\n\n" +
                    "只返回JSON格式的内容，不要包含其他文字说明。";
            
            SystemMessage systemMessage = new SystemMessage(renderedPrompt);
            
            // 使用ChatClient调用AI服务
            String evaluationJson;
            try {
                evaluationJson = chatClient.prompt()
                    .messages(systemMessage)
                    .call()
                    .content();
            } catch (Exception e) {
                log.error("AI服务调用失败: {}", e.getMessage());
                // AI服务调用失败时返回默认评估结果
                return createDefaultEvaluationResult(question);
            }
            
            if (evaluationJson == null || evaluationJson.isEmpty()) {
                log.error("AI返回空结果");
                return createDefaultEvaluationResult(question);
            }
            
            // 清理和解析JSON
            try {
                String cleanedJson = cleanJsonString(evaluationJson);
                return objectMapper.readValue(cleanedJson, Map.class);
            } catch (Exception e1) {
                log.error("JSON解析失败，尝试修复: {}", e1.getMessage());
                try {
                    // 尝试进一步修复和解析JSON
                    String fixedJson = fixJsonString(evaluationJson);
                    return objectMapper.readValue(fixedJson, Map.class);
                } catch (Exception e2) {
                    log.error("修复后JSON解析仍然失败: {}", e2.getMessage());
                    return createDefaultEvaluationResult(question);
                }
            }
        } catch (Exception e) {
            log.error("评估答案失败: {}", e.getMessage());
            return createDefaultEvaluationResult(question);
        }
    }
    
    /**
     * 创建默认评估结果
     */
    private Map<String, Object> createDefaultEvaluationResult(AdaptiveQuestion question) {
        Map<String, Object> defaultResult = new HashMap<>();
        defaultResult.put("isCorrect", false);
        defaultResult.put("score", 0);
        defaultResult.put("feedback", "系统无法评估答案，请联系管理员");
        defaultResult.put("correctExplanation", "正确答案是: " + (question.getAnswer() != null ? question.getAnswer() : "未提供"));
        return defaultResult;
    }
    
    /**
     * 评估精确匹配的答案（填空题、选择题）
     */
    private Map<String, Object> evaluateExactMatchAnswer(AdaptiveQuestion question) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 安全检查
            if (question.getAnswer() == null) {
                log.error("正确答案为空");
                return createDefaultEvaluationResult(question);
            }
            
            String correctAnswer = question.getAnswer().trim();
            String userAnswer = question.getUserAnswer() == null ? "" : question.getUserAnswer().trim();
            
            boolean isCorrect = false;
            
            // 根据题目类型进行不同的匹配策略
            if ("SINGLE_CHOICE".equals(question.getQuestionType()) || 
                "MULTI_CHOICE".equals(question.getQuestionType())) {
                
                // 选择题答案可能是逗号分隔的选项ID列表
                // 将两个答案标准化：排序并移除空格
                String[] correctOptions = correctAnswer.split(",");
                String[] userOptions = userAnswer.split(",");
                
                // 排序并转换为集合进行比较
                Arrays.sort(correctOptions);
                Arrays.sort(userOptions);
                
                // 忽略大小写比较
                isCorrect = Arrays.toString(correctOptions).equalsIgnoreCase(Arrays.toString(userOptions));
                
            } else if ("FILL_BLANK".equals(question.getQuestionType())) {
                // 填空题匹配可以更宽松，忽略大小写和空白
                isCorrect = correctAnswer.equalsIgnoreCase(userAnswer);
            } else {
                // 默认匹配策略
                isCorrect = correctAnswer.equalsIgnoreCase(userAnswer);
            }
            
            // 构建结果
            result.put("isCorrect", isCorrect);
            result.put("score", isCorrect ? 100.0 : 0.0);
            result.put("feedback", isCorrect ? "回答正确" : "回答错误");
            result.put("correctExplanation", "正确答案是: " + question.getAnswer());
            
            return result;
        } catch (Exception e) {
            log.error("评估精确匹配答案失败: {}", e.getMessage());
            return createDefaultEvaluationResult(question);
        }
    }
    
    /**
     * 标准化判断题答案
     */
    private String standardizeTrueFalseAnswer(String answer) {
        if (answer == null) {
            return "";
        }
        
        try {
            String upperAnswer = answer.toUpperCase().trim();
            
            // 正确答案的各种可能形式
            if (upperAnswer.equals("TRUE") || upperAnswer.equals("T") || 
                upperAnswer.equals("YES") || upperAnswer.equals("Y") || 
                upperAnswer.equals("1") || upperAnswer.equals("正确") ||
                upperAnswer.equals("\"TRUE\"") || upperAnswer.equals("\"T\"") ||
                upperAnswer.equals("\"YES\"") || upperAnswer.equals("\"Y\"") ||
                upperAnswer.equals("\"1\"") || upperAnswer.equals("\"正确\"")) {
                return "TRUE";
            } 
            // 错误答案的各种可能形式
            else if (upperAnswer.equals("FALSE") || upperAnswer.equals("F") || 
                    upperAnswer.equals("NO") || upperAnswer.equals("N") || 
                    upperAnswer.equals("0") || upperAnswer.equals("错误") ||
                    upperAnswer.equals("\"FALSE\"") || upperAnswer.equals("\"F\"") ||
                    upperAnswer.equals("\"NO\"") || upperAnswer.equals("\"N\"") ||
                    upperAnswer.equals("\"0\"") || upperAnswer.equals("\"错误\"")) {
                return "FALSE";
            }
            
            // 如果答案包含"正确"、"对"、"是"等关键词，认为是TRUE
            if (upperAnswer.contains("正确") || upperAnswer.contains("对") || 
                upperAnswer.contains("是") || upperAnswer.contains("TRUE") || 
                upperAnswer.contains("YES")) {
                return "TRUE";
            }
            
            // 如果答案包含"错误"、"错"、"否"等关键词，认为是FALSE
            if (upperAnswer.contains("错误") || upperAnswer.contains("错") || 
                upperAnswer.contains("否") || upperAnswer.contains("不") || 
                upperAnswer.contains("FALSE") || upperAnswer.contains("NO")) {
                return "FALSE";
            }
            
            // 默认情况返回原答案大写形式
            return upperAnswer;
        } catch (Exception e) {
            log.error("标准化判断题答案失败: {}", e.getMessage());
            return "";
        }
    }
    
    /**
     * 分析测试结果
     */
    private Map<String, Object> analyzeResults(AdaptiveExam exam, List<AdaptiveQuestion> questions, 
                                           Map<String, Double> topicScores) {
        // 构建提示模板参数
        Map<String, Object> params = new HashMap<>();
        params.put("subject", exam.getSubject());
        params.put("topics", exam.getTopics());
        params.put("questionCount", questions.size());
        
        long correctCount = questions.stream()
                .filter(q -> q.getIsCorrect() != null && q.getIsCorrect())
                .count();
        
        double score = questions.size() > 0 ? 
                ((double) correctCount / questions.size()) * 100 : 0;
        
        params.put("correctCount", correctCount);
        params.put("score", score);
        params.put("topicScores", topicScores);
        
        // 使用Spring AI的最新API调用方式
        PromptTemplate promptTemplate = new PromptTemplate(RESULT_ANALYSIS_PROMPT_TEMPLATE);
        String renderedPrompt = promptTemplate.render(params);
        SystemMessage systemMessage = new SystemMessage(renderedPrompt);
        
        try {
            // 使用ChatClient调用AI服务
            String analysisJson = chatClient.prompt()
                .messages(systemMessage)
                .call()
                .content();
            
            // 解析JSON
            return objectMapper.readValue(analysisJson, Map.class);
        } catch (Exception e) {
            log.error("分析结果失败", e);
            throw new RuntimeException("分析结果失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据难度计算分值
     */
    private double calculateScore(int difficulty) {
        // 1-5的难度对应的分值
        switch (difficulty) {
            case 1: return 2.0;
            case 2: return 4.0;
            case 3: return 6.0;
            case 4: return 8.0;
            case 5: return 10.0;
            default: return 5.0;
        }
    }
    
    /**
     * 清理JSON字符串
     */
    private String cleanJsonString(String jsonString) {
        if (jsonString == null) {
            return "{}";
        }
        
        // 删除可能的Markdown代码块标记
        String cleaned = jsonString.replaceAll("```json", "").replaceAll("```", "");
        
        // 删除开头和结尾的非JSON字符
        int startIndex = cleaned.indexOf('{');
        int endIndex = cleaned.lastIndexOf('}');
        
        if (startIndex >= 0 && endIndex >= 0 && endIndex > startIndex) {
            cleaned = cleaned.substring(startIndex, endIndex + 1);
        }
        
        return cleaned;
    }
    
    /**
     * 修复JSON字符串
     */
    private String fixJsonString(String jsonString) {
        if (jsonString == null) {
            return "{}";
        }
        
        try {
            // 删除可能的Markdown代码块标记，这是首要步骤
            String cleaned = jsonString.replaceAll("```json", "").replaceAll("```", "");
            
            // 删除开头和结尾的非JSON字符
            int startIndex = cleaned.indexOf('{');
            int endIndex = cleaned.lastIndexOf('}');
            
            if (startIndex >= 0 && endIndex >= 0 && endIndex > startIndex) {
                cleaned = cleaned.substring(startIndex, endIndex + 1);
            }
            
            // 替换常见的JSON格式错误
            String fixed = cleaned
                .replaceAll("'", "\"")                       // 单引号替换为双引号
                .replaceAll("(\\w+)\\s*:", "\"$1\":")        // 将无引号的键加上双引号
                .replaceAll(":\\s*([a-zA-Z][a-zA-Z0-9]*)", ": \"$1\"")  // 将无引号的字符串值加上双引号
                .replaceAll(":\\s*true/false", ": false")    // 修复true/false语法
                .replaceAll(":\\s*\\d+[-]\\d+", ": 0")       // 修复数字范围语法
                .replaceAll(":\\s*分数\\([^)]*\\)", ": 0")   // 修复分数说明
                .replaceAll("\"isCorrect\":\\s*true/false", "\"isCorrect\": false") // 特殊处理isCorrect字段
                .replaceAll("\"score\":\\s*分数\\([^)]*\\)", "\"score\": 0"); // 特殊处理score字段
            
            // 确保是有效的JSON对象
            if (!fixed.startsWith("{")) {
                fixed = "{" + fixed;
            }
            if (!fixed.endsWith("}")) {
                fixed = fixed + "}";
            }
            
            // 尝试修复可能缺失的引号
            if (!isValidJson(fixed)) {
                fixed = fixed.replaceAll("([^\\s\"]+)\\s*:", "\"$1\":");  // 更激进地处理键
                fixed = fixed.replaceAll(":([^\\s\"{}\\[\\],0-9]+)", ": \"$1\"");  // 更激进地处理值
            }
            
            return fixed;
        } catch (Exception e) {
            log.error("JSON修复过程中出错: {}", e.getMessage());
            return "{}";
        }
    }
    
    /**
     * 检查字符串是否为有效JSON
     */
    private boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 评估判断题答案
     */
    private Map<String, Object> evaluateTrueFalseAnswer(AdaptiveQuestion question) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 确保答案和用户输入不为空
            if (question.getAnswer() == null) {
                log.error("判断题正确答案为空");
                return createDefaultEvaluationResult(question);
            }
            
            // 标准化答案和用户输入
            String correctAnswer = standardizeTrueFalseAnswer(question.getAnswer());
            String userAnswer = standardizeTrueFalseAnswer(question.getUserAnswer());
            
            // 比较答案
            boolean isCorrect = correctAnswer.equals(userAnswer);
            
            // 构建结果
            result.put("isCorrect", isCorrect);
            result.put("score", isCorrect ? 100.0 : 0.0);
            result.put("feedback", isCorrect ? "回答正确" : "回答错误");
            
            // 设置解释
            String displayAnswer = "TRUE".equals(correctAnswer) ? "正确" : "错误";
            result.put("correctExplanation", "正确答案是: " + displayAnswer);
            
            return result;
        } catch (Exception e) {
            log.error("评估判断题答案失败: {}", e.getMessage());
            return createDefaultEvaluationResult(question);
        }
    }
    
    /**
     * 将实体转换为DTO
     */
    private AdaptiveExamDTO convertToDTO(AdaptiveExam exam) {
        AdaptiveExamDTO dto = new AdaptiveExamDTO();
        BeanUtils.copyProperties(exam, dto);
        
        // 处理知识点数组
        if (exam.getTopics() != null && !exam.getTopics().isEmpty()) {
            dto.setTopics(exam.getTopics().split(","));
        } else {
            dto.setTopics(new String[0]);
        }
        
        return dto;
    }
    
    /**
     * 将题目实体转换为DTO
     */
    private QuestionDTO convertToQuestionDTO(AdaptiveQuestion question) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setQuestionType(question.getQuestionType());
        dto.setContent(question.getContent());
        dto.setTopic(question.getTopic());
        dto.setDifficulty(question.getDifficulty());
        dto.setScore(question.getScore());
        
        // 设置建议时间，根据难度调整
        dto.setTimeLimit(question.getDifficulty() * 30); // 每难度等级30秒
        
        // 根据题目类型，可以提供提示
        dto.setShowHint("SHORT_ANSWER".equals(question.getQuestionType()));
        
        try {
            // 解析选项
            if (question.getOptions() != null && !question.getOptions().isEmpty()) {
                dto.setOptions(objectMapper.readValue(question.getOptions(), List.class));
            }
        } catch (JsonProcessingException e) {
            log.error("解析选项失败", e);
            throw new RuntimeException("系统错误");
        }
        
        return dto;
    }
    
    /**
     * 创建默认分析结果（当AI分析失败时使用）
     */
    private Map<String, Object> createDefaultAnalysisResult(AdaptiveExam exam, List<AdaptiveQuestion> questions, 
                                                      Map<String, Double> topicScores) {
        Map<String, Object> result = new HashMap<>();
        
        // 计算得分
        int totalQuestions = questions.size();
        long correctCount = questions.stream()
                .filter(q -> q.getIsCorrect() != null && q.getIsCorrect())
                .count();
        
        double score = totalQuestions > 0 ? 
                ((double) correctCount / totalQuestions) * 100 : 0;
        
        // 能力水平
        String abilityLevel;
        double abilityScore = score;
        if (score >= 90) {
            abilityLevel = "专家级";
        } else if (score >= 80) {
            abilityLevel = "进阶级";
        } else if (score >= 70) {
            abilityLevel = "中级";
        } else if (score >= 60) {
            abilityLevel = "基础级";
        } else {
            abilityLevel = "入门级";
        }
        
        // 按分数排序知识点
        List<Map.Entry<String, Double>> sortedTopics = new ArrayList<>(topicScores.entrySet());
        sortedTopics.sort(Map.Entry.<String, Double>comparingByValue().reversed());
        
        // 提取优势和弱势知识点
        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        
        int topicCount = sortedTopics.size();
        for (int i = 0; i < Math.min(3, topicCount); i++) {
            strengths.add(sortedTopics.get(i).getKey());
        }
        
        for (int i = Math.max(0, topicCount - 3); i < topicCount; i++) {
            weaknesses.add(sortedTopics.get(i).getKey());
        }
        
        // 生成简单分析
        String analysis = String.format(
            "您在%s测试中的总体表现为%s水平，得分为%.1f分。您在%d道题目中答对了%d道，正确率为%.1f%%。",
            exam.getSubject(), abilityLevel, score, totalQuestions, correctCount, score
        );
        
        // 生成简单建议
        String recommendations = "建议您针对弱势知识点进行重点复习，可以参考相关学习资源和练习题，提高这些领域的掌握程度。";
        
        // 组装结果
        result.put("abilityLevel", abilityLevel);
        result.put("abilityScore", abilityScore);
        result.put("strengths", strengths);
        result.put("weaknesses", weaknesses);
        result.put("analysis", analysis);
        result.put("recommendations", recommendations);
        
        return result;
    }
} 