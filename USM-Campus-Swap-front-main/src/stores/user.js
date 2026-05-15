import { defineStore } from 'pinia'
import { ref } from 'vue'
import { userLogin, userLogout, getCurrentUser } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)
  
  // 全局未读消息总数
  const totalUnread = ref(0)

  // 1. 登录
  const login = async (loginForm) => {
    const userData = await userLogin(loginForm)
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
      console.warn('获取用户信息失败：可能未登录或后端未开启')
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
      totalUnread.value = 0 // 注销时清空未读数
      localStorage.removeItem('user')
    }
  }

  // 修改未读数的方法
  const setTotalUnread = (count) => {
    totalUnread.value = count
  }

  const isLoggedIn = () => {
    return !!userInfo.value
  }

  return {
    userInfo,
    totalUnread,
    setTotalUnread,
    login,
    logout,
    fetchCurrentUser,
    isLoggedIn
  }
})