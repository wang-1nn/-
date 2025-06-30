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
    // 默认已登录状态，使用教师角色便于测试
    isLoggedIn: true,
    user: {
      id: 1,
      name: '张老师',
      avatar: 'https://i.pravatar.cc/300',
      email: 'teacher@example.com'
    },
    userRole: 'teacher',
    token: 'mock-jwt-token'
  }),
  
  actions: {
    login(credentials) {
      // 模拟登录请求
      return new Promise((resolve) => {
        setTimeout(() => {
          this.isLoggedIn = true
          
          // 根据用户名判断角色
          if (credentials.username.includes('admin')) {
            this.userRole = 'admin'
            this.user = {
              id: 3,
              name: '管理员',
              avatar: 'https://i.pravatar.cc/300?img=3',
              email: 'admin@example.com'
      }
          } else if (credentials.username.includes('student')) {
            this.userRole = 'student'
            this.user = {
              id: 2,
              name: '李同学',
              avatar: 'https://i.pravatar.cc/300?img=2',
              email: 'student@example.com'
            }
          } else {
            this.userRole = 'teacher'
            this.user = {
              id: 1,
              name: '张老师',
              avatar: 'https://i.pravatar.cc/300?img=1',
              email: 'teacher@example.com'
            }
          }
          
          this.token = 'mock-jwt-token'
          resolve({ success: true })
        }, 1000)
      })
    },

    logout() {
      this.isLoggedIn = false
      this.user = null
      this.userRole = null
      this.token = null
    }
  }
});