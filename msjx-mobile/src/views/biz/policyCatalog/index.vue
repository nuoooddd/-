<template>
  <div class="app-container" style="background:#f6f8fb;min-height:100%;padding:12px">
    <div class="catalog-hero">
      <h1 class="hero-title">政策超市</h1>
      <p class="hero-desc">浏览全部惠企政策 · 一键申请</p>
      <div class="hero-stats">
        <div class="hero-stat">
          <span class="stat-num">{{ allPolicies.length }}</span>
          <span class="stat-label">政策总数</span>
        </div>
        <div class="hero-stat">
          <span class="stat-num">{{ activeCount }}</span>
          <span class="stat-label">生效中</span>
        </div>
        <div class="hero-stat">
          <span class="stat-num">{{ typeCount }}</span>
          <span class="stat-label">政策类型</span>
        </div>
      </div>
    </div>

    <div class="catalog-toolbar">
      <div class="toolbar-left">
        <el-input v-model="filterName" placeholder="搜索政策名称..." clearable size="small" prefix-icon="Search" @input="applyFilter" style="flex:1" />
        <el-select v-model="filterType" clearable placeholder="类型" size="small" @change="applyFilter" style="width:80px">
          <el-option label="补贴" value="1" />
          <el-option label="奖励" value="2" />
          <el-option label="减免" value="3" />
        </el-select>
      </div>
    </div>

    <div v-if="viewMode === 'card'" class="catalog-grid" v-loading="loading">
      <div v-for="(p, idx) in policies" :key="p.policyId" class="policy-card animate-fade-in-up" :class="'stagger-' + ((idx % 6) + 1)" @click="openDetail(p)">
        <div class="card-accent" :class="'accent-' + p.policyType"></div>
        <div class="card-body">
          <div class="card-top">
            <span class="type-tag" :class="'tag-' + p.policyType">{{ typeMap[p.policyType] || p.policyType }}</span>
            <span class="status-badge" :class="p.status === '0' ? 'badge-active' : 'badge-inactive'">{{ p.status === '0' ? '生效中' : '已停用' }}</span>
          </div>
          <h3 class="card-title">{{ p.policyName }}</h3>
          <p class="card-dept">{{ p.publishDept || '未指定部门' }}</p>
          <div class="card-meta">
            <span class="meta-amount" v-if="p.amount">
              <span class="amount-symbol">¥</span>{{ Number(p.amount).toLocaleString() }}
            </span>
            <span class="meta-date">{{ p.startDate }} ~ {{ p.endDate }}</span>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && policies.length === 0" description="暂无匹配政策" />
    </div>

    <div v-else class="catalog-table animate-fade-in-up" v-loading="loading">
      <el-table :data="policies" style="width: 100%" stripe @row-click="openDetail">
        <el-table-column label="政策名称" prop="policyName" min-width="200">
          <template #default="{ row }">
            <span style="font-weight: 600; color: #1e293b;">{{ row.policyName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="80" align="center">
          <template #default="{ row }">
            <span class="type-tag" :class="'tag-' + row.policyType">{{ typeMap[row.policyType] || row.policyType }}</span>
          </template>
        </el-table-column>
        <el-table-column label="金额(元)" width="130" align="right">
          <template #default="{ row }">
            <span v-if="row.amount" class="money-badge">{{ Number(row.amount).toLocaleString() }}</span>
            <span v-else style="color: #94a3b8;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="发布部门" prop="publishDept" width="160" />
        <el-table-column label="有效期" width="200">
          <template #default="{ row }">
            <span style="color: #64748b; font-size: 13px;">{{ row.startDate }} ~ {{ row.endDate }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <span class="status-badge" :class="row.status === '0' ? 'badge-active' : 'badge-inactive'">{{ row.status === '0' ? '生效中' : '已停用' }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="detailVisible" :title="detailData?.policyName" width="92%" class="detail-dialog" destroy-on-close>
      <div class="detail-content" v-if="detailData">
        <div class="detail-header">
          <span class="type-tag" :class="'tag-' + detailData.policyType" style="font-size: 14px; padding: 4px 14px;">{{ typeMap[detailData.policyType] || detailData.policyType }}</span>
          <span class="status-badge" :class="detailData.status === '0' ? 'badge-active' : 'badge-inactive'">{{ detailData.status === '0' ? '生效中' : '已停用' }}</span>
        </div>
        <div class="detail-amount" v-if="detailData.amount">
          <span class="amount-label">补贴金额</span>
          <span class="amount-value">¥ {{ Number(detailData.amount).toLocaleString() }}</span>
        </div>
        <div class="detail-grid">
          <div class="detail-item">
            <span class="item-label">发布部门</span>
            <span class="item-value">{{ detailData.publishDept || '未指定' }}</span>
          </div>
          <div class="detail-item">
            <span class="item-label">有效期</span>
            <span class="item-value">{{ detailData.startDate }} ~ {{ detailData.endDate }}</span>
          </div>
        </div>
        <div v-if="detailData.pdfName" style="margin-bottom:16px;padding:10px 14px;background:#f0f9eb;border-radius:8px;border:1px solid #b7eb8f">
          <span style="font-size:13px;color:#8c8c8c;margin-right:8px">政策文件</span>
          <el-link type="primary" :underline="false" @click="previewPolicyPdf(detailData)">{{ detailData.pdfName }}</el-link>
        </div>
        <div class="detail-desc" v-if="detailData.description">
          <span class="item-label">政策说明</span>
          <p>{{ detailData.description }}</p>
        </div>
        <div v-if="detailRules.length > 0" style="margin-top:16px">
          <div style="font-size:14px;font-weight:600;color:#303133;margin-bottom:12px">匹配条件</div>
          <div v-for="(rule, ri) in detailRules" :key="ri" style="margin-bottom:12px;background:#fafafa;border-radius:8px;padding:12px">
            <div style="font-size:13px;font-weight:500;color:#1e293b;margin-bottom:8px">{{ rule.ruleName }}</div>
            <div style="font-size:12px;color:#94a3b8;margin-bottom:6px;font-family:monospace">{{ rule.conditionExpr }}</div>
            <div class="cond-items">
              <div v-for="(cond, ci) in rule.parsedConds" :key="ci" class="cond-row">
                <span class="cond-field">{{ fieldNameMap[cond.field] || cond.field }}</span>
                <span class="cond-op">{{ opMap[cond.op] || cond.op }}</span>
                <span class="cond-val">{{ cond.displayVal }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="openApplyDialog(detailData)" v-if="detailData?.status === '0'">
          <el-icon><Promotion /></el-icon> 申请该政策
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="applyVisible" title="申请政策" width="92%" destroy-on-close append-to-body>
      <el-form :model="applyForm" label-width="100px">
        <el-form-item label="政策名称">
          <el-input :value="applyForm.policyName" disabled />
        </el-form-item>
        <el-form-item label="申请说明">
          <el-input v-model="applyForm.applyRemark" type="textarea" :rows="3" placeholder="请简要说明申请理由" />
        </el-form-item>
        <el-form-item label="佐证材料">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handleEvidenceSuccess"
            :before-upload="beforeEvidenceUpload"
            :file-list="evidenceFileList"
            :limit="1"
            :on-exceed="() => ElMessage.warning('只能上传一个佐证材料')"
            accept=".pdf,.jpg,.jpeg,.png"
          >
            <el-button type="primary" plain><el-icon><Upload /></el-icon> 上传佐证材料(PDF/图片)</el-button>
            <template #tip><div style="font-size:12px;color:#94a3b8;margin-top:4px">支持PDF、JPG、PNG格式，大小不超过20MB</div></template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" :loading="applyLoading" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>

    <el-dialog title="政策文件预览" v-model="pdfPreviewVisible" width="92%" append-to-body>
      <iframe v-if="pdfPreviewUrl" :src="pdfPreviewUrl" style="width:100%;height:60vh;border:none" />
      <template #footer>
        <el-button type="primary" @click="downloadPdf" icon="Download">下载</el-button>
        <el-button @click="pdfPreviewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onActivated, onMounted } from 'vue'
import { getAllPolicies, applyPolicy } from '@/api/biz/user'
import { listEeRule } from '@/api/biz/rule'
import { ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'

const loading = ref(false)
const allRules = ref([])
const detailRules = ref([])
const policies = ref([])
const allPolicies = ref([])
const filterName = ref('')
const filterType = ref('')
const filterStatus = ref('0')
const viewMode = ref('card')
const detailVisible = ref(false)
const detailData = ref(null)
const pdfPreviewVisible = ref(false)
const pdfPreviewUrl = ref('')

const applyVisible = ref(false)
const applyLoading = ref(false)
const applyForm = ref({ policyId: null, policyName: '', applyRemark: '', evidenceUrl: '', evidenceName: '' })
const evidenceFileList = ref([])
const uploadUrl = ref(import.meta.env.VITE_APP_BASE_API + '/common/upload')
const uploadHeaders = ref({ Authorization: 'Bearer ' + getToken() })

const typeMap = { '1': '补贴', '2': '奖励', '3': '减免' }

const activeCount = computed(() => allPolicies.value.filter(p => p.status === '0').length)
const typeCount = computed(() => new Set(allPolicies.value.map(p => p.policyType)).size)

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

function loadData() {
  loading.value = true
  getAllPolicies().then(r => {
    if (r.code === 200) {
      allPolicies.value = r.data || []
      applyFilter()
    }
    loading.value = false
  }).catch(() => { loading.value = false })
  listEeRule({ pageNum: 1, pageSize: 9999 }).then(res => { allRules.value = res.rows || [] })
}

function applyFilter() {
  let list = allPolicies.value
  if (filterName.value) list = list.filter(p => p.policyName.includes(filterName.value))
  if (filterType.value) list = list.filter(p => p.policyType === filterType.value)
  if (filterStatus.value !== '' && filterStatus.value !== undefined) list = list.filter(p => p.status === filterStatus.value)
  policies.value = list
}

function previewPolicyPdf(policy) {
  if (policy.pdfUrl) {
    let url = policy.pdfUrl;
    if (url.startsWith('http')) {
      const idx = url.indexOf('/profile/');
      if (idx !== -1) url = '/dev-api' + url.substring(idx);
    } else if (url.startsWith('/profile')) {
      url = '/dev-api' + url;
    }
    pdfPreviewUrl.value = url + '#toolbar=0';
    pdfPreviewVisible.value = true;
  }
}

function downloadPdf() {
  if (pdfPreviewUrl.value) {
    let url = pdfPreviewUrl.value.replace('#toolbar=0', '');
    const a = document.createElement('a');
    a.href = url;
    a.download = '';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }
}

function openDetail(p) {
  detailData.value = p
  const rules = allRules.value.filter(r => r.policyId === p.policyId && r.status === '0')
  detailRules.value = rules.map(r => ({
    ...r,
    parsedConds: parseConditions(r.conditionExpr)
  }))
  detailVisible.value = true
}

function openApplyDialog(policy) {
  detailVisible.value = false
  applyForm.value = { policyId: policy.policyId, policyName: policy.policyName, applyRemark: '', evidenceUrl: '', evidenceName: '' }
  evidenceFileList.value = []
  applyVisible.value = true
}

function beforeEvidenceUpload(file) {
  const isPdfOrImage = file.type === 'application/pdf' || file.type.startsWith('image/')
  const isLt20M = file.size / 1024 / 1024 < 20
  if (!isPdfOrImage) { ElMessage.error('只能上传PDF或图片文件'); return false }
  if (!isLt20M) { ElMessage.error('文件大小不能超过20MB'); return false }
  return true
}

function handleEvidenceSuccess(res) {
  if (res.code === 200) {
    applyForm.value.evidenceUrl = res.fileName
    applyForm.value.evidenceName = res.originalFilename || res.fileName
    evidenceFileList.value = [{ name: applyForm.value.evidenceName, url: res.fileName }]
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

function submitApply() {
  if (!applyForm.value.evidenceUrl) {
    ElMessage.warning('请上传佐证材料后再提交申请')
    return
  }
  applyLoading.value = true
  applyPolicy(applyForm.value).then(() => {
    ElMessage.success('申请已提交，管理员将尽快审核')
    applyVisible.value = false
  }).catch(() => {
    ElMessage.error('申请提交失败')
  }).finally(() => {
    applyLoading.value = false
  })
}

onMounted(loadData)
onActivated(loadData)
</script>

<style lang="scss" scoped>


.catalog-hero {
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
  border-radius: 20px;
  padding: 36px 40px;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -20%;
    width: 400px;
    height: 400px;
    background: radial-gradient(circle, rgba(59,130,246,0.1), transparent 70%);
    border-radius: 50%;
  }

  &::after {
    content: '';
    position: absolute;
    bottom: -30%;
    left: 10%;
    width: 300px;
    height: 300px;
    background: radial-gradient(circle, rgba(124, 58, 237, 0.08), transparent 70%);
    border-radius: 50%;
  }
}

.hero-title {
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 6px 0;
  position: relative;
  z-index: 1;
}

.hero-desc {
  font-size: 14px;
  color: #64748b;
  margin: 0 0 24px 0;
  position: relative;
  z-index: 1;
}

.hero-stats {
  display: flex;
  gap: 40px;
  position: relative;
  z-index: 1;
}

.hero-stat {
  display: flex;
  flex-direction: column;
}

.stat-num {
  font-size: 32px;
  font-weight: 700;
  color: #0f172a;
}

.stat-label {
  font-size: 12px;
  color: #64748b;
  margin-top: 2px;
}

.catalog-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 12px;
}

.toolbar-left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-box {
  width: 280px;
}

.catalog-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  min-height: 200px;
}

.policy-card {
  background: #ffffff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08);
  }
}

.card-accent {
  height: 4px;
  width: 100%;

  &.accent-1 { background: linear-gradient(90deg, #409EFF, #66b1ff); }
  &.accent-2 { background: linear-gradient(90deg, #67C23A, #85ce61); }
  &.accent-3 { background: linear-gradient(90deg, #E6A23C, #ebb563); }
}

.card-body {
  padding: 20px;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.type-tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;

  &.tag-1 { background: rgba(64, 158, 255, 0.1); color: #409EFF; }
  &.tag-2 { background: rgba(103, 194, 58, 0.1); color: #67C23A; }
  &.tag-3 { background: rgba(230, 162, 60, 0.1); color: #E6A23C; }
}

.status-badge {
  display: inline-flex;
  align-items: center;
  font-size: 12px;
  font-weight: 500;
  padding: 2px 8px;
  border-radius: 20px;

  &.badge-active {
    background: rgba(103, 194, 58, 0.1);
    color: #67C23A;

    &::before {
      content: '';
      display: inline-block;
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: #67C23A;
      margin-right: 4px;
    }
  }

  &.badge-inactive {
    background: rgba(245, 108, 108, 0.1);
    color: #F56C6C;

    &::before {
      content: '';
      display: inline-block;
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: #F56C6C;
      margin-right: 4px;
    }
  }
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-dept {
  font-size: 13px;
  color: #94a3b8;
  margin: 0 0 16px 0;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
}

.meta-amount {
  font-size: 18px;
  font-weight: 700;
  color: #ff6a00;

  .amount-symbol {
    font-size: 13px;
    font-weight: 500;
  }
}

.meta-date {
  font-size: 12px;
  color: #94a3b8;
}

.catalog-table {
  background: #ffffff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);

  :deep(.el-table) {
    --el-table-border-color: #f1f5f9;
    --el-table-header-bg-color: #f8fafc;
  }
}

.detail-dialog {
  :deep(.el-dialog) {
    border-radius: 20px;
    overflow: hidden;
  }

  :deep(.el-dialog__header) {
    background: linear-gradient(135deg, #f8fafc, #ffffff);
    border-bottom: 1px solid #f1f5f9;
    padding: 20px 24px;
  }

  :deep(.el-dialog__title) {
    font-weight: 700;
    color: #1e293b;
  }
}

.detail-content {
  padding: 4px 0;
}

.detail-header {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 20px;
}

.detail-amount {
  background: linear-gradient(135deg, #fff7ed, #ffedd5);
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.amount-label {
  font-size: 14px;
  color: #9a3412;
}

.amount-value {
  font-size: 24px;
  font-weight: 700;
  color: #ea580c;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-label {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 500;
}

.item-value {
  font-size: 14px;
  color: #1e293b;
  font-weight: 500;
}

.detail-desc {
  .item-label {
    display: block;
    margin-bottom: 8px;
  }

  p {
    font-size: 14px;
    color: #475569;
    line-height: 1.7;
    margin: 0;
    background: #f8fafc;
    padding: 12px 16px;
    border-radius: 10px;
  }
}

.cond-items { display: flex; flex-wrap: wrap; gap: 6px; }
.cond-row { display: inline-flex; align-items: center; gap: 4px; background: #fff; border: 1px solid #e8e8e8; border-radius: 6px; padding: 4px 10px; font-size: 13px; }
.cond-field { color: #1890ff; font-weight: 500; }
.cond-op { color: #8c8c8c; }
.cond-val { color: #303133; font-weight: 600; }

</style>