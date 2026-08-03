<template>
  <div class="fp-page">
    <div class="fp-header">
      <h2>资金公示</h2>
      <p>公开透明 · 接受监督 · 免申即享</p>
    </div>

    <div class="fp-stats">
      <div class="stat-item">
        <div class="stat-val" style="color:#1890ff">{{ fulfilledCount }}</div>
        <div class="stat-label">已兑付</div>
      </div>
      <div class="stat-item">
        <div class="stat-val" style="color:#52c41a">￥{{ formatMoney(totalAmount) }}</div>
        <div class="stat-label">兑付总额</div>
      </div>
      <div class="stat-item">
        <div class="stat-val" style="color:#e6a23c">{{ policyCount }}</div>
        <div class="stat-label">涉及政策</div>
      </div>
      <div class="stat-item">
        <div class="stat-val" style="color:#722ed1">{{ targetCount }}</div>
        <div class="stat-label">受益对象</div>
      </div>
    </div>

    <div class="fp-search">
      <el-input v-model="filterName" placeholder="搜索政策/对象名称" clearable prefix-icon="Search" @input="applyFilter" />
    </div>

    <div class="fp-list" v-loading="loading">
      <div class="fp-card" v-for="(item, idx) in filteredList" :key="idx">
        <div class="fc-top">
          <span class="fc-policy">{{ item.policyName }}</span>
          <el-tag :type="item.status==='ARCHIVED'?'info':'success'" size="small">{{ item.status==='ARCHIVED'?'已归档':'已兑付' }}</el-tag>
        </div>
        <div class="fc-mid">
          <span class="fc-target">{{ item.targetName }}</span>
          <span class="fc-amount">￥{{ formatMoney(item.fundAmount) }}</span>
        </div>
        <div class="fc-bottom">
          <el-tag :type="{'0':'success','1':'warning','2':'danger'}[item.riskLevel]" size="small" effect="dark">{{{'0':'低风险','1':'中风险','2':'高风险'}[item.riskLevel]}}</el-tag>
          <el-tag v-if="item.riskLevel==='0'" type="success" size="small" effect="plain">免审直达</el-tag>
          <el-tag v-else type="warning" size="small" effect="plain">人工审核</el-tag>
          <span class="fc-time">{{ item.updateTime || '--' }}</span>
        </div>
      </div>
      <div v-if="!loading && filteredList.length===0" class="fp-empty">暂无公示数据</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { listEeMatchRecord } from '@/api/biz/matchRecord'

const loading = ref(false)
const filterName = ref('')
const allList = ref([])

const filteredList = computed(() => {
  if (!filterName.value) return allList.value
  const kw = filterName.value.toLowerCase()
  return allList.value.filter(r =>
    (r.policyName || '').toLowerCase().includes(kw) ||
    (r.targetName || '').toLowerCase().includes(kw)
  )
})

const fulfilledCount = computed(() => allList.value.length)
const totalAmount = computed(() => allList.value.reduce((s, r) => s + (r.fundAmount || 0), 0))
const policyCount = computed(() => new Set(allList.value.map(r => r.policyId)).size)
const targetCount = computed(() => new Set(allList.value.map(r => r.targetId)).size)

function formatMoney(v) {
  if (!v && v !== 0) return '0.00'
  return Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function loadData() {
  loading.value = true
  listEeMatchRecord({ status: 'FULFILLED', pageNum: 1, pageSize: 9999 }).then(res => {
    let list = res.rows || []
    listEeMatchRecord({ status: 'ARCHIVED', pageNum: 1, pageSize: 9999 }).then(res2 => {
      list = list.concat(res2.rows || [])
      allList.value = list
      loading.value = false
    })
  }).catch(() => { loading.value = false })
}

function applyFilter() {}

onMounted(loadData)
</script>

<style scoped>
.fp-page { background:#f6f8fb; min-height:100%; padding:12px; }

.fp-header { margin-bottom:12px; }
.fp-header h2 { font-size:18px; font-weight:bold; color:#1a3a5c; margin:0 0 4px; }
.fp-header p { font-size:12px; color:#888; margin:0; }

.fp-stats { display:flex; gap:0; background:rgba(255,255,255,0.8); border-radius:12px; overflow:hidden; margin-bottom:12px; border:1px solid #e8ecf1; }
.stat-item { flex:1; text-align:center; padding:12px 4px; border-right:1px solid rgba(0,0,0,0.04); }
.stat-item:last-child { border-right:none; }
.stat-val { font-size:16px; font-weight:bold; }
.stat-label { font-size:10px; color:#8c8c8c; margin-top:2px; }

.fp-search { margin-bottom:12px; }

.fp-list { display:flex; flex-direction:column; gap:10px; }
.fp-card { background:#fff; border-radius:10px; padding:12px; border:1px solid #e8ecf1; }
.fc-top { display:flex; justify-content:space-between; align-items:center; margin-bottom:8px; }
.fc-policy { font-size:13px; font-weight:600; color:#1e293b; flex:1; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; margin-right:8px; }
.fc-mid { display:flex; justify-content:space-between; align-items:center; margin-bottom:8px; }
.fc-target { font-size:12px; color:#64748b; }
.fc-amount { font-size:14px; font-weight:bold; color:#c2410c; font-family:Consolas,monospace; }
.fc-bottom { display:flex; align-items:center; gap:6px; flex-wrap:wrap; }
.fc-time { font-size:11px; color:#94a3b8; margin-left:auto; }

.fp-empty { padding:40px 20px; text-align:center; color:#94a3b8; font-size:13px; }
</style>
