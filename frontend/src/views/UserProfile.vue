<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUserProfile, updateUser } from '@/api/user.js'
import { followUser, unfollowUser } from '@/api/follow.js'
import { setUser, getUser } from '@/utils/auth.js'
import { formatDate } from '@/utils/format.js'
import StarFollowButton from '@/components/StarFollowButton.vue'

const route = useRoute()
const router = useRouter()
const isViewMode = computed(() => route.query.view === '1')
const profile = ref(null)
const loading = ref(false)
const saving = ref(false)
const editingProfile = ref(false)
const editingPassword = ref(false)

const profileForm = ref({ username: '', nickname: '' })
const passwordForm = ref({ password: '', confirmPassword: '' })

const emitRefresh = () => {
  window.dispatchEvent(new CustomEvent('avatar-changed'))
}

const fetchProfile = async () => {
  const userId = route.params.id
  if (!userId) return
  loading.value = true
  try {
    const res = await getUserProfile(userId)
    if (res.success) {
      profile.value = res.profile
      profileForm.value = { username: res.profile.username, nickname: res.profile.nickname }
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

const handleToggleFollow = async () => {
  try {
    let res
    if (profile.value.isFollowing) {
      res = await unfollowUser(profile.value.id)
    } else {
      res = await followUser(profile.value.id)
    }
    if (res.success) {
      profile.value.isFollowing = !profile.value.isFollowing
      profile.value.followerCount += profile.value.isFollowing ? 1 : -1
      ElMessage.success(profile.value.isFollowing ? '关注成功' : '取消关注成功')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const goSettingsAvatar = () => {
  router.push('/settings?open=avatar')
}

const saveProfile = async () => {
  saving.value = true
  try {
    const res = await updateUser({
      nickname: profileForm.value.nickname,
      username: profileForm.value.username
    })
    if (res.success) {
      profile.value.nickname = profileForm.value.nickname
      profile.value.username = profileForm.value.username
      const stored = getUser() || {}
      setUser({ ...stored, nickname: profileForm.value.nickname, username: profileForm.value.username })
      ElMessage.success('修改成功')
      editingProfile.value = false
      emitRefresh()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('修改失败')
  } finally {
    saving.value = false
  }
}

const cancelEditProfile = () => {
  editingProfile.value = false
  profileForm.value = { username: profile.value.username, nickname: profile.value.nickname }
}

const savePassword = async () => {
  if (!passwordForm.value.password) { ElMessage.warning('请输入新密码'); return }
  if (passwordForm.value.password !== passwordForm.value.confirmPassword) { ElMessage.warning('两次密码输入不一致'); return }
  saving.value = true
  try {
    const res = await updateUser({ password: passwordForm.value.password })
    if (res.success) {
      ElMessage.success('密码修改成功')
      passwordForm.value = { password: '', confirmPassword: '' }
      editingPassword.value = false
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('修改失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchProfile()
})

watch(() => route.params.id, () => {
  fetchProfile()
})
</script>

<template>
  <div v-loading="loading" class="user-profile">
    <div v-if="profile" class="profile-card">
      <div class="profile-header">
        <div
          class="avatar-area"
          :class="{ clickable: profile.isSelf && !isViewMode }"
          @click="profile.isSelf && !isViewMode && goSettingsAvatar()"
        >
          <el-avatar :size="80" :src="profile.avatar">
            {{ profile.nickname?.charAt(0) || profile.username?.charAt(0) || 'U' }}
          </el-avatar>
          <div v-if="profile.isSelf && !isViewMode" class="avatar-overlay">更换头像</div>
        </div>
        <div class="profile-info">
          <h2>{{ profile.nickname || profile.username }}</h2>
          <p class="username">@{{ profile.username }}</p>
          <p v-if="profile.bio" class="bio">{{ profile.bio }}</p>
          <p class="join-time">加入于 {{ formatDate(profile.createTime) }}</p>
        </div>
        <div v-if="!profile.isSelf" class="profile-actions">
          <StarFollowButton
            :following="profile.isFollowing"
            @toggle="handleToggleFollow"
          />
        </div>
      </div>
      <div class="profile-stats">
        <div class="stat-item">
          <span class="stat-num">{{ profile.articleCount }}</span>
          <span class="stat-label">文章</span>
        </div>
        <div class="stat-item">
          <span class="stat-num">{{ profile.likeCount }}</span>
          <span class="stat-label">获赞</span>
        </div>
        <div class="stat-item">
          <span class="stat-num">{{ profile.followingCount }}</span>
          <span class="stat-label">关注</span>
        </div>
        <div class="stat-item">
          <span class="stat-num">{{ profile.followerCount }}</span>
          <span class="stat-label">粉丝</span>
        </div>
      </div>
      <div v-if="!profile.isSelf" class="profile-msg-area">
        <button class="msg-btn" type="button" @click="router.push(`/messages?userId=${profile.id}`)">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="22" y1="2" x2="11" y2="13"></line>
            <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
          </svg>
          <span>私信</span>
        </button>
      </div>
      <div v-if="profile.isSelf && !isViewMode" class="profile-settings-area">
        <div v-if="editingPassword" class="edit-section">
          <el-input v-model="passwordForm.password" type="password" autocomplete="new-password" placeholder="新密码" show-password />
          <el-input v-model="passwordForm.confirmPassword" type="password" autocomplete="new-password" placeholder="确认新密码" show-password />
          <div class="edit-actions">
            <el-button class="donate-btn" size="small" :loading="saving" @click="savePassword">保存</el-button>
            <el-button class="neu-btn" size="small" @click="editingPassword = false; passwordForm = { password: '', confirmPassword: '' }">取消</el-button>
          </div>
        </div>

        <div v-if="editingProfile" class="edit-section">
          <el-input v-model="profileForm.username" autocomplete="username" spellcheck="false" placeholder="用户名" />
          <el-input v-model="profileForm.nickname" autocomplete="nickname" placeholder="昵称" />
          <div class="edit-actions">
            <el-button class="donate-btn" size="small" :loading="saving" @click="saveProfile">保存</el-button>
            <el-button class="neu-btn" size="small" @click="cancelEditProfile">取消</el-button>
          </div>
        </div>

        <div v-if="!editingPassword && !editingProfile" class="bottom-actions">
          <el-button class="neu-btn" @click="editingPassword = true">修改密码</el-button>
          <el-button class="donate-btn" @click="editingProfile = true">修改资料</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.user-profile {
  padding: 20px 40px;
  max-width: 800px;
  margin: 0 auto;
}
.profile-card {
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 32px;
  border: 1px solid rgba(0, 0, 0, 0.06);
}
.profile-header {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  margin-bottom: 24px;
}
.avatar-area {
  position: relative;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}
.avatar-area.clickable {
  cursor: pointer;
}
.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  border-radius: 50%;
  opacity: 0;
  transition: opacity 0.2s;
}
.avatar-area:hover .avatar-overlay {
  opacity: 1;
}
.profile-info {
  flex: 1;
}
.profile-info h2 {
  margin: 0 0 4px;
  font-size: 24px;
}
.profile-info .username {
  color: #9ca3af;
  font-size: 14px;
  margin: 0 0 8px;
}
.profile-info .bio {
  color: #4b5563;
  margin: 0 0 8px;
  line-height: 1.6;
}
.profile-info .join-time {
  color: #9ca3af;
  font-size: 13px;
  margin: 0;
}
.profile-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 10px;
}
.msg-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 20px;
  border: none;
  background: linear-gradient(135deg, #ff9a56, #ff6b6b);
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.35);
}
.msg-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(255, 107, 107, 0.45);
}
.profile-msg-area {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
.profile-msg-area .msg-btn {
  width: 100%;
  justify-content: center;
  height: 44px;
  border-radius: 12px;
}
.profile-stats {
  display: flex;
  justify-content: center;
  gap: 40px;
  padding-top: 20px;
  border-top: 1px solid #e5e4e7;
}
.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
.stat-num {
  font-size: 20px;
  font-weight: 600;
  color: #08060d;
}
.stat-label {
  font-size: 13px;
  color: #9ca3af;
}
.profile-settings-area {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e5e4e7;
}
.edit-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.edit-actions {
  display: flex;
  gap: 8px;
}
.bottom-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>
