<template>
  <div class="m-page">
    <div class="page-title">操作审计日志</div>

    <div class="search-bar">
      <el-input v-model="searchUserName" placeholder="搜索操作人" clearable @keyup.enter="getList" prefix-icon="Search" />
      <el-select v-model="searchModule" placeholder="模块" clearable style="width:90px" @change="getList">
        <el-option label="政策" value="政策管理" /><el-option label="规则" value="规则管理" /><el-option label="匹配" value="匹配兑现" /><el-option label="资金" value="资金管理" /><el-option label="目标" value="目标数据" />
      </el-select>
      <el-button type="primary" size="small" @click="getList">搜索</el-button>
    </div>

    <div v-loading="loading">
      <div v-for="item in logList" :key="item.id || item.createTime" class="card-item">
        <div class="ci-top">
          <span class="ci-name">{{ item.userName }}</span>
          <span class="ci-module" :class="'mod-'+item.module">{{ getModuleLabel(item.module) }}</span>
        </div>
        <div class="ci-mid">
          <span class="ci-op">{{ item.operation }}</span>
          <span class="ci-id" v-if="item.targetId">ID:{{ item.targetId }}</span>
        </div>
        <div class="ci-detail" v-if="item.detail">{{ item.detail }}</div>
        <div class="ci-bottom">
          <span>{{ item.createTime }}</span>
          <span v-if="item.ip" class="ci-ip">{{ item.ip }}</span>
        </div>
      </div>
      <el-empty v-if="!loading && logList.length===0" description="暂无日志" :image-size="60" />
    </div>

    <div class="pager-row" v-if="total > 0">
      <el-button :disabled="pageNum<=1" @click="pageNum--;getList()" size="small">上一页</el-button>
      <span>{{ pageNum }} / {{ Math.ceil(total/pageSize) }}</span>
      <el-button :disabled="pageNum>=Math.ceil(total/pageSize)" @click="pageNum++;getList()" size="small">下一页</el-button>
    </div>
  </div>
</template>

<script setup name="EeAuditLog">
import { ref, onMounted } from 'vue'
import { listEeAuditLog } from '@/api/biz/auditLog'

const logList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const loading = ref(false)
const searchUserName = ref('')
const searchModule = ref('')

function getList() {
  loading.value = true
  listEeAuditLog({ pageNum: pageNum.value, pageSize: pageSize.value, userName: searchUserName.value || undefined, module: searchModule.value || undefined }).then(res => { logList.value = res.rows || []; total.value = res.total || 0 }).finally(() => { loading.value = false })
}
function getModuleLabel(m) { return { policy:'政策管理', rule:'规则管理', matchRecord:'匹配兑现', fund:'资金管理', targetData:'目标数据' }[m] || m }
onMounted(() => { getList() })
</script>

<style scoped>
.m-page { padding:12px; background:#f5f6fa; min-height:100%; }
.page-title { font-size:16px; font-weight:700; color:#1e293b; margin-bottom:12px; }
.search-bar { display:flex; gap:6px; margin-bottom:10px; }
.search-bar .el-input { flex:1; }
.card-item { background:#fff; border:1px solid #e2e8f0; border-radius:10px; padding:12px; margin-bottom:8px; }
.ci-top { display:flex; justify-content:space-between; align-items:center; margin-bottom:6px; }
.ci-name { font-size:14px; font-weight:600; color:#1e293b; }
.ci-module { font-size:11px; padding:2px 8px; border-radius:6px; font-weight:600; }
.mod-policy { background:rgba(64,158,255,0.1); color:#409EFF; }
.mod-rule { background:rgba(230,162,60,0.1); color:#E6A23C; }
.mod-matchRecord { background:rgba(103,194,58,0.1); color:#67C23A; }
.mod-fund { background:rgba(245,108,108,0.1); color:#F56C6C; }
.mod-targetData { background:rgba(144,147,153,0.1); color:#909399; }
.ci-mid { display:flex; align-items:center; gap:8px; margin-bottom:4px; }
.ci-op { font-size:13px; color:#475569; font-weight:500; }
.ci-id { font-size:11px; color:#94a3b8; font-family:Consolas,monospace; }
.ci-detail { font-size:12px; color:#64748b; margin-bottom:4px; line-height:1.5; }
.ci-bottom { display:flex; justify-content:space-between; font-size:11px; color:#94a3b8; }
.ci-ip { font-family:Consolas,monospace; }
.pager-row { display:flex; justify-content:center; align-items:center; gap:12px; padding:10px 0; font-size:13px; color:#64748b; }
</style>
