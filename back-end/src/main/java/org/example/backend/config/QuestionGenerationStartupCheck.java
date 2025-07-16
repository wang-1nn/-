package org.example.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.example.backend.service.QuestionBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 题目生成模块启动检查
 */
@Slf4j
@Component
public class QuestionGenerationStartupCheck implements CommandLineRunner {

    @Autowired
    private QuestionBankService questionBankService;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== 题目生成模块启动检查 ===");
        
        try {
            // 检查服务是否正确注入
            if (questionBankService != null) {
                log.info("✅ QuestionBankService 注入成功");
            } else {
                log.error("❌ QuestionBankService 注入失败");
            }
            
            // 检查题库数据
            try {
                int questionCount = questionBankService.getAllQuestions().size();
                log.info("✅ 题库连接正常，当前题目数量: {}", questionCount);
            } catch (Exception e) {
                log.warn("⚠️ 题库连接异常: {}", e.getMessage());
            }
            
            log.info("=== 题目生成模块检查完成 ===");
            
        } catch (Exception e) {
            log.error("❌ 题目生成模块启动检查失败: {}", e.getMessage(), e);
        }
    }
}
