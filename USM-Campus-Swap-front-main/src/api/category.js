import request from '@/plugins/request.js'

// 获取所有分类
export function listCategories() {
    return request({
        url: '/category/list',
        method: 'get'
    })
}