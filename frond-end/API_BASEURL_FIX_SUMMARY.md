# API BaseURL修复总结

## 🎯 问题描述

前端API调用发送到了错误的端口（5173而不是8080），这是因为使用了原生fetch API而没有使用项目配置的axios baseURL。

## 🔧 修复内容

### 1. questionGeneration.js
**修复前：**
```javascript
const response = await fetch('/api/teacher/question-generator/generate', {
  method: 'POST',
  headers: headers,
  credentials: 'include',
  body: JSON.stringify(requestData)
})
```

**修复后：**
```javascript
// 添加工具函数
const getApiUrl = (path) => {
  const baseURL = 'http://localhost:8080'
  return path.startsWith('http') ? path : `${baseURL}${path}`
}

// 使用正确的baseURL
const fullUrl = getApiUrl('/api/teacher/question-generator/generate')
const response = await fetch(fullUrl, {
  method: 'POST',
  headers: headers,
  credentials: 'include',
  body: JSON.stringify(requestData)
})
```

### 2. QuestionBank.vue（teacher/exam-assessment-module）
**修复前：**
```javascript
const res = await fetch('/api/teacher/Ai/GenerateQuestions', {
  method: 'POST',
  headers: sseHeaders,
  body: JSON.stringify(payload)
});
```

**修复后：**
```javascript
const res = await fetch('http://localhost:8080/api/teacher/Ai/GenerateQuestions', {
  method: 'POST',
  headers: sseHeaders,
  body: JSON.stringify(payload)
});
```

### 3. QuestionBank.vue（question目录）
**修复前：**
```javascript
const res = await fetch('/api/teacher/Ai/GenerateQuestions', {
  method: 'POST',
  headers: sseHeaders,
  body: JSON.stringify(payload)
});
```

**修复后：**
```javascript
const res = await fetch('http://localhost:8080/api/teacher/Ai/GenerateQuestions', {
  method: 'POST',
  headers: sseHeaders,
  body: JSON.stringify(payload)
});
```

### 4. LessonPrepWorkbench.vue
**修复前：**
```javascript
fetch('/api/teacher/Ai/OptimizateOutline', {
  method: 'POST',
  headers,
  body: formData
})
```

**修复后：**
```javascript
fetch('http://localhost:8080/api/teacher/Ai/OptimizateOutline', {
  method: 'POST',
  headers,
  body: formData
})
```

## ✅ 已确认正确的API调用

### 使用您的net方法的接口（自动使用正确baseURL）
以下接口使用了您的get/post方法，会自动使用axios配置的baseURL：

1. **QuestionBank.vue中的管理功能**
   - `get('/api/teacher/Ai/Questions')` - 获取所有题目
   - `get('/api/teacher/Ai/GetQuestionById')` - 根据批次获取题目
   - `get('/api/teacher/Ai/Batches')` - 获取批次列表
   - `post('/api/teacher/Ai/Questions/{id}/delete')` - 删除题目

2. **questionGeneration.js中的其他方法**
   - `post('/api/teacher/question-generator/parse-and-save')` - 解析保存题目
   - `post('/api/teacher/question-bank/batch-save')` - 批量保存
   - 其他所有get/post调用

## 🔍 验证方法

### 1. 检查网络请求
打开浏览器开发者工具 → Network标签，确认请求发送到：
- ✅ `http://localhost:8080/api/teacher/question-generator/generate`
- ❌ ~~`http://localhost:5173/api/teacher/question-generator/generate`~~

### 2. 测试流程
1. 启动后端服务（端口8080）
2. 启动前端服务（端口5173）
3. 访问题目生成页面
4. 点击生成题目按钮
5. 检查Network标签中的请求URL

## 📊 修复状态

| 文件 | 方法 | 状态 | 说明 |
|------|------|------|------|
| questionGeneration.js | generateQuestions | ✅ 已修复 | 使用getApiUrl工具函数 |
| QuestionBank.vue (teacher) | 流式生成 | ✅ 已修复 | 直接使用完整URL |
| QuestionBank.vue (question) | 流式生成 | ✅ 已修复 | 直接使用完整URL |
| LessonPrepWorkbench.vue | 优化大纲 | ✅ 已修复 | 直接使用完整URL |
| 其他get/post调用 | 所有方法 | ✅ 正确 | 使用您的net方法 |

## 🎯 为什么需要特殊处理流式响应

### 原因
1. **流式响应（SSE）**需要使用原生fetch API
2. **您的net方法**基于axios，不支持流式响应
3. **axios的baseURL配置**不会自动应用到fetch调用

### 解决方案
对于流式响应，我们：
1. 使用原生fetch API
2. 手动添加完整的URL（包含baseURL）
3. 保持与您的net方法相同的认证机制

## 🔧 最佳实践

### 1. 普通API调用
```javascript
import { get, post } from '@/net'

// 推荐：使用您的net方法
get('/api/teacher/endpoint', params, successCallback, failureCallback)
post('/api/teacher/endpoint', data, successCallback, failureCallback)
```

### 2. 流式响应API调用
```javascript
// 必须使用fetch，但要手动添加baseURL
const response = await fetch('http://localhost:8080/api/teacher/stream-endpoint', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': localStorage.getItem('authToken') ? `Bearer ${localStorage.getItem('authToken')}` : ''
  },
  credentials: 'include',
  body: JSON.stringify(data)
})
```

## 🚀 测试建议

1. **清除浏览器缓存**，确保使用最新代码
2. **重启前后端服务**，确保配置生效
3. **检查Network标签**，确认请求URL正确
4. **测试完整流程**，从生成到保存

修复完成后，所有API调用都应该正确发送到后端的8080端口！
