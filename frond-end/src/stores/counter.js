import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    isLoggedIn: false,
    token: null, // 存储JWT token
    user: null // 存储用户信息
  }),
  actions: {
    initAuth() {
      // 从 localStorage 中加载 token 和用户信息
      const savedToken = localStorage.getItem('authToken');
      const savedUser = localStorage.getItem('userInfo');

      if (savedToken) {
        this.token = savedToken;
        this.isLoggedIn = true;
        this.user = savedUser ? JSON.parse(savedUser) : null;
      }
    },

    login(user) {
      // 假设这里是你获得的 token
      const token = user.token; // 这里的 user 应该是包含 token 的对象
      this.token = token;
      this.user = user; // 存储用户信息
      this.isLoggedIn = true;

      // 将 token 和用户信息保存到 localStorage
      localStorage.setItem('authToken', token);
      localStorage.setItem('userInfo', JSON.stringify(user));
    },

    logout() {
      this.user = null;
      this.token = null;
      this.isLoggedIn = false;

      // 清除 localStorage 中的用户信息
      localStorage.removeItem('authToken');
      localStorage.removeItem('userInfo');
    }
  }
});