import axios from 'axios';
import {ElMessage} from "element-plus"; //引入用到的组件

const defaultError = () => ElMessage.error('发生错误，请联系管理员。') //定义默认错误提示语
const defaultFailure = (message) => ElMessage.warning(message) //后端请求返回失败信息时将其打印
function getAuthToken() {
    const token = localStorage.getItem('authToken');
    if (token) {
        // 确保返回的token带有Bearer前缀
        return `Bearer ${token}`;
    }
    return '';
}
//post请求示例
function post(url, data, success, failure = defaultFailure, error = defaultError, useJson = true) {
    let headers = {
        "Authorization": getAuthToken()
    };

    let requestData;

    // 如果传入的是 FormData，说明需要以 multipart/form-data 上传（常用于文件）
    if (data instanceof FormData) {
        requestData = data;
        // 让浏览器自动设置带有 boundary 的 Content-Type
        // axios 在发现数据是 FormData 时，会自动设置正确的 multipart 头，因此这里不显式指定
    } else {
        const contentType = useJson ? "application/json" : "application/x-www-form-urlencoded";
        headers["Content-Type"] = contentType;
        requestData = useJson ? JSON.stringify(data) : new URLSearchParams(data).toString();
    }

    axios.post(url, requestData, {
        headers,
        withCredentials: true
    })
        .then(({data}) => {
            if (data.success) {
                success(data.message, data.data, data.status);
            } else {
                failure(data.message, data.data, data.status);
            }
        })
        .catch(error);
}


function get(url, data = null, success, failure = defaultFailure, error = defaultError) {
    const config = {
        withCredentials: true,
        params: data,  // 将数据作为查询参数
        headers: {
            "Authorization": getAuthToken(),
        }
    };

    axios.get(url, config)
        .then(({data}) => {

            if (data.success)
                success(data.message,data.data, data.status)
            else
                failure(data.message,data.data, data.status)
        })
        .catch(error)
}

function InternalGet(url, success, failure = defaultFailure, error = defaultError) {
    axios.get(url, {
        withCredentials: true
    }).then((response) => {
        success(response.data);
    }).catch(error);
}


export {get, post, InternalGet} //导出get post InternalGet方法 供所有组件使用