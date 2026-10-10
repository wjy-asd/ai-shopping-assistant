import { reactive } from 'vue'

// 固定测试用户 ID（暂不做登录注册）
export const USER_ID = 1

// 全局状态：cartVersion 用于跨页面触发购物车刷新
export const appState = reactive({
  cartVersion: 0
})

// 购物车数据发生变化时调用，通知购物车页面重新加载
export function bumpCart() {
  appState.cartVersion++
}
