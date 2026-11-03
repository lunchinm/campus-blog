<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { getUserInfo, updateUser, uploadAvatar, getPresetAvatars } from '@/api/user.js'
import { setUser, getUser } from '@/utils/auth.js'

const route = useRoute()

const presets = ref([])
const avatarDialogVisible = ref(false)
const editingProfile = ref(false)
const editingPassword = ref(false)

const userInfo = ref({ username: '', nickname: '', avatar: '' })
const profileForm = ref({ username: '', nickname: '' })
const passwordForm = ref({ password: '', confirmPassword: '' })
const avatarUrl = ref('')
const saving = ref(false)

const cropVisible = ref(false)
const cropImageSrc = ref('')
const cropCanvasRef = ref(null)
const previewCanvasRef = ref(null)
let rawImage = null
let imgScale = 1
let panX = 0
let panY = 0
let dragging = false
let lastMouseX = 0
let lastMouseY = 0
const CANVAS_SIZE = 300
const CROP_RADIUS = 100
const PREVIEW_SIZE = 80

const emitRefresh = () => {
  window.dispatchEvent(new CustomEvent('avatar-changed'))
}

const fetchUserInfo = async () => {
  try {
    const res = await getUserInfo()
    if (res.success) {
      userInfo.value = { username: res.username || '', nickname: res.nickname || '', avatar: res.avatar || '' }
      profileForm.value = { username: res.username || '', nickname: res.nickname || '' }
      avatarUrl.value = res.avatar || ''
    }
  } catch (error) {}
}

const fetchPresets = async () => {
  try {
    const res = await getPresetAvatars()
    if (res.success) presets.value = res.avatars
  } catch (error) {}
}

const handleAvatarSelect = (url) => {
  avatarUrl.value = url
  avatarDialogVisible.value = false
  saveAvatar(url)
}

const handleFileChange = (uploadFile) => {
  const file = uploadFile.raw
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  const reader = new FileReader()
  reader.onload = (e) => {
    cropImageSrc.value = e.target.result
    cropVisible.value = true
    nextTick(() => initCrop(e.target.result))
  }
  reader.readAsDataURL(file)
}

const initCrop = (src) => {
  rawImage = new Image()
  rawImage.onload = () => {
    const fitScaleW = CANVAS_SIZE / rawImage.width
    const fitScaleH = CANVAS_SIZE / rawImage.height
    imgScale = Math.min(fitScaleW, fitScaleH)
    panX = 0
    panY = 0
    render()
  }
  rawImage.src = src
}

const render = () => {
  if (!rawImage) return
  drawMainCanvas()
  drawPreviewCanvas()
}

const drawMainCanvas = () => {
  const canvas = cropCanvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  canvas.width = CANVAS_SIZE
  canvas.height = CANVAS_SIZE
  ctx.clearRect(0, 0, CANVAS_SIZE, CANVAS_SIZE)

  ctx.save()
  ctx.translate(CANVAS_SIZE / 2 + panX, CANVAS_SIZE / 2 + panY)
  ctx.scale(imgScale, imgScale)
  ctx.drawImage(rawImage, -rawImage.width / 2, -rawImage.height / 2)
  ctx.restore()

  ctx.strokeStyle = 'rgba(64,158,255,0.8)'
  ctx.lineWidth = 2
  ctx.setLineDash([6, 4])
  ctx.beginPath()
  ctx.arc(CANVAS_SIZE / 2, CANVAS_SIZE / 2, CROP_RADIUS, 0, Math.PI * 2)
  ctx.stroke()
  ctx.setLineDash([])

  ctx.fillStyle = 'rgba(0,0,0,0.4)'
  ctx.fillRect(0, 0, CANVAS_SIZE, CANVAS_SIZE)
  ctx.globalCompositeOperation = 'destination-out'
  ctx.beginPath()
  ctx.arc(CANVAS_SIZE / 2, CANVAS_SIZE / 2, CROP_RADIUS, 0, Math.PI * 2)
  ctx.fill()
  ctx.globalCompositeOperation = 'source-over'
}

const drawPreviewCanvas = () => {
  const pcanvas = previewCanvasRef.value
  if (!pcanvas) return
  const pctx = pcanvas.getContext('2d')
  pcanvas.width = PREVIEW_SIZE
  pcanvas.height = PREVIEW_SIZE
  pctx.clearRect(0, 0, PREVIEW_SIZE, PREVIEW_SIZE)

  const tmpCanvas = document.createElement('canvas')
  const tmpSize = CROP_RADIUS * 2
  tmpCanvas.width = tmpSize
  tmpCanvas.height = tmpSize
  const tctx = tmpCanvas.getContext('2d')

  tctx.save()
  tctx.translate(CROP_RADIUS + panX, CROP_RADIUS + panY)
  tctx.scale(imgScale, imgScale)
  tctx.drawImage(rawImage, -rawImage.width / 2, -rawImage.height / 2)
  tctx.restore()

  pctx.save()
  pctx.beginPath()
  pctx.arc(PREVIEW_SIZE / 2, PREVIEW_SIZE / 2, PREVIEW_SIZE / 2, 0, Math.PI * 2)
  pctx.clip()
  pctx.drawImage(tmpCanvas, 0, 0, tmpSize, tmpSize, 0, 0, PREVIEW_SIZE, PREVIEW_SIZE)
  pctx.restore()

  pctx.strokeStyle = '#e5e4e7'
  pctx.lineWidth = 1
  pctx.beginPath()
  pctx.arc(PREVIEW_SIZE / 2, PREVIEW_SIZE / 2, PREVIEW_SIZE / 2, 0, Math.PI * 2)
  pctx.stroke()
}

const onCropMouseDown = (e) => {
  dragging = true
  lastMouseX = e.clientX
  lastMouseY = e.clientY
}

const onCropMouseMove = (e) => {
  if (!dragging) return
  const dx = e.clientX - lastMouseX
  const dy = e.clientY - lastMouseY
  panX += dx
  panY += dy
  lastMouseX = e.clientX
  lastMouseY = e.clientY
  render()
}

const onCropMouseUp = () => { dragging = false }

const onWheelZoom = (e) => {
  e.preventDefault()
  const factor = e.deltaY > 0 ? 0.92 : 1.08
  const newScale = imgScale * factor
  const maxScale = 10
  const fitScaleW = CANVAS_SIZE / rawImage.width
  const fitScaleH = CANVAS_SIZE / rawImage.height
  const minScale = Math.min(fitScaleW, fitScaleH) * 0.5
  if (newScale < minScale || newScale > maxScale) return
  imgScale = newScale
  render()
}

const confirmCrop = async () => {
  if (!rawImage) return
  const outSize = 200
  const canvas = document.createElement('canvas')
  canvas.width = outSize
  canvas.height = outSize
  const ctx = canvas.getContext('2d')

  ctx.save()
  ctx.beginPath()
  ctx.arc(outSize / 2, outSize / 2, outSize / 2, 0, Math.PI * 2)
  ctx.clip()

  ctx.translate(CROP_RADIUS + panX, CROP_RADIUS + panY)
  ctx.scale(imgScale, imgScale)
  ctx.drawImage(rawImage, -rawImage.width / 2, -rawImage.height / 2)
  ctx.restore()

  canvas.toBlob(async (blob) => {
    if (!blob) return
    const file = new File([blob], 'avatar.png', { type: 'image/png' })
    try {
      const res = await uploadAvatar(file)
      if (res.success) {
        const finalUrl = res.url
        const userRes = await updateUser({ avatar: finalUrl })
        if (!userRes.success) {
          ElMessage.error(userRes.message || '保存头像失败')
          return
        }
        avatarUrl.value = finalUrl
        userInfo.value.avatar = finalUrl
        const stored = getUser() || {}
        setUser({ ...stored, avatar: finalUrl })
        emitRefresh()
        ElMessage.success('头像上传成功')
        cropVisible.value = false
        avatarDialogVisible.value = false
      } else {
        ElMessage.error(res.message)
      }
    } catch (error) {
      ElMessage.error('上传失败')
    }
  }, 'image/png', 0.9)
}

const cancelCrop = () => { cropVisible.value = false }

const saveAvatar = async (url) => {
  try {
    const res = await updateUser({ avatar: url })
    if (res.success) {
      userInfo.value.avatar = url
      const stored = JSON.parse(localStorage.getItem('blog_user')) || {}
      setUser({ ...stored, avatar: url })
      emitRefresh()
    }
  } catch (error) {}
}

const saveProfile = async () => {
  saving.value = true
  try {
    const res = await updateUser({
      nickname: profileForm.value.nickname,
      username: profileForm.value.username
    })
    if (res.success) {
      userInfo.value.nickname = profileForm.value.nickname
      userInfo.value.username = profileForm.value.username
      const stored = JSON.parse(localStorage.getItem('blog_user')) || {}
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
    } else { ElMessage.error(res.message) }
  } catch (error) { ElMessage.error('修改失败') } finally { saving.value = false }
}

onMounted(() => {
  fetchUserInfo()
  fetchPresets()
  if (route.query.open === 'avatar') {
    avatarDialogVisible.value = true
  }
})
</script>

<template>
  <div class="user-settings">
    <h2>个人设置</h2>

    <div class="profile-card">
      <div class="avatar-section">
        <button class="avatar-wrapper" type="button" @click="avatarDialogVisible = true" aria-label="更换头像">
          <el-avatar :size="80" :src="avatarUrl">{{ userInfo.nickname?.charAt(0) || userInfo.username?.charAt(0) || 'U' }}</el-avatar>
          <div class="avatar-overlay">更换头像</div>
        </button>
      </div>

      <div class="info-rows">
        <div class="info-row"><span class="info-label">用户：</span><span class="info-value">{{ userInfo.username }}</span></div>
        <div class="info-row"><span class="info-label">昵称：</span><span class="info-value">{{ userInfo.nickname }}</span></div>
      </div>

      <div v-if="editingPassword" class="edit-section">
        <el-input v-model="passwordForm.password" name="password" type="password" autocomplete="new-password" placeholder="新密码" show-password />
        <el-input v-model="passwordForm.confirmPassword" name="confirmPassword" type="password" autocomplete="new-password" placeholder="确认新密码" show-password />
        <div class="edit-actions">
          <el-button class="donate-btn" size="small" :loading="saving" @click="savePassword">保存</el-button>
          <el-button class="neu-btn" size="small" @click="editingPassword = false">取消</el-button>
        </div>
      </div>

      <div v-if="editingProfile" class="edit-section">
        <el-input v-model="profileForm.username" name="username" autocomplete="username" spellcheck="false" placeholder="用户名" />
        <el-input v-model="profileForm.nickname" name="nickname" autocomplete="nickname" placeholder="昵称" />
        <div class="edit-actions">
          <el-button class="donate-btn" size="small" :loading="saving" @click="saveProfile">保存</el-button>
          <el-button class="neu-btn" size="small" @click="editingProfile = false; profileForm.value = { username: userInfo.username, nickname: userInfo.nickname }">取消</el-button>
        </div>
      </div>

      <div v-if="!editingPassword && !editingProfile" class="profile-actions">
        <el-button class="neu-btn" @click="editingPassword = true">修改密码</el-button>
        <el-button class="donate-btn" @click="editingProfile = true">修改资料</el-button>
      </div>
    </div>

    <el-dialog v-model="avatarDialogVisible" width="520px" :show-close="false" class="avatar-dialog">
      <template #header>
        <div class="custom-header">
          <span></span>
          <span class="dialog-title">选择头像</span>
          <el-button link @click="avatarDialogVisible = false" class="close-btn">×</el-button>
        </div>
      </template>
      <div class="avatar-picker">
        <div class="picker-section">
          <h4>预设头像</h4>
          <div class="preset-grid">
            <div v-for="(url, idx) in presets" :key="idx"
              class="preset-item" :class="{ active: avatarUrl === url }"
              @click="handleAvatarSelect(url)">
              <el-avatar :size="60" :src="url" />
            </div>
          </div>
        </div>
        <div class="picker-section">
          <h4>本地上传</h4>
          <el-upload accept="image/*" :show-file-list="false" :auto-upload="false" :on-change="handleFileChange">
            <el-button class="donate-btn" type="primary">选择图片</el-button>
          </el-upload>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="cropVisible" width="480px" :show-close="false" append-to-body class="crop-dialog">
      <template #header>
        <div class="custom-header">
          <span></span>
          <span class="dialog-title">裁剪头像</span>
          <el-button link @click="cropVisible = false" class="close-btn">×</el-button>
        </div>
      </template>
      <div class="crop-container">
        <div class="crop-area"
          @mousedown="onCropMouseDown"
          @mousemove="onCropMouseMove"
          @mouseup="onCropMouseUp"
          @mouseleave="onCropMouseUp"
          @wheel.prevent="onWheelZoom">
          <canvas ref="cropCanvasRef" class="crop-canvas"></canvas>
          <p class="crop-hint">拖拽移动 · 滚轮缩放 · 蓝圈内为最终效果</p>
        </div>
        <div class="crop-preview-area">
          <p class="preview-label">预览</p>
          <canvas ref="previewCanvasRef" class="preview-canvas"></canvas>
        </div>
      </div>
      <template #footer>
        <el-button class="neu-btn" @click="cancelCrop">取消</el-button>
        <el-button class="donate-btn" type="primary" @click="confirmCrop">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.user-settings { padding: 20px 40px; max-width: 500px; margin: 0 auto; }
h2 { margin-bottom: 30px; }
.profile-card { display: flex; flex-direction: column; align-items: center; gap: 24px; }
.avatar-section { display: flex; justify-content: center; }
.avatar-wrapper { position: relative; cursor: pointer; border-radius: 50%; overflow: hidden; background: none; border: none; padding: 0; }
.avatar-overlay { position: absolute; inset: 0; background: rgba(0,0,0,0.5); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 14px; opacity: 0; transition: opacity .2s; }
.avatar-wrapper:hover .avatar-overlay { opacity: 1; }
.info-rows { width: 100%; display: flex; flex-direction: column; gap: 14px; }
.info-row { display: flex; align-items: center; font-size: 15px; }
.info-label { color: #9ca3af; width: 70px; text-align: right; flex-shrink: 0; }
.info-value { color: #08060d; }
.edit-section { width: 100%; display: flex; flex-direction: column; gap: 12px; }
.edit-actions { display: flex; gap: 8px; }
.profile-actions { display: flex; gap: 12px; margin-top: 8px; }

.dialog-title { font-size: 16px; font-weight: 600; color: #303133; }
.custom-header { display: flex; align-items: center; justify-content: space-between; }
.custom-header .dialog-title { flex: 1; text-align: center; }
.close-btn { font-size: 20px; color: #909399; padding: 4px 8px; flex-shrink: 0; }
.close-btn:hover { color: #303133; }
.avatar-picker { display: flex; flex-direction: column; gap: 20px; }
.picker-section h4 { margin: 0 0 12px; font-size: 15px; font-weight: 500; color: #606266; }
.preset-grid { display: flex; flex-wrap: wrap; gap: 12px; }
.preset-item { display: flex; justify-content: center; align-items: center; padding: 8px; border: 2px solid transparent; border-radius: 8px; cursor: pointer; transition: border-color .2s; flex: 0 0 auto; }
.preset-item:hover { border-color: #409eff; }
.preset-item.active { border-color: #409eff; background: rgba(64,158,255,.1); }

.crop-container { display: flex; gap: 24px; align-items: flex-start; }
.crop-area { position: relative; flex-shrink: 0; }
.crop-canvas { display: block; cursor: grab; border-radius: 4px; border: 1px solid #dcdfe6; }
.crop-canvas:active { cursor: grabbing; }
.crop-hint { margin: 8px 0 0; font-size: 12px; color: #909399; text-align: center; }
.crop-preview-area { display: flex; flex-direction: column; align-items: center; gap: 8px; }
.preview-label { font-size: 13px; color: #909399; margin: 0; }
.preview-canvas { display: block; border-radius: 50%; border: 2px solid #e5e4e7; }
</style>

<style>
.avatar-dialog .el-dialog__header { padding-bottom: 12px; }
.crop-dialog .el-dialog__header { padding-bottom: 12px; }
</style>
