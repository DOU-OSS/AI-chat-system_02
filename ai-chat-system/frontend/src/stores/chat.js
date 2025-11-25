import { defineStore } from 'pinia'
import { 
  createConversation as createConversationApi,
  getUserConversations as getUserConversationsApi,
  getConversation as getConversationApi,
  deleteConversation as deleteConversationApi,
  getDeletedConversations as getDeletedConversationsApi,
  restoreConversation as restoreConversationApi,
  emptyRecycleBin as emptyRecycleBinApi
} from '../api/conversation'
import { sendMessage as sendMessageApi } from '../api/chat'
import { ElMessage } from 'element-plus'

export const useChatStore = defineStore('chat', {
  state: () => ({
    conversations: [],
    deletedConversations: [],
    currentConversation: null,
    loading: false,
    sending: false
  }),

  getters: {
    currentMessages: (state) => state.currentConversation?.messages || []
  },

  actions: {
    async loadConversations() {
      try {
        this.loading = true
        const response = await getUserConversationsApi()
        this.conversations = response.data
      } catch (error) {
        ElMessage.error('Failed to load conversations')
      } finally {
        this.loading = false
      }
    },

    async createConversation(title, aiRoleId, selectedModel) {
      try {
        const response = await createConversationApi({ title, aiRoleId, selectedModel })
        this.conversations.unshift(response.data)
        this.currentConversation = response.data
        return response.data
      } catch (error) {
        ElMessage.error('Failed to create conversation')
        throw error
      }
    },

    async loadConversation(id) {
      try {
        this.loading = true
        const response = await getConversationApi(id)
        this.currentConversation = response.data
        return response.data
      } catch (error) {
        ElMessage.error('Failed to load conversation')
        throw error
      } finally {
        this.loading = false
      }
    },

    async sendMessage(content, model) {
      if (!this.currentConversation) {
        ElMessage.error('Please select a conversation first')
        return
      }

      try {
        this.sending = true
        
        // Add user message to UI immediately
        const userMessage = {
          id: Date.now(),
          content,
          role: 'user',
          createdAt: new Date().toISOString()
        }
        
        if (!this.currentConversation.messages) {
          this.currentConversation.messages = []
        }
        this.currentConversation.messages.push(userMessage)

        // Send message to backend
        const response = await sendMessageApi({
          conversationId: this.currentConversation.id,
          content,
          aiRoleId: this.currentConversation.aiRoleId,
          model
        })

        // Add AI response
        this.currentConversation.messages.push(response.data)
        
        return response.data
      } catch (error) {
        // Remove the user message if sending failed
        if (this.currentConversation.messages) {
          this.currentConversation.messages.pop()
        }
        ElMessage.error('Failed to send message')
        throw error
      } finally {
        this.sending = false
      }
    },

    async deleteConversation(id, permanent = false) {
      try {
        await deleteConversationApi(id, permanent)
        // 先移除本地列表中的对话
        this.conversations = this.conversations.filter(c => c.id !== id)
        if (this.currentConversation?.id === id) {
          this.currentConversation = null
        }
        // 等待一小段时间确保后端删除操作完成
        await new Promise(resolve => setTimeout(resolve, 100))
        // 重新加载对话列表以确保数据同步
        await this.loadConversations()
        ElMessage.success(permanent ? 'Conversation permanently deleted' : 'Conversation moved to recycle bin')
      } catch (error) {
        console.error('Delete conversation error:', error)
        ElMessage.error('Failed to delete conversation')
        // 即使删除失败，也重新加载对话列表以保持数据一致性
        await this.loadConversations()
        throw error
      }
    },

    async loadDeletedConversations() {
      try {
        this.loading = true
        const response = await getDeletedConversationsApi()
        this.deletedConversations = response.data
      } catch (error) {
        ElMessage.error('Failed to load recycle bin')
      } finally {
        this.loading = false
      }
    },

    async restoreConversation(id) {
      try {
        await restoreConversationApi(id)
        this.deletedConversations = this.deletedConversations.filter(c => c.id !== id)
        await this.loadConversations()
        ElMessage.success('Conversation restored')
      } catch (error) {
        ElMessage.error('Failed to restore conversation')
        throw error
      }
    },

    async emptyRecycleBin() {
      try {
        await emptyRecycleBinApi()
        this.deletedConversations = []
        ElMessage.success('Recycle bin emptied')
      } catch (error) {
        ElMessage.error('Failed to empty recycle bin')
        throw error
      }
    },

    selectConversation(conversation) {
      this.currentConversation = conversation
    },

    clearCurrentConversation() {
      this.currentConversation = null
    }
  }
})
