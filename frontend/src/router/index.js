import { createRouter, createWebHashHistory } from 'vue-router'
import MainLayout from '../layout/MainLayout.vue'

// 定义路由：使用 hash 模式，避免后端部署时刷新 404
const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      component: MainLayout,
      redirect: '/products',
      children: [
        { path: 'products', name: 'Products', component: () => import('../views/ProductList.vue'), meta: { title: '商品列表' } },
        { path: 'cart', name: 'Cart', component: () => import('../views/Cart.vue'), meta: { title: '购物车' } },
        { path: 'orders', name: 'Orders', component: () => import('../views/Orders.vue'), meta: { title: '我的订单' } },
        { path: 'logistics', name: 'Logistics', component: () => import('../views/Logistics.vue'), meta: { title: '物流查询' } },
        { path: 'chat', name: 'Chat', component: () => import('../views/Chat.vue'), meta: { title: '在线客服' } }
      ]
    }
  ]
})

export default router
