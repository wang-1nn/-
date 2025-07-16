# 题目生成API接入指南

## 概述

本指南说明如何在前端使用您的net封装的post和get请求方法来接入题目生成后端API。

## 已完成的接入

### 1. QuestionGenerator.vue（题目生成页面）

#### 功能接入状态
- ✅ **题目生成**：使用流式响应生成题目
- ✅ **题目解析保存**：解析AI响应并保存到数据库
- ✅ **批量保存到题库**：将生成的题目保存到题库

#### 使用的API接口
```javascript
// 1. 题目生成（流式响应）
POST /api/teacher/question-generator/generate

// 2. 解析并保存题目
POST /api/teacher/question-generator/parse-and-save

// 3. 批量保存到题库
POST /api/teacher/question-bank/batch-save
```

#### 代码示例
```javascript
import { questionGenerationAPI } from '@/api/questionGeneration'

// 生成题目
const generateQuestions = async () => {
  const requestData = {
    topic: '数据结构',
    selectedTypes: ['single', 'blank'],
    difficulty: 3,
    count: 5,
    requirements: '注重实际应用'
  }

  try {
    // 流式生成
    const aiResponse = await questionGenerationAPI.generateQuestions(
      requestData,
      (chunk) => console.log('收到数据:', chunk),
      (fullResponse) => console.log('生成完成'),
      (error) => console.error('生成出错:', error)
    )

    // 解析保存
    const result = await questionGenerationAPI.parseAndSaveQuestions(aiResponse, requestData)
    console.log('保存成功:', result.data)

  } catch (error) {
    console.error('操作失败:', error)
  }
}
```

### 2. QuestionBank.vue（题库管理页面）

#### 功能接入状态
- ✅ **获取所有题目**：使用您的get方法
- ✅ **根据批次获取题目**：使用您的get方法
- ✅ **获取批次列表**：使用您的get方法
- ✅ **删除题目**：使用您的post方法
- ✅ **流式生成题目**：使用fetch处理SSE

#### 使用的API接口
```javascript
// 1. 获取所有题目
GET /api/teacher/Ai/Questions

// 2. 根据批次获取题目
GET /api/teacher/Ai/GetQuestionById?questionId={batchId}

// 3. 获取批次列表
GET /api/teacher/Ai/Batches

// 4. 删除题目
POST /api/teacher/Ai/Questions/{id}/delete

// 5. 流式生成题目
POST /api/teacher/Ai/GenerateQuestions
```

#### 代码示例
```javascript
import { get, post } from '@/net'

// 获取题目列表
const loadQuestionList = async () => {
  get('/api/teacher/Ai/Questions', null,
    (message, data) => {
      questionList.value = data
    },
    (failMsg) => {
      ElMessage.error(failMsg)
    },
    (error) => {
      ElMessage.error('获取题库数据失败')
    }
  )
}

// 删除题目
const deleteQuestion = (question) => {
  post(`/api/teacher/Ai/Questions/${question.id}/delete`, {},
    () => {
      ElMessage.success('删除成功')
      loadQuestionList() // 重新加载
    },
    (failMsg) => {
      ElMessage.error(failMsg || '删除失败')
    }
  )
}
```

## API工具类

### questionGenerationAPI

创建了统一的API工具类 `/src/api/questionGeneration.js`，封装了所有题目生成相关的API调用：

```javascript
import { questionGenerationAPI } from '@/api/questionGeneration'

// 所有方法都返回Promise，便于async/await使用
const methods = {
  generateQuestions,      // 流式生成题目
  parseAndSaveQuestions,  // 解析保存题目
  batchSaveToBank,       // 批量保存到题库
  getAllQuestions,       // 获取所有题目
  getQuestionsByBatch,   // 根据批次获取题目
  getBatches,           // 获取批次列表
  getQuestionsByType,   // 根据题型筛选
  getQuestionsByDifficulty, // 根据难度筛选
  deleteQuestion,       // 删除题目
  updateQuestion,       // 更新题目
  getStatistics        // 获取统计信息
}
```

## 认证处理

### Token管理
所有API调用都自动处理认证token：

```javascript
// 自动从localStorage获取token
const token = localStorage.getItem('authToken')
const headers = {
  'Authorization': token ? `Bearer ${token}` : ''
}
```

### 使用您的net方法
```javascript
import { post, get } from '@/net'

// post方法自动处理认证
post('/api/endpoint', data, successCallback, failureCallback, errorCallback)

// get方法自动处理认证
get('/api/endpoint', params, successCallback, failureCallback, errorCallback)
```

## 流式响应处理

### 题目生成流式响应
由于题目生成需要流式响应，使用fetch处理，但保持认证机制一致：

```javascript
const generateQuestions = async (requestData) => {
  // 使用localStorage中的token，保持与您的net方法一致
  const token = localStorage.getItem('authToken')
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': token ? `Bearer ${token}` : ''
  }

  const response = await fetch('/api/teacher/question-generator/generate', {
    method: 'POST',
    headers: headers,
    credentials: 'include', // 等同于withCredentials: true
    body: JSON.stringify(requestData)
  })

  // 处理流式响应
  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  
  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    
    const chunk = decoder.decode(value)
    // 处理数据块
  }
}
```

## 错误处理

### 统一错误处理模式
```javascript
// 使用您的net方法的错误处理模式
post('/api/endpoint', data,
  (message, data) => {
    // 成功回调
    console.log('成功:', message, data)
  },
  (message) => {
    // 失败回调（后端返回失败）
    ElMessage.error(message)
  },
  (error) => {
    // 错误回调（网络错误等）
    ElMessage.error('请求出错，请重试')
  }
)
```

### Promise封装
API工具类将您的回调模式封装为Promise：

```javascript
// 原始回调模式
post('/api/endpoint', data, success, failure, error)

// 封装为Promise
const apiCall = (data) => {
  return new Promise((resolve, reject) => {
    post('/api/endpoint', data,
      (message, data) => resolve({ message, data }),
      (message) => reject(new Error(message)),
      (error) => reject(error)
    )
  })
}
```

## 使用建议

### 1. 优先使用API工具类
```javascript
// 推荐：使用封装的API工具
import { questionGenerationAPI } from '@/api/questionGeneration'
const result = await questionGenerationAPI.getAllQuestions()

// 而不是直接调用
import { get } from '@/net'
get('/api/teacher/Ai/Questions', ...)
```

### 2. 错误处理
```javascript
try {
  const result = await questionGenerationAPI.getAllQuestions()
  // 处理成功结果
} catch (error) {
  ElMessage.error(`操作失败：${error.message}`)
}
```

### 3. 加载状态管理
```javascript
const loading = ref(false)

const loadData = async () => {
  loading.value = true
  try {
    const result = await questionGenerationAPI.getAllQuestions()
    // 处理数据
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}
```

## 总结

1. ✅ **完全兼容**：使用您的net封装的post和get方法
2. ✅ **认证一致**：保持与现有系统相同的token处理方式
3. ✅ **错误处理**：遵循您的错误处理模式
4. ✅ **流式响应**：特殊处理题目生成的流式响应需求
5. ✅ **API工具**：提供统一的API调用工具类
6. ✅ **向后兼容**：不影响现有代码，可以逐步迁移

所有题目生成相关功能已完全接入后端API，使用您的网络请求封装方法，确保与现有系统的一致性。
