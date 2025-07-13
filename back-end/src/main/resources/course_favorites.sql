-- ----------------------------
-- Table structure for course_favorites
-- ----------------------------
DROP TABLE IF EXISTS `course_favorites`;
CREATE TABLE `course_favorites` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_course`(`user_id` ASC, `course_id` ASC) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  CONSTRAINT `fk_favorite_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_favorite_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程收藏表' ROW_FORMAT = Dynamic; 