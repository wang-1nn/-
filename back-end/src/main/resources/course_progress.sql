/*
 创建课程学习进度表
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for course_progress
-- ----------------------------
DROP TABLE IF EXISTS `course_progress`;
CREATE TABLE `course_progress`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `offering_id` bigint NOT NULL COMMENT '开课ID',
  `last_position` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '上次学习位置',
  `completion_percentage` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '完成百分比',
  `last_study_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '上次学习时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_offering`(`student_id`, `offering_id`) USING BTREE,
  CONSTRAINT `fk_progress_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_progress_offering` FOREIGN KEY (`offering_id`) REFERENCES `course_offerings` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程学习进度表' ROW_FORMAT = Dynamic;

-- Add discussion_likes table
DROP TABLE IF EXISTS `discussion_likes`;
CREATE TABLE `discussion_likes` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `discussion_id` bigint NOT NULL COMMENT '讨论ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_discussion`(`user_id` ASC, `discussion_id` ASC) USING BTREE,
  INDEX `idx_discussion_id`(`discussion_id` ASC) USING BTREE,
  CONSTRAINT `fk_like_discussion` FOREIGN KEY (`discussion_id`) REFERENCES `course_discussions` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '讨论点赞表' ROW_FORMAT = Dynamic;

-- Insert sample data
INSERT INTO `discussion_likes` VALUES (1, 3, 1, '2025-07-10 17:16:10');
INSERT INTO `discussion_likes` VALUES (2, 4, 1, '2025-07-10 18:20:10');
INSERT INTO `discussion_likes` VALUES (3, 5, 1, '2025-07-11 09:10:10');
INSERT INTO `discussion_likes` VALUES (4, 3, 2, '2025-07-10 17:30:10');
INSERT INTO `discussion_likes` VALUES (5, 4, 2, '2025-07-11 10:15:10');

-- Add sample data for course_discussions
INSERT INTO `course_discussions` VALUES (3, 4, 3, '计算机网络中的TCP协议问题', '请问TCP的三次握手过程是怎样的？为什么需要三次握手？', '2025-07-11 10:16:10', '2025-07-11 11:16:10', 2, 8, 3, '网络协议,TCP/IP', 0, 1);
INSERT INTO `course_discussions` VALUES (4, 4, 4, '关于网络层的路由选择算法', '请教老师，距离矢量路由算法和链路状态路由算法的主要区别是什么？', '2025-07-11 09:16:10', '2025-07-11 12:16:10', 1, 5, 2, '路由算法,网络层', 0, 1);

-- Add sample data for discussion_replies
INSERT INTO `discussion_replies` VALUES (4, 3, 2, 'TCP的三次握手过程是：1. 客户端发送SYN包给服务器，请求建立连接；2. 服务器收到后回复SYN+ACK包，表示同意建立连接；3. 客户端再发送ACK包，确认连接建立。三次握手可以防止已失效的连接请求报文段突然又传送到服务端而产生错误。', '2025-07-11 10:30:10', 2, NULL, 1);
INSERT INTO `discussion_replies` VALUES (5, 3, 3, '谢谢老师的详细解答，我明白了！', '2025-07-11 11:16:10', 0, 4, 1);
INSERT INTO `discussion_replies` VALUES (6, 4, 2, '距离矢量路由算法是分布式的，每个路由器只知道相邻路由器的距离；而链路状态路由算法中，每个路由器都有整个网络的拓扑信息。前者容易产生环路，收敛速度慢；后者收敛快，但需要更多的计算资源。', '2025-07-11 12:16:10', 1, NULL, 1);

SET FOREIGN_KEY_CHECKS = 1; 