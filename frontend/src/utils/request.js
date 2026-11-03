import axios from 'axios'

// 创建 axios 实例
const request = axios.create({
    baseURL: '/api',
    timeout: 10000,                     // 请求超时时间（毫秒）
    headers: {
        'Content-Type': 'application/json'
    }
})

request.interceptors.request.use(
    config => {
        const token = localStorage.getItem('blog_token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    error => {
        console.error('请求错误:', error)
        return Promise.reject(error)
    }
)

// 响应拦截器（统一处理响应数据或错误）
request.interceptors.response.use(
    response => {
        // 直接返回响应体中的数据（根据后端实际返回结构调整）
        return response.data
    },
    error => {
        console.error('响应错误:', error)
        // 可以在此处统一提示错误信息
        return Promise.reject(error)
    }
)

export default request