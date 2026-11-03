<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getDrafts, deleteDraft } from '@/api/draft.js'
import { formatDate } from '@/utils/format.js'
import ScrollObserver from '@/components/ScrollObserver.vue'

const router = useRouter()
const drafts = ref([])
const loading = ref(false)

const fetchDrafts = async () => {
  loading.value = true
  try {
    const res = await getDrafts()
    drafts.value = res || []
  } catch (error) {
    ElMessage.error('获取草稿列表失败')
  } finally {
    loading.value = false
  }
}

const handleEdit = (draft) => {
  router.push(`/article/edit?draft=${draft.id}`)
}

const handleDelete = async (draft) => {
  try {
    await ElMessageBox.confirm('确定删除这条草稿吗？')
    const res = await deleteDraft(draft.id)
    if (res.success) {
      ElMessage.success('删除成功')
      fetchDrafts()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {}
}

const parseTags = (tags) => {
  if (!tags) return []
  if (typeof tags === 'string') {
    try { return JSON.parse(tags) } catch { return [] }
  }
  return tags
}

onMounted(() => {
  fetchDrafts()
})
</script>

<template>
  <div class="drafts-page">
    <div class="header">
      <h2>草稿箱</h2>
      <el-button class="donate-btn" @click="router.push('/article/edit')">写文章</el-button>
    </div>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="3" animated />
    </div>

    <div v-else-if="drafts.length === 0" class="empty">
      <el-empty description="暂无草稿">
        <el-button class="donate-btn" @click="router.push('/article/edit')">去写文章</el-button>
      </el-empty>
    </div>

    <div v-else class="draft-list">
      <ScrollObserver
        v-for="(draft, index) in drafts"
        :key="draft.id"
        :threshold="0.2"
      >
        <div
          class="item-card"
          :style="{ transitionDelay: `${(index % 5) * 0.03}s` }"
        >
          <div class="card-body" @click="handleEdit(draft)">
            <div class="card-content">
              <h3>{{ draft.title || '无标题' }}</h3>
              <p class="meta-row">
                <span class="word-count">{{ draft.content ? draft.content.length : 0 }} 字</span>
                <span class="sep">|</span>
                <span>{{ formatDate(draft.updated_at || draft.created_at) }}</span>
              </p>
              <div v-if="draft.tags" class="article-tags">
                <span
                  v-for="tag in parseTags(draft.tags).slice(0, 3)"
                  :key="typeof tag === 'string' ? tag : tag.name"
                  class="tag-chip"
                  :style="{
                    background: (tag.color || '#409eff') + '1a',
                    color: tag.color || '#409eff',
                    borderColor: (tag.color || '#409eff') + '4d'
                  }"
                >
                  {{ typeof tag === 'string' ? tag : tag.name }}
                </span>
                <span v-if="parseTags(draft.tags).length > 3" class="tag-more">+{{ parseTags(draft.tags).length - 3 }}</span>
              </div>
              <p v-if="draft.summary" class="summary">{{ draft.summary }}</p>
            </div>
          </div>
          <div class="card-actions">
            <el-button size="small" class="neu-btn" @click="handleEdit(draft)">继续编辑</el-button>
            <el-button size="small" class="donate-btn-red" @click="handleDelete(draft)">删除</el-button>
          </div>
        </div>
      </ScrollObserver>
    </div>
  </div>
</template>

<style scoped>
.drafts-page {
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
.draft-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.item-card {
  border: 1px solid #e5e4e7;
  border-radius: 8px;
  overflow: hidden;
  transition: box-shadow 0.3s ease, border-color 0.3s ease, opacity 0.2s ease, transform 0.2s ease;
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
.card-body {
  padding: 20px;
  cursor: pointer;
  transition: box-shadow 0.3s ease;
}
.card-body:hover {
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}
.card-content {
  flex: 1;
  min-width: 0;
}
.item-card h3 {
  margin: 0 0 8px;
  color: #08060d;
}
.meta-row {
  color: #9ca3af;
  font-size: 14px;
  margin: 0 0 4px;
}
.meta-row .sep {
  margin: 0 8px;
}
.word-count {
  color: #9ca3af;
}
.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
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
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-actions {
  padding: 10px 20px;
  border-top: 1px solid #e5e4e7;
  background: #fafafa;
  display: flex;
  gap: 8px;
}
</style>
