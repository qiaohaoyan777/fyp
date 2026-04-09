import axios from 'axios'
import router from '@/router'; // 🌟 确保路径对应你的路由定义文件

const request = axios.create({
    baseURL: 'http://localhost:8081/api',
    timeout: 10000,
    withCredentials: true,
    headers: {
         'Content-Type': 'application/json;charset=utf-8'
     }
 })

// Request interceptor - Add logging
request.interceptors.request.use(
    config => {
        console.log(`[Request] ${config.method?.toUpperCase()} ${config.url}`, config.data || '')
        return config
    },
    error => {
        console.error('[Request Error]', error)
        return Promise.reject(error)
    }
)

// Response interceptor - Logic fix
request.interceptors.response.use(
    response => {
        console.log(`[Response ${response.status}] ${response.config.url}`, response.data)

        const res = response.data

        // Case 1: Return data directly (no 'code' field)
        if (res.code === undefined) {
            console.log('Returning data directly (no code field):', res)
            return res
        }

        // Case 2: Business response with 'code' field
        console.log('Business response code:', res.code, 'message:', res.message)

        // Business success (assuming success code is 0 or 200)
        if (res.code === 0 || res.code === 200) {
            // Return 'data' field, if no 'data', return the entire 'res'
            return res.data !== undefined ? res.data : res
        }

        // Business failure - Password error, etc.
        if (res.code === 40100) {
            console.warn('User not logged in or password incorrect')
            localStorage.removeItem('user');
            
            // 🌟 终极核心修复：同时检查路由实例和真实的浏览器地址栏！
            const currentVuePath = router.currentRoute?.value?.path;
            const currentUrl = window.location.href;
            
            // 只要当前的网址里不包含 'login'，才去执行跳转
            if (currentVuePath !== '/login' && !currentUrl.includes('/login')) {
                // 用 replace 代替 push，这样不会在浏览器历史里产生无数个废记录
                router.replace('/login');
            }
            
            // 遇到未登录直接拒绝，把错误抛给组件，打断后续请求
            return Promise.reject(new Error(res.message || 'Please login first'));
        }

        // Throw business error
        // Priority: description > message > default text
        const errorMsg = res.description || res.message || `Request failed (code: ${res.code})`
        return Promise.reject(new Error(errorMsg))

    },
    error => {
        console.error('[Response Error]', {
            message: error.message,
            code: error.code,
            response: error.response,
            request: error.request
        })

        // Check for network errors or no response
        if (error.code === 'ECONNABORTED') {
            return Promise.reject(new Error('Request timed out, please check your network connection'))
        }

        if (!error.response) {
            // No response received
            if (error.request) {
                return Promise.reject(new Error('No response from server, please check if the backend service is running'))
            } else {
                return Promise.reject(new Error('Request configuration error'))
            }
        }

        // HTTP response received but with error status code
        const { status, data } = error.response

        if (status === 401) {
            return Promise.reject(new Error('Authentication failed, please check your credentials'))
        } else if (status === 404) {
            return Promise.reject(new Error('API interface not found'))
        } else if (status >= 500) {
            return Promise.reject(new Error('Internal server error'))
        }

        // Try to get specific error message from backend
        const errorMessage = data?.message ||
            data?.msg ||
            `Request failed (${status})`

        return Promise.reject(new Error(errorMessage))
    }
)

export default request