<template>
  <div class="app-container" style="background: #f6f8fb; min-height: calc(100vh - 84px); padding: 20px;">
    <div class="calendar-header">
      <h2>政策日历与预警</h2>
      <p>政策有效期时间轴 · 到期自动预警 · 关键节点提醒</p>
    </div>

    <el-row :gutter="20" class="mb20">
      <el-col :span="6">
        <div class="glass-card stat-primary">
          <div class="card-value">{{ policies.length }}</div>
          <div class="card-label">政策总数</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-card stat-success">
          <div class="card-value">{{ activePolicies.length }}</div>
          <div class="card-label">生效中</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-card stat-warning">
          <div class="card-value">{{ expiringPolicies.length }}</div>
          <div class="card-label">即将到期(7天内)</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-card stat-danger">
          <div class="card-value">{{ expiredPolicies.length }}</div>
          <div class="card-label">已到期</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-card style="border-radius: 12px; box-shadow: 0 4px 12px 0 rgba(0,0,0,0.05);">
          <template #header>
            <div class="card-header-flex">
              <span class="header-title"><el-icon><Calendar /></el-icon> 政策时间轴</span>
              <el-radio-group v-model="viewMode" size="small">
                <el-radio-button label="all">全部</el-radio-button>
                <el-radio-button label="active">生效中</el-radio-button>
                <el-radio-button label="expiring">即将到期</el-radio-button>
                <el-radio-button label="expired">已到期</el-radio-button>
              </el-radio-group>
            </div>
          </template>

          <div class="timeline-container">
            <div v-for="policy in filteredPolicies" :key="policy.policyId" class="timeline-item" :class="getPolicyClass(policy)">
              <div class="timeline-dot" :style="{ background: getPolicyColor(policy) }"></div>
              <div class="timeline-line"></div>
              <div class="timeline-content">
                <div class="policy-card" :class="getPolicyClass(policy)" @click="openDetail(policy)">
                  <div class="policy-header">
                    <span class="policy-name">{{ policy.policyName }}</span>
                    <el-tag :type="getPolicyStatusTag(policy)" size="small" effect="dark" style="border-radius: 4px;">
                      {{ getPolicyStatusLabel(policy) }}
                    </el-tag>
                  </div>
                  <div class="policy-meta">
                    <span class="meta-item"><el-icon><OfficeBuilding /></el-icon> {{ policy.publishDept || '未指定' }}</span>
                    <span class="meta-item"><el-icon><Money /></el-icon> {{ formatMoney(policy.amount) }}元</span>
                    <span class="meta-item"><el-icon><Clock /></el-icon> 截止: {{ policy.endDate || '长期' }}</span>
                  </div>
                  <div class="policy-type-tag">
                    <el-tag :type="policy.policyType === '1' ? 'primary' : policy.policyType === '2' ? 'success' : 'warning'" size="small" effect="plain">
                      {{ policy.policyType === '1' ? '补贴' : policy.policyType === '2' ? '奖励' : '减免' }}
                    </el-tag>
                  </div>
                  <div v-if="isExpiring(policy)" class="warning-bar">
                    <el-icon color="#e6a23c"><WarningFilled /></el-icon>
                    <span>该政策即将到期，请关注兑付进度</span>
                  </div>
                  <div v-if="isExpired(policy)" class="danger-bar">
                    <el-icon color="#f56c6c"><CircleCloseFilled /></el-icon>
                    <span>该政策已到期，请及时处理</span>
                  </div>
                </div>
              </div>
            </div>
            <el-empty v-if="filteredPolicies.length === 0" description="暂无政策数据" :image-size="80" />
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card style="border-radius: 12px; box-shadow: 0 4px 12px 0 rgba(0,0,0,0.05);">
          <template #header>
            <span class="header-title"><el-icon><Warning /></el-icon> 预警通知</span>
          </template>
          <div v-if="expiringPolicies.length > 0">
            <div v-for="p in expiringPolicies" :key="p.policyId" class="alert-item alert-warning">
              <el-icon color="#e6a23c"><WarningFilled /></el-icon>
              <div class="alert-body">
                <div class="alert-title">{{ p.policyName }}</div>
                <div class="alert-desc">即将到期 · {{ p.publishDept }}</div>
              </div>
            </div>
          </div>
          <div v-if="expiredPolicies.length > 0">
            <div v-for="p in expiredPolicies" :key="'e-'+p.policyId" class="alert-item alert-danger">
              <el-icon color="#f56c6c"><CircleCloseFilled /></el-icon>
              <div class="alert-body">
                <div class="alert-title">{{ p.policyName }}</div>
                <div class="alert-desc">已到期 · 请及时处理</div>
              </div>
            </div>
          </div>
          <el-empty v-if="expiringPolicies.length === 0 && expiredPolicies.length === 0" description="暂无预警" :image-size="60" />
        </el-card>

        <el-card style="border-radius: 12px; box-shadow: 0 4px 12px 0 rgba(0,0,0,0.05); margin-top: 20px;">
          <template #header>
            <span class="header-title"><el-icon><DataLine /></el-icon> 政策类型分布</span>
          </template>
          <div class="type-dist">
            <div class="type-item">
              <span class="type-label">补贴类</span>
              <el-progress :percentage="getTypePercent('1')" :stroke-width="12" color="#1890ff" />
            </div>
            <div class="type-item">
              <span class="type-label">奖励类</span>
              <el-progress :percentage="getTypePercent('2')" :stroke-width="12" color="#52c41a" />
            </div>
            <div class="type-item">
              <span class="type-label">减免类</span>
              <el-progress :percentage="getTypePercent('3')" :stroke-width="12" color="#e6a23c" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  
    <el-dialog v-model="detailVisible" :title="detailPolicy?.policyName" width="650px" destroy-on-close class="policy-detail-dialog">
      <div class="detail-content" v-if="detailPolicy">
        <div class="detail-header">
          <el-tag :type="{'1':'','2':'success','3':'warning'}[detailPolicy.policyType]" size="default">
            {{ {'1':'补贴','2':'奖励','3':'减免'}[detailPolicy.policyType] || detailPolicy.policyType }}
          </el-tag>
          <el-tag :type="detailPolicy.status==='0'?'success':'info'" size="default">
            {{ detailPolicy.status==='0'?'生效中':'已停用' }}
          </el-tag>
        </div>
        <div class="detail-amount" v-if="detailPolicy.amount">
          <span class="amount-label">补贴金额</span>
          <span class="amount-value">¥ {{ Number(detailPolicy.amount).toLocaleString() }}</span>
        </div>
        <el-descriptions :column="2" border size="small" style="margin-bottom:16px">
          <el-descriptions-item label="发布部门">{{ detailPolicy.publishDept || '未指定' }}</el-descriptions-item>
          <el-descriptions-item label="有效期">{{ detailPolicy.startDate }} ~ {{ detailPolicy.endDate }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="detailPolicy.remark" style="margin-bottom:16px">
          <div style="font-size:13px;color:#8c8c8c;margin-bottom:6px">政策说明</div>
          <div style="font-size:14px;color:#475569;line-height:1.7;background:#f8fafc;padding:12px;border-radius:8px">{{ detailPolicy.remark }}</div>
        </div>
        <div v-if="detailPolicy.pdfName" style="margin-bottom:16px;padding:10px 14px;background:#f0f9eb;border-radius:8px;border:1px solid #b7eb8f">
          <span style="font-size:13px;color:#8c8c8c;margin-right:8px">政策文件</span>
          <el-link type="primary" :underline="false" @click="previewPdf(detailPolicy)">{{ detailPolicy.pdfName }}</el-link>
        </div>
        <!-- Rule conditions -->
        <div v-if="detailRules.length > 0" style="margin-top:16px">
          <div style="font-size:14px;font-weight:600;color:#303133;margin-bottom:12px">匹配条件</div>
          <div v-for="(rule, ri) in detailRules" :key="ri" style="margin-bottom:12px;background:#fafafa;border-radius:8px;padding:12px">
            <div style="font-size:13px;font-weight:500;color:#1e293b;margin-bottom:8px">{{ rule.ruleName }}</div>
            <div style="font-size:12px;color:#94a3b8;margin-bottom:6px;font-family:monospace">{{ rule.conditionExpr }}</div>
            <div class="condition-items">
              <div v-for="(cond, ci) in rule.parsedConds" :key="ci" class="cond-row">
                <span class="cond-field">{{ fieldNameMap[cond.field] || cond.field }}</span>
                <span class="cond-op">{{ opMap[cond.op] || cond.op }}</span>
                <span class="cond-val">{{ cond.displayVal }}</span>
              </div>
            </div>
          </div>
        </div>
        <div v-else style="margin-top:16px;text-align:center;color:#94a3b8;padding:20px">
          暂无匹配条件
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

const activePolicies = computed(() => policies.value.filter(p => p.status === '0' && !isExpired(p)))
const expiringPolicies = computed(() => policies.value.filter(p => p.status === '0' && isExpiring(p)))
const expiredPolicies = computed(() => policies.value.filter(p => isExpired(p)))

const filteredPolicies = computed(() => {
  if (viewMode.value === 'active') return activePolicies.value
  if (viewMode.value === 'expiring') return expiringPolicies.value
  if (viewMode.value === 'expired') return expiredPolicies.value
  return policies.value
})

function isExpiring(policy) {
  if (!policy.endDate) return false
  const endTime = new Date(policy.endDate).getTime()
  const now = Date.now()
  const diff = endTime - now
  return diff > 0 && diff < 7 * 24 * 60 * 60 * 1000
}

function isExpired(policy) {
  if (!policy.endDate) return false
  return new Date(policy.endDate).getTime() < Date.now()
}

function getPolicyColor(policy) {
  if (isExpired(policy)) return '#f56c6c'
  if (isExpiring(policy)) return '#e6a23c'
  if (policy.status === '0') return '#52c41a'
  return '#c0c4cc'
}

function getPolicyClass(policy) {
  if (isExpired(policy)) return 'expired'
  if (isExpiring(policy)) return 'expiring'
  return 'active'
}

function getPolicyStatusTag(policy) {
  if (isExpired(policy)) return 'danger'
  if (isExpiring(policy)) return 'warning'
  if (policy.status === '0') return 'success'
  return 'info'
}

function getPolicyStatusLabel(policy) {
  if (isExpired(policy)) return '已到期'
  if (isExpiring(policy)) return '即将到期'
  if (policy.status === '0') return '生效中'
  return '已停用'
}

function formatMoney(val) {
  if (!val) return '0.00'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function getTypePercent(type) {
  if (policies.value.length === 0) return 0
  return Math.round(policies.value.filter(p => p.policyType === type).length / policies.value.length * 100)
}

const fieldNameMap = {
  industry: '所属行业', scale: '企业规模', registered_capital: '注册资本(万)',
  high_tech: '高新技术企业', business_status: '经营状态', revenue: '年营收(万)',
  research_spend: '研发投入(万)', growth_rate: '营收增长率(%)', disabled: '残疾人雇佣比例(%)',
  tax_status: '税收状态', credit_score: '信用评分', green_certified: '绿色认证',
  social_insurance_cnt: '社保人数', carbon_grade: '碳排放等级',
  age: '年龄', gender: '性别', income: '年收入(万)', education: '学历'
}
const opMap = { '>=': '≥', '<=': '≤', '==': '=', '!=': '≠', '>': '>', '<': '<' }
const fieldValMap = {
  tax_status: { normal: '正常', abnormal: '异常' },
  scale: { large: '大型', medium: '中型', small: '小型', micro: '微型' },
  industry: { 'High-tech': '高新技术', 'Manufacturing': '制造业', 'Retail': '零售业', 'Finance': '金融业', 'Service': '服务业', 'Agriculture': '农业' },
  gender: { male: '男', female: '女' },
  education: { bachelor: '本科', master: '硕士', doctor: '博士', highschool: '高中' },
  carbon_grade: { A: 'A级', B: 'B级', C: 'C级', D: 'D级' }
}

function getList() {
  listEePolicy({ pageNum: 1, pageSize: 9999 }).then(res => { policies.value = res.rows || [] })
  listEeRule({ pageNum: 1, pageSize: 9999 }).then(res => { allRules.value = res.rows || [] })
}

function parseConditions(expr) {
  if (!expr) return []
  const parts = []
  const orParts = expr.split(/\s+or\s+/i)
  for (const orPart of orParts) {
    const andParts = orPart.split(/\s+and\s+/i)
    for (const part of andParts) {
      const m = part.trim().match(/^(.+?)(>=|<=|==|!=|>|<)(.+)$/)
      if (m) {
        const field = m[1].trim()
        const op = m[2].trim()
        let val = m[3].trim().replace(/^['"]|['"]$/g, '')
        let displayVal = val
        if (fieldValMap[field] && fieldValMap[field][val]) {
          displayVal = fieldValMap[field][val]
        } else if (field === 'high_tech' || field === 'green_certified') {
          displayVal = val === 'true' ? '是' : '否'
        }
        parts.push({ field, op, val, displayVal })
      }
    }
  }
  return parts
}

function openDetail(policy) {
  detailPolicy.value = policy
  const rules = allRules.value.filter(r => r.policyId === policy.policyId && r.status === '0')
  detailRules.value = rules.map(r => ({
    ...r,
    parsedConds: parseConditions(r.conditionExpr)
  }))
  detailVisible.value = true
}

function previewPdf(policy) {
  if (policy.pdfUrl) {
    const baseURL = import.meta.env.VITE_APP_BASE_API
    window.open(baseURL + policy.pdfUrl, '_blank')
  }
}

onMounted(() => { getList() })
</script>

<style scoped>
.calendar-header { text-align: center; margin-bottom: 24px; }
.calendar-header h2 { font-size: 24px; font-weight: bold; background: linear-gradient(90deg, #1890ff, #722ed1); -webkit-background-clip: text; -webkit-text-fill-color: transparent; margin: 0 0 8px 0; }
.calendar-header p { font-size: 14px; color: #8c8c8c; margin: 0; }

.glass-card { background: rgba(255,255,255,0.7); backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.4); border-radius: 16px; padding: 20px 16px; display: flex; flex-direction: column; align-items: center; box-shadow: 0 8px 32px 0 rgba(31,38,135,0.05); transition: all 0.3s; }
.glass-card:hover { transform: translateY(-3px); box-shadow: 0 12px 40px 0 rgba(31,38,135,0.1); }
.stat-primary .card-value { color: #1890ff; } .stat-success .card-value { color: #52c41a; } .stat-warning .card-value { color: #e6a23c; } .stat-danger .card-value { color: #f56c6c; }
.card-value { font-size: 28px; font-weight: bold; font-family: 'Outfit', sans-serif; }
.card-label { font-size: 12px; color: #8c8c8c; margin-top: 6px; }

.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.header-title { font-size: 16px; font-weight: bold; color: #303133; display: flex; align-items: center; gap: 8px; }

.timeline-container { position: relative; padding-left: 20px; }
.timeline-item { position: relative; padding-left: 30px; padding-bottom: 20px; }
.timeline-dot { position: absolute; left: 0; top: 8px; width: 14px; height: 14px; border-radius: 50%; z-index: 2; }
.timeline-line { position: absolute; left: 6px; top: 22px; width: 2px; height: calc(100% - 22px); background: #e8e8e8; }
.timeline-item:last-child .timeline-line { display: none; }

.policy-card { background: #fafafa; cursor: pointer; border: 1px solid #f0f0f0; border-radius: 10px; padding: 16px; transition: all 0.2s; }
.policy-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
.policy-card.expiring { border-left: 3px solid #e6a23c; background: #fffbf0; }
.policy-card.expired { border-left: 3px solid #f56c6c; background: #fff1f0; }
.policy-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.policy-name { font-size: 15px; font-weight: bold; color: #303133; }
.policy-meta { display: flex; gap: 16px; margin-bottom: 8px; }
.meta-item { font-size: 12px; color: #8c8c8c; display: flex; align-items: center; gap: 4px; }
.policy-type-tag { margin-bottom: 8px; }
.warning-bar { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #e6a23c; padding: 6px 10px; background: #fff7e6; border-radius: 6px; margin-top: 8px; }
.danger-bar { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #f56c6c; padding: 6px 10px; background: #fff1f0; border-radius: 6px; margin-top: 8px; }

.alert-item { display: flex; align-items: flex-start; gap: 10px; padding: 12px; border-radius: 8px; margin-bottom: 8px; }
.alert-warning { background: #fffbf0; border: 1px solid #ffe58f; }
.alert-danger { background: #fff1f0; border: 1px solid #ffa39e; }
.alert-title { font-size: 13px; font-weight: 500; color: #303133; }
.alert-desc { font-size: 12px; color: #8c8c8c; margin-top: 2px; }

.type-dist { padding: 8px 0; }
.type-item { margin-bottom: 16px; }
.type-label { font-size: 13px; color: #606266; margin-bottom: 6px; display: block; }

.mb20 { margin-bottom: 20px; }

.policy-detail-dialog :deep(.el-dialog) { border-radius: 16px; }
.policy-detail-dialog :deep(.el-dialog__header) { border-bottom: 1px solid #f0f0f0; padding: 20px 24px; }
.detail-content { padding: 4px 0; }
.detail-header { display: flex; gap: 8px; align-items: center; margin-bottom: 16px; }
.detail-amount { background: linear-gradient(135deg, #fff7ed, #ffedd5); border-radius: 12px; padding: 14px 18px; margin-bottom: 16px; display: flex; justify-content: space-between; align-items: center; }
.amount-label { font-size: 14px; color: #9a3412; }
.amount-value { font-size: 22px; font-weight: 700; color: #ea580c; }
.condition-items { display: flex; flex-wrap: wrap; gap: 6px; }
.cond-row { display: inline-flex; align-items: center; gap: 4px; background: #fff; border: 1px solid #e8e8e8; border-radius: 6px; padding: 4px 10px; font-size: 13px; }
.cond-field { color: #1890ff; font-weight: 500; }
.cond-op { color: #8c8c8c; }
.cond-val { color: #303133; font-weight: 600; }

</style>