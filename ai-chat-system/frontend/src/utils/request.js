import axios from 'axios'
import { ElMessage } from 'element-plus'
// 去掉这行：import { useAuthStore } from '../stores/auth'
import router from '../router'

const request = axios.create({
    baseURL: '/api', // 'http://localhost:8082/api',
    timeout: 120000 // 默认120秒，对于深度思考模式足够
})

// 添加请求拦截器
request.interceptors.request.use(
    function(config) {
        // 1. 从 localStorage 中获取保存的 Token
        const token = localStorage.getItem('token');

        // 2. 如果有 Token，就添加到 Authorization 头里
        if (token) {
            // 注意：Bearer 后面必须有一个空格，不能少！
            config.headers.Authorization = `Bearer ${token}`;
        }

        return config;
    },
    function(error) {
        return Promise.reject(error);
    }
);

// Response interceptor
request.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code !== 200) {
            ElMessage.error(res.message || 'Error')
            return Promise.reject(new Error(res.message || 'Error'))
        }
        return res
    },
    error => {
        console.error('Response error:', error)

        if (error.response?.status === 401) {
            // 去掉 useAuthStore()，直接操作 localStorage 和路由
            localStorage.removeItem('token'); // 清除无效 Token
            router.push('/login')
            ElMessage.error('Authentication expired, please login again')
        } else {
            const message = error.response?.data?.message || error.message || 'Network error'
            ElMessage.error(message)
        }

        return Promise.reject(error)
    }
)

export default request