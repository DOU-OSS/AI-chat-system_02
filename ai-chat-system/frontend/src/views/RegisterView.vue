<template>
  <div class="register-container">
    <div class="ai-particles"></div>
    <div class="auth-card">
      <div class="logo-section">
        <div class="ai-logo">
          <span class="logo-icon">🤖</span>
        </div>
        <h1 class="system-title">Join AI Chat</h1>
        <p class="system-subtitle">Create your account and start chatting with AI</p>
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
        <el-form-item prop="email">
          <el-input
            v-model="form.email"
            placeholder="Email"
            prefix-icon="Message"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="verificationCode">
          <div class="verification-code-input">
            <el-input
              v-model="form.verificationCode"
              placeholder="Verification Code"
              prefix-icon="Key"
              size="large"
              style="flex: 1"
            />
            <el-button
              size="large"
              :disabled="countdown > 0 || !form.email"
              @click="sendVerificationCode"
              class="verification-button"
            >
              {{ countdown > 0 ? `${countdown}s` : 'Send Code' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="Password"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="Confirm Password"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input
            v-model="form.nickname"
            placeholder="Nickname (Optional)"
            prefix-icon="Avatar"
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleRegister"
            class="gradient-button"
          >
            {{ loading ? 'Creating Account...' : 'Create Account' }}
          </el-button>
        </el-form-item>
        <el-form-item>
          <div class="auth-footer">
            <span class="footer-text">Already have an account?</span>
            <router-link to="/login" class="login-link">
              Sign In
            </router-link>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  email: '',
  verificationCode: '',
  password: '',
  confirmPassword: '',
  nickname: ''
})

const countdown = ref(0)
let timer = null

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('Passwords do not match'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: 'Please enter username', trigger: 'blur' },
    { min: 3, max: 50, message: 'Username must be 3-50 characters', trigger: 'blur' }
  ],
  email: [
    { required: true, message: 'Please enter email', trigger: 'blur' },
    { type: 'email', message: 'Please enter valid email', trigger: 'blur' }
  ],
  password: [
    { required: true, message: 'Please enter password', trigger: 'blur' },
    { min: 6, message: 'Password must be at least 6 characters', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: 'Please confirm password', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: 'Please enter verification code', trigger: 'blur' },
    { len: 6, message: 'Verification code must be 6 digits', trigger: 'blur' }
  ]
}

const sendVerificationCode = async () => {
  if (!form.email) {
    ElMessage.error('Please enter your email first')
    return
  }
  
  try {
    await request({
      url: '/auth/send-verification-code',
      method: 'post',
      params: { email: form.email }
    })
    ElMessage.success('Verification code sent to your email')
    startCountdown()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || 'Failed to send verification code')
  }
}

const startCountdown = () => {
  countdown.value = 60
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

const handleRegister = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    // 先验证验证码
    await request({
      url: '/auth/verify-code',
      method: 'post',
      params: { 
        email: form.email,
        code: form.verificationCode
      }
    })
    
    // 验证成功后注册
    await authStore.register({
      username: form.username,
      email: form.email,
      password: form.password,
      nickname: form.nickname
    })
    ElMessage.success('Registration successful!')
    router.push('/login')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || 'Registration failed')
    console.error('Registration failed:', error)
  } finally {
    loading.value = false
  }
}

onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.register-container {
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
  margin-bottom: 30px;
}

.ai-logo {
  width: 70px;
  height: 70px;
  margin: 0 auto 15px;
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
  font-size: 35px;
}

.system-title {
  font-size: 26px;
  font-weight: 700;
  color: #1a1f3a;
  margin: 0 0 8px;
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

.login-link {
  color: #2196f3;
  text-decoration: none;
  font-weight: 600;
  margin-left: 5px;
  transition: color 0.3s ease;
}

.login-link:hover {
  color: #00bcd4;
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
  margin-bottom: 20px;
}

:deep(.el-form-item__error) {
  font-size: 12px;
  padding-top: 4px;
}

.verification-code-input {
  display: flex;
  gap: 10px;
  width: 100%;
}

.verification-button {
  background: linear-gradient(135deg, #00bcd4, #2196f3);
  border: none;
  color: white;
  font-weight: 600;
  width: 120px;
  transition: all 0.3s ease;
}

.verification-button:hover:not(:disabled) {
  background: linear-gradient(135deg, #00acc1, #1e88e5);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(33, 150, 243, 0.3);
}

.verification-button:disabled {
  background: #e0e0e0;
  color: #9e9e9e;
  cursor: not-allowed;
}
</style>
