<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { addArticle, updateArticle, getArticleDetail } from '@/api/article.js'
import { saveDraft, updateDraft, getDraft } from '@/api/draft.js'
import TagInput from '@/components/TagInput.vue'
import VditorEditor from '@/components/VditorEditor.vue'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id
const isDraftEdit = !!route.query.draft
const form = ref({ title: '', summary: '', content: '', tags: [] })
const loading = ref(false)
const draftSaving = ref(false)
const draftId = ref(null)
const editorRef = ref(null)
const cancelled = ref(false)
let autoSaveTimer = null

const getEditorContent = () => {
  return editorRef.value ? editorRef.value.getValue() : form.value.content
}

const doSaveDraft = async () => {
  const content = getEditorContent()
  if (!form.value.title && !content) return
  const draftData = {
    title: form.value.title,
    summary: form.value.summary,
    content: content,
    tags: JSON.stringify(form.value.tags || [])
  }
  if (draftId.value) {
    await updateDraft(draftId.value, draftData)
  } else {
    const res = await saveDraft(draftData)
    if (res.id) {
      draftId.value = res.id
    }
  }
}

const saveToDraft = async () => {
  draftSaving.value = true
  try {
    await doSaveDraft()
    ElMessage.success('草稿已保存')
    router.push('/drafts')
  } catch (error) {
    ElMessage.error('草稿保存失败')
  } finally {
    draftSaving.value = false
  }
}

const startAutoSave = () => {
  stopAutoSave()
  autoSaveTimer = setInterval(async () => {
    if (cancelled.value) return
    try {
      await doSaveDraft()
    } catch (error) {
    }
  }, 30000)
}

const stopAutoSave = () => {
  if (autoSaveTimer) {
    clearInterval(autoSaveTimer)
    autoSaveTimer = null
  }
}

const handleCancel = () => {
  cancelled.value = true
  stopAutoSave()
  router.back()
}

const predefinedTags = ref([
  { name: '技术', type: 0, color: '#409eff' },
  { name: '生活', type: 0, color: '#67c23a' },
  { name: '学习', type: 0, color: '#e6a23c' },
  { name: '随笔', type: 0, color: '#9b59b6' },
  { name: '旅游', type: 0, color: '#00bcd4' },
  { name: '美食', type: 0, color: '#e91e63' },
  { name: '其他', type: 0, color: '#909399' }
])

const fetchArticle = async () => {
  if (!isEdit) return
  loading.value = true
  try {
    const res = await getArticleDetail(route.params.id)
    if (res.success === false) {
      ElMessage.error(res.message)
      return
    }
    form.value = {
      title: res.title || '',
      summary: res.summary || '',
      content: res.content || '',
      tags: res.tags || []
    }
  } catch (error) {
    ElMessage.error('获取文章失败')
  } finally {
    loading.value = false
  }
}

const fetchDraft = async () => {
  if (!isDraftEdit) return
  loading.value = true
  try {
    const draftIdParam = route.query.draft
    const res = await getDraft(draftIdParam)
    if (res.success === false) {
      ElMessage.error(res.message)
      return
    }
    draftId.value = Number(draftIdParam)
    form.value = {
      title: res.title || '',
      summary: res.summary || '',
      content: res.content || '',
      tags: res.tags ? (typeof res.tags === 'string' ? JSON.parse(res.tags) : res.tags) : []
    }
  } catch (error) {
    ElMessage.error('获取草稿失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  const content = getEditorContent()
  console.log('保存 - 标题:', form.value.title)
  console.log('保存 - 内容长度:', content?.length)
  console.log('保存 - 内容前100字符:', content?.substring(0, 100))
  if (!form.value.title || !content) {
    ElMessage.warning('请填写标题和内容')
    return
  }
  loading.value = true
  try {
    const submitData = {
      title: form.value.title,
      summary: form.value.summary,
      content: content,
      tags: form.value.tags
    }
    let res
    if (isEdit) {
      res = await updateArticle({ ...submitData, id: Number(route.params.id) })
    } else {
      res = await addArticle(submitData)
    }
    console.log('保存响应:', res)
    if (res.success) {
      stopAutoSave()
      ElMessage.success(isEdit ? '更新成功' : '发布成功')
      router.push(`/article/${isEdit ? route.params.id : res.id}`)
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    console.error('保存错误:', error)
    ElMessage.error('操作失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (isEdit) {
    fetchArticle()
  } else if (isDraftEdit) {
    fetchDraft()
    startAutoSave()
  } else {
    startAutoSave()
  }
})

onUnmounted(() => {
  stopAutoSave()
})

watch(() => route.params.id, (newId, oldId) => {
  if (newId !== oldId) {
    fetchArticle()
  }
})
</script>

<template>
  <div class="article-edit">
    <h2>{{ isEdit ? '编辑文章' : '写文章' }}</h2>
    <el-form :model="form" label-width="60px" @submit.prevent="handleSubmit">
      <el-form-item label="标题">
        <el-input v-model="form.title" name="title" autocomplete="off" spellcheck="false" placeholder="请输入文章标题" />
      </el-form-item>
      <el-form-item label="标签">
        <TagInput v-model="form.tags" :predefined-tags="predefinedTags" />
      </el-form-item>
      <el-form-item label="摘要">
        <el-input v-model="form.summary" name="summary" type="textarea" :rows="2" autocomplete="off" placeholder="请输入文章摘要（选填）" />
      </el-form-item>
      <el-form-item label="内容" class="content-item">
        <VditorEditor ref="editorRef" v-model="form.content" />
      </el-form-item>
      <el-form-item>
        <el-button class="donate-btn" type="primary" native-type="submit" :loading="loading">{{ isEdit ? '保存' : '发布' }}</el-button>
        <el-button v-if="!isEdit" class="neu-btn" type="button" :loading="draftSaving" @click="saveToDraft">保存草稿</el-button>
        <el-button class="neu-btn" type="button" @click="handleCancel">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
.article-edit {
  padding: 20px 40px;
}
h2 {
  margin-bottom: 30px;
}
/* 内容项占满宽度 */
.content-item :deep(.el-form-item__content) {
  width: 100%;
  display: block;
}
</style>
