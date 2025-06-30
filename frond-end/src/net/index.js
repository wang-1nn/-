import axios from 'axios';
import {ElMessage} from 'element-plus';
import {useAuthStore} from '@/stores/counter';
import router from '@/router';

// 创建axios实例
const instance = axios.create({
  // 配置基础URL，可以从环境变量中获取
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  // 超时时间
  timeout: 15000,
  // 允许跨域带cookie
  withCredentials: true
});

// 请求拦截器
instance.interceptors.request.use(
  (config) => {
    // 获取认证状态
    const authStore = useAuthStore();
    
    // 如果已登录，添加token到请求头
    if (authStore.token) {
      config.headers['Authorization'] = `Bearer ${authStore.token}`;
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
instance.interceptors.response.use(
  (response) => {
    // 直接返回响应数据
    return response.data;
  },
  (error) => {
    // 处理错误响应
    handleResponseError(error);
    return Promise.reject(error);
  }
);

// 处理响应错误
const handleResponseError = (error) => {
  // 网络错误
  if (!error.response) {
    ElMessage.error('网络错误，请检查您的网络连接');
    return;
  }
  
  // 获取错误状态码
  const status = error.response.status;
  
  // 根据状态码处理
  switch (status) {
    case 400:
      ElMessage.error('请求参数错误');
      break;
    case 401:
      // 未授权，清除token并跳转到登录页
      ElMessage.error('登录已过期，请重新登录');
      const authStore = useAuthStore();
      authStore.logout();
      router.push('/login');
      break;
    case 403:
      ElMessage.error('没有权限访问该资源');
      break;
    case 404:
      ElMessage.error('请求的资源不存在');
      break;
    case 500:
      ElMessage.error('服务器内部错误');
      break;
    default:
      ElMessage.error(`请求失败：${error.response.data.message || '未知错误'}`);
  }
};

// GET请求
export const get = async (url, params = {}, headers = {}, showError = true, useCache = false) => {
  try {
    const config = {
      params,
      headers
    };
    
    // 可选缓存控制
    if (useCache) {
      config.headers['Cache-Control'] = 'max-age=300';
    }
    
    return await instance.get(url, config);
  } catch (error) {
    if (showError) {
      handleResponseError(error);
    }
    throw error;
  }
};

// POST请求
export const post = async (url, data = {}, params = {}, headers = {}, showError = true, parseJson = true) => {
  try {
    const config = {
      params,
      headers
    };
    
    if (parseJson) {
      config.headers['Content-Type'] = 'application/json';
    }
    
    return await instance.post(url, data, config);
  } catch (error) {
    if (showError) {
      handleResponseError(error);
    }
    throw error;
  }
};

// PUT请求
export const put = async (url, data = {}, params = {}, headers = {}, showError = true) => {
  try {
    const config = {
      params,
      headers
    };
    
    return await instance.put(url, data, config);
  } catch (error) {
    if (showError) {
      handleResponseError(error);
    }
    throw error;
  }
};

// DELETE请求
export const del = async (url, params = {}, headers = {}, showError = true) => {
  try {
    const config = {
      params,
      headers
    };
    
    return await instance.delete(url, config);
  } catch (error) {
    if (showError) {
      handleResponseError(error);
}
    throw error;
  }
};

// 流式响应处理
export const streamResponse = async (url, data = {}, onData, onComplete, onError, params = {}, headers = {}) => {
  try {
    // 设置响应类型为文本流
    const config = {
      params,
      headers,
      responseType: 'stream',
      onDownloadProgress: (progressEvent) => {
        // 获取响应文本
        const responseText = progressEvent.target.responseText;
        
        // 解析SSE数据
        if (responseText) {
          const lines = responseText.split('\n');
          
          // 处理每一行数据
          for (const line of lines) {
            if (line.startsWith('data: ')) {
              const data = line.substring(6);
              
              try {
                // 尝试解析JSON
                const jsonData = JSON.parse(data);
                onData && onData(jsonData);
              } catch (e) {
                // 如果不是JSON，直接传递文本
                onData && onData(data);
              }
            }
          }
        }
      }
    };
    
    // 发送请求
    const response = await instance.post(url, data, config);
    
    // 完成回调
    onComplete && onComplete(response);
    
    return response;
  } catch (error) {
    // 错误回调
    onError && onError(error);
    handleResponseError(error);
    throw error;
  }
};

// 上传文件
export const uploadFile = async (url, file, onProgress, params = {}, headers = {}, showError = true) => {
  try {
    const formData = new FormData();
    formData.append('file', file);
    
    // 添加其他参数
    for (const key in params) {
      formData.append(key, params[key]);
    }
    
    const config = {
      headers: {
        'Content-Type': 'multipart/form-data',
        ...headers
      },
      onUploadProgress: (progressEvent) => {
        // 计算上传进度
        const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
        onProgress && onProgress(progress);
      }
    };
    
    return await instance.post(url, formData, config);
  } catch (error) {
    if (showError) {
      handleResponseError(error);
}
    throw error;
  }
};

export default {
  get,
  post,
  put,
  del,
  streamResponse,
  uploadFile
};