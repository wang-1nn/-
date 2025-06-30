import {createRouter, createWebHistory} from 'vue-router'
import {useAuthStore} from '@/stores/counter.js'

// 布局组件
const TeacherLayout = () => import('@/components/layout/TeacherLayout.vue')
// const StudentLayout = () => import('@/components/layout/StudentLayout.vue')
// const AdminLayout = () => import('@/components/layout/AdminLayout.vue')

// 公共页面
const Login = () => import('@/views/auth/Login.vue')

// 教师页面
const TeacherDashboard = () => import('@/views/teacher/Dashboard.vue')
const LessonPrepWorkbench = () => import('@/views/teacher/LessonPrepWorkbench.vue')
const QuestionBank = () => import('@/views/teacher/QuestionBank.vue')
const QuestionGenerator = () => import('@/views/teacher/QuestionGenerator.vue')
const AutoGrader = () => import('@/views/teacher/AutoGrader.vue')
const Analysis = () => import('@/views/teacher/analysis.vue')
const Exams = () => import('@/views/teacher/Exams.vue')
const ExamEditor = () => import('@/views/teacher/ExamEditor.vue')
const AiAssistant = () => import('@/views/teacher/AiAssistant.vue')
const StudentManagement = () => import('@/views/teacher/StudentManagement.vue')
const TeachingPlans = () => import('@/views/teacher/TeachingPlans.vue')
const Scores = () => import('@/views/teacher/Scores.vue')

// 学生页面
// const StudentDashboard = () => import('@/views/student/Dashboard.vue')
// const Practice = () => import('@/views/student/Practice.vue')

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
            role: 'teacher'
        },
        children: [
            {
                path: 'dashboard',
                name: 'TeacherDashboard',
                component: TeacherDashboard,
                meta: {
                    title: '教师工作台',
                    icon: 'el-icon-data-board',
                }
            },
            {
                path: 'lesson-prep-workbench',
                name: 'LessonPrepWorkbench',
                component: LessonPrepWorkbench,
                meta: {
                    title: '智能备课工作台',
                    icon: 'el-icon-cpu',
                }
            },
            {
                path: 'question-generator',
                name: 'QuestionGenerator',
                component: QuestionGenerator,
                meta: {
                    title: '题目生成',
                    icon: 'el-icon-magic-stick',
                }
            },
            {
                path: 'auto-grader',
                name: 'AutoGrader',
                component: AutoGrader,
                meta: {
                    title: '自动批改',
                    icon: 'el-icon-check',
                }
            },
            {
                path: 'analysis',
                name: 'Analysis',
                component: Analysis,
                meta: {
                    title: '学情分析',
                    icon: 'el-icon-data-analysis',
                }
            },
            {
                path: 'question-bank',
                name: 'QuestionBank',
                component: QuestionBank,
                meta: {
                    title: '题库管理',
                    icon: 'el-icon-collection',
                }
            },
            {
                path: 'teaching-plans',
                name: 'TeachingPlans',
                component: TeachingPlans,
                meta: { 
                    title: '教学计划', 
                    icon: 'el-icon-notebook-1' 
                }
            },
            {
                path: 'exams',
                name: 'Exams',
                component: Exams,
                meta: { 
                    title: '考核设计', 
                    icon: 'el-icon-edit' 
                }
            },
            {
                path: 'scores',
                name: 'Scores',
                component: Scores,
                meta: { 
                    title: '成绩录入', 
                    icon: 'el-icon-tickets' 
                }
            },
            {
                path: 'ai-assistant',
                name: 'AiAssistant',
                component: AiAssistant,
                meta: { 
                    title: '智能助手', 
                    icon: 'el-icon-chat-dot-round' 
                }
            },
            {
                path: 'students',
                name: 'StudentManagement',
                component: StudentManagement,
                meta: { 
                    title: '学生管理', 
                    icon: 'el-icon-user-solid' 
                }
            },
            {
                path: 'exams/new',
                name: 'ExamCreate',
                component: ExamEditor,
                meta: { 
                    title: '新建考核', 
                    icon: 'el-icon-plus' 
                }
            },
            {
                path: 'exams/:examId/edit',
                name: 'ExamEdit',
                component: ExamEditor,
                props: true,
                meta: { 
                    title: '编辑考核',
                    hidden: true
                }
            },
        ]
    },
    /* 注释学生路由
    {
        path: '/student',
        component: StudentLayout,
        redirect: '/student/dashboard',
        meta: {
            requiresAuth: true,
            role: 'student'
        },
        children: [
            {
                path: 'dashboard',
                name: 'StudentDashboard',
                component: StudentDashboard,
                meta: {
                    title: '学习中心',
                    icon: 'el-icon-monitor',
                }
            },
            {
                path: 'practice',
                name: 'Practice',
                component: Practice,
                meta: {
                    title: '实时练习',
                    icon: 'el-icon-edit-outline',
                }
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
                path: 'exam/:examId',
                name: 'student-exam-taking',
                component: () => import('@/views/student/ExamTaking.vue'),
                meta: { title: '参加考试', requiresRole: 3 }
            },
            {
                path: 'mistakes',
                name: 'student-mistakes',
                component: () => import('@/views/student/Mistakes.vue'),
                meta: { title: '错题本', requiresRole: 3 }
            },
            {
                path: 'scores',
                name: 'student-scores',
                component: () => import('@/views/student/Scores.vue'),
                meta: { title: '我的成绩', requiresRole: 3 }
            }
        ]
    },
    */
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
    }
    // 角色检查
    else if (to.meta.role && authStore.isLoggedIn && authStore.userRole !== to.meta.role) {
        // 根据用户角色重定向到对应的首页
        if (authStore.userRole === 'teacher') {
            next({ path: '/teacher/dashboard' })
        } else if (authStore.userRole === 'student') {
            next({ path: '/student/dashboard' })
        } else if (authStore.userRole === 'admin') {
            next({ path: '/admin/dashboard' })
        } else {
            next({ name: 'Login' })
        }
    } else {
        next()
    }
})

export default router
