<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  threshold: { type: Number, default: 0.2 },
  rootMargin: { type: String, default: '0px' }
})

const emit = defineEmits(['change'])

const elRef = ref(null)
const visible = ref(false)
let observer = null

onMounted(() => {
  if (!elRef.value) return
  observer = new IntersectionObserver(
    ([entry]) => {
      const isIntersecting = entry.isIntersecting
      visible.value = isIntersecting
      emit('change', isIntersecting)
    },
    { threshold: props.threshold, rootMargin: props.rootMargin }
  )
  observer.observe(elRef.value)
})

onUnmounted(() => {
  if (observer) observer.disconnect()
})
</script>

<template>
  <div ref="elRef" class="scroll-observer" :class="{ 'scroll-visible': visible }">
    <slot />
  </div>
</template>

<style scoped>
.scroll-observer {
  display: block;
}
</style>
