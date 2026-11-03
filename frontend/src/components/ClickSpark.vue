<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  sparkColor: { type: String, default: '#fff' },
  sparkSize: { type: Number, default: 10 },
  sparkRadius: { type: Number, default: 15 },
  sparkCount: { type: Number, default: 8 },
  duration: { type: Number, default: 400 }
})

const sparkCanvas = ref(null)
const sparks = []
let animId = null
let ro = null
let resizeTimeout = null

const easeFunc = (t) => t * (2 - t)

const resizeCanvas = () => {
  const canvas = sparkCanvas.value
  if (!canvas) return
  const parent = canvas.parentElement
  if (!parent) return
  const { width, height } = parent.getBoundingClientRect()
  if (canvas.width !== width || canvas.height !== height) {
    canvas.width = width
    canvas.height = height
  }
}

const draw = (timestamp) => {
  const canvas = sparkCanvas.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  ctx.clearRect(0, 0, canvas.width, canvas.height)

  for (let i = sparks.length - 1; i >= 0; i--) {
    const spark = sparks[i]
    const elapsed = timestamp - spark.startTime
    if (elapsed >= props.duration) {
      sparks.splice(i, 1)
      continue
    }

    const progress = elapsed / props.duration
    const eased = easeFunc(progress)
    const distance = eased * props.sparkRadius
    const lineLength = props.sparkSize * (1 - eased)

    const x1 = spark.x + distance * Math.cos(spark.angle)
    const y1 = spark.y + distance * Math.sin(spark.angle)
    const x2 = spark.x + (distance + lineLength) * Math.cos(spark.angle)
    const y2 = spark.y + (distance + lineLength) * Math.sin(spark.angle)

    ctx.strokeStyle = props.sparkColor
    ctx.lineWidth = 2
    ctx.beginPath()
    ctx.moveTo(x1, y1)
    ctx.lineTo(x2, y2)
    ctx.stroke()
  }

  animId = requestAnimationFrame(draw)
}

const handleClick = (e) => {
  const canvas = sparkCanvas.value
  if (!canvas) return
  const rect = canvas.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top

  const now = performance.now()
  for (let i = 0; i < props.sparkCount; i++) {
    sparks.push({
      x,
      y,
      angle: (2 * Math.PI * i) / props.sparkCount,
      startTime: now
    })
  }
}

onMounted(() => {
  const canvas = sparkCanvas.value
  if (!canvas) return
  const parent = canvas.parentElement
  if (!parent) return

  resizeCanvas()

  ro = new ResizeObserver(() => {
    clearTimeout(resizeTimeout)
    resizeTimeout = setTimeout(resizeCanvas, 100)
  })
  ro.observe(parent)

  animId = requestAnimationFrame(draw)
})

onUnmounted(() => {
  if (animId) cancelAnimationFrame(animId)
  if (ro) ro.disconnect()
  clearTimeout(resizeTimeout)
})
</script>

<template>
  <div class="click-spark-wrapper" @click="handleClick">
    <canvas ref="sparkCanvas" class="spark-canvas"></canvas>
    <slot></slot>
  </div>
</template>

<style scoped>
.click-spark-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
}
.spark-canvas {
  width: 100%;
  height: 100%;
  display: block;
  user-select: none;
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: none;
  z-index: 9999;
}
</style>
