// 教师页面
const TeacherDashboard = () => import('@/views/teacher/dashboard-module/Dashboard.vue')
const LessonPrepWorkbench = () => import('@/views/teacher/lesson-preparation-module/LessonPrepWorkbench.vue')
const QuestionBank = () => import('@/views/teacher/exam-assessment-module/QuestionBank.vue')
const QuestionGenerator = () => import('@/views/teacher/exam-assessment-module/QuestionGenerator.vue')
const AutoGrader = () => import('@/views/teacher/analytics-module/AutoGrader.vue')
const Analysis = () => import('@/views/teacher/analytics-module/analysis.vue')
const Exams = () => import('@/views/teacher/exam-assessment-module/Exams.vue')
const ExamEditor = () => import('@/views/teacher/exam-assessment-module/ExamEditor.vue')
const ExamResult = () => import('@/views/teacher/exam-assessment-module/ExamResult.vue')
const AiAssistant = () => import('@/views/teacher/ai-assistant-module/AiAssistant.vue')
const StudentManagement = () => import('@/views/teacher/student-management-module/StudentManagement.vue')
const TeachingPlans = () => import('@/views/teacher/lesson-preparation-module/TeachingPlans.vue')
const Scores = () => import('@/views/teacher/student-management-module/Scores.vue')
// 新增组件
const LessonDesigner = () => import('@/views/teacher/lesson-preparation-module/LessonDesigner.vue')
const LessonPlanGenerator = () => import('@/views/teacher/lesson-preparation-module/LessonPlanGenerator.vue')
const OutlineImport = () => import('@/views/teacher/lesson-preparation-module/OutlineImport.vue')

// 教师路由配置
const teacherRoutes = [
    // 仪表盘模块
    {
        path: 'dashboard',
        name: 'TeacherDashboard',
        component: TeacherDashboard,
        meta: {
            title: '教师工作台',
            icon: 'el-icon-data-board',
        }
    },

    // 备课系统模块
    {
        path: 'lesson-preparation-module/workbench',
        name: 'LessonPrepWorkbench',
        component: LessonPrepWorkbench,
        meta: {
            title: '智能备课工作台',
            icon: 'el-icon-cpu',
        }
    },
    {
        path: 'lesson-preparation-module/plans',
        name: 'TeachingPlans',
        component: TeachingPlans,
        meta: {
            title: '教学计划',
            icon: 'el-icon-notebook-1'
        }
    },
    {
        path: 'lesson-preparation-module/designer',
        name: 'LessonDesigner',
        component: LessonDesigner,
        meta: {
            title: '课程设计器',
            icon: 'el-icon-edit-outline',
            hidden: true
        }
    },
    {
        path: 'lesson-preparation-module/generator',
        name: 'LessonPlanGenerator',
        component: LessonPlanGenerator,
        meta: {
            title: '教案生成器',
            icon: 'el-icon-magic-stick',
            hidden: true
        }
    },
    {
        path: 'lesson-preparation-module/outline-import',
        name: 'OutlineImport',
        component: OutlineImport,
        meta: {
            title: '教学大纲导入',
            icon: 'el-icon-upload',
            hidden: true
        }
    },

    // 考试评估模块
    {
        path: 'exam-assessment-module/exams',
        name: 'Exams',
        component: Exams,
        meta: {
            title: '考试管理',
            icon: 'el-icon-edit'
        }
    },
    {
        path: 'exam-assessment-module/exam/:id',
        name: 'ExamEditor',
        component: ExamEditor,
        props: true,
        meta: {
            title: '编辑考试',
            hidden: true
        }
    },
    {
        path: 'exam-assessment-module/result/:id',
        name: 'ExamResult',
        component: ExamResult,
        props: true,
        meta: {
            title: '考试统计',
            hidden: true
        }
    },
    {
        path: 'exam-assessment-module/question-bank',
        name: 'QuestionBank',
        component: QuestionBank,
        meta: {
            title: '题库管理',
            icon: 'el-icon-collection',
        }
    },
    {
        path: 'exam-assessment-module/question-generator',
        name: 'QuestionGenerator',
        component: QuestionGenerator,
        meta: {
            title: '题目生成',
            icon: 'el-icon-magic-stick',
        }
    },

    // 分析模块
    {
        path: 'analytics-module/analysis',
        name: 'Analysis',
        component: Analysis,
        meta: {
            title: '学情分析',
            icon: 'el-icon-data-analysis',
        }
    },
    {
        path: 'analytics-module/auto-grader',
        name: 'AutoGrader',
        component: AutoGrader,
        meta: {
            title: '智能评分',
            icon: 'el-icon-check',
        }
    },

    // 学生管理模块
    {
        path: 'student-management-module/students',
        name: 'StudentManagement',
        component: StudentManagement,
        meta: {
            title: '学生管理',
            icon: 'el-icon-user-solid'
        }
    },
    {
        path: 'student-management-module/scores',
        name: 'Scores',
        component: Scores,
        meta: {
            title: '成绩管理',
            icon: 'el-icon-tickets'
        }
    },

    // AI助教模块
    {
        path: 'ai-assistant-module/assistant',
        name: 'AiAssistant',
        component: AiAssistant,
        meta: {
            title: 'AI助教',
            icon: 'el-icon-chat-dot-round'
        }
    },

    // 旧的路径重定向到新的模块化路径
    {
        path: 'lesson-prep-workbench',
        redirect: '/teacher/lesson-preparation-module/workbench',
        meta: { hidden: true }
    },
    {
        path: 'question-generator',
        redirect: '/teacher/exam-assessment-module/question-generator',
        meta: { hidden: true }
    },
    {
        path: 'auto-grader',
        redirect: '/teacher/analytics-module/auto-grader',
        meta: { hidden: true }
    },
    {
        path: 'analysis',
        redirect: '/teacher/analytics-module/analysis',
        meta: { hidden: true }
    },
    {
        path: 'question-bank',
        redirect: '/teacher/exam-assessment-module/question-bank',
        meta: { hidden: true }
    },
    {
        path: 'teaching-plans',
        redirect: '/teacher/lesson-preparation-module/plans',
        meta: { hidden: true }
    },
    {
        path: 'exams',
        redirect: '/teacher/exam-assessment-module/exams',
        meta: { hidden: true }
    },
    {
        path: 'scores',
        redirect: '/teacher/student-management-module/scores',
        meta: { hidden: true }
    },
    {
        path: 'ai-assistant',
        redirect: '/teacher/ai-assistant-module/assistant',
        meta: { hidden: true }
    },
    {
        path: 'students',
        redirect: '/teacher/student-management-module/students',
        meta: { hidden: true }
    },
    {
        path: 'lesson-designer',
        redirect: '/teacher/lesson-preparation-module/designer',
        meta: { hidden: true }
    },
    {
        path: 'lesson-plan-generator',
        redirect: '/teacher/lesson-preparation-module/generator',
        meta: { hidden: true }
    },
    {
        path: 'outline-import',
        redirect: '/teacher/lesson-preparation-module/outline-import',
        meta: { hidden: true }
    },
];

export default teacherRoutes;