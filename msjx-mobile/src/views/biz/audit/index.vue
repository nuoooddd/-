<template>
  <div class="m-page">
    <div class="stat-row">
      <div class="stat-item" style="background:#d1fae5"><div class="si-val">{{ approvedCount }}</div><div class="si-label">通过</div></div>
      <div class="stat-item" style="background:#fef2f2"><div class="si-val">{{ disabledCount }}</div><div class="si-label">禁用</div></div>
      <div class="stat-item" style="background:#ede9fe"><div class="si-val">{{ total }}</div><div class="si-label">总数</div></div>
    </div>

    <div class="search-bar">
      <el-input v-model="searchRealName" placeholder="搜索姓名" clearable @keyup.enter="getList" prefix-icon="Search" />
      <el-select v-model="searchStatus" placeholder="状态" clearable style="width:80px" @change="getList">
        <el-option label="通过" value="1" /><el-option label="禁用" value="2" />
      </el-select>
      <el-button type="primary" size="small" @click="getList">搜索</el-button>
    </div>

    <div v-loading="loading">
      <div v-for="item in auditList" :key="item.recordId" class="card-item">
        <div class="ci-top">
          <span class="ci-name">{{ item.realName || item.userName || '--' }}</span>
          <el-tag :type="item.auditStatus==='1'?'success':'danger'" size="small" effect="dark">{{ item.auditStatus==='1'?'通过':'禁用' }}</el-tag>
        </div>
        <div class="ci-mid">
          <span class="ci-info">{{ item.phonenumber || '--' }}</span>
        </div>
        <div class="ci-bottom">
          <span>{{ item.createTime }}</span>
        </div>
        <div v-if="item.idCardFront || item.idCardBack" class="ci-photos">
          <div v-if="item.idCardFront" class="photo-item" @click="previewImage(item.idCardFront)">
            <img :src="resolveUrl(item.idCardFront)" class="photo-thumb" />
            <span>正面</span>
          </div>
          <div v-if="item.idCardBack" class="photo-item" @click="previewImage(item.idCardBack)">
            <img :src="resolveUrl(item.idCardBack)" class="photo-thumb" />
            <span>反面</span>
          </div>
        </div>
        <div class="ci-actions" @click.stop>
          <el-button v-if="item.auditStatus==='1'" type="danger" size="small" @click="handleDisable(item)" round>禁用</el-button>
          <el-button v-if="item.auditStatus==='2'" type="success" size="small" @click="handleEnable(item)" round>解禁</el-button>
          <el-button type="danger" size="small" plain @click="handleDelete(item)" round>删除</el-button>
        </div>
      </div>
      <el-empty v-if="!loading && auditList.length===0" description="暂无记录" :image-size="60" />
    </div>

    <div class="pager-row" v-if="total > 0">
      <el-button :disabled="pageNum<=1" @click="pageNum--;getList()" size="small">上一页</el-button>
      <span>{{ pageNum }} / {{ Math.ceil(total/pageSize) }}</span>
      <el-button :disabled="pageNum>=Math.ceil(total/pageSize)" @click="pageNum++;getList()" size="small">下一页</el-button>
    </div>

    <el-dialog v-model="previewVisible" title="证件预览" width="92%" append-to-body>
      <img v-if="previewUrl" :src="previewUrl" style="width:100%;border-radius:8px" />
    </el-dialog>
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
const previewVisible = ref(false)
const previewUrl = ref('')

const approvedCount = computed(() => auditList.value.filter(r => r.auditStatus === '1').length)
const disabledCount = computed(() => auditList.value.filter(r => r.auditStatus === '2').length)

function resolveUrl(url) {
  if (!url) return ''
  const baseApi = import.meta.env.VITE_APP_BASE_API
  if (url.startsWith('http')) { const idx = url.indexOf('/profile/'); if (idx !== -1) return baseApi + url.substring(idx); return url }
  if (url.startsWith('/profile')) return baseApi + url
  return url
}

function previewImage(url) { previewUrl.value = resolveUrl(url); previewVisible.value = true }

function getList() {
  loading.value = true
  listAudit({ pageNum: pageNum.value, pageSize: pageSize.value, realName: searchRealName.value || undefined, phonenumber: searchPhone.value || undefined, auditStatus: searchStatus.value || undefined }).then(res => { auditList.value = res.rows || []; total.value = res.total || 0 }).finally(() => { loading.value = false })
}

function handleDisable(row) {
  ElMessageBox.confirm('确认禁用「' + (row.realName || row.userName) + '」？', '禁用确认', { type: 'warning' }).then(() => { disableAudit(row.recordId).then(() => { ElMessage.success('已禁用'); getList() }) }).catch(() => {})
}
function handleEnable(row) {
  ElMessageBox.confirm('确认解禁「' + (row.realName || row.userName) + '」？', '解禁确认', { type: 'success' }).then(() => { enableAudit(row.recordId).then(() => { ElMessage.success('已解禁'); getList() }) }).catch(() => {})
}
function handleDelete(row) {
  ElMessageBox.confirm('确认删除该记录？不可恢复。', '删除确认', { type: 'error' }).then(() => { deleteAudit(row.recordId).then(() => { ElMessage.success('已删除'); getList() }) }).catch(() => {})
}
onMounted(() => { getList() })
</script>

<style scoped>
.m-page { padding:12px; background:#f5f6fa; min-height:100%; }
.stat-row { display:flex; gap:8px; margin-bottom:12px; }
.stat-item { flex:1; text-align:center; padding:10px 4px; border-radius:10px; }
.si-val { font-size:16px; font-weight:700; color:#1e293b; font-family:'Outfit',sans-serif; }
.si-label { font-size:10px; color:#64748b; margin-top:2px; }
.search-bar { display:flex; gap:6px; margin-bottom:10px; }
.search-bar .el-input { flex:1; }
.card-item { background:#fff; border:1px solid #e2e8f0; border-radius:10px; padding:12px; margin-bottom:8px; }
.ci-top { display:flex; justify-content:space-between; align-items:center; margin-bottom:6px; }
.ci-name { font-size:14px; font-weight:600; color:#1e293b; }
.ci-mid { margin-bottom:6px; }
.ci-info { font-size:12px; color:#64748b; font-family:Consolas,monospace; }
.ci-bottom { font-size:11px; color:#94a3b8; }
.ci-photos { display:flex; gap:10px; margin:8px 0; padding:8px; background:#f8fafc; border-radius:8px; }
.photo-item { display:flex; flex-direction:column; align-items:center; gap:4px; cursor:pointer; }
.photo-item span { font-size:11px; color:#64748b; }
.photo-thumb { width:80px; height:56px; object-fit:cover; border-radius:6px; border:1px solid #e2e8f0; }
.ci-actions { display:flex; gap:6px; margin-top:8px; padding-top:8px; border-top:1px solid #f1f5f9; }
.pager-row { display:flex; justify-content:center; align-items:center; gap:12px; padding:10px 0; font-size:13px; color:#64748b; }
</style>
