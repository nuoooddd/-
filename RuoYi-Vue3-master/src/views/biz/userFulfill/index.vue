<template>
  <div class="app-container" style="background:#f6f8fb;min-height:calc(100vh-84px);padding:20px">

    <el-row :gutter="20" class="mb20">
      <el-col :span="6">
        <div class="glass-card stat-primary">
          <div class="card-icon"><el-icon><Money /></el-icon></div>
          <div class="card-info"><div class="card-value">{{ fulFilledCount }} <span class="unit">笔</span></div><div class="card-label">已兑付笔数</div></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-card stat-success">
          <div class="card-icon"><el-icon><Wallet /></el-icon></div>
          <div class="card-info"><div class="card-value">￥{{ formatMoney(totalAmount) }}</div><div class="card-label">兑付总额</div></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-card stat-cyan">
          <div class="card-icon"><el-icon><Bell /></el-icon></div>
          <div class="card-info"><div class="card-value">{{ pendingCount }} <span class="unit">项</span></div><div class="card-label">待确认数</div></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-card stat-purple">
          <div class="card-icon"><el-icon><CircleCheck /></el-icon></div>
          <div class="card-info"><div class="card-value">{{ passRate }}% <span class="unit">通过</span></div><div class="card-label">免审通过率</div></div>
        </div>
      </el-col>
    </el-row>

    <el-card style="border-radius:12px;box-shadow:0 4px 12px 0 rgba(0,0,0,0.05)">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span style="font-size:16px;font-weight:bold;display:flex;align-items:center;gap:8px"><el-icon><List /></el-icon> 兑付明细</span>
          <el-button type="success" plain icon="Download" @click="handleExport" style="border-radius:8px">导出Excel</el-button>
        </div>
      </template>
      <el-table v-loading="loading" :data="recordList" :header-cell-style="{background:'#f5f7fa',color:'#606266',fontWeight:'bold'}">
        <el-table-column label="政策名称" prop="policyName" min-width="220" show-overflow-tooltip />
        <el-table-column label="兑现金额" align="right" width="130">
          <template #default="s"><span style="color:#c2410c;font-weight:bold">￥{{formatMoney(s.row.fundAmount)}}</span></template>
        </el-table-column>
        <el-table-column label="流程阶段" align="center" width="100">
          <template #default="s">
            <el-tag :type="{MATCHED:'primary',PUSHED:'warning',CONFIRMED:'',FULFILLED:'success',ARCHIVED:'info'}[s.row.status]" size="small">{{getStatusLabel(s.row.status)}}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="风控" align="center" width="80">
          <template #default="s">
            <el-tag :type="{'0':'success','1':'warning','2':'danger'}[s.row.riskLevel]" size="small" effect="dark">{{{'0':'低·免审','1':'中·审核','2':'高·拦截'}[s.row.riskLevel]}}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="匹配时间" align="center" width="155">
          <template #default="s">{{ s.row.matchTime || s.row.createTime || '--' }}</template>
        </el-table-column>
        <el-table-column label="更新时间" align="center" width="155">
          <template #default="s">{{ s.row.updateTime || '--' }}</template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="180">
          <template #default="s">
            <el-button link type="primary" @click="showTimeline(s.row)">时间线</el-button>
            <el-button v-if="s.row.status==='PUSHED'" link type="primary" @click="handleConfirm(s.row)">确认意愿</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-drawer v-model="timelineVisible" title="流程时间线" size="400px">
      <div class="tl-wrap">
        <div class="tl-node" :class="{active:true}">
          <div class="tl-dot"></div>
          <div class="tl-body">
            <div class="tl-title">智能匹配</div>
            <div class="tl-time">{{ timelineRecord.matchTime || timelineRecord.createTime || '--' }}</div>
            <div class="tl-desc">系统自动比对画像与规则</div>
          </div>
        </div>
        <div class="tl-node" :class="{active: hasStatus('PUSHED') }">
          <div class="tl-dot"></div>
          <div class="tl-body">
            <div class="tl-title">政策推送</div>
            <div class="tl-time">{{ hasStatus('PUSHED') ? (timelineRecord.updateTime || '--') : '等待中' }}</div>
            <div class="tl-desc">政策已推送至企业待办</div>
          </div>
        </div>
        <div class="tl-node" :class="{active: hasStatus('CONFIRMED') }">
          <div class="tl-dot"></div>
          <div class="tl-body">
            <div class="tl-title">意愿确认</div>
            <div class="tl-time">{{ hasStatus('CONFIRMED') ? (timelineRecord.updateTime || '--') : '等待中' }}</div>
            <div class="tl-desc">企业已确认享受意愿</div>
          </div>
        </div>
        <div class="tl-node" :class="{active: hasStatus('FULFILLED') }">
          <div class="tl-dot"></div>
          <div class="tl-body">
            <div class="tl-title">自动兑付</div>
            <div class="tl-time">{{ hasStatus('FULFILLED') ? (timelineRecord.updateTime || '--') : '等待中' }}</div>
            <div class="tl-desc">资金已拨付至企业账户</div>
          </div>
        </div>
        <div class="tl-node" :class="{active: hasStatus('ARCHIVED') }">
          <div class="tl-dot"></div>
          <div class="tl-body">
            <div class="tl-title">公示归档</div>
            <div class="tl-time">{{ hasStatus('ARCHIVED') ? (timelineRecord.updateTime || '--') : '等待中' }}</div>
            <div class="tl-desc">已完成公示并归档</div>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
.glass-card { background:rgba(255,255,255,0.7); backdrop-filter:blur(10px); border:1px solid rgba(255,255,255,0.4); border-radius:16px; padding:20px; display:flex; flex-direction:column; box-shadow:0 8px 32px 0 rgba(31,38,135,0.05); transition:all 0.3s; position:relative; overflow:hidden; }
.glass-card::before { content:""; position:absolute; top:0; left:0; width:100%; height:5px; }
.glass-card:hover { transform:translateY(-5px); box-shadow:0 12px 40px 0 rgba(31,38,135,0.1); }
.stat-primary::before { background:linear-gradient(90deg,#1890ff,#36cfc9); }
.stat-success::before { background:linear-gradient(90deg,#52c41a,#bae637); }
.stat-cyan::before { background:linear-gradient(90deg,#13c2c2,#87e8de); }
.stat-purple::before { background:linear-gradient(90deg,#722ed1,#b37feb); }
.card-icon { font-size:24px; width:44px; height:44px; border-radius:10px; background:rgba(24,144,255,0.1); color:#1890ff; display:flex; align-items:center; justify-content:center; margin-bottom:15px; }
.stat-success .card-icon { background:rgba(82,196,26,0.1); color:#52c41a; }
.stat-cyan .card-icon { background:rgba(19,194,194,0.1); color:#13c2c2; }
.stat-purple .card-icon { background:rgba(114,46,209,0.1); color:#722ed1; }
.card-value { font-size:28px; font-weight:bold; color:#262626; font-family:'Outfit',sans-serif; }
.card-value .unit { font-size:14px; font-weight:normal; color:#8c8c8c; margin-left:5px; }
.card-label { font-size:13px; color:#8c8c8c; margin-top:5px; }
.mb20 { margin-bottom:20px; }
.tl-wrap { padding:0 12px; }
.tl-node { display:flex; gap:16px; padding-bottom:28px; position:relative; }
.tl-node::before { content:''; position:absolute; left:7px; top:18px; bottom:0; width:2px; background:#e8e8e8; }
.tl-node:last-child::before { display:none; }
.tl-node.active .tl-dot { background:#1890ff; box-shadow:0 0 0 4px rgba(24,144,255,.2); }
.tl-dot { width:16px; height:16px; border-radius:50%; background:#d9d9d9; flex-shrink:0; margin-top:2px; }
.tl-body { flex:1; }
.tl-title { font-weight:600; font-size:15px; }
.tl-time { color:#999; font-size:12px; margin:2px 0; }
.tl-desc { color:#bbb; font-size:12px; }
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
const passRate = computed(() => {
  const total = recordList.value.length
  if (total === 0) return 0
  const exempt = recordList.value.filter(r => r.riskLevel === '0').length
  return Math.round((exempt / total) * 100)
})

function formatMoney(v) { if(!v) return '0.00'; return Number(v).toLocaleString('zh-CN',{minimumFractionDigits:2}) }
function getStatusLabel(s) { return {MATCHED:'已匹配',PUSHED:'待确认',CONFIRMED:'已确认·待拨付',FULFILLED:'已兑付',ARCHIVED:'已归档'}[s]||s }

var statusOrder = {MATCHED:0, PUSHED:1, CONFIRMED:2, FULFILLED:3, ARCHIVED:4}
function hasStatus(s) { return statusOrder[timelineRecord.value.status] >= statusOrder[s] }

function showTimeline(row) { timelineRecord.value = row; timelineVisible.value = true }

function handleConfirm(row) {
  proxy.$modal.confirm('确认接受「'+row.policyName+'」？金额 '+formatMoney(row.fundAmount)+' 元将拨付至您的银行账户。').then(() => {
    userConfirm(row.recordId).then(() => { proxy.$modal.msgSuccess('确认成功！等待管理员拨付资金，到账后将自动通知您。'); loadList() })
  }).catch(()=>{})
}

function handleExport() { proxy.download("/biz/matchRecord/export", {}, "匹配兑现记录.xlsx") }
function loadList() { loading.value = true; getMyRecords().then(r => { recordList.value = r.rows||[]; loading.value = false }) }
onMounted(() => loadList())
onActivated(() => loadList())
</script>