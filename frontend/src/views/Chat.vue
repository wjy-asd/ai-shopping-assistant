<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { chat } from '../api'
import { bumpCart } from '../store/app'
import { chatState, switchConversation, startNewConversation } from '../store/chat'

// 当前输入内容
const input = ref('')
// 是否正在等待 AI 回复
const loading = ref(false)
// 聊天区域 DOM 引用
const chatBodyRef = ref(null)

// 会话ID（memoryId）：持久化到本地，点"新会话"时换新ID，模拟不同用户/会话
const memoryId = ref(localStorage.getItem('aixiaogou_memoryId') || '1')
// 可选会话列表（模拟不同登录用户）
const userOptions = [
  { label: '用户1', value: '1' },
  { label: '用户2', value: '2' },
  { label: '用户3', value: '3' }
]

// 按当前模式调用后端 AI 接口（融合后的统一智能客服入口）
async function sendMessage() {
  const text = input.value.trim()
  if (!text || loading.value) return

  chatState.messages.push({ role: 'user', content: text })
  input.value = ''
  loading.value = true
  await scrollToBottom()

  try {
    const payload = { memoryId: String(memoryId.value), message: text }
    // 统一走多轮客服：带记忆、会话隔离、工具调用（搜索/加购/物流/知识检索全自动触发）
    const data = await chat(payload)
    chatState.messages.push({ role: 'assistant', content: data })
    // AI 可能执行了加入购物车等操作，通知购物车页面刷新
    bumpCart()
  } catch (e) {
    chatState.messages.push({
      role: 'assistant',
      content: '请求失败：' + (e?.message || '请确认后端服务已启动')
    })
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

// 开始一个新会话：换新的会话ID并建一个空对话
function resetChat() {
  memoryId.value = String(Date.now())
  localStorage.setItem('aixiaogou_memoryId', memoryId.value)
  startNewConversation(memoryId.value)
}

// 切换会话用户：切换 memoryId，保留该会话之前的历史消息
function onSwitchUser() {
  localStorage.setItem('aixiaogou_memoryId', memoryId.value)
  switchConversation(memoryId.value)
}

// 自动滚动到底部
async function scrollToBottom() {
  await nextTick()
  if (chatBodyRef.value) {
    chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight
  }
}

// 初始化：按本地保存的 memoryId 载入对应会话的历史消息
onMounted(() => {
  switchConversation(memoryId.value)
})
</script>

<template>
  <div class="chat-page">
    <el-card class="chat-card" shadow="never">
      <template #header>
        <div class="chat-header">
          <span>在线客服</span>
          <div class="header-tools">
            <span class="user-label">会话</span>
            <el-select v-model="memoryId" size="small" style="width: 110px" @change="onSwitchUser">
              <el-option v-for="u in userOptions" :key="u.value" :label="u.label" :value="u.value" />
            </el-select>
            <el-button size="small" type="info" plain @click="resetChat">新会话</el-button>
          </div>
        </div>
      </template>

      <div ref="chatBodyRef" class="chat-body">
        <el-empty
          v-if="chatState.messages.length === 0"
          description="您好，我是AI小购智能客服，可以帮你找商品、加入购物车、查物流、推荐商品～"
        />
        <div v-for="(msg, index) in chatState.messages" :key="index" class="message-row" :class="msg.role">
          <div class="message-bubble">{{ msg.content }}</div>
        </div>
        <div v-if="loading" class="message-row assistant">
          <div class="message-bubble typing">正在处理...</div>
        </div>
      </div>

      <div class="chat-input">
        <el-input
          v-model="input"
          type="textarea"
          :rows="3"
          placeholder="例如：我想买一双运动鞋，有什么推荐吗？（Enter 发送，Shift+Enter 换行）"
          @keydown.enter.exact.prevent="sendMessage"
        />
        <el-button type="primary" :loading="loading" @click="sendMessage">发送</el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.chat-page {
  height: calc(100vh - 40px);
}

.chat-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 16px;
  font-weight: 600;
  gap: 12px;
  flex-wrap: wrap;
}

.header-tools {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 400;
  flex-wrap: wrap;
}

.user-label {
  font-size: 13px;
  color: #909399;
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  background: #fff;
}

.message-row {
  display: flex;
  margin-bottom: 12px;
}

.message-row.user {
  justify-content: flex-end;
}

.message-row.assistant {
  justify-content: flex-start;
}

.message-bubble {
  max-width: 78%;
  padding: 10px 14px;
  border-radius: 8px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.user .message-bubble {
  background: #ff6a00;
  color: #fff;
}

.assistant .message-bubble {
  background: #f0f2f5;
  color: #303133;
}

.typing {
  color: #909399;
  font-style: italic;
}

.chat-input {
  display: flex;
  gap: 8px;
  padding: 12px;
  align-items: flex-end;
}

.chat-input .el-textarea {
  flex: 1;
}
</style>
