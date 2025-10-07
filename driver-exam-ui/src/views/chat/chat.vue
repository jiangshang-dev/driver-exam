<template>
  <div class="chat-container">
    <div class="chat-history-list" :class="{ dark: isDarkMode }">
      <div class="chat-history-header" :class="{ dark: isDarkMode }">
        <span class="header-title">💬 对话历史</span>
        <div class="header-actions">
          <button class="new-conv-btn" @click="startNewConversation">✨ 新对话</button>
          <button class="dark-toggle-btn" @click="toggleDarkMode">{{ isDarkMode ? '☀️' : '🌙' }}</button>
        </div>
      </div>
      <div class="history-list">
        <div
          class="history-item"
          :class="{ active: activeConversationId === conv.id, dark: isDarkMode }"
          v-for="conv in conversationList"
          :key="conv.id"
          @click="switchConversation(conv.id)"
        >
          {{ conv.title }}
        </div>
      </div>
    </div>

    <div class="chat-main" :class="{ dark: isDarkMode }">
      <div class="chat-content" :class="{ dark: isDarkMode }" ref="chatContainer">
        <div
          v-for="(msg, idx) in activeMessages"
          :key="idx"
          class="message"
          :class="[
            msg.role === 'user' ? 'user-message' : 'ai-message',
            isDarkMode ? 'dark' : ''
          ]"
        >
          <template v-if="msg.role === 'assistant'">
            <span v-if="msg.isLoading || msg.isStreaming" class="loading-indicator">
              <AiMessageContent
                :content="msg.content.slice(0, msg.visibleChars || 0)"
                :dark="isDarkMode"
              />
              <span v-if="msg.isLoading || msg.isStreaming" class="thinking-icon">💭</span>
            </span>
            <span v-else>
              <AiMessageContent
                :content="msg.content"
                :dark="isDarkMode"
              />
            </span>
          </template>
          <template v-else>
            <div class="user-message-content">
              <span class="user-icon">👤</span>
              <span class="user-text">{{ msg.content }}</span>
            </div>
          </template>
        </div>
      </div>

      <div class="chat-input-area" :class="{ dark: isDarkMode }">
        <div class="input-container">
          <textarea
            ref="textareaRef"
            :class="{ dark: isDarkMode }"
            v-model="userInput"
            :disabled="isLoading"
            placeholder="💬 请输入您的问题..."
            @input="adjustTextareaHeight"
            @keydown.enter.exact.prevent="sendMessage"
            @keydown.enter.shift.exact.prevent="userInput += '\n'"
            rows="2"
          ></textarea>
        </div>
        <div class="input-actions">
          <div class="send-actions">
            <button
              @click="sendMessage"
              :disabled="!userInput.trim() || isLoading"
              class="send-btn"
            >
              {{ isLoading ? '✨ 思考中...' : '🚀 发送' }}
            </button>
            <button
              class="stop-btn"
              v-if="isLoading"
              @click="stopResponse"
            >
              ⏹️ 停止
            </button>
          </div>
          <div class="input-hint">
            Press Enter to send • Shift+Enter for new line
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch, computed } from 'vue'
import { ElMessage } from "element-plus"
import baseService from "@/service/baseService";
import AiMessageContent from '@/components/AiMessageContent.vue'

// 定义消息类型
interface Message {
  role: 'user' | 'assistant'
  content: string
  isLoading?: boolean
  visibleChars?: number
  isStreaming?: boolean
}

// 定义会话类型
interface Conversation {
  id: string
  title: string
  messages: Message[]
}

// 响应式数据
const isDarkMode = ref(false)
const userInput = ref('')
const isLoading = ref(false)
const chatContainer = ref<HTMLElement | null>(null)
const textareaRef = ref<HTMLTextAreaElement | null>(null)
const activeConversationId = ref('')
const conversationList = ref<Conversation[]>([])
const abortController = ref<AbortController | null>(null)
let typingTimer: number | null = null

// 计算属性：当前会话的消息
const activeMessages = computed(() => {
  const conv = conversationList.value.find(c => c.id === activeConversationId.value)
  return conv ? conv.messages : []
})

// 调整文本区域高度
const adjustTextareaHeight = () => {
  if (textareaRef.value) {
    textareaRef.value.style.height = 'auto'
    textareaRef.value.style.height = `${Math.min(textareaRef.value.scrollHeight, 200)}px`
  }
}

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
  localStorage.setItem('chatDarkMode', isDarkMode.value.toString())
}

// 新建会话
const startNewConversation = () => {
  const newId = Date.now().toString()
  const newConversation: Conversation = {
    id: newId,
    title: '新对话',
    messages: [{
      role: 'assistant',
      content: '你好！我是您的专业AI技术顾问 🤖，很高兴为您服务！请问有什么技术问题需要我帮助解答吗？',
      isLoading: false,
      visibleChars: 0,
      isStreaming: false
    }]
  }

  conversationList.value.push(newConversation)
  activeConversationId.value = newId

  // 更新第一条消息的可见字符数
  nextTick(() => {
    const conv = conversationList.value.find(c => c.id === newId)
    if (conv && conv.messages.length > 0) {
      conv.messages[0].visibleChars = conv.messages[0].content.length
    }
    scrollToBottom()
    if (textareaRef.value) textareaRef.value.focus()
    adjustTextareaHeight()
  })
}

// 切换会话
const switchConversation = (id: string) => {
  activeConversationId.value = id
  nextTick(() => {
    scrollToBottom()
  })
}

// 模拟逐字打印效果
const startTypingEffect = (messageIndex: number) => {
  const conv = conversationList.value.find(c => c.id === activeConversationId.value)
  if (!conv) return

  const message = conv.messages[messageIndex]
  if (!message || (message.visibleChars && message.visibleChars >= message.content.length)) {
    if (typingTimer) {
      clearInterval(typingTimer)
      typingTimer = null
    }
    conv.messages[messageIndex].isStreaming = false
    return
  }

  if (!message.visibleChars) {
    message.visibleChars = 0
  }
  message.visibleChars++
  scrollToBottom()
}

// 发送消息
const sendMessage = async () => {
  if (!userInput.value.trim() || isLoading.value) return

  // 取消之前的请求
  if (abortController.value) {
    abortController.value.abort()
  }

  abortController.value = new AbortController()

  // 添加用户消息
  const userMessage: Message = {
    role: 'user',
    content: userInput.value.trim(),
    isLoading: false,
    visibleChars: userInput.value.trim().length,
    isStreaming: false
  }

  // 获取当前会话
  let conv = conversationList.value.find(c => c.id === activeConversationId.value)
  if (!conv) {
    // 如果没有会话，创建一个新会话
    startNewConversation()
    conv = conversationList.value.find(c => c.id === activeConversationId.value)
    if (!conv) return
  }

  conv.messages.push(userMessage)

  // 添加AI助手消息
  const assistantMessage: Message = {
    role: 'assistant',
    content: '',
    isLoading: true,
    visibleChars: 0,
    isStreaming: true
  }
  conv.messages.push(assistantMessage)

  // 清空输入框
  userInput.value = ''
  adjustTextareaHeight()
  scrollToBottom()
  isLoading.value = true

  try {
    // 调用AI接口（使用已配置好的接口）
    const response = await baseService.get(`/ai/chat?message=${encodeURIComponent(userMessage.content)}&memoryId=${activeConversationId.value}`).then((res) => {
      return res.data;
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const reader = response.body?.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''
    const messageIndex = conv.messages.length - 1

    // 清除之前的打字效果定时器
    if (typingTimer) {
      clearInterval(typingTimer)
      typingTimer = null
    }

    // 读取流式响应
    while (reader) {
      const { done, value } = await reader.read()
      if (done) break

      const chunk = decoder.decode(value, { stream: true })
      buffer += chunk
      conv.messages[messageIndex].content = buffer
      conv.messages[messageIndex].isLoading = false

      // 启动打字效果
      if (!typingTimer) {
        typingTimer = window.setInterval(() => {
          startTypingEffect(messageIndex)
        }, 20) as unknown as number
      }

      scrollToBottom()
    }
  } catch (error: any) {
    if (error.name === 'AbortError') {
      console.log('请求被用户中止')
    } else {
      console.error('请求出错:', error)
      const conv = conversationList.value.find(c => c.id === activeConversationId.value)
      if (conv) {
        const lastMessage = conv.messages[conv.messages.length - 1]
        lastMessage.content = '抱歉，请求过程中出现错误: ' + error.message
        if (lastMessage.visibleChars !== undefined) {
          lastMessage.visibleChars = lastMessage.content.length
        }
      }
    }
  } finally {
    const conv = conversationList.value.find(c => c.id === activeConversationId.value)
    if (conv) {
      const lastMessage = conv.messages[conv.messages.length - 1]
      lastMessage.isLoading = false
      lastMessage.isStreaming = false
      if (lastMessage.visibleChars !== undefined && lastMessage.visibleChars < lastMessage.content.length) {
        lastMessage.visibleChars = lastMessage.content.length
      }
    }

    isLoading.value = false
    abortController.value = null

    if (typingTimer) {
      clearInterval(typingTimer)
      typingTimer = null
    }

    scrollToBottom()
  }
}

// 停止响应
const stopResponse = () => {
  if (abortController.value) {
    abortController.value.abort()
    const conv = conversationList.value.find(c => c.id === activeConversationId.value)
    if (conv) {
      const lastMessage = conv.messages[conv.messages.length - 1]
      lastMessage.isLoading = false
      lastMessage.isStreaming = false
      if (lastMessage.visibleChars !== undefined && lastMessage.visibleChars < lastMessage.content.length) {
        lastMessage.visibleChars = lastMessage.content.length
      }
    }
    isLoading.value = false
    abortController.value = null
    if (typingTimer) {
      clearInterval(typingTimer)
      typingTimer = null
    }
  }
}

// 组件挂载时初始化
onMounted(() => {
  isDarkMode.value = localStorage.getItem('chatDarkMode') === 'true'
    || (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches)

  // 创建初始会话
  startNewConversation()
})

// 监听消息变化自动滚动
watch(activeMessages, scrollToBottom, { deep: true })
</script>

<style scoped lang="less">
.chat-container {
  display: flex;
  width: 100%;
  height: 100vh;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

.chat-history-list {
  width: 280px;
  min-width: 280px;
  height: 100%;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-right: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 2px 0 20px rgba(0, 0, 0, 0.1);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
}

.chat-history-list.dark {
  background: rgba(30, 35, 41, 0.95);
  border-right: 1px solid rgba(255, 255, 255, 0.1);
}

.chat-history-header {
  padding: 20px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  font-size: 16px;
  color: #2c3e50;
}

.chat-history-header.dark {
  background: rgba(42, 48, 58, 0.8);
  color: #e1e8ed;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.new-conv-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  margin-left: 8px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.new-conv-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.chat-main {
  flex: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(15px);
  border-radius: 20px 0 0 20px;
  margin: 10px 10px 10px 0;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.chat-main.dark {
  background: rgba(35, 39, 47, 0.9);
}

.chat-content {
  flex: 1;
  overflow-y: auto;
  padding: 30px;
  background: transparent;
  scrollbar-width: thin;
  scrollbar-color: rgba(0, 0, 0, 0.2) transparent;
}

.chat-content::-webkit-scrollbar {
  width: 6px;
}

.chat-content::-webkit-scrollbar-track {
  background: transparent;
}

.chat-content::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

.chat-content::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}

.chat-input-area {
  padding: 20px 30px 30px 30px;
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(0, 0, 0, 0.08);
}

.chat-input-area.dark {
  background: rgba(42, 48, 58, 0.5);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

textarea {
  width: 100%;
  min-height: 50px;
  max-height: 200px;
  padding: 16px 20px;
  border: 2px solid rgba(102, 126, 234, 0.2);
  border-radius: 25px;
  resize: none;
  font-size: 15px;
  background: rgba(255, 255, 255, 0.8);
  color: #2c3e50;
  transition: all 0.3s ease;
  outline: none;
  font-family: inherit;
  line-height: 1.5;
}

textarea:focus {
  border-color: #667eea;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

textarea.dark {
  background: rgba(42, 48, 58, 0.8);
  color: #e1e8ed;
  border: 2px solid rgba(255, 255, 255, 0.1);
}

textarea.dark:focus {
  border-color: #667eea;
  background: rgba(42, 48, 58, 0.95);
}

.send-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 25px;
  cursor: pointer;
  margin-top: 15px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.send-btn:disabled {
  background: linear-gradient(135deg, #bdc3c7 0%, #95a5a6 100%);
  cursor: not-allowed;
  box-shadow: none;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.stop-btn {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  color: #fff;
  margin-left: 12px;
  padding: 12px 20px;
  box-shadow: 0 4px 15px rgba(255, 107, 107, 0.3);
  border: none;
  border-radius: 25px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  margin-top: 15px;
}

.stop-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 107, 107, 0.4);
}

.history-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.history-item {
  padding: 16px 20px;
  margin: 5px 0;
  border-radius: 12px;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.6);
  transition: all 0.3s ease;
  font-size: 14px;
  color: #2c3e50;
}

.history-item.active, .history-item:hover {
  background: rgba(102, 126, 234, 0.1);
  transform: translateX(5px);
}

.history-item.dark {
  background: rgba(42, 48, 58, 0.6);
  color: #e1e8ed;
}

.history-item.dark.active, .history-item.dark:hover {
  background: rgba(102, 126, 234, 0.2);
}

.message {
  margin-bottom: 24px;
  padding: 16px 20px;
  border-radius: 18px;
  max-width: 85%;
  word-break: break-word;
  font-size: 15px;
  line-height: 1.6;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  animation: messageSlideIn 0.3s ease-out;
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.user-message {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  margin-left: auto;
  text-align: left;
  border-bottom-right-radius: 6px;
}

.ai-message {
  background: rgba(255, 255, 255, 0.9);
  color: #2c3e50;
  border-bottom-left-radius: 6px;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.message.dark {
  color: #e1e8ed;
}

.ai-message.dark {
  background: rgba(42, 48, 58, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.user-message.dark {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.loading-indicator {
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.thinking-icon {
  opacity: 0.7;
  font-size: 18px;
}

.user-message-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-icon {
  font-size: 16px;
}

.input-container {
  position: relative;
}

.input-container::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 27px;
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: -1;
}

.input-container:focus-within::before {
  opacity: 0.2;
}

.input-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 15px;
}

.send-actions {
  display: flex;
  align-items: center;
}

.input-hint {
  font-size: 12px;
  color: #94a3b8;
  font-style: italic;
}

.dark-toggle-btn {
  background: rgba(52, 58, 64, 0.8);
  color: #fff;
  border: none;
  padding: 8px 12px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  margin-left: 8px;
}

.dark-toggle-btn:hover {
  background: rgba(35, 39, 47, 0.9);
  transform: scale(1.05);
}

.header-actions {
  display: flex;
  align-items: center;
}

.header-title {
  font-weight: 600;
  font-size: 16px;
}

/* AI消息内容组件样式 */
.think-block {
  display: flex;
  align-items: flex-start;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-left: 4px solid #667eea;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 16px;
  font-style: italic;
  color: #4a5568;
  font-size: 15px;
  position: relative;
  backdrop-filter: blur(5px);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);
}

.think-block .think-icon {
  margin-right: 12px;
  font-size: 20px;
  flex-shrink: 0;
  margin-top: 2px;
  filter: drop-shadow(0 1px 2px rgba(102, 126, 234, 0.3));
}

.think-block.dark {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(118, 75, 162, 0.15) 100%);
  color: #cbd5e0;
  border-left: 4px solid #667eea;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.think-block .think-label {
  font-weight: 600;
  margin-right: 8px;
  color: #667eea;
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.think-block.dark .think-label {
  color: #a78bfa;
}

.ai-message-content-paragraph {
  margin-bottom: 12px;
  white-space: pre-wrap;
  word-break: break-word;
}

/* Markdown Table Styles */
.ai-message-content-markdown table {
  border-collapse: collapse;
  width: 100%;
  margin-bottom: 16px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.ai-message-content-markdown th,
.ai-message-content-markdown td {
  border: 1px solid rgba(102, 126, 234, 0.2);
  padding: 12px 16px;
  text-align: left;
}

.ai-message-content-markdown th {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  font-weight: 600;
  color: #4a5568;
}

.ai-message-content-markdown td {
  background: rgba(255, 255, 255, 0.8);
}

.chat-content.dark .ai-message-content-markdown table {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.chat-content.dark .ai-message-content-markdown th {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.2) 0%, rgba(118, 75, 162, 0.2) 100%);
  color: #cbd5e0;
  border-color: rgba(255, 255, 255, 0.1);
}

.chat-content.dark .ai-message-content-markdown td {
  background: rgba(42, 48, 58, 0.8);
  color: #e1e8ed;
  border-color: rgba(255, 255, 255, 0.1);
}

/* Markdown code block */
.ai-message-content-markdown pre {
  background: rgba(248, 250, 252, 0.9);
  border-radius: 12px;
  padding: 16px 20px;
  overflow-x: auto;
  margin-bottom: 16px;
  border: 1px solid rgba(102, 126, 234, 0.1);
  font-family: 'SF Mono', Monaco, 'Cascadia Code', 'Roboto Mono', Consolas, 'Courier New', monospace;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.chat-content.dark .ai-message-content-markdown pre {
  background: rgba(42, 48, 58, 0.9);
  color: #e1e8ed;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.ai-message-content-markdown code {
  background: rgba(102, 126, 234, 0.1);
  border-radius: 6px;
  padding: 3px 6px;
  font-size: 14px;
  font-family: 'SF Mono', Monaco, 'Cascadia Code', 'Roboto Mono', Consolas, 'Courier New', monospace;
  color: #667eea;
  font-weight: 500;
}

.chat-content.dark .ai-message-content-markdown code {
  background: rgba(102, 126, 234, 0.2);
  color: #a78bfa;
}

.ai-message-content-markdown ul,
.ai-message-content-markdown ol {
  margin-left: 24px;
  margin-bottom: 12px;
}

.ai-message-content-markdown li {
  margin-bottom: 4px;
}

.ai-message-content-markdown blockquote {
  border-left: 4px solid #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  color: #4a5568;
  margin: 16px 0;
  padding: 16px 20px;
  border-radius: 0 12px 12px 0;
  font-style: italic;
}

.chat-content.dark .ai-message-content-markdown blockquote {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  color: #cbd5e0;
  border-left: 4px solid #667eea;
}

/* 自定义滚动条样式 */
.history-list::-webkit-scrollbar {
  width: 6px;
}

.history-list::-webkit-scrollbar-track {
  background: transparent;
}

.history-list::-webkit-scrollbar-thumb {
  background: rgba(102, 126, 234, 0.3);
  border-radius: 3px;
}

.history-list::-webkit-scrollbar-thumb:hover {
  background: rgba(102, 126, 234, 0.5);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-history-list {
    width: 260px;
    position: absolute;
    z-index: 1000;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
  }

  .chat-history-list.mobile-open {
    transform: translateX(0);
  }

  .chat-main {
    margin: 10px;
    border-radius: 20px;
  }
}
</style>
