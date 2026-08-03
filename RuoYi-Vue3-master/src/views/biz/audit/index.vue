<template>
  <div class="app-container" style="background:#f6f8fb;min-height:calc(100vh - 84px);padding:20px">
    <div class="audit-hero">
      <h1 class="hero-title">注册审核</h1>
      <p class="hero-desc">用户注册记录管理 · 禁用违规账户</p>
    </div>

    <el-row :gutter="20" class="mb20">
      <el-col :span="8">
        <div class="glass-card stat-success">
          <div class="card-icon"><el-icon><CircleCheck /></el-icon></div>
          <div class="card-info"><div class="card-value">{{ approvedCount }} <span class="unit">条</span></div><div class="card-label">通过</div></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="glass-card stat-danger">
          <div class="card-icon"><el-icon><CircleClose /></el-icon></div>
          <div class="card-info"><div class="card-value">{{ disabledCount }} <span class="unit">条</span></div><div class="card-label">禁用</div></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="glass-card stat-purple">
          <div class="card-icon"><el-icon><Document /></el-icon></div>
          <div class="card-info"><div class="card-value">{{ total }} <span class="unit">条</span></div><div class="card-label">总数</div></div>
        </div>
      </el-col>
    </el-row>

    <div class="biz-section">
      <div class="card-header-flex">
        <span class="header-title"><el-icon><List /></el-icon> 注册记录</span>
        <div class="filter-bar">
          <el-input v-model="searchRealName" placeholder="搜索姓名" clearable size="default" style="width:140px;" @keyup.enter="getList" prefix-icon="Search" />
          <el-input v-model="searchPhone" placeholder="搜索手机号" clearable size="default" style="width:150px;" @keyup.enter="getList" prefix-icon="Search" />
          <el-select v-model="searchStatus" placeholder="状态" clearable size="default" style="width:100px;" @change="getList">
            <el-option label="通过" value="1" />
            <el-option label="禁用" value="2" />
          </el-select>
          <el-button type="primary" icon="Search" @click="getList" size="default">搜索</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="auditList" style="width:100%;" :header-cell-style="{background:'#f8fafc',color:'#475569',fontWeight:'600',fontSize:'13px'}" stripe>
        <el-table-column label="申请人" align="center" min-width="80">
          <template #default="{ row }">
            <span style="font-weight:500;color:#1e293b;">{{ row.realName || row.userName || '--' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="手机号" align="center" prop="phonenumber" min-width="120">
          <template #default="{ row }">
            <span style="color:#64748b;font-family:Consolas,monospace;font-size:13px;">{{ row.phonenumber || '--' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="证件材料" align="center" min-width="160">
          <template #default="{ row }">
            <div style="display:flex;gap:8px;justify-content:center;align-items:center;">
              <div v-if="row.idCardFront" style="text-align:center">
                <el-image :src="resolveUrl(row.idCardFront)" :preview-src-list="getAllPreviewUrls(row)" fit="cover" style="width:60px;height:40px;border-radius:4px;cursor:pointer" />
                <div style="font-size:11px;color:#909399;margin-top:2px">正面</div>
              </div>
              <div v-if="row.idCardBack" style="text-align:center">
                <el-image :src="resolveUrl(row.idCardBack)" :preview-src-list="getAllPreviewUrls(row)" fit="cover" style="width:60px;height:40px;border-radius:4px;cursor:pointer" />
                <div style="font-size:11px;color:#909399;margin-top:2px">反面</div>
              </div>
              <el-tag v-if="!row.idCardFront && !row.idCardBack" size="small" type="info">未上传</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" min-width="80">
          <template #default="{ row }">
            <el-tag :type="row.auditStatus==='1'?'success':'danger'" size="small" effect="dark">{{ row.auditStatus==='1'?'通过':'禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" align="center" prop="createTime" min-width="150">
          <template #default="{ row }">
            <span style="color:#64748b;font-size:13px;">{{ row.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.auditStatus==='1'" link type="danger" @click="handleDisable(row)">禁用</el-button>
            <el-button v-if="row.auditStatus==='2'" link type="success" @click="handleEnable(row)">解禁</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="pageNum" v-model:limit="pageSize" @pagination="getList" />
    </div>
  </div>
</template>

<script setup name="EeAudit">
import { ref, computed, onMounted } from 'vue'
import { listAudit, disableAudit, enableAudit, deleteAudit } from '@/api/biz/audit'
import { ElMessage, ElMessageBox } from 'element-plus'

const auditList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const loading = ref(false)
const searchRealName = ref('')
const searchPhone = ref('')
const searchStatus = ref('')

const approvedCount = computed(() => auditList.value.filter(r => r.auditStatus === '1').length)
const disabledCount = computed(() => auditList.value.filter(r => r.auditStatus === '2').length)

function resolveUrl(url) {
  if (!url) return ''
  const baseApi = import.meta.env.VITE_APP_BASE_API
  if (url.startsWith('http')) { const idx = url.indexOf('/profile/'); if (idx !== -1) return baseApi + url.substring(idx); return url }
  if (url.startsWith('/profile')) return baseApi + url
  return url
}

function getAllPreviewUrls(row) {
  const urls = []
  if (row.idCardFront) urls.push(resolveUrl(row.idCardFront))
  if (row.idCardBack) urls.push(resolveUrl(row.idCardBack))
  return urls
}

function getList() {
  loading.value = true
  listAudit({
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    realName: searchRealName.value || undefined,
    phonenumber: searchPhone.value || undefined,
    auditStatus: searchStatus.value || undefined
  }).then(res => {
    auditList.value = res.rows || []
    total.value = res.total || 0
  }).finally(() => { loading.value = false })
}

function handleDisable(row) {
  ElMessageBox.confirm('确认禁用「' + (row.realName || row.userName) + '」的账户？禁用后该用户将无法登录。', '禁用确认', { type: 'warning' }).then(() => {
    disableAudit(row.recordId).then(() => {
      ElMessage.success('已禁用')
      getList()
    })
  }).catch(() => {})
}

function handleEnable(row) {
  ElMessageBox.confirm('确认解禁「' + (row.realName || row.userName) + '」的账户？', '解禁确认', { type: 'success' }).then(() => {
    enableAudit(row.recordId).then(() => {
      ElMessage.success('已解禁')
      getList()
    })
  }).catch(() => {})
}

function handleDelete(row) {
  ElMessageBox.confirm('确认删除「' + (row.realName || row.userName) + '」的注册记录？此操作不可恢复。', '删除确认', { type: 'error' }).then(() => {
    deleteAudit(row.recordId).then(() => {
      ElMessage.success('已删除')
      getList()
    })
  }).catch(() => {})
}

onMounted(() => { getList() })
</script>

<style lang="scss" scoped>
.audit-hero { margin-bottom: 24px; }
.hero-title { font-size: 24px; font-weight: 700; background: linear-gradient(135deg, #409EFF, #7c3aed); -webkit-background-clip: text; -webkit-text-fill-color: transparent; margin: 0 0 4px 0; }
.hero-desc { font-size: 14px; color: #94a3b8; margin: 0; }

.glass-card {
  background: rgba(255, 255, 255, 0.7); backdrop-filter: blur(10px); border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 16px; padding: 20px; display: flex; flex-direction: column;
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.05); transition: all 0.3s; position: relative; overflow: hidden;
  &::before { content: ""; position: absolute; top: 0; left: 0; width: 100%; height: 5px; }
  &:hover { transform: translateY(-5px); box-shadow: 0 12px 40px 0 rgba(31, 38, 135, 0.1); }
}
.stat-success::before { background: linear-gradient(90deg, #67c23a, #85ce61); }
.stat-danger::before { background: linear-gradient(90deg, #f56c6c, #f89898); }
.stat-purple::before { background: linear-gradient(90deg, #7c3aed, #a78bfa); }

.card-icon { font-size: 24px; width: 44px; height: 44px; border-radius: 10px; display: flex; align-items: center; justify-content: center; margin-bottom: 15px; }
.stat-success .card-icon { background: rgba(103, 194, 58, 0.1); color: #67c23a; }
.stat-danger .card-icon { background: rgba(245, 108, 108, 0.1); color: #f56c6c; }
.stat-purple .card-icon { background: rgba(124, 58, 237, 0.1); color: #7c3aed; }

.card-value { font-size: 28px; font-weight: bold; color: #262626; font-family: 'Outfit', sans-serif; .unit { font-size: 14px; font-weight: normal; color: #8c8c8c; margin-left: 5px; } }
.card-label { font-size: 13px; color: #8c8c8c; margin-top: 5px; }
.filter-bar { display: flex; gap: 10px; align-items: center; }
.mb20 { margin-bottom: 20px; }
</style>
