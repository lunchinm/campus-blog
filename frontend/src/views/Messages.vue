<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUser } from '@/utils/auth.js'
import { formatDate } from '@/utils/format.js'
import {
    sendMessage,
    getConversations,
    getMessages,
    markConversationAsRead,
    getUnreadMessageCount
} from '@/api/message.js'
import { getUserProfile } from '@/api/user.js'
import { getArticleDetail } from '@/api/article.js'
import ScrollObserver from '@/components/ScrollObserver.vue'

const route = useRoute()
const router = useRouter()
const currentUser = getUser()

const conversations = ref([])
const messages = ref([])
const activeConversationId = ref(null)
const activeConversation = ref(null)
const inputText = ref('')
const loadingConversations = ref(false)
const loadingMessages = ref(false)
const sending = ref(false)
const messagesContainer = ref(null)
const fileInput = ref(null)
const unreadCount = ref(0)
let messagePollTimer = null
const articleCache = ref({})
const showEmojiPicker = ref(false)
const emojis = ['😀','😂','🥰','😍','😎','😭','😡','👍','👎','🙏','🎉','❤️','🔥','👀','🤔','😅','😊','😘','🤗','😴']

const otherUser = computed(() => {
    if (!activeConversation.value) return null
    return activeConversation.value.userId === currentUser?.id
        ? activeConversation.value.otherUser
        : activeConversation.value.otherUser
})

const fetchConversations = async () => {
    loadingConversations.value = true
    try {
        const res = await getConversations()
        conversations.value = res || []
    } catch (error) {
        ElMessage.error('获取会话列表失败')
    } finally {
        loadingConversations.value = false
    }
}

const fetchMessages = async (conversationId) => {
    loadingMessages.value = true
    try {
        const res = await getMessages(conversationId)
        messages.value = res || []
        await loadArticlePreviews(messages.value)
        await nextTick(() => scrollToBottom())
    } catch (error) {
        ElMessage.error('获取消息失败')
    } finally {
        loadingMessages.value = false
    }
}

const selectConversation = async (conv) => {
    // 如果 conversationId 是临时字符串（如 user_123），说明会话尚未真正创建
    if (typeof conv.conversationId === 'string' && conv.conversationId.startsWith('user_')) {
        activeConversationId.value = conv.conversationId
        activeConversation.value = conv
        messages.value = []
        return
    }
    activeConversationId.value = conv.conversationId
    activeConversation.value = conv
    messages.value = []
    await fetchMessages(conv.conversationId)
    if (conv.unreadCount > 0) {
        try {
            await markConversationAsRead(conv.conversationId)
            conv.unreadCount = 0
            await fetchUnreadTotal()
            window.dispatchEvent(new CustomEvent('message-read'))
        } catch (error) {}
    }
}

const scrollToBottom = () => {
    const el = messagesContainer.value
    if (el) {
        el.scrollTop = el.scrollHeight
    }
}

const handleSendText = async () => {
    if (!inputText.value.trim() || !activeConversation.value) return
    const content = inputText.value.trim()
    const receiverId = activeConversation.value.otherUser?.id
    if (!receiverId) return
    sending.value = true
    try {
        const res = await sendMessage(receiverId, content, 'text')
        if (res.success) {
            inputText.value = ''
            // 如果是临时会话，发送成功后刷新会话列表以获取真实 conversationId
            if (typeof activeConversation.value.conversationId === 'string' && activeConversation.value.conversationId.startsWith('user_')) {
                await fetchConversations()
                const newConv = conversations.value.find(c => c.otherUser?.id === receiverId)
                if (newConv) {
                    activeConversation.value = newConv
                    activeConversationId.value = newConv.conversationId
                    await fetchMessages(newConv.conversationId)
                }
            } else {
                messages.value.push({
                    id: Date.now(),
                    senderId: currentUser?.id,
                    receiverId,
                    content,
                    type: 'text',
                    createTime: new Date().toISOString()
                })
                await nextTick(() => scrollToBottom())
                await fetchConversations()
            }
        } else {
            ElMessage.error(res.message || '发送失败')
        }
    } catch (error) {
        ElMessage.error('发送失败')
    } finally {
        sending.value = false
    }
}

const handleSendImage = async (file) => {
    if (!activeConversation.value) return
    const receiverId = activeConversation.value.otherUser?.id
    if (!receiverId) return
    const formData = new FormData()
    formData.append('file', file)
    try {
        const uploadRes = await fetch('/api/upload', {
            method: 'POST',
            headers: {
                Authorization: `Bearer ${localStorage.getItem('blog_token')}`
            },
            body: formData
        }).then(r => r.json())
        if (!uploadRes.success && !uploadRes.url) {
            ElMessage.error(uploadRes.message || '上传失败')
            return
        }
        const imageUrl = uploadRes.url || uploadRes.data?.url
        const res = await sendMessage(receiverId, imageUrl, 'image')
        if (res.success) {
            // 如果是临时会话，发送成功后刷新会话列表以获取真实 conversationId
            if (typeof activeConversation.value.conversationId === 'string' && activeConversation.value.conversationId.startsWith('user_')) {
                await fetchConversations()
                const newConv = conversations.value.find(c => c.otherUser?.id === receiverId)
                if (newConv) {
                    activeConversation.value = newConv
                    activeConversationId.value = newConv.conversationId
                    await fetchMessages(newConv.conversationId)
                }
            } else {
                messages.value.push({
                    id: Date.now(),
                    senderId: currentUser?.id,
                    receiverId,
                    content: imageUrl,
                    type: 'image',
                    createTime: new Date().toISOString()
                })
                await nextTick(() => scrollToBottom())
                await fetchConversations()
            }
        } else {
            ElMessage.error(res.message || '发送失败')
        }
    } catch (error) {
        ElMessage.error('图片发送失败')
    }
}

const onFileChange = (e) => {
    const file = e.target.files[0]
    if (!file) return
    if (!file.type.startsWith('image/')) {
        ElMessage.warning('请选择图片文件')
        return
    }
    handleSendImage(file)
    e.target.value = ''
}

const triggerImageUpload = () => {
    fileInput.value?.click()
}

const insertEmoji = (emoji) => {
    inputText.value += emoji
    showEmojiPicker.value = false
}

const isSelf = (msg) => {
    return msg.senderId === currentUser?.id
}

const fetchUnreadTotal = async () => {
    try {
        const res = await getUnreadMessageCount()
        unreadCount.value = res.count || 0
    } catch (error) {}
}

const initFromQuery = async () => {
    const userId = route.query.userId
    if (!userId) return
    const existing = conversations.value.find(c => String(c.otherUser?.id) === String(userId))
    if (existing) {
        await selectConversation(existing)
        return
    }
    try {
        const profileRes = await getUserProfile(userId)
        if (!profileRes.success) {
            ElMessage.error(profileRes.message || '用户不存在')
            return
        }
        const conv = {
            conversationId: `user_${userId}`,
            otherUser: profileRes.profile,
            lastMessage: '',
            lastMessageTime: null,
            unreadCount: 0
        }
        conversations.value.unshift(conv)
        await selectConversation(conv)
    } catch (error) {
        ElMessage.error('获取用户信息失败')
    }
}

const sendArticleLink = async (articleId) => {
    if (!activeConversation.value) return
    const receiverId = activeConversation.value.otherUser?.id
    if (!receiverId) return
    sending.value = true
    try {
        const res = await sendMessage(receiverId, `article:${articleId}`, 'article', String(articleId))
        if (res.success) {
            // 如果是临时会话，发送成功后刷新会话列表以获取真实 conversationId
            if (typeof activeConversation.value.conversationId === 'string' && activeConversation.value.conversationId.startsWith('user_')) {
                await fetchConversations()
                const newConv = conversations.value.find(c => c.otherUser?.id === receiverId)
                if (newConv) {
                    activeConversation.value = newConv
                    activeConversationId.value = newConv.conversationId
                    await fetchMessages(newConv.conversationId)
                }
            } else {
                messages.value.push({
                    id: Date.now(),
                    senderId: currentUser?.id,
                    receiverId,
                    content: `article:${articleId}`,
                    type: 'article',
                    extraData: String(articleId),
                    createTime: new Date().toISOString()
                })
                await nextTick(() => scrollToBottom())
                await fetchConversations()
            }
        } else {
            ElMessage.error(res.message || '发送失败')
        }
    } catch (error) {
        ElMessage.error('发送文章链接失败')
    } finally {
        sending.value = false
    }
}

const pollMessages = async () => {
    const convId = activeConversation.value?.conversationId
    if (typeof convId !== 'number') return
    try {
        const res = await getMessages(convId)
        if (res && Array.isArray(res)) {
            const hasNew = res.length > messages.value.length
            if (hasNew) {
                messages.value = res
                await loadArticlePreviews(res)
                await nextTick(() => scrollToBottom())
            }
        }
    } catch (error) {}
}

const loadArticlePreviews = async (msgs) => {
    const articleMsgs = msgs.filter(m => m.type === 'article' && m.extraData)
    for (const msg of articleMsgs) {
        const id = msg.extraData
        if (!articleCache.value[id]) {
            try {
                const res = await getArticleDetail(id)
                if (res) {
                    articleCache.value[id] = res
                }
            } catch (e) {}
        }
    }
}

const pollConversations = async () => {
    try {
        const res = await getConversations()
        if (res && Array.isArray(res)) {
            conversations.value = res
            // 保持当前活跃会话的引用最新
            const activeId = activeConversation.value?.conversationId
            if (typeof activeId === 'number') {
                const updated = res.find(c => c.conversationId === activeId)
                if (updated) {
                    activeConversation.value = updated
                }
            }
        }
    } catch (error) {}
}

onMounted(() => {
    fetchConversations().then(() => {
        initFromQuery()
    })
    fetchUnreadTotal()
    messagePollTimer = setInterval(() => {
        pollMessages()
        pollConversations()
    }, 5000)
})

onUnmounted(() => {
    if (messagePollTimer) clearInterval(messagePollTimer)
})

watch(() => route.query.userId, () => {
    initFromQuery()
})
</script>

<template>
  <div class="messages-page">
    <div class="chat-layout">
      <!-- 左侧会话列表 -->
      <div class="conversation-list">
        <div class="list-header">
          <h3>私信</h3>
        </div>
        <div v-loading="loadingConversations" class="list-body">
          <ScrollObserver
            v-for="(conv, index) in conversations"
            :key="conv.conversationId"
            :threshold="0.2"
          >
            <div
              :class="['conversation-item', { active: activeConversationId === conv.conversationId }]"
              :style="{ transitionDelay: `${(index % 5) * 0.03}s` }"
              @click="selectConversation(conv)"
            >
              <el-avatar :size="44" :src="conv.otherUser?.avatar">
                {{ conv.otherUser?.nickname?.charAt(0) || conv.otherUser?.username?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="conv-info">
                <span class="conv-name">{{ conv.otherUser?.nickname || conv.otherUser?.username }}</span>
                <span v-if="conv.lastMessageTime" class="conv-time">{{ formatDate(conv.lastMessageTime) }}</span>
                <span class="conv-preview">{{ conv.lastMessage || '暂无消息' }}</span>
                <el-badge v-if="conv.unreadCount > 0" :value="conv.unreadCount" class="conv-unread" />
              </div>
            </div>
          </ScrollObserver>
          <el-empty v-if="!loadingConversations && conversations.length === 0" description="暂无会话" />
        </div>
      </div>

      <!-- 右侧聊天区域 -->
      <div class="chat-area">
        <div class="chat-header">
          <span class="chat-title">{{ activeConversation ? (otherUser?.nickname || otherUser?.username) : '请选择会话' }}</span>
        </div>

        <div ref="messagesContainer" v-loading="loadingMessages" class="messages-list">
          <template v-if="activeConversation">
            <div
              v-for="msg in messages"
              :key="msg.id"
              :class="['message-row', isSelf(msg) ? 'self' : 'other']"
            >
              <div class="message-wrapper">
                <el-avatar :size="36" :src="isSelf(msg) ? currentUser?.avatar : otherUser?.avatar">
                  {{ isSelf(msg)
                    ? (currentUser?.nickname?.charAt(0) || currentUser?.username?.charAt(0) || 'U')
                    : (otherUser?.nickname?.charAt(0) || otherUser?.username?.charAt(0) || 'U') }}
                </el-avatar>
                <div class="message-meta-column">
                  <span class="message-time-above">{{ formatDate(msg.createTime) }}</span>
                  <div class="message-bubble">
                    <template v-if="msg.type === 'image'">
                      <img :src="msg.content" class="message-image" @click="() => {}" />
                    </template>
                    <template v-else-if="msg.type === 'article'">
                      <div v-if="articleCache[msg.extraData]" class="article-card" @click="router.push(`/article/${msg.extraData}`)">
                        <div class="article-card-title">{{ articleCache[msg.extraData].title }}</div>
                        <div class="article-card-summary">{{ articleCache[msg.extraData].summary }}</div>
                        <div class="article-card-meta">
                          <span class="article-card-author">{{ articleCache[msg.extraData].authorNickname || articleCache[msg.extraData].authorName }}</span>
                          <span class="article-card-time">{{ formatDate(articleCache[msg.extraData].createTime) }}</span>
                        </div>
                      </div>
                      <div v-else class="article-link" @click="router.push(`/article/${msg.extraData}`)">
                        <span class="article-link-icon">📄</span>
                        <span class="article-link-text">查看文章</span>
                      </div>
                    </template>
                    <template v-else>
                      <span class="message-text">{{ msg.content }}</span>
                    </template>
                  </div>
                </div>
              </div>
            </div>
          </template>
          <div v-else class="empty-chat">
            <el-empty description="选择左侧会话开始聊天" />
          </div>
        </div>

        <div v-if="activeConversation" class="chat-input-area">
          <input ref="fileInput" type="file" accept="image/*" style="display: none" @change="onFileChange" />
          <button class="attach-btn" type="button" @click="triggerImageUpload" title="发送图片">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="12" y1="5" x2="12" y2="19"></line>
              <line x1="5" y1="12" x2="19" y2="12"></line>
            </svg>
          </button>
          <button class="emoji-btn" type="button" @click="showEmojiPicker = !showEmojiPicker" title="表情">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"></circle>
              <path d="M8 14s1.5 2 4 2 4-2 4-2"></path>
              <line x1="9" y1="9" x2="9.01" y2="9"></line>
              <line x1="15" y1="9" x2="15.01" y2="9"></line>
            </svg>
          </button>
          <div v-if="showEmojiPicker" class="emoji-picker">
            <span v-for="emoji in emojis" :key="emoji" class="emoji-item" @click="insertEmoji(emoji)">{{ emoji }}</span>
          </div>
          <el-input
            v-model="inputText"
            class="chat-input"
            placeholder="输入消息..."
            @keyup.enter="handleSendText"
          />
          <button class="send-btn" type="button" :disabled="sending || !inputText.trim()" @click="handleSendText">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="22" y1="2" x2="11" y2="13"></line>
              <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.messages-page {
  padding: 20px 0;
  margin: 0 auto;
  width: 100%;
}
.chat-layout {
  display: flex;
  height: calc(100vh - 140px);
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid rgba(0, 0, 0, 0.06);
  width: 100%;
}
.conversation-list {
  width: 300px;
  max-width: 300px;
  flex-shrink: 0;
  border-right: 1px solid rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
}
.chat-area {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
.list-header {
  padding: 14px 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  background: rgba(255, 255, 255, 0.6);
  height: 52px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
}
.list-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #08060d;
}
.list-body {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}
.conversation-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  border-radius: 10px;
  cursor: pointer;
  transition: background-color 0.2s, opacity 0.2s ease, transform 0.2s ease;
  opacity: 0;
  transform: scale(0.7);
}
.scroll-observer.scroll-visible .conversation-item {
  opacity: 1;
  transform: scale(1);
}
.conversation-item:hover {
  background: rgba(0, 0, 0, 0.04);
}
.conversation-item.active {
  background: rgba(64, 158, 255, 0.1);
}
.conv-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 0;
  line-height: 1;
}
.conv-name {
  font-size: 14px;
  font-weight: 500;
  color: #08060d;
  line-height: 1;
}
.conv-time {
  font-size: 10px;
  color: #9ca3af;
  line-height: 1;
  margin-top: 1px;
}
.conv-preview {
  font-size: 12px;
  color: #9ca3af;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1;
  margin-top: 3px;
}
.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.chat-header {
  padding: 14px 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  background: rgba(255, 255, 255, 0.6);
  height: 52px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
}
.chat-title {
  font-size: 16px;
  font-weight: 600;
  color: #08060d;
}
.messages-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.empty-chat {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
.message-row {
  display: flex;
  flex-direction: column;
  gap: 0;
}
.message-row.self {
  align-items: flex-end;
}
.message-row.other {
  align-items: flex-start;
}
.message-wrapper {
  display: flex;
  align-items: flex-start;
  gap: 6px;
}
.message-row.self .message-wrapper {
  flex-direction: row-reverse;
}
.message-meta-column {
  display: flex;
  flex-direction: column;
  gap: 2px;
  align-items: flex-start;
  max-width: 60%;
  min-width: 0;
}
.message-meta-column:has(.article-card) {
  max-width: 85%;
}
.message-row.self .message-meta-column {
  align-items: flex-end;
}
.message-time-above {
  font-size: 11px;
  color: #9ca3af;
  line-height: 1;
  white-space: nowrap;
}
.message-bubble {
  padding: 4px 8px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(0, 0, 0, 0.06);
  display: inline-flex;
  flex-direction: column;
  align-self: flex-start;
  min-width: 0;
}
.message-bubble:has(.article-card) {
  display: block;
  width: 100%;
  padding: 6px;
}
.message-row.self .message-bubble {
  background: linear-gradient(135deg, #ff9a56, #ff6b6b);
  color: #fff;
  border: none;
  align-self: flex-end;
}
.message-row.self .message-bubble:has(.article-card) {
  background: rgba(255, 255, 255, 0.95);
  color: inherit;
}
.message-text {
  font-size: 14px;
  line-height: 1.3;
  word-break: break-word;
}
.message-image {
  max-width: 200px;
  max-height: 200px;
  border-radius: 8px;
  cursor: pointer;
  object-fit: cover;
}
.article-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}
.article-link:hover {
  background: rgba(255, 255, 255, 0.4);
}
.article-link-icon {
  font-size: 16px;
}
.article-link-text {
  font-size: 14px;
}
.article-card {
  display: block;
  width: 100%;
  min-width: 260px;
  max-width: 100%;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 10px;
  padding: 14px 16px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border: 1px solid rgba(0, 0, 0, 0.08);
  box-sizing: border-box;
}
.article-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}
.article-card-title {
  font-size: 16px;
  font-weight: 600;
  color: #08060d;
  margin: 0 0 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.article-card-summary {
  font-size: 13px;
  color: #6b6375;
  line-height: 1.5;
  margin: 0 0 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.article-card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #9ca3af;
}
.article-card-author {
  font-weight: 500;
  color: #666;
}
.chat-input-area {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  background: rgba(255, 255, 255, 0.6);
  position: relative;
}
.attach-btn, .emoji-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(8px);
  color: #666;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s, color 0.2s;
}
.attach-btn:hover, .emoji-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  color: #409eff;
}
.emoji-picker {
  position: absolute;
  bottom: 56px;
  left: 60px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 12px;
  padding: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  width: 200px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  z-index: 100;
}
.emoji-item {
  font-size: 20px;
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  transition: background 0.2s;
  user-select: none;
}
.emoji-item:hover {
  background: rgba(0, 0, 0, 0.06);
}
.chat-input {
  flex: 1;
}
.chat-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(8px);
  box-shadow: none;
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 20px;
  padding: 0 14px;
}
.chat-input :deep(.el-input__inner) {
  color: #08060d;
}
.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #ff9a56, #ff6b6b);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s, box-shadow 0.2s;
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.35);
}
.send-btn:hover {
  transform: scale(1.08);
  box-shadow: 0 6px 16px rgba(255, 107, 107, 0.45);
}
.send-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}
</style>
