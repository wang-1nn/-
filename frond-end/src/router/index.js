import {createRouter, createWebHistory} from 'vue-router'
import {useAuthStore} from '@/stores/counter.js'

// 布局组件
const TeacherLayout = () => import('@/components/layout/TeacherLayout.vue')
const StudentLayout = () => import('@/components/layout/StudentLayout.vue')
// const AdminLayout = () => import('@/components/layout/AdminLayout.vue')

// 公共页面
const Login = () => import('@/views/auth/Login.vue')

// 导入模块化路由
import teacherRoutes from './modules/teacher.js'

// 学生页面
const StudentDashboard = () => import('@/views/student/Dashboard.vue')
const Practice = () => import('@/views/student/Practice.vue') // 旧页面，后续可删除
const LearningCenter = () => import('@/views/student/LearningCenter.vue')

// 管理员页面
// const AdminDashboard = () => import('@/views/admin/Dashboard.vue')
// const UserManagement = () => import('@/views/admin/UserManagement.vue')

// 路由配置
const routes = [
    {
        path: '/',
        redirect: '/login'
    },
    {
        path: '/login',
        name: 'Login',
        component: Login,
        meta: {
            title: '登录',
            requiresAuth: false
        }
    },
    /* 注释管理员路由
    {
        path: '/admin',
        component: AdminLayout,
        redirect: '/admin/dashboard',
        meta: {
            requiresAuth: true,
            role: 'admin'
        },
        children: [
            {
                path: 'dashboard',
                name: 'AdminDashboard',
                component: AdminDashboard,
                meta: {
                    title: '系统概况',
                    icon: 'el-icon-data-board',
                }
            },
            {
                path: 'users',
                name: 'UserManagement',
                component: UserManagement,
                meta: {
                    title: '用户管理',
                    icon: 'el-icon-user',
                }
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
        ]
    },
    */
    {
        path: '/teacher',
        component: TeacherLayout,
        redirect: '/teacher/dashboard',
        meta: {
            requiresAuth: true,
            role: 'teacher',
            requiresRole: 2
        },
        children: teacherRoutes
    },
    {
        path: '/student',
        component: StudentLayout,
        redirect: '/student/dashboard',
        meta: {
            requiresAuth: true,
            role: 'student',
            requiresRole: 3
        },
        children: [
            { path: 'dashboard', name: 'StudentDashboard', component: StudentDashboard, meta: { title: '学习中心', icon: 'el-icon-monitor', requiresRole: 3 } },
            { path: 'learning/:courseId/:chapterId', name: 'LearningCenter', component: LearningCenter, meta: { title: '学习中心', hidden:true, requiresRole: 3 }, props: true },
            { path: 'practice', name: 'Practice', component: Practice, meta: { title: '实时练习', hidden:true, requiresRole: 3 } },
            { path: 'courses', name: 'student-courses', component: () => import('@/views/student/Courses.vue'), meta: { title: '我的课程', requiresRole: 3 } },
            { path: 'course/:courseId', name: 'student-course-detail', component: () => import('@/views/student/CourseDetail.vue'), meta: { title: '课程详情', hidden:true, requiresRole: 3 }, props:true },
            { path: 'tasks', name: 'student-tasks', component: () => import('@/views/student/Tasks.vue'), meta: { title: '计划', hidden:true, requiresRole: 3 } },
            { path: 'exams', name: 'student-exams', component: () => import('@/views/student/ExamCenter.vue'), meta: { title: '考试中心', requiresRole: 3 } },
            { path: 'resources', name: 'student-resources', component: () => import('@/views/student/Resources.vue'), meta: { title: '学习资源', requiresRole: 3 } },
            { path: 'ai-assistant', name: 'student-ai-assistant', component: () => import('@/views/student/AiAssistant.vue'), meta: { title: '智能助手', requiresRole: 3 } },
            { path: 'exam/:examId', name: 'student-exam-taking', component: () => import('@/views/student/ExamTaking.vue'), meta: { title: '参加考试', requiresRole: 3 }, props: true },
            { path: 'exams/take/:examId', name: 'student-exam-taking-alt', component: () => import('@/views/student/ExamTaking.vue'), meta: { title: '参加考试', requiresRole: 3 }, props: true },
            { path: 'mistakes', name: 'student-mistakes', component: () => import('@/views/student/Mistakes.vue'), meta: { title: '错题本', hidden:true, requiresRole: 3 } },
            { path: 'scores', name: 'student-scores', component: () => import('@/views/student/Scores.vue'), meta: { title: '我的成绩', requiresRole: 3 } },
            { path: 'scores/detail/:id', name: 'student-score-detail', component: () => import('@/views/student/ScoreDetail.vue'), meta: { title: '成绩详情', hidden:true, requiresRole: 3 }, props: true },
            { path: 'adaptive-exam', name: 'student-adaptive-exam', component: () => import('@/views/student/AdaptiveExam.vue'), meta: { title: '自适应测试', requiresRole: 3 } },
            { path: 'adaptive-exam/start', name: 'student-adaptive-exam-start', component: () => import('@/views/student/AdaptiveExamStart.vue'), meta: { title: '开始自适应测试', hidden:true, requiresRole: 3 } },
            { path: 'adaptive-exam/:id', name: 'student-adaptive-exam-taking', component: () => import('@/views/student/AdaptiveExamTaking.vue'), meta: { title: '进行自适应测试', hidden:true, requiresRole: 3 }, props: true },
            { path: 'adaptive-exam/report/:id', name: 'student-adaptive-exam-report', component: () => import('@/views/student/AdaptiveExamReport.vue'), meta: { title: '测试报告', hidden:true, requiresRole: 3 }, props: true },
            { path: 'learning-timeline', name: 'student-learning-timeline', component: () => import('@/views/student/LearningTimeline.vue'), meta: { title: '学习轨迹', requiresRole: 3 } },
            { path: 'achievements', name: 'student-achievements', component: () => import('@/views/student/Achievements.vue'), meta: { title: '我的成就', requiresRole: 3 } }
        ]
    },
    {
        path: '/:pathMatch(.*)*',
        redirect: '/login'
    }
]

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
    const authStore = useAuthStore()
    document.title = to.meta.title ? `${to.meta.title} - 智慧教学平台` : '智慧教学平台'

    // 检查是否需要认证
    if (to.meta.requiresAuth && !authStore.isLoggedIn) {
        next({ name: 'Login' })
        return
    }
    
    // 检查用户角色ID
    if (to.meta.requiresRole && authStore.user && authStore.user.roleId) {
        // 如果路径需要特定角色ID但用户没有对应权限
        if (to.meta.requiresRole !== authStore.user.roleId) {
            // 根据用户角色ID重定向到对应的首页
            switch (authStore.user.roleId) {
                case 1:
                    next({ path: '/admin/dashboard' })
                    break
                case 2:
                    next({ path: '/teacher/dashboard' })
                    break
                case 3:
                    next({ path: '/student/dashboard' })
                    break
                default:
                    next({ name: 'Login' })
            }
            return
        }
    }

    // 对于根路径访问，根据登录状态和角色重定向
    if (to.path === '/') {
        if (!authStore.isLoggedIn) {
            next({ name: 'Login' })
            return
        }
        
        // 根据角色ID重定向到相应首页
        if (authStore.user && authStore.user.roleId) {
            switch (authStore.user.roleId) {
                case 1:
                    next({ path: '/admin/dashboard' })
                    break
                case 2:
                    next({ path: '/teacher/dashboard' })
                    break
                case 3:
                    next({ path: '/student/dashboard' })
                    break
                default:
                    next({ name: 'Login' })
            }
            return
        }
    } 
    
    // 对于登录页，如果已登录则重定向到相应首页
    if (to.name === 'Login' && authStore.isLoggedIn && authStore.user && authStore.user.roleId) {
        // 根据角色ID重定向到相应首页
        switch (authStore.user.roleId) {
            case 1:
                next({ path: '/admin/dashboard' })
                break
            case 2:
                next({ path: '/teacher/dashboard' })
                break
            case 3:
                next({ path: '/student/dashboard' })
                break
            default:
                next()
        }
        return
    }
    
    // 其他情况正常放行
    next()
})

export default router
