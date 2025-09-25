<template>
  <div class="chat-container">
    <!-- 侧边栏 - 对话历史 -->
    <div class="chat-sidebar" :class="{ 'dark': isDarkMode }">
      <div class="sidebar-header">
        <span class="header-title">对话历史</span>
        <div class="header-actions">
          <a-button type="primary" size="small" @click="startNewConversation">
            新对话
          </a-button>
          <a-button size="small" @click="toggleDarkMode" class="theme-toggle">
            {{ isDarkMode ? '☀️' : '🌙' }}
          </a-button>
        </div>
      </div>
      <div class="history-list">
        <div class="history-item active">
          当前会话
        </div>
        <!-- 可扩展为多会话历史 -->
      </div>
    </div>

    <!-- 主聊天区域 -->
    <div class="chat-main">
      <!-- 消息展示区域 -->
      <div
          ref="chatContainer"
          class="chat-messages"
          :class="{ 'dark': isDarkMode }"
      >
        <div
            v-for="(message, index) in messages"
            :key="index"
            class="message-wrapper"
            :class="message.role === 'user' ? 'user-wrapper' : 'ai-wrapper'"
        >
          <div
              class="message"
              :class="[
              message.role === 'user' ? 'user-message' : 'ai-message',
              { 'dark': isDarkMode }
            ]"
          >
            <template v-if="message.role === 'assistant'">
              <div v-if="message.isStreaming || message.isLoading">
                <ai-message-content
                    :content="message.content.slice(0, message.visibleChars)"
                    :dark="isDarkMode"
                />
                <span v-if="message.isStreaming || message.isLoading" class="typing-cursor">▍</span>
              </div>
              <div v-else>
                <ai-message-content
                    :content="message.content"
                    :dark="isDarkMode"
                />
              </div>
            </template>
            <template v-else>
              {{ message.content }}
            </template>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input-area" :class="{ 'dark': isDarkMode }">
        <a-textarea
            ref="textareaRef"
            v-model:value="userInput"
            :placeholder="'请输入您的问题...'"
            :disabled="isLoading"
            :auto-size="{ minRows: 3, maxRows: 8 }"
            @keydown.enter.exact.prevent="sendMessage"
            class="chat-textarea"
            :class="{ 'dark': isDarkMode }"
        />
        <div class="input-actions">
          <a-button
              type="primary"
              @click="sendMessage"
              :disabled="!userInput.trim() || isLoading"
          >
            发送
          </a-button>
          <a-button
              v-if="isLoading"
              type="default"
              danger
              @click="stopResponse"
              class="stop-btn"
          >
            停止响应
          </a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted, watch } from 'vue'
import { message } from 'ant-design-vue'
import { get } from '@lib/axios'

// API 函数
const streamChatApi = (message) => {
  return get("/ai/chat?message=" + message)
}

// AI消息内容组件
const AiMessageContent = {
  name: 'AiMessageContent',
  props: {
    content: String,
    dark: Boolean
  },
  computed: {
    parsedBlocks() {
      const blocks = []
      let text = this.content || ''
      let lastIndex = 0
      const thinkOpen = '<think>'
      const thinkClose = '</think>'

      while (true) {
        const start = text.indexOf(thinkOpen, lastIndex)
        if (start === -1) {
          const rest = text.slice(lastIndex)
          if (rest.trim() !== '') {
            blocks.push({ type: 'text', content: rest })
          }
          break
        }

        if (start > lastIndex) {
          const before = text.slice(lastIndex, start)
          if (before.trim() !== '') {
            blocks.push({ type: 'text', content: before })
          }
        }

        const end = text.indexOf(thinkClose, start + thinkOpen.length)
        if (end === -1) {
          const rest = text.slice(start)
          if (rest.trim() !== '') {
            blocks.push({ type: 'text', content: rest })
          }
          break
        }

        const thinkContent = text.slice(start + thinkOpen.length, end)
        blocks.push({ type: 'think', content: thinkContent })
        lastIndex = end + thinkClose.length
      }

      return blocks
    }
  },
  methods: {
    renderMarkdown(content) {
      // 简单的markdown处理
      return content
          .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
          .replace(/\*(.+?)\*/g, '<em>$1</em>')
          .replace(/`(.+?)`/g, '<code>$1</code>')
          .replace(/\n/g, '<br>')
    }
  },
  template: `
    <div>
      <div
        v-for="(block, i) in parsedBlocks"
        :key="i"
      >
        <div
          v-if="block.type === 'think'"
          class="think-block"
          :class="{ 'dark': dark }"
        >
          <span class="think-icon" title="思考">💡</span>
          <span class="think-label">思考：</span>
          <span>{{ block.content.trim() }}</span>
        </div>
        <div
          v-else
          class="ai-message-content-markdown"
          v-html="renderMarkdown(block.content.trim())"
        >
        </div>
      </div>
    </div>
  `
}

// 响应式数据
const messages = ref([])
const userInput = ref('')
const isLoading = ref(false)
const isDarkMode = ref(false)
const chatContainer = ref(null)
const textareaRef = ref(null)
const memoryId = ref(Date.now().toString())

let controller = null
let typingInterval = null

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

// 切换暗黑模式
const toggleDarkMode = () => {
  isDarkMode.value = !isDarkMode.value
  localStorage.setItem('darkMode', isDarkMode.value)
}

// 新建会话
const startNewConversation = () => {
  messages.value = []
  memoryId.value = Date.now().toString()
  messages.value.push({
    role: 'assistant',
    content: '你好！我是材料库专家提供的AI材料库顾问小白，请问有什么能帮到您？',
    isLoading: false,
    visibleChars: 0,
    isStreaming: false
  })
  messages.value[0].visibleChars = messages.value[0].content.length
  scrollToBottom()
  nextTick(() => {
    if (textareaRef.value) textareaRef.value.focus()
  })
}

// 模拟逐字打印效果
const startTypingEffect = (messageIndex) => {
  const messageItem = messages.value[messageIndex]
  if (!messageItem || messageItem.visibleChars >= messageItem.content.length) {
    clearInterval(typingInterval)
    typingInterval = null
    messages.value[messageIndex].isStreaming = false
    return
  }
  messages.value[messageIndex].visibleChars++
  scrollToBottom()
}

// 发送消息
const sendMessage = async () => {
  if (!userInput.value.trim() || isLoading.value) return

  if (controller) {
    controller.abort()
  }
  controller = new AbortController()

  const userMessage = {
    role: 'user',
    content: userInput.value.trim(),
    isLoading: false,
    visibleChars: userInput.value.trim().length,
    isStreaming: false
  }

  messages.value.push(userMessage)

  const assistantMessage = {
    role: 'assistant',
    content: '',
    isLoading: true,
    visibleChars: 0,
    isStreaming: true
  }

  messages.value.push(assistantMessage)
  userInput.value = ''
  scrollToBottom()
  isLoading.value = true

  try {
    const response = await streamChatApi(userMessage.content)
    /*const response = await fetch(`/material/ai/chat?message=${encodeURIComponent(userMessage.content)}&memoryId=${memoryId.value}`, {
      signal: controller.signal
    })*/

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''
    let messageIndex = messages.value.length - 1

    if (typingInterval) {
      clearInterval(typingInterval)
      typingInterval = null
    }

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      const chunk = decoder.decode(value, { stream: true })
      buffer += chunk
      messages.value[messageIndex].content = buffer
      messages.value[messageIndex].isLoading = false

      if (!typingInterval) {
        typingInterval = setInterval(() => {
          startTypingEffect(messageIndex)
        }, 20)
      }

      scrollToBottom()
    }
  } catch (error) {
    if (error.name === 'AbortError') {
      console.log('请求被用户中止')
    } else {
      console.error('请求出错:', error)
      const lastMessage = messages.value[messages.value.length - 1]
      lastMessage.content = '抱歉，请求过程中出现错误: ' + error.message
      lastMessage.visibleChars = lastMessage.content.length
      message.error('请求失败：' + error.message)
    }
  } finally {
    const lastMessage = messages.value[messages.value.length - 1]
    lastMessage.isLoading = false
    lastMessage.isStreaming = false
    if (lastMessage.visibleChars < lastMessage.content.length) {
      lastMessage.visibleChars = lastMessage.content.length
    }
    isLoading.value = false
    controller = null
    if (typingInterval) {
      clearInterval(typingInterval)
      typingInterval = null
    }
    scrollToBottom()
  }
}

// 停止响应
const stopResponse = () => {
  if (controller) {
    controller.abort()
    const lastMessage = messages.value[messages.value.length - 1]
    lastMessage.isLoading = false
    lastMessage.isStreaming = false
    if (lastMessage.visibleChars < lastMessage.content.length) {
      lastMessage.visibleChars = lastMessage.content.length
    }
    isLoading.value = false
    controller = null
    if (typingInterval) {
      clearInterval(typingInterval)
      typingInterval = null
    }
  }
}

// 初始化
onMounted(() => {
  isDarkMode.value = localStorage.getItem('darkMode') === 'true' ||
      (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches)

  startNewConversation()
})

// 监听消息变化自动滚动
watch(messages, scrollToBottom, { deep: true })
</script>

<style scoped>
.chat-container {
  display: flex;
  height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 侧边栏样式 */
.chat-sidebar {
  width: 22%;
  min-width: 180px;
  max-width: 260px;
  height: 100%;
  border-right: 1px solid #e8e8e8;
  background: #fafafa;
  display: flex;
  flex-direction: column;
  transition: all 0.3s;
}

.chat-sidebar.dark {
  background: #1f1f1f;
  border-right: 1px solid #333;
}

.sidebar-header {
  padding: 18px 15px 10px 15px;
  border-bottom: 1px solid #e8e8e8;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chat-sidebar.dark .sidebar-header {
  background: #262626;
  border-bottom: 1px solid #333;
}

.header-title {
  font-weight: 600;
  font-size: 16px;
  color: #1f1f1f;
}

.chat-sidebar.dark .header-title {
  color: #fff;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.theme-toggle {
  min-width: 32px;
  padding: 4px 8px;
}

.history-list {
  flex: 1;
  overflow-y: auto;
}

.history-item {
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  color: #666;
  transition: all 0.2s;
}

.history-item:hover,
.history-item.active {
  background-color: #e6f7ff;
  color: #1890ff;
}

.chat-sidebar.dark .history-item {
  border-bottom: 1px solid #333;
  color: #ccc;
}

.chat-sidebar.dark .history-item:hover,
.chat-sidebar.dark .history-item.active {
  background-color: #1a1a1a;
  color: #40a9ff;
}

/* 主聊天区域 */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
}

.chat-main.dark {
  background: #0d0d0d;
}

/* 消息展示区域 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #fff;
  transition: background 0.3s;
}

.chat-messages.dark {
  background: #0d0d0d;
}

.message-wrapper {
  margin-bottom: 20px;
  display: flex;
}

.user-wrapper {
  justify-content: flex-end;
}

.ai-wrapper {
  justify-content: flex-start;
}

.message {
  max-width: 80%;
  padding: 12px 16px;
  border-radius: 12px;
  word-break: break-word;
  font-size: 15px;
  line-height: 1.6;
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
  transition: all 0.3s;
}

.user-message {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.ai-message {
  background: #f6f6f6;
  color: #333;
  border-bottom-left-radius: 4px;
}

.chat-messages.dark .ai-message {
  background: #1a1a1a;
  color: #e6e6e6;
}

.typing-cursor {
  opacity: 0.5;
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 50% { opacity: 0.5; }
  51%, 100% { opacity: 0; }
}

/* Think块样式 */
.think-block {
  display: flex;
  align-items: flex-start;
  background: #f0f6ff;
  border-left: 4px solid #1890ff;
  border-radius: 6px;
  padding: 12px 16px;
  margin-bottom: 12px;
  font-style: italic;
  color: #1890ff;
  font-size: 14px;
}

.think-block.dark {
  background: #1a2332;
  color: #69b1ff;
  border-left: 4px solid #40a9ff;
}

.think-icon {
  margin-right: 8px;
  font-size: 16px;
  flex-shrink: 0;
  margin-top: 2px;
}

.think-label {
  font-weight: 600;
  margin-right: 6px;
}

/* AI消息内容样式 */
.ai-message-content-markdown {
  margin-bottom: 8px;
  white-space: pre-wrap;
  word-break: break-word;
}

.ai-message-content-markdown strong {
  font-weight: 600;
}

.ai-message-content-markdown em {
  font-style: italic;
}

.ai-message-content-markdown code {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 13px;
}

.chat-messages.dark .ai-message-content-markdown code {
  background: #262626;
  color: #87ceeb;
}

/* 输入区域 */
.chat-input-area {
  border-top: 1px solid #e8e8e8;
  padding: 20px;
  background: #fff;
  transition: all 0.3s;
}

.chat-input-area.dark {
  background: #0d0d0d;
  border-top: 1px solid #333;
}

.chat-textarea {
  margin-bottom: 12px;
}

.chat-textarea.dark {
  background: #1a1a1a !important;
  border-color: #333 !important;
  color: #e6e6e6 !important;
}

.chat-textarea.dark:focus {
  border-color: #40a9ff !important;
  box-shadow: 0 0 0 2px rgba(64, 169, 255, 0.2) !important;
}

.input-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stop-btn {
  background: #ff7875 !important;
  border-color: #ff7875 !important;
}

.stop-btn:hover {
  background: #ff4d4f !important;
  border-color: #ff4d4f !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-sidebar {
    width: 280px;
    position: absolute;
    left: -280px;
    z-index: 1000;
    transition: left 0.3s;
  }

  .chat-sidebar.open {
    left: 0;
  }

  .chat-main {
    width: 100%;
  }

  .message {
    max-width: 95%;
  }
}

/* 滚动条样式 */
.chat-messages::-webkit-scrollbar,
.history-list::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track,
.history-list::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.chat-messages::-webkit-scrollbar-thumb,
.history-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover,
.history-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.chat-messages.dark::-webkit-scrollbar-track,
.chat-sidebar.dark .history-list::-webkit-scrollbar-track {
  background: #2a2a2a;
}

.chat-messages.dark::-webkit-scrollbar-thumb,
.chat-sidebar.dark .history-list::-webkit-scrollbar-thumb {
  background: #555;
}
</style>