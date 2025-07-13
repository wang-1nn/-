import {defineStore} from 'pinia'

export const useCounterStore = defineStore('counter', {
  state: () => {
    return { count: 0 };
  },
  actions: {
    increment() {
      this.count++;
    },
  },
});

// 认证状态管理
export const useAuthStore = defineStore('auth', {
  state: () => ({
    isLoggedIn: false,
    user: null,
    userRole: null,
    token: null
  }),
  
  actions: {
    login(credentials) {
      // 设置登录状态
          this.isLoggedIn = true
          
      // 从credentials中获取角色和用户数据
      if (credentials.role) {
        this.userRole = credentials.role
      }
      
      // 设置用户信息
      if (credentials.user) {
        this.user = credentials.user
      }
      
      // 设置JWT令牌
      if (credentials.token) {
        this.token = credentials.token
        // 将token保存到localStorage
        localStorage.setItem('authToken', credentials.token)
      }
      
      // 将登录信息保存到本地存储，以便刷新页面后能够保持登录状态
      localStorage.setItem('auth', JSON.stringify({
        isLoggedIn: this.isLoggedIn,
        user: this.user,
        userRole: this.userRole,
        token: this.token
      }))
    },

    logout() {
      this.isLoggedIn = false
      this.user = null
      this.userRole = null
      this.token = null
      
      // 清除本地存储中的登录信息
      localStorage.removeItem('auth')
      localStorage.removeItem('authToken')
    },
    
    // 初始化方法，从本地存储中恢复登录状态
    initializeFromStorage() {
      const storedAuth = localStorage.getItem('auth')
      if (storedAuth) {
        try {
          const auth = JSON.parse(storedAuth)
          this.isLoggedIn = auth.isLoggedIn
          this.user = auth.user
          this.userRole = auth.userRole
          this.token = auth.token
        } catch (e) {
          console.error('Failed to parse stored auth', e)
          this.logout()
        }
      }
    }
  }
});