import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user' 
import { ElMessage } from 'element-plus' 
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
        component: () => import('../views/GoodsDetails.vue'),
        meta: { requiresAuth: true } 
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
        path: '/payment/:orderId?', 
        name: 'Payment',
        component: () => import('../views/Payment.vue'),
        props: true,
        meta: { requiresAuth: true } 
    },
    {
        path: '/card-payment',
        name: 'CardPayment',
        component: () => import('../views/CardPayment.vue'),
        meta: { requiresAuth: true }
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
        path: '/register',
        name: 'Register',
        component: () => import('../views/Register.vue')
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
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes,
    scrollBehavior() {
        return { top: 0 }
    }
})

// 3. 全局前置守卫
router.beforeEach(async (to, from, next) => {
    const userStore = useUserStore()

    // 🌟 核心修复区：定义白名单，如果是去登录或注册，千万别再发请求查用户信息了！
    const whiteList = ['/login', '/register'];

    // 1. 尝试获取用户信息 (只在没有 userInfo，且【不在白名单】时才查)
    if (!userStore.userInfo && !whiteList.includes(to.path)) {
        try {
            await userStore.fetchCurrentUser()
        } catch (error) {
            console.warn('Session check failed or user not logged in')
        }
    }

    // 2. 鉴权逻辑：如果目标页面需要登录
    if (to.meta.requiresAuth) {
        if (!userStore.userInfo) {
            ElMessage.warning('Please login first to access this feature.')
            next({ path: '/login', query: { redirect: to.fullPath } }) 
            return
        }
    }

    // 管理员权限校验
    if (to.meta.requiresAdmin) {
        if (userStore.userInfo?.userRole !== 1) {
            ElMessage.error('Access Denied: Admin privileges required')
            next('/') 
            return
        }
    }

    // 3. 防止已登录用户重复访问登录页
    if (whiteList.includes(to.path) && userStore.userInfo) {
        next('/')
        return
    }

    // 4. 绿灯放行
    next()
})

export default router