import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// Vite 配置：使用 Vue 插件，并配置开发代理解决前后端跨域问题
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      // 将前端 /xiaogou 开头的请求代理到后端 8080 端口
      '/xiaogou': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // 将前端 /api 开头的业务接口代理到后端 8080 端口
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
