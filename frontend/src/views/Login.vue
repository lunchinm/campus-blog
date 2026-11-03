<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '@/api/user.js'
import { setToken, setUser } from '@/utils/auth.js'

const router = useRouter()
const form = ref({ username: '', password: '' })
const loading = ref(false)

const handleLogin = async () => {
    if (!form.value.username || !form.value.password) {
        ElMessage.warning('请填写完整信息')
        return
    }
    loading.value = true
    try {
        const res = await login(form.value)
        if (res.success) {
            setToken(res.token)
            setUser(res.user)
            ElMessage.success('登录成功')
            router.push('/')
        } else {
            ElMessage.error(res.message)
        }
    } catch (error) {
        ElMessage.error('登录失败，请检查网络')
    } finally {
        loading.value = false
    }
}
</script>

<template>
  <div class="login-page">
    <form class="form" @submit.prevent="handleLogin">
      <h2 class="title">登录</h2>
      <input
        class="input"
        type="text"
        v-model="form.username"
        name="username"
        autocomplete="username"
        spellcheck="false"
        placeholder="请输入用户名"
      />
      <input
        class="input"
        type="password"
        v-model="form.password"
        name="password"
        autocomplete="current-password"
        placeholder="请输入密码"
      />
      <button class="button" type="submit" :disabled="loading">
        {{ loading ? '登录中...' : '登录' }}
      </button>
      <p class="switch">
        还没有账号？<a @click="router.push('/register')">去注册</a>
      </p>
    </form>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 80px 20px 20px;
}
.form {
  position: relative;
  display: flex;
  align-items: center;
  flex-direction: column;
  gap: 0;
  background: transparent;
  border-radius: 2.5em;
  padding: 36px 32px;
  width: 340px;
  z-index: 1;
}
.form::before {
  content: '';
  position: absolute;
  inset: -1px;
  background: linear-gradient(325deg, #3a7de9 0%, #47b8ff 55%, #3a7de9 90%);
  background-size: 280% auto;
  border-radius: calc(2.5em + 1px);
  z-index: -1;
  -webkit-mask:
    linear-gradient(#fff 0 0) content-box,
    linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  padding: 1px;
}
.form::after {
  content: '';
  position: absolute;
  inset: 0;
  background: transparent;
  border-radius: 2.5em;
  z-index: -1;
  pointer-events: none;
}
.title {
  color: #409eff;
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 10px;
  letter-spacing: 2px;
}
.input {
  width: calc(100% - 10px);
  padding: 8px;
  margin-bottom: 20px;
  border: 1px solid transparent;
  border-bottom: 1px solid #409eff;
  outline: none;
  background-color: transparent;
  color: #409eff;
  font-family: Arial, Helvetica, sans-serif;
  font-size: 14px;
  transition: 0.2s;
}
.input:focus {
  border: 1px solid transparent;
  border-bottom: 1px solid #409eff;
  background-color: transparent;
  color: #08060d;
}
.input::placeholder {
  color: #8c9db5;
}
.input:-webkit-autofill,
.input:-webkit-autofill:hover,
.input:-webkit-autofill:focus {
  -webkit-text-fill-color: #409eff;
  -webkit-box-shadow: 0 0 0px 1000px transparent inset;
  transition: background-color 5000s ease-in-out 0s;
  caret-color: #409eff;
}
.button {
  margin-top: 10px;
  width: 100%;
  font-family: Arial, Helvetica, sans-serif;
  font-size: 16px;
  font-weight: 600;
  background-size: 280% auto;
  background-image: linear-gradient(325deg, #3a7de9 0%, #47b8ff 55%, #3a7de9 90%);
  color: #fff;
  padding: 0.7em 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid transparent;
  border-radius: 5em;
  overflow: hidden;
  transition: all 0.8s ease;
  cursor: pointer;
  box-shadow:
    0px 0px 20px rgba(71, 184, 255, 0.5),
    0px 5px 5px -1px rgba(58, 125, 233, 0.25),
    inset 4px 4px 8px rgba(175, 230, 255, 0.5),
    inset -4px -4px 8px rgba(19, 95, 216, 0.35);
}
.button:hover {
  background-position: right top;
  color: #fff;
}
.button:active {
  border: 2px solid transparent;
  background: #3a7de9;
  color: #fff;
}
.button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
.switch {
  color: #8c9db5;
  font-size: 13px;
  margin: 8px 0 0;
}
.switch a {
  color: #409eff;
  cursor: pointer;
  text-decoration: underline;
}
.switch a:hover {
  color: #47b8ff;
}
</style>
