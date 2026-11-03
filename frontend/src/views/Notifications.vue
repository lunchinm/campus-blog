<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNotifications, markAsRead, markAllAsRead } from '@/api/notification.js'
import { formatDate } from '@/utils/format.js'
import ScrollObserver from '@/components/ScrollObserver.vue'

const router = useRouter()
const notifications = ref([])
const loading = ref(false)

const fetchNotifications = async () => {
  loading.value = true
  try {
    const res = await getNotifications()
    notifications.value = res
  } catch (error) {
    ElMessage.error('获取通知失败')
  } finally {
    loading.value = false
  }
}

const handleClick = async (n) => {
  if (!n.isRead) {
    try { await markAsRead(n.id) } catch (error) {}
    n.isRead = true
  }
  if (n.articleId) {
    router.push(`/article/${n.articleId}`)
  } else if (n.actor?.id) {
    router.push(`/user/${n.actor.id}`)
  }
}

const handleMarkAllRead = async () => {
  try {
    await markAllAsRead()
    notifications.value.forEach(n => n.isRead = true)
    window.dispatchEvent(new CustomEvent('notifications-read'))
    ElMessage.success('全部标记为已读')
  } catch (error) {}
}

onMounted(() => {
  fetchNotifications()
})
</script>

<template>
  <div class="notifications-page">
    <div class="header">
      <h2>消息通知</h2>
      <el-button class="donate-btn" size="small" @click="handleMarkAllRead">全部已读</el-button>
    </div>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="3" animated />
    </div>

    <div v-else-if="notifications.length === 0" class="empty">
      <el-empty description="暂无通知" />
    </div>

    <div v-else class="notification-list">
      <ScrollObserver
        v-for="(n, index) in notifications"
        :key="n.id"
        :threshold="0.2"
      >
        <div
          :class="['notification-item', { unread: !n.isRead }]"
          :style="{ transitionDelay: `${(index % 5) * 0.03}s` }"
          @click="handleClick(n)"
        >
          <el-avatar :size="40" :src="n.actor?.avatar">
            {{ n.actor?.nickname?.charAt(0) || 'U' }}
          </el-avatar>
          <div class="notification-content">
            <p class="notification-text">
              <strong>{{ n.actor?.nickname }}</strong> {{ n.content }}
            </p>
            <span class="notification-time">{{ formatDate(n.createTime) }}</span>
          </div>
          <div v-if="!n.isRead" class="unread-dot"></div>
        </div>
      </ScrollObserver>
    </div>
  </div>
</template>

<style scoped>
.notifications-page {
  padding: 20px 40px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}
.loading {
  padding: 20px 0;
}
.empty {
  padding: 60px 0;
}
.notification-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.notification-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s, opacity 0.2s ease, transform 0.2s ease;
  opacity: 0;
  transform: scale(0.7);
}
.scroll-observer.scroll-visible .notification-item {
  opacity: 1;
  transform: scale(1);
}
.notification-item:hover {
  background: rgba(255, 255, 255, 1);
}
.notification-item.unread {
  background: rgba(64, 158, 255, 0.05);
}
.notification-content {
  flex: 1;
}
.notification-text {
  margin: 0 0 4px;
  color: #333;
}
.notification-time {
  font-size: 12px;
  color: #999;
}
.unread-dot {
  width: 8px;
  height: 8px;
  background: #f56c6c;
  border-radius: 50%;
}
</style>
