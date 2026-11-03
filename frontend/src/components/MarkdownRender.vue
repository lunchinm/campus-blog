<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'
import Vditor from 'vditor'

const props = defineProps({
  content: { type: String, default: '' }
})

const previewRef = ref(null)

const renderMarkdown = () => {
  if (!previewRef.value || !props.content) return
  
  Vditor.preview(previewRef.value, props.content, {
    theme: {
      current: 'light'
    },
    hljs: {
      enable: true,
      style: 'github',
      lineNumber: true
    },
    speech: {
      enable: false
    },
    anchor: true,
    cdn: 'https://unpkg.com/vditor@3.11.2',
    after: () => {
      // 代码高亮完成后可以添加额外处理
    }
  })
}

watch(() => props.content, () => {
  nextTick(() => {
    renderMarkdown()
  })
})

onMounted(() => {
  renderMarkdown()
})
</script>

<template>
  <div ref="previewRef" class="markdown-render"></div>
</template>

<style scoped>
.markdown-render {
  font-size: 16px;
  line-height: 1.8;
  color: #08060d;
}

.markdown-render :deep(h1) {
  font-size: 28px;
  font-weight: 700;
  margin: 32px 0 16px;
  color: #08060d;
  border-bottom: 2px solid #e5e4e7;
  padding-bottom: 8px;
}

.markdown-render :deep(h2) {
  font-size: 24px;
  font-weight: 600;
  margin: 28px 0 14px;
  color: #08060d;
}

.markdown-render :deep(h3) {
  font-size: 20px;
  font-weight: 600;
  margin: 24px 0 12px;
  color: #08060d;
}

.markdown-render :deep(p) {
  margin: 0 0 16px;
  text-align: justify;
}

.markdown-render :deep(strong) {
  font-weight: 700;
  color: #08060d;
}

.markdown-render :deep(em) {
  font-style: italic;
}

.markdown-render :deep(blockquote) {
  margin: 16px 0;
  padding: 12px 20px;
  border-left: 4px solid #409eff;
  background: rgba(64, 158, 255, 0.06);
  border-radius: 0 8px 8px 0;
  color: #6b6375;
}

.markdown-render :deep(blockquote p:last-child) {
  margin-bottom: 0;
}

.markdown-render :deep(ul),
.markdown-render :deep(ol) {
  margin: 0 0 16px;
  padding-left: 24px;
}

.markdown-render :deep(li) {
  margin-bottom: 6px;
}

.markdown-render :deep(a) {
  color: #409eff;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: border-color 0.2s ease;
}

.markdown-render :deep(a:hover) {
  border-bottom-color: #409eff;
}

.markdown-render :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 16px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.markdown-render :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.markdown-render :deep(th),
.markdown-render :deep(td) {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #e5e4e7;
}

.markdown-render :deep(th) {
  background: #fafafa;
  font-weight: 600;
  color: #08060d;
}

.markdown-render :deep(tr:hover) {
  background: rgba(64, 158, 255, 0.02);
}

.markdown-render :deep(hr) {
  border: none;
  border-top: 1px solid #e5e4e7;
  margin: 24px 0;
}

/* 行内代码 */
.markdown-render :deep(code:not(pre code)) {
  font-family: 'Fira Code', 'Consolas', 'Monaco', monospace;
  font-size: 14px;
  background: rgba(0, 0, 0, 0.04);
  padding: 2px 6px;
  border-radius: 4px;
  color: #d63384;
}

/* 代码块 - 使用 github 主题浅色背景 */
.markdown-render :deep(pre) {
  background: #f6f8fa;
  border-radius: 8px;
  padding: 16px;
  overflow-x: auto;
  margin: 16px 0;
}

.markdown-render :deep(pre code) {
  background: transparent;
  padding: 0;
  font-size: 14px;
  line-height: 1.6;
}

/* hljs github 主题颜色 */
.markdown-render :deep(.hljs-keyword) { color: #d73a49; font-weight: 600; }
.markdown-render :deep(.hljs-string) { color: #032f62; }
.markdown-render :deep(.hljs-function) { color: #6f42c1; }
.markdown-render :deep(.hljs-number) { color: #005cc5; }
.markdown-render :deep(.hljs-comment) { color: #6a737d; font-style: italic; }
.markdown-render :deep(.hljs-operator) { color: #d73a49; }
.markdown-render :deep(.hljs-punctuation) { color: #24292e; }
.markdown-render :deep(.hljs-variable) { color: #e36209; }
.markdown-render :deep(.hljs-title) { color: #6f42c1; }
.markdown-render :deep(.hljs-params) { color: #24292e; }
.markdown-render :deep(.hljs-built_in) { color: #005cc5; }

.markdown-render :deep(.vditor-anchor) {
  margin-right: 8px;
  color: #9ca3af;
  text-decoration: none;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.markdown-render :deep(h1:hover .vditor-anchor),
.markdown-render :deep(h2:hover .vditor-anchor),
.markdown-render :deep(h3:hover .vditor-anchor) {
  opacity: 1;
}
</style>
