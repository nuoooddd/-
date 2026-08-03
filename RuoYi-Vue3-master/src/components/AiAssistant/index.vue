<template>

  <Teleport to="body">
    <div class="ai-fab" v-show="!isOpen" :class="{ dragging: isDragging }" :style="fabStyle"
      @mousedown.prevent="onDragStart"
      @touchstart="onDragStart"
    >
      <el-icon :size="28"><ChatDotRound /></el-icon>
      <div class="fab-pulse"></div>
    </div>
  </Teleport>

  <Teleport to="body">
    <transition name="chat-slide">
      <div class="chat-panel" v-if="isOpen">
        <div class="chat-header">
          <div class="header-left">
            <div class="ai-avatar">🤖</div>
            <div>
              <div class="ai-name">免申即享智能助手</div>
              <div class="ai-status">在线 · 政策咨询 · 规则解读</div>
            </div>
          </div>
          <el-button text @click="clearSession" title="清空对话"><el-icon><Delete /></el-icon></el-button>
          <el-button text @click="isOpen = false" title="关闭"><el-icon><Close /></el-icon></el-button>
        </div>

        <div class="chat-messages" ref="messagesRef">
          <div class="msg-row" v-for="(msg, i) in messages" :key="i" :class="msg.role">
            <div class="msg-avatar" v-if="msg.role === 'assistant'">🤖</div>
            <div class="msg-bubble" v-html="renderMarkdown(msg.content)"></div>
          </div>
          <div class="msg-row assistant" v-if="loading && !streamingContent">
            <div class="msg-avatar">🤖</div>
            <div class="msg-bubble typing">
              <span class="dot"></span><span class="dot"></span><span class="dot"></span>
            </div>
          </div>
        </div>

        <div class="quick-questions" v-if="messages.length <= 1">
          <div class="quick-q" v-for="q in quickQuestions" :key="q" @click="sendMessage(q)">{{ q }}</div>
        </div>

        <div class="chat-input">
          <el-input
            v-model="inputText"
            placeholder="输入您的问题，如：我能享受哪些政策？"
            @keyup.enter="sendMessage(inputText)"
            :disabled="loading"
            clearable
          >
            <template #append>
              <el-button :icon="Promotion" @click="sendMessage(inputText)" :loading="loading" type="primary" />
            </template>
          </el-input>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script setup>
import { ref, reactive, computed, nextTick, onMounted, onUnmounted } from 'vue'
import { getToken } from '@/utils/auth'
import { Promotion, ChatDotRound, Delete, Close } from '@element-plus/icons-vue'

const isOpen = ref(false)
const isDragging = ref(false)
const inputText = ref('')
const loading = ref(false)
const streamingContent = ref('')
const messagesRef = ref(null)
const messages = ref([
  { role: 'assistant', content: '👋 您好！我是免申即享智能助手，可以为您解答政策补贴、匹配规则、风控审核等问题。请问有什么可以帮您？' }
])

const quickQuestions = [
  '有哪些补贴政策？',
  '高新技术企业能享受什么？',
  '残疾人有什么补助？',
  '稳岗奖励怎么申请？',
  '绿色制造有什么优惠？'
]

// ========== 悬浮球拖拽 ==========
const FAB_SIZE = 56
const DRAG_THRESHOLD = 6

const pos = reactive({ x: 0, y: 0 })
const dragState = reactive({ startX: 0, startY: 0, startPosX: 0, startPosY: 0, moved: false })

function getDefaultPos() {
  const isMobile = window.innerWidth < 992
  return {
    x: Math.max(0, window.innerWidth - FAB_SIZE - 16),
    y: Math.max(0, window.innerHeight - FAB_SIZE - (isMobile ? 80 : 24))
  }
}

const saved = localStorage.getItem('ai-fab-pos')
if (saved) {
  try { const p = JSON.parse(saved); pos.x = p.x; pos.y = p.y } catch {}
}
if (pos.x === 0 && pos.y === 0) {
  const def = getDefaultPos()
  pos.x = def.x; pos.y = def.y
}

onMounted(() => {
  window.addEventListener('resize', onResize)
  const clamped = clampPos(pos.x, pos.y)
  const isMobile = window.innerWidth < 992
  if (isMobile && clamped.y > window.innerHeight - 80) {
    clamped.y = window.innerHeight - 80
  }
  pos.x = clamped.x; pos.y = clamped.y
})

const fabStyle = computed(() => ({
  left: pos.x + 'px',
  top: pos.y + 'px',
  position: 'fixed'
}))

function clampPos(x, y) {
  return {
    x: Math.max(0, Math.min(window.innerWidth - FAB_SIZE, x)),
    y: Math.max(0, Math.min(window.innerHeight - FAB_SIZE, y))
  }
}

function snapToEdge(x) {
  const center = window.innerWidth / 2
  return x < center ? 16 : window.innerWidth - FAB_SIZE - 16
}

function getEventPos(e) {
  if (e.touches && e.touches.length > 0) return { x: e.touches[0].clientX, y: e.touches[0].clientY }
  return { x: e.clientX, y: e.clientY }
}

function onDragStart(e) {

  const p = getEventPos(e)
  dragState.startX = p.x
  dragState.startY = p.y
  dragState.startPosX = pos.x
  dragState.startPosY = pos.y
  dragState.moved = false
  isDragging.value = false

  document.addEventListener('mousemove', onDragMove)
  document.addEventListener('mouseup', onDragEnd)
  document.addEventListener('touchmove', onDragMove, { passive: false })
  document.addEventListener('touchend', onDragEnd)
}

function onDragMove(e) {
  if (e.cancelable) e.preventDefault()
  const p = getEventPos(e)
  const dx = p.x - dragState.startX
  const dy = p.y - dragState.startY
  if (Math.abs(dx) < DRAG_THRESHOLD && Math.abs(dy) < DRAG_THRESHOLD) return
  dragState.moved = true
  isDragging.value = true
  const clamped = clampPos(dragState.startPosX + dx, dragState.startPosY + dy)
  pos.x = clamped.x
  pos.y = clamped.y
}

function onDragEnd() {
  document.removeEventListener('mousemove', onDragMove)
  document.removeEventListener('mouseup', onDragEnd)
  document.removeEventListener('touchmove', onDragMove)
  document.removeEventListener('touchend', onDragEnd)
  isDragging.value = false
  if (dragState.moved) {
    pos.x = snapToEdge(pos.x)
    pos.y = clampPos(pos.x, pos.y).y
    localStorage.setItem('ai-fab-pos', JSON.stringify({ x: pos.x, y: pos.y }))
  } else {
    toggleChat()
  }
}

function onResize() {
  const clamped = clampPos(pos.x, pos.y)
  pos.x = clamped.x
  pos.y = clamped.y
}


onUnmounted(() => window.removeEventListener('resize', onResize))
// ========== 悬浮球拖拽 END ==========

function toggleChat() { isOpen.value = !isOpen.value }

function renderMarkdown(text) {
  if (!text) return ''
  return text
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br>')
}

async function sendMessage(text) {
  if (!text || !text.trim() || loading.value) return
  const msg = text.trim()
  inputText.value = ''
  messages.value.push({ role: 'user', content: msg })
  loading.value = true
  streamingContent.value = ''
  await scrollToBottom()

  const assistantMsg = { role: 'assistant', content: '' }
  messages.value.push(assistantMsg)

  try {
    const baseURL = import.meta.env.VITE_APP_BASE_API
    const token = getToken()
    const response = await fetch(baseURL + '/biz/ai/chat/stream', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
      body: JSON.stringify({ message: msg })
    })

    if (!response.ok) {
      throw new Error('SSE failed: ' + response.status)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    let currentEvent = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        if (line.startsWith('event:')) {
          currentEvent = line.substring(6).trim()
          continue
        }
        if (!line.startsWith('data:')) continue
        const data = line.substring(5).trim()
        if (!data) continue

        if (currentEvent === 'token') {
          assistantMsg.content += data
          streamingContent.value = assistantMsg.content
          await scrollToBottom()
        } else if (currentEvent === 'done') {
          if (data && !assistantMsg.content) {
            assistantMsg.content = data
          }
        } else if (currentEvent === 'error') {
          if (!assistantMsg.content) {
            assistantMsg.content = '⚠️ ' + data
          }
        } else {
          // plain data fallback
          if (data !== '[DONE]') {
            assistantMsg.content += data
            streamingContent.value = assistantMsg.content
            await scrollToBottom()
          }
        }
        currentEvent = ''
      }
    }

    if (!assistantMsg.content) {
      assistantMsg.content = '暂未收到回复，请重试'
    }
  } catch (e) {
    // Fallback to keyword matching
    if (!assistantMsg.content) {
      try {
        const baseURL = import.meta.env.VITE_APP_BASE_API
        const token = getToken()
        const fallbackResp = await fetch(baseURL + '/ai/ask', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
          body: JSON.stringify({ question: msg })
        })
        const fallbackResult = await fallbackResp.json()
        if (fallbackResult.code === 200 && fallbackResult.data) {
          assistantMsg.content = fallbackResult.data.answer || '暂无相关信息'
        } else {
          assistantMsg.content = '⚠️ ' + (fallbackResult.msg || '服务异常')
        }
      } catch (fallbackErr) {
        assistantMsg.content = '⚠️ 服务暂时不可用，请稍后再试'
      }
    }
  }

  streamingContent.value = ''
  loading.value = false
  await scrollToBottom()
}

async function clearSession() {
  try {
    const baseURL = import.meta.env.VITE_APP_BASE_API
    const token = getToken()
    await fetch(baseURL + '/biz/ai/session', { method: 'DELETE', headers: { 'Authorization': 'Bearer ' + token } })
  } catch {}
  messages.value = [{ role: 'assistant', content: '👋 对话已清空，请问有什么可以帮您？' }]
}

async function scrollToBottom() {
  await nextTick()
  if (messagesRef.value) messagesRef.value.scrollTop = messagesRef.value.scrollHeight
}
</script>

<style>

.ai-fab {
  position: fixed; z-index: 9999;
  width: 56px; height: 56px; border-radius: 50%; background: linear-gradient(135deg, #1890ff, #722ed1);
  display: flex; align-items: center; justify-content: center; color: #fff; cursor: grab;
  box-shadow: 0 4px 16px rgba(24,144,255,0.4); transition: box-shadow 0.3s, background 0.3s, opacity 0.2s;
  user-select: none; -webkit-user-select: none; touch-action: none;
}
.ai-fab:active { cursor: grabbing; }
.ai-fab:hover { box-shadow: 0 6px 24px rgba(24,144,255,0.5); }
.ai-fab.active { background: #909399; box-shadow: none; }
.ai-fab.dragging { opacity: 0.85; transform: scale(1.08); box-shadow: 0 8px 28px rgba(24,144,255,0.55); transition: none; }
.fab-pulse {
  position: absolute; inset: -4px; border-radius: 50%; border: 2px solid rgba(24,144,255,0.4);
  animation: fab-pulse 2s infinite;
}
@keyframes fab-pulse { 0%{transform:scale(1);opacity:1} 100%{transform:scale(1.4);opacity:0} }

.chat-panel {
  position: fixed; z-index: 9999; bottom: 90px; right: 24px; width: 420px; height: 560px;
  background: #fff; border-radius: 16px; box-shadow: 0 12px 48px rgba(0,0,0,0.15);
  display: flex; flex-direction: column; overflow: hidden; border: 1px solid #e8e8e8;
  pointer-events: auto;
}

@media (max-width: 768px) {
  .chat-panel {
    bottom: 60px; right: 8px; left: 8px; width: auto; height: 70vh; border-radius: 12px;
  }
  .ai-fab {
    width: 48px; height: 48px;
  }
  .ai-fab .el-icon { font-size: 22px; }
}

.chat-header {
  padding: 16px 20px; background: linear-gradient(135deg, #1890ff, #722ed1); color: #fff;
  display: flex; justify-content: space-between; align-items: center; flex-shrink: 0;
}
.header-left { display: flex; align-items: center; gap: 12px; }
.ai-avatar { font-size: 28px; }
.ai-name { font-size: 15px; font-weight: 600; }
.ai-status { font-size: 11px; opacity: 0.8; margin-top: 2px; }
.chat-header .el-button { color: rgba(255,255,255,0.7); }
.chat-header .el-button:hover { color: #fff; }

.chat-messages {
  flex: 1; overflow-y: auto; padding: 16px 20px; display: flex; flex-direction: column; gap: 14px;
  background: #f9f9fb;
}
.msg-row { display: flex; gap: 8px; max-width: 90%; }
.msg-row.user { align-self: flex-end; flex-direction: row-reverse; }
.msg-avatar { font-size: 20px; flex-shrink: 0; margin-top: 2px; }
.msg-bubble {
  padding: 10px 14px; border-radius: 12px; font-size: 13px; line-height: 1.7; word-break: break-word;
}
.msg-row.assistant .msg-bubble { background: #fff; color: #1a1a1a; border: 1px solid #e8e8e8; }
.msg-row.user .msg-bubble { background: linear-gradient(135deg, #1890ff, #36cfc9); color: #fff; }

.typing { display: flex; gap: 4px; padding: 14px 18px; }
.dot { width: 8px; height: 8px; border-radius: 50%; background: #bfbfbf; animation: typing-dot 1.4s infinite; }
.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes typing-dot { 0%,60%,100%{transform:translateY(0)} 30%{transform:translateY(-6px)} }

.quick-questions {
  padding: 8px 20px; display: flex; flex-wrap: wrap; gap: 8px; flex-shrink: 0;
  border-top: 1px solid #f0f0f0; background: #fff;
}
.quick-q {
  padding: 6px 12px; border-radius: 16px; font-size: 12px; color: #1890ff;
  background: #e6f7ff; border: 1px solid #91d5ff; cursor: pointer; transition: all 0.2s;
}
.quick-q:hover { background: #1890ff; color: #fff; }

.chat-input { padding: 12px 16px; border-top: 1px solid #f0f0f0; flex-shrink: 0; background: #fff; }
.chat-input :deep(.el-input-group__append) { padding: 0; }
.chat-input :deep(.el-input-group__append .el-button) { border-radius: 0 8px 8px 0; }

.chat-slide-enter-active, .chat-slide-leave-active { transition: all 0.3s ease; }
.chat-slide-enter-from, .chat-slide-leave-to { opacity: 0; transform: translateY(20px) scale(0.95); }
</style>