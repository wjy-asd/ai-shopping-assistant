import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例，统一超时时间
const service = axios.create({
  timeout: 60000
})

// 响应拦截器：统一处理后端 Result 结构 { code, message, data }
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 如果后端返回 Result 结构（含 code 字段），取出 data
    if (res && typeof res === 'object' && 'code' in res) {
      if (res.code === 200) {
        return res.data
      }
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    const msg = error?.response?.data?.message || error?.message || '网络错误'
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default service
