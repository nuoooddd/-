<template>
  <div class="app-container" style="background: #f6f8fb; min-height: 100%; padding: 12px;">
    <div class="msg-header">
      <h2>消息中心</h2>
      <div class="msg-actions">
        <el-button type="primary" icon="EditPen" size="small" @click="openCompose" style="border-radius: 8px;">写信</el-button>
        <el-button icon="Check" size="small" @click="handleMarkAllRead" :disabled="unreadCount === 0" style="border-radius: 8px;">全部已读</el-button>
      </div>
    </div>

    <div class="msg-stats">
      <div class="stat-item" @click="filterAll()">
        <div class="stat-val" style="color:#1890ff">{{ total }}</div>
        <div class="stat-label">全部</div>
      </div>
      <div class="stat-item" @click="filterByRead('0')">
        <div class="stat-val" style="color:#f56c6c">{{ unreadCount }}</div>
        <div class="stat-label">未读</div>
      </div>
      <div class="stat-item" @click="filterByRead('1')">
        <div class="stat-val" style="color:#52c41a">{{ readCount }}</div>
        <div class="stat-label">已读</div>
      </div>
      <div class="stat-item" @click="filterByType('mail')">
        <div class="stat-val" style="color:#722ed1">{{ mailCount }}</div>
        <div class="stat-label">站内信</div>
      </div>
    </div>

    <div class="msg-filter">
      <el-select v-model="queryParams.msgType" @change="getList" size="small" style="width:100%">
        <el-option label="全部类型" value="" />
        <el-option label="政策推送" value="push" />
        <el-option label="兑付通知" value="fulfill" />
        <el-option label="系统通知" value="system" />
        <el-option label="站内信" value="mail" />
      </el-select>
    </div>

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

    <el-dialog v-model="detailVisible" title="消息详情" width="92%" append-to-body>
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

    <el-dialog v-model="composeVisible" :title="composeForm.receiverId ? '回复信件' : '写信'" width="92%" destroy-on-close append-to-body>
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
.msg-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.msg-header h2 { font-size: 18px; font-weight: bold; color: #1a3a5c; margin: 0; }
.msg-actions { display: flex; gap: 6px; align-items: center; }

.msg-stats { display:flex; gap:0; background:rgba(255,255,255,0.8); border-radius:10px; overflow:hidden; margin-bottom:10px; border:1px solid #e8ecf1; }
.stat-item { flex:1; text-align:center; padding:10px 4px; cursor:pointer; border-right:1px solid rgba(0,0,0,0.04); }
.stat-item:last-child { border-right:none; }
.stat-val { font-size:16px; font-weight:bold; }
.stat-label { font-size:10px; color:#8c8c8c; margin-top:2px; }

.msg-filter { margin-bottom: 10px; }

.msg-item { display: flex; align-items: flex-start; padding: 10px 12px; border-radius: 10px; margin-bottom: 6px; background: #fafafa; border: 1px solid #f0f0f0; cursor: pointer; position: relative; }
.msg-item.unread { background: #fff; border-left: 3px solid #1890ff; }
.msg-dot { position: absolute; top: 14px; left: 8px; width: 6px; height: 6px; border-radius: 50%; background: #f56c6c; }
.msg-icon { width: 32px; height: 32px; border-radius: 8px; background: #f5f7fa; display: flex; align-items: center; justify-content: center; margin-right: 10px; flex-shrink: 0; }
.msg-body { flex: 1; min-width: 0; }
.msg-title-row { display: flex; align-items: center; gap: 6px; margin-bottom: 2px; flex-wrap:wrap; }
.msg-sender { font-size: 11px; color: #722ed1; background: #f9f0ff; padding: 1px 6px; border-radius: 3px; flex-shrink: 0; }
.msg-title { font-size: 13px; font-weight: 500; color: #303133; }
.msg-content { font-size: 12px; color: #606266; line-height: 1.4; margin-bottom: 2px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.msg-time { font-size: 11px; color: #c0c4cc; }
.msg-ops { flex-shrink: 0; margin-left: 4px; }

.msg-detail { padding:4px 0; }
.detail-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; flex-wrap:wrap; gap:4px; }
.detail-sender { color: #722ed1; font-size: 12px; font-weight: 500; }
.detail-time { color:#999; font-size:12px; }
.detail-title { font-size:16px; font-weight:600; color:#303133; margin:0 0 12px; }
.detail-content { font-size:13px; color:#606266; line-height:1.7; padding:12px; background:#f9f9fb; border-radius:8px; white-space:pre-wrap; }
.detail-footer { margin-top:16px; padding-top:12px; border-top:1px solid #f0f0f0; display:flex; justify-content:flex-end; gap:8px; }
</style>
