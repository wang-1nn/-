# 快速诊断和修复指南

## 🎯 当前问题
前端调用 `/api/teacher/question-generator/parse-and-save` 返回500错误

## 🔍 诊断步骤

### 1. 测试基础功能
```bash
# 测试1: 简单功能测试
curl -X POST http://localhost:8080/api/teacher/question-generator/test-simple \
  -H "Content-Type: application/json"

# 测试2: 解析保存测试
curl -X POST http://localhost:8080/api/teacher/question-generator/test-parse-save \
  -H "Content-Type: application/json"
```

### 2. 检查后端日志
启动后端服务后，观察控制台输出：
```
INFO  - 开始简单测试
INFO  - 测试请求: QuestionGenerationRequest(...)
INFO  - 测试AI响应: [{"type":"single"...}]
DEBUG - 开始解析AI响应，响应长度: 234
```

### 3. 检查数据库连接
```sql
-- 检查questions表是否存在
SHOW TABLES LIKE 'questions';

-- 检查表结构
DESCRIBE questions;

-- 查看最新的题目记录
SELECT * FROM questions ORDER BY created_at DESC LIMIT 5;
```

## 🔧 可能的问题和解决方案

### 问题1: 数据库连接问题
**症状**: 日志显示数据库相关错误
**解决**: 检查application.yml中的数据库配置

### 问题2: JSON解析问题
**症状**: JsonParseException或类型转换错误
**解决**: 
```java
// 已添加安全的类型转换方法
private String getStringFromMap(Map<String, Object> map, String key)
private Integer getIntegerFromMap(Map<String, Object> map, String key)
private List<String> getListFromMap(Map<String, Object> map, String key)
```

### 问题3: 服务注入问题
**症状**: NullPointerException或NoSuchBeanDefinitionException
**解决**: 检查@Autowired注解和组件扫描

### 问题4: 前端数据格式问题
**症状**: 参数解析失败
**解决**: 检查前端发送的数据结构

## 📊 前端发送的数据格式
```javascript
{
  "aiResponse": "AI生成的响应内容",
  "originalRequest": {
    "topic": "java面向对象",
    "selectedTypes": ["single", "multiple"],
    "difficulty": 3,
    "count": 5,
    "requirements": "额外要求"
  }
}
```

## 🚀 修复后的改进

### 1. 增强的错误处理
```java
@PostMapping("/question-generator/parse-and-save")
public RestBean<List<GeneratedQuestionDTO>> parseAndSaveQuestions(
        @RequestBody Map<String, Object> requestData,
        HttpServletRequest httpRequest) {
    try {
        log.info("收到解析保存请求，数据: {}", requestData);
        
        // 验证必要参数
        if (requestData == null) {
            return RestBean.failure(400, "请求数据不能为空");
        }
        
        // 安全的数据提取和转换
        // ...
        
    } catch (Exception e) {
        log.error("解析保存题目失败", e);
        return RestBean.failure(500, "解析保存题目失败：" + e.getMessage());
    }
}
```

### 2. 安全的类型转换
```java
// 防止ClassCastException
private String getStringFromMap(Map<String, Object> map, String key) {
    Object value = map.get(key);
    return value != null ? value.toString() : null;
}
```

### 3. 详细的日志记录
```java
log.info("收到解析保存请求，数据: {}", requestData);
log.info("解析请求对象: {}", request);
log.info("成功解析并保存{}道题目", questions.size());
```

## 🧪 测试流程

### 1. 重启后端服务
```bash
cd back-end
mvn spring-boot:run
```

### 2. 运行诊断测试
```bash
# 基础功能测试
curl -X POST http://localhost:8080/api/teacher/question-generator/test-simple

# 解析保存测试
curl -X POST http://localhost:8080/api/teacher/question-generator/test-parse-save
```

### 3. 前端测试
1. 访问题目生成页面
2. 填写参数：
   - 知识点：java面向对象
   - 题目数量：2
   - 难度：3
3. 点击生成题目
4. 观察前端控制台和后端日志

### 4. 验证结果
```sql
-- 检查是否有新的题目记录
SELECT question_id, content, question_type, difficulty, created_by, is_ai_generated 
FROM questions 
WHERE is_ai_generated = true 
ORDER BY created_at DESC 
LIMIT 10;
```

## 📋 检查清单

- [ ] 后端服务正常启动
- [ ] 数据库连接正常
- [ ] 测试端点返回成功
- [ ] 日志显示详细信息
- [ ] 前端能正常调用接口
- [ ] 题目正确保存到数据库
- [ ] 前端页面显示生成的题目

## 🔧 如果仍有问题

### 查看完整错误堆栈
在application.yml中添加：
```yaml
logging:
  level:
    org.example.backend: DEBUG
    org.springframework.web: DEBUG
```

### 检查具体错误
1. 查看后端控制台的完整错误信息
2. 检查前端Network标签的请求详情
3. 验证数据库表结构和权限

### 联系支持
提供以下信息：
1. 完整的错误日志
2. 前端发送的请求数据
3. 数据库表结构
4. 应用配置信息

修复完成后，题目生成和保存功能应该能够正常工作！
