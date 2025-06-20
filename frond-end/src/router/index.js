import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import TeacherLayout from '@/components/layout/TeacherLayout.vue'
import StudentLayout from '@/components/layout/StudentLayout.vue'
import {useAuthStore} from "@/stores/counter.js";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            redirect: '/login'
        },
        {
            path: '/login',
            name: 'login',
            component: () => import('@/views/Login.vue'),
            meta: { requiresAuth: false }
        },
        // 管理员路由
        {
            path: '/admin',
            component: AdminLayout,
            children: [
                {
                    path: '',
                    redirect: '/admin/dashboard'
                },
                {
                    path: 'dashboard',
                    name: 'admin-dashboard',
                    component: () => import('@/views/admin/Dashboard.vue'),
                    meta: { title: '系统概况', requiresRole: 1 }
                },
                {
                    path: 'users',
                    name: 'admin-users',
                    component: () => import('@/views/admin/UserManagement.vue'),
                    meta: { title: '用户管理', requiresRole: 1 }
                },
                {
                    path: 'classes',
                    name: 'admin-classes',
                    component: () => import('@/views/admin/ClassManagement.vue'),
                    meta: { title: '班级管理', requiresRole: 1 }
                },
                {
                    path: 'courses',
                    name: 'admin-courses',
                    component: () => import('@/views/admin/CourseManagement.vue'),
                    meta: { title: '课程管理', requiresRole: 1 }
                },
                {
                    path: 'resources',
                    name: 'admin-resources',
                    component: () => import('@/views/admin/Resources.vue'),
                    meta: { title: '资源管理', requiresRole: 1 }
                },
                {
                    path: 'logs',
                    name: 'admin-logs',
                    component: () => import('@/views/admin/SystemLogs.vue'),
                    meta: { title: '系统日志', requiresRole: 1 }
                },
                {
                    path: 'settings',
                    name: 'admin-settings',
                    component: () => import('@/views/admin/Settings.vue'),
                    meta: { title: '系统设置', requiresRole: 1 }
                },
            ],
            meta: { requiresAuth: true, requiresRole: 1 }
        },
        // 教师路由
        {
            path: '/teacher',
            component: TeacherLayout,
            children: [
                {
                    path: '',
                    redirect: '/teacher/dashboard'
                },
                {
                    path: 'dashboard',
                    name: 'teacher-dashboard',
                    component: () => import('@/views/teacher/Dashboard.vue'),
                    meta: { title: '工作台', requiresRole: 2 }
                },
                {
                    path: 'courses',
                    name: 'teacher-courses',
                    component: () => import('@/views/teacher/CourseManagement.vue'),
                    meta: { title: '课程管理', requiresRole: 2 }
                },
                {
                    path: 'teaching-plans',
                    name: 'teacher-teaching-plans',
                    component: () => import('@/views/teacher/TeachingPlans.vue'),
                    meta: { title: '教学计划', requiresRole: 2 }
                },
                {
                    path: 'exams',
                    name: 'teacher-exams',
                    component: () => import('@/views/teacher/Exams.vue'),
                    meta: { title: '考核设计', requiresRole: 2 }
                },
                {
                    path: 'scores',
                    name: 'teacher-scores',
                    component: () => import('@/views/teacher/Scores.vue'),
                    meta: { title: '学情分析', requiresRole: 2 }
                },
                {
                    path: 'ai-assistant',
                    name: 'teacher-ai-assistant',
                    component: () => import('@/views/teacher/AiAssistant.vue'),
                    meta: { title: '智能助手', requiresRole: 2 }
                },
                {
                    path: 'classes',
                    name: 'teacher-classes',
                    component: () => import('@/views/teacher/ClassManagement.vue'),
                    meta: { title: '班级管理', requiresRole: 2 }
                },
                {
                    path: 'students',
                    name: 'teacher-students',
                    component: () => import('@/views/teacher/StudentManagement.vue'),
                    meta: { title: '学生管理', requiresRole: 2 }
                },
                {
                    path: 'question-bank',
                    name: 'teacher-question-bank',
                    component: () => import('@/views/teacher/QuestionBank.vue'),
                    meta: { title: '题库管理', requiresRole: 2 }
                },
            ],
            meta: { requiresAuth: true, requiresRole: 2 }
        },
        // 学生路由
        {
            path: '/student',
            component: StudentLayout,
            children: [
                {
                    path: '',
                    redirect: '/student/dashboard'
                },
                {
                    path: 'dashboard',
                    name: 'student-dashboard',
                    component: () => import('@/views/student/Dashboard.vue'),
                    meta: { title: '学习主页', requiresRole: 3 }
                },
                {
                    path: 'courses',
                    name: 'student-courses',
                    component: () => import('@/views/student/Courses.vue'),
                    meta: { title: '我的课程', requiresRole: 3 }
                },
                {
                    path: 'tasks',
                    name: 'student-tasks',
                    component: () => import('@/views/student/Tasks.vue'),
                    meta: { title: '练习与纠错', requiresRole: 3 }
                },
                {
                    path: 'exams',
                    name: 'student-exams',
                    component: () => import('@/views/student/Exams.vue'),
                    meta: { title: '我的考核', requiresRole: 3 }
                },
                {
                    path: 'resources',
                    name: 'student-resources',
                    component: () => import('@/views/student/Resources.vue'),
                    meta: { title: '学习资源', requiresRole: 3 }
                },
                {
                    path: 'ai-assistant',
                    name: 'student-ai-assistant',
                    component: () => import('@/views/student/AiAssistant.vue'),
                    meta: { title: '智能助手', requiresRole: 3 }
                },
                {
                    path: 'scores',
                    name: 'student-scores',
                    component: () => import('@/views/student/Scores.vue'),
                    meta: { title: '我的成绩', requiresRole: 3 }
                },
                {
                    path: 'mistakes',
                    name: 'student-mistakes',
                    component: () => import('@/views/student/Mistakes.vue'),
                    meta: { title: '错题本', requiresRole: 3 }
                },
                {
                    path: 'exams/:examId',
                    name: 'student-exam-detail',
                    component: () => import('@/views/student/ExamTaking.vue'),
                    meta: { title: '考试详情', requiresRole: 3 }
                },
                {
                    path: 'practice',
                    name: 'student-practice',
                    component: () => import('@/views/student/Practice.vue'),
                    meta: { title: '实时练习', requiresRole: 3 }
                },
            ],
            meta: { requiresAuth: true, requiresRole: 3 }
        },
        // 默认路由
        {
            path: '/:pathMatch(.*)*',
            redirect: '/login'
        }
    ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
    const auth = useAuthStore()

    // 如果是登录页，直接通过
    if (to.path === '/login') {
        // 已登录状态访问登录页，根据角色重定向
        if (auth.isLoggedIn && auth.user) {
            if (auth.user.roleId === 1) return next('/admin')
            if (auth.user.roleId === 2) return next('/teacher')
            if (auth.user.roleId === 3) return next('/student')
        }
        // 未登录则正常显示登录页
        return next()
    }

    // 需要认证的页面，但未登录
    if (to.meta.requiresAuth && !auth.isLoggedIn) {
        return next('/login')
    }

    // 角色限制检查
    if (to.meta.requiresRole && auth.user && to.meta.requiresRole !== auth.user.roleId) {
        return next('/login')
    }

    next()
})

export default router
