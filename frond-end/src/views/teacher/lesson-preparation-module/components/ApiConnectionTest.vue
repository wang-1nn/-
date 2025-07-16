<template>
  <div class="api-test p-6 bg-white rounded-lg shadow">
    <h3 class="text-lg font-semibold mb-4">API连接测试</h3>

    <div class="space-y-4">
      <!-- 基础连接测试 -->
      <div class="test-item">
        <h4 class="font-medium mb-2">1. 基础连接测试</h4>
        <el-button @click="testBasicConnection" :loading="loading.basic" type="primary">
          测试后端连接
        </el-button>
        <div v-if="results.basic" class="mt-2 p-2 bg-gray-100 rounded text-sm">
          {{ results.basic }}
        </div>
      </div>

      <!-- AI接口测试 -->
      <div class="test-item">
        <h4 class="font-medium mb-2">2. AI接口URL测试</h4>
        <el-button @click="testAiEndpoints" :loading="loading.ai" type="success">
          测试AI接口
        </el-button>
        <div v-if="results.ai" class="mt-2 p-2 bg-gray-100 rounded text-sm">
          {{ results.ai }}
        </div>
      </div>

      <!-- 文件上传测试 -->
      <div class="test-item">
        <h4 class="font-medium mb-2">3. 文件上传测试</h4>
        <input ref="fileInput" type="file" accept=".pdf,.doc,.docx" @change="handleFileSelect" class="mb-2">
        <br>
        <el-button @click="testFileUpload" :loading="loading.upload" type="warning" :disabled="!selectedFile">
          测试文件上传
        </el-button>
        <div v-if="results.upload" class="mt-2 p-2 bg-gray-100 rounded text-sm">
          {{ results.upload }}
        </div>
      </div>

      <!-- 清空结果 -->
      <div class="mt-6">
        <el-button @click="clearResults" type="default">清空测试结果</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { getApiUrl, authenticatedFetch, aiAPI } from '@/utils/apiUtils.js';

// 状态管理
const loading = reactive({
  basic: false,
  ai: false,
  upload: false
});

const results = reactive({
  basic: '',
  ai: '',
  upload: ''
});

const selectedFile = ref(null);
const fileInput = ref(null);

// 处理文件选择
const handleFileSelect = (event) => {
  const file = event.target.files[0];
  if (file) {
    selectedFile.value = file;
    ElMessage.info(`已选择文件: ${file.name}`);
  }
};

// 测试基础连接
const testBasicConnection = async () => {
  loading.basic = true;
  try {
    // 测试一个简单的GET请求
    const response = await authenticatedFetch('/api/teacher/dashboard/summary?uid=1');

    if (response.ok) {
      results.basic = `✅ 连接成功! 状态码: ${response.status}`;
      ElMessage.success('后端连接正常');
    } else {
      results.basic = `❌ 连接失败! 状态码: ${response.status}`;
      ElMessage.error('后端连接失败');
    }
  } catch (error) {
    results.basic = `❌ 连接错误: ${error.message}`;
    ElMessage.error('连接错误: ' + error.message);
  } finally {
    loading.basic = false;
  }
};

// 测试AI接口
const testAiEndpoints = async () => {
  loading.ai = true;
  try {
    // 测试AI接口的URL构建
    const optimizeUrl = getApiUrl('/api/teacher/Ai/OptimizateOutline');
    const lessonUrl = getApiUrl('/api/teacher/Ai/CreateLesson?id=test');

    results.ai = `✅ URL构建成功!\n优化大纲: ${optimizeUrl}\n生成教案: ${lessonUrl}`;

    // 尝试发送一个简单的请求测试连通性
    try {
      const response = await authenticatedFetch('/api/teacher/Ai/OptimizateOutline', {
        method: 'POST',
        body: new FormData() // 空的FormData用于测试
      });

      results.ai += `\n连通性测试: 状态码 ${response.status}`;
    } catch (fetchError) {
      results.ai += `\n连通性测试: ${fetchError.message}`;
    }

    ElMessage.success('AI接口URL测试完成');
  } catch (error) {
    results.ai = `❌ AI接口测试失败: ${error.message}`;
    ElMessage.error('AI接口测试失败');
  } finally {
    loading.ai = false;
  }
};

// 测试文件上传
const testFileUpload = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择一个文件');
    return;
  }

  loading.upload = true;
  try {
    // 创建FormData
    const formData = new FormData();
    formData.append('message', '测试文件上传');
    formData.append('files', selectedFile.value);
    formData.append('conversationId', 'test-123');

    results.upload = `开始测试文件上传...\n文件名: ${selectedFile.value.name}\n文件大小: ${selectedFile.value.size} bytes`;

    // 使用AI API工具测试
    await aiAPI.optimizeOutline(
        formData,
        // onChunk
        (data) => {
          results.upload += `\n收到数据块: ${JSON.stringify(data)}`;
        },
        // onComplete
        () => {
          results.upload += '\n✅ 文件上传测试完成';
          ElMessage.success('文件上传测试完成');
        },
        // onError
        (error) => {
          results.upload += `\n❌ 上传错误: ${error.message}`;
          ElMessage.error('文件上传测试失败');
        }
    );

  } catch (error) {
    results.upload = `❌ 文件上传测试失败: ${error.message}`;
    ElMessage.error('文件上传测试失败');
  } finally {
    loading.upload = false;
  }
};

// 清空测试结果
const clearResults = () => {
  Object.keys(results).forEach(key => {
    results[key] = '';
  });
  selectedFile.value = null;
  if (fileInput.value) {
    fileInput.value.value = '';
  }
  ElMessage.info('测试结果已清空');
};
</script>

<style scoped>
.test-item {
  padding: 1rem;
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  background-color: #f9fafb;
}

.test-item h4 {
  color: #374151;
}
</style>
