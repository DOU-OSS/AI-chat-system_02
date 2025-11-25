import { defineStore } from 'pinia'
import { login as loginApi, register as registerApi } from '../api/auth'
import { ElMessage } from 'element-plus'

export const useAuthStore = defineStore('auth', {
  state: () => {
    let user = null
    try {
      const userStr = localStorage.getItem('user')
      if (userStr) {
        user = JSON.parse(userStr)
      }
    } catch (e) {
      console.error('Failed to parse user from localStorage:', e)
      localStorage.removeItem('user')
    }
    return {
      token: localStorage.getItem('token') || '',
      user: user
    }
  },

  getters: {
    isLoggedIn: (state) => !!state.token,
    currentUser: (state) => state.user
  },

  actions: {
    async login(loginData) {
      try {
        const response = await loginApi(loginData)
        const { token, user } = response.data
        
        this.token = token
        this.user = user
        
        localStorage.setItem('token', token)
        localStorage.setItem('user', JSON.stringify(user))
        
        ElMessage.success('Login successful')
        return response
      } catch (error) {
        throw error
      }
    },

    async register(registerData) {
      try {
        const response = await registerApi(registerData)
        ElMessage.success('Registration successful, please login')
        return response
      } catch (error) {
        throw error
      }
    },

    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      ElMessage.success('Logout successful')
    }
  }
})
