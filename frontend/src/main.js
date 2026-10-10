import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles/global.css'
import App from './App.vue'
import router from './router'

// 创建 Vue 应用，并全局注册 Element Plus 组件库与路由
const app = createApp(App)
app.use(ElementPlus)
app.use(router)
app.mount('#app')
