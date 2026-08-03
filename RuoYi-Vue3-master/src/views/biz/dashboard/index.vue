<template>
  <div class="dashboard-page">
    <div class="page-header">
      <div class="header-left">
        <h2>免申即享 · 管理控制台</h2>
        <p>从"人找政策"到"政策找人" — 零申请 · 零材料 · 零跑腿 · 自动兑付</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="$router.push('/biz/matchRecord')"><el-icon><Cpu /></el-icon> 智能比对</el-button>
        <el-button @click="$router.push('/biz/policy')"><el-icon><Memo /></el-icon> 政策管理</el-button>
        <el-button @click="$router.push('/biz/dataScreen')"><el-icon><DataLine /></el-icon> 数据大屏</el-button>
      </div>
    </div>

    <div class="stat-cards">
      <div class="stat-card" v-for="s in statCards" :key="s.key" @click="s.route && $router.push(s.route)">
        <div class="sc-icon" :style="{background: s.bgColor}"><el-icon :size="20"><component :is="s.icon" /></el-icon></div>
        <div class="sc-info">
          <div class="sc-value">{{ s.value }}</div>
          <div class="sc-label">{{ s.label }}</div>
        </div>
        <el-icon class="sc-arrow"><ArrowRight /></el-icon>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :span="16">
        <div class="section-card">
          <div class="sec-header">
            <h3>五步闭环流转</h3>
            <el-button text type="primary" @click="$router.push('/biz/matchRecord')">查看全部 <el-icon><ArrowRight /></el-icon></el-button>
          </div>
          <div class="pipeline">
            <div class="pipe-step" v-for="(step, i) in steps" :key="step.status" :class="{active: getStepCount(step.status) > 0}">
              <div class="ps-dot" :style="{background: step.color}"></div>
              <div class="ps-info">
                <div class="ps-name">{{ step.name }}</div>
                <div class="ps-count">{{ getStepCount(step.status) }} 笔</div>
              </div>
              <div v-if="i < steps.length - 1" class="ps-line"></div>
            </div>
          </div>
          <el-table :data="recentRecords" size="small" stripe>
            <el-table-column label="政策" prop="policyName" min-width="180" show-overflow-tooltip />
            <el-table-column label="对象" prop="targetName" min-width="120" show-overflow-tooltip />
            <el-table-column label="金额" align="right" width="120">
              <template #default="s"><span class="money-text">{{ formatMoney(s.row.fundAmount) }}</span></template>
            </el-table-column>
            <el-table-column label="状态" align="center" width="100">
              <template #default="s"><el-tag :type="statusTag(s.row.status)" size="small">{{ statusLabel(s.row.status) }}</el-tag></template>
            </el-table-column>
            <el-table-column label="风控" align="center" width="80">
              <template #default="s"><el-tag :type="riskTag(s.row.riskLevel)" size="small">{{ riskLabel(s.row.riskLevel) }}</el-tag></template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="section-card">
          <div class="sec-header"><h3>效能对比</h3></div>
          <div class="compare-item old">
            <div class="ci-label">传统模式 · "人找政策"</div>
            <div class="ci-metric"><span class="ci-num">15~30</span><span class="ci-unit">天</span></div>
            <div class="ci-desc">多轮报送、现场跑腿、人工盖章</div>
          </div>
          <div class="compare-item new">
            <div class="ci-label">免申即享 · "政策找人"</div>
            <div class="ci-metric"><span class="ci-num">&lt;1</span><span class="ci-unit">秒</span></div>
            <div class="ci-desc">零材料、智能匹配、资金直达</div>
          </div>
          <div class="exempt-rate">
            <span class="er-label">绿色通道免审率</span>
            <el-progress :percentage="exemptRate" :stroke-width="10" :color="'#0052ff'" />
          </div>
        </div>

        <div class="section-card" style="margin-top:20px;">
          <div class="sec-header">
            <h3>风控概览</h3>
            <el-button text type="primary" @click="$router.push('/biz/matchRecord')">详情 <el-icon><ArrowRight /></el-icon></el-button>
          </div>
          <div class="risk-row">
            <div class="risk-item low"><div class="ri-count">{{ riskLow }}</div><div class="ri-label">低风险</div></div>
            <div class="risk-item mid"><div class="ri-count">{{ riskMid }}</div><div class="ri-label">中风险</div></div>
            <div class="risk-item high"><div class="ri-count">{{ riskHigh }}</div><div class="ri-label">高风险</div></div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top:20px;">
      <el-col :span="12">
        <div class="section-card">
          <div class="sec-header">
            <h3>资金池预算</h3>
            <el-button text type="primary" @click="$router.push('/biz/fund')">管理 <el-icon><ArrowRight /></el-icon></el-button>
          </div>
          <div v-for="fund in fundList" :key="fund.fundId" class="fund-row">
            <div class="fr-head"><span class="fr-name">{{ fund.policyName || '政策#' + fund.policyId }}</span><span class="fr-amount">{{ formatMoney(fund.usedAmount) }} / {{ formatMoney(fund.totalBudget) }}</span></div>
            <el-progress :percentage="fund.totalBudget > 0 ? Math.min(Math.round((fund.usedAmount / fund.totalBudget) * 100), 100) : 0" :stroke-width="8" :color="fundColor(fund)" />
          </div>
          <el-empty v-if="fundList.length === 0" description="暂无资金池数据" :image-size="50" />
        </div>
      </el-col>
      <el-col :span="12">
        <div class="section-card">
          <div class="sec-header">
            <h3>政策到期预警</h3>
            <el-button text type="primary" @click="$router.push('/biz/policy')">管理 <el-icon><ArrowRight /></el-icon></el-button>
          </div>
          <div v-if="expiringPolicies.length > 0" class="expire-list">
            <div v-for="p in expiringPolicies" :key="p.policyId" class="expire-row" :class="{urgent: p.daysLeft <= 30}">
              <div class="er-left">
                <el-icon v-if="p.daysLeft <= 30" color="#ef4444"><WarningFilled /></el-icon>
                <el-icon v-else color="#f59e0b"><AlarmClock /></el-icon>
                <span>{{ p.policyName }}</span>
              </div>
              <div class="er-right">
                <span class="er-date">{{ p.endDate }}</span>
                <el-tag :type="p.daysLeft <= 30 ? 'danger' : 'warning'" size="small">剩余{{ p.daysLeft }}天</el-tag>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无即将到期的政策" :image-size="50" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="EeDashboard">
import { listEePolicy } from "@/api/biz/policy"
import { listEeRule } from "@/api/biz/rule"
import { listEeTargetData } from "@/api/biz/targetData"
import { listEeFund } from "@/api/biz/fund"
import { listEeMatchRecord } from "@/api/biz/matchRecord"
import { ref, computed, onMounted } from 'vue'

const policyCount = ref(0)
const ruleCount = ref(0)
const targetCount = ref(0)
const matchRecords = ref([])
const fundList = ref([])
const policies = ref([])

const statCards = computed(() => [
  { key:'policy', label:'生效政策', value:policyCount.value, icon:'Memo', bgColor:'#e0e7ff', route:'/biz/policy' },
  { key:'rule', label:'匹配规则', value:ruleCount.value, icon:'SetUp', bgColor:'#d1fae5', route:'/biz/rule' },
  { key:'target', label:'目标对象', value:targetCount.value, icon:'User', bgColor:'#fef3c7', route:'/biz/targetData' },
  { key:'match', label:'匹配记录', value:matchRecords.value.length, icon:'Cpu', bgColor:'#fce7f3', route:'/biz/matchRecord' },
  { key:'fulfilled', label:'已兑付(万)', value:formatMoneyShort(fulfilledAmount.value), icon:'Money', bgColor:'#ede9fe', route:'/biz/fund' },
  { key:'archived', label:'已归档', value:archivedCount.value, icon:'CircleCheck', bgColor:'#e0f2fe', route:'/biz/matchRecord' }
])

const expiringPolicies = computed(() => {
  const now = new Date()
  return policies.value
    .filter(p => p.status === '0' && p.endDate)
    .map(p => { const end = new Date(p.endDate); const daysLeft = Math.ceil((end - now) / (1000 * 60 * 60 * 24)); return { ...p, daysLeft } })
    .filter(p => p.daysLeft > 0 && p.daysLeft <= 180)
    .sort((a, b) => a.daysLeft - b.daysLeft)
})

const steps = [
  { name:"系统比对", status:"MATCHED", color:"#0052ff" },
  { name:"精准推送", status:"PUSHED", color:"#06b6d4" },
  { name:"意愿确认", status:"CONFIRMED", color:"#f59e0b" },
  { name:"自动兑付", status:"FULFILLED", color:"#10b981" },
  { name:"公示归档", status:"ARCHIVED", color:"#8b5cf6" }
]

const fulfilledAmount = computed(() => matchRecords.value.filter(r => r.status==='FULFILLED'||r.status==='ARCHIVED').reduce((s,r) => s+Number(r.fundAmount||0), 0))
const archivedCount = computed(() => matchRecords.value.filter(r => r.status==='ARCHIVED').length)
const recentRecords = computed(() => matchRecords.value.slice(0, 5))
const exemptRate = computed(() => { if (!matchRecords.value.length) return 0; return Math.round(matchRecords.value.filter(r => r.riskLevel==='0').length / matchRecords.value.length * 100) })
const riskLow = computed(() => matchRecords.value.filter(r => r.riskLevel==='0').length)
const riskMid = computed(() => matchRecords.value.filter(r => r.riskLevel==='1').length)
const riskHigh = computed(() => matchRecords.value.filter(r => r.riskLevel==='2').length)

function getStepCount(status) { return matchRecords.value.filter(r => r.status===status).length }
function formatMoney(v) { if(!v) return '0.00'; return Number(v).toLocaleString('zh-CN',{minimumFractionDigits:2,maximumFractionDigits:2}) }
function formatMoneyShort(v) { if(!v) return '0'; return (Number(v)/10000).toFixed(1) }
function fundColor(f) { const r = f.totalBudget>0?(f.usedAmount/f.totalBudget)*100:0; if(r>=90) return '#ef4444'; if(r>=70) return '#f59e0b'; return '#0052ff' }
function statusTag(s) { return {MATCHED:'primary',PUSHED:'info',CONFIRMED:'warning',FULFILLED:'success',ARCHIVED:'success'}[s]||'' }
function statusLabel(s) { return {MATCHED:'已匹配',PUSHED:'已推送',CONFIRMED:'意愿确认',FULFILLED:'已兑付',ARCHIVED:'已归档'}[s]||s }
function riskTag(l) { return {'0':'success','1':'warning','2':'danger'}[l]||'info' }
function riskLabel(l) { return {'0':'低','1':'中','2':'高'}[l]||'未评' }

function loadAll() {
  listEePolicy().then(r => { const rows = r.rows || []; policyCount.value = rows.filter(p => p.status==='0').length; policies.value = rows })
  listEeRule().then(r => { ruleCount.value = (r.rows||[]).filter(r2 => r2.status==='0').length })
  listEeTargetData().then(r => { targetCount.value = (r.rows||[]).filter(t => t.status==='0').length })
  listEeFund().then(r => { fundList.value = r.rows||[] })
  listEeMatchRecord().then(r => { matchRecords.value = r.rows||[] })
}
onMounted(() => { loadAll() })
</script>

<style lang="scss" scoped>
.dashboard-page { padding: 24px; background: #f5f7fa; min-height: calc(100vh - 84px); }

.page-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;
  h2 { font-size: 20px; font-weight: 700; color: #0f172a; margin: 0 0 4px; }
  p { font-size: 13px; color: #64748b; margin: 0; }
}
.header-actions { display: flex; gap: 8px; }

.stat-cards {
  display: grid; grid-template-columns: repeat(6, 1fr); gap: 16px; margin-bottom: 24px;
}
.stat-card {
  display: flex; align-items: center; gap: 12px; padding: 16px 18px;
  background: #fff; border: 1px solid #e2e8f0; border-radius: 10px;
  cursor: pointer; transition: all 0.2s;
  &:hover { border-color: #0052ff; box-shadow: 0 2px 8px rgba(0,82,255,0.08); transform: translateY(-1px); }
}
.sc-icon { width: 40px; height: 40px; border-radius: 10px; display: flex; align-items: center; justify-content: center; color: #0052ff; flex-shrink: 0; }
.sc-value { font-size: 22px; font-weight: 700; color: #0f172a; font-family: "Outfit", sans-serif; }
.sc-label { font-size: 12px; color: #64748b; margin-top: 2px; }
.sc-arrow { margin-left: auto; color: #cbd5e1; font-size: 14px; }

.section-card {
  background: #fff; border: 1px solid #e2e8f0; border-radius: 10px; padding: 20px;
}
.sec-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
  h3 { font-size: 15px; font-weight: 600; color: #0f172a; margin: 0; }
}

.pipeline { display: flex; align-items: center; margin-bottom: 16px; gap: 0; }
.pipe-step {
  display: flex; align-items: center; gap: 8px; flex: 1; position: relative;
  &.active .ps-dot { box-shadow: 0 0 0 3px rgba(0,82,255,0.15); }
}
.ps-dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; opacity: 0.4; transition: all 0.2s; }
.pipe-step.active .ps-dot { opacity: 1; }
.ps-name { font-size: 12px; color: #64748b; font-weight: 500; }
.pipe-step.active .ps-name { color: #0f172a; }
.ps-count { font-size: 11px; color: #94a3b8; }
.ps-line { flex: 1; height: 2px; background: #e2e8f0; margin: 0 4px; min-width: 16px; }

.money-text { font-weight: 600; color: #0052ff; font-family: Consolas, monospace; font-size: 13px; }

.compare-item { padding: 14px; border-radius: 8px; margin-bottom: 10px; }
.compare-item.old { background: #fef2f2; border: 1px solid #fecaca; }
.compare-item.new { background: #f0fdf4; border: 1px solid #bbf7d0; }
.ci-label { font-size: 12px; font-weight: 600; margin-bottom: 6px; }
.compare-item.old .ci-label { color: #dc2626; }
.compare-item.new .ci-label { color: #16a34a; }
.ci-metric { display: flex; align-items: baseline; gap: 4px; }
.ci-num { font-size: 24px; font-weight: 700; font-family: "Outfit", sans-serif; }
.compare-item.old .ci-num { color: #dc2626; }
.compare-item.new .ci-num { color: #16a34a; }
.ci-unit { font-size: 12px; color: #64748b; }
.ci-desc { font-size: 11px; color: #94a3b8; margin-top: 4px; }

.exempt-rate { margin-top: 12px; }
.er-label { font-size: 12px; color: #64748b; display: block; margin-bottom: 6px; }

.risk-row { display: flex; gap: 12px; }
.risk-item { flex: 1; text-align: center; padding: 14px; border-radius: 8px; }
.risk-item.low { background: #f0fdf4; border: 1px solid #bbf7d0; }
.risk-item.mid { background: #fffbeb; border: 1px solid #fde68a; }
.risk-item.high { background: #fef2f2; border: 1px solid #fecaca; }
.ri-count { font-size: 24px; font-weight: 700; font-family: "Outfit", sans-serif; }
.risk-item.low .ri-count { color: #16a34a; }
.risk-item.mid .ri-count { color: #d97706; }
.risk-item.high .ri-count { color: #dc2626; }
.ri-label { font-size: 12px; color: #64748b; margin-top: 4px; }

.fund-row { margin-bottom: 14px; }
.fr-head { display: flex; justify-content: space-between; margin-bottom: 4px; }
.fr-name { font-size: 13px; font-weight: 500; color: #334155; }
.fr-amount { font-size: 12px; color: #94a3b8; font-family: Consolas, monospace; }

.expire-list { display: flex; flex-direction: column; gap: 8px; }
.expire-row { display: flex; justify-content: space-between; align-items: center; padding: 10px 14px; border-radius: 8px; background: #f8fafc; border: 1px solid #e2e8f0; }
.expire-row.urgent { background: #fef2f2; border-color: #fecaca; }
.er-left { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #334155; }
.er-right { display: flex; align-items: center; gap: 12px; }
.er-date { font-size: 12px; color: #94a3b8; }
</style>
