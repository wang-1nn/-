-- 教师个人信息
SELECT real_name FROM users WHERE user_id = 1;

-- 课程总数
SELECT COUNT(*) FROM courses WHERE teacher_id = 1;

-- 近7天新增课程数
SELECT COUNT(*) FROM courses WHERE teacher_id = 1 AND created_at >= CURDATE() - INTERVAL 7 DAY;

-- 活跃课程数
SELECT COUNT(DISTINCT co.course_id) 
FROM course_offerings co 
JOIN courses c ON co.course_id = c.id 
WHERE c.teacher_id = 1 AND co.status = 1;

-- 学生总数（去重）
SELECT COUNT(DISTINCT e.student_id) 
FROM courses c 
JOIN course_offerings co ON c.id = co.course_id 
JOIN enrollments e ON co.id = e.offering_id 
WHERE c.teacher_id = 1;

-- 活跃学生数（30天内登录）
SELECT COUNT(DISTINCT u.user_id) 
FROM users u 
JOIN enrollments e ON u.user_id = e.student_id 
JOIN course_offerings co ON e.offering_id = co.id 
JOIN courses c ON co.course_id = c.id 
WHERE c.teacher_id = 1 AND u.last_login >= CURDATE() - INTERVAL 30 DAY;

-- 教案总数
SELECT count(*) FROM lesson_plans WHERE creator_id = 1;

-- 近7天新增教案数
SELECT count(*) FROM lesson_plans WHERE creator_id = 1 AND created_at >= CURDATE() - INTERVAL 7 DAY;

-- 教学资源总数
SELECT count(*) FROM teaching_resources WHERE created_by = '1';

-- 近7天新增教学资源数
SELECT count(*) FROM teaching_resources WHERE created_by = '1' AND created_at >= CURDATE() - INTERVAL 7 DAY;

-- 查询最近5条公告（从教案表中获取）
SELECT p.id, p.title, p.content, p.created_at as timestamp, c.title as courseName 
FROM lesson_plans p 
JOIN courses c ON p.course_id = c.id 
WHERE p.creator_id = 1 
ORDER BY p.created_at DESC 
LIMIT 5;

-- 查询所有任务
SELECT * FROM todo_tasks WHERE user_id = 1 ORDER BY due_date ASC;

-- 学生活动统计（近7天）
SELECT DATE(start_time) as activity_date, COUNT(*) as activity_count 
FROM learning_records lr 
JOIN enrollments e ON lr.user_id = e.student_id 
JOIN course_offerings co ON e.offering_id = co.id 
JOIN courses c ON co.course_id = c.id 
WHERE c.teacher_id = 1 AND lr.start_time >= CURDATE() - INTERVAL 6 DAY 
GROUP BY activity_date 
ORDER BY activity_date ASC;

-- 模块访问统计
SELECT module, COUNT(*) as access_count 
FROM usage_statistics 
WHERE user_id = '1' 
GROUP BY module 
ORDER BY access_count DESC; 