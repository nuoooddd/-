<template>
  <div class="app-container" style="background:#f6f8fb;min-height:calc(100vh-84px);padding:20px">
    <div class="pub-header">
      <h2>资金公示</h2>
      <p>公开透明 · 接受监督 · 免申即享资金兑付公示</p>
    </div>

    <el-row :gutter="16" class="mb20">
      <el-col :span="6">
        <div class="glass-card stat-primary">
          <div class="card-value">{{ fulfilledCount }}</div>
          <div class="card-label">已兑付笔数</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-card stat-success">
          <div class="card-value">￥{{ formatMoney(totalAmount) }}</div>
          <div class="card-label">兑付总额</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-card stat-warning">
          <div class="card-value">{{ policyCount }}</div>
          <div class="card-label">涉及政策</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-card stat-purple">
          <div class="card-value">{{ targetCount }}</div>
          <div class="card-label">受益对象</div>
        </div>
      </el-col>
    </el-row>

    <el-card style="border-radius:12px;box-shadow:0 4px 12px 0 rgba(0,0,0,0.05)">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span style="font-size:16px;font-weight:bold;display:flex;align-items:center;gap:8px"><el-icon><List /></el-icon> 兑付公示列表</span>
          <el-input v-model="filterName" placeholder="搜索政策/对象名称" clearable style="width:240px" prefix-icon="Search" @input="applyFilter" />
        </div>
      </template>
      <el-table v-loading="loading" :data="filteredList" :header-cell-style="{background:'#f5f7fa',color:'#606266',fontWeight:'bold'}" stripe>
        <el-table-column label="序号" type="index" width="60" align="center" />
        <el-table-column label="政策名称" prop="policyName" min-width="200" show-overflow-tooltip />
        <el-table-column label="受益对象" prop="targetName" min-width="160" show-overflow-tooltip />
        <el-table-column label="兑付金额" align="right" width="130">
          <template #default="s"><span style="color:#c2410c;font-weight:bold">￥{{formatMoney(s.row.fundAmount)}}</span></template>
        </el-table-column>
        <el-table-column label="风控等级" align="center" width="100">
          <template #default="s">
            <el-tag :type="{'0':'success','1':'warning','2':'danger'}[s.row.riskLevel]" size="small" effect="dark">{{{'0':'低风险','1':'中风险','2':'高风险'}[s.row.riskLevel]}}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核方式" align="center" width="100">
          <template #default="s">
            <el-tag v-if="s.row.riskLevel==='0'" type="success" size="small" effect="plain">免审直达</el-tag>
            <el-tag v-else type="warning" size="small" effect="plain">人工审核</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="兑付时间" align="center" width="160">
          <template #default="s">{{ s.row.updateTime || '--' }}</template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="90">
          <template #default="s">
            <el-tag :type="s.row.status==='ARCHIVED'?'info':'success'" size="small">{{s.row.status==='ARCHIVED'?'已归档':'已兑付'}}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
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
.pub-header { margin-bottom: 24px; }
.pub-header h2 { font-size: 22px; font-weight: bold; color: #1a3a5c; margin: 0 0 6px; }
.pub-header p { font-size: 14px; color: #888; margin: 0; }

.glass-card { background: rgba(255,255,255,0.7); backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.4); border-radius: 16px; padding: 20px 16px; display: flex; flex-direction: column; align-items: center; box-shadow: 0 8px 32px 0 rgba(31,38,135,0.05); transition: all 0.3s; }
.glass-card:hover { transform: translateY(-3px); box-shadow: 0 12px 40px 0 rgba(31,38,135,0.1); }
.stat-primary .card-value { color: #1890ff; } .stat-success .card-value { color: #52c41a; } .stat-warning .card-value { color: #e6a23c; } .stat-purple .card-value { color: #722ed1; }
.card-value { font-size: 26px; font-weight: bold; }
.card-label { font-size: 12px; color: #8c8c8c; margin-top: 6px; }

.mb20 { margin-bottom: 20px; }
</style>