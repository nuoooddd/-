<template>
  <div class="ai-page">
    <div class="ai-sidebar">
      <div class="sidebar-header">
        <div class="sidebar-brand">
          <div class="brand-dot"></div>
          <span>对话历史</span>
        </div>
        <el-button text size="small" @click="newSession" title="新建对话">
          <el-icon><Plus /></el-icon>
        </el-button>
      </div>
      <div class="session-list">
        <div v-for="s in sessionList" :key="s.id" class="session-item" :class="{ active: s.id === currentSessionId }" @click="switchSession(s)">
          <el-icon size="14"><ChatDotRound /></el-icon>
          <span class="session-title">{{ s.title }}</span>
        </div>
        <div v-if="sessionList.length === 0" class="session-empty">暂无对话记录</div>
      </div>
      <div class="sidebar-footer">
        <div class="ai-model-tag">
          <el-icon><Cpu /></el-icon>
          <span>智谱GLM-5.1</span>
        </div>
      </div>
    </div>

    <div class="ai-main">
      <div class="main-header">
        <div class="header-info">
          <h3>免申即享智能助手</h3>
          <span class="header-desc">政策解读 · 规则匹配 · 补贴查询 · 流程指引</span>
        </div>
        <el-button text @click="clearCurrentSession"><el-icon><Delete /></el-icon> 清空对话</el-button>
      </div>

      <div class="chat-area" ref="chatAreaRef">
        <div v-if="messages.length <= 1" class="welcome-section">
          <div class="welcome-icon">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
              <rect width="48" height="48" rx="14" fill="#0052ff"/>
              <path d="M16 24C16 19.58 19.58 16 24 16C28.42 16 32 19.58 32 24" stroke="white" stroke-width="2.5" stroke-linecap="round"/>
              <circle cx="20" cy="26" r="2" fill="white"/>
              <circle cx="28" cy="26" r="2" fill="white"/>
              <path d="M20 32C20 32 22 34 24 34C26 34 28 32 28 32" stroke="white" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <h2>您好，我是免申即享智能助手</h2>
          <p>我可以帮您解答政策补贴、匹配规则、风控审核等问题</p>
          <div class="quick-grid">
            <div class="quick-card" v-for="q in quickQuestions" :key="q.text" @click="sendMessage(q.text)">
              <el-icon :size="20" :color="q.color"><component :is="q.icon" /></el-icon>
              <div class="qc-text">{{ q.text }}</div>
            </div>
          </div>
        </div>

        <template v-for="(msg, i) in messages" :key="i">
          <div v-if="i > 0 || messages.length > 1" class="msg-row" :class="msg.role">
            <div class="msg-avatar" v-if="msg.role === 'assistant'">
              <div class="avatar-ai">AI</div>
            </div>
            <div class="msg-content">
              <div class="msg-bubble" v-html="renderMarkdown(msg.content)"></div>
            </div>
          </div>
        </template>

        <div class="msg-row assistant" v-if="loading && !streamingContent">
          <div class="msg-avatar"><div class="avatar-ai">AI</div></div>
          <div class="msg-content">
            <div class="msg-bubble typing">
              <span class="dot"></span><span class="dot"></span><span class="dot"></span>
            </div>
          </div>
        </div>
      </div>

      <div class="input-area">
        <div class="input-box">
          <el-input
            v-model="inputText"
            type="textarea"
            :autosize="{ minRows: 1, maxRows: 4 }"
            placeholder="输入您的问题，如：我能享受哪些政策？"
            @keydown.enter.exact.prevent="sendMessage(inputText)"
            :disabled="loading"
            resize="none"
          />
          <el-button type="primary" :icon="Promotion" circle @click="sendMessage(inputText)" :loading="loading" :disabled="!inputText.trim()" class="send-btn" />
        </div>
        <div class="input-hint">按 Enter 发送，Shift+Enter 换行</div>
      </div>
    </div>
  </div>
</template>

<script setup name="AiAssistant">
import { ref, reactive, nextTick, onMounted } from 'vue'
import { getToken } from '@/utils/auth'
import { Promotion, Plus, Delete, ChatDotRound, Cpu, Memo, Money, Opportunity, Checked } from '@element-plus/icons-vue'

const inputText = ref('')
const loading = ref(false)
const streamingContent = ref('')
const chatAreaRef = ref(null)
const currentSessionId = ref('1')

const sessionList = ref([
  { id: '1', title: '政策咨询对话' }
])

const messages = ref([
  { role: 'assistant', content: '您好！我是免申即享智能助手，可以为您解答政策补贴、匹配规则、风控审核等问题。请问有什么可以帮您？' }
])

const quickQuestions = [
  { text: '有哪些补贴政策可以申请？', icon: 'Memo', color: '#0052ff' },
  { text: '高新技术企业能享受什么？', icon: 'Opportunity', color: '#10b981' },
  { text: '残疾人有什么补助政策？', icon: 'Checked', color: '#f59e0b' },
  { text: '稳岗奖励怎么申请？', icon: 'Money', color: '#8b5cf6' }
]

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

    if (!response.ok) throw new Error('SSE failed: ' + response.status)

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
          if (data && !assistantMsg.content) assistantMsg.content = data
        } else if (currentEvent === 'error') {
          if (!assistantMsg.content) assistantMsg.content = data
        } else {
          if (data !== '[DONE]') {
            assistantMsg.content += data
            streamingContent.value = assistantMsg.content
            await scrollToBottom()
          }
        }
        currentEvent = ''
      }
    }

    if (!assistantMsg.content) assistantMsg.content = '暂未收到回复，请重试'
  } catch (e) {
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
          assistantMsg.content = fallbackResult.msg || '服务异常'
        }
      } catch {
        assistantMsg.content = '服务暂时不可用，请稍后再试'
      }
    }
  }

  streamingContent.value = ''
  loading.value = false
  await scrollToBottom()
}

function newSession() {
  const id = String(sessionList.value.length + 1)
  sessionList.value.unshift({ id, title: '新对话' })
  currentSessionId.value = id
  messages.value = [{ role: 'assistant', content: '您好！请问有什么可以帮您？' }]
}

function switchSession(s) {
  currentSessionId.value = s.id
}

async function clearCurrentSession() {
  try {
    const baseURL = import.meta.env.VITE_APP_BASE_API
    const token = getToken()
    await fetch(baseURL + '/biz/ai/session', { method: 'DELETE', headers: { 'Authorization': 'Bearer ' + token } })
  } catch {}
  messages.value = [{ role: 'assistant', content: '对话已清空，请问有什么可以帮您？' }]
}

async function scrollToBottom() {
  await nextTick()
  if (chatAreaRef.value) chatAreaRef.value.scrollTop = chatAreaRef.value.scrollHeight
}

onMounted(() => { scrollToBottom() })
</script>

<style lang="scss" scoped>
.ai-page {
  display: flex; height: calc(100vh - 84px); background: #fff;
  border-radius: 12px; overflow: hidden; border: 1px solid #e2e8f0;
  font-family: "Plus Jakarta Sans", "PingFang SC", "Microsoft YaHei UI", sans-serif;
}

.ai-sidebar {
  width: 240px; background: #f8fafc; border-right: 1px solid #e2e8f0;
  display: flex; flex-direction: column; flex-shrink: 0;
}
.sidebar-header {
  padding: 16px; display: flex; justify-content: space-between; align-items: center;
  border-bottom: 1px solid #e2e8f0;
}
.sidebar-brand {
  display: flex; align-items: center; gap: 8px; font-size: 13px; font-weight: 600; color: #334155;
}
.brand-dot { width: 8px; height: 8px; border-radius: 50%; background: #10b981; }
.session-list { flex: 1; overflow-y: auto; padding: 8px; }
.session-item {
  display: flex; align-items: center; gap: 8px; padding: 10px 12px;
  border-radius: 8px; font-size: 13px; color: #64748b; cursor: pointer;
  transition: all 0.15s; margin-bottom: 2px;
  &:hover { background: #e2e8f0; color: #334155; }
  &.active { background: #e0e7ff; color: #0052ff; font-weight: 500; }
}
.session-title { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.session-empty { text-align: center; padding: 24px; font-size: 12px; color: #94a3b8; }
.sidebar-footer {
  padding: 12px 16px; border-top: 1px solid #e2e8f0;
}
.ai-model-tag {
  display: flex; align-items: center; gap: 6px; font-size: 11px;
  color: #64748b; background: #e2e8f0; padding: 4px 10px; border-radius: 6px;
  width: fit-content;
}

.ai-main { flex: 1; display: flex; flex-direction: column; min-width: 0; }
.main-header {
  padding: 16px 24px; border-bottom: 1px solid #e2e8f0;
  display: flex; justify-content: space-between; align-items: center; flex-shrink: 0;
}
.header-info h3 { font-size: 16px; font-weight: 600; color: #0f172a; margin: 0; }
.header-desc { font-size: 12px; color: #64748b; margin-top: 2px; }

.chat-area {
  flex: 1; overflow-y: auto; padding: 24px; display: flex; flex-direction: column; gap: 20px;
}

.welcome-section {
  display: flex; flex-direction: column; align-items: center; padding: 40px 0 20px;
  h2 { font-size: 20px; font-weight: 600; color: #0f172a; margin: 16px 0 8px; }
  p { font-size: 14px; color: #64748b; margin: 0 0 28px; }
}
.welcome-icon { opacity: 0.9; }
.quick-grid {
  display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; width: 100%; max-width: 480px;
}
.quick-card {
  display: flex; align-items: center; gap: 10px; padding: 14px 16px;
  background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 10px;
  cursor: pointer; transition: all 0.2s;
  &:hover { border-color: #0052ff; background: #f0f4ff; transform: translateY(-1px); box-shadow: 0 2px 8px rgba(0,82,255,0.08); }
}
.qc-text { font-size: 13px; color: #334155; font-weight: 500; }

.msg-row { display: flex; gap: 12px; max-width: 80%; }
.msg-row.user { align-self: flex-end; flex-direction: row-reverse; }
.msg-avatar { flex-shrink: 0; margin-top: 2px; }
.avatar-ai {
  width: 32px; height: 32px; border-radius: 8px; background: #0052ff; color: #fff;
  display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700;
}
.msg-content { min-width: 0; }
.msg-bubble {
  padding: 12px 16px; border-radius: 12px; font-size: 14px; line-height: 1.7;
  word-break: break-word;
}
.msg-row.assistant .msg-bubble { background: #f1f5f9; color: #1e293b; }
.msg-row.user .msg-bubble { background: #0052ff; color: #fff; }

.typing { display: flex; gap: 5px; padding: 16px 20px; }
.dot { width: 8px; height: 8px; border-radius: 50%; background: #94a3b8; animation: typing-dot 1.4s infinite; }
.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes typing-dot { 0%,60%,100%{transform:translateY(0)} 30%{transform:translateY(-6px)} }

.input-area {
  padding: 16px 24px; border-top: 1px solid #e2e8f0; flex-shrink: 0;
  background: #fff;
}
.input-box {
  display: flex; align-items: flex-end; gap: 10px;
  background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px;
  padding: 8px 12px; transition: border-color 0.2s;
  &:focus-within { border-color: #0052ff; box-shadow: 0 0 0 3px rgba(0,82,255,0.08); }
  :deep(.el-textarea__inner) {
    border: none; background: transparent; box-shadow: none; padding: 4px 0;
    font-size: 14px; color: #0f172a; resize: none;
    &::placeholder { color: #94a3b8; }
  }
}
.send-btn {
  flex-shrink: 0; width: 36px; height: 36px; border-radius: 10px;
  background: #0052ff; border-color: #0052ff;
  &:hover { background: #0044dd; }
  &:disabled { background: #cbd5e1; border-color: #cbd5e1; }
}
.input-hint { font-size: 11px; color: #94a3b8; margin-top: 6px; padding-left: 4px; }
</style>