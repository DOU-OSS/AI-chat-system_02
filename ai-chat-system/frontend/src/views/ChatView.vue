<template>
  <div class="chat-app">
    <!-- 动态背景 -->
    <div class="background-animation" :style="{ backgroundColor: backgroundColor }">
      <div class="floating-orb orb-1"></div>
      <div class="floating-orb orb-2"></div>
      <div class="floating-orb orb-3"></div>
    </div>

    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="header-top">
          <div class="logo">
            <span class="logo-icon">🤖</span>
            <span class="logo-text">AI Chat</span>
          </div>
          <div 
            class="user-avatar-header" 
            @click="previewAvatar"
          >
            <div class="user-avatar">
              <img 
                v-if="currentUser?.avatar" 
                :src="currentUser.avatar" 
                :alt="currentUser?.nickname || currentUser?.username"
                class="avatar-image"
              />
              <span v-else>{{ currentUser?.nickname?.charAt(0) || currentUser?.username?.charAt(0) || 'U' }}</span>
            </div>
          </div>
        </div>
        <button @click="goToRoles" class="roles-btn">
          <span class="icon">🎭</span>
          <span>AI Roles</span>
        </button>
        <button @click="createNewChat" class="new-chat-btn">
          <span class="icon">✨</span>
          <span>New Chat</span>
        </button>
      </div>

      <div class="conversations">
        <div class="section-title">Conversations</div>
        <div
          v-for="conv in conversations"
          :key="conv.id"
          :class="['conversation-item', { active: currentConversation?.id === conv.id }]"
          @click="selectConversation(conv)"
        >
          <div class="conversation-content">
            <div class="conversation-icon">💬</div>
            <div class="conversation-info">
              <div class="conversation-title">{{ conv.title }}</div>
              <div class="conversation-time">{{ formatTime(conv.lastMessageAt) }}</div>
            </div>
          </div>
          <button 
            class="delete-conversation-btn"
            @click.stop="deleteConversationItem(conv.id)"
            title="Delete Conversation"
          >
            <span>🗑️</span>
          </button>
        </div>
      </div>

      <div class="sidebar-footer">
        <button @click="goToRecycleBin" class="recycle-bin-btn">
          <span class="icon">🗑️</span>
          <span>Recycle Bin</span>
        </button>
        <div class="user-action-buttons">
          <button @click="goToProfile" class="action-btn profile-btn">
            <span class="btn-icon">👤</span>
            <span class="btn-text">Profile</span>
          </button>
          <button @click="logout" class="action-btn logout-btn">
            <span class="btn-icon">🚪</span>
            <span class="btn-text">Logout</span>
          </button>
        </div>
      </div>
    </aside>

    <!-- 主聊天区域 -->
    <main class="main-content">
      <div v-if="!currentConversation" class="empty-state">
        <div class="empty-icon">💭</div>
        <h2>Welcome to AI Chat</h2>
        <p>Select a conversation or create a new one to start chatting</p>
        <button @click="createNewChat" class="start-btn">
          <span>🚀</span>
          Start New Conversation
        </button>
      </div>

      <div v-else class="chat-container">
        <!-- 聊天头部 -->
        <header class="chat-header">
          <div class="header-left">
            <h3>{{ currentConversation.title }}</h3>
            <div class="model-selector">
              <select 
                v-model="selectedModel" 
                @change="handleModelChange"
                :disabled="sending"
                class="model-select"
              >
                <option 
                  v-for="model in availableModels" 
                  :key="model.id" 
                  :value="model.id"
                  :disabled="!model.available"
                >
                  {{ model.name }}
                </option>
              </select>
            </div>
          </div>
          <div class="header-actions">
            <div class="theme-switcher">
              <button 
                @click="setTheme('white')" 
                :class="['theme-btn', { active: currentTheme === 'white' }]"
                title="白色主题"
              >
                <span>⚪</span>
              </button>
              <button 
                @click="setTheme('gray')" 
                :class="['theme-btn', { active: currentTheme === 'gray' }]"
                title="灰色主题"
              >
                <span>⚫</span>
              </button>
              <button 
                @click="setTheme('green')" 
                :class="['theme-btn', { active: currentTheme === 'green' }]"
                title="护眼绿"
              >
                <span>🟢</span>
              </button>
            </div>
          </div>
        </header>

        <!-- 消息区域 -->
        <div ref="messagesContainer" class="messages-area">
          <!-- 空状态：有会话但没有消息时显示角色描述 -->
          <div v-if="currentMessages && currentMessages.length === 0 && currentConversation?.aiRoleDescription" class="role-description-empty">
            <div class="role-description-card">
              <div class="role-avatar-large">
                <span>🤖</span>
              </div>
              <h3>{{ currentConversation.aiRoleName || 'AI Assistant' }}</h3>
              <p class="role-description-text">{{ currentConversation.aiRoleDescription }}</p>
            </div>
          </div>
          
          <div
            v-for="msg in currentMessages"
            :key="msg.id"
            :class="['message', msg.role.toLowerCase()]"
          >
            <div class="message-wrapper">
              <div class="message-avatar" :class="msg.role">
                <img 
                  v-if="(msg.role?.toLowerCase() === 'user') && currentUser?.avatar" 
                  :src="currentUser.avatar" 
                  :alt="currentUser?.nickname || currentUser?.username"
                  class="avatar-image"
                />
                <span v-else-if="msg.role?.toLowerCase() === 'user'">{{ currentUser?.nickname?.charAt(0) || currentUser?.username?.charAt(0) || 'U' }}</span>
                <span v-else>🤖</span>
              </div>
              <div class="message-content">
                <!-- 思考过程（如果有） -->
                <div v-if="parseThinking(msg.content).thinking" class="thinking-section">
                  <div class="thinking-header" @click="toggleThinking(msg.id)">
                    <span>🤔 思考过程</span>
                    <span class="toggle-icon">{{ isThinkingExpanded(msg.id) ? '▼' : '▶' }}</span>
                  </div>
                  <div v-show="isThinkingExpanded(msg.id)" class="thinking-content">
                    <div class="thinking-text">{{ parseThinking(msg.content).thinking }}</div>
                  </div>
                </div>
                
                <!-- 最终答案 -->
                <div class="message-text" v-html="renderMarkdown(parseThinking(msg.content).answer)"></div>
                <div class="message-footer">
                  <div class="message-time">{{ formatMessageTime(msg.createdAt) }}</div>
                  <div class="message-status" v-if="msg.role?.toLowerCase() === 'user'">
                    <span v-if="msg.status === 'sending'" class="status-indicator sending">
                      <span class="sending-dot"></span>
                      Sending...
                    </span>
                    <span v-else-if="msg.status === 'sent'" class="status-indicator sent">
                      ✓ Sent
                    </span>
                    <span v-else-if="msg.status === 'failed'" class="status-indicator failed">
                      ✗ Failed
                    </span>
                  </div>
                </div>
                
                <!-- 消息操作按钮 -->
                <div class="message-actions">
                  <button @click="copyMessage(msg.content)" class="action-btn copy-btn" title="Copy message">
                    <span>📋</span>
                  </button>
                  <button 
                    v-if="msg.role?.toLowerCase() === 'assistant'"
                    @click="toggleFeedback(msg, 'like')" 
                    :class="['action-btn', 'feedback-btn', { active: msg.feedback === 'like' }]"
                    title="Like this response"
                  >
                    <span>👍</span>
                  </button>
                  <button 
                    v-if="msg.role?.toLowerCase() === 'assistant'"
                    @click="toggleFeedback(msg, 'dislike')" 
                    :class="['action-btn', 'feedback-btn', { active: msg.feedback === 'dislike' }]"
                    title="Dislike this response"
                  >
                    <span>👎</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 正在输入指示器 -->
          <div v-if="sending" class="message assistant typing">
            <div class="message-wrapper">
              <div class="message-avatar">
                <span>🤖</span>
              </div>
              <div class="message-content">
                <div class="typing-indicator">
                  <span class="dot"></span>
                  <span class="dot"></span>
                  <span class="dot"></span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <div class="input-container">
            <div class="input-wrapper">
              <textarea
                ref="messageInput"
                v-model="inputMessage"
                placeholder="Type your message here... (Enter/Ctrl+Enter to send, Shift+Enter for new line, Esc/Ctrl+K to clear)"
                @keydown.enter.exact.prevent="sendMessage"
                @keydown.enter.shift.prevent=""
                @keydown.ctrl.enter.prevent="sendMessage"
                @keydown.escape="clearInput"
                @keydown="handleKeyDown"
                @input="autoResizeTextarea"
                :disabled="sending"
                class="message-input"
                rows="1"
              ></textarea>
              <button
                @click="sendMessage"
                :disabled="!inputMessage.trim() || sending"
                class="send-btn"
                title="Send message"
              >
                <span v-if="!sending">📤</span>
                <span v-else class="loading-spinner">⏳</span>
              </button>
            </div>
            <div class="input-options">
              <label v-show="supportsThinking" class="thinking-toggle">
                <input type="checkbox" v-model="enableThinking" />
                <span class="toggle-label">
                  <span>🤔</span>
                  Enable Thinking Mode
                </span>
              </label>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 头像预览对话框 -->
    <el-dialog
      v-model="showAvatarPreview"
      title="Avatar Preview"
      width="500px"
      align-center
      class="avatar-preview-dialog"
    >
      <div class="avatar-preview-container">
        <img 
          v-if="currentUser?.avatar" 
          :src="currentUser.avatar" 
          :alt="currentUser?.nickname || currentUser?.username"
          class="avatar-preview-image"
        />
        <div v-else class="avatar-preview-placeholder">
          <span>{{ currentUser?.nickname?.charAt(0) || currentUser?.username?.charAt(0) || 'U' }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="showAvatarPreview = false">Close</el-button>
        <el-button type="primary" @click="triggerAvatarUpload">
          Change Avatar
        </el-button>
        <el-button 
          v-if="currentUser?.avatar"
          type="danger" 
          @click="removeAvatar"
        >
          Remove Avatar
        </el-button>
      </template>
    </el-dialog>

    <!-- 隐藏的文件上传输入 -->
    <input
      ref="avatarFileInput"
      type="file"
      accept="image/*"
      style="display: none"
      @change="handleAvatarUpload"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '../stores/chat'
import { useAuthStore } from '../stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { marked } from 'marked'
import request from '../utils/request'
import { updateConversationModel } from '../api/conversation'

const router = useRouter()
const chatStore = useChatStore()
const authStore = useAuthStore()

const inputMessage = ref('')
const messagesContainer = ref()
const messageInput = ref()
const selectedModel = ref('')
const availableModels = ref([])
const loadingModels = ref(false)
const enableThinking = ref(false)
const currentTheme = ref('gray')
const expandedThinking = ref(new Set())
const showAvatarPreview = ref(false)
const avatarFileInput = ref(null)

// 主题配色方案
const themes = {
  white: {
    name: '白色',
    bgColor: '#ffffff',
    textColor: '#1f2937',
    messageAreaBg: '#f9fafb',
    userMsgBg: '#2563eb',
    userMsgText: '#ffffff',
    assistantMsgBg: '#e5e7eb',
    assistantMsgText: '#1f2937',
    inputBg: '#ffffff',
    inputBorder: '#d1d5db',
    timestampColor: 'rgba(107, 114, 128, 0.8)'
  },
  gray: {
    name: '灰色',
    bgColor: '#1f2937',
    textColor: '#e5e7eb',
    messageAreaBg: '#111827',
    userMsgBg: '#2563eb',
    userMsgText: '#ffffff',
    assistantMsgBg: 'rgba(31, 41, 55, 0.96)',
    assistantMsgText: '#ffffff',
    inputBg: 'rgba(31, 41, 55, 0.5)',
    inputBorder: 'rgba(75, 85, 99, 0.5)',
    timestampColor: 'rgba(156, 163, 175, 0.8)'
  },
  green: {
    name: '护眼绿',
    bgColor: '#c7edcc',
    textColor: '#1e3a20',
    messageAreaBg: '#e8f5e9',
    userMsgBg: '#2e7d32',
    userMsgText: '#ffffff',
    assistantMsgBg: '#a5d6a7',
    assistantMsgText: '#1b5e20',
    inputBg: '#ffffff',
    inputBorder: '#81c784',
    timestampColor: 'rgba(46, 125, 50, 0.7)'
  }
}

const backgroundColor = computed(() => themes[currentTheme.value].bgColor)
const textColor = computed(() => themes[currentTheme.value].textColor)

const supportsThinking = computed(() => {
  const model = selectedModel.value?.toLowerCase() || ''
  return model.includes('glm-4') || 
         model.includes('gpt-4') ||
         model.includes('claude') ||
         model.includes('qwen') ||
         model.includes('o1')
})

const currentUser = computed(() => authStore.currentUser)
const conversations = computed(() => chatStore.conversations)
const currentConversation = computed(() => chatStore.currentConversation)
const currentMessages = computed(() => chatStore.currentMessages)
const sending = computed(() => chatStore.sending)

marked.setOptions({
  breaks: true,
  gfm: true
})

const renderMarkdown = (text) => {
  if (!text) return ''
  return marked(text)
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return 'Just now'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}m ago`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}h ago`
  return date.toLocaleDateString()
}

// 获取最近使用的模型
const getLastUsedModel = () => {
  try {
    return localStorage.getItem('lastUsedModel') || null
  } catch (e) {
    console.error('Failed to get last used model:', e)
    return null
  }
}

// 保存最近使用的模型
const saveLastUsedModel = (model) => {
  try {
    localStorage.setItem('lastUsedModel', model)
  } catch (e) {
    console.error('Failed to save last used model:', e)
  }
}

// 设置主题
const setTheme = (themeName) => {
  currentTheme.value = themeName
  localStorage.setItem('chatTheme', themeName)
  ElMessage.success(`已切换到${themes[themeName].name}主题`)
}

const formatMessageTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  // Today
  if (days === 0) {
    if (minutes < 1) return 'Just now'
    if (minutes < 60) return `${minutes}m ago`
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }
  
  // Yesterday
  if (days === 1) return `Yesterday ${date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`
  
  // This week
  if (days < 7) return date.toLocaleDateString([], { weekday: 'short' }) + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  
  // Older
  return date.toLocaleDateString([], { month: 'short', day: 'numeric' }) + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

const createNewChat = async () => {
  try {
    const { value: title } = await ElMessageBox.prompt(
      'Enter a title for the new conversation',
      'New Conversation',
      {
        confirmButtonText: 'Create',
        cancelButtonText: 'Cancel',
        inputValue: 'New Conversation'
      }
    )
    
    if (title) {
      // 创建新会话时，使用最近使用的模型（如果可用）
      let modelToUse = selectedModel.value
      const lastUsed = getLastUsedModel()
      if (lastUsed) {
        const lastUsedAvailable = availableModels.value.find(m => m.id === lastUsed && m.available)
        if (lastUsedAvailable) {
          modelToUse = lastUsed
          selectedModel.value = lastUsed
        }
      }
      // 创建对话时保存当前选择的模型
      await chatStore.createConversation(title, null, modelToUse)
    }
  } catch (error) {
    // User cancelled
  }
}

const selectConversation = async (conv) => {
  await chatStore.loadConversation(conv.id)
  // 恢复对话保存的模型选择，如果没有则使用最近使用的模型
  if (chatStore.currentConversation?.selectedModel) {
    const savedModel = chatStore.currentConversation.selectedModel
    // 检查保存的模型是否仍然可用
    const modelAvailable = availableModels.value.find(m => m.id === savedModel && m.available)
    if (modelAvailable) {
      selectedModel.value = savedModel
    } else {
      // 如果保存的模型不可用，使用最近使用的模型
      const lastUsed = getLastUsedModel()
      const lastUsedAvailable = availableModels.value.find(m => m.id === lastUsed && m.available)
      if (lastUsedAvailable) {
        selectedModel.value = lastUsed
      } else {
        // 如果最近使用的模型也不可用，使用第一个可用模型
        const firstAvailable = availableModels.value.find(m => m.available)
        if (firstAvailable) {
          selectedModel.value = firstAvailable.id
        }
      }
    }
  } else {
    // 如果没有保存的模型，使用最近使用的模型
    const lastUsed = getLastUsedModel()
    const lastUsedAvailable = availableModels.value.find(m => m.id === lastUsed && m.available)
    if (lastUsedAvailable) {
      selectedModel.value = lastUsed
    } else {
      // 如果最近使用的模型不可用，使用第一个可用模型
      const firstAvailable = availableModels.value.find(m => m.available)
      if (firstAvailable) {
        selectedModel.value = firstAvailable.id
      }
    }
  }
  // 延迟滚动，确保消息已加载
  setTimeout(() => scrollToBottom(true), 200)
}

const sendMessage = async () => {
  const message = inputMessage.value.trim()
  if (!message) return

  inputMessage.value = ''
  
  // 立即滚动到底部（显示用户消息）
  scrollToBottom(true)
  
  // 如果启用thinking模式，添加特殊标记
  let finalMessage = message
  if (enableThinking.value && supportsThinking.value) {
    finalMessage = `[THINKING_MODE]\n${message}`
  }
  
  try {
    // 保存当前使用的模型为最近一次使用的模型
    saveLastUsedModel(selectedModel.value)
    
    await chatStore.sendMessage(finalMessage, selectedModel.value)
    // 消息发送后再次滚动
    scrollToBottom()
  } catch (error) {
    // 如果发送失败，可以在这里设置消息状态
    console.error('Failed to send message:', error)
    ElMessage.error('Failed to send message. Please try again.')
  }
}

const deleteChat = async () => {
  try {
    const { value } = await ElMessageBox.confirm(
      'Are you sure you want to delete this conversation?',
      'Delete Conversation',
      {
        confirmButtonText: 'Move to Recycle Bin',
        cancelButtonText: 'Cancel',
        distinguishCancelAndClose: true,
        type: 'warning',
        showClose: false
      }
    )
    
    await chatStore.deleteConversation(currentConversation.value.id, false)
  } catch (error) {
    // User cancelled
  }
}

const goToRecycleBin = () => {
  router.push('/recycle-bin')
}

const previewAvatar = () => {
  showAvatarPreview.value = true
}

const triggerAvatarUpload = () => {
  showAvatarPreview.value = false
  avatarFileInput.value?.click()
}

const handleAvatarUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('Please select an image file')
    return
  }
  
  // 验证文件大小（最大 5MB）
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('Image size should be less than 5MB')
    return
  }
  
  try {
    // 将文件转换为 base64
    const reader = new FileReader()
    reader.onload = async (e) => {
      const base64String = e.target.result
      
      try {
        // 更新头像
        const response = await request({
          url: '/auth/profile',
          method: 'put',
          data: {
            avatar: base64String
          }
        })
        
        // 更新本地用户信息
        if (authStore.user) {
          authStore.user.avatar = base64String
          localStorage.setItem('user', JSON.stringify(authStore.user))
        }
        
        ElMessage.success('Avatar updated successfully')
      } catch (error) {
        ElMessage.error(error.response?.data?.message || 'Failed to update avatar')
      }
    }
    
    reader.readAsDataURL(file)
  } catch (error) {
    ElMessage.error('Failed to read image file')
  }
  
  // 清空 input，以便可以再次选择同一文件
  event.target.value = ''
}

const removeAvatar = async () => {
  try {
    await ElMessageBox.confirm(
      'Are you sure you want to remove your avatar?',
      'Remove Avatar',
      {
        confirmButtonText: 'Remove',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }
    )
    
    // 移除头像
    await request({
      url: '/auth/profile',
      method: 'put',
      data: {
        avatar: null
      }
    })
    
    // 更新本地用户信息
    if (authStore.user) {
      authStore.user.avatar = null
      localStorage.setItem('user', JSON.stringify(authStore.user))
    }
    
    showAvatarPreview.value = false
    ElMessage.success('Avatar removed successfully')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || 'Failed to remove avatar')
    }
  }
}

const deleteConversationItem = async (conversationId) => {
  try {
    const { value } = await ElMessageBox.confirm(
      'Are you sure you want to delete this conversation?',
      'Delete Conversation',
      {
        confirmButtonText: 'Move to Recycle Bin',
        cancelButtonText: 'Cancel',
        distinguishCancelAndClose: true,
        type: 'warning',
        showClose: false
      }
    )
    
    await chatStore.deleteConversation(conversationId, false)
  } catch (error) {
    // User cancelled
  }
}

const scrollToBottom = async (force = false) => {
  // 等待DOM更新
  await nextTick()
  
  if (messagesContainer.value) {
    // 使用smooth滚动，除非是强制滚动
    messagesContainer.value.scrollTo({
      top: messagesContainer.value.scrollHeight,
      behavior: force ? 'auto' : 'smooth'
    })
    
    // 再次确保滚动到底部（处理异步加载的内容）
    setTimeout(() => {
      if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
      }
    }, 100)
  }
}

const goToRoles = () => {
  router.push('/roles')
}

const goToProfile = () => {
  router.push('/profile')
}

const handleModelChange = async (model) => {
  const selectedModelInfo = availableModels.value.find(m => m.id === model)
  if (selectedModelInfo) {
    ElMessage.success(`Switched to ${selectedModelInfo.name}`)
  }
  
  // 保存模型选择到当前会话
  if (chatStore.currentConversation?.id) {
    try {
      await updateConversationModel(chatStore.currentConversation.id, model)
      // 更新当前对话的模型选择
      if (chatStore.currentConversation) {
        chatStore.currentConversation.selectedModel = model
      }
    } catch (error) {
      console.error('Failed to update conversation model:', error)
      // 不显示错误消息，因为这只是保存用户偏好，不影响主要功能
    }
  }
  
  // 保存为最近使用的模型
  saveLastUsedModel(model)
}

const copyMessage = async (content) => {
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('Message copied to clipboard!')
  } catch (error) {
    // Fallback for older browsers
    const textArea = document.createElement('textarea')
    textArea.value = content
    document.body.appendChild(textArea)
    textArea.select()
    document.execCommand('copy')
    document.body.removeChild(textArea)
    ElMessage.success('Message copied to clipboard!')
  }
}

const handleKeyDown = (event) => {
  // Ctrl+/ to toggle thinking mode
  if (event.ctrlKey && event.key === '/') {
    event.preventDefault()
    if (supportsThinking.value) {
      enableThinking.value = !enableThinking.value
      ElMessage.success(`Thinking mode ${enableThinking.value ? 'enabled' : 'disabled'}`)
    }
  }
  
  // Ctrl+K to clear input
  if (event.ctrlKey && event.key === 'k') {
    event.preventDefault()
    clearInput()
  }
}

const clearInput = () => {
  inputMessage.value = ''
  autoResizeTextarea()
  ElMessage.info('Input cleared')
}

const autoResizeTextarea = () => {
  if (messageInput.value) {
    messageInput.value.style.height = 'auto'
    const newHeight = Math.min(messageInput.value.scrollHeight, 200)
    messageInput.value.style.height = newHeight + 'px'
  }
}

// 解析思考过程
 const parseThinking = (content) => {
  if (!content) return { thinking: null, answer: content }
  
  const thinkingMatch = content.match(/<thinking>([\s\S]*?)<\/thinking>/)
  const answerMatch = content.match(/<answer>([\s\S]*?)<\/answer>/)
  
  if (thinkingMatch && answerMatch) {
    return {
      thinking: thinkingMatch[1].trim(),
      answer: answerMatch[1].trim()
    }
  }
  
  return { thinking: null, answer: content }
}

// 切换思考过程展开/折叠
const toggleThinking = (messageId) => {
  if (expandedThinking.value.has(messageId)) {
    expandedThinking.value.delete(messageId)
  } else {
    expandedThinking.value.add(messageId)
  }
}

const isThinkingExpanded = (messageId) => {
  return expandedThinking.value.has(messageId)
}

const loadAvailableModels = async () => {
  try {
    loadingModels.value = true
    const response = await request({
      url: '/ai-models',
      method: 'get'
    })
    
    if (response.data && response.data.length > 0) {
      availableModels.value = response.data
      
      // 如果有当前会话且会话有保存的模型，优先使用会话的模型
      if (chatStore.currentConversation?.selectedModel) {
        const savedModel = chatStore.currentConversation.selectedModel
        const savedModelAvailable = response.data.find(m => m.id === savedModel && m.available)
        if (savedModelAvailable) {
          selectedModel.value = savedModel
          return
        }
      }
      
      // 如果没有当前会话或会话没有保存的模型，使用最近使用的模型
      const lastUsed = getLastUsedModel()
      if (lastUsed) {
        const lastUsedAvailable = response.data.find(m => m.id === lastUsed && m.available)
        if (lastUsedAvailable) {
          selectedModel.value = lastUsed
          return
        }
      }
      
      // 如果最近使用的模型不可用，使用第一个可用模型
      const firstAvailable = response.data.find(m => m.available)
      if (firstAvailable) {
        selectedModel.value = firstAvailable.id
      }
    }
  } catch (error) {
    console.error('Failed to load AI models:', error)
    ElMessage.warning('Failed to load AI models. Using default.')
  } finally {
    loadingModels.value = false
  }
}

const logout = async () => {
  try {
    await ElMessageBox.confirm(
      'Are you sure you want to logout?',
      'Logout',
      {
        confirmButtonText: 'Logout',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }
    )
    
    authStore.logout()
    router.push('/login')
  } catch (error) {
    // User cancelled
  }
}

// Watch for new messages to scroll
watch(currentMessages, () => {
  scrollToBottom()
}, { deep: true })

// Watch for sending state change
watch(sending, (newVal) => {
  if (!newVal) {
    // 当sending变为false时（AI回复完成），滚动到底部
    setTimeout(() => scrollToBottom(), 100)
  }
})

onMounted(async () => {
  await chatStore.loadConversations()
  await loadAvailableModels()
  
  // 加载保存的主题
  const savedTheme = localStorage.getItem('chatTheme')
  if (savedTheme && themes[savedTheme]) {
    currentTheme.value = savedTheme
  }
  
  // 确保滚动到底部
  setTimeout(() => scrollToBottom(true), 300)
})
</script>

<style scoped>
/* 全局样式 */
.chat-app {
  display: flex;
  height: 100vh;
  /* 背景改为更柔和的深色渐变 */
  background: radial-gradient(circle at top, #1f2937 0%, #020617 55%, #000000 100%);
  color: v-bind(textColor);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-size: 14px;
  line-height: 1.6;
  position: relative;
  overflow: hidden;
}

/* 动态背景 */
.background-animation {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.floating-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(40px);
  animation: float 20s infinite ease-in-out;
}

.orb-1 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(0, 188, 212, 0.3) 0%, transparent 70%);
  top: -150px;
  left: -150px;
  animation-delay: 0s;
}

.orb-2 {
  width: 250px;
  height: 250px;
  background: radial-gradient(circle, rgba(33, 150, 243, 0.3) 0%, transparent 70%);
  bottom: -125px;
  right: -125px;
  animation-delay: 5s;
}

.orb-3 {
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(156, 39, 176, 0.2) 0%, transparent 70%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: 10s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(30px, -30px) scale(1.1); }
  50% { transform: translate(-20px, 20px) scale(0.9); }
  75% { transform: translate(-30px, -20px) scale(1.05); }
}

/* 侧边栏 */
.sidebar {
  width: 320px;
  background: rgba(26, 31, 58, 0.8);
  backdrop-filter: blur(20px);
  border-right: 1px solid rgba(0, 188, 212, 0.2);
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 10;
}

.sidebar-header {
  padding: 24px;
  background: rgba(17, 21, 39, 0.6);
  border-bottom: 1px solid rgba(0, 188, 212, 0.2);
}

.header-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.user-avatar-header {
  cursor: pointer;
  transition: transform 0.2s;
}

.user-avatar-header:hover {
  transform: scale(1.05);
}

/* 头像预览对话框样式 */
:deep(.avatar-preview-dialog) {
  .el-dialog__body {
    padding: 30px;
    display: flex;
    justify-content: center;
    align-items: center;
  }
}

.avatar-preview-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  min-height: 300px;
}

.avatar-preview-image {
  max-width: 100%;
  max-height: 400px;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
  object-fit: contain;
}

.avatar-preview-placeholder {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: linear-gradient(135deg, #00bcd4, #2196f3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 80px;
  font-weight: 700;
  color: white;
  box-shadow: 0 8px 24px rgba(0, 188, 212, 0.4);
}


.logo-icon {
  font-size: 32px;
  filter: drop-shadow(0 0 12px rgba(0, 188, 212, 0.6));
}

.logo-text {
  font-size: 24px;
  font-weight: 700;
  background: linear-gradient(135deg, #00bcd4, #2196f3);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.roles-btn, .new-chat-btn {
  width: 100%;
  padding: 14px 16px;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.roles-btn {
  background: rgba(156, 39, 176, 0.2);
  color: #ce93d8;
  border: 1px solid rgba(156, 39, 176, 0.3);
}

.roles-btn:hover {
  background: rgba(156, 39, 176, 0.3);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(156, 39, 176, 0.3);
}

.new-chat-btn {
  background: linear-gradient(135deg, #00bcd4, #2196f3);
  color: white;
  box-shadow: 0 6px 20px rgba(0, 188, 212, 0.3);
}

.new-chat-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 188, 212, 0.4);
}

.icon {
  font-size: 18px;
}

/* 会话列表 */
.conversations {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.section-title {
  font-size: 12px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.5);
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 12px;
}

.recycle-bin-btn {
  width: 100%;
  padding: 12px 16px;
  margin-bottom: 16px;
  background: rgba(102, 126, 234, 0.1);
  border: 1px solid rgba(102, 126, 234, 0.3);
  border-radius: 8px;
  color: #667eea;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
}

.recycle-bin-btn:hover {
  background: rgba(102, 126, 234, 0.2);
  border-color: rgba(102, 126, 234, 0.5);
  transform: translateY(-1px);
}

.recycle-bin-btn .icon {
  font-size: 16px;
}

.sidebar-footer .recycle-bin-btn {
  margin-bottom: 16px;
}

.conversation-item {
  padding: 12px 16px;
  margin-bottom: 8px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid transparent;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
}

.conversation-item:hover {
  background: rgba(0, 188, 212, 0.1);
  border-color: rgba(0, 188, 212, 0.2);
  transform: translateX(4px);
}

.conversation-item.active {
  background: linear-gradient(135deg, rgba(0, 188, 212, 0.2), rgba(33, 150, 243, 0.2));
  border-color: rgba(0, 188, 212, 0.4);
}

.conversation-content {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

/* 删除按钮样式 */
.delete-conversation-btn {
  opacity: 0;
  visibility: hidden;
  background: rgba(244, 67, 54, 0.1);
  border: 1px solid rgba(244, 67, 54, 0.3);
  border-radius: 8px;
  padding: 6px 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 32px;
  margin-left: 8px;
}

.delete-conversation-btn:hover {
  background: rgba(244, 67, 54, 0.2);
  border-color: rgba(244, 67, 54, 0.5);
  transform: scale(1.1);
}

.delete-conversation-btn span {
  font-size: 16px;
}

/* 鼠标悬停时显示删除按钮 */
.conversation-item:hover .delete-conversation-btn {
  opacity: 1;
  visibility: visible;
}

.conversation-icon {
  font-size: 20px;
  opacity: 0.8;
}

.conversation-info {
  flex: 1;
  min-width: 0;
}

.conversation-title {
  color: #e0e0e0;
  font-weight: 500;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conversation-time {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
}

/* 侧边栏底部 */
.sidebar-footer {
  padding: 16px;
  background: rgba(17, 21, 39, 0.8);
  border-top: 1px solid rgba(0, 188, 212, 0.3);
}

.user-info-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #00bcd4, #2196f3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
  box-shadow: 0 4px 12px rgba(0, 188, 212, 0.4);
  overflow: hidden;
  color: white;
}

.user-avatar .avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.user-details {
  flex: 1;
}

.user-name {
  color: #ffffff;
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 4px;
}

.user-status {
  color: #4ade80;
  font-size: 12px;
  font-weight: 500;
}

.user-action-buttons {
  display: flex;
  gap: 8px;
}

.action-btn {
  flex: 1;
  height: 42px;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
}

.btn-icon {
  font-size: 18px;
}

.btn-text {
  font-size: 13px;
  font-weight: 600;
}

.profile-btn {
  background: rgba(0, 188, 212, 0.15);
  border: 1px solid rgba(0, 188, 212, 0.4);
  color: #00e5ff;
}

.profile-btn:hover {
  background: rgba(0, 188, 212, 0.25);
  border-color: rgba(0, 188, 212, 0.6);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 188, 212, 0.3);
}

.logout-btn {
  background: rgba(244, 67, 54, 0.15);
  border: 1px solid rgba(244, 67, 54, 0.4);
  color: #ff5252;
}

.logout-btn:hover {
  background: rgba(244, 67, 54, 0.25);
  border-color: rgba(244, 67, 54, 0.6);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(244, 67, 54, 0.3);
}

/* 主内容区 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 10;
}

/* 空状态 */
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 40px;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 24px;
  opacity: 0.6;
}

.empty-state h2 {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 12px;
  background: linear-gradient(135deg, #00bcd4, #2196f3);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.empty-state p {
  font-size: 18px;
  color: v-bind('currentTheme === "white" ? "rgba(107, 114, 128, 0.8)" : currentTheme === "green" ? "rgba(46, 125, 50, 0.8)" : "rgba(255, 255, 255, 0.6)"');
  margin-bottom: 32px;
}

.start-btn {
  padding: 16px 32px;
  background: linear-gradient(135deg, #00bcd4, #2196f3);
  border: none;
  border-radius: 12px;
  color: white;
  font-weight: 600;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 8px 25px rgba(0, 188, 212, 0.3);
}

.start-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 35px rgba(0, 188, 212, 0.4);
}

/* 聊天容器 */
.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  background: v-bind('currentTheme === "white" ? "rgba(255, 255, 255, 0.95)" : currentTheme === "green" ? "rgba(232, 245, 233, 0.95)" : "transparent"');
  border-radius: 12px;
}

/* 聊天头部 */
.chat-header {
  padding: 20px 24px;
  background: v-bind('currentTheme === "white" ? "rgba(249, 250, 251, 0.98)" : currentTheme === "green" ? "rgba(220, 237, 200, 0.98)" : "rgba(26, 31, 58, 0.6)"');
  border-bottom: 1px solid v-bind('currentTheme === "white" ? "rgba(209, 213, 219, 0.5)" : currentTheme === "green" ? "rgba(129, 199, 132, 0.5)" : "rgba(0, 188, 212, 0.2)"');
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.chat-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: v-bind('themes[currentTheme].textColor');
}

.model-select {
  padding: 8px 12px;
  background: v-bind('currentTheme === "white" ? "rgba(243, 244, 246, 0.8)" : currentTheme === "green" ? "rgba(200, 230, 201, 0.5)" : "rgba(0, 188, 212, 0.1)"');
  border: 1px solid v-bind('currentTheme === "white" ? "rgba(209, 213, 219, 0.8)" : currentTheme === "green" ? "rgba(129, 199, 132, 0.8)" : "rgba(0, 188, 212, 0.3)"');
  border-radius: 8px;
  color: v-bind('currentTheme === "white" ? "#1f2937" : currentTheme === "green" ? "#1b5e20" : "#00bcd4"');
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.model-select:hover {
  border-color: rgba(0, 188, 212, 0.5);
  box-shadow: 0 0 12px rgba(0, 188, 212, 0.2);
}

.model-select:focus {
  outline: none;
  border-color: rgba(0, 188, 212, 0.6);
  box-shadow: 0 0 0 3px rgba(0, 188, 212, 0.1);
}

.header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

/* 主题切换器 */
.theme-switcher {
  display: flex;
  gap: 6px;
  padding: 4px;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 10px;
}

.theme-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.1);
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 16px;
}

.theme-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

.theme-btn.active {
  background: rgba(0, 188, 212, 0.3);
  border-color: rgba(0, 188, 212, 0.8);
  box-shadow: 0 2px 8px rgba(0, 188, 212, 0.4);
}

/* 消息区域 */
.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  background-color: v-bind('themes[currentTheme].messageAreaBg');
}

/* 角色描述空状态 */
.role-description-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
}

.role-description-card {
  max-width: 600px;
  width: 100%;
  text-align: center;
  padding: 40px;
  background: v-bind('currentTheme === "white" ? "rgba(249, 250, 251, 0.8)" : currentTheme === "green" ? "rgba(220, 237, 200, 0.8)" : "rgba(26, 31, 58, 0.6)"');
  border-radius: 20px;
  backdrop-filter: blur(10px);
  border: 1px solid v-bind('currentTheme === "white" ? "rgba(209, 213, 219, 0.5)" : currentTheme === "green" ? "rgba(129, 199, 132, 0.5)" : "rgba(0, 188, 212, 0.2)"');
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.role-avatar-large {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #00bcd4, #2196f3);
  border-radius: 50%;
  font-size: 40px;
  box-shadow: 0 4px 20px rgba(0, 188, 212, 0.3);
}

.role-description-card h3 {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 16px 0;
  background: linear-gradient(135deg, #00bcd4, #2196f3);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  color: v-bind('themes[currentTheme].textColor');
}

.role-description-text {
  font-size: 16px;
  line-height: 1.8;
  color: v-bind('currentTheme === "white" ? "rgba(107, 114, 128, 0.9)" : currentTheme === "green" ? "rgba(46, 125, 50, 0.9)" : "rgba(255, 255, 255, 0.8)"');
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.messages-area::-webkit-scrollbar {
  width: 8px;
}

.messages-area::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 4px;
}

.messages-area::-webkit-scrollbar-thumb {
  background: rgba(0, 188, 212, 0.3);
  border-radius: 4px;
}

.messages-area::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 188, 212, 0.5);
}

/* 消息 */
.message {
  animation: messageSlideIn 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
  transition: all 0.3s ease;
}

.message:hover {
  transform: translateY(-1px);
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.message {
  display: flex;
  width: 100%;
}

.message.user {
  justify-content: flex-end;
}

.message.assistant {
  justify-content: flex-start;
}

.message-wrapper {
  display: flex;
  gap: 8px;
  max-width: 85%;
}

.message.user .message-wrapper {
  flex-direction: row-reverse;
}

.message.assistant .message-wrapper {
  max-width: min(75%, 700px);
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
  font-weight: 700;
  color: #ffffff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  overflow: hidden;
}

.message-avatar .avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.message.user .message-avatar {
  background: linear-gradient(135deg, #00bcd4, #2196f3);
  box-shadow: 0 4px 12px rgba(0, 188, 212, 0.4);
}

.message.assistant .message-avatar {
  background: linear-gradient(135deg, #9c27b0, #673ab7);
  box-shadow: 0 4px 12px rgba(156, 39, 176, 0.3);
  font-size: 18px;
}

.message-content {
  position: relative;
  display: inline-block;
  max-width: 100%;
}

.message-text {
  padding: 10px 16px;
  border-radius: 14px;
  font-size: 14px;
  line-height: 1.5;
  word-wrap: break-word;
  overflow-wrap: break-word;
  color: #ffffff;
  position: relative;
}

.message.user .message-text {
  /* 用户消息 */
  background: v-bind('themes[currentTheme].userMsgBg');
  color: v-bind('themes[currentTheme].userMsgText');
  border-bottom-right-radius: 6px;
  padding: 8px 14px;
  min-width: fit-content;
}

.message.assistant .message-text {
  /* 助手消息 */
  background: v-bind('themes[currentTheme].assistantMsgBg');
  color: v-bind('themes[currentTheme].assistantMsgText');
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-bottom-left-radius: 6px;
  padding: 12px 16px;
}

/* 思考过程样式 */
.thinking-section {
  background: rgba(0, 188, 212, 0.1);
  border-left: 3px solid #00bcd4;
  border-radius: 8px;
  margin-bottom: 12px;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.thinking-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  background: rgba(0, 188, 212, 0.15);
  cursor: pointer;
  font-weight: 600;
  color: #00e5ff;
  font-size: 14px;
  user-select: none;
  transition: all 0.2s ease;
}

.thinking-header:hover {
  background: rgba(0, 188, 212, 0.25);
}

.toggle-icon {
  font-size: 12px;
  color: #00bcd4;
  transition: transform 0.2s ease;
}

.thinking-content {
  padding: 0;
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    max-height: 0;
  }
  to {
    opacity: 1;
    max-height: 500px;
  }
}

.thinking-text {
  padding: 12px 14px;
  color: #9ca3af;
  font-size: 13px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-wrap: break-word;
  background: rgba(0, 0, 0, 0.2);
  border-top: 1px solid rgba(0, 188, 212, 0.2);
  font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
}

.message-time {
  font-size: 11px;
  color: v-bind('themes[currentTheme].timestampColor');
  margin-top: 4px;
  padding: 0 4px;
}

.message.user .message-time {
  text-align: right;
}

.message.assistant .message-time {
  text-align: left;
}

/* 消息页脚 */
.message-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 4px;
  padding: 0 4px;
}

.message-status {
  font-size: 11px;
  display: flex;
  align-items: center;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  opacity: 0.8;
}

.status-indicator.sending {
  color: #ff9800;
}

.status-indicator.sent {
  color: #4caf50;
}

.status-indicator.failed {
  color: #f44336;
}

.sending-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #ff9800;
  animation: pulse-dot 1.5s infinite ease-in-out;
}

@keyframes pulse-dot {
  0%, 100% { opacity: 0.3; transform: scale(0.8); }
  50% { opacity: 1; transform: scale(1.2); }
}

/* 消息操作按钮 */
.message-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  gap: 4px;
  opacity: 0;
  transform: translateY(-5px);
  transition: all 0.3s ease;
  pointer-events: none;
}

.message:hover .message-actions {
  opacity: 1;
  transform: translateY(0);
  pointer-events: auto;
}

.message.user .message-actions {
  right: 8px;
  left: auto;
}

.message.assistant .message-actions {
  right: 8px;
}

.action-btn {
  width: 24px;
  height: 24px;
  border: none;
  border-radius: 6px;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(10px);
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  transition: all 0.2s ease;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.action-btn:hover {
  background: rgba(0, 0, 0, 0.8);
  transform: scale(1.1);
  border-color: rgba(0, 188, 212, 0.3);
}

.feedback-btn.active {
  background: rgba(0, 188, 212, 0.3);
  color: #00bcd4;
  border-color: rgba(0, 188, 212, 0.5);
}

.feedback-btn.active:hover {
  background: rgba(0, 188, 212, 0.5);
  transform: scale(1.1);
}

.copy-btn:hover {
  background: rgba(76, 175, 80, 0.3);
  color: #4caf50;
  border-color: rgba(76, 175, 80, 0.5);
}

/* Markdown样式 */
.message-text :deep(p) {
  margin: 0 0 8px 0;
}

.message-text :deep(p:last-child) {
  margin-bottom: 0;
}

.message-text :deep(ol),
.message-text :deep(ul) {
  margin: 8px 0;
  padding-left: 24px;
  list-style-position: outside;
}

.message-text :deep(ol) {
  list-style-type: decimal;
}

.message-text :deep(ul) {
  list-style-type: disc;
}

.message-text :deep(li) {
  margin: 4px 0;
  padding-left: 4px;
}

.message-text :deep(code) {
  background: v-bind('currentTheme === "white" ? "rgba(0, 0, 0, 0.08)" : currentTheme === "green" ? "rgba(46, 125, 50, 0.15)" : "rgba(0, 0, 0, 0.3)"');
  color: v-bind('currentTheme === "white" ? "#e11d48" : currentTheme === "green" ? "#1b5e20" : "#fbbf24"');
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Fira Code', 'Courier New', monospace;
  font-size: 14px;
}

.message-text :deep(pre) {
  background: v-bind('currentTheme === "white" ? "rgba(0, 0, 0, 0.06)" : currentTheme === "green" ? "rgba(46, 125, 50, 0.1)" : "rgba(0, 0, 0, 0.4)"');
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 8px 0;
  border: 1px solid v-bind('currentTheme === "white" ? "rgba(0, 0, 0, 0.1)" : currentTheme === "green" ? "rgba(46, 125, 50, 0.2)" : "rgba(255, 255, 255, 0.1)"');
}

.message-text :deep(pre code) {
  background: none;
  padding: 0;
  color: inherit;
}

/* 输入指示器 */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 14px 18px;
  background: rgba(55, 65, 81, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 188, 212, 0.2);
  border-radius: 16px;
  border-bottom-left-radius: 4px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #00bcd4;
  animation: typing 1.4s infinite ease-in-out;
}

.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* 输入区域 */
.input-area {
  padding: 14px 20px;
  background: v-bind('currentTheme === "white" ? "#ffffff" : currentTheme === "green" ? "#e8f5e9" : "rgba(15, 23, 42, 0.95)"');
  border-top: 1px solid v-bind('themes[currentTheme].inputBorder');
  backdrop-filter: v-bind('currentTheme === "gray" ? "blur(16px)" : "none"');
}

.input-container {
  max-width: 100%;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: center;
}

.message-input {
  flex: 1;
  background: v-bind('themes[currentTheme].inputBg');
  border: 1px solid v-bind('themes[currentTheme].inputBorder');
  border-radius: 10px;
  padding: 12px 16px;
  color: v-bind('themes[currentTheme].textColor');
  font-size: 15px;
  line-height: 1.6;
  resize: none;
  min-height: 48px;
  max-height: 200px;
  overflow-y: auto;
  transition: all 0.2s ease;
  font-family: inherit;
}

.message-input::placeholder {
  color: v-bind('currentTheme === "white" || currentTheme === "green" ? "rgba(107, 114, 128, 0.6)" : "rgba(156, 163, 175, 0.5)"');
}

.message-input:focus {
  outline: none;
  border-color: rgba(0, 188, 212, 0.6);
  box-shadow: 0 0 12px rgba(0, 188, 212, 0.3);
}

.message-input:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.send-btn {
  width: 48px;
  height: 48px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #3fb7ff, #4f8cff);
  color: white;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0, 188, 212, 0.3);
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 188, 212, 0.4);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.loading-spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 输入选项 */
.input-options {
  margin-top: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.thinking-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: v-bind('currentTheme === "white" ? "#2563eb" : currentTheme === "green" ? "#2e7d32" : "#00bcd4"');
  font-weight: 500;
}

.thinking-toggle input[type="checkbox"] {
  width: 16px;
  height: 16px;
  accent-color: v-bind('currentTheme === "white" ? "#2563eb" : currentTheme === "green" ? "#2e7d32" : "#00bcd4"');
}

.toggle-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: v-bind('currentTheme === "white" ? "rgba(71, 85, 105, 0.9)" : currentTheme === "green" ? "rgba(46, 125, 50, 0.9)" : "rgba(255, 255, 255, 0.8)"');
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    width: 280px;
  }
  
  .message-wrapper {
    max-width: 90%;
  }
  
  .chat-header {
    padding: 16px 20px;
  }
  
  .messages-area {
    padding: 16px;
  }
  
  .input-area {
    padding: 16px 20px;
  }
}

@media (max-width: 640px) {
  .sidebar {
    position: fixed;
    left: -320px;
    top: 0;
    height: 100vh;
    z-index: 100;
    transition: left 0.3s ease;
  }
  
  .sidebar.open {
    left: 0;
  }
  
  .main-content {
    margin-left: 0;
  }
}
</style>
