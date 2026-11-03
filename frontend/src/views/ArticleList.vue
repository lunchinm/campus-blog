<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getArticleList, searchArticles } from '@/api/article.js'
import { getTags } from '@/api/tag.js'
import { isLoggedIn } from '@/utils/auth.js'
import { formatDate } from '@/utils/format.js'
import ScrollObserver from '@/components/ScrollObserver.vue'

const router = useRouter()
const articles = ref([])
const tags = ref([])
const selectedTag = ref('')
const searchKeyword = ref('')
const loading = ref(false)
let searchTimer = null

const filteredArticles = computed(() => {
  let result = articles.value
  if (selectedTag.value) {
    result = result.filter(a =>
      a.tags && a.tags.some(t => t.name === selectedTag.value)
    )
  }
  if (searchKeyword.value.trim()) {
    const kw = searchKeyword.value.trim().toLowerCase()
    result = result.filter(a => a.title.toLowerCase().includes(kw))
  }
  return result
})

const fetchArticles = async () => {
  loading.value = true
  try {
    const res = await getArticleList()
    articles.value = res
  } catch (error) {
    ElMessage.error('获取文章列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    // 搜索通过 computed 实时过滤，此处可扩展为调用后端接口
    // 如果后端已实现 searchArticles，可改为：
    // if (searchKeyword.value.trim()) {
    //   const res = await searchArticles(searchKeyword.value.trim())
    //   articles.value = res
    // } else {
    //   fetchArticles()
    // }
  }, 300)
}

const clearSearch = () => {
  searchKeyword.value = ''
}

const fetchTags = async () => {
  try {
    const res = await getTags()
    tags.value = res
  } catch (error) {}
}

onMounted(() => {
  fetchTags()
  fetchArticles()
})
</script>

<template>
  <div class="article-list">
    <div class="header">
      <form class="search-form" @submit.prevent>
        <label for="article-search">
          <input
            id="article-search"
            v-model="searchKeyword"
            class="search-input-twitter"
            type="text"
            required
            placeholder="搜索文章标题..."
            @input="handleSearch"
          >
          <div class="fancy-bg" />
          <div class="search-icon">
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <g>
                <path d="M21.53 20.47l-3.66-3.66C19.195 15.24 20 13.214 20 11c0-4.97-4.03-9-9-9s-9 4.03-9 9 4.03 9 9 9c2.215 0 4.24-.804 5.808-2.13l3.66 3.66c.147.146.34.22.53.22s.385-.073.53-.22c.295-.293.295-.767.002-1.06zM3.5 11c0-4.135 3.365-7.5 7.5-7.5s7.5 3.365 7.5 7.5-3.365 7.5-7.5 7.5-7.5-3.365-7.5-7.5z" />
              </g>
            </svg>
          </div>
          <button class="close-btn" type="button" @click="clearSearch">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />
            </svg>
          </button>
        </label>
      </form>
      <div class="filter-area">
        <div class="tag-selector-list" role="tablist" aria-label="文章标签筛选">
          <button
            :class="['tag-option-sm', { checked: selectedTag === '' }]"
            @click="selectedTag = ''"
            role="tab"
            :aria-selected="selectedTag === ''"
            type="button"
          >
            <span class="tag-name">全部</span>
          </button>
          <button
            v-for="tag in tags"
            :key="tag.id"
            :class="['tag-option-sm', { checked: selectedTag === tag.name }]"
            :style="selectedTag === tag.name ? { color: tag.color || '#409eff', background: (tag.color || '#409eff') + '1f', boxShadow: '0 0 0 2px ' + (tag.color || '#409eff') + '40' } : {}"
            @click="selectedTag = tag.name"
            role="tab"
            :aria-selected="selectedTag === tag.name"
            type="button"
          >
            <span class="tag-name">{{ tag.name }}</span>
          </button>
        </div>
      </div>
      <el-button v-if="isLoggedIn()" class="donate-btn" @click="router.push('/article/edit')">写文章</el-button>
    </div>
    <div v-loading="loading" class="articles">
      <ScrollObserver
        v-for="(article, index) in filteredArticles"
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
      <el-empty v-if="!loading && filteredArticles.length === 0" description="暂无文章" />
    </div>
  </div>
</template>

<style scoped>
.article-list {
  padding: 20px 40px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 16px;
  flex-wrap: wrap;
}
.search-form {
  --input-text-color: #08060d;
  --input-bg-color: transparent;
  --focus-input-bg-color: #283542;
  --text-color: #949faa;
  --active-color: #1b9bee;
  --width-of-input: 240px;
  --inline-padding-of-input: 1.2em;
  --gap: 0.9rem;
  font-size: 0.9rem;
  display: flex;
  gap: 0.5rem;
  align-items: center;
  width: var(--width-of-input);
  position: relative;
  isolation: isolate;
}
.search-form label {
  width: 100%;
  padding: 0 0.8em;
  height: 36px;
  padding-inline: var(--inline-padding-of-input);
  display: flex;
  align-items: center;
}
.search-form .fancy-bg {
  position: absolute;
  width: 100%;
  inset: 0;
  background: var(--input-bg-color);
  border: 1px solid #e5e4e7;
  border-radius: 30px;
  height: 36px;
  z-index: -1;
  pointer-events: none;
  box-shadow: rgba(0, 0, 0, 0.16) 0px 1px 4px;
}
.search-form .search-icon,
.search-form .close-btn {
  position: absolute;
}
.search-form .search-icon {
  fill: var(--text-color);
  left: var(--inline-padding-of-input);
}
.search-form .search-icon svg {
  width: 16px;
  display: block;
}
.search-form .close-btn {
  border: none;
  right: var(--inline-padding-of-input);
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
.search-form .close-btn svg {
  width: 10px;
  height: 10px;
  display: block;
}
.search-input-twitter {
  color: var(--input-text-color);
  width: 100%;
  margin-inline: min(2em, calc(var(--inline-padding-of-input) + var(--gap)));
  background: none;
  border: none;
  font-size: 0.9rem;
}
.search-input-twitter:focus {
  outline: none;
}
.search-input-twitter::placeholder {
  color: var(--text-color);
}
.search-input-twitter:focus ~ .fancy-bg {
  border: 1px solid var(--active-color);
  background: var(--focus-input-bg-color);
}
.search-input-twitter:focus ~ .search-icon {
  fill: var(--active-color);
}
.search-input-twitter:focus ~ .search-icon svg path {
  fill: var(--active-color);
}
.search-input-twitter:valid ~ .close-btn {
  opacity: 1;
  visibility: visible;
}
.search-input-twitter:-webkit-autofill,
.search-input-twitter:-webkit-autofill:hover,
.search-input-twitter:-webkit-autofill:focus,
.search-input-twitter:-webkit-autofill:active {
  -webkit-transition: color 9999s ease-out, background-color 9999s ease-out;
  -webkit-transition-delay: 9999s;
}
.filter-area {
  display: flex;
  align-items: center;
  flex: 1;
}
.tag-selector-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.tag-option-sm {
  display: inline-flex;
  align-items: center;
  height: 36px;
  padding: 0 14px;
  border-radius: 18px;
  cursor: pointer;
  transition: background-color 0.3s ease, color 0.3s ease, box-shadow 0.3s ease;
  user-select: none;
  font-size: 13px;
  background: rgba(0, 0, 0, 0.04);
  border: none;
}
.tag-option-sm:hover {
  background: rgba(64, 158, 255, 0.08);
}
.tag-option-sm.checked {
  color: #409eff;
  background: rgba(64, 158, 255, 0.12);
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.25);
}
.tag-option-sm .tag-name {
  color: #606266;
  transition: color 0.3s;
  white-space: nowrap;
}
.tag-option-sm.checked .tag-name {
  font-weight: 500;
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
.summary {
  color: #6b6375;
  margin: 0;
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
</style>
