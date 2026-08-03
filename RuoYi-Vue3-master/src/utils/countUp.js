import { ref, watch, onUnmounted } from 'vue'

export function useCountUp(target, duration = 1200) {
  const displayValue = ref(0)
  let animationFrame = null
  let startTime = null

  function animate(timestamp) {
    if (!startTime) startTime = timestamp
    const progress = Math.min((timestamp - startTime) / duration, 1)
    const eased = 1 - Math.pow(1 - progress, 3)
    displayValue.value = Math.round(eased * target.value)
    if (progress < 1) {
      animationFrame = requestAnimationFrame(animate)
    }
  }

  function start() {
    if (animationFrame) cancelAnimationFrame(animationFrame)
    startTime = null
    animationFrame = requestAnimationFrame(animate)
  }

  watch(target, () => {
    start()
  })

  onUnmounted(() => {
    if (animationFrame) cancelAnimationFrame(animationFrame)
  })

  return { displayValue, start }
}