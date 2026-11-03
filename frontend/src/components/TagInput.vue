<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  predefinedTags: { type: Array, default: () => [] },
  placeholder: { type: String, default: '输入标签，按回车添加...' }
})

const emit = defineEmits(['update:modelValue'])

const inputValue = ref('')
const isFocused = ref(false)

const selectedTags = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const availablePredefined = computed(() => {
  return props.predefinedTags.filter(tag => !selectedTags.value.some(t => t.name === tag.name))
})

const showDropdown = computed(() => {
  return isFocused.value && (availablePredefined.value.length > 0 || inputValue.value.trim())
})

const addTag = (tagName, tagColor) => {
  const trimmed = tagName.trim()
  if (!trimmed) return
  if (selectedTags.value.some(t => t.name === trimmed)) return

  const isPredefined = props.predefinedTags.some(t => t.name === trimmed)
  const color = tagColor || '#909399'
  selectedTags.value = [...selectedTags.value, { name: trimmed, type: isPredefined ? 0 : 1, color }]
  inputValue.value = ''
}

const removeTag = (index) => {
  const newTags = [...selectedTags.value]
  newTags.splice(index, 1)
  selectedTags.value = newTags
}

const handleKeydown = (e) => {
  if (e.key === 'Enter') {
    e.preventDefault()
    addTag(inputValue.value, null)
  } else if (e.key === 'Backspace' && !inputValue.value && selectedTags.value.length > 0) {
    removeTag(selectedTags.value.length - 1)
  }
}

const handleBlur = () => {
  setTimeout(() => { isFocused.value = false }, 200)
}
</script>

<template>
  <div class="tag-input-wrapper">
    <div
      class="tag-input-container"
      :class="{ focused: isFocused }"
      @click="$refs.inputRef.focus()"
    >
      <span
        v-for="(tag, index) in selectedTags"
        :key="tag.name"
        class="tag-chip"
        :style="{
          background: (tag.color || '#909399') + '1a',
          color: tag.color || '#909399',
          borderColor: (tag.color || '#909399') + '4d'
        }"
      >
        {{ tag.name }}
        <button class="tag-remove" type="button" @click.stop="removeTag(index)">
          <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
            <path d="M3 3L9 9M9 3L3 9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </button>
      </span>
      <input
        ref="inputRef"
        v-model="inputValue"
        class="tag-input"
        :placeholder="selectedTags.length === 0 ? placeholder : ''"
        @focus="isFocused = true"
        @blur="handleBlur"
        @keydown="handleKeydown"
      />
    </div>

    <transition name="dropdown">
      <div v-if="showDropdown" class="tag-dropdown">
        <div v-if="availablePredefined.length > 0" class="dropdown-section">
          <div class="dropdown-label">预定义标签</div>
          <div class="dropdown-tags">
            <button
              v-for="tag in availablePredefined"
              :key="tag.name"
              class="dropdown-tag"
              :style="{ color: tag.color || '#409eff', background: (tag.color || '#409eff') + '0f', borderColor: (tag.color || '#409eff') + '33' }"
              type="button"
              @click="addTag(tag.name, tag.color)"
            >
              {{ tag.name }}
            </button>
          </div>
        </div>
        <div v-if="inputValue.trim() && !selectedTags.some(t => t.name === inputValue.trim())" class="dropdown-section">
          <button
            class="dropdown-tag"
            :style="{ color: '#909399', background: '#9093990f', borderColor: '#90939933' }"
            type="button"
            @click="addTag(inputValue, null)"
          >
            + 添加自定义标签 "{{ inputValue.trim() }}"
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.tag-input-wrapper {
  position: relative;
  width: 100%;
}

.tag-input-container {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  min-height: 44px;
  border: 1px solid #e5e4e7;
  border-radius: 8px;
  background: #fff;
  cursor: text;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
}

.tag-input-container.focused {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
}

.tag-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid transparent;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  animation: chipIn 0.2s ease;
}

@keyframes chipIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.tag-chip:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.tag-remove {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  border: none;
  background: transparent;
  cursor: pointer;
  border-radius: 50%;
  transition: background-color 0.2s ease;
  padding: 0;
}

.tag-remove:hover {
  background: rgba(0, 0, 0, 0.08);
}

.tag-input {
  flex: 1;
  min-width: 120px;
  border: none;
  outline: none;
  font-size: 14px;
  color: #08060d;
  background: transparent;
  padding: 4px 0;
}

.tag-input::placeholder {
  color: #9ca3af;
}

.tag-dropdown {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  background: #fff;
  border: 1px solid #e5e4e7;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  padding: 8px;
  z-index: 100;
  max-height: 200px;
  overflow-y: auto;
}

.dropdown-section {
  margin-bottom: 8px;
}

.dropdown-section:last-child {
  margin-bottom: 0;
}

.dropdown-label {
  font-size: 12px;
  color: #9ca3af;
  padding: 4px 8px;
  margin-bottom: 4px;
}

.dropdown-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.dropdown-tag {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.2s ease;
  background: transparent;
}

.dropdown-tag:hover {
  transform: translateY(-1px);
}

.dropdown-tag:nth-of-type(n+2) {
  width: auto;
}
.dropdown-tag:last-of-type {
  width: 100%;
  justify-content: flex-start;
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
