<template>
  <div class="api-test-panel p-6 bg-white rounded-lg shadow">
    <h3 class="text-lg font-semibold mb-4">API接入测试面板</h3>

    <div class="space-y-4">
      <!-- 测试保存教案 -->
      <div class="test-section">
        <h4 class="font-medium mb-2">1. 测试保存教案</h4>
        <el-button @click="testSaveLessonPlan" :loading="loading.save" type="primary">
          测试保存教案
        </el-button>
        <div v-if="results.save" class="mt-2 p-2 bg-gray-100 rounded text-sm">
          {{ results.save }}
        </div>
      </div>

      <!-- 测试获取教案列表 -->
      <div class="test-section">
        <h4 class="font-medium mb-2">2. 测试获取教案列表</h4>
        <el-button @click="testGetLessonPlans" :loading="loading.list" type="success">
          测试获取列表
        </el-button>
        <div v-if="results.list" class="mt-2 p-2 bg-gray-100 rounded text-sm">
          {{ results.list }}
        </div>
      </div>

      <!-- 测试更新教案 -->
      <div class="test-section">
        <h4 class="font-medium mb-2">3. 测试更新教案</h4>
        <el-input v-model="testLessonId" placeholder="输入教案ID" class="mb-2" style="width: 200px" />
        <el-button @click="testUpdateLessonPlan" :loading="loading.update" type="warning">
          测试更新教案
        </el-button>
        <div v-if="results.update" class="mt-2 p-2 bg-gray-100 rounded text-sm">
          {{ results.update }}
        </div>
      </div>

      <!-- 测试删除教案 -->
      <div class="test-section">
        <h4 class="font-medium mb-2">4. 测试删除教案</h4>
        <el-input v-model="testDeleteId" placeholder="输入要删除的教案ID" class="mb-2" style="width: 200px" />
        <el-button @click="testDeleteLessonPlan" :loading="loading.delete" type="danger">
          测试删除教案
        </el-button>
        <div v-if="results.delete" class="mt-2 p-2 bg-gray-100 rounded text-sm">
          {{ results.delete }}
        </div>
      </div>

      <!-- 测试获取模板 -->
      <div class="test-section">
        <h4 class="font-medium mb-2">5. 测试获取模板</h4>
        <el-button @click="testGetTemplates" :loading="loading.templates" type="info">
          测试获取模板
        </el-button>
        <div v-if="results.templates" class="mt-2 p-2 bg-gray-100 rounded text-sm">
          {{ results.templates }}
        </div>
      </div>

      <!-- 测试获取推荐资源 -->
      <div class="test-section">
        <h4 class="font-medium mb-2">6. 测试获取推荐资源</h4>
        <el-button @click="testGetResources" :loading="loading.resources" type="primary">
          测试获取资源
        </el-button>
        <div v-if="results.resources" class="mt-2 p-2 bg-gray-100 rounded text-sm">
          {{ results.resources }}
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
import { ref, reactive, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/stores/counter.js';
import { lessonPlanAPI } from '@/api/lessonPlan.js';

// 用户信息
const authStore = useAuthStore();
const uid = computed(() => authStore.user?.id || authStore.user?.userId || '1');

// 状态管理
const loading = reactive({
  save: false,
  list: false,
  update: false,
  delete: false,
  templates: false,
  resources: false
});

const results = reactive({
  save: '',
  list: '',
  update: '',
  delete: '',
  templates: '',
  resources: ''
});

const testLessonId = ref('');
const testDeleteId = ref('');

// 测试保存教案
const testSaveLessonPlan = async () => {
  loading.save = true;
  try {
    const testLessonPlan = {
      title: '测试教案 - ' + new Date().toLocaleString(),
      subject: '数学',
      grade: '高一',
      content: '<h1>测试教案内容</h1><p>这是一个测试教案。</p>',
      markdownContent: '# 测试教案内容\n\n这是一个测试教案。',
      templateType: 'standard',
      status: 'draft',
      duration: 45,
      isAiGenerated: false,
      tags: ['测试', 'API']
    };

    const { message, data } = await lessonPlanAPI.saveLessonPlan(testLessonPlan, uid.value);
    results.save = `成功: ${message}, 教案ID: ${data}`;
    testLessonId.value = data.toString(); // 保存ID用于后续测试
    ElMessage.success('保存教案测试成功');
  } catch (error) {
    results.save = `失败: ${error.message}`;
    ElMessage.error('保存教案测试失败');
  } finally {
    loading.save = false;
  }
};

// 测试获取教案列表
const testGetLessonPlans = async () => {
  loading.list = true;
  try {
    const params = {
      uid: uid.value,
      page: 1,
      size: 5
    };

    const { message, data } = await lessonPlanAPI.getLessonPlans(params);
    results.list = `成功: ${message}, 获取到 ${data.list?.length || 0} 条记录, 总数: ${data.total || 0}`;
    ElMessage.success('获取教案列表测试成功');
  } catch (error) {
    results.list = `失败: ${error.message}`;
    ElMessage.error('获取教案列表测试失败');
  } finally {
    loading.list = false;
  }
};

// 测试更新教案
const testUpdateLessonPlan = async () => {
  if (!testLessonId.value) {
    ElMessage.warning('请先保存一个教案或输入教案ID');
    return;
  }

  loading.update = true;
  try {
    const updateData = {
      title: '更新后的测试教案 - ' + new Date().toLocaleString(),
      subject: '物理',
      grade: '高二',
      content: '<h1>更新后的教案内容</h1><p>这是更新后的测试教案。</p>',
      status: 'published'
    };

    const { message } = await lessonPlanAPI.updateLessonPlan(testLessonId.value, updateData, uid.value);
    results.update = `成功: ${message}`;
    ElMessage.success('更新教案测试成功');
  } catch (error) {
    results.update = `失败: ${error.message}`;
    ElMessage.error('更新教案测试失败');
  } finally {
    loading.update = false;
  }
};

// 测试删除教案
const testDeleteLessonPlan = async () => {
  if (!testDeleteId.value) {
    ElMessage.warning('请输入要删除的教案ID');
    return;
  }

  loading.delete = true;
  try {
    const { message } = await lessonPlanAPI.deleteLessonPlan(testDeleteId.value, uid.value);
    results.delete = `成功: ${message}`;
    ElMessage.success('删除教案测试成功');
  } catch (error) {
    results.delete = `失败: ${error.message}`;
    ElMessage.error('删除教案测试失败');
  } finally {
    loading.delete = false;
  }
};

// 测试获取模板
const testGetTemplates = async () => {
  loading.templates = true;
  try {
    const { message, data } = await lessonPlanAPI.getTemplates(uid.value);
    results.templates = `成功: ${message}, 获取到 ${data?.length || 0} 个模板`;
    ElMessage.success('获取模板测试成功');
  } catch (error) {
    results.templates = `失败: ${error.message}`;
    ElMessage.error('获取模板测试失败');
  } finally {
    loading.templates = false;
  }
};

// 测试获取推荐资源
const testGetResources = async () => {
  loading.resources = true;
  try {
    const params = {
      subject: '数学',
      grade: '高中',
      limit: 10
    };

    const { message, data } = await lessonPlanAPI.getRecommendedResources(params);
    results.resources = `成功: ${message}, 获取到 ${data?.length || 0} 个资源`;
    ElMessage.success('获取推荐资源测试成功');
  } catch (error) {
    results.resources = `失败: ${error.message}`;
    ElMessage.error('获取推荐资源测试失败');
  } finally {
    loading.resources = false;
  }
};

// 清空测试结果
const clearResults = () => {
  Object.keys(results).forEach(key => {
    results[key] = '';
  });
  testLessonId.value = '';
  testDeleteId.value = '';
  ElMessage.info('测试结果已清空');
};
</script>

<style scoped>
.test-section {
  padding: 1rem;
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  background-color: #f9fafb;
}

.test-section h4 {
  color: #374151;
}
</style>
