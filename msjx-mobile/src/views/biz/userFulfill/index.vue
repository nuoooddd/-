<template>
  <div class="m-page">
    <div class="stat-row">
      <div class="stat-item" style="background:#e0e7ff"><div class="si-val">{{ fulFilledCount }}</div><div class="si-label">已兑付</div></div>
      <div class="stat-item" style="background:#d1fae5"><div class="si-val">{{ formatMoneyShort(totalAmount) }}</div><div class="si-label">总额(万)</div></div>
      <div class="stat-item" style="background:#fef3c7"><div class="si-val">{{ pendingCount }}</div><div class="si-label">待确认</div></div>
      <div class="stat-item" style="background:#ede9fe"><div class="si-val">{{ passRate }}%</div><div class="si-label">免审率</div></div>
    </div>

    <div class="list-header">
      <span>兑付明细</span>
      <el-button size="small" @click="handleExport" round>导出</el-button>
    </div>

    <div v-loading="loading">
      <div v-for="item in recordList" :key="item.recordId" class="card-item">
        <div class="ci-top">
          <span class="ci-name">{{ item.policyName }}</span>
          <el-tag :type="{MATCHED:'primary',PUSHED:'warning',CONFIRMED:'',FULFILLED:'success',ARCHIVED:'info'}[item.status]" size="small">{{ getStatusLabel(item.status) }}</el-tag>
        </div>
        <div class="ci-mid">
          <span class="ci-money">{{ formatMoney(item.fundAmount) }} 元</span>
          <el-tag :type="{'0':'success','1':'warning','2':'danger'}[item.riskLevel]" size="small" effect="dark">{{ {'0':'低·免审','1':'中·审核','2':'高·拦截'}[item.riskLevel] }}</el-tag>
        </div>
        <div class="ci-bottom">
          <span>{{ item.matchTime || item.createTime || '--' }}</span>
        </div>
        <div class="ci-actions" @click.stop>
          <el-button link type="primary" size="small" @click="showTimeline(item)">时间线</el-button>
          <el-button v-if="item.status==='PUSHED'" link type="primary" size="small" @click="handleConfirm(item)">确认意愿</el-button>
        </div>
      </div>
      <el-empty v-if="!loading && recordList.length===0" description="暂无记录" :image-size="60" />
    </div>

    <el-drawer v-model="timelineVisible" direction="btt" size="60%" :with-header="false">
      <div class="drawer-title">流程时间线</div>
      <div class="tl-wrap">
        <div class="tl-node" :class="{active:true}"><div class="tl-dot"></div><div class="tl-body"><div class="tl-title">智能匹配</div><div class="tl-time">{{ timelineRecord.matchTime || timelineRecord.createTime || '--' }}</div><div class="tl-desc">系统自动比对画像与规则</div></div></div>
        <div class="tl-node" :class="{active:hasStatus('PUSHED')}"><div class="tl-dot"></div><div class="tl-body"><div class="tl-title">政策推送</div><div class="tl-time">{{ hasStatus('PUSHED')?(timelineRecord.updateTime||'--'):'等待中' }}</div><div class="tl-desc">政策已推送至企业待办</div></div></div>
        <div class="tl-node" :class="{active:hasStatus('CONFIRMED')}"><div class="tl-dot"></div><div class="tl-body"><div class="tl-title">意愿确认</div><div class="tl-time">{{ hasStatus('CONFIRMED')?(timelineRecord.updateTime||'--'):'等待中' }}</div><div class="tl-desc">企业已确认享受意愿</div></div></div>
        <div class="tl-node" :class="{active:hasStatus('FULFILLED')}"><div class="tl-dot"></div><div class="tl-body"><div class="tl-title">自动兑付</div><div class="tl-time">{{ hasStatus('FULFILLED')?(timelineRecord.updateTime||'--'):'等待中' }}</div><div class="tl-desc">资金已拨付至企业账户</div></div></div>
        <div class="tl-node" :class="{active:hasStatus('ARCHIVED')}"><div class="tl-dot"></div><div class="tl-body"><div class="tl-title">公示归档</div><div class="tl-time">{{ hasStatus('ARCHIVED')?(timelineRecord.updateTime||'--'):'等待中' }}</div><div class="tl-desc">已完成公示并归档</div></div></div>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
.m-page { padding:12px; background:#f5f6fa; min-height:100%; }
.stat-row { display:flex; gap:6px; margin-bottom:12px; }
.stat-item { flex:1; text-align:center; padding:10px 4px; border-radius:10px; }
.si-val { font-size:16px; font-weight:700; color:#1e293b; font-family:'Outfit',sans-serif; }
.si-label { font-size:10px; color:#64748b; margin-top:2px; }
.list-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:8px; font-size:14px; font-weight:600; color:#0f172a; }
.card-item { background:#fff; border:1px solid #e2e8f0; border-radius:10px; padding:12px; margin-bottom:8px; }
.ci-top { display:flex; justify-content:space-between; align-items:center; margin-bottom:6px; }
.ci-name { font-size:14px; font-weight:600; color:#1e293b; flex:1; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; margin-right:8px; }
.ci-mid { display:flex; align-items:center; gap:8px; margin-bottom:6px; }
.ci-money { color:#c2410c; font-weight:700; font-size:14px; font-family:Consolas,monospace; }
.ci-bottom { font-size:11px; color:#94a3b8; }
.ci-actions { display:flex; gap:4px; margin-top:8px; padding-top:8px; border-top:1px solid #f1f5f9; }
.drawer-title { font-size:16px; font-weight:700; color:#1e293b; padding:12px 0; text-align:center; }
.tl-wrap { padding:0 12px; }
.tl-node { display:flex; gap:12px; padding-bottom:20px; position:relative; }
.tl-node::before { content:''; position:absolute; left:6px; top:16px; bottom:0; width:2px; background:#e2e8f0; }
.tl-node:last-child::before { display:none; }
.tl-node.active .tl-dot { background:#4f46e5; box-shadow:0 0 0 4px rgba(79,70,229,0.2); }
.tl-dot { width:14px; height:14px; border-radius:50%; background:#d9d9d9; flex-shrink:0; margin-top:2px; }
.tl-body { flex:1; }
.tl-title { font-weight:600; font-size:14px; color:#1e293b; }
.tl-time { color:#94a3b8; font-size:11px; margin:2px 0; }
.tl-desc { color:#c0c4cc; font-size:11px; }
</style>

<script setup name="UserFulfill">
import { ref, computed, onActivated, onMounted, getCurrentInstance } from 'vue'
import { getMyRecords, userConfirm } from '@/api/biz/user'

const { proxy } = getCurrentInstance()
const recordList = ref([])
const loading = ref(true)
const timelineVisible = ref(false)
const timelineRecord = ref({})

const fulFilledCount = computed(() => recordList.value.filter(r => r.status==='FULFILLED'||r.status==='ARCHIVED').length)
const totalAmount = computed(() => recordList.value.filter(r => r.status==='FULFILLED'||r.status==='ARCHIVED').reduce((s,r) => s+(r.fundAmount||0), 0))
const pendingCount = computed(() => recordList.value.filter(r => r.status==='PUSHED').length)
const passRate = computed(() => { const t = recordList.value.length; if (t===0) return 0; return Math.round(recordList.value.filter(r => r.riskLevel==='0').length/t*100) })

function formatMoney(v) { if(!v) return '0.00'; return Number(v).toLocaleString('zh-CN',{minimumFractionDigits:2}) }
function formatMoneyShort(v) { if(!v) return '0'; return (Number(v)/10000).toFixed(1) }
function getStatusLabel(s) { return {MATCHED:'已匹配',PUSHED:'待确认',CONFIRMED:'已确认',FULFILLED:'已兑付',ARCHIVED:'已归档'}[s]||s }

var statusOrder = {MATCHED:0, PUSHED:1, CONFIRMED:2, FULFILLED:3, ARCHIVED:4}
function hasStatus(s) { return statusOrder[timelineRecord.value.status] >= statusOrder[s] }
function showTimeline(row) { timelineRecord.value = row; timelineVisible.value = true }

function handleConfirm(row) {
  proxy.$modal.confirm('确认接受「'+row.policyName+'」？金额 '+formatMoney(row.fundAmount)+' 元将拨付至您的银行账户。').then(() => {
    userConfirm(row.recordId).then(() => { proxy.$modal.msgSuccess('确认成功！'); loadList() })
  }).catch(()=>{})
}
function handleExport() { proxy.download("/biz/matchRecord/export", {}, "匹配兑现记录.xlsx") }
function loadList() { loading.value = true; getMyRecords().then(r => { recordList.value = r.rows||[]; loading.value = false }) }
onMounted(() => loadList())
onActivated(() => loadList())
</script>
