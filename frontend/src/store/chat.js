import { reactive, watch } from 'vue'

// 持久化 key
const STORAGE_KEY = 'aixiaogou_conversations'
const MEMORY_KEY = 'aixiaogou_memoryId'

// 从 localStorage 读取历史对话
function loadConversations() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY)) || {}
  } catch {
    return {}
  }
}

function saveConversations(data) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
  } catch {
    // 存储空间不足等异常忽略
  }
}

// 全局聊天状态：按会话ID分桶保存，持久化到 localStorage，刷新不丢
// conversations 结构：{ '1': [...用户1的消息], '2': [...用户2的消息] }
export const chatState = reactive({
  currentId: localStorage.getItem(MEMORY_KEY) || '1',
  conversations: loadConversations(),
  // 当前显示的消息数组（指向 conversations[currentId]）
  messages: []
})

// 初始化：指向当前会话的消息数组
if (!chatState.conversations[chatState.currentId]) {
  chatState.conversations[chatState.currentId] = []
}
chatState.messages = chatState.conversations[chatState.currentId]

// 任何会话消息变化都自动写回 localStorage
watch(
  () => chatState.conversations,
  (val) => saveConversations(val),
  { deep: true }
)

// 切换到指定会话：不丢历史，显示该会话之前的对话
export function switchConversation(id) {
  chatState.currentId = String(id)
  localStorage.setItem(MEMORY_KEY, chatState.currentId)
  if (!chatState.conversations[chatState.currentId]) {
    chatState.conversations[chatState.currentId] = []
  }
  chatState.messages = chatState.conversations[chatState.currentId]
}

// 新会话：由调用方传入新的 memoryId（通常是时间戳），建一个空桶
export function startNewConversation(id) {
  chatState.currentId = String(id)
  localStorage.setItem(MEMORY_KEY, chatState.currentId)
  chatState.conversations[chatState.currentId] = []
  chatState.messages = chatState.conversations[chatState.currentId]
}
