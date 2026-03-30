import request from '@/plugins/request.js'

// 1. 发布商品
export function publishGoods(data) {
    return request({
        url: '/goods/publish',
        method: 'post',
        data
    })
}

// 2. 获取我的商品 (个人中心用)
export function listMyGoods() {
    return request({
        url: '/goods/my',
        method: 'get'
    })
}

// 3. 新增：获取公开商品列表 (首页用)
export function getGoodsList(params) {
    return request({
        url: '/goods/list',
        method: 'get',
        params // 这里会自动把 pageNum, pageSize, keyword 等拼接到 URL 上
    })
}

// 4. 获取商品详情 (详情页用，为下一步做准备)
export function getGoodsDetail(id) {
    return request({
        url: `/goods/${id}`,
        method: 'get'
    })
}