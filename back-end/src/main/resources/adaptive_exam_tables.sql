-- 自适应测试表
CREATE TABLE IF NOT EXISTS `adaptive_exams` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '测试ID',
  `user_id` bigint NOT NULL COMMENT '学生ID',
  `subject` varchar(100) NOT NULL COMMENT '测试科目',
  `difficulty` varchar(20) NOT NULL DEFAULT 'medium' COMMENT '当前难度水平',
  `starting_difficulty` varchar(20) NOT NULL DEFAULT 'medium' COMMENT '起始难度',
  `question_count` int NOT NULL DEFAULT 20 COMMENT '题目总数',
  `current_question` int NOT NULL DEFAULT 0 COMMENT '当前题目序号',
  `time_limit` int NOT NULL DEFAULT 45 COMMENT '时间限制(分钟)',
  `topics` text COMMENT '知识点，以逗号分隔',
  `status` varchar(20) NOT NULL COMMENT '测试状态',
  `score` decimal(5,2) DEFAULT NULL COMMENT '最终得分',
  `ability_level` decimal(5,2) DEFAULT NULL COMMENT '能力水平评估值',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_adaptive_exam_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自适应智能测试表';

-- 自适应测试题目表
CREATE TABLE IF NOT EXISTS `adaptive_questions` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '题目ID',
  `exam_id` bigint NOT NULL COMMENT '测试ID',
  `question_type` varchar(20) NOT NULL COMMENT '题目类型',
  `content` text NOT NULL COMMENT '题目内容',
  `options` text COMMENT '选项，JSON格式',
  `answer` text NOT NULL COMMENT '正确答案',
  `user_answer` text COMMENT '用户答案',
  `topic` varchar(100) NOT NULL COMMENT '所属知识点',
  `difficulty` int NOT NULL COMMENT '题目难度(1-5)',
  `score` decimal(5,2) NOT NULL COMMENT '题目分值',
  `is_correct` tinyint(1) DEFAULT NULL COMMENT '是否答对',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `answered_at` datetime DEFAULT NULL COMMENT '作答时间',
  PRIMARY KEY (`id`),
  KEY `idx_exam_id` (`exam_id`),
  CONSTRAINT `fk_adaptive_question_exam` FOREIGN KEY (`exam_id`) REFERENCES `adaptive_exams` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自适应测试题目表';

-- 自适应测试结果表
CREATE TABLE IF NOT EXISTS `adaptive_exam_results` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '结果ID',
  `exam_id` bigint NOT NULL COMMENT '测试ID',
  `user_id` bigint NOT NULL COMMENT '学生ID',
  `score` decimal(5,2) NOT NULL COMMENT '最终得分',
  `ability_level` decimal(5,2) NOT NULL COMMENT '能力水平评估',
  `total_questions` int NOT NULL COMMENT '总题数',
  `correct_count` int NOT NULL COMMENT '答对题数',
  `topic_scores` json DEFAULT NULL COMMENT '各知识点得分情况',
  `strengths` json DEFAULT NULL COMMENT '优势知识点',
  `weaknesses` json DEFAULT NULL COMMENT '弱势知识点',
  `analysis` text COMMENT 'AI分析报告',
  `recommendations` text COMMENT '学习建议',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_exam_id` (`exam_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_adaptive_result_exam` FOREIGN KEY (`exam_id`) REFERENCES `adaptive_exams` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_adaptive_result_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自适应测试结果表'; 