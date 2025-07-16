/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80400 (8.4.0)
 Source Host           : localhost:3306
 Source Schema         : ruanjianbei

 Target Server Type    : MySQL
 Target Server Version : 80400 (8.4.0)
 File Encoding         : 65001

 Date: 16/07/2025 13:45:28
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for adaptive_exam_results
-- ----------------------------
DROP TABLE IF EXISTS `adaptive_exam_results`;
CREATE TABLE `adaptive_exam_results`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '结果ID',
  `exam_id` bigint NOT NULL COMMENT '测试ID',
  `user_id` bigint NOT NULL COMMENT '学生ID',
  `score` decimal(5, 2) NOT NULL COMMENT '最终得分',
  `ability_level` decimal(5, 2) NOT NULL COMMENT '能力水平评估',
  `total_questions` int NOT NULL COMMENT '总题数',
  `correct_count` int NOT NULL COMMENT '答对题数',
  `topic_scores` json NULL COMMENT '各知识点得分情况',
  `strengths` json NULL COMMENT '优势知识点',
  `weaknesses` json NULL COMMENT '弱势知识点',
  `analysis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'AI分析报告',
  `recommendations` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '学习建议',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_exam_id`(`exam_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_adaptive_result_exam` FOREIGN KEY (`exam_id`) REFERENCES `adaptive_exams` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_adaptive_result_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '自适应测试结果表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for adaptive_exams
-- ----------------------------
DROP TABLE IF EXISTS `adaptive_exams`;
CREATE TABLE `adaptive_exams`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '测试ID',
  `user_id` bigint NOT NULL COMMENT '学生ID',
  `subject` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '测试科目',
  `difficulty` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'medium' COMMENT '当前难度水平',
  `starting_difficulty` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'medium' COMMENT '起始难度',
  `question_count` int NOT NULL DEFAULT 20 COMMENT '题目总数',
  `current_question` int NOT NULL DEFAULT 0 COMMENT '当前题目序号',
  `time_limit` int NOT NULL DEFAULT 45 COMMENT '时间限制(分钟)',
  `topics` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '知识点，以逗号分隔',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '测试状态',
  `score` decimal(5, 2) NULL DEFAULT NULL COMMENT '最终得分',
  `ability_level` decimal(5, 2) NULL DEFAULT NULL COMMENT '能力水平评估值',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_adaptive_exam_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '自适应智能测试表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for adaptive_questions
-- ----------------------------
DROP TABLE IF EXISTS `adaptive_questions`;
CREATE TABLE `adaptive_questions`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '题目ID',
  `exam_id` bigint NOT NULL COMMENT '测试ID',
  `question_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题目类型',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题目内容',
  `options` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '选项，JSON格式',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '正确答案',
  `user_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '用户答案',
  `topic` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '所属知识点',
  `difficulty` int NOT NULL COMMENT '题目难度(1-5)',
  `score` decimal(5, 2) NOT NULL COMMENT '题目分值',
  `is_correct` tinyint(1) NULL DEFAULT NULL COMMENT '是否答对',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `answered_at` datetime NULL DEFAULT NULL COMMENT '作答时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_exam_id`(`exam_id` ASC) USING BTREE,
  CONSTRAINT `fk_adaptive_question_exam` FOREIGN KEY (`exam_id`) REFERENCES `adaptive_exams` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 197 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '自适应测试题目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for announcements
-- ----------------------------
DROP TABLE IF EXISTS `announcements`;
CREATE TABLE `announcements`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '公告内容',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'system' COMMENT '公告类型: system-系统公告, course-课程公告, urgent-紧急通知',
  `target_audience` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'all' COMMENT '目标受众: all-所有人, teachers-教师, students-学生, class-特定班级',
  `course_id` bigint NULL DEFAULT NULL COMMENT '关联课程ID',
  `publisher_id` bigint NOT NULL COMMENT '发布者ID',
  `is_important` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否重要',
  `publish_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '过期时间',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'draft' COMMENT '状态: draft-草稿, published-已发布, expired-已过期',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_publisher_status`(`publisher_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_publish_time`(`publish_time` ASC) USING BTREE,
  INDEX `fk_announcement_course`(`course_id` ASC) USING BTREE,
  CONSTRAINT `fk_announcement_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_announcement_publisher` FOREIGN KEY (`publisher_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for answers
-- ----------------------------
DROP TABLE IF EXISTS `answers`;
CREATE TABLE `answers`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '答案ID',
  `submission_id` bigint NOT NULL COMMENT '提交记录ID',
  `question_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题目ID',
  `user_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '用户答案',
  `score` decimal(5, 2) NULL DEFAULT NULL COMMENT '得分',
  `is_correct` tinyint(1) NULL DEFAULT NULL COMMENT '是否正确',
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评语',
  `answered_at` datetime NULL DEFAULT NULL COMMENT '作答时间',
  `graded_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '批改人ID',
  `graded_at` datetime NULL DEFAULT NULL COMMENT '批改时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_submission_id`(`submission_id` ASC) USING BTREE,
  INDEX `idx_question_id`(`question_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 758 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学生答案表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for assignment_submissions
-- ----------------------------
DROP TABLE IF EXISTS `assignment_submissions`;
CREATE TABLE `assignment_submissions`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `assignment_id` bigint NOT NULL COMMENT '作业ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '提交内容',
  `file_urls` json NULL COMMENT '附件文件URLs',
  `submit_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'draft' COMMENT '状态: draft-草稿, submitted-已提交, graded-已批改, returned-已退回',
  `grade` decimal(5, 2) NULL DEFAULT NULL COMMENT '得分',
  `feedback` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '教师反馈',
  `graded_at` datetime NULL DEFAULT NULL COMMENT '批改时间',
  `graded_by` bigint NULL DEFAULT NULL COMMENT '批改教师ID',
  `is_late` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否迟交',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_assignment_student`(`assignment_id` ASC, `student_id` ASC) USING BTREE,
  INDEX `idx_student_status`(`student_id` ASC, `status` ASC) USING BTREE,
  INDEX `fk_asub_grader`(`graded_by` ASC) USING BTREE,
  CONSTRAINT `fk_asub_assignment` FOREIGN KEY (`assignment_id`) REFERENCES `assignments` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_asub_grader` FOREIGN KEY (`graded_by`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_asub_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '作业提交表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for assignments
-- ----------------------------
DROP TABLE IF EXISTS `assignments`;
CREATE TABLE `assignments`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `teacher_id` bigint NOT NULL COMMENT '教师ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '作业标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '作业描述',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '作业内容',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'homework' COMMENT '作业类型: homework-家庭作业, quiz-小测验, project-项目, practice-练习',
  `total_score` decimal(5, 2) NOT NULL DEFAULT 100.00 COMMENT '总分',
  `due_date` datetime NOT NULL COMMENT '截止时间',
  `allow_late_submission` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否允许迟交',
  `late_penalty` decimal(3, 2) NOT NULL DEFAULT 0.00 COMMENT '迟交扣分比例',
  `auto_grade` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否自动批改',
  `is_published` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否发布',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_due_date`(`course_id` ASC, `due_date` ASC) USING BTREE,
  INDEX `idx_teacher_status`(`teacher_id` ASC, `is_published` ASC) USING BTREE,
  CONSTRAINT `fk_assignment_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_assignment_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '作业表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for chapter_contents
-- ----------------------------
DROP TABLE IF EXISTS `chapter_contents`;
CREATE TABLE `chapter_contents`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '内容ID',
  `chapter_id` bigint NOT NULL COMMENT '章节ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内容标题',
  `content_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内容类型：VIDEO-视频, DOCUMENT-文档, QUIZ-测验',
  `content_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '内容URL（视频或文档链接）',
  `content_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '内容文本（富文本内容）',
  `duration` int NULL DEFAULT NULL COMMENT '时长（秒，适用于视频）',
  `order_num` int NOT NULL DEFAULT 0 COMMENT '排序序号',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-不可见，1-可见',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_chapter_id`(`chapter_id` ASC) USING BTREE,
  INDEX `idx_order_num`(`order_num` ASC) USING BTREE,
  CONSTRAINT `fk_content_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `course_chapters` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '章节内容表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for chapter_ratings
-- ----------------------------
DROP TABLE IF EXISTS `chapter_ratings`;
CREATE TABLE `chapter_ratings`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `chapter_id` bigint NOT NULL COMMENT '章节ID',
  `rating` int NOT NULL COMMENT '评分(1-5)',
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评价内容',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_chapter`(`student_id` ASC, `chapter_id` ASC) USING BTREE,
  INDEX `idx_chapter_id`(`chapter_id` ASC) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  CONSTRAINT `fk_rating_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `course_chapters` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_rating_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_rating_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '章节评价表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for classes
-- ----------------------------
DROP TABLE IF EXISTS `classes`;
CREATE TABLE `classes`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '班级唯一ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '班级名称',
  `grade` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '年级',
  `major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '专业',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 204 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '班级信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for course_chapters
-- ----------------------------
DROP TABLE IF EXISTS `course_chapters`;
CREATE TABLE `course_chapters`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '章节ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '章节标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '章节描述',
  `order_num` int NOT NULL DEFAULT 0 COMMENT '排序序号',
  `duration` int NULL DEFAULT NULL COMMENT '预计学习时长（分钟）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-不可见，1-可见',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  INDEX `idx_order_num`(`order_num` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程章节表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for course_discussions
-- ----------------------------
DROP TABLE IF EXISTS `course_discussions`;
CREATE TABLE `course_discussions`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '讨论ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `user_id` bigint NOT NULL COMMENT '发起人ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '讨论标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '讨论内容',
  `publish_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `last_reply_time` datetime NULL DEFAULT NULL COMMENT '最后回复时间',
  `reply_count` int NOT NULL DEFAULT 0 COMMENT '回复数量',
  `view_count` int NOT NULL DEFAULT 0 COMMENT '浏览数量',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞数量',
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签，逗号分隔',
  `is_sticky` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否置顶',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_publish_time`(`publish_time` ASC) USING BTREE,
  INDEX `idx_last_reply_time`(`last_reply_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程讨论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for course_favorites
-- ----------------------------
DROP TABLE IF EXISTS `course_favorites`;
CREATE TABLE `course_favorites`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_course`(`user_id` ASC, `course_id` ASC) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  CONSTRAINT `fk_favorite_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_favorite_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程收藏表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for course_offerings
-- ----------------------------
DROP TABLE IF EXISTS `course_offerings`;
CREATE TABLE `course_offerings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `class_id` bigint NOT NULL COMMENT '班级ID',
  `semester` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学期',
  `status` tinyint NULL DEFAULT 0 COMMENT '开课状态: 0-未开始, 1-进行中, 2-已结束',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_course_class_semester`(`course_id` ASC, `class_id` ASC, `semester` ASC) USING BTREE,
  INDEX `class_id`(`class_id` ASC) USING BTREE,
  CONSTRAINT `course_offerings_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `course_offerings_ibfk_2` FOREIGN KEY (`class_id`) REFERENCES `classes` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 306 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '开课表，将课程和班级关联' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for course_progress
-- ----------------------------
DROP TABLE IF EXISTS `course_progress`;
CREATE TABLE `course_progress`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `chapter_id` bigint NULL DEFAULT NULL,
  `last_position` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `completion_percentage` decimal(5, 2) NULL DEFAULT 0.00,
  `last_access_time` datetime NULL DEFAULT NULL COMMENT '最后访问时间',
  `study_time` int NOT NULL DEFAULT 0 COMMENT '学习时长(分钟)',
  `completion_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'not_started' COMMENT '完成状态: not_started-未开始, in_progress-进行中, completed-已完成',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_course`(`student_id` ASC, `course_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 102 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for courses
-- ----------------------------
DROP TABLE IF EXISTS `courses`;
CREATE TABLE `courses`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '课程唯一ID',
  `teacher_id` bigint NOT NULL COMMENT '创建教师ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程标题',
  `subject` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属学科',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '课程描述',
  `is_published` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否发布',
  `student_count` int NOT NULL DEFAULT 0 COMMENT '学生数量',
  `chapter_count` int NOT NULL DEFAULT 0 COMMENT '章节数量',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片url',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `teacher_id`(`teacher_id` ASC) USING BTREE,
  CONSTRAINT `courses_ibfk_1` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 106 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程模板表，定义课程内容' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for discussion_likes
-- ----------------------------
DROP TABLE IF EXISTS `discussion_likes`;
CREATE TABLE `discussion_likes`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `discussion_id` bigint NOT NULL COMMENT '讨论ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_discussion`(`user_id` ASC, `discussion_id` ASC) USING BTREE,
  INDEX `idx_discussion_id`(`discussion_id` ASC) USING BTREE,
  CONSTRAINT `fk_like_discussion` FOREIGN KEY (`discussion_id`) REFERENCES `course_discussions` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '讨论点赞表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for discussion_replies
-- ----------------------------
DROP TABLE IF EXISTS `discussion_replies`;
CREATE TABLE `discussion_replies`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '回复ID',
  `discussion_id` bigint NOT NULL COMMENT '讨论ID',
  `user_id` bigint NOT NULL COMMENT '回复用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '回复内容',
  `reply_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '回复时间',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞数量',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父回复ID（用于嵌套回复）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_discussion_id`(`discussion_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_reply_time`(`reply_time` ASC) USING BTREE,
  CONSTRAINT `fk_reply_discussion` FOREIGN KEY (`discussion_id`) REFERENCES `course_discussions` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '讨论回复表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for enrollments
-- ----------------------------
DROP TABLE IF EXISTS `enrollments`;
CREATE TABLE `enrollments`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `offering_id` bigint NOT NULL COMMENT '开课ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_offering`(`student_id` ASC, `offering_id` ASC) USING BTREE,
  INDEX `offering_id`(`offering_id` ASC) USING BTREE,
  CONSTRAINT `enrollments_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `enrollments_ibfk_2` FOREIGN KEY (`offering_id`) REFERENCES `course_offerings` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 406 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学生选课关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for exams
-- ----------------------------
DROP TABLE IF EXISTS `exams`;
CREATE TABLE `exams`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `offering_id` bigint NOT NULL COMMENT '针对哪个开课班级的考核',
  `creator_id` bigint NOT NULL,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `exam_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '作业、测验、考试',
  `start_time` timestamp NULL DEFAULT NULL,
  `end_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `offering_id`(`offering_id` ASC) USING BTREE,
  INDEX `creator_id`(`creator_id` ASC) USING BTREE,
  CONSTRAINT `exams_ibfk_1` FOREIGN KEY (`offering_id`) REFERENCES `course_offerings` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `exams_ibfk_2` FOREIGN KEY (`creator_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 506 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '考核信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for grades
-- ----------------------------
DROP TABLE IF EXISTS `grades`;
CREATE TABLE `grades`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `item_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评分项类型: assignment-作业, exam-考试, quiz-测验, project-项目',
  `item_id` bigint NOT NULL COMMENT '评分项ID',
  `score` decimal(5, 2) NOT NULL COMMENT '得分',
  `weight` decimal(5, 2) NOT NULL DEFAULT 1.00 COMMENT '权重',
  `comments` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评语',
  `graded_by` bigint NOT NULL COMMENT '评分教师ID',
  `graded_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评分时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_course`(`student_id` ASC, `course_id` ASC) USING BTREE,
  INDEX `idx_item`(`item_type` ASC, `item_id` ASC) USING BTREE,
  INDEX `fk_grade_course_ref`(`course_id` ASC) USING BTREE,
  INDEX `fk_grade_teacher_user`(`graded_by` ASC) USING BTREE,
  CONSTRAINT `fk_grade_course_ref` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_grade_student_user` FOREIGN KEY (`student_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_grade_teacher_user` FOREIGN KEY (`graded_by`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '成绩记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for learning_analytics
-- ----------------------------
DROP TABLE IF EXISTS `learning_analytics`;
CREATE TABLE `learning_analytics`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `subject_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `strength_areas` json NULL,
  `weak_areas` json NULL,
  `learning_style` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for learning_behaviors
-- ----------------------------
DROP TABLE IF EXISTS `learning_behaviors`;
CREATE TABLE `learning_behaviors`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `chapter_id` bigint NULL DEFAULT NULL COMMENT '章节ID',
  `content_id` bigint NULL DEFAULT NULL COMMENT '内容ID',
  `behavior_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '行为类型：VIEW-查看, DOWNLOAD-下载, COMPLETE-完成, QUIZ-测验',
  `duration` int NULL DEFAULT NULL COMMENT '持续时间（秒）',
  `progress` decimal(5, 2) NULL DEFAULT NULL COMMENT '进度百分比',
  `score` decimal(5, 2) NULL DEFAULT NULL COMMENT '得分（适用于测验）',
  `device_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备类型',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_behavior_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_behavior_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学习行为记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for learning_notes
-- ----------------------------
DROP TABLE IF EXISTS `learning_notes`;
CREATE TABLE `learning_notes`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '笔记ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `chapter_id` bigint NOT NULL COMMENT '章节ID',
  `content_id` bigint NOT NULL COMMENT '内容ID',
  `note_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '笔记内容',
  `time_point` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '视频时间点(HH:MM:SS)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_course`(`student_id` ASC, `course_id` ASC) USING BTREE,
  INDEX `idx_chapter_content`(`chapter_id` ASC, `content_id` ASC) USING BTREE,
  INDEX `fk_note_course`(`course_id` ASC) USING BTREE,
  INDEX `fk_note_content`(`content_id` ASC) USING BTREE,
  CONSTRAINT `fk_note_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `course_chapters` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_note_content` FOREIGN KEY (`content_id`) REFERENCES `chapter_contents` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_note_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_note_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学习笔记表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for learning_records
-- ----------------------------
DROP TABLE IF EXISTS `learning_records`;
CREATE TABLE `learning_records`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学生ID',
  `course_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程ID',
  `content_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '内容类型: 视频、文档、问答等',
  `content_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '具体内容ID',
  `start_time` datetime NOT NULL COMMENT '开始学习时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束学习时间',
  `duration` int NULL DEFAULT NULL COMMENT '学习时长(秒)',
  `progress` decimal(5, 2) NULL DEFAULT NULL COMMENT '完成进度(百分比)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 138 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学习活动记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for learning_statistics
-- ----------------------------
DROP TABLE IF EXISTS `learning_statistics`;
CREATE TABLE `learning_statistics`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `total_duration` int NOT NULL DEFAULT 0 COMMENT '总学习时长（秒）',
  `visit_count` int NOT NULL DEFAULT 0 COMMENT '访问次数',
  `last_visit_date` date NULL DEFAULT NULL COMMENT '最后访问日期',
  `consecutive_days` int NOT NULL DEFAULT 0 COMMENT '连续学习天数',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_course`(`student_id` ASC, `course_id` ASC) USING BTREE,
  INDEX `fk_stats_course`(`course_id` ASC) USING BTREE,
  CONSTRAINT `fk_stats_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_stats_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学习统计表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for learning_statistics_detail
-- ----------------------------
DROP TABLE IF EXISTS `learning_statistics_detail`;
CREATE TABLE `learning_statistics_detail`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '统计详情ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `chapter_id` bigint NOT NULL COMMENT '章节ID',
  `duration` int NOT NULL DEFAULT 0 COMMENT '学习时长（秒）',
  `visit_count` int NOT NULL DEFAULT 0 COMMENT '访问次数',
  `last_visit_time` datetime NOT NULL COMMENT '最后访问时间',
  `is_completed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否完成(0-否，1-是)',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_chapter`(`student_id` ASC, `chapter_id` ASC) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  INDEX `fk_stats_detail_chapter`(`chapter_id` ASC) USING BTREE,
  CONSTRAINT `fk_stats_detail_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `course_chapters` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_stats_detail_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_stats_detail_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 155 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学习统计详情表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for lesson_plans
-- ----------------------------
DROP TABLE IF EXISTS `lesson_plans`;
CREATE TABLE `lesson_plans`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NOT NULL COMMENT '所属课程ID',
  `creator_id` bigint NOT NULL COMMENT '创建者ID',
  `teacher_id` bigint NOT NULL COMMENT '教师ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '教案标题',
  `subject` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学科',
  `grade` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '年级',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '教案内容 (Markdown)',
  `markdown_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '教案内容 (Markdown)',
  `outline_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '教案大纲',
  `template_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板类型',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿，PUBLISHED-已发布',
  `duration` int NULL DEFAULT NULL COMMENT '课时长度（分钟）',
  `difficulty_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '难度等级',
  `tags` json NULL COMMENT '标签',
  `is_ai_generated` tinyint(1) NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `course_id`(`course_id` ASC) USING BTREE,
  INDEX `creator_id`(`creator_id` ASC) USING BTREE,
  INDEX `teacher_id`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_subject_grade`(`subject` ASC, `grade` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `lesson_plans_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `lesson_plans_ibfk_2` FOREIGN KEY (`creator_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `lesson_plans_ibfk_3` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '教案信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mistake_collection
-- ----------------------------
DROP TABLE IF EXISTS `mistake_collection`;
CREATE TABLE `mistake_collection`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `question_id` bigint NOT NULL,
  `exam_id` bigint NULL DEFAULT NULL,
  `added_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `review_count` int NULL DEFAULT 0,
  `mastery_level` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for notification_reads
-- ----------------------------
DROP TABLE IF EXISTS `notification_reads`;
CREATE TABLE `notification_reads`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `announcement_id` bigint NOT NULL COMMENT '公告ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `read_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_announcement_user`(`announcement_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `fk_notification_user`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_notification_announcement` FOREIGN KEY (`announcement_id`) REFERENCES `announcements` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知阅读记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for question_batch
-- ----------------------------
DROP TABLE IF EXISTS `question_batch`;
CREATE TABLE `question_batch`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `question_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `batch_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `question_id`(`question_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 229 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for questions
-- ----------------------------
DROP TABLE IF EXISTS `questions`;
CREATE TABLE `questions`  (
  `question_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `exam_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `question_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `options` json NULL,
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `analysis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `score` decimal(5, 2) NULL DEFAULT NULL,
  `difficulty` tinyint NULL DEFAULT NULL,
  `created_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `is_ai_generated` tinyint(1) NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `id` int NOT NULL AUTO_INCREMENT,
  `subject` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 307 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for spring_ai_chat_memory
-- ----------------------------
DROP TABLE IF EXISTS `spring_ai_chat_memory`;
CREATE TABLE `spring_ai_chat_memory`  (
  `conversation_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `timestamp` timestamp NOT NULL,
  CONSTRAINT `TYPE_CHECK` CHECK (`type` in (_utf8mb4'USER',_utf8mb4'ASSISTANT',_utf8mb4'SYSTEM',_utf8mb4'TOOL'))
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for student_exams
-- ----------------------------
DROP TABLE IF EXISTS `student_exams`;
CREATE TABLE `student_exams`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学生ID',
  `exam_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '考核ID',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始作答时间',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `score` decimal(5, 2) NULL DEFAULT NULL COMMENT '得分',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态: 0-未开始，1-进行中，2-已提交，3-已批改',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学生考核记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for subjects
-- ----------------------------
DROP TABLE IF EXISTS `subjects`;
CREATE TABLE `subjects`  (
  `subject_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学科唯一标识',
  `subject_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学科名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '学科描述',
  PRIMARY KEY (`subject_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学科信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for submissions
-- ----------------------------
DROP TABLE IF EXISTS `submissions`;
CREATE TABLE `submissions`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '提交记录ID',
  `exam_id` bigint NOT NULL COMMENT '考试ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `status` tinyint NOT NULL COMMENT '状态：0-进行中，1-已提交未批改，2-已批改',
  `submitted_at` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `total_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '总分',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_exam_student`(`exam_id` ASC, `student_id` ASC) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  CONSTRAINT `fk_submission_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 609 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '考试提交记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for system_logs
-- ----------------------------
DROP TABLE IF EXISTS `system_logs`;
CREATE TABLE `system_logs`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户ID',
  `action` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作内容',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作IP',
  `user_agent` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '用户代理信息',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  `status` tinyint NULL DEFAULT NULL COMMENT '状态: 0-失败，1-成功',
  `details` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '详细信息',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for teaching_plans
-- ----------------------------
DROP TABLE IF EXISTS `teaching_plans`;
CREATE TABLE `teaching_plans`  (
  `plan_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '教案唯一标识',
  `course_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '所属课程ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '教案标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '教案内容',
  `objectives` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '教学目标',
  `key_points` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '教学重点',
  `difficult_points` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '教学难点',
  `time_allocation` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '教学时间分配',
  `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '创建者ID',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  `is_ai_generated` tinyint(1) NULL DEFAULT 0 COMMENT '是否AI生成',
  PRIMARY KEY (`plan_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '教案信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for teaching_resources
-- ----------------------------
DROP TABLE IF EXISTS `teaching_resources`;
CREATE TABLE `teaching_resources`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '资源唯一ID',
  `course_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '所属课程ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '资源标题',
  `subject` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学科',
  `grade` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '年级',
  `resource_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '资源类型: 文档、PPT、视频等',
  `file_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件存储路径',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '外部URL链接',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '资源描述',
  `tags` json NULL COMMENT '标签',
  `is_public` tinyint(1) NULL DEFAULT 0 COMMENT '是否公开',
  `quality_score` decimal(3, 2) NULL DEFAULT 0.00 COMMENT '质量评分',
  `view_count` int NULL DEFAULT 0 COMMENT '查看次数',
  `download_count` int NULL DEFAULT 0 COMMENT '下载次数',
  `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '上传者ID',
  `created_at` datetime NOT NULL COMMENT '上传时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_ai_generated` tinyint(1) NULL DEFAULT 0 COMMENT '是否AI生成',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_subject_grade`(`subject` ASC, `grade` ASC) USING BTREE,
  INDEX `idx_resource_type`(`resource_type` ASC) USING BTREE,
  INDEX `idx_is_public`(`is_public` ASC) USING BTREE,
  INDEX `idx_quality_score`(`quality_score` ASC) USING BTREE,
  INDEX `idx_created_by`(`created_by` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '教学资源信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for todo_tasks
-- ----------------------------
DROP TABLE IF EXISTS `todo_tasks`;
CREATE TABLE `todo_tasks`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '任务所属用户ID (e.g., 教师)',
  `task_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务类型: e.g., GRADING, ANNOUNCEMENT',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '任务描述',
  `priority` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'medium' COMMENT '优先级: high-高, medium-中, low-低',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING, COMPLETED',
  `due_date` timestamp NULL DEFAULT NULL COMMENT '截止日期',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `todo_tasks_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '待办任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for usage_statistics
-- ----------------------------
DROP TABLE IF EXISTS `usage_statistics`;
CREATE TABLE `usage_statistics`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户ID',
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '使用模块: 备课系统、考核系统、学习助手等',
  `action` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '具体操作',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `duration` int NULL DEFAULT NULL COMMENT '使用时长(秒)',
  `request_count` int NULL DEFAULT 1 COMMENT '请求次数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统使用统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_class_relation
-- ----------------------------
DROP TABLE IF EXISTS `user_class_relation`;
CREATE TABLE `user_class_relation`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '关系ID',
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户ID',
  `class_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '班级ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户与班级的关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_questions
-- ----------------------------
DROP TABLE IF EXISTS `user_questions`;
CREATE TABLE `user_questions`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '问题ID',
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学生ID',
  `course_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '相关课程ID',
  `question` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '问题内容',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '回答内容',
  `asked_at` datetime NOT NULL COMMENT '提问时间',
  `answered_at` datetime NULL DEFAULT NULL COMMENT '回答时间',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态: 0-未回答，1-已回答',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学生提问记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户登录名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `real_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '电子邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '电话号码',
  `role_id` int NOT NULL COMMENT '角色ID: 1-管理员，2-教师，3-学生',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像图片URL',
  `created_at` date NOT NULL COMMENT '账户创建时间',
  `last_login` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `status` tinyint NULL DEFAULT 1 COMMENT '账户状态: 1-正常，0-禁用',
  `classId` bigint NULL DEFAULT NULL COMMENT '班级id',
  PRIMARY KEY (`user_id`) USING BTREE,
  INDEX `class_key`(`classId` ASC) USING BTREE,
  CONSTRAINT `class_key` FOREIGN KEY (`classId`) REFERENCES `classes` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 202 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户信息表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
