<template>
  <div class="app-container" style="background: #f6f8fb; min-height: calc(100vh - 84px); padding: 20px;">
    <div class="msg-header">
      <h2>消息通知中心</h2>
      <div class="msg-actions">
        <el-button type="primary" icon="EditPen" @click="openCompose" style="border-radius: 8px;">写信</el-button>
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="badge-item">
          <el-button icon="Check" @click="handleMarkAllRead" :disabled="unreadCount === 0" style="border-radius: 8px;">全部已读</el-button>
        </el-badge>
      </div>
    </div>

    <el-row :gutter="16" class="mb20">
      <el-col :span="6">
        <div class="glass-card stat-primary" @click="filterAll()" style="cursor:pointer">
          <div class="card-value">{{ total }}</div>
          <div class="card-label">全部消息</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-card stat-danger" @click="filterByRead('0')" style="cursor:pointer">
          <div class="card-value">{{ unreadCount }}</div>
          <div class="card-label">未读消息</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-card stat-success" @click="filterByRead('1')" style="cursor:pointer">
          <div class="card-value">{{ readCount }}</div>
          <div class="card-label">已读消息</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-card stat-warning" @click="filterByType('mail')" style="cursor:pointer">
          <div class="card-value">{{ mailCount }}</div>
          <div class="card-label">站内信</div>
        </div>
      </el-col>
    </el-row>

    <el-card style="border-radius: 12px; box-shadow: 0 4px 12px 0 rgba(0,0,0,0.05);">
      <template #header>
        <div class="card-header-flex">
          <span class="header-title"><el-icon><ChatLineRound /></el-icon> 消息列表</span>
          <el-radio-group v-model="queryParams.msgType" @change="getList" size="small">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="push">政策推送</el-radio-button>
            <el-radio-button label="fulfill">兑付通知</el-radio-button>
            <el-radio-button label="system">系统通知</el-radio-button>
            <el-radio-button label="mail">站内信</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <div v-for="msg in messageList" :key="msg.messageId" class="msg-item" :class="{ unread: msg.isRead === '0' }" @click="handleRead(msg)">
        <div class="msg-dot" v-if="msg.isRead === '0'"></div>
        <div class="msg-icon">
          <el-icon :size="20" :color="getMsgIconColor(msg.msgType)">
            <component :is="getMsgIcon(msg.msgType)" />
          </el-icon>
        </div>
        <div class="msg-body">
          <div class="msg-title-row">
            <span v-if="msg.senderName" class="msg-sender">{{ msg.senderName }}</span>
            <span class="msg-title">{{ msg.title }}</span>
            <el-tag :type="getMsgTypeTag(msg.msgType)" size="small" effect="light" style="border-radius: 4px;">{{ getMsgTypeLabel(msg.msgType) }}</el-tag>
          </div>
          <div class="msg-content">{{ msg.content }}</div>
          <div class="msg-time">{{ msg.createTime }}</div>
        </div>
        <div class="msg-ops">
          <el-button v-if="msg.isRead === '1'" link type="warning" icon="ChatDotSquare" @click.stop="handleMarkUnread(msg)" size="small" title="标记未读" />
          <el-button link type="danger" icon="Delete" @click.stop="handleDelete(msg)" size="small" />
        </div>
      </div>

      <el-empty v-if="messageList.length === 0" description="暂无消息" :image-size="80" />

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>

    <el-dialog v-model="detailVisible" title="消息详情" width="560px" append-to-body>
      <div v-if="detailMsg" class="msg-detail">
        <div class="detail-header">
          <el-tag :type="getMsgTypeTag(detailMsg.msgType)" size="small" effect="dark">{{ getMsgTypeLabel(detailMsg.msgType) }}</el-tag>
          <span v-if="detailMsg.senderName" class="detail-sender">来自：{{ detailMsg.senderName }}</span>
          <span class="detail-time">{{ detailMsg.createTime }}</span>
        </div>
        <h3 class="detail-title">{{ detailMsg.title }}</h3>
        <div class="detail-content">{{ detailMsg.content }}</div>
        <div class="detail-footer">
          <el-button v-if="detailMsg.senderId" type="primary" plain size="small" @click="openReply(detailMsg)">
            <el-icon><ChatDotSquare /></el-icon> 回复
          </el-button>
          <el-button v-if="detailMsg.isRead === '1'" type="warning" plain size="small" @click="handleMarkUnread(detailMsg); detailVisible=false">
            <el-icon><ChatDotSquare /></el-icon> 标记未读
          </el-button>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="composeVisible" :title="composeForm.receiverId ? '回复信件' : '写信'" width="560px" destroy-on-close append-to-body>
      <el-form :model="composeForm" label-width="80px">
        <el-form-item label="收件人">
          <el-select v-model="composeForm.receiverId" placeholder="选择收件人" filterable :disabled="!!composeForm.receiverName" style="width:100%">
            <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName + ' (' + u.userName + ')'" :value="u.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="主题">
          <el-input v-model="composeForm.title" placeholder="请输入信件主题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="composeForm.content" type="textarea" :rows="5" placeholder="请输入信件内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="composeVisible = false">取消</el-button>
        <el-button type="primary" :loading="composeLoading" @click="handleSend">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="EeMessage">
import { ref, computed, onActivated, onMounted, getCurrentInstance } from 'vue'
import { listEeMessage, delEeMessage, markRead, markAllRead, markUnread, getUnreadCount, sendMessage, getMessageUsers } from '@/api/biz/message'
import { ElMessage, ElMessageBox } from 'element-plus'

const { proxy } = getCurrentInstance()
const messageList = ref([])
const detailVisible = ref(false)
const detailMsg = ref(null)
const total = ref(0)
const unreadCount = ref(0)
const queryParams = ref({ pageNum: 1, pageSize: 10, msgType: '', isRead: '' })

const composeVisible = ref(false)
const composeLoading = ref(false)
const composeForm = ref({ receiverId: null, receiverName: '', title: '', content: '' })
const userOptions = ref([])

const readCount = computed(() => total.value - unreadCount.value)
const pushCount = computed(() => messageList.value.filter(m => m.msgType === 'push').length)
const mailCount = computed(() => messageList.value.filter(m => m.msgType === 'mail').length)

function getList() {
  listEeMessage(queryParams.value).then(res => {
    messageList.value = res.rows || []
    total.value = res.total || 0
  })
  getUnreadCount().then(res => { unreadCount.value = res.data || 0 })
}

function filterByRead(isRead) {
  queryParams.value.isRead = isRead
  queryParams.value.msgType = ''
  queryParams.value.pageNum = 1
  getList()
}

function filterByType(msgType) {
  queryParams.value.msgType = msgType
  queryParams.value.isRead = ''
  queryParams.value.pageNum = 1
  getList()
}

function filterAll() {
  queryParams.value.isRead = ''
  queryParams.value.msgType = ''
  queryParams.value.pageNum = 1
  getList()
}

function handleRead(msg) {
  detailMsg.value = msg
  detailVisible.value = true
  if (msg.isRead === '0') {
    markRead(msg.messageId).then(() => {
      msg.isRead = '1'
      getList()
    })
  }
}

function handleMarkUnread(msg) {
  markUnread(msg.messageId).then(() => {
    msg.isRead = '0'
    ElMessage.success('已标记为未读')
    getList()
  })
}

function handleMarkAllRead() {
  markAllRead().then(() => {
    ElMessage.success('已全部标记为已读')
    getList()
  })
}

function handleDelete(msg) {
  ElMessageBox.confirm('确认删除该消息？', '提示', { type: 'warning' }).then(() => {
    delEeMessage(msg.messageId).then(() => {
      ElMessage.success('删除成功')
      getList()
    })
  }).catch(() => {})
}

function openCompose() {
  composeForm.value = { receiverId: null, receiverName: '', title: '', content: '' }
  loadUsers()
  composeVisible.value = true
}

function openReply(msg) {
  detailVisible.value = false
  composeForm.value = {
    receiverId: msg.senderId,
    receiverName: msg.senderName,
    title: '回复：' + msg.title,
    content: ''
  }
  composeVisible.value = true
}

function loadUsers() {
  getMessageUsers().then(res => {
    userOptions.value = (res.data || []).filter(u => u.userId !== undefined)
  })
}

function handleSend() {
  if (!composeForm.value.receiverId) { ElMessage.warning('请选择收件人'); return }
  if (!composeForm.value.title) { ElMessage.warning('请输入主题'); return }
  if (!composeForm.value.content) { ElMessage.warning('请输入内容'); return }
  composeLoading.value = true
  sendMessage({
    receiverId: composeForm.value.receiverId,
    title: composeForm.value.title,
    content: composeForm.value.content
  }).then(() => {
    ElMessage.success('发送成功')
    composeVisible.value = false
    getList()
  }).catch(() => {
    ElMessage.error('发送失败')
  }).finally(() => {
    composeLoading.value = false
  })
}

function getMsgIcon(type) { return { push: 'Share', fulfill: 'Money', system: 'InfoFilled', mail: 'Message' }[type] || 'Bell' }
function getMsgIconColor(type) { return { push: '#1890ff', fulfill: '#52c41a', system: '#e6a23c', mail: '#722ed1' }[type] || '#909399' }
function getMsgTypeTag(type) { return { push: 'primary', fulfill: 'success', system: 'warning', mail: '' }[type] || 'info' }
function getMsgTypeLabel(type) { return { push: '政策推送', fulfill: '兑付通知', system: '系统通知', mail: '站内信' }[type] || '通知' }

onMounted(() => { getList() })
onActivated(() => { getList() })
</script>

<style scoped>
.msg-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.msg-header h2 { font-size: 22px; font-weight: bold; color: #1a3a5c; margin: 0; }
.msg-actions { display: flex; gap: 10px; align-items: center; }

.glass-card { background: rgba(255,255,255,0.7); backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.4); border-radius: 16px; padding: 20px 16px; display: flex; flex-direction: column; align-items: center; box-shadow: 0 8px 32px 0 rgba(31,38,135,0.05); transition: all 0.3s; }
.glass-card:hover { transform: translateY(-3px); box-shadow: 0 12px 40px 0 rgba(31,38,135,0.1); }
.stat-primary .card-value { color: #1890ff; } .stat-danger .card-value { color: #f56c6c; } .stat-success .card-value { color: #52c41a; } .stat-warning .card-value { color: #722ed1; }
.card-value { font-size: 28px; font-weight: bold; }
.card-label { font-size: 12px; color: #8c8c8c; margin-top: 6px; }

.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.header-title { font-size: 16px; font-weight: bold; color: #303133; display: flex; align-items: center; gap: 8px; }

.msg-item { display: flex; align-items: flex-start; padding: 16px; border-radius: 10px; margin-bottom: 8px; background: #fafafa; border: 1px solid #f0f0f0; cursor: pointer; transition: all 0.2s; position: relative; }
.msg-item:hover { background: #f0f5ff; border-color: #adc6ff; }
.msg-item.unread { background: #fff; border-left: 3px solid #1890ff; }
.msg-dot { position: absolute; top: 18px; left: 8px; width: 8px; height: 8px; border-radius: 50%; background: #f56c6c; }
.msg-icon { width: 40px; height: 40px; border-radius: 10px; background: #f5f7fa; display: flex; align-items: center; justify-content: center; margin-right: 12px; flex-shrink: 0; }
.msg-body { flex: 1; min-width: 0; }
.msg-title-row { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.msg-sender { font-size: 12px; color: #722ed1; background: #f9f0ff; padding: 1px 8px; border-radius: 4px; flex-shrink: 0; }
.msg-title { font-size: 14px; font-weight: 500; color: #303133; }
.msg-content { font-size: 13px; color: #606266; line-height: 1.5; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.msg-time { font-size: 12px; color: #c0c4cc; }
.msg-ops { flex-shrink: 0; margin-left: 8px; }

.msg-detail { padding:8px 0; }
.detail-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:16px; }
.detail-sender { color: #722ed1; font-size: 13px; font-weight: 500; }
.detail-time { color:#999; font-size:13px; }
.detail-title { font-size:18px; font-weight:600; color:#303133; margin:0 0 16px; }
.detail-content { font-size:14px; color:#606266; line-height:1.8; padding:16px; background:#f9f9fb; border-radius:8px; white-space:pre-wrap; }
.detail-footer { margin-top:20px; padding-top:16px; border-top:1px solid #f0f0f0; display:flex; justify-content:flex-end; gap:8px; }

.mb20 { margin-bottom: 20px; }
</style>
