<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { isLoggedIn, getUser, removeToken } from '@/utils/auth.js'
import { getUnreadCount } from '@/api/notification.js'
import { getUnreadMessageCount } from '@/api/message.js'
import ClickSpark from '@/components/ClickSpark.vue'

const router = useRouter()

const loggedIn = ref(isLoggedIn())
const currentUser = ref(getUser())
const avatarVersion = ref(0)
const unreadCount = ref(0)
const unreadMessageCount = ref(0)
let unreadTimer = null
let messageUnreadTimer = null

const avatarSrc = computed(() => {
    if (!currentUser.value?.avatar) return null
    const sep = currentUser.value.avatar.includes('?') ? '&' : '?'
    return `${currentUser.value.avatar}${sep}v=${avatarVersion.value}`
})

router.afterEach(() => {
    loggedIn.value = isLoggedIn()
    currentUser.value = getUser()
})

const refreshUser = () => {
    currentUser.value = getUser()
    avatarVersion.value = Date.now()
}

window.addEventListener('avatar-changed', refreshUser)

const removeAvatarListener = () => {
    window.removeEventListener('avatar-changed', refreshUser)
}

const onNotificationsRead = () => {
    unreadCount.value = 0
}

const onMessageRead = () => {
    fetchUnreadMessageCount()
}

const handleLogout = () => {
    removeToken()
    ElMessage.success('已退出登录')
    router.push('/')
}

const fetchUnreadCount = async () => {
    if (!loggedIn.value) return
    try {
        const res = await getUnreadCount()
        unreadCount.value = res.count || 0
    } catch (error) {}
}

const fetchUnreadMessageCount = async () => {
    if (!loggedIn.value) return
    try {
        const res = await getUnreadMessageCount()
        unreadMessageCount.value = res.count || 0
    } catch (error) {}
}

const bubbleRefs = ref([])
const bubbleCount = 7

const colors = [
    ['#0fb4ff', '#ff4484', '#ffeb3b', '#ff4484', '#ffffff'],
    ['#ff4484', '#0fb4ff', '#ffffff', '#ffeb3b', '#0fb4ff'],
    ['#ffeb3b', '#ffffff', '#0fb4ff', '#ff4484', '#ff4484'],
    ['#0fb4ff', '#ffeb3b', '#ff4484', '#ffffff', '#ff4484'],
    ['#ffffff', '#ff4484', '#0fb4ff', '#ffeb3b', '#0fb4ff'],
    ['#ff4484', '#ffeb3b', '#ffffff', '#0fb4ff', '#ff4484'],
    ['#ffeb3b', '#0fb4ff', '#ff4484', '#ffffff', '#0fb4ff']
]

let bubbles = []
let animFrameId = null

const initBubbles = () => {
    const h = window.innerHeight
    const w = window.innerWidth
    bubbles = []
    for (let i = 0; i < bubbleCount; i++) {
        const baseSize = 40 + Math.random() * 30
        const speed = 0.3 + Math.random() * 0.5
        const wobbleAmp = 15 + Math.random() * 25
        const wobbleFreq = 0.8 + Math.random() * 1.5
        const startX = Math.random() * w * 0.8 + w * 0.1
        bubbles.push({
            t: Math.random(),
            baseSize,
            maxSize: baseSize * (3 + Math.random() * 3),
            speed,
            wobbleAmp,
            wobbleFreq,
            startX,
            colorSet: colors[i]
        })
    }
}

const animate = () => {
    const h = window.innerHeight
    const w = window.innerWidth
    bubbleRefs.value.forEach((el, i) => {
        if (!el) return
        const b = bubbles[i]
        b.t += b.speed * 0.0015
        if (b.t > 1.2) {
            b.t = -0.05
            b.baseSize = 40 + Math.random() * 30
            b.maxSize = b.baseSize * (3 + Math.random() * 3)
            b.speed = 0.3 + Math.random() * 0.5
            b.wobbleAmp = 15 + Math.random() * 25
            b.wobbleFreq = 0.8 + Math.random() * 1.5
            b.startX = Math.random() * w * 0.8 + w * 0.1
        }

        const progress = Math.max(0, Math.min(b.t, 1))
        const sizeProgress = Math.sqrt(progress)

        const currentSize = b.baseSize + (b.maxSize - b.baseSize) * sizeProgress
        const y = h + 50 - progress * (h + 100 + currentSize)
        const x = b.startX + Math.sin(b.t * b.wobbleFreq * Math.PI * 2) * b.wobbleAmp

        el.style.width = `${currentSize}px`
        el.style.height = `${currentSize}px`
        el.style.left = `${x}px`
        el.style.top = `${y}px`
        el.style.opacity = progress < 0.08 ? progress / 0.08 : progress > 0.9 ? (1 - progress) / 0.1 : 1
    })
    animFrameId = requestAnimationFrame(animate)
}

onMounted(() => {
    initBubbles()
    animFrameId = requestAnimationFrame(animate)
    fetchUnreadCount()
    fetchUnreadMessageCount()
    unreadTimer = setInterval(fetchUnreadCount, 30000)
    messageUnreadTimer = setInterval(fetchUnreadMessageCount, 30000)
    window.addEventListener('notifications-read', onNotificationsRead)
    window.addEventListener('message-read', onMessageRead)
})

onUnmounted(() => {
    if (animFrameId) cancelAnimationFrame(animFrameId)
    removeAvatarListener()
    if (unreadTimer) clearInterval(unreadTimer)
    if (messageUnreadTimer) clearInterval(messageUnreadTimer)
    window.removeEventListener('notifications-read', onNotificationsRead)
    window.removeEventListener('message-read', onMessageRead)
})
</script>

<template>
  <ClickSpark spark-color="#ffffff" :spark-count="8" :spark-size="10" :spark-radius="15">
    <div id="app">
      <div class="bubble-bg">
        <div v-for="(cs, idx) in colors" :key="idx" :ref="el => bubbleRefs[idx] = el" class="bubble">
          <span :style="{ borderColor: cs[0] }"></span>
          <span :style="{ borderColor: cs[1] }"></span>
          <span :style="{ borderColor: cs[2] }"></span>
          <span :style="{ borderColor: cs[3] }"></span>
          <span :style="{ borderColor: cs[4] }"></span>
        </div>
      </div>
      <nav>
        <div class="pill-nav-items">
          <ul class="pill-list" role="menubar">
            <li role="none">
              <router-link to="/articles" class="pill" active-class="is-active" role="menuitem" aria-label="文章">
                <span class="hover-circle" aria-hidden="true"></span>
                <span class="label-stack">
                  <span class="pill-label">文章</span>
                  <span class="pill-label-hover" aria-hidden="true">文章</span>
                </span>
              </router-link>
            </li>
            <template v-if="loggedIn">
              <li role="none">
                <router-link to="/my-articles" class="pill" active-class="is-active" role="menuitem" aria-label="我的">
                  <span class="hover-circle" aria-hidden="true"></span>
                  <span class="label-stack">
                    <span class="pill-label">我的</span>
                    <span class="pill-label-hover" aria-hidden="true">我的</span>
                  </span>
                </router-link>
              </li>
              <li role="none">
                <router-link to="/drafts" class="pill" active-class="is-active" role="menuitem" aria-label="草稿箱">
                  <span class="hover-circle" aria-hidden="true"></span>
                  <span class="label-stack">
                    <span class="pill-label">草稿箱</span>
                    <span class="pill-label-hover" aria-hidden="true">草稿箱</span>
                  </span>
                </router-link>
              </li>
              <li role="none">
                <router-link to="/notifications" class="pill" active-class="is-active" role="menuitem" aria-label="通知">
                  <span class="hover-circle" aria-hidden="true"></span>
                  <span class="label-stack">
                    <span class="pill-label">通知</span>
                    <span class="pill-label-hover" aria-hidden="true">通知</span>
                  </span>
                  <span v-if="unreadCount > 0" class="pill-badge">{{ unreadCount }}</span>
                </router-link>
              </li>
              <li role="none">
                <router-link to="/messages" class="pill" active-class="is-active" role="menuitem" aria-label="私信">
                  <span class="hover-circle" aria-hidden="true"></span>
                  <span class="label-stack">
                    <span class="pill-label">私信</span>
                    <span class="pill-label-hover" aria-hidden="true">私信</span>
                  </span>
                  <span v-if="unreadMessageCount > 0" class="pill-badge">{{ unreadMessageCount }}</span>
                </router-link>
              </li>
            </template>
          </ul>
        </div>
        <div class="nav-right">
          <template v-if="loggedIn">
            <div class="user-area">
              <router-link :to="`/user/${currentUser?.id}`" aria-label="个人主页">
                <el-avatar
                  :size="36"
                  :src="avatarSrc"
                  :key="avatarVersion"
                  class="nav-avatar"
                >{{ currentUser?.nickname?.charAt(0) || currentUser?.username?.charAt(0) || 'U' }}</el-avatar>
              </router-link>
              <span class="user-nickname">{{ currentUser?.nickname || currentUser?.username }}</span>
              <button class="logout-btn" @click="handleLogout" type="button" title="退出登录">
                <div class="logout-box">
                  <span class="logout-elem">
                    <svg viewBox="0 0 46 40" xmlns="http://www.w3.org/2000/svg">
                      <path d="M46 20.038c0-.7-.3-1.5-.8-2.1l-16-17c-1.1-1-3.2-1.4-4.4-.3-1.2 1.1-1.2 3.3 0 4.4l11.3 11.9H3c-1.7 0-3 1.3-3 3s1.3 3 3 3h33.1l-11.3 11.9c-1 1-1.2 3.3 0 4.4 1.2 1.1 3.3.8 4.4-.3l16-17c.5-.5.8-1.1.8-1.9z"></path>
                    </svg>
                  </span>
                  <span class="logout-elem">
                    <svg viewBox="0 0 46 40">
                      <path d="M46 20.038c0-.7-.3-1.5-.8-2.1l-16-17c-1.1-1-3.2-1.4-4.4-.3-1.2 1.1-1.2 3.3 0 4.4l11.3 11.9H3c-1.7 0-3 1.3-3 3s1.3 3 3 3h33.1l-11.3 11.9c-1 1-1.2 3.3 0 4.4 1.2 1.1 3.3.8 4.4-.3l16-17c.5-.5.8-1.1.8-1.9z"></path>
                    </svg>
                  </span>
                </div>
              </button>
            </div>
          </template>
          <template v-else>
            <el-button class="donate-btn nav-auth-btn" size="small" @click="router.push('/login')">登录</el-button>
            <el-button class="neu-btn nav-auth-btn" size="small" @click="router.push('/register')">注册</el-button>
          </template>
        </div>
      </nav>
      <router-view />
    </div>
  </ClickSpark>
</template>

<style scoped>
#app {
  position: relative;
  min-height: 100vh;
  width: 100%;
}
.bubble-bg {
  position: fixed;
  inset: 0;
  overflow: hidden;
  z-index: -1;
  background: linear-gradient(135deg, #fff5f8, #f0f7ff);
}
.bubble-bg::before,
.bubble-bg::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 200%;
  height: 200%;
  background: conic-gradient(
    from 0deg,
    #ffd1dc,
    #ffe4d6,
    #fff5cc,
    #e8f2d0,
    #d4ecff,
    #dce6ff,
    #ede3ff,
    #ffe4d6,
    #ffd1dc
  );
  transform: translate(-50%, -50%);
  filter: blur(80px);
  opacity: 0.55;
}
.bubble-bg::after {
  width: 180%;
  height: 180%;
  opacity: 0.35;
}
@media (prefers-reduced-motion: no-preference) {
  .bubble-bg::before {
    animation: rotate 8s linear infinite;
  }
  .bubble-bg::after {
    animation: rotate-reverse 10s linear infinite;
  }
}
@keyframes rotate {
  0% { transform: translate(-50%, -50%) rotate(0deg); }
  100% { transform: translate(-50%, -50%) rotate(360deg); }
}
@keyframes rotate-reverse {
  0% { transform: translate(-50%, -50%) rotate(0deg); }
  100% { transform: translate(-50%, -50%) rotate(-360deg); }
}
.bubble {
  position: absolute;
  border-radius: 50%;
  box-shadow: inset 0 0 25px rgba(255, 255, 255, 0.25);
  will-change: transform, opacity;
}
.bubble::before {
  content: '';
  position: absolute;
  top: 22%;
  left: 22%;
  width: 16%;
  height: 16%;
  border-radius: 50%;
  background: #fff;
  filter: blur(2px);
}
.bubble::after {
  content: '';
  position: absolute;
  top: 38%;
  left: 38%;
  width: 11%;
  height: 11%;
  border-radius: 50%;
  background: #fff;
  filter: blur(2px);
}
.bubble span {
  position: absolute;
  border-radius: 50%;
}
.bubble span:nth-child(1) {
  inset: 6%;
  border-left: 7px solid;
  filter: blur(6px);
}
.bubble span:nth-child(2) {
  inset: 6%;
  border-right: 7px solid;
  filter: blur(6px);
}
.bubble span:nth-child(3) {
  inset: 6%;
  border-top: 7px solid;
  filter: blur(6px);
}
.bubble span:nth-child(4) {
  inset: 18%;
  border-left: 7px solid;
  filter: blur(10px);
}
.bubble span:nth-child(5) {
  inset: 6%;
  border-bottom: 5px solid;
  transform: rotate(330deg);
  filter: blur(6px);
}
nav {
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(10px);
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}
.pill-nav-items {
  position: relative;
  display: flex;
  align-items: center;
  height: 42px;
  background: transparent;
  flex: 1;
  justify-content: flex-start;
}
.pill-list {
  list-style: none;
  display: flex;
  align-items: stretch;
  gap: 8px;
  margin: 0;
  padding: 0;
  height: 100%;
}
.pill-list > li {
  display: flex;
  height: 100%;
  position: relative;
}
.pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 0 18px;
  background: #fff;
  color: #222;
  text-decoration: none;
  border-radius: 9999px;
  box-sizing: border-box;
  font-weight: 600;
  font-size: 13px;
  line-height: 1;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  white-space: nowrap;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: color 0.3s ease;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.pill .hover-circle {
  position: absolute;
  left: 50%;
  bottom: -36px;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #000;
  transform: translateX(-50%) scale(0);
  transition: transform 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  z-index: 1;
  pointer-events: none;
}
.pill:hover .hover-circle,
.pill.is-active .hover-circle {
  transform: translateX(-50%) scale(1);
}
.pill .label-stack {
  position: relative;
  display: inline-block;
  line-height: 1;
  z-index: 2;
  overflow: hidden;
  height: 1.2em;
}
.pill .pill-label {
  position: relative;
  z-index: 2;
  display: block;
  line-height: 1.2;
  transition: transform 0.3s ease, opacity 0.3s ease;
}
.pill .pill-label-hover {
  position: absolute;
  left: 0;
  top: 0;
  color: #fff;
  z-index: 3;
  display: block;
  line-height: 1.2;
  transform: translateY(100%);
  opacity: 0;
  transition: transform 0.3s ease, opacity 0.3s ease;
}
.pill:hover .pill-label,
.pill.is-active .pill-label {
  transform: translateY(-100%);
  opacity: 0;
}
.pill:hover .pill-label-hover,
.pill.is-active .pill-label-hover {
  transform: translateY(0);
  opacity: 1;
}
.pill.is-active::after {
  content: '';
  position: absolute;
  bottom: -6px;
  left: 50%;
  transform: translateX(-50%);
  width: 10px;
  height: 10px;
  background: #000;
  border-radius: 50%;
  z-index: 4;
}
.pill-badge {
  position: absolute;
  top: 2px;
  right: 6px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  background: #f56c6c;
  color: #fff;
  border-radius: 8px;
  font-size: 10px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 5;
}
.nav-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 10px;
}
.user-area {
  display: flex;
  align-items: center;
  gap: 10px;
}
.nav-avatar {
  cursor: pointer;
  flex-shrink: 0;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  border: 2px solid #90caf9;
}
.nav-avatar:hover {
  transform: scale(1.1);
}
.user-nickname {
  color: #08060d;
  font-weight: 500;
  font-size: 14px;
}
.logout-btn {
  display: block;
  position: relative;
  width: 42px;
  height: 42px;
  margin: 0;
  overflow: hidden;
  outline: none;
  background-color: transparent;
  cursor: pointer;
  border: 0;
  flex-shrink: 0;
}
.logout-btn:before,
.logout-btn:after {
  content: "";
  position: absolute;
  border-radius: 50%;
  inset: 5px;
}
.logout-btn:before {
  border: 3px solid #e0e0e0;
  transition: opacity 0.4s cubic-bezier(0.77, 0, 0.175, 1) 80ms,
    transform 0.5s cubic-bezier(0.455, 0.03, 0.515, 0.955) 80ms;
}
.logout-btn:after {
  border: 3px solid #96daf0;
  transform: scale(1.3);
  transition: opacity 0.4s cubic-bezier(0.165, 0.84, 0.44, 1),
    transform 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  opacity: 0;
}
.logout-btn:hover:before,
.logout-btn:focus:before {
  opacity: 0;
  transform: scale(0.7);
  transition: opacity 0.4s cubic-bezier(0.165, 0.84, 0.44, 1),
    transform 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}
.logout-btn:hover:after,
.logout-btn:focus:after {
  opacity: 1;
  transform: scale(1);
  transition: opacity 0.4s cubic-bezier(0.77, 0, 0.175, 1) 80ms,
    transform 0.5s cubic-bezier(0.455, 0.03, 0.515, 0.955) 80ms;
}
.logout-box {
  display: flex;
  position: absolute;
  top: 0;
  left: 0;
}
.logout-elem {
  display: block;
  width: 16px;
  height: 16px;
  margin: 13px 13px 0 13px;
  transform: rotate(180deg);
  fill: #888;
}
.logout-btn:hover .logout-box,
.logout-btn:focus .logout-box {
  transition: 0.4s;
  transform: translateX(-42px);
}
.nav-auth-btn.el-button:focus-visible {
  box-shadow: 0 0 0 2px #409eff !important;
}
</style>
