import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user' // 1. 引入 Store
import Home from '../views/Home.vue'

const routes = [
    {
        path: '/',
        name: 'Home',
        component: Home
    },
    {
        path: '/goods/:id',
        name: 'GoodsDetail',
        component: () => import('../views/GoodsDetails.vue')
    },
    {
        path: '/sell',
        name: 'Sell',
        component: () => import('../views/Sell.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/my-products',
        name: 'MyProducts',
        component: () => import('../views/MyProducts.vue'),
        meta: {requiresAuth: true}
    },
    {
        path: '/payment/:orderId?', // 使用动态 ID 获取订单
        name: 'Payment',
        component: () => import('../views/Payment.vue'),
        props: true,
        meta: { requiresAuth: true } // 只有登录用户可以支付
    },
    {
        path: '/my-wishlist',
        name: 'MyWishlist',
        component: () => import('../views/Wishlist.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/orders',
        name: 'Orders',
        component: () => import('../views/Orders.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/messages',
        name: 'Messages',
        component: () => import('../views/Messages.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/help',
        name: 'Help',
        component: () => import('../views/Help.vue')
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue')
    },
    {
        path: '/admin/goods',
        name: 'AdminGoods',
        component: () => import('../views/AdminGoodsView.vue'),
        meta: {
            requiresAuth: true,
            requiresAdmin: true
        }
    },
    {
        path: '/admin/orders',
        name: 'AdminOrders',
        component: () => import('../views/AdminOrderView.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
        path: '/admin/users',
        name: 'AdminUsers',
        component: () => import('../views/AdminUserView.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('../views/Register.vue')
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes,
    // 优化体验：每次跳转页面，自动滚动到顶部
    scrollBehavior() {
        return { top: 0 }
    }
})

// 3. 全局前置守卫 (核心逻辑)
router.beforeEach(async (to, from, next) => {
    const userStore = useUserStore()

    // 1. 如果 Pinia 里没有用户信息 (说明是刚刷新，或者真的没登录)
    if (!userStore.userInfo) {
        try {
            // 🔥 关键修改：去掉 document.cookie 的判断
            // 直接尝试去后端拉取用户信息。
            // 如果后端有 Session，这里就会成功，Pinia 就有值了。
            await userStore.fetchCurrentUser()
        } catch (error) {
            // 如果报错（比如 401 未登录），说明真的没登录，或者 Session 过期了
            // 这里什么都不用做，userStore.userInfo 依然是 null
            console.warn('Session check failed or user not logged in')
        }
    }

    // 2. 鉴权逻辑
    if (to.meta.requiresAuth) {
        // 如果需要登录，且经过上面的尝试后，依然没有用户信息
        if (!userStore.userInfo) {
            next('/login') // 踢回登录页
            return
        }
    }

    if (to.meta.requiresAdmin) {
        // 如果不是管理员 (假设 role 为 1 是管理员)
        if (userStore.userInfo?.userRole !== 1) {
            // 注意：这里的字段名要对应你后端返回的 role 字段
            ElMessage.error('Access Denied: Admin privileges required')
            next('/') // 踢回首页
            return
        }
    }

    // 3. 针对登录页的优化
    // 如果用户已经登录了，还想去访问 /login，直接把他踢回主页或上次的页面
    if (to.path === '/login' && userStore.userInfo) {
        next('/')
        return
    }

    // 放行
    next()
})

export default router