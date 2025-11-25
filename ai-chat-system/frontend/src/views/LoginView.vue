<template>
  <div class="login-container">
    <div class="ai-particles"></div>
    <div class="auth-card">
      <div class="logo-section">
        <div class="ai-logo">
          <span class="logo-icon">🤖</span>
        </div>
        <h1 class="system-title">AI Chat System</h1>
        <p class="system-subtitle">Powered by Advanced AI Technology</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="Username"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="Password"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            class="gradient-button"
          >
            {{ loading ? 'Logging in...' : 'Login to AI Chat' }}
          </el-button>
        </el-form-item>
        <el-form-item>
          <div class="auth-footer">
            <span class="footer-text">Don't have an account?</span>
            <router-link to="/register" class="register-link">
              Create Account
            </router-link>
          </div>
        </el-form-item>
        <div class="features-hint">
          <span>💬 Chat with AI</span>
          <span>•</span>
          <span>🎭 Multiple AI Roles</span>
          <span>•</span>
          <span>📝 Save Conversations</span>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: 'Please enter username', trigger: 'blur' }
  ],
  password: [
    { required: true, message: 'Please enter password', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await authStore.login(form)
    router.push('/chat')
  } catch (error) {
    console.error('Login failed:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0a0e27 0%, #1a1f3a 100%);
  position: relative;
  overflow: hidden;
}

.ai-particles {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: 
    radial-gradient(circle at 20% 50%, rgba(0, 188, 212, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 50%, rgba(33, 150, 243, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 50% 30%, rgba(0, 123, 255, 0.1) 0%, transparent 50%);
  animation: float 20s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  33% { transform: translateY(-10px) rotate(1deg); }
  66% { transform: translateY(10px) rotate(-1deg); }
}

.auth-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 40px;
  width: 100%;
  max-width: 450px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 1;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.logo-section {
  text-align: center;
  margin-bottom: 40px;
}

.ai-logo {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #00bcd4, #2196f3);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 30px rgba(33, 150, 243, 0.3);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0% { transform: scale(1); box-shadow: 0 10px 30px rgba(33, 150, 243, 0.3); }
  50% { transform: scale(1.05); box-shadow: 0 10px 40px rgba(33, 150, 243, 0.5); }
  100% { transform: scale(1); box-shadow: 0 10px 30px rgba(33, 150, 243, 0.3); }
}

.logo-icon {
  font-size: 40px;
}

.system-title {
  font-size: 28px;
  font-weight: 700;
  color: #1a1f3a;
  margin: 0 0 10px;
  background: linear-gradient(135deg, #00bcd4, #2196f3);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.system-subtitle {
  color: #64748b;
  font-size: 14px;
  margin: 0;
}

.gradient-button {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #00bcd4, #2196f3);
  border: none;
  font-weight: 600;
  font-size: 16px;
  transition: all 0.3s ease;
}

.gradient-button:hover {
  background: linear-gradient(135deg, #00acc1, #1e88e5);
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(33, 150, 243, 0.3);
}

.auth-footer {
  text-align: center;
  width: 100%;
}

.footer-text {
  color: #64748b;
  font-size: 14px;
}

.register-link {
  color: #2196f3;
  text-decoration: none;
  font-weight: 600;
  margin-left: 5px;
  transition: color 0.3s ease;
}

.register-link:hover {
  color: #00bcd4;
}

.features-hint {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
  color: #94a3b8;
  font-size: 12px;
}

:deep(.el-input__wrapper) {
  box-shadow: none !important;
  background-color: #f8fafc;
  border-radius: 12px;
  padding: 4px 12px;
}

:deep(.el-input__inner) {
  font-size: 15px;
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}
</style>
