<script setup>
import { ref, computed, watch, nextTick } from 'vue'

const props = defineProps({
  items: { type: Array, default: () => [] },
  showGradients: { type: Boolean, default: true },
  enableArrowNavigation: { type: Boolean, default: true },
  displayScrollbar: { type: Boolean, default: true },
  initialSelectedIndex: { type: Number, default: -1 },
  itemClassName: { type: String, default: '' },
  tag: { type: String, default: 'div' }
})

const emit = defineEmits(['itemSelect'])

const listRef = ref(null)
const selectedIndex = ref(props.initialSelectedIndex)
const keyboardNav = ref(false)
const topGradientOpacity = ref(0)
const bottomGradientOpacity = ref(1)

const visibleMap = ref(new Map())

const setVisible = (index, isVisible) => {
  visibleMap.value.set(index, isVisible)
}

const isVisible = (index) => {
  return visibleMap.value.get(index) ?? false
}

const handleItemMouseEnter = (index) => {
  selectedIndex.value = index
}

const handleItemClick = (item, index) => {
  selectedIndex.value = index
  emit('itemSelect', item, index)
}

const handleScroll = (e) => {
  const { scrollTop, scrollHeight, clientHeight } = e.target
  topGradientOpacity.value = Math.min(scrollTop / 50, 1)
  const bottomDistance = scrollHeight - (scrollTop + clientHeight)
  bottomGradientOpacity.value = scrollHeight <= clientHeight ? 0 : Math.min(bottomDistance / 50, 1)
}

const handleKeyDown = (e) => {
  if (!props.enableArrowNavigation) return
  if (e.key === 'ArrowDown' || (e.key === 'Tab' && !e.shiftKey)) {
    e.preventDefault()
    keyboardNav.value = true
    selectedIndex.value = Math.min(selectedIndex.value + 1, props.items.length - 1)
  } else if (e.key === 'ArrowUp' || (e.key === 'Tab' && e.shiftKey)) {
    e.preventDefault()
    keyboardNav.value = true
    selectedIndex.value = Math.max(selectedIndex.value - 1, 0)
  } else if (e.key === 'Enter') {
    if (selectedIndex.value >= 0 && selectedIndex.value < props.items.length) {
      e.preventDefault()
      emit('itemSelect', props.items[selectedIndex.value], selectedIndex.value)
    }
  }
}

watch(selectedIndex, async (val) => {
  if (!keyboardNav.value || val < 0 || !listRef.value) return
  await nextTick()
  const container = listRef.value
  const selectedItem = container.querySelector(`[data-index="${val}"]`)
  if (selectedItem) {
    const extraMargin = 50
    const containerScrollTop = container.scrollTop
    const containerHeight = container.clientHeight
    const itemTop = selectedItem.offsetTop
    const itemBottom = itemTop + selectedItem.offsetHeight
    if (itemTop < containerScrollTop + extraMargin) {
      container.scrollTo({ top: itemTop - extraMargin, behavior: 'smooth' })
    } else if (itemBottom > containerScrollTop + containerHeight - extraMargin) {
      container.scrollTo({ top: itemBottom - containerHeight + extraMargin, behavior: 'smooth' })
    }
  }
  keyboardNav.value = false
})

const containerClasses = computed(() => {
  return ['animated-list-container', { 'no-scrollbar': !props.displayScrollbar }]
})
</script>

<template>
  <div class="animated-list-wrapper" tabindex="0" @keydown="handleKeyDown">
    <div ref="listRef" :class="containerClasses" @scroll="handleScroll">
      <component
        :is="tag"
        v-for="(item, index) in items"
        :key="index"
        :data-index="index"
        class="animated-list-item"
        :class="[
          itemClassName,
          { selected: selectedIndex === index, visible: isVisible(index) }
        ]"
        @mouseenter="handleItemMouseEnter(index)"
        @click="handleItemClick(item, index)"
      >
        <slot :item="item" :index="index" :selected="selectedIndex === index">
          <p class="animated-list-item-text">{{ item }}</p>
        </slot>
      </component>
    </div>
    <template v-if="showGradients">
      <div class="animated-list-gradient top" :style="{ opacity: topGradientOpacity }"></div>
      <div class="animated-list-gradient bottom" :style="{ opacity: bottomGradientOpacity }"></div>
    </template>
  </div>
</template>

<style scoped>
.animated-list-wrapper {
  position: relative;
  outline: none;
}
.animated-list-container {
  max-height: 60vh;
  overflow-y: auto;
  padding: 8px 0;
}
.animated-list-container.no-scrollbar {
  scrollbar-width: none;
}
.animated-list-container.no-scrollbar::-webkit-scrollbar {
  display: none;
}
.animated-list-item {
  margin-bottom: 1rem;
  cursor: pointer;
  opacity: 0;
  transform: scale(0.7);
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.animated-list-item.visible {
  opacity: 1;
  transform: scale(1);
}
.animated-list-item.selected {
  background: rgba(27, 155, 238, 0.08);
  border-radius: 8px;
}
.animated-list-item-text {
  margin: 0;
  padding: 8px 12px;
}
.animated-list-gradient {
  position: absolute;
  left: 0;
  right: 0;
  height: 40px;
  pointer-events: none;
  transition: opacity 0.2s ease;
}
.animated-list-gradient.top {
  top: 0;
  background: linear-gradient(to bottom, rgba(255,255,255,0.95), transparent);
}
.animated-list-gradient.bottom {
  bottom: 0;
  background: linear-gradient(to top, rgba(255,255,255,0.95), transparent);
}
</style>
