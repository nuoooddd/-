<template>
  <div class="m-page">
    <div class="stat-row">
      <div class="stat-item" style="background:#e0e7ff"><div class="si-val">{{ policies.length }}</div><div class="si-label">总数</div></div>
      <div class="stat-item" style="background:#d1fae5"><div class="si-val">{{ activePolicies.length }}</div><div class="si-label">生效中</div></div>
      <div class="stat-item" style="background:#fef3c7"><div class="si-val">{{ expiringPolicies.length }}</div><div class="si-label">即将到期</div></div>
      <div class="stat-item" style="background:#fef2f2"><div class="si-val">{{ expiredPolicies.length }}</div><div class="si-label">已到期</div></div>
    </div>

    <div class="filter-bar">
      <el-radio-group v-model="viewMode" size="small">
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="active">生效</el-radio-button>
        <el-radio-button label="expiring">临期</el-radio-button>
        <el-radio-button label="expired">到期</el-radio-button>
      </el-radio-group>
    </div>

    <div class="section-title">政策时间轴</div>
    <div class="timeline-container">
      <div v-for="policy in filteredPolicies" :key="policy.policyId" class="tl-item" :class="getPolicyClass(policy)" @click="openDetail(policy)">
        <div class="tl-dot" :style="{background:getPolicyColor(policy)}"></div>
        <div class="tl-body">
          <div class="tl-top">
            <span class="tl-name">{{ policy.policyName }}</span>
            <el-tag :type="getPolicyStatusTag(policy)" size="small" effect="dark">{{ getPolicyStatusLabel(policy) }}</el-tag>
          </div>
          <div class="tl-meta">
            <span>{{ policy.publishDept || '--' }}</span>
            <span>{{ formatMoney(policy.amount) }}元</span>
          </div>
          <div class="tl-date">截止: {{ policy.endDate || '长期' }}</div>
          <div v-if="isExpiring(policy)" class="tl-warn"><el-icon><WarningFilled /></el-icon> 即将到期</div>
          <div v-if="isExpired(policy)" class="tl-danger"><el-icon><CircleCloseFilled /></el-icon> 已到期</div>
        </div>
      </div>
      <el-empty v-if="filteredPolicies.length===0" description="暂无政策" :image-size="50" />
    </div>

    <div class="section-title" style="margin-top:16px">预警通知</div>
    <div v-if="expiringPolicies.length>0">
      <div v-for="p in expiringPolicies" :key="'w'+p.policyId" class="alert-card alert-warn">
        <el-icon color="#e6a23c"><WarningFilled /></el-icon>
        <div><div class="al-title">{{ p.policyName }}</div><div class="al-desc">即将到期 · {{ p.publishDept }}</div></div>
      </div>
    </div>
    <div v-if="expiredPolicies.length>0">
      <div v-for="p in expiredPolicies" :key="'d'+p.policyId" class="alert-card alert-danger">
        <el-icon color="#f56c6c"><CircleCloseFilled /></el-icon>
        <div><div class="al-title">{{ p.policyName }}</div><div class="al-desc">已到期 · 请及时处理</div></div>
      </div>
    </div>
    <el-empty v-if="expiringPolicies.length===0 && expiredPolicies.length===0" description="暂无预警" :image-size="40" />

    <div class="section-title" style="margin-top:16px">类型分布</div>
    <div class="type-dist">
      <div class="type-item"><span class="type-label">补贴类</span><el-progress :percentage="getTypePercent('1')" :stroke-width="8" color="#1890ff" /></div>
      <div class="type-item"><span class="type-label">奖励类</span><el-progress :percentage="getTypePercent('2')" :stroke-width="8" color="#52c41a" /></div>
      <div class="type-item"><span class="type-label">减免类</span><el-progress :percentage="getTypePercent('3')" :stroke-width="8" color="#e6a23c" /></div>
    </div>

    <el-dialog v-model="detailVisible" :title="detailPolicy?.policyName" width="92%" destroy-on-close>
      <div v-if="detailPolicy" class="detail-body">
        <div class="detail-tags">
          <el-tag :type="{'1':'','2':'success','3':'warning'}[detailPolicy.policyType]" size="small">{{ {'1':'补贴','2':'奖励','3':'减免'}[detailPolicy.policyType] }}</el-tag>
          <el-tag :type="detailPolicy.status==='0'?'success':'info'" size="small">{{ detailPolicy.status==='0'?'生效中':'已停用' }}</el-tag>
        </div>
        <div v-if="detailPolicy.amount" class="detail-amount">
          <span>补贴金额</span><span class="da-val">¥ {{ Number(detailPolicy.amount).toLocaleString() }}</span>
        </div>
        <div class="detail-row"><span>发布部门</span><span>{{ detailPolicy.publishDept||'--' }}</span></div>
        <div class="detail-row"><span>有效期</span><span>{{ detailPolicy.startDate||'?' }} ~ {{ detailPolicy.endDate||'长期' }}</span></div>
        <div v-if="detailPolicy.remark" class="detail-remark">{{ detailPolicy.remark }}</div>
        <div v-if="detailRules.length>0" class="detail-rules">
          <div class="dr-title">匹配条件</div>
          <div v-for="(rule,ri) in detailRules" :key="ri" class="dr-item">
            <div class="dr-name">{{ rule.ruleName }}</div>
            <div v-for="(cond,ci) in rule.parsedConds" :key="ci" class="dr-cond">
              <span class="dc-field">{{ fieldNameMap[cond.field]||cond.field }}</span>
              <span class="dc-op">{{ opMap[cond.op]||cond.op }}</span>
              <span class="dc-val">{{ cond.displayVal }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="EePolicyCalendar">
import { ref, computed, onMounted } from 'vue'
import { listEeRule } from '@/api/biz/rule'
import { listEePolicy } from '@/api/biz/policy'

const allRules = ref([])
const detailVisible = ref(false)
const detailPolicy = ref(null)
const detailRules = ref([])
const policies = ref([])
const viewMode = ref('all')

const activePolicies = computed(() => policies.value.filter(p => p.status==='0' && !isExpired(p)))
const expiringPolicies = computed(() => policies.value.filter(p => p.status==='0' && isExpiring(p)))
const expiredPolicies = computed(() => policies.value.filter(p => isExpired(p)))
const filteredPolicies = computed(() => {
  if (viewMode.value==='active') return activePolicies.value
  if (viewMode.value==='expiring') return expiringPolicies.value
  if (viewMode.value==='expired') return expiredPolicies.value
  return policies.value
})

function isExpiring(p) { if (!p.endDate) return false; const d = new Date(p.endDate).getTime()-Date.now(); return d>0 && d<7*24*60*60*1000 }
function isExpired(p) { if (!p.endDate) return false; return new Date(p.endDate).getTime()<Date.now() }
function getPolicyColor(p) { if (isExpired(p)) return '#f56c6c'; if (isExpiring(p)) return '#e6a23c'; if (p.status==='0') return '#52c41a'; return '#c0c4cc' }
function getPolicyClass(p) { if (isExpired(p)) return 'expired'; if (isExpiring(p)) return 'expiring'; return 'active' }
function getPolicyStatusTag(p) { if (isExpired(p)) return 'danger'; if (isExpiring(p)) return 'warning'; if (p.status==='0') return 'success'; return 'info' }
function getPolicyStatusLabel(p) { if (isExpired(p)) return '已到期'; if (isExpiring(p)) return '即将到期'; if (p.status==='0') return '生效中'; return '已停用' }
function formatMoney(v) { if (!v) return '0.00'; return Number(v).toLocaleString('zh-CN',{minimumFractionDigits:2}) }
function getTypePercent(t) { if (!policies.value.length) return 0; return Math.round(policies.value.filter(p=>p.policyType===t).length/policies.value.length*100) }

const fieldNameMap = { industry:'所属行业',scale:'企业规模',registered_capital:'注册资本(万)',high_tech:'高新技术企业',business_status:'经营状态',revenue:'年营收(万)',research_spend:'研发投入(万)',growth_rate:'营收增长率(%)',disabled:'残疾人雇佣比例(%)',tax_status:'税收状态',credit_score:'信用评分',green_certified:'绿色认证',social_insurance_cnt:'社保人数',carbon_grade:'碳排放等级',age:'年龄',gender:'性别',income:'年收入(万)',education:'学历' }
const opMap = { '>=':'≥','<=':'≤','==':'=','!=':'≠','>':'>','<':'<' }
const fieldValMap = { tax_status:{normal:'正常',abnormal:'异常'},scale:{large:'大型',medium:'中型',small:'小型',micro:'微型'},industry:{'High-tech':'高新技术','Manufacturing':'制造业','Retail':'零售业','Finance':'金融业','Service':'服务业','Agriculture':'农业'},gender:{male:'男',female:'女'},education:{bachelor:'本科',master:'硕士',doctor:'博士',highschool:'高中'},carbon_grade:{A:'A级',B:'B级',C:'C级',D:'D级'} }

function parseConditions(expr) {
  if (!expr) return []
  const parts = []
  for (const orPart of expr.split(/\s+or\s+/i)) {
    for (const part of orPart.split(/\s+and\s+/i)) {
      const m = part.trim().match(/^(.+?)(>=|<=|==|!=|>|<)(.+)$/)
      if (m) { const field=m[1].trim(),op=m[2].trim(); let val=m[3].trim().replace(/^['"]|['"]$/g,''); let displayVal=val; if(fieldValMap[field]&&fieldValMap[field][val]) displayVal=fieldValMap[field][val]; else if(field==='high_tech'||field==='green_certified') displayVal=val==='true'?'是':'否'; parts.push({field,op,val,displayVal}) }
    }
  }
  return parts
}

function openDetail(policy) {
  detailPolicy.value = policy
  const rules = allRules.value.filter(r => r.policyId===policy.policyId && r.status==='0')
  detailRules.value = rules.map(r => ({...r, parsedConds:parseConditions(r.conditionExpr)}))
  detailVisible.value = true
}

function getList() {
  listEePolicy({pageNum:1,pageSize:9999}).then(res => { policies.value = res.rows||[] })
  listEeRule({pageNum:1,pageSize:9999}).then(res => { allRules.value = res.rows||[] })
}
onMounted(() => { getList() })
</script>

<style scoped>
.m-page { padding:12px; background:#f5f6fa; min-height:100%; }
.stat-row { display:flex; gap:6px; margin-bottom:12px; }
.stat-item { flex:1; text-align:center; padding:10px 4px; border-radius:10px; }
.si-val { font-size:16px; font-weight:700; color:#1e293b; font-family:'Outfit',sans-serif; }
.si-label { font-size:10px; color:#64748b; margin-top:2px; }
.filter-bar { margin-bottom:12px; }
.section-title { font-size:14px; font-weight:700; color:#1e293b; margin-bottom:8px; }

.timeline-container { padding-left:16px; }
.tl-item { display:flex; gap:10px; padding-bottom:16px; position:relative; }
.tl-item::before { content:''; position:absolute; left:6px; top:16px; bottom:0; width:2px; background:#e2e8f0; }
.tl-item:last-child::before { display:none; }
.tl-dot { width:14px; height:14px; border-radius:50%; flex-shrink:0; margin-top:4px; }
.tl-body { flex:1; background:#fff; border:1px solid #e2e8f0; border-radius:10px; padding:10px; }
.tl-item.expiring .tl-body { border-left:3px solid #e6a23c; background:#fffbf0; }
.tl-item.expired .tl-body { border-left:3px solid #f56c6c; background:#fff5f5; }
.tl-top { display:flex; justify-content:space-between; align-items:center; margin-bottom:4px; }
.tl-name { font-size:13px; font-weight:600; color:#1e293b; flex:1; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; margin-right:6px; }
.tl-meta { display:flex; gap:10px; font-size:11px; color:#64748b; margin-bottom:4px; }
.tl-date { font-size:11px; color:#94a3b8; }
.tl-warn { color:#e6a23c; font-size:11px; display:flex; align-items:center; gap:4px; margin-top:4px; }
.tl-danger { color:#f56c6c; font-size:11px; display:flex; align-items:center; gap:4px; margin-top:4px; }

.alert-card { display:flex; align-items:flex-start; gap:8px; padding:10px; border-radius:8px; margin-bottom:6px; }
.alert-warn { background:#fffbf0; border:1px solid #ffe58f; }
.alert-danger { background:#fff5f5; border:1px solid #ffa39e; }
.al-title { font-size:13px; font-weight:500; color:#1e293b; }
.al-desc { font-size:11px; color:#64748b; margin-top:2px; }

.type-dist { }
.type-item { margin-bottom:10px; }
.type-label { font-size:12px; color:#64748b; display:block; margin-bottom:4px; }

.detail-body { }
.detail-tags { display:flex; gap:6px; margin-bottom:10px; }
.detail-amount { display:flex; justify-content:space-between; align-items:center; background:linear-gradient(135deg,#fff7ed,#ffedd5); border-radius:10px; padding:10px 14px; margin-bottom:10px; font-size:13px; color:#9a3412; }
.da-val { font-size:18px; font-weight:700; color:#ea580c; }
.detail-row { display:flex; justify-content:space-between; padding:6px 0; font-size:13px; border-bottom:1px solid #f1f5f9; }
.detail-row span:first-child { color:#64748b; }
.detail-row span:last-child { color:#1e293b; font-weight:500; }
.detail-remark { font-size:13px; color:#475569; line-height:1.6; background:#f8fafc; padding:10px; border-radius:8px; margin-top:10px; }
.detail-rules { margin-top:12px; }
.dr-title { font-size:13px; font-weight:600; color:#1e293b; margin-bottom:8px; }
.dr-item { background:#f8fafc; border-radius:8px; padding:10px; margin-bottom:8px; }
.dr-name { font-size:12px; font-weight:500; color:#1e293b; margin-bottom:6px; }
.dr-cond { display:inline-flex; align-items:center; gap:3px; background:#fff; border:1px solid #e2e8f0; border-radius:4px; padding:2px 6px; font-size:11px; margin:2px; }
.dc-field { color:#4f46e5; font-weight:500; }
.dc-op { color:#94a3b8; }
.dc-val { color:#1e293b; font-weight:600; }
</style>
