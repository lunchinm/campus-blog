import request from '@/utils/request'

// 查询所有商品列表
export const getGoodsList = () => {
    return request.get('/goods/list')
}

// 根据 id 查询单个商品
export const getGoodsById = (id) => {
    return request.get(`/goods/${id}`)
}

// 新增商品
export const addGoods = (data) => {
    return request.post('/goods/add', data)
}

// 修改商品
export const updateGoods = (data) => {
    return request.put('/goods/update', data)
}

// 删除商品
export const deleteGoods = (id) => {
    return request.delete(`/goods/delete/${id}`)
}