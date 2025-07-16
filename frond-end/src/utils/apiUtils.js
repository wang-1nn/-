/**
 * API工具函数
 * 统一处理API请求的URL和配置
 */

// 从main.js中获取配置的baseURL
const API_BASE_URL = 'http://localhost:8080';

/**
 * 获取完整的API URL
 * @param {string} path - API路径
 * @returns {string} 完整的URL
 */
export function getApiUrl(path) {
    // 如果路径已经是完整URL，直接返回
    if (path.startsWith('http://') || path.startsWith('https://')) {
        return path;
    }

    // 确保路径以/开头
    const normalizedPath = path.startsWith('/') ? path : `/${path}`;

    return `${API_BASE_URL}${normalizedPath}`;
}

/**
 * 获取认证token
 * @returns {string} Bearer token
 */
export function getAuthToken() {
    const token = localStorage.getItem('authToken');
    return token ? `Bearer ${token}` : '';
}

/**
 * 创建带认证的fetch请求
 * @param {string} url - 请求URL
 * @param {object} options - fetch选项
 * @returns {Promise<Response>} fetch响应
 */
export async function authenticatedFetch(url, options = {}) {
    const fullUrl = getApiUrl(url);

    const defaultOptions = {
        credentials: 'include',
        headers: {
            'Authorization': getAuthToken(),
            ...options.headers
        }
    };

    const mergedOptions = {
        ...defaultOptions,
        ...options,
        headers: {
            ...defaultOptions.headers,
            ...options.headers
        }
    };

    return fetch(fullUrl, mergedOptions);
}

/**
 * 处理SSE流式响应
 * @param {string} url - SSE URL
 * @param {object} options - 配置选项
 * @param {function} onMessage - 消息处理函数
 * @param {function} onError - 错误处理函数
 * @param {function} onClose - 关闭处理函数
 * @returns {EventSource} EventSource实例
 */
export function createSSEConnection(url, options = {}) {
    const fullUrl = getApiUrl(url);

    // 对于SSE，我们需要在URL中添加认证参数
    const urlWithAuth = new URL(fullUrl);
    const token = localStorage.getItem('authToken');
    if (token) {
        urlWithAuth.searchParams.set('token', token);
    }

    const eventSource = new EventSource(urlWithAuth.toString());

    return eventSource;
}

/**
 * 处理流式fetch响应
 * @param {string} url - 请求URL
 * @param {object} options - fetch选项
 * @param {function} onChunk - 数据块处理函数
 * @param {function} onComplete - 完成处理函数
 * @param {function} onError - 错误处理函数
 */
export async function handleStreamResponse(url, options = {}, onChunk, onComplete, onError) {
    try {
        const response = await authenticatedFetch(url, options);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer = '';

        while (true) {
            const { done, value } = await reader.read();
            if (done) break;

            const chunk = decoder.decode(value, { stream: true });
            buffer += chunk;

            // 处理SSE格式的数据
            const lines = buffer.split('\n');
            buffer = lines.pop() || '';

            for (const line of lines) {
                if (line.startsWith('data: ')) {
                    const data = line.slice(6).trim();
                    if (data === '[DONE]' || data === '[done]') {
                        if (onComplete) onComplete();
                        return;
                    }

                    // 如果数据为空，跳过
                    if (!data) continue;

                    try {
                        // 尝试解析JSON
                        const parsed = JSON.parse(data);
                        if (onChunk) onChunk(parsed);
                    } catch (e) {
                        // 如果不是JSON，直接传递原始数据作为content
                        if (onChunk) onChunk({ content: data });
                    }
                } else if (line.trim() && !line.startsWith('data:') && onChunk) {
                    // 处理非SSE格式的流数据，但排除包含"data:"的行
                    onChunk({ content: line.trim() });
                }
            }
        }

        if (onComplete) onComplete();
    } catch (error) {
        if (onError) onError(error);
    }
}

/**
 * AI相关API的专用工具
 */
export const aiAPI = {
    /**
     * 优化教学大纲
     * @param {FormData} formData - 包含文件和参数的FormData
     * @param {function} onChunk - 数据块处理函数
     * @param {function} onComplete - 完成处理函数
     * @param {function} onError - 错误处理函数
     */
    async optimizeOutline(formData, onChunk, onComplete, onError) {
        await handleStreamResponse(
            '/api/teacher/Ai/OptimizateOutline',
            {
                method: 'POST',
                body: formData
            },
            onChunk,
            onComplete,
            onError
        );
    },

    /**
     * 生成教案
     * @param {FormData} formData - 包含消息的FormData
     * @param {string} conversationId - 会话ID
     * @param {function} onChunk - 数据块处理函数
     * @param {function} onComplete - 完成处理函数
     * @param {function} onError - 错误处理函数
     */
    async createLesson(formData, conversationId, onChunk, onComplete, onError) {
        await handleStreamResponse(
            `/api/teacher/Ai/CreateLesson?id=${conversationId}`,
            {
                method: 'POST',
                body: formData
            },
            onChunk,
            onComplete,
            onError
        );
    }
};

export default {
    getApiUrl,
    getAuthToken,
    authenticatedFetch,
    createSSEConnection,
    handleStreamResponse,
    aiAPI
};
