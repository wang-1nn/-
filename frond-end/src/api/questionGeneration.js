import { post, get } from '@/net'

// 获取正确的API URL
const getApiUrl = (path) => {
  const baseURL = 'http://localhost:8080'
  return path.startsWith('http') ? path : `${baseURL}${path}`
}

// 转换难度等级为后端枚举
const convertDifficultyToLevel = (difficulty) => {
  switch (difficulty) {
    case 1:
    case 2:
      return 'EASY'
    case 3:
      return 'MEDIUM'
    case 4:
    case 5:
      return 'HARD'
    default:
      return 'MEDIUM'
  }
}

// 转换题型为后端枚举
const convertTypeToEnum = (type) => {
  switch (type) {
    case 'single':
    case 'multiple':
      return 'CHOICE'
    case 'blank':
      return 'FILL'
    case 'true_false':
      return 'JUDGE'
    case 'shortAnswer':
    case 'short':
      return 'SHORT'
    default:
      return 'CHOICE'
  }
}

/**
 * 题目生成相关API
 */
export const questionGenerationAPI = {

  /**
   * 生成题目（流式响应）- 使用现有的TeacherController接口
   * @param {Object} requestData 生成请求数据
   * @param {Function} onChunk 数据块处理函数
   * @param {Function} onComplete 完成回调
   * @param {Function} onError 错误回调
   */
  async generateQuestions(requestData, onChunk, onComplete, onError) {
    try {
      // 转换为QGRequest格式
      const qgRequest = {
        subject: requestData.topic || '通用学科', // 学科
        knowledgePoints: [requestData.topic], // 知识点数组
        level: convertDifficultyToLevel(requestData.difficulty), // 难度枚举
        type: convertTypeToEnum(requestData.selectedTypes[0]), // 题型枚举（取第一个）
        count: requestData.count || 5 // 题目数量
      }

      console.log('转换后的QGRequest:', qgRequest)

      // 获取认证token
      const token = localStorage.getItem('authToken')
      const headers = {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      }

      // 使用现有的生成接口
      const fullUrl = getApiUrl('/api/teacher/Ai/GenerateQuestions')

      const response = await fetch(fullUrl, {
        method: 'POST',
        headers: headers,
        credentials: 'include',
        body: JSON.stringify(qgRequest)
      })

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let aiResponse = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        const chunk = decoder.decode(value)
        aiResponse += chunk

        // 调用数据块处理函数
        if (onChunk) {
          onChunk(chunk)
        }
      }

      // 调用完成回调
      if (onComplete) {
        onComplete(aiResponse)
      }

      return aiResponse

    } catch (error) {
      console.error('题目生成失败:', error)
      if (onError) {
        onError(error)
      }
      throw error
    }
  },

  /**
   * 解析并保存生成的题目（现在题目在生成时已自动保存，此方法保持兼容性）
   * @param {string} aiResponse AI响应内容
   * @param {Object} originalRequest 原始请求数据
   * @returns {Promise}
   */
  parseAndSaveQuestions(aiResponse, originalRequest) {
    return new Promise((resolve, reject) => {
      // 现在题目在生成时已经自动保存到数据库
      // 这里只需要返回成功状态，保持前端逻辑兼容
      console.log('题目已在生成时自动保存到数据库')
      resolve({
        message: '题目已保存',
        data: [] // 返回空数组，因为具体题目数据需要通过getAllQuestions获取
      })
    })
  },

  /**
   * 批量保存题目到题库（现在题目在生成时已自动保存，此方法保持兼容性）
   * @param {Array} questions 题目列表
   * @returns {Promise}
   */
  batchSaveToBank(questions) {
    return new Promise((resolve, reject) => {
      // 现在题目在生成时已经自动保存到数据库
      console.log('题目已在生成时自动保存到数据库')
      resolve({
        message: '题目已保存',
        data: questions
      })
    })
  },

  /**
   * 获取所有题目
   * @returns {Promise}
   */
  getAllQuestions() {
    return new Promise((resolve, reject) => {
      get('/api/teacher/Ai/Questions', null,
        (message, data) => {
          resolve({ message, data })
        },
        (message) => {
          reject(new Error(message))
        },
        (error) => {
          reject(error)
        }
      )
    })
  },

  /**
   * 根据批次ID获取题目
   * @param {string} batchId 批次ID
   * @returns {Promise}
   */
  getQuestionsByBatch(batchId) {
    return new Promise((resolve, reject) => {
      get('/api/teacher/Ai/GetQuestionById', { questionId: batchId },
        (message, data) => {
          resolve({ message, data })
        },
        (message) => {
          reject(new Error(message))
        },
        (error) => {
          reject(error)
        }
      )
    })
  },

  /**
   * 获取批次列表
   * @returns {Promise}
   */
  getBatches() {
    return new Promise((resolve, reject) => {
      get('/api/teacher/Ai/Batches', null,
        (message, data) => {
          resolve({ message, data })
        },
        (message) => {
          reject(new Error(message))
        },
        (error) => {
          reject(error)
        }
      )
    })
  },

  /**
   * 根据题型筛选题目
   * @param {string} questionType 题型
   * @returns {Promise}
   */
  /**
   * 删除题目
   * @param {number} questionId 题目ID
   * @returns {Promise}
   */
  deleteQuestion(questionId) {
    return new Promise((resolve, reject) => {
      post(`/api/teacher/Ai/Questions/${questionId}/delete`, {},
        (message, data) => {
          resolve({ message, data })
        },
        (message) => {
          reject(new Error(message))
        },
        (error) => {
          reject(error)
        }
      )
    })
  }
}

export default questionGenerationAPI
