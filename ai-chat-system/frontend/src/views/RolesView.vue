<template>
  <div class="ai-roles-container">
    <!-- Animated Background -->
    <div class="ai-network-bg"></div>
    
    <div class="roles-content">
      <!-- Header -->
      <div class="ai-header">
        <div class="header-left">
          <button @click="goBack" class="back-btn">
            <span>←</span> Back to Chat
          </button>
          <div class="header-title">
            <span class="ai-emoji">🤖</span>
            <h1>AI Personality Lab</h1>
          </div>
        </div>
        <button @click="showCreateDialog" class="create-role-btn">
          <span class="plus-icon">+</span>
          <span>Create AI Role</span>
        </button>
      </div>

      <!-- Tabs -->
      <div class="ai-tabs">
        <button 
          :class="['tab-btn', { active: activeTab === 'mine' }]"
          @click="activeTab = 'mine'"
        >
          <span>🔒</span> My AI Roles
        </button>
        <button 
          :class="['tab-btn', { active: activeTab === 'public' }]"
          @click="activeTab = 'public'"
        >
          <span>🌐</span> Community Roles
        </button>
      </div>

      <!-- Roles Grid -->
      <div class="roles-grid">
        <template v-if="activeTab === 'mine'">
          <div v-for="role in myRoles" :key="role.id" class="ai-role-card" @click="selectRole(role)">
            <div class="role-avatar">
              <span>{{ getAIEmoji(role.name) }}</span>
            </div>
            <div class="role-content">
              <h3 class="role-name">{{ role.name }}</h3>
              <p class="role-description">{{ role.description || 'Custom AI personality' }}</p>
              <div class="role-actions">
                <button @click.stop="editRole(role)" class="action-btn edit">
                  <span>✏️</span> Edit
                </button>
                <button @click.stop="deleteRole(role.id)" class="action-btn delete">
                  <span>🗑</span> Delete
                </button>
              </div>
            </div>
          </div>
        </template>

        <template v-else>
          <div v-for="role in publicRoles" :key="role.id" class="ai-role-card public" @click="selectRole(role)">
            <div class="role-avatar">
              <span>{{ getAIEmoji(role.name) }}</span>
            </div>
            <div class="role-content">
              <h3 class="role-name">{{ role.name }}</h3>
              <p class="role-description">{{ role.description || 'Community AI personality' }}</p>
            </div>
          </div>
        </template>
      </div>
    </div>

    <!-- Create/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? 'Edit Role' : 'Create Role'"
      width="600px"
    >
      <el-form ref="formRef" :model="roleForm" :rules="rules" label-width="120px">
        <el-form-item label="Name" prop="name">
          <el-input v-model="roleForm.name" placeholder="Enter role name" />
        </el-form-item>
        <el-form-item label="Description">
          <el-input
            v-model="roleForm.description"
            type="textarea"
            :rows="2"
            placeholder="Enter description"
          />
        </el-form-item>
        <el-form-item label="System Prompt" prop="systemPrompt">
          <el-input
            v-model="roleForm.systemPrompt"
            type="textarea"
            :rows="4"
            placeholder="Enter system prompt for this role"
          />
        </el-form-item>
        <el-form-item label="Public">
          <el-switch v-model="roleForm.isPublic" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="saveRole" :loading="saving">
          {{ isEdit ? 'Update' : 'Create' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MoreFilled } from '@element-plus/icons-vue'
import {
  createRole,
  getUserRoles,
  getPublicRoles,
  updateRole,
  deleteRole as deleteRoleApi
} from '../api/role'
import { useChatStore } from '../stores/chat'
import request from '../utils/request'

const router = useRouter()
const chatStore = useChatStore()

const activeTab = ref('mine')
const myRoles = ref([])
const publicRoles = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref()
const availableModels = ref([])
const loadingModels = ref(false)

const roleForm = reactive({
  id: null,
  name: '',
  description: '',
  systemPrompt: '',
  model: '',
  isPublic: false
})

const rules = {
  name: [
    { required: true, message: 'Please enter role name', trigger: 'blur' }
  ],
  systemPrompt: [
    { required: true, message: 'Please enter system prompt', trigger: 'blur' }
  ]
}

const goBack = () => {
  router.push('/chat')
}

const loadRoles = async () => {
  try {
    const [myRolesData, publicRolesData] = await Promise.all([
      getUserRoles(),
      getPublicRoles()
    ])
    myRoles.value = myRolesData.data || []
    publicRoles.value = publicRolesData.data || []
  } catch (error) {
    console.error('Failed to load roles:', error)
    ElMessage.error('Failed to load roles')
  }
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
    }
  } catch (error) {
    console.error('Failed to load models:', error)
    ElMessage.error('Failed to load available models')
  } finally {
    loadingModels.value = false
  }
}

const getModelDisplayName = (modelId) => {
  if (!modelId) return 'Unknown'
  
  const model = availableModels.value.find(m => m.id === modelId)
  if (model) {
    return model.name
  }
  
  // 如果找不到，返回原始ID
  return modelId
}

const showCreateDialog = () => {
  isEdit.value = false
  Object.assign(roleForm, {
    id: null,
    name: '',
    description: '',
    systemPrompt: '',
    model: '', // 不设置默认值，由后端或保存时自动处理
    isPublic: false
  })
  dialogVisible.value = true
}

const editRole = (role) => {
  isEdit.value = true
  Object.assign(roleForm, role)
  dialogVisible.value = true
}

const saveRole = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    // 创建新角色时，如果没有设置 model，使用第一个可用模型或空值
    const roleData = { ...roleForm }
    if (!isEdit.value && !roleData.model && availableModels.value.length > 0) {
      roleData.model = availableModels.value[0].id
    }
    
    if (isEdit.value) {
      await updateRole(roleForm.id, roleData)
      ElMessage.success('Role updated successfully')
    } else {
      await createRole(roleData)
      ElMessage.success('Role created successfully')
    }
    dialogVisible.value = false
    loadRoles()
  } catch (error) {
    ElMessage.error('Failed to save role')
  } finally {
    saving.value = false
  }
}

const getAIEmoji = (name) => {
  const emojiMap = {
    'Assistant': '🤖',
    'Teacher': '🎓',
    'Programmer': '💻',
    'Writer': '✍️',
    'Counselor': '💔',
    'Analyst': '📊',
    'Artist': '🎨',
    'Chef': '👨‍🍳',
    'Doctor': '👨‍⚕️',
    'Coach': '🏅',
    'Translator': '🌐',
    'Marketing': '📢'
  }
  
  for (const [key, emoji] of Object.entries(emojiMap)) {
    if (name.toLowerCase().includes(key.toLowerCase())) {
      return emoji
    }
  }
  return '🤖' // Default robot emoji
}

const selectRole = async (role) => {
  try {
    // Create a new conversation with this role
    await chatStore.createConversation(`Chat with ${role.name}`, role.id)
    ElMessage.success(`Started conversation with ${role.name}`)
    // Navigate to chat page
    router.push('/chat')
  } catch (error) {
    ElMessage.error('Failed to start conversation')
  }
}

const deleteRole = async (id) => {
  try {
    await ElMessageBox.confirm(
      'Are you sure you want to delete this role?',
      'Delete Role',
      {
        confirmButtonText: 'Delete',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }
    )
    
    await deleteRoleApi(id)
    ElMessage.success('Role deleted successfully')
    loadRoles()
  } catch (error) {
    // User cancelled or error occurred
  }
}

onMounted(async () => {
  await loadRoles()
  await loadAvailableModels()
})
</script>

<style scoped>
/* Container */
.ai-roles-container {
  min-height: 100vh;
  background: #0a0e27;
  position: relative;
  overflow: hidden;
}

/* Animated Background */
.ai-network-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 30%, rgba(0, 188, 212, 0.08) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(156, 39, 176, 0.08) 0%, transparent 50%),
    radial-gradient(circle at 50% 50%, rgba(33, 150, 243, 0.05) 0%, transparent 70%);
  animation: float 20s ease-in-out infinite;
  pointer-events: none;
}

@keyframes float {
  0%, 100% { transform: scale(1) rotate(0deg); }
  50% { transform: scale(1.1) rotate(5deg); }
}

.roles-content {
  position: relative;
  z-index: 1;
  width: 100%;
  padding: 30px 50px;
  box-sizing: border-box;
}

/* Header */
.ai-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
  padding: 20px 30px;
  background: rgba(26, 31, 58, 0.6);
  border-radius: 20px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 188, 212, 0.2);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 30px;
}

.back-btn {
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  color: #94a3b8;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #e0e0e0;
  transform: translateX(-3px);
}

.header-title {
  display: flex;
  align-items: center;
  gap: 15px;
}

.ai-emoji {
  font-size: 36px;
  filter: drop-shadow(0 0 15px rgba(0, 188, 212, 0.5));
}

.header-title h1 {
  margin: 0;
  font-size: 32px;
  background: linear-gradient(135deg, #00bcd4, #9c27b0);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.create-role-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #00bcd4, #2196f3);
  border: none;
  border-radius: 12px;
  color: white;
  font-weight: 600;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 5px 20px rgba(0, 188, 212, 0.3);
}

.create-role-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0, 188, 212, 0.4);
}

.plus-icon {
  font-size: 20px;
  font-weight: 300;
}

/* Tabs */
.ai-tabs {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
}

.tab-btn {
  flex: 1;
  max-width: 200px;
  padding: 14px 24px;
  background: rgba(26, 31, 58, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: #94a3b8;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.tab-btn:hover {
  background: rgba(26, 31, 58, 0.6);
  color: #e0e0e0;
}

.tab-btn.active {
  background: linear-gradient(135deg, rgba(0, 188, 212, 0.2), rgba(33, 150, 243, 0.2));
  border-color: rgba(0, 188, 212, 0.4);
  color: #00bcd4;
}

/* Roles Grid */
.roles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

/* Role Card */
.ai-role-card {
  background: rgba(26, 31, 58, 0.6);
  border-radius: 20px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  cursor: pointer;
}

.ai-role-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #00bcd4, #2196f3);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.ai-role-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 40px rgba(0, 188, 212, 0.2);
  border-color: rgba(0, 188, 212, 0.3);
}

.ai-role-card:hover::before {
  opacity: 1;
}

.ai-role-card.public {
  background: rgba(156, 39, 176, 0.1);
}

.role-avatar {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, rgba(0, 188, 212, 0.2), rgba(33, 150, 243, 0.2));
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  font-size: 32px;
}

.role-content {
  flex: 1;
}

.role-name {
  font-size: 20px;
  font-weight: 600;
  color: #e0e0e0;
  margin: 0 0 8px;
}

.role-description {
  color: #94a3b8;
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 16px;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.role-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  font-size: 13px;
}

.stat-icon {
  font-size: 16px;
}

.stat-value {
  color: #e0e0e0;
}

.role-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  flex: 1;
  padding: 8px 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.05);
  color: #94a3b8;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #e0e0e0;
}

.action-btn.edit:hover {
  border-color: rgba(0, 188, 212, 0.3);
  background: rgba(0, 188, 212, 0.1);
  color: #00bcd4;
}

.action-btn.delete:hover {
  border-color: rgba(239, 68, 68, 0.3);
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

/* Dialog Overrides */
:deep(.el-dialog) {
  background: rgba(26, 31, 58, 0.95) !important;
  border: 1px solid rgba(0, 188, 212, 0.2);
  border-radius: 20px;
}

:deep(.el-dialog__title) {
  color: #e0e0e0;
  font-size: 20px;
}

:deep(.el-form-item__label) {
  color: #94a3b8;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner),
:deep(.el-select__wrapper) {
  background: rgba(255, 255, 255, 0.05) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  box-shadow: none !important;
}

:deep(.el-input__inner),
:deep(.el-textarea__inner),
:deep(.el-select__selected-item) {
  color: #e0e0e0 !important;
}

/* 下拉框选项样式 - 确保文字完整显示 */
/* 注意：下拉菜单挂载在 body 上，需要使用全局样式 */

:deep(.el-slider__runway) {
  background: rgba(255, 255, 255, 0.1);
}

:deep(.el-slider__bar) {
  background: linear-gradient(135deg, #00bcd4, #2196f3);
}
</style>

<style scoped>
.roles-container {
  height: 100vh;
  background: #f5f5f5;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.role-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.role-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.role-info {
  min-height: 100px;
}

.description {
  color: #666;
  margin-bottom: 12px;
  font-size: 14px;
  line-height: 1.5;
}

.role-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
