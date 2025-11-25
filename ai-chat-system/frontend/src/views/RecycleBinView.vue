<template>
  <div class="recycle-bin-view">
    <!-- 动态背景 -->
    <div class="background-animation" :style="{ backgroundColor: '#1a1d29' }">
      <div class="floating-orb orb-1"></div>
      <div class="floating-orb orb-2"></div>
      <div class="floating-orb orb-3"></div>
    </div>

    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="logo">
          <span class="logo-icon">🤖</span>
          <span class="logo-text">AI Chat</span>
        </div>
        <button @click="goToChat" class="back-btn">
          <span class="icon">←</span>
          <span>Back to Chat</span>
        </button>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="main-content">
      <div class="recycle-bin-container">
        <div class="page-header">
          <h1>Recycle Bin</h1>
          <div class="header-actions">
            <el-button 
              type="danger" 
              :disabled="deletedConversations.length === 0"
              @click="handleEmptyRecycleBin"
            >
              <el-icon><Delete /></el-icon>
              Empty Recycle Bin
            </el-button>
          </div>
        </div>

        <div v-if="loading" class="loading-container">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>Loading...</span>
        </div>

        <div v-else-if="deletedConversations.length === 0" class="empty-state">
          <div class="empty-icon">🗑️</div>
          <h2>Recycle Bin is Empty</h2>
          <p>Deleted conversations will appear here</p>
        </div>

        <div v-else class="conversations-list">
          <div
            v-for="conv in deletedConversations"
            :key="conv.id"
            class="conversation-item"
          >
            <div class="conversation-content">
              <div class="conversation-icon">💬</div>
              <div class="conversation-info">
                <div class="conversation-title">{{ conv.title }}</div>
                <div class="conversation-meta">
                  <span class="conversation-time">Deleted: {{ formatTime(conv.deletedAt) }}</span>
                  <span v-if="conv.lastMessageAt" class="conversation-time">
                    Last message: {{ formatTime(conv.lastMessageAt) }}
                  </span>
                </div>
              </div>
            </div>
            <div class="conversation-actions">
              <el-button 
                type="primary" 
                size="small"
                @click="handleRestore(conv.id)"
              >
                <el-icon><RefreshLeft /></el-icon>
                Restore
              </el-button>
              <el-button 
                type="danger" 
                size="small"
                @click="handlePermanentDelete(conv.id)"
              >
                <el-icon><Delete /></el-icon>
                Delete Permanently
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '../stores/chat'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, RefreshLeft, Loading } from '@element-plus/icons-vue'

const router = useRouter()
const chatStore = useChatStore()

const loading = ref(false)
const deletedConversations = ref([])

onMounted(async () => {
  await loadDeletedConversations()
})

const loadDeletedConversations = async () => {
  loading.value = true
  try {
    await chatStore.loadDeletedConversations()
    deletedConversations.value = chatStore.deletedConversations
  } catch (error) {
    ElMessage.error('Failed to load recycle bin')
  } finally {
    loading.value = false
  }
}

const formatTime = (time) => {
  if (!time) return 'Unknown'
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) {
    return 'Just now'
  } else if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}m ago`
  } else if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}h ago`
  } else if (diff < 604800000) {
    return `${Math.floor(diff / 86400000)}d ago`
  } else {
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }
}

const handleRestore = async (id) => {
  try {
    await ElMessageBox.confirm(
      'Are you sure you want to restore this conversation?',
      'Restore Conversation',
      {
        confirmButtonText: 'Restore',
        cancelButtonText: 'Cancel',
        type: 'info'
      }
    )
    
    await chatStore.restoreConversation(id)
    deletedConversations.value = chatStore.deletedConversations
    ElMessage.success('Conversation restored')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('Failed to restore conversation')
    }
  }
}

const handlePermanentDelete = async (id) => {
  try {
    await ElMessageBox.confirm(
      'Are you sure you want to permanently delete this conversation? This action cannot be undone!',
      'Permanently Delete',
      {
        confirmButtonText: 'Delete Permanently',
        cancelButtonText: 'Cancel',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    await chatStore.deleteConversation(id, true)
    deletedConversations.value = chatStore.deletedConversations
    ElMessage.success('Conversation permanently deleted')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('Failed to delete conversation')
    }
  }
}

const handleEmptyRecycleBin = async () => {
  try {
    await ElMessageBox.confirm(
      'Are you sure you want to empty the recycle bin? This will permanently delete all conversations and cannot be undone!',
      'Empty Recycle Bin',
      {
        confirmButtonText: 'Empty',
        cancelButtonText: 'Cancel',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    await chatStore.emptyRecycleBin()
    deletedConversations.value = []
    ElMessage.success('Recycle bin emptied')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('Failed to empty recycle bin')
    }
  }
}

const goToChat = () => {
  router.push('/chat')
}
</script>

<style scoped>
.recycle-bin-view {
  display: flex;
  height: 100vh;
  overflow: hidden;
  position: relative;
}

.background-animation {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  overflow: hidden;
}

.floating-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.3;
  animation: float 20s infinite ease-in-out;
}

.orb-1 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.orb-2 {
  width: 250px;
  height: 250px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  top: 60%;
  right: 10%;
  animation-delay: 5s;
}

.orb-3 {
  width: 200px;
  height: 200px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  bottom: 10%;
  left: 50%;
  animation-delay: 10s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(30px, -30px) scale(1.1);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.9);
  }
}

.sidebar {
  width: 280px;
  background: rgba(26, 29, 41, 0.95);
  backdrop-filter: blur(10px);
  border-right: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  flex-direction: column;
  z-index: 1;
  position: relative;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  font-size: 20px;
  font-weight: 600;
  color: white;
}

.logo-icon {
  font-size: 24px;
}

.back-btn {
  width: 100%;
  padding: 12px 16px;
  background: rgba(102, 126, 234, 0.1);
  border: 1px solid rgba(102, 126, 234, 0.3);
  border-radius: 8px;
  color: #667eea;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.back-btn:hover {
  background: rgba(102, 126, 234, 0.2);
  border-color: rgba(102, 126, 234, 0.5);
}

.main-content {
  flex: 1;
  overflow-y: auto;
  z-index: 1;
  position: relative;
}

.recycle-bin-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.page-header h1 {
  color: white;
  font-size: 32px;
  font-weight: 600;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  color: rgba(255, 255, 255, 0.6);
  gap: 16px;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: rgba(255, 255, 255, 0.6);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-state h2 {
  color: white;
  font-size: 24px;
  margin-bottom: 10px;
}

.conversations-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.conversation-item {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.2s;
}

.conversation-item:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.2);
}

.conversation-content {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.conversation-icon {
  font-size: 24px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(102, 126, 234, 0.2);
  border-radius: 8px;
}

.conversation-info {
  flex: 1;
}

.conversation-title {
  color: white;
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
}

.conversation-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.conversation-time {
  color: rgba(255, 255, 255, 0.5);
  font-size: 13px;
}

.conversation-actions {
  display: flex;
  gap: 8px;
}

:deep(.el-button) {
  font-weight: 500;
}

:deep(.el-button--danger) {
  background-color: #f56c6c;
  border-color: #f56c6c;
}

:deep(.el-button--danger:hover) {
  background-color: #f78989;
  border-color: #f78989;
}
</style>


