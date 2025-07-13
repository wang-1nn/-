/*
 * 学生答案表
 */
DROP TABLE IF EXISTS `answers`;
CREATE TABLE `answers` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '答案ID',
  `submission_id` bigint NOT NULL COMMENT '提交记录ID',
  `question_id` varchar(50) NOT NULL COMMENT '题目ID',
  `user_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '用户答案',
  `score` decimal(5, 2) DEFAULT NULL COMMENT '得分',
  `is_correct` tinyint(1) DEFAULT NULL COMMENT '是否正确',
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '评语',
  `answered_at` datetime DEFAULT NULL COMMENT '作答时间',
  `graded_by` varchar(50) DEFAULT NULL COMMENT '批改人ID',
  `graded_at` datetime DEFAULT NULL COMMENT '批改时间',
  PRIMARY KEY (`id`),
  KEY `idx_submission_id` (`submission_id`),
  KEY `idx_question_id` (`question_id`),
  CONSTRAINT `fk_answer_submission` FOREIGN KEY (`submission_id`) REFERENCES `submissions` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学生答案表';

/*
 * 考试提交记录表
 */
DROP TABLE IF EXISTS `submissions`;
CREATE TABLE `submissions` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '提交记录ID',
  `exam_id` varchar(50) NOT NULL COMMENT '考试ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `status` tinyint NOT NULL COMMENT '状态：0-进行中，1-已提交未批改，2-已批改',
  `submitted_at` datetime DEFAULT NULL COMMENT '提交时间',
  `total_score` decimal(5, 2) DEFAULT NULL COMMENT '总分',
  PRIMARY KEY (`id`),
  KEY `idx_exam_student` (`exam_id`, `student_id`),
  KEY `idx_student_id` (`student_id`),
  CONSTRAINT `fk_submission_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='考试提交记录表';

-- 添加学生答案
-- 高等数学期中考试答案
INSERT INTO `answers` (`id`, `submission_id`, `question_id`, `user_answer`, `score`, `is_correct`, `comment`, `answered_at`, `graded_by`, `graded_at`) VALUES
(701, 601, 'q1001', '1', NULL, NULL, NULL, '2023-10-25 14:15:00', NULL, NULL),
(702, 601, 'q1002', '1', NULL, NULL, NULL, '2023-10-25 14:25:00', NULL, NULL),
(703, 601, 'q1003', '1', NULL, NULL, NULL, '2023-10-25 14:35:00', NULL, NULL),
(704, 601, 'q1004', '1', NULL, NULL, NULL, '2023-10-25 14:45:00', NULL, NULL),
(705, 601, 'q1005', '0', NULL, NULL, NULL, '2023-10-25 15:00:00', NULL, NULL); 