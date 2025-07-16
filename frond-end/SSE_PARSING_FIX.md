# SSE数据解析修复

## 🎯 问题分析

后端返回的是SSE（Server-Sent Events）格式的数据，以`data:`开头，但前端解析函数在查找`message`开头的行，导致无法正确解析题目数据。

## 📊 数据格式对比

### 后端实际返回的数据
```
data:{"question":"在网络层中，主要负责将数据包从源主机传送到目的主机的是哪种协议？","options":["A. TCP","B. IP","C. UDP","D. HTTP"],"answer":"B","explain":"网络层的主要功能是负责数据包的寻址和转发，IP协议正是实现这一功能的核心协议。","questionId":"6e5a9853-9ee5-404c-8cda-5a5c3386e976"}

data:{"question":"在网络层中，主要负责数据包的选择和转发的是哪一层？","options":["A. 物理层","B. 数据链路层","C. 网络层","D. 传输层"],"answer":"C","explain":"网络层负责将数据包从源地址传送到目的地址，进行路由选择和转发.","questionId":"6e5a9853-9ee5-404c-8cda-5a5c3386e976"}
```

### 前端原来查找的格式
```javascript
if (line.trim().startsWith('message')) {
  // 这里查找的是message开头，但实际是data:开头
}
```

## ✅ 修复内容

### 1. 更新解析逻辑
```javascript
const parseAndDisplayQuestions = (chunk, generatedQuestions) => {
  try {
    console.log('收到chunk数据:', chunk)
    
    // 解析chunk中的SSE数据
    const lines = chunk.split('\n')
    
    for (const line of lines) {
      const trimmedLine = line.trim()
      
      // 处理SSE格式的data:行
      if (trimmedLine.startsWith('data:')) {
        // 提取data:后面的JSON部分
        const jsonStr = trimmedLine.substring(5).trim() // 移除"data:"前缀
        
        if (jsonStr && jsonStr.startsWith('{')) {
          try {
            const questionData = JSON.parse(jsonStr)
            console.log('解析到题目数据:', questionData)
            
            // 转换为前端格式并显示
            // ...
          } catch (parseError) {
            console.warn('解析题目JSON失败:', parseError, jsonStr)
          }
        }
      }
    }
  } catch (error) {
    console.error('解析chunk失败:', error)
  }
}
```

### 2. 添加详细日志
- `console.log('收到chunk数据:', chunk)` - 显示原始数据
- `console.log('解析到题目数据:', questionData)` - 显示解析结果
- `console.log('新增题目:', formattedQuestion.content)` - 显示添加的题目
- `console.log('当前题目总数:', questions.value.length)` - 显示总数

### 3. 清空之前的题目
```javascript
// 清空之前的题目列表
questions.value = []
```

### 4. 重复检测
```javascript
// 检查是否已存在相同题目（避免重复）
const exists = generatedQuestions.some(q => q.content === formattedQuestion.content)
if (!exists) {
  generatedQuestions.push(formattedQuestion)
  questions.value.push(formattedQuestion)
  console.log('新增题目:', formattedQuestion.content)
} else {
  console.log('题目已存在，跳过:', questionData.question)
}
```

## 🔧 数据转换流程

### 1. 接收SSE数据
```
data:{"question":"...","options":["A. TCP","B. IP"],"answer":"B","explain":"...","questionId":"..."}
```

### 2. 提取JSON
```javascript
const jsonStr = trimmedLine.substring(5).trim() // 移除"data:"
// 结果: {"question":"...","options":["A. TCP","B. IP"],"answer":"B","explain":"...","questionId":"..."}
```

### 3. 解析JSON
```javascript
const questionData = JSON.parse(jsonStr)
// 结果: {question: "...", options: ["A. TCP", "B. IP"], answer: "B", explain: "...", questionId: "..."}
```

### 4. 转换为前端格式
```javascript
const formattedQuestion = {
  id: questionData.questionId,
  type: 'single',
  content: questionData.question,
  answer: questionData.answer,
  analysis: questionData.explain,
  difficulty: generationConfig.difficulty,
  topic: generationConfig.topic,
  selected: false,
  isEditing: false,
  options: questionData.options.map((option) => ({
    key: option.charAt(0), // "A"
    value: option.substring(3) // "TCP"
  }))
}
```

### 5. 实时显示
```javascript
questions.value.push(formattedQuestion)
```

## 🧪 测试验证

### 1. 打开浏览器控制台
查看以下日志输出：
```
收到chunk数据: data:{"question":"..."}
解析到题目数据: {question: "...", options: [...], ...}
新增题目: 在网络层中，主要负责将数据包从源主机传送到目的主机的是哪种协议？
当前题目总数: 1
```

### 2. 观察页面效果
- 题目应该在生成过程中逐个出现
- 每个题目包含完整的内容、选项、答案、解析
- 不应该有重复题目

### 3. 验证数据格式
检查题目显示格式：
```
题目：在网络层中，主要负责将数据包从源主机传送到目的主机的是哪种协议？
A. TCP
B. IP
C. UDP  
D. HTTP
答案：B
解析：网络层的主要功能是负责数据包的寻址和转发，IP协议正是实现这一功能的核心协议。
```

## 📋 预期结果

### 生成过程
1. 点击"生成题目"按钮
2. 清空之前的题目列表
3. 开始接收SSE数据流
4. 实时解析并显示题目
5. 生成完成后显示成功消息

### 控制台输出
```
收到chunk数据: data:{"question":"在网络层中..."}
解析到题目数据: {question: "在网络层中...", options: Array(4), answer: "B", explain: "网络层的主要功能...", questionId: "6e5a9853-9ee5-404c-8cda-5a5c3386e976"}
新增题目: 在网络层中，主要负责将数据包从源主机传送到目的主机的是哪种协议？
当前题目总数: 1
...
生成完成，完整响应: [完整响应内容]
生成的题目总数: 5
```

### 页面显示
- 题目在生成过程中逐个出现
- 每个题目格式正确
- 选项正确解析（A. TCP, B. IP等）
- 答案和解析正确显示

现在前端应该能够正确解析和显示SSE格式的题目数据了！
