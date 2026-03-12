import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/modules'

export const useUserStore = defineStore('user', () => {
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const token = ref(localStorage.getItem('token') || '')

  const isAuthenticated = computed(() => !!token.value)
  const userInfo = computed(() => user.value)

  async function login(loginData) {
    try {
      const response = await authApi.login(loginData)
      token.value = response.data.accessToken
      user.value = response.data.user

      localStorage.setItem('token', response.data.accessToken)
      localStorage.setItem('user', JSON.stringify(response.data.user))

      return response
    } catch (error) {
      throw error
    }
  }

  async function register(registerData) {
    try {
      const response = await authApi.register(registerData)
      return response
    } catch (error) {
      throw error
    }
  }

  async function getCurrentUser() {
    try {
      const response = await authApi.getCurrentUser()
      user.value = response.data
      localStorage.setItem('user', JSON.stringify(response.data))
      return response
    } catch (error) {
      throw error
    }
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return {
    user,
    token,
    isAuthenticated,
    userInfo,
    login,
    register,
    getCurrentUser,
    logout
  }
})