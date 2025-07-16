package org.example.backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.entity.dto.GeneratedQuestionDTO;
import org.example.backend.entity.dto.QuestionGenerationRequest;
import org.example.backend.entity.pojo.Question;
import org.example.backend.mapper.QuestionMapper;
import org.example.backend.service.QuestionBankService;
import org.example.backend.service.impl.ai.QuestionGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * 题库管理服务实现
 */
@Slf4j
@Service
public class QuestionBankServiceImpl implements QuestionBankService {

    @Autowired
    private QuestionGenerationService questionGenerationService;

    @Autowired
    private QuestionMapper questionMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Flux<String> generateQuestions(QuestionGenerationRequest request) {
        log.info("开始生成题目，用户：{}，知识点：{}", request.getCreatedBy(), request.getTopic());
        return questionGenerationService.generateQuestions(request);
    }

    @Override
    public List<GeneratedQuestionDTO> parseAndSaveQuestions(String aiResponse, QuestionGenerationRequest request) {
        return questionGenerationService.parseAndSaveQuestions(aiResponse, request);
    }

    @Override
    public List<Question> getAllQuestions() {
        try {
            return questionMapper.listAll();
        } catch (Exception e) {
            log.error("获取所有题目失败：{}", e.getMessage(), e);
            throw new RuntimeException("获取题目列表失败");
        }
    }

    @Override
    public List<Question> getQuestionsByBatchId(String batchId) {
        try {
            return questionMapper.findQuestionById(batchId);
        } catch (Exception e) {
            log.error("根据批次ID获取题目失败：{}", e.getMessage(), e);
            throw new RuntimeException("获取批次题目失败");
        }
    }

    @Override
    public List<Question> getQuestionsByType(String questionType) {
        try {
            return questionMapper.findQuestionsByType(questionType);
        } catch (Exception e) {
            log.error("根据题型获取题目失败：{}", e.getMessage(), e);
            throw new RuntimeException("获取题型题目失败");
        }
    }

    @Override
    public List<Question> getQuestionsByDifficulty(Integer difficulty) {
        try {
            return questionMapper.findQuestionsByDifficulty(difficulty);
        } catch (Exception e) {
            log.error("根据难度获取题目失败：{}", e.getMessage(), e);
            throw new RuntimeException("获取难度题目失败");
        }
    }

    @Override
    public boolean saveQuestionToBank(GeneratedQuestionDTO questionDTO, String createdBy) {
        try {
            Question question = convertDTOToEntity(questionDTO, createdBy);
            int result = questionMapper.insertQuestion(question);

            if (result > 0) {
                log.info("题目保存成功，ID：{}", question.getQuestionId());
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("保存题目失败：{}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean batchSaveQuestionsToBank(List<GeneratedQuestionDTO> questions, String createdBy) {
        try {
            int successCount = 0;
            for (GeneratedQuestionDTO questionDTO : questions) {
                if (saveQuestionToBank(questionDTO, createdBy)) {
                    successCount++;
                }
            }

            log.info("批量保存题目完成，成功：{}/{}道", successCount, questions.size());
            return successCount == questions.size();
        } catch (Exception e) {
            log.error("批量保存题目失败：{}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean deleteQuestion(String questionId) {
        try {
            int result = questionMapper.deleteQuestion(questionId);
            if (result > 0) {
                log.info("题目删除成功，ID：{}", questionId);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("删除题目失败：{}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean updateQuestion(Question question) {
        try {
            int result = questionMapper.updateQuestion(question);
            if (result > 0) {
                log.info("题目更新成功，ID：{}", question.getQuestionId());
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("更新题目失败：{}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Question getQuestionById(String questionId) {
        try {
            List<Question> questions = questionMapper.findQuestionById(questionId);
            return questions.isEmpty() ? null : questions.get(0);
        } catch (Exception e) {
            log.error("根据ID获取题目失败：{}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将DTO转换为实体
     */
    private Question convertDTOToEntity(GeneratedQuestionDTO dto, String createdBy) {
        Question question = new Question();
        question.setQuestionId(UUID.randomUUID().toString());
        question.setQuestionType(dto.getType());
        question.setContent(dto.getContent());
        question.setAnswer(dto.getAnswer());
        question.setAnalysis(dto.getAnalysis());
        question.setDifficulty(dto.getDifficulty());
        question.setScore(BigDecimal.valueOf(1.0));
        question.setCreatedBy(createdBy);
        question.setIsAiGenerated(true);

        // 处理选项
        if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
            try {
                question.setOptions(objectMapper.writeValueAsString(dto.getOptions()));
            } catch (Exception e) {
                log.error("选项序列化失败：{}", e.getMessage());
            }
        }

        return question;
    }
}
