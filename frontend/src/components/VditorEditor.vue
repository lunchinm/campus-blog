<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import Vditor from 'vditor'
import 'vditor/dist/index.css'
import 'highlight.js/styles/github.css'

const props = defineProps({
  modelValue: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])

const editorRef = ref(null)
let vditor = null

const initEditor = () => {
  vditor = new Vditor(editorRef.value, {
    height: 500,
    mode: 'sv',
    placeholder: '开始写作...',
    theme: 'classic',
    icon: 'material',
    toolbar: [
      'emoji',
      'headings',
      'bold',
      'italic',
      'strike',
      '|',
      'line',
      'quote',
      'list',
      'ordered-list',
      'check',
      '|',
      'code',
      'link',
      'table',
      '|',
      'upload',
      'fullscreen'
    ],
    toolbarConfig: {
      pin: true
    },
    cache: {
      enable: false
    },
    cdn: 'https://unpkg.com/vditor@3.11.2',
    upload: {
      url: '/api/upload',
      max: 2 * 1024 * 1024,
      accept: 'image/*',
      multiple: false,
      filename: (name) => name.replace(/[^(a-zA-Z0-9\u4e00-\u9fa5\.)]/g, '').replace(/[\?\\/:\|<>\*\[\]\$\^\%\#\@\!\~\+\=\,;]/g, ''),
      success: (editor, msg) => {
        const response = JSON.parse(msg)
        if (response.success && response.url) {
          vditor.insertValue(`![${response.name || 'image'}](${response.url})`)
        } else {
          ElMessage.error(response.message || '上传失败')
        }
      },
      error: (msg) => {
        ElMessage.error('图片上传失败')
      }
    },
    preview: {
      theme: {
        current: 'light'
      },
      hljs: {
        enable: true,
        style: 'github',
        lineNumber: true
      },
      markdown: {
        codeBlockPreview: true
      }
    },
    input: (value) => {
      emit('update:modelValue', value)
    },
    change: (value) => {
      emit('update:modelValue', value)
    },
    after: () => {
      if (props.modelValue) {
        vditor.setValue(props.modelValue)
      }
    }
  })
}

const getValue = () => {
  return vditor ? vditor.getValue() : ''
}

watch(() => props.modelValue, (newVal) => {
  if (vditor && vditor.getValue() !== newVal) {
    vditor.setValue(newVal)
  }
})

defineExpose({ getValue })

onMounted(() => {
  nextTick(() => {
    initEditor()
  })
})

onUnmounted(() => {
  if (vditor) {
    vditor.destroy()
    vditor = null
  }
})
</script>

<template>
  <div ref="editorRef" class="vditor-editor"></div>
</template>

<style scoped>
.vditor-editor {
  border-radius: 8px;
  width: 100%;
}

.vditor-editor :deep(.vditor) {
  border: 1px solid #e5e4e7;
  border-radius: 8px;
  width: 100% !important;
}

.vditor-editor :deep(.vditor-toolbar) {
  background: #fafafa;
  border-bottom: 1px solid #e5e4e7;
  padding: 8px 12px;
}

.vditor-editor :deep(.vditor-toolbar__item) {
  margin: 0 4px;
}

.vditor-editor :deep(.vditor-toolbar__item button) {
  color: #606266;
  padding: 6px 8px;
  border-radius: 4px;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.vditor-editor :deep(.vditor-toolbar__item button:hover) {
  background: rgba(64, 158, 255, 0.1);
  color: #409eff;
}

/* 工具栏提示不被裁剪 */
.vditor-editor :deep(.vditor-toolbar) {
  overflow: visible;
}
.vditor-editor :deep(.vditor-toolbar__item) {
  position: relative;
}

/* SV 模式编辑区 */
.vditor-editor :deep(.vditor-sv) {
  background: #fff;
  padding: 16px;
  font-size: 15px;
  line-height: 1.8;
  text-align: left;
}

/* SV 模式代码块样式 */
.vditor-editor :deep(.vditor-sv .vditor-ir__node[data-type="code-block"]) {
  background: #f6f8fa;
  border-radius: 6px;
  padding: 16px;
  margin: 8px 0;
}

.vditor-editor :deep(.vditor-sv .vditor-ir__node[data-type="code-block"] pre) {
  margin: 0;
  background: transparent;
}

.vditor-editor :deep(.vditor-sv .vditor-ir__node[data-type="code-block"] code) {
  font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
  background: transparent;
}

/* 预览区代码高亮样式 */
.vditor-editor :deep(.vditor-preview .hljs-keyword) {
  color: #d73a49;
  font-weight: 600;
}
.vditor-editor :deep(.vditor-preview .hljs-string) {
  color: #032f62;
}
.vditor-editor :deep(.vditor-preview .hljs-function) {
  color: #6f42c1;
}
.vditor-editor :deep(.vditor-preview .hljs-number) {
  color: #005cc5;
}
.vditor-editor :deep(.vditor-preview .hljs-comment) {
  color: #6a737d;
  font-style: italic;
}
.vditor-editor :deep(.vditor-preview .hljs-operator) {
  color: #d73a49;
}
.vditor-editor :deep(.vditor-preview .hljs-punctuation) {
  color: #24292e;
}
.vditor-editor :deep(.vditor-preview .hljs-variable) {
  color: #e36209;
}
.vditor-editor :deep(.vditor-preview .hljs-title) {
  color: #6f42c1;
}
.vditor-editor :deep(.vditor-preview .hljs-params) {
  color: #24292e;
}
.vditor-editor :deep(.vditor-preview .hljs-built_in) {
  color: #005cc5;
}
</style>
