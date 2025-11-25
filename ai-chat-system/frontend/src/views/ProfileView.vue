<template>
  <div class="profile-container">
    <!-- AI Neural Network Background -->
    <div class="neural-background"></div>
    
    <el-container>
      <el-header class="profile-header">
        <div class="header-content">
          <button @click="goBack" class="back-btn">
            <span>←</span>
          </button>
          <h2>👤 My Profile</h2>
        </div>
      </el-header>
      
      <el-main class="profile-main">
        <el-row :gutter="24" justify="center" class="profile-row">
          <!-- 左侧：账户信息 -->
          <el-col :xs="24" :sm="24" :md="12" :lg="10" class="profile-col">
            <el-card class="info-card">
              <template #header>
                <div class="card-header">
                  <span class="header-icon">🔐</span>
                  <span>Account Information</span>
                </div>
              </template>
              
              <el-descriptions :column="1" border>
                <el-descriptions-item label="Username">
                  <span class="value-text">{{ user?.username }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="Email">
                  <div class="email-row">
                    <span class="value-text">{{ user?.email }}</span>
                    <el-tag 
                      v-if="user?.emailVerified" 
                      type="success" 
                      size="small"
                      effect="dark"
                    >
                      ✓ Verified
                    </el-tag>
                    <el-tag 
                      v-else 
                      type="warning" 
                      size="small"
                    >
                      ⚠ Unverified
                    </el-tag>
                  </div>
                </el-descriptions-item>
                <el-descriptions-item label="Nickname">
                  <span class="value-text">{{ user?.nickname || '-' }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="Member Since">
                  <span class="value-text">{{ formatDate(user?.createdAt) }}</span>
                </el-descriptions-item>
              </el-descriptions>
              
              <el-divider />
              
              <div class="action-buttons">
                <el-button @click="showChangePasswordDialog = true" type="primary" size="large" class="action-btn">
                  <el-icon><Lock /></el-icon>
                  Change Password
                </el-button>
                
                <el-button @click="editProfile" type="primary" size="large" class="action-btn">
                  <el-icon><Edit /></el-icon>
                  Edit Profile
                </el-button>
              </div>
            </el-card>
          </el-col>

          <!-- 右侧：使用统计 -->
          <el-col :xs="24" :sm="24" :md="12" :lg="10" class="profile-col">
            <el-card class="usage-card">
              <template #header>
                <div class="card-header">
                  <span class="header-icon">📊</span>
                  <span>AI Usage Statistics</span>
                </div>
              </template>
              
              <div class="usage-stats">
                <div class="stat-item">
                  <div class="stat-icon conversations">💬</div>
                  <div class="stat-content">
                    <div class="stat-value">{{ usageStats.conversationCount }}</div>
                    <div class="stat-label">Total Conversations</div>
                  </div>
                </div>

                <div class="stat-item">
                  <div class="stat-icon messages">✉️</div>
                  <div class="stat-content">
                    <div class="stat-value">{{ usageStats.messageCount }}</div>
                    <div class="stat-label">Messages Sent</div>
                  </div>
                </div>

                <div class="stat-item">
                  <div class="stat-icon roles">🎭</div>
                  <div class="stat-content">
                    <div class="stat-value">{{ usageStats.roleCount }}</div>
                    <div class="stat-label">AI Roles Created</div>
                  </div>
                </div>

                <div class="stat-item">
                  <div class="stat-icon tokens">⚡</div>
                  <div class="stat-content">
                    <div class="stat-value">{{ formatNumber(usageStats.tokenUsage) }}</div>
                    <div class="stat-label">Tokens Used (Est.)</div>
                  </div>
                </div>
              </div>
              
              <!-- Token Usage Progress -->
              <div class="token-usage-progress">
                <div class="progress-header">
                  <span class="progress-label">Token Usage</span>
                  <span class="progress-percentage">{{ tokenUsagePercentage }}%</span>
                </div>
                <el-progress 
                  :percentage="tokenUsagePercentage" 
                  :color="progressColor"
                  :stroke-width="12"
                  :show-text="false"
                  class="token-progress-bar"
                />
                <div class="progress-footer">
                  <span class="progress-used">{{ formatNumber(usageStats.tokenUsage) }} / {{ formatNumber(totalTokens) }}</span>
                  <span class="progress-remaining">{{ formatNumber(remainingTokens) }} remaining</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>
    
    <!-- Edit Profile Dialog -->
    <el-dialog 
      v-model="showEditDialog" 
      title="Edit Profile"
      width="400px"
      class="ai-dialog"
    >
      <el-form ref="editFormRef" :model="editForm" label-width="80px">
        <el-form-item label="Email">
          <el-input v-model="editForm.email" placeholder="Enter email" />
        </el-form-item>
        <el-form-item label="Nickname">
          <el-input v-model="editForm.nickname" placeholder="Enter nickname" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showEditDialog = false">Cancel</el-button>
        <el-button type="primary" @click="saveProfile">
          Save Changes
        </el-button>
      </template>
    </el-dialog>
    
    <!-- Change Password Dialog -->
    <el-dialog 
      v-model="showChangePasswordDialog" 
      title="Change Password"
      width="400px"
      class="ai-dialog"
    >
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef">
        <el-form-item prop="currentPassword">
          <el-input 
            v-model="passwordForm.currentPassword" 
            type="password"
            placeholder="Current Password"
            show-password
          />
        </el-form-item>
        <el-form-item prop="newPassword">
          <el-input 
            v-model="passwordForm.newPassword" 
            type="password"
            placeholder="New Password (min 6 chars)"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input 
            v-model="passwordForm.confirmPassword" 
            type="password"
            placeholder="Confirm New Password"
            show-password
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showChangePasswordDialog = false">Cancel</el-button>
        <el-button type="primary" @click="changePassword">
          Change Password
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useChatStore } from '../stores/chat'
import { ElMessage } from 'element-plus'
import { Lock, Edit } from '@element-plus/icons-vue'
import request from '../utils/request'

const router = useRouter()
const authStore = useAuthStore()
const chatStore = useChatStore()

const user = computed(() => authStore.currentUser)
const showEditDialog = ref(false)
const showChangePasswordDialog = ref(false)
const editFormRef = ref(null)
const passwordFormRef = ref()

const editForm = ref({
  email: '',
  nickname: ''
})

const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// Usage statistics
const usageStats = ref({
  conversationCount: 0,
  messageCount: 0,
  roleCount: 0,
  tokenUsage: 0
})

// Week activity data
const weekActivity = ref([])

// Top models data
const topModels = ref([])

// Token quota - 每个新用户有100000 token
const totalTokens = ref(100000)

// 计算token使用百分比
const tokenUsagePercentage = computed(() => {
  if (totalTokens.value === 0) return 0
  const percentage = (usageStats.value.tokenUsage / totalTokens.value) * 100
  return Math.min(Math.round(percentage), 100)
})

// 计算剩余token
const remainingTokens = computed(() => {
  return Math.max(totalTokens.value - usageStats.value.tokenUsage, 0)
})

// 根据使用百分比设置进度条颜色
const progressColor = computed(() => {
  const percentage = tokenUsagePercentage.value
  if (percentage < 50) {
    return '#00bcd4' // 青色 - 使用量少
  } else if (percentage < 80) {
    return '#2196f3' // 蓝色 - 使用量中等
  } else if (percentage < 95) {
    return '#ff9800' // 橙色 - 使用量较高
  } else {
    return '#f44336' // 红色 - 使用量很高
  }
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('Please confirm password'))
  } else if (value !== passwordForm.value.newPassword) {
    callback(new Error('Passwords do not match'))
  } else {
    callback()
  }
}

const passwordRules = {
  currentPassword: [
    { required: true, message: 'Please enter current password', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: 'Please enter new password', trigger: 'blur' },
    { min: 6, message: 'Password must be at least 6 characters', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const goBack = () => {
  router.push('/chat')
}

const editProfile = () => {
  editForm.value.email = user.value?.email || ''
  editForm.value.nickname = user.value?.nickname || ''
  showEditDialog.value = true
}

const saveProfile = async () => {
  try {
    await request({
      url: '/auth/profile',
      method: 'put',
      data: editForm.value
    })
    ElMessage.success('Profile updated successfully')
    showEditDialog.value = false
    // Refresh user data
    authStore.user = { ...authStore.user, ...editForm.value }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || 'Failed to update profile')
  }
}

const changePassword = async () => {
  try {
    await passwordFormRef.value.validate()
    await request({
      url: '/auth/change-password',
      method: 'post',
      data: {
        currentPassword: passwordForm.value.currentPassword,
        newPassword: passwordForm.value.newPassword
      }
    })
    ElMessage.success('Password changed successfully')
    showChangePasswordDialog.value = false
    passwordForm.value = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || 'Failed to change password')
  }
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString()
}

const formatNumber = (num) => {
  if (!num) return '0'
  return num.toLocaleString()
}

// Load usage statistics
const loadUsageStats = async () => {
  try {
    // 从后端API获取统计数据
    const response = await request({
      url: '/statistics',
      method: 'get'
    })
    
    if (response.data) {
      const stats = response.data
      usageStats.value = {
        conversationCount: stats.conversationCount || 0,
        messageCount: stats.messageCount || 0,
        roleCount: stats.roleCount || 0,
        tokenUsage: stats.tokenUsage || 0
      }
      
      // 设置周活跃度数据
      if (stats.weekActivity && stats.weekActivity.length > 0) {
        weekActivity.value = stats.weekActivity
      }
      
      // 设置模型使用统计
      if (stats.modelUsage && stats.modelUsage.length > 0) {
        topModels.value = stats.modelUsage
      }
    }
  } catch (error) {
    console.error('Failed to load usage stats:', error)
    ElMessage.error('Failed to load statistics')
  }
}

// 监听对话列表长度变化，自动刷新统计信息
watch(() => chatStore.conversations.length, async (newLength, oldLength) => {
  // 只有当长度真正变化时才刷新（避免初始化时重复调用）
  if (oldLength !== undefined && newLength !== oldLength) {
    // 等待一小段时间确保后端删除操作完成
    await new Promise(resolve => setTimeout(resolve, 300))
    await loadUsageStats()
  }
}, { immediate: false })

// 页面激活时也刷新统计信息
onActivated(() => {
  loadUsageStats()
})

onMounted(() => {
  loadUsageStats()
})
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  background: #0f0f1e;
  position: relative;
  overflow: auto;
}

/* Neural Network Background */
.neural-background {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 30% 50%, rgba(0, 188, 212, 0.05) 0%, transparent 50%),
    radial-gradient(circle at 70% 50%, rgba(33, 150, 243, 0.05) 0%, transparent 50%);
  opacity: 0.5;
  animation: pulse 10s ease-in-out infinite;
  pointer-events: none;
}

@keyframes pulse {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 0.6; }
}

/* Header */
.profile-header {
  background: rgba(26, 31, 58, 0.95);
  border-bottom: 1px solid rgba(0, 188, 212, 0.2);
  display: flex;
  align-items: center;
  padding: 0 32px;
  backdrop-filter: blur(10px);
  position: relative;
  z-index: 10;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
}

.back-btn {
  background: rgba(0, 188, 212, 0.1);
  border: 1px solid rgba(0, 188, 212, 0.3);
  border-radius: 8px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #00bcd4;
  font-size: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: rgba(0, 188, 212, 0.2);
  transform: translateX(-3px);
}

.profile-header h2 {
  margin: 0;
  font-size: 28px;
  background: linear-gradient(135deg, #00bcd4, #2196f3);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 600;
}

/* Main Content */
.profile-main {
  padding: 32px;
  position: relative;
  z-index: 1;
}

:deep(.profile-row) {
  display: flex;
  align-items: stretch;
}

:deep(.profile-col) {
  display: flex;
  flex-direction: column;
}

:deep(.profile-col .el-card) {
  width: 100%;
  height: 100%;
  flex: 1;
}

/* Cards */
.info-card, .usage-card {
  background: rgba(26, 31, 58, 0.8);
  border: 1px solid rgba(0, 188, 212, 0.2);
  border-radius: 20px;
  backdrop-filter: blur(10px);
  margin-bottom: 24px;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
}

.info-card {
  min-height: 450px;
}

.usage-card {
  min-height: 450px;
}

.usage-card :deep(.el-card__body) {
  padding-bottom: 24px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.info-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  flex: 1;
  justify-content: space-between;
}

/* 让右侧统计区域紧贴底部，不留空白 */
.usage-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 24px;
  margin-top: 0;
  width: 100%;
}

/* Token Usage Progress */
.token-usage-progress {
  margin-top: auto;
  padding: 20px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.progress-label {
  font-size: 14px;
  font-weight: 600;
  color: #e0e0e0;
}

.progress-percentage {
  font-size: 18px;
  font-weight: bold;
  background: linear-gradient(135deg, #00bcd4, #2196f3);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.token-progress-bar {
  margin-bottom: 12px;
}

:deep(.token-progress-bar .el-progress-bar__outer) {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
}

:deep(.token-progress-bar .el-progress-bar__inner) {
  border-radius: 10px;
  transition: all 0.3s ease;
}

.progress-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}

.progress-used {
  color: #00bcd4;
  font-weight: 600;
}

.progress-remaining {
  color: #9e9e9e;
  font-weight: 500;
}

.info-card:hover, .usage-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 40px rgba(0, 188, 212, 0.2);
  border-color: rgba(0, 188, 212, 0.4);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 600;
  font-size: 18px;
  color: #e0e0e0;
}

.header-icon {
  font-size: 24px;
  filter: drop-shadow(0 0 8px rgba(0, 188, 212, 0.5));
}

/* Descriptions */
:deep(.el-descriptions) {
  background: transparent;
}

:deep(.el-descriptions__label) {
  font-weight: 600;
  color: #b0bec5 !important;
  background: rgba(30, 41, 59, 0.8) !important;
  font-size: 14px;
}

:deep(.el-descriptions__content) {
  color: #ffffff !important;
  background: rgba(15, 23, 42, 0.6) !important;
  font-size: 15px;
}

:deep(.el-descriptions__cell) {
  border-color: rgba(0, 188, 212, 0.2) !important;
  padding: 16px !important;
}

.value-text {
  color: #00e5ff !important;
  font-weight: 600;
  font-size: 15px;
  text-shadow: 0 0 8px rgba(0, 229, 255, 0.3);
}

.email-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* Divider */
:deep(.el-divider) {
  margin: 24px 0;
  border-color: rgba(255, 255, 255, 0.1);
}

/* Action Buttons */
.action-buttons {
  display: flex;
  gap: 12px;
}

.action-buttons .el-button,
.action-buttons .action-btn {
  flex: 1;
  height: 48px;
  font-size: 16px;
  font-weight: 700;
  border-radius: 12px;
  transition: all 0.3s ease;
}

:deep(.action-btn.el-button--primary) {
  background: linear-gradient(135deg, #00bcd4, #2196f3);
  border: none;
  color: #ffffff !important;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

:deep(.action-btn.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 188, 212, 0.4);
}

:deep(.action-btn .el-button__text) {
  font-weight: 700 !important;
  font-size: 16px !important;
}

/* Usage Statistics - 已在上面定义 */

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.stat-item:hover {
  background: rgba(0, 188, 212, 0.1);
  border-color: rgba(0, 188, 212, 0.3);
  transform: translateY(-2px);
}

.stat-icon {
  font-size: 36px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: rgba(0, 188, 212, 0.1);
  border: 1px solid rgba(0, 188, 212, 0.3);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  background: linear-gradient(135deg, #00bcd4, #2196f3);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 13px;
  color: #9e9e9e;
  font-weight: 500;
}

/* Activity Chart */
.usage-chart h4,
.model-usage h4 {
  color: #e0e0e0;
  margin-bottom: 16px;
  font-size: 16px;
}

.activity-bars {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  height: 120px;
  gap: 8px;
  padding: 16px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 12px;
}

.activity-day {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.bar-container {
  width: 100%;
  height: 80px;
  display: flex;
  align-items: flex-end;
}

.bar {
  width: 100%;
  background: linear-gradient(to top, #00bcd4, #2196f3);
  border-radius: 4px 4px 0 0;
  transition: all 0.3s ease;
  cursor: pointer;
}

.bar:hover {
  opacity: 0.8;
  transform: scaleY(1.05);
}

.day-label {
  font-size: 12px;
  color: #9e9e9e;
  font-weight: 500;
}

/* Model Usage */
.model-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.model-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.model-name {
  color: #e0e0e0;
  font-weight: 500;
  font-size: 14px;
}

:deep(.el-progress__text) {
  color: #00bcd4 !important;
  font-weight: 600;
}

/* Dialog */
.ai-dialog :deep(.el-dialog) {
  background: rgba(26, 31, 58, 0.95);
  border: 1px solid rgba(0, 188, 212, 0.3);
  backdrop-filter: blur(10px);
}

.ai-dialog :deep(.el-dialog__title) {
  color: #e0e0e0;
}

.ai-dialog :deep(.el-form-item__label) {
  color: #9e9e9e;
}

.ai-dialog :deep(.el-input__wrapper) {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: none;
}

.ai-dialog :deep(.el-input__inner) {
  color: #e0e0e0;
}

/* Responsive */
@media (max-width: 768px) {
  .usage-stats {
    grid-template-columns: 1fr;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .profile-main {
    padding: 16px;
  }
}
</style>
