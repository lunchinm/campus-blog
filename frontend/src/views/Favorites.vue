<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyFavorites } from '@/api/favorite.js'
import { formatDate } from '@/utils/format.js'
import ScrollObserver from '@/components/ScrollObserver.vue'

const router = useRouter()
const articles = ref([])
const loading = ref(false)

const fetchFavorites = async () => {
  loading.value = true
  try {
    const res = await getMyFavorites()
    articles.value = res
  } catch (error) {
    ElMessage.error('获取收藏列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchFavorites()
})
</script>

<template>
  <div class="favorites">
    <h2>我的收藏</h2>
    <div v-loading="loading" class="articles">
      <ScrollObserver
        v-for="(article, index) in articles"
        :key="article.id"
        :threshold="0.2"
      >
        <router-link
          :to="`/article/${article.id}`"
          class="article-card"
          :style="{ transitionDelay: `${(index % 5) * 0.03}s` }"
        >
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
        </router-link>
      </ScrollObserver>
      <el-empty v-if="!loading && articles.length === 0" description="还没有收藏的文章" />
    </div>
  </div>
</template>

<style scoped>
.favorites {
  padding: 20px 40px;
}
h2 {
  margin-bottom: 20px;
}
.article-card {
  display: block;
  text-decoration: none;
  color: inherit;
  border: 1px solid #e5e4e7;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
  cursor: pointer;
  transition: box-shadow 0.3s ease, opacity 0.2s ease, transform 0.2s ease;
  opacity: 0;
  transform: scale(0.7);
}
.scroll-observer.scroll-visible .article-card {
  opacity: 1;
  transform: scale(1);
}
.article-card:focus-visible {
  outline: 2px solid #409eff;
  outline-offset: 2px;
}
.article-card:hover {
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}
.article-card h3 {
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
.meta-time {
  color: #9ca3af;
  font-size: 14px;
  margin: 0 0 4px;
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
</style>
