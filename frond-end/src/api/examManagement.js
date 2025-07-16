import { get, post } from '@/net'

const API_BASE = '/api/teacher'

/**
 * 获取教师的考试列表
 * @param {string} uid - 教师ID
 * @param {string} status - 考试状态（可选）：即将开始、进行中、已结束、草稿
 * @param {string} keyword - 搜索关键字（可选）
 * @param {number} page - 页码
 * @param {number} size - 每页大小
 * @param {Function} success - 成功回调
 * @param {Function} failure - 失败回调
 */
export function getTeacherExams(uid, status, keyword, page = 1, size = 10, success, failure) {
  const params = { uid, page, size }
  if (status) {
    params.status = status
  }
  if (keyword) {
    params.keyword = keyword
  }
  get(`${API_BASE}/exams`, params, success, failure)
}

/**
 * 获取题目列表供选择
 * @param {string} uid - 教师ID
 * @param {string} subject - 学科（必填）
 * @param {number} page - 页码
 * @param {number} size - 每页大小
 * @param {Function} success - 成功回调
 * @param {Function} failure - 失败回调
 */
export function getQuestionsForSelection(uid, subject, page = 1, size = 20, success, failure) {
  if (!subject || subject.trim() === '') {
    failure && failure('科目不能为空');
    return;
  }

  const params = { uid, subject: subject.trim(), page, size }
  get(`${API_BASE}/exams/questions`, params, success, failure)
}

/**
 * 获取教师所教的课程和班级
 * @param {string} uid - 教师ID
 * @param {Function} success - 成功回调
 * @param {Function} failure - 失败回调
 */
export function getTeacherCoursesAndClasses(uid, success, failure) {
  get(`${API_BASE}/exams/courses-classes`, { uid }, success, failure)
}

/**
 * 创建考试
 * @param {string} uid - 教师ID
 * @param {Object} examData - 考试数据
 * @param {Function} success - 成功回调
 * @param {Function} failure - 失败回调
 */
export function createExam(uid, examData, success, failure) {
  post(`${API_BASE}/exams?uid=${uid}`, examData, success, failure)
}

/**
 * 获取考试详情（包含题目信息）
 * @param {number} examId - 考试ID
 * @param {string} uid - 教师ID
 * @param {Function} success - 成功回调
 * @param {Function} failure - 失败回调
 */
export function getExamDetail(examId, uid, success, failure) {
  get(`${API_BASE}/exams/${examId}`, { uid }, success, failure)
}

/**
 * 删除考试
 * @param {number} examId - 考试ID
 * @param {string} uid - 教师ID
 * @param {Function} success - 成功回调
 * @param {Function} failure - 失败回调
 */
export function deleteExam(examId, uid, success, failure) {
  post(`${API_BASE}/exams/${examId}/delete?uid=${uid}`, {}, success, failure)
}

/**
 * 更新考试
 * @param {number} examId - 考试ID
 * @param {string} uid - 教师ID
 * @param {Object} examData - 考试数据
 * @param {Function} success - 成功回调
 * @param {Function} failure - 失败回调
 */
export function updateExam(examId, uid, examData, success, failure) {
  post(`${API_BASE}/exams/${examId}?uid=${uid}`, examData, success, failure)
}

/**
 * 获取教师创建的题目的所有学科列表
 * @param {string} uid - 教师ID
 * @param {Function} success - 成功回调
 * @param {Function} failure - 失败回调
 */
export function getQuestionSubjects(uid, success, failure) {
  get(`${API_BASE}/exams/question-subjects`, { uid }, success, failure)
}
