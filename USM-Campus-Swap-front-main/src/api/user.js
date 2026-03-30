import request from '@/plugins/request.js'

// 1. 注册
export function userRegister(data) {
    return request({
        url: '/user/register',
        method: 'post',
        data // 包含 username, userAccount, password, checkPassword 等
    })
}

// 2. 登录
export function userLogin(data) {
    return request({
        url: '/user/login',
        method: 'post',
        data // 包含 userAccount, userPassword
    })
}

// 3. 获取当前登录用户
export function getCurrentUser() {
    return request({
        url: '/user/current',
        method: 'get'
    })
}

// 4. 注销
export function userLogout() {
    return request({
        url: '/user/logout',
        method: 'post'
    })
}