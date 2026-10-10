import request from './request'

// 商品相关接口
export const getProducts = (params) => request.get('/api/products', { params })
export const getProduct = (id) => request.get(`/api/products/${id}`)

// 购物车相关接口
export const getCart = (userId) => request.get('/api/cart', { params: { userId } })
export const addToCart = (data) => request.post('/api/cart', data)
export const updateCart = (id, data) => request.put(`/api/cart/${id}`, data)
export const removeCart = (id) => request.delete(`/api/cart/${id}`)
export const clearCart = (userId) => request.delete('/api/cart', { params: { userId } })
export const checkoutCart = (userId) => request.post('/api/cart/checkout', null, { params: { userId } })

// 订单相关接口
export const checkoutOrder = (userId) => request.post('/api/order/checkout', null, { params: { userId } })
export const getOrders = (userId) => request.get('/api/order', { params: { userId } })
export const getOrder = (orderNo) => request.get(`/api/order/${orderNo}`)
export const cancelOrder = (orderNo) => request.post(`/api/order/${orderNo}/cancel`)

// 物流相关接口
export const getLogistics = (orderNo) => request.get(`/api/logistics/${orderNo}`)
export const getUserLogistics = (userId) => request.get(`/api/logistics/user/${userId}`)

// AI 智能客服接口
export const chat = (data) => request.post('/xiaogou/chat', data)
export const basicChat = (data) => request.post('/xiaogou/basic/chat', data)
export const promptChat = (data) => request.post('/xiaogou/prompt/chat', data)
export const rag = (data) => request.post('/xiaogou/rag', data)
