import { defineStore } from 'pinia'
import { ref } from 'vue'
import { userLogin, userLogout, getCurrentUser } from '@/api/user'

export const useUserStore = defineStore('user', () => {
    const userInfo = ref(null)

    // 1. 登录
    const login = async (loginForm) => {
        // 【修改点】直接获取数据，不需要判断 res.code
        // 如果登录失败，拦截器会抛出异常，直接跳到 Login.vue 的 catch 块
        const userData = await userLogin(loginForm)

        // 此时 userData 就是后端返回的 User 对象
        userInfo.value = userData
        localStorage.setItem('user', JSON.stringify(userData))

        return userData
    }


    // 2. 获取当前用户
    const fetchCurrentUser = async () => {
        try {
            const user = await getCurrentUser()
            userInfo.value = user
            localStorage.setItem('user', JSON.stringify(user))
        } catch (error) {
            userInfo.value = null
            localStorage.removeItem('user')
            throw error
        }
    }

    // 3. 注销
    const logout = async () => {
        try {
            await userLogout()
        } catch (error) {
            console.error('注销错误:', error)
        } finally {
            userInfo.value = null
            localStorage.removeItem('user')
        }
    }

    const isLoggedIn = () => {
        return !!userInfo.value
    }

    return {
        userInfo,
        login,
        logout,
        fetchCurrentUser,
        isLoggedIn
    }
})