<script setup>
import { ref, watch, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getArticleDetail, deleteArticle } from '@/api/article.js'
import { getLikeStatus, toggleLike } from '@/api/like.js'
import { getFavoriteStatus, toggleFavorite } from '@/api/favorite.js'
import { getComments, addComment, deleteComment, toggleCommentLike, getCommentLikeStatus } from '@/api/comment.js'
import { isLoggedIn, getUser } from '@/utils/auth.js'
import { formatDate } from '@/utils/format.js'
import { followUser, unfollowUser, getFollowing } from '@/api/follow.js'
import { getUserProfile } from '@/api/user.js'
import { sendMessage } from '@/api/message.js'
import MarkdownRender from '@/components/MarkdownRender.vue'
import StarFollowButton from '@/components/StarFollowButton.vue'
import ScrollObserver from '@/components/ScrollObserver.vue'

const route = useRoute()
const router = useRouter()
const article = ref(null)
const loading = ref(false)
const liked = ref(false)
const likeCount = ref(0)
const favorited = ref(false)
const favoriteCount = ref(0)
const shareCount = ref(0)
const comments = ref([])
const commentText = ref('')
const submitting = ref(false)
const commentLoading = ref(false)
const showCommentInput = ref(false)
const replyingTo = ref(null)
const replyText = ref('')
const replySubmitting = ref(false)
const commentLikes = ref({})
const authorFollowed = ref(false)
const avatarVersion = ref(0)
const shareSubmitting = ref(false)
const showShareDialog = ref(false)
const shareSearchQuery = ref('')
const shareUserList = ref([])
const shareSelectedUsers = ref([])
const shareLoading = ref(false)

const authorAvatarSrc = computed(() => {
  if (!article.value?.authorAvatar) return null
  const sep = article.value.authorAvatar.includes('?') ? '&' : '?'
  return `${article.value.authorAvatar}${sep}v=${avatarVersion.value}`
})

const flattenComments = (tree, depth = 0) => {
  const result = []
  for (const item of tree) {
    result.push({ ...item, depth })
    if (item.children && item.children.length > 0) {
      result.push(...flattenComments(item.children, depth + 1))
    }
  }
  return result
}

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await getArticleDetail(route.params.id)
    if (res.success === false) {
      ElMessage.error(res.message)
      return
    }
    article.value = res
    avatarVersion.value = Date.now()
    likeCount.value = res.likeCount ?? 0
    shareCount.value = res.shareCount ?? 0
  } catch (error) {
    ElMessage.error('获取文章失败')
  } finally {
    loading.value = false
  }
}

const fetchLikeStatus = async () => {
  if (!isLoggedIn()) return
  try {
    const res = await getLikeStatus(route.params.id)
    liked.value = res.liked
  } catch (error) {}
}

const fetchFavoriteStatus = async () => {
  if (!isLoggedIn()) return
  try {
    const res = await getFavoriteStatus(route.params.id)
    favorited.value = res.favorited
    favoriteCount.value = res.count
  } catch (error) {}
}

const fetchComments = async () => {
  commentLoading.value = true
  try {
    const res = await getComments(route.params.id)
    comments.value = flattenComments(res)
  } catch (error) {
  } finally {
    commentLoading.value = false
  }
}

const fetchAuthorFollowStatus = async () => {
  if (!isLoggedIn() || !article.value) return
  const user = getUser()
  if (!user || user.id === article.value.userId) return
  try {
    const res = await getUserProfile(article.value.userId)
    if (res.success) {
      authorFollowed.value = res.profile.isFollowing
    }
  } catch (error) {}
}

const handleLike = async () => {
  if (!isLoggedIn()) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    const res = await toggleLike(route.params.id)
    liked.value = res.liked
    likeCount.value = res.count
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleFavorite = async () => {
  if (!isLoggedIn()) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    const res = await toggleFavorite(route.params.id)
    favorited.value = res.favorited
    favoriteCount.value = res.count
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleAddComment = async () => {
  if (!isLoggedIn()) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!commentText.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  submitting.value = true
  try {
    const res = await addComment(route.params.id, commentText.value.trim())
    if (res.success) {
      commentText.value = ''
      ElMessage.success('评论成功')
      fetchComments()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('评论失败')
  } finally {
    submitting.value = false
  }
}

const startReply = (comment) => {
  replyingTo.value = comment
  replyText.value = ''
}

const cancelReply = () => {
  replyingTo.value = null
  replyText.value = ''
}

const handleAddReply = async () => {
  if (!isLoggedIn()) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!replyText.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  replySubmitting.value = true
  try {
    const res = await addComment(route.params.id, replyText.value.trim(), String(replyingTo.value.id))
    if (res.success) {
      replyText.value = ''
      replyingTo.value = null
      ElMessage.success('回复成功')
      fetchComments()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('回复失败')
  } finally {
    replySubmitting.value = false
  }
}

const fetchCommentLikes = async () => {
  if (!isLoggedIn()) return
  try {
    const flatComments = flattenComments(comments.value)
    for (const comment of flatComments) {
      const res = await getCommentLikeStatus(comment.id)
      commentLikes.value[comment.id] = {
        liked: res.liked,
        count: res.count || 0
      }
    }
  } catch (error) {}
}

const handleCommentLike = async (comment) => {
  if (!isLoggedIn()) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    const res = await toggleCommentLike(comment.id)
    if (res.success) {
      const current = commentLikes.value[comment.id] || { liked: false, count: 0 }
      commentLikes.value[comment.id] = {
        liked: !current.liked,
        count: current.liked ? current.count - 1 : current.count + 1
      }
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDeleteComment = async (commentId) => {
  try {
    await ElMessageBox.confirm('确定删除这条评论吗？')
    const res = await deleteComment(commentId)
    if (res.success) {
      ElMessage.success('删除成功')
      fetchComments()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {}
}

const handleDeleteArticle = async () => {
  try {
    await ElMessageBox.confirm('确定删除这篇文章吗？')
    const res = await deleteArticle(route.params.id)
    if (res.success) {
      ElMessage.success('删除成功')
      router.push('/articles')
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {}
}

const handleToggleFollow = async () => {
  if (!isLoggedIn()) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    let res
    if (authorFollowed.value) {
      res = await unfollowUser(article.value.userId)
    } else {
      res = await followUser(article.value.userId)
    }
    if (res.success) {
      authorFollowed.value = !authorFollowed.value
      ElMessage.success(authorFollowed.value ? '关注成功' : '取消关注成功')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleShare = async () => {
  if (!isLoggedIn()) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  showShareDialog.value = true
  shareSearchQuery.value = ''
  shareSelectedUsers.value = []
  await fetchShareUserList()
}

const fetchShareUserList = async () => {
  shareLoading.value = true
  try {
    const user = getUser()
    const res = await getFollowing(user.id)
    shareUserList.value = res || []
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    shareLoading.value = false
  }
}

const filteredShareUsers = computed(() => {
  if (!shareSearchQuery.value.trim()) return shareUserList.value
  const q = shareSearchQuery.value.trim().toLowerCase()
  return shareUserList.value.filter(u =>
    (u.nickname && u.nickname.toLowerCase().includes(q)) ||
    (u.username && u.username.toLowerCase().includes(q))
  )
})

const toggleShareUser = (userId) => {
  const idx = shareSelectedUsers.value.indexOf(userId)
  if (idx > -1) {
    shareSelectedUsers.value.splice(idx, 1)
  } else {
    shareSelectedUsers.value.push(userId)
  }
}

const confirmShare = async () => {
  if (shareSelectedUsers.value.length === 0) {
    ElMessage.warning('请选择至少一个接收者')
    return
  }
  shareSubmitting.value = true
  try {
    for (const userId of shareSelectedUsers.value) {
      await sendMessage(userId, `article:${article.value.id}`, 'article', String(article.value.id))
    }
    shareCount.value += shareSelectedUsers.value.length
    ElMessage.success('分享成功')
    showShareDialog.value = false
  } catch (error) {
    ElMessage.error('分享失败')
  } finally {
    shareSubmitting.value = false
  }
}

const goAuthorProfile = () => {
  if (!article.value) return
  const user = getUser()
  if (user && user.id === article.value.userId) {
    router.push(`/user/${article.value.userId}`)
  } else {
    router.push(`/user/${article.value.userId}?view=1`)
  }
}

const isAuthor = () => {
  if (!article.value || !isLoggedIn()) return false
  const user = getUser()
  return user && user.id === article.value.userId
}

const isCommentAuthor = (comment) => {
  const user = getUser()
  return user && user.id === comment.userId
}

onMounted(() => {
  fetchDetail().then(() => fetchAuthorFollowStatus())
  fetchLikeStatus()
  fetchFavoriteStatus()
  fetchComments().then(() => {
    fetchCommentLikes()
  })
})

watch(() => route.params.id, (newId, oldId) => {
  if (newId && newId !== oldId) {
    fetchDetail().then(() => fetchAuthorFollowStatus())
    fetchLikeStatus()
    fetchFavoriteStatus()
    fetchComments()
  }
})
</script>

<template>
  <div v-loading="loading" class="article-detail">
    <div v-if="article" class="top-bar">
      <button class="cta" type="button" @click="router.push('/articles')">
        <svg
          id="arrow-horizontal"
          xmlns="http://www.w3.org/2000/svg"
          width="30"
          height="10"
          viewBox="0 0 46 16"
        >
          <path
            id="Path_10"
            data-name="Path 10"
            d="M8,0,6.545,1.455l5.506,5.506H-30V9.039H12.052L6.545,14.545,8,16l8-8Z"
            transform="translate(38) scale(-1, 1)"
          ></path>
        </svg>
        <span class="hover-underline-animation">返回列表</span>
      </button>
      <div class="author-section">
        <div class="author-avatar" @click="goAuthorProfile">
          <el-avatar
            :size="32"
            :src="authorAvatarSrc"
            :key="avatarVersion"
          >
            {{ article.authorName?.charAt(0) || 'U' }}
          </el-avatar>
        </div>
        <span class="author-name" @click="goAuthorProfile">{{ article.authorName }}</span>
        <StarFollowButton
          v-if="!isAuthor() && isLoggedIn()"
          :following="authorFollowed"
          @toggle="handleToggleFollow"
        />
      </div>
    </div>
    <div v-if="article" class="content">
      <h1>{{ article.title }}</h1>
      <p class="meta-row">
        <span>{{ article.viewCount }} 次浏览</span>
        <span class="sep">|</span>
        <span>{{ formatDate(article.createTime) }}</span>
      </p>

      <div v-if="article.tags && article.tags.length > 0" class="article-tags">
        <span
          v-for="tag in article.tags"
          :key="tag.name"
          class="tag-item"
          :style="{
            background: (tag.color || '#409eff') + '1a',
            color: tag.color || '#409eff',
            borderColor: (tag.color || '#409eff') + '4d'
          }"
        >
          {{ tag.name }}
        </span>
      </div>

      <div class="article-body">
        <MarkdownRender :content="article.content" />
      </div>

      <div class="action-bar">
        <div class="comment-btn-wrapper group">
          <button class="btn comment-btn" type="button" @click="showCommentInput = !showCommentInput">
            <svg
              stroke-linejoin="round"
              stroke-linecap="round"
              stroke="currentColor"
              stroke-width="2"
              viewBox="0 0 24 24"
              height="44"
              width="44"
              xmlns="http://www.w3.org/2000/svg"
              class="comment-icon"
              fill="none"
            >
              <path fill="none" d="M0 0h24v24H0z" stroke="none"></path>
              <path d="M8 9h8"></path>
              <path d="M8 13h6"></path>
              <path
                d="M18 4a3 3 0 0 1 3 3v8a3 3 0 0 1 -3 3h-5l-5 3v-3h-2a3 3 0 0 1 -3 -3v-8a3 3 0 0 1 3 -3h12z"
              ></path>
            </svg>
          </button>
          <span class="tooltip">评论</span>
          <span class="count">{{ comments.length }}</span>
        </div>
        <div class="like-btn-wrapper group">
          <button class="btn like-btn" :class="{ active: liked }" type="button" @click="handleLike">
            <svg
              class="icon"
              xmlns="http://www.w3.org/2000/svg"
              width="32"
              height="32"
              viewBox="0 0 17.503 15.625"
            >
              <path
                id="Fill"
                d="M8.752,15.625h0L1.383,8.162a4.824,4.824,0,0,1,0-6.762,4.679,4.679,0,0,1,6.674,0l.694.7.694-.7a4.678,4.678,0,0,1,6.675,0,4.825,4.825,0,0,1,0,6.762L8.752,15.624ZM4.72,1.25A3.442,3.442,0,0,0,2.277,2.275a3.562,3.562,0,0,0,0,5l6.475,6.556,6.475-6.556a3.563,3.563,0,0,0,0-5A3.443,3.443,0,0,0,12.786,1.25h-.01a3.415,3.415,0,0,0-2.443,1.038L8.752,3.9,7.164,2.275A3.442,3.442,0,0,0,4.72,1.25Z"
              ></path>
            </svg>
          </button>
          <span class="tooltip">点赞</span>
          <span class="count">{{ likeCount }}</span>
        </div>
        <div class="favorite-btn-wrapper group">
          <button
            class="btn favorite-btn"
            type="button"
            :class="{ active: favorited }"
            @click="handleFavorite"
            aria-label="收藏"
          >
            <svg
              class="favorite-icon"
              height="44"
              viewBox="0 0 24 24"
              width="44"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path d="M9.362,9.158c0,0-3.16,0.35-5.268,0.584c-0.19,0.023-0.358,0.15-0.421,0.343s0,0.394,0.14,0.521    c1.566,1.429,3.919,3.569,3.919,3.569c-0.002,0-0.646,3.113-1.074,5.19c-0.036,0.188,0.032,0.387,0.196,0.506    c0.163,0.119,0.373,0.121,0.538,0.028c1.844-1.048,4.606-2.624,4.606-2.624s2.763,1.576,4.604,2.625    c0.168,0.092,0.378,0.09,0.541-0.029c0.164-0.119,0.232-0.318,0.195-0.505c-0.428-2.078-1.071-5.191-1.071-5.191    s2.353-2.14,3.919-3.566c0.14-0.131,0.202-0.332,0.14-0.524s-0.23-0.319-0.42-0.341c-2.108-0.236-5.269-0.586-5.269-0.586    s-1.31-2.898-2.183-4.83c-0.082-0.173-0.254-0.294-0.456-0.294s-0.375,0.122-0.453,0.294C10.671,6.26,9.362,9.158,9.362,9.158z"></path>
            </svg>
          </button>
          <span class="tooltip">收藏</span>
          <span class="count">{{ favoriteCount }}</span>
        </div>
        <div class="share-btn-wrapper group">
          <button
            class="btn share-btn"
            type="button"
            @click="handleShare"
            aria-label="分享"
          >
            <svg
              class="share-icon"
              height="32"
              viewBox="0 0 24 24"
              width="32"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <circle cx="18" cy="5" r="3"></circle>
              <circle cx="6" cy="12" r="3"></circle>
              <circle cx="18" cy="19" r="3"></circle>
              <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"></line>
              <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"></line>
            </svg>
          </button>
          <span class="tooltip">分享</span>
          <span class="count">{{ shareCount }}</span>
        </div>
      </div>

      <div v-if="showCommentInput" class="comment-input-area">
        <el-input
          v-model="commentText"
          type="textarea"
          :rows="3"
          placeholder="写下你的评论..."
        />
        <div class="comment-input-actions">
          <el-button class="donate-btn" type="primary" :loading="submitting" @click="handleAddComment">发表</el-button>
          <el-button class="neu-btn" @click="showCommentInput = false; commentText = ''">取消</el-button>
        </div>
      </div>

      <div class="comments-section">
        <h3>评论 ({{ comments.length }})</h3>

        <div v-loading="commentLoading" class="comment-list">
          <ScrollObserver
            v-for="(comment, index) in comments"
            :key="comment.id"
            :threshold="0.2"
          >
            <div class="comment-item" :class="{ reply: comment.depth > 0, 'reply-deep': comment.depth > 1 }" :style="{ transitionDelay: `${(index % 5) * 0.03}s` }">
              <div class="comment-header">
                <span class="comment-nickname">{{ comment.nickname }}</span>
                <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
                <button
                  class="comment-like-btn"
                  type="button"
                  :class="{ active: commentLikes[comment.id]?.liked }"
                  @click="handleCommentLike(comment)"
                >
                  <svg width="14" height="14" viewBox="0 0 17.503 15.625" xmlns="http://www.w3.org/2000/svg">
                    <path v-if="!commentLikes[comment.id]?.liked" d="M8.752,15.625h0L1.383,8.162a4.824,4.824,0,0,1,0-6.762,4.679,4.679,0,0,1,6.674,0l.694.7.694-.7a4.678,4.678,0,0,1,6.675,0,4.825,4.825,0,0,1,0,6.762L8.752,15.624ZM4.619,1.891A3.435,3.435,0,0,0,2.188,4.166a3.5,3.5,0,0,0,0,4.91L8.752,15.4l6.564-6.324a3.5,3.5,0,0,0,0-4.91,3.526,3.526,0,0,0-5.025,0l-1.744,1.779a.671.671,0,0,1-.958,0L6.806,3.166A3.453,3.453,0,0,0,4.619,1.891Z" transform="translate(0 0)"/>
                    <path v-else d="M8.752,15.625h0L1.383,8.162a4.824,4.824,0,0,1,0-6.762,4.679,4.679,0,0,1,6.674,0l.694.7.694-.7a4.678,4.678,0,0,1,6.675,0,4.825,4.825,0,0,1,0,6.762L8.752,15.624Z" transform="translate(0 0)"/>
                  </svg>
                  <span class="comment-like-count">{{ commentLikes[comment.id]?.count || 0 }}</span>
                </button>
                <el-button
                  size="small"
                  type="primary"
                  text
                  class="reply-btn"
                  @click="startReply(comment)"
                >回复</el-button>
                <el-button
                  v-if="isCommentAuthor(comment)"
                  size="small"
                  type="danger"
                  text
                  @click="handleDeleteComment(comment.id)"
                >删除</el-button>
              </div>
              <div class="comment-body">
                <span v-if="comment.depth > 0" class="reply-prefix">@{{ comment.nickname }}</span>
                {{ comment.content }}
              </div>
              <div v-if="replyingTo?.id === comment.id" class="reply-input-area">
                <el-input
                  v-model="replyText"
                  type="textarea"
                  :rows="2"
                  placeholder="写下你的回复..."
                />
                <div style="display: flex; gap: 8px;">
                  <el-button class="donate-btn" type="primary" size="small" :loading="replySubmitting" @click="handleAddReply">发送</el-button>
                  <el-button class="neu-btn" size="small" @click="cancelReply">取消</el-button>
                </div>
              </div>
            </div>
          </ScrollObserver>
          <el-empty v-if="!commentLoading && comments.length === 0" description="暂无评论" />
        </div>
      </div>

      <div v-if="isAuthor()" class="actions">
        <el-button class="neu-btn" @click="router.push(`/article/edit/${article.id}`)">编辑</el-button>
        <el-button class="donate-btn-red" @click="handleDeleteArticle">删除</el-button>
      </div>
    </div>

    <!-- 分享弹窗 -->
    <div v-if="showShareDialog" class="share-dialog-overlay" @click.self="showShareDialog = false">
      <div class="share-dialog">
        <div class="share-dialog-header">
          <h3>分享文章</h3>
          <button class="share-dialog-close" @click="showShareDialog = false">×</button>
        </div>
        <form class="share-search-form" @submit.prevent>
          <label for="share-search">
            <input
              id="share-search"
              v-model="shareSearchQuery"
              class="share-search-input"
              type="text"
              required
              placeholder="搜索用户..."
            >
            <div class="fancy-bg" />
            <div class="search-icon">
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <g>
                  <path d="M21.53 20.47l-3.66-3.66C19.195 15.24 20 13.214 20 11c0-4.97-4.03-9-9-9s-9 4.03-9 9 4.03 9 9 9c2.215 0 4.24-.804 5.808-2.13l3.66 3.66c.147.146.34.22.53.22s.385-.073.53-.22c.295-.293.295-.767.002-1.06zM3.5 11c0-4.135 3.365-7.5 7.5-7.5s7.5 3.365 7.5 7.5-3.365 7.5-7.5 7.5-7.5-3.365-7.5-7.5z" />
                </g>
              </svg>
            </div>
            <button class="close-btn" type="button" @click="shareSearchQuery = ''">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />
              </svg>
            </button>
          </label>
        </form>
        <div v-loading="shareLoading" class="share-dialog-body">
          <div v-for="user in filteredShareUsers" :key="user.id" class="share-user-item">
            <div class="share-user-info">
              <el-avatar :size="36" :src="user.avatar">
                {{ user.nickname?.charAt(0) || user.username?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="share-user-name">{{ user.nickname || user.username }}</span>
            </div>
            <div class="cntr">
              <input :id="'cbx-'+user.id" type="checkbox" class="hidden-xs-up" :checked="shareSelectedUsers.includes(user.id)" @change="toggleShareUser(user.id)">
              <label :for="'cbx-'+user.id" class="cbx"></label>
            </div>
          </div>
          <el-empty v-if="!shareLoading && filteredShareUsers.length === 0" description="暂无用户" />
        </div>
        <div class="share-dialog-footer">
          <el-button class="neu-btn" @click="showShareDialog = false">取消</el-button>
          <el-button class="donate-btn" type="primary" :loading="shareSubmitting" @click="confirmShare">发送</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.article-detail {
  padding: 20px 40px;
}
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.author-section {
  display: flex;
  align-items: center;
  gap: 10px;
}
.author-avatar {
  cursor: pointer;
  flex-shrink: 0;
  border-radius: 50%;
  transition: transform 0.2s ease;
}
.author-avatar:hover {
  transform: scale(1.1);
}
.author-name {
  font-size: 14px;
  font-weight: 500;
  color: #08060d;
  cursor: pointer;
  transition: color 0.2s;
}
.author-name:hover {
  color: #409eff;
}
.cta {
  border: none;
  background: none;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.cta span {
  padding-bottom: 7px;
  letter-spacing: 4px;
  font-size: 18px;
  padding-left: 15px;
  text-transform: uppercase;
}
.cta svg {
  transform: translateX(0);
  transition: transform 0.3s ease;
}
.cta:hover svg {
  transform: translateX(-8px);
}
.cta:active svg {
  transform: scale(0.9) translateX(-8px);
}
.hover-underline-animation {
  position: relative;
  color: black;
  padding-bottom: 20px;
}
.hover-underline-animation::after {
  content: "";
  position: absolute;
  width: 100%;
  transform: scaleX(0);
  height: 2px;
  bottom: 0;
  left: 0;
  background-color: #000000;
  transform-origin: bottom right;
  transition: transform 0.25s ease-out;
}
.cta:hover .hover-underline-animation::after {
  transform: scaleX(1);
  transform-origin: bottom right;
}
.content {
  border: 1px solid #e5e4e7;
  border-radius: 8px;
  padding: 30px 40px;
  transition: box-shadow 0.3s ease;
}
.content:focus-visible {
  outline: 2px solid #409eff;
  outline-offset: 2px;
}
.content:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}
.content h1 {
  margin-bottom: 30px;
  word-break: break-word;
  text-align: center;
}
.meta-row {
  color: #9ca3af;
  font-size: 14px;
  margin: 0 0 12px;
  text-align: center;
}
.meta-row .sep {
  margin: 0 8px;
}
.article-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  margin-bottom: 30px;
}
.tag-item {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid transparent;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.tag-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}
.article-body {
  margin-bottom: 30px;
  text-align: left;
}
.action-bar {
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
  gap: 12px;
  align-items: flex-start;
}
.comment-btn-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  position: relative;
}
.comment-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  border-radius: 10px;
  cursor: pointer;
  border: none;
  background-color: transparent;
  position: relative;
  padding: 0;
}
.comment-icon {
  width: 32px;
  height: 32px;
  stroke: #666;
  transition: transform 0.2s ease-in-out, stroke 0.2s ease-in-out;
}
.comment-btn:hover .comment-icon {
  transform: scale(1.25);
  stroke: #3b82f6;
}
.tooltip {
  position: absolute;
  top: -56px;
  left: 50%;
  transform: translateX(-50%) scaleX(0);
  transform-origin: left;
  z-index: 20;
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid #d1d5db;
  background-color: white;
  font-size: 14px;
  font-weight: bold;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  transition: transform 0.3s ease-in-out, opacity 0.3s ease-in-out, visibility 0.3s ease-in-out;
  white-space: nowrap;
  opacity: 0;
  visibility: hidden;
}
.group:hover .tooltip {
  transform: translateX(-50%) scaleX(1);
  opacity: 1;
  visibility: visible;
}
.like-btn-wrapper,
.favorite-btn-wrapper,
.share-btn-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  position: relative;
}
.count {
  font-size: 13px;
  color: #666;
  margin-top: 4px;
}

.like-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  border-radius: 10px;
  cursor: pointer;
  border: none;
  background-color: transparent;
  position: relative;
  padding: 0;
}
.icon {
  width: 32px;
  height: 32px;
  transition: transform 0.2s linear;
}
.icon path {
  fill: #ccc;
  transition: fill 0.2s linear;
}
.like-btn:hover > .icon {
  transform: scale(1.2);
}
.like-btn:hover > .icon path {
  fill: rgb(177, 139, 189);
}
.like-btn.active .icon path {
  fill: #e74c3c;
}

.favorite-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  border-radius: 10px;
  cursor: pointer;
  border: none;
  background-color: transparent;
  position: relative;
  padding: 0;
}
.favorite-btn .favorite-icon {
  width: 44px;
  height: 44px;
  transition: transform 0.3s ease, fill 0.3s ease;
  fill: #666;
}
.favorite-btn:hover .favorite-icon {
  transform: scale(1.1);
}
.favorite-btn.active .favorite-icon {
  fill: #f59e0b;
}

.share-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  border-radius: 10px;
  cursor: pointer;
  border: none;
  background-color: transparent;
  position: relative;
  padding: 0;
}
.share-icon {
  width: 32px;
  height: 32px;
  stroke: #666;
  transition: transform 0.2s ease, stroke 0.2s ease;
}
.share-btn:hover .share-icon {
  transform: scale(1.2);
  stroke: #409eff;
}

.comment-input-area {
  margin-bottom: 20px;
}
.comment-input-actions {
  margin-top: 10px;
  display: flex;
  gap: 8px;
}
.comments-section {
  border-top: 1px solid #e5e4e7;
  padding-top: 20px;
  margin-bottom: 30px;
  text-align: left;
}
.comments-section h3 {
  margin: 0 0 20px;
}
.comment-list {
  min-height: 60px;
}
.comment-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  transition: opacity 0.2s ease, transform 0.2s ease;
  opacity: 0;
  transform: scale(0.7);
}
.scroll-observer.scroll-visible .comment-item {
  opacity: 1;
  transform: scale(1);
}
.comment-item.reply {
  padding-left: 40px;
}
.comment-item.reply-deep {
  padding-left: 80px;
}
.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
  flex-wrap: wrap;
}
.comment-nickname {
  font-weight: 500;
  color: #08060d;
}
.comment-time {
  color: #9ca3af;
  font-size: 13px;
}
.comment-body {
  color: #6b6375;
  line-height: 1.5;
}
.comment-like-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  border: 1px solid #e5e4e7;
  border-radius: 4px;
  background: transparent;
  cursor: pointer;
  font-size: 12px;
  color: #9ca3af;
  transition: all 0.2s ease;
}
.comment-like-btn svg {
  fill: #9ca3af;
  transition: fill 0.2s ease, transform 0.2s ease;
}
.comment-like-btn:hover {
  border-color: #f56c6c;
  color: #f56c6c;
}
.comment-like-btn:hover svg {
  fill: #f56c6c;
  transform: scale(1.1);
}
.comment-like-btn.active {
  border-color: #f56c6c;
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
  box-shadow: 0 2px 4px rgba(245, 108, 108, 0.3);
  transform: translateY(-1px);
}
.comment-like-btn.active svg {
  fill: #f56c6c;
  filter: drop-shadow(0 1px 1px rgba(245, 108, 108, 0.4));
}
.comment-like-count {
  font-size: 12px;
}
.reply-btn {
  font-size: 12px;
  padding: 0 4px;
}
.reply-prefix {
  color: #409eff;
  margin-right: 4px;
}
.reply-input-area {
  margin-top: 8px;
  display: flex;
  gap: 8px;
  align-items: flex-start;
}
.reply-input-area .el-input {
  flex: 1;
}
.actions {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e5e4e7;
}

/* 分享弹窗 */
.share-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.share-dialog {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  width: 420px;
  max-height: 70vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}
.share-dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}
.share-dialog-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #08060d;
}
.share-dialog-close {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: none;
  background: rgba(0, 0, 0, 0.06);
  color: #666;
  cursor: pointer;
  font-size: 18px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
.share-dialog-close:hover {
  background: rgba(0, 0, 0, 0.1);
}
.share-search-form {
  --input-text-color: #08060d;
  --input-bg-color: transparent;
  --focus-input-bg-color: #283542;
  --text-color: #949faa;
  --active-color: #1b9bee;
  --width-of-input: 100%;
  --inline-padding-of-input: 1.2em;
  --gap: 0.9rem;
  font-size: 0.9rem;
  display: flex;
  gap: 0.5rem;
  align-items: center;
  width: var(--width-of-input);
  position: relative;
  isolation: isolate;
  padding: 10px 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  box-sizing: border-box;
}
.share-search-form label {
  width: 100%;
  padding: 0 0.8em;
  height: 36px;
  padding-inline: var(--inline-padding-of-input);
  display: flex;
  align-items: center;
  box-sizing: border-box;
}
.share-search-form .fancy-bg {
  position: absolute;
  left: 20px;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  background: var(--input-bg-color);
  border: 1px solid #e5e4e7;
  border-radius: 30px;
  height: 36px;
  z-index: -1;
  pointer-events: none;
  box-shadow: rgba(0, 0, 0, 0.16) 0px 1px 4px;
  box-sizing: border-box;
}
.share-search-form .search-icon,
.share-search-form .close-btn {
  position: absolute;
}
.share-search-form .search-icon {
  fill: var(--text-color);
  left: calc(20px + var(--inline-padding-of-input));
}
.share-search-form .search-icon svg {
  width: 16px;
  display: block;
}
.share-search-form .close-btn {
  border: none;
  right: calc(20px + var(--inline-padding-of-input));
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  padding: 0.1em;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: var(--active-color);
  opacity: 0;
  visibility: hidden;
  cursor: pointer;
}
.share-search-form .close-btn svg {
  width: 10px;
  height: 10px;
  display: block;
}
.share-search-input {
  color: var(--input-text-color);
  width: 100%;
  margin-inline: min(2em, calc(var(--inline-padding-of-input) + var(--gap)));
  background: none;
  border: none;
  font-size: 0.9rem;
}
.share-search-input:focus {
  outline: none;
}
.share-search-input::placeholder {
  color: var(--text-color);
}
.share-search-input:focus ~ .fancy-bg {
  border: 1px solid var(--active-color);
  background: var(--focus-input-bg-color);
}
.share-search-input:focus ~ .search-icon {
  fill: var(--active-color);
}
.share-search-input:focus ~ .search-icon svg path {
  fill: var(--active-color);
}
.share-search-input:valid ~ .close-btn {
  opacity: 1;
  visibility: visible;
}
.share-search-input:-webkit-autofill,
.share-search-input:-webkit-autofill:hover,
.share-search-input:-webkit-autofill:focus,
.share-search-input:-webkit-autofill:active {
  -webkit-transition: color 9999s ease-out, background-color 9999s ease-out;
  -webkit-transition-delay: 9999s;
}
.share-dialog-body {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}
.share-user-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  cursor: pointer;
  transition: background 0.2s;
}
.share-user-item:hover {
  background: rgba(0, 0, 0, 0.03);
}
.share-user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}
.share-user-name {
  font-size: 14px;
  color: #08060d;
}
.share-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 12px 20px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

/* 自定义勾选框 */
.cntr {
  position: relative;
}
.cbx {
  position: relative;
  top: 1px;
  width: 22px;
  height: 22px;
  border: 1px solid #c8ccd4;
  border-radius: 50%;
  vertical-align: middle;
  transition: background 0.1s ease;
  cursor: pointer;
  display: block;
}
.cbx:after {
  content: '';
  position: absolute;
  top: 3px;
  left: 7px;
  width: 5px;
  height: 10px;
  opacity: 0;
  transform: rotate(45deg) scale(0);
  border-right: 2px solid #fff;
  border-bottom: 2px solid #fff;
  transition: all 0.3s ease;
  transition-delay: 0.15s;
}
.hidden-xs-up {
  display: none !important;
}
input[type="checkbox"]:checked + .cbx {
  border-color: transparent;
  background: #6871f1;
  animation: jelly 0.6s ease;
}
input[type="checkbox"]:checked + .cbx:after {
  opacity: 1;
  transform: rotate(45deg) scale(1);
}
@keyframes jelly {
  from { transform: scale(1, 1); }
  30% { transform: scale(1.25, 0.75); }
  40% { transform: scale(0.75, 1.25); }
  50% { transform: scale(1.15, 0.85); }
  65% { transform: scale(0.95, 1.05); }
  75% { transform: scale(1.05, 0.95); }
  to { transform: scale(1, 1); }
}
</style>
