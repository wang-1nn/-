-- 清空现有数据（如果需要）
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE users;
TRUNCATE TABLE courses;
TRUNCATE TABLE classes;
TRUNCATE TABLE course_offerings;
TRUNCATE TABLE enrollments;
TRUNCATE TABLE lesson_plans;
TRUNCATE TABLE teaching_resources;
TRUNCATE TABLE todo_tasks;
TRUNCATE TABLE learning_records;
TRUNCATE TABLE usage_statistics;
SET FOREIGN_KEY_CHECKS = 1;

-- 插入用户数据
INSERT INTO users (user_id, username, password, real_name, email, phone, role_id, created_at, last_login, status) VALUES
(1, 'teacher1', 'password', '王老师', 'teacher1@example.com', '13800138001', 2, CURDATE(), NOW() - INTERVAL 1 DAY, 1),
(2, 'teacher2', 'password', '李老师', 'teacher2@example.com', '13800138002', 2, CURDATE(), NOW() - INTERVAL 3 DAY, 1),
(3, 'student1', 'password', '张三', 'student1@example.com', '13800138003', 3, CURDATE(), NOW(), 1),
(4, 'student2', 'password', '李四', 'student2@example.com', '13800138004', 3, CURDATE(), NOW() - INTERVAL 2 DAY, 1),
(5, 'student3', 'password', '王五', 'student3@example.com', '13800138005', 3, CURDATE(), NOW() - INTERVAL 5 DAY, 1),
(6, 'student4', 'password', '赵六', 'student4@example.com', '13800138006', 3, CURDATE(), NOW() - INTERVAL 10 DAY, 1),
(7, 'student5', 'password', '钱七', 'student5@example.com', '13800138007', 3, CURDATE(), NOW() - INTERVAL 15 DAY, 1),
(8, 'admin1', 'password', '管理员', 'admin@example.com', '13800138008', 1, CURDATE(), NOW(), 1);

-- 插入班级数据
INSERT INTO classes (id, name, grade, major) VALUES
(1, '计算机科学1班', '2022级', '计算机科学与技术'),
(2, '软件工程1班', '2022级', '软件工程'),
(3, '人工智能1班', '2023级', '人工智能');

-- 插入课程数据
INSERT INTO courses (id, title, subject, description, teacher_id, created_at) VALUES
(1, 'Java程序设计', '计算机科学', 'Java编程基础与进阶', 1, NOW() - INTERVAL 30 DAY),
(2, '数据结构', '计算机科学', '计算机基础数据结构', 1, NOW() - INTERVAL 5 DAY),
(3, '算法分析', '计算机科学', '常见算法与复杂度分析', 1, NOW() - INTERVAL 2 DAY),
(4, '计算机网络', '计算机科学', '网络通信原理', 2, NOW() - INTERVAL 60 DAY),
(5, '操作系统', '计算机科学', '操作系统原理与实践', 2, NOW() - INTERVAL 45 DAY);

-- 插入课程开设数据（修改为符合表结构，没有start_date和end_date字段）
INSERT INTO course_offerings (id, course_id, class_id, semester, status) VALUES
(1, 1, 1, '2023-2024-2', 1),
(2, 2, 1, '2023-2024-2', 1),
(3, 3, 2, '2023-2024-2', 1),
(4, 4, 3, '2023-2024-2', 0),
(5, 5, 2, '2023-2024-1', 0);

-- 插入选课数据（修改为符合表结构，没有enroll_date和status字段）
INSERT INTO enrollments (id, student_id, offering_id) VALUES
(1, 3, 1),
(2, 3, 2),
(3, 4, 1),
(4, 5, 1),
(5, 6, 2),
(6, 7, 3),
(7, 4, 3),
(8, 5, 2);

-- 插入教案数据
INSERT INTO lesson_plans (id, title, content, creator_id, course_id, created_at, is_ai_generated) VALUES
(1, 'Java入门第一周', 'Java基础语法介绍', 1, 1, NOW() - INTERVAL 25 DAY, 0),
(2, 'Java面向对象', '类与对象，继承与多态', 1, 1, NOW() - INTERVAL 18 DAY, 0),
(3, '链表与数组', '基础数据结构介绍', 1, 2, NOW() - INTERVAL 4 DAY, 1),
(4, '图与树', '高级数据结构', 1, 2, NOW() - INTERVAL 3 DAY, 1),
(5, '排序算法', '常见排序算法分析', 1, 3, NOW() - INTERVAL 1 DAY, 0);

-- 插入教学资源数据（修改为符合表结构，使用resource_id字段）
INSERT INTO teaching_resources (resource_id, course_id, title, resource_type, url, created_by, created_at, is_ai_generated) VALUES
('TR001', '1', 'Java基础PPT', 'PRESENTATION', '/resources/java-basics.ppt', '1', NOW() - INTERVAL 25 DAY, 0),
('TR002', '1', 'Java练习题', 'EXERCISE', '/resources/java-exercises.pdf', '1', NOW() - INTERVAL 20 DAY, 0),
('TR003', '2', '数据结构示例代码', 'CODE', '/resources/ds-examples.zip', '1', NOW() - INTERVAL 4 DAY, 0),
('TR004', '3', '算法分析教材', 'TEXTBOOK', '/resources/algorithm-textbook.pdf', '1', NOW() - INTERVAL 1 DAY, 0),
('TR005', '1', '期中考试复习资料', 'DOCUMENT', '/resources/midterm-review.docx', '1', NOW() - INTERVAL 5 DAY, 1);

-- 插入任务数据
INSERT INTO todo_tasks (id, user_id, task_type, title, status, due_date, created_at) VALUES
(1, 1, '0', '准备Java期中考试', 'PENDING', NOW() + INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY),
(2, 1, '1', '批改数据结构作业', 'COMPLETED', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 15 DAY),
(3, 1, '2', '更新算法课件', 'PENDING', NOW() + INTERVAL 20 DAY, NOW() - INTERVAL 5 DAY),
(4, 1, '0', '提交教学计划', 'PENDING', NOW() + INTERVAL 5 DAY, NOW() - INTERVAL 2 DAY),
(5, 1, '1', '参加教研室会议', 'PENDING', NOW() + INTERVAL 2 DAY, NOW() - INTERVAL 1 DAY);

-- 插入学习记录数据（修改为符合表结构）
INSERT INTO learning_records (id, user_id, course_id, content_type, content_id, start_time, end_time, duration, progress) VALUES
(1, '3', '1', '视频', '1', NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY + INTERVAL 45 MINUTE, 45, 100),
(2, '4', '1', '视频', '1', NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY + INTERVAL 30 MINUTE, 30, 80),
(3, '5', '1', '文档', '2', NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY + INTERVAL 60 MINUTE, 60, 100),
(4, '3', '2', '视频', '3', NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY + INTERVAL 120 MINUTE, 120, 90),
(5, '4', '2', '文档', '4', NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY + INTERVAL 90 MINUTE, 90, 75),
(6, '6', '2', '视频', '5', NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY + INTERVAL 45 MINUTE, 45, 50),
(7, '3', '3', '文档', '6', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY + INTERVAL 180 MINUTE, 180, 100),
(8, '7', '3', '视频', '7', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY + INTERVAL 60 MINUTE, 60, 40),
(9, '3', '3', '文档', '8', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY + INTERVAL 120 MINUTE, 120, 100),
(10, '4', '3', '视频', '9', NOW(), NOW() + INTERVAL 90 MINUTE, 90, 80);

-- 插入使用统计数据（修改为符合表结构，包含module和action）
INSERT INTO usage_statistics (id, user_id, module, action, start_time, end_time, duration, request_count) VALUES
(1, '1', '课程管理', '课程编辑', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY + INTERVAL 30 MINUTE, 1800, 25),
(2, '1', '教学资源', '资源上传', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY + INTERVAL 45 MINUTE, 2700, 18),
(3, '1', '作业批改', '批阅作业', NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY + INTERVAL 60 MINUTE, 3600, 12),
(4, '1', '成绩管理', '成绩录入', NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY + INTERVAL 40 MINUTE, 2400, 8),
(5, '1', '学情分析', '分析报表', NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY + INTERVAL 25 MINUTE, 1500, 5); 