<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyArticles, deleteArticle, batchDeleteArticles } from '@/api/article.js'
import { getMyComments, deleteComment, batchDeleteComments } from '@/api/comment.js'
import { getMyLikes } from '@/api/like.js'
import { getMyFavorites, batchRemoveFavorites } from '@/api/favorite.js'
import { formatDate } from '@/utils/format.js'
import ScrollObserver from '@/components/ScrollObserver.vue'

const router = useRouter()
const activeTab = ref('articles')
const loading = ref(false)
const manageMode = ref(false)
const selectedIds = ref([])

const articles = ref([])
const comments = ref([])
const likes = ref([])
const favorites = ref([])

const currentList = computed(() => {
  switch (activeTab.value) {
    case 'articles': return articles.value
    case 'comments': return comments.value
    case 'likes': return likes.value
    case 'favorites': return favorites.value
    default: return []
  }
})

const allSelected = computed(() => {
  const list = currentList.value
  return list.length > 0 && selectedIds.value.length === list.length
})

const fetchData = async () => {
  loading.value = true
  try {
    const [a, c, l, f] = await Promise.all([
      getMyArticles(),
      getMyComments(),
      getMyLikes(),
      getMyFavorites()
    ])
    articles.value = a
    comments.value = c
    likes.value = l
    favorites.value = f
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const getItemId = (item) => {
  if (activeTab.value === 'comments') return item.id
  return item.id
}

const toggleManage = () => {
  manageMode.value = !manageMode.value
  if (!manageMode.value) selectedIds.value = []
}

const toggleSelectAll = () => {
  if (allSelected.value) {
    selectedIds.value = []
  } else {
    selectedIds.value = currentList.value.map(getItemId)
  }
}

const toggleSelect = (id) => {
  const idx = selectedIds.value.indexOf(id)
  if (idx === -1) selectedIds.value.push(id)
  else selectedIds.value.splice(idx, 1)
}

const handleDelete = async (item) => {
  const id = item.id
  const isComment = activeTab.value === 'comments'
  try {
    await ElMessageBox.confirm(`确定删除这条${isComment ? '评论' : '文章'}吗？`)
    const res = isComment ? await deleteComment(id) : await deleteArticle(id)
    if (res.success) {
      ElMessage.success('删除成功')
      fetchData()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error('删除失败')
    }
  }
}

const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择')
    return
  }
  const tab = activeTab.value
  let label = ''
  if (tab === 'articles') label = '文章'
  else if (tab === 'comments') label = '评论'
  else if (tab === 'favorites') label = '收藏'

  const action = tab === 'favorites' ? '取消收藏' : '删除'
  try {
    await ElMessageBox.confirm(`确定${action}选中的 ${selectedIds.value.length} 条${label}吗？`)
    let res
    if (tab === 'articles') res = await batchDeleteArticles(selectedIds.value)
    else if (tab === 'comments') res = await batchDeleteComments(selectedIds.value)
    else if (tab === 'favorites') res = await batchRemoveFavorites(selectedIds.value)
    if (res && res.success) {
      ElMessage.success(`${action}成功`)
      selectedIds.value = []
      manageMode.value = false
      fetchData()
    } else {
      ElMessage.error(res?.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error('操作失败')
    }
  }
}

const handleCardClick = (item) => {
  if (manageMode.value) {
    toggleSelect(getItemId(item))
    return
  }
  if (activeTab.value === 'comments') {
    router.push(`/article/${item.articleId}`)
  } else {
    router.push(`/article/${item.id}`)
  }
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="my-articles">
    <el-tabs v-model="activeTab" @tab-change="manageMode = false; selectedIds = []">
      <el-tab-pane label="文章" name="articles" />
      <el-tab-pane label="评论" name="comments" />
      <el-tab-pane label="点赞" name="likes" />
      <el-tab-pane label="收藏" name="favorites" />
    </el-tabs>

    <div class="header">
      <div class="header-actions">
        <template v-if="manageMode">
          <el-button size="small" class="neu-btn" @click="toggleSelectAll">{{ allSelected ? '取消全选' : '全选' }}</el-button>
          <el-button v-if="activeTab !== 'likes'" size="small" class="donate-btn-red" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
            {{ activeTab === 'favorites' ? '取消收藏' : '删除' }} ({{ selectedIds.length }})
          </el-button>
          <el-button size="small" class="neu-btn" @click="toggleManage">退出管理</el-button>
        </template>
        <template v-else>
          <el-button size="small" class="neu-btn" @click="toggleManage">管理</el-button>
          <el-button v-if="activeTab === 'articles'" class="donate-btn" @click="router.push('/article/edit')">写文章</el-button>
        </template>
      </div>
    </div>

    <div v-loading="loading" class="items">
      <!-- 文章 Tab -->
      <template v-if="activeTab === 'articles'">
        <ScrollObserver
          v-for="(article, index) in articles"
          :key="article.id"
          :threshold="0.2"
        >
          <div class="item-card" :class="{ selected: selectedIds.includes(article.id) }" :style="{ transitionDelay: `${(index % 5) * 0.03}s` }">
            <div class="card-body" @click="handleCardClick(article)">
              <div v-if="manageMode" class="checkbox" @click.stop="toggleSelect(article.id)">
                <el-checkbox :model-value="selectedIds.includes(article.id)" />
              </div>
              <div class="card-content">
                <h3>{{ article.title }}</h3>
                <p class="meta-row">
                  <span>{{ article.viewCount }} 次浏览</span>
                  <span class="sep">|</span>
                  <span>{{ article.likeCount ?? 0 }} 赞</span>
                  <span class="sep">|</span>
                  <span>{{ article.commentCount ?? 0 }} 评论</span>
                </p>
                <p class="meta-time">{{ formatDate(article.createTime) }}</p>
                <div v-if="article.tags && article.tags.length > 0" class="article-tags">
                  <span
                    v-for="tag in article.tags.slice(0, 3)"
                    :key="tag.name"
                    class="tag-chip"
                    :style="{
                      background: (tag.color || '#409eff') + '1a',
                      color: tag.color || '#409eff',
                      borderColor: (tag.color || '#409eff') + '4d'
                    }"
                  >
                    {{ tag.name }}
                  </span>
                  <span v-if="article.tags.length > 3" class="tag-more">+{{ article.tags.length - 3 }}</span>
                </div>
                <p v-if="article.summary" class="summary">{{ article.summary }}</p>
              </div>
            </div>
            <div v-if="!manageMode" class="card-actions">
              <el-button size="small" class="neu-btn" @click="router.push(`/article/edit/${article.id}`)">编辑</el-button>
              <el-button size="small" class="donate-btn-red" @click="handleDelete(article)">删除</el-button>
            </div>
          </div>
        </ScrollObserver>
        <el-empty v-if="!loading && articles.length === 0" description="还没有写过文章" />
      </template>

      <!-- 评论 Tab -->
      <template v-if="activeTab === 'comments'">
        <ScrollObserver
          v-for="(comment, index) in comments"
          :key="comment.id"
          :threshold="0.2"
        >
          <div class="item-card" :class="{ selected: selectedIds.includes(comment.id) }" :style="{ transitionDelay: `${(index % 5) * 0.03}s` }">
            <div class="card-body" @click="handleCardClick(comment)">
              <div v-if="manageMode" class="checkbox" @click.stop="toggleSelect(comment.id)">
                <el-checkbox :model-value="selectedIds.includes(comment.id)" />
              </div>
              <div class="card-content">
                <h3 class="comment-title">{{ comment.articleTitle }}</h3>
                <p class="comment-preview">{{ comment.content }}</p>
                <p class="meta-time">{{ formatDate(comment.createTime) }}</p>
              </div>
            </div>
            <div v-if="!manageMode" class="card-actions">
              <el-button size="small" class="neu-btn" @click="router.push(`/article/${comment.articleId}`)">查看原文</el-button>
              <el-button size="small" class="donate-btn-red" @click="handleDelete(comment)">删除</el-button>
            </div>
          </div>
        </ScrollObserver>
        <el-empty v-if="!loading && comments.length === 0" description="还没有评论" />
      </template>

      <!-- 点赞 Tab -->
      <template v-if="activeTab === 'likes'">
        <ScrollObserver
          v-for="(article, index) in likes"
          :key="article.id"
          :threshold="0.2"
        >
          <div class="item-card" :style="{ transitionDelay: `${(index % 5) * 0.03}s` }" @click="handleCardClick(article)">
            <div class="card-body">
              <div v-if="manageMode" class="checkbox" @click.stop="toggleSelect(article.id)">
                <el-checkbox :model-value="selectedIds.includes(article.id)" />
              </div>
              <div class="card-content">
                <h3>{{ article.title }}</h3>
                <p class="meta-row">
                  <span>{{ article.authorName }}</span>
                  <span class="sep">|</span>
                  <span>{{ article.viewCount }} 次浏览</span>
                  <span class="sep">|</span>
                  <span>{{ article.likeCount ?? 0 }} 赞</span>
                </p>
                <p class="meta-time">{{ formatDate(article.createTime) }}</p>
                <div v-if="article.tags && article.tags.length > 0" class="article-tags">
                  <span
                    v-for="tag in article.tags.slice(0, 3)"
                    :key="tag.name"
                    class="tag-chip"
                    :style="{
                      background: (tag.color || '#409eff') + '1a',
                      color: tag.color || '#409eff',
                      borderColor: (tag.color || '#409eff') + '4d'
                    }"
                  >
                    {{ tag.name }}
                  </span>
                  <span v-if="article.tags.length > 3" class="tag-more">+{{ article.tags.length - 3 }}</span>
                </div>
                <p v-if="article.summary" class="summary">{{ article.summary }}</p>
              </div>
            </div>
          </div>
        </ScrollObserver>
        <el-empty v-if="!loading && likes.length === 0" description="还没有点赞的文章" />
      </template>

      <!-- 收藏 Tab -->
      <template v-if="activeTab === 'favorites'">
        <ScrollObserver
          v-for="(article, index) in favorites"
          :key="article.id"
          :threshold="0.2"
        >
          <div class="item-card" :class="{ selected: selectedIds.includes(article.id) }" :style="{ transitionDelay: `${(index % 5) * 0.03}s` }">
            <div class="card-body" @click="handleCardClick(article)">
              <div v-if="manageMode" class="checkbox" @click.stop="toggleSelect(article.id)">
                <el-checkbox :model-value="selectedIds.includes(article.id)" />
              </div>
              <div class="card-content">
                <h3>{{ article.title }}</h3>
                <p class="meta-row">
                  <span>{{ article.authorName }}</span>
                  <span class="sep">|</span>
                  <span>{{ article.viewCount }} 次浏览</span>
                  <span class="sep">|</span>
                  <span>{{ article.likeCount ?? 0 }} 赞</span>
                  <span class="sep">|</span>
                  <span>{{ article.commentCount ?? 0 }} 评论</span>
                </p>
                <p class="meta-time">{{ formatDate(article.createTime) }}</p>
                <div v-if="article.tags && article.tags.length > 0" class="article-tags">
                  <span
                    v-for="tag in article.tags.slice(0, 3)"
                    :key="tag.name"
                    class="tag-chip"
                    :style="{
                      background: (tag.color || '#409eff') + '1a',
                      color: tag.color || '#409eff',
                      borderColor: (tag.color || '#409eff') + '4d'
                    }"
                  >
                    {{ tag.name }}
                  </span>
                  <span v-if="article.tags.length > 3" class="tag-more">+{{ article.tags.length - 3 }}</span>
                </div>
                <p v-if="article.summary" class="summary">{{ article.summary }}</p>
              </div>
            </div>
          </div>
        </ScrollObserver>
        <el-empty v-if="!loading && favorites.length === 0" description="还没有收藏的文章" />
      </template>
    </div>
  </div>
</template>

<style scoped>
.my-articles {
  padding: 20px 40px;
}
.header {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 20px;
}
.header-actions {
  display: flex;
  gap: 8px;
}
.item-card {
  border: 1px solid #e5e4e7;
  border-radius: 8px;
  margin-bottom: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.3s ease, border-color 0.3s ease, background-color 0.3s ease, opacity 0.2s ease, transform 0.2s ease;
  opacity: 0;
  transform: scale(0.7);
}
.scroll-observer.scroll-visible .item-card {
  opacity: 1;
  transform: scale(1);
}
.item-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}
.item-card.selected {
  border-color: #409eff;
  background: rgba(64, 158, 255, 0.03);
}
.card-body {
  padding: 20px;
  cursor: pointer;
  transition: box-shadow 0.3s ease;
  display: flex;
  align-items: flex-start;
  gap: 12px;
}
.card-body:hover {
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}
.checkbox {
  padding-top: 2px;
}
.item-card h3 {
  margin: 0 0 8px;
  color: #08060d;
}
.comment-title {
  font-size: 15px;
  color: #409eff;
}
.comment-preview {
  color: #6b6375;
  margin: 0 0 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-content {
  flex: 1;
  min-width: 0;
}
.meta-row {
  color: #9ca3af;
  font-size: 14px;
  margin: 0 0 4px;
}
.meta-row .sep {
  margin: 0 8px;
}
.meta-time {
  color: #9ca3af;
  font-size: 14px;
  margin: 0 0 8px;
}
.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
  justify-content: center;
}
.tag-chip {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  border: 1px solid transparent;
  transition: transform 0.2s ease;
}
.tag-chip:hover {
  transform: translateY(-1px);
}
.tag-more {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #9ca3af;
  background: rgba(0, 0, 0, 0.04);
}
.summary {
  color: #6b6375;
  margin: 0;
}
.card-actions {
  padding: 10px 20px;
  border-top: 1px solid #e5e4e7;
  background: #fafafa;
  display: flex;
  gap: 8px;
}
</style>
