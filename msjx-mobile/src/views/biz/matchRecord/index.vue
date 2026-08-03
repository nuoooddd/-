<template>
  <div class="m-page">
    <div class="stat-row">
      <div class="stat-item" style="background:#e0e7ff"><div class="si-val">{{ total }}</div><div class="si-label">匹配数</div></div>
      <div class="stat-item" style="background:#d1fae5"><div class="si-val">{{ formatMoneyShort(totalFulfilledAmount) }}</div><div class="si-label">已兑付(万)</div></div>
      <div class="stat-item" style="background:#fef3c7"><div class="si-val">{{ exemptRate }}%</div><div class="si-label">免审率</div></div>
      <div class="stat-item" style="background:#ede9fe"><div class="si-val">{{ archivedCount }}</div><div class="si-label">已归档</div></div>
    </div>

    <div class="pipeline-row">
      <div v-for="step in steps" :key="step.status" class="pipe-chip" :class="{active:currentFilterStatus===step.status}" @click="filterByStatus(step.status)">
        <div class="pc-dot" :style="{background:step.color}"></div>
        <span>{{ step.name }}</span>
        <span class="pc-count">{{ getStepCount(step.status) }}</span>
      </div>
      <div class="pipe-chip" :class="{active:currentFilterStatus==='ALL'}" @click="filterByStatus('ALL')">
        <span>全部</span><span class="pc-count">{{ matchRecordList.length }}</span>
      </div>
    </div>

    <div class="action-bar">
      <el-button type="success" size="small" @click="handleTriggerMatch" round>增量匹配</el-button>
      <el-button size="small" @click="getList" round>刷新</el-button>
      <el-button v-if="currentFilterStatus!=='ALL'" size="small" @click="currentFilterStatus='ALL'" round>清除筛选</el-button>
    </div>

    <div v-if="currentFilterStatus==='APPLIED'" class="filter-tip">
      <el-icon><Warning /></el-icon> 已筛选「申请中」记录，共 {{ filteredList.length }} 条
    </div>

    <div v-loading="loading">
      <div v-for="item in filteredList" :key="item.recordId" class="card-item">
        <div class="ci-top">
          <span class="ci-name">{{ item.policyName }}</span>
          <el-tag :type="getStatusTag(item.status)" size="small">{{ getStatusLabel(item.status) }}</el-tag>
        </div>
        <div class="ci-mid">
          <span class="ci-target">{{ item.targetName }}</span>
          <el-tag :type="getRiskTag(item.riskLevel)" effect="dark" size="small">{{ getRiskLabel(item.riskLevel) }}</el-tag>
          <el-tag v-if="item.riskLevel==='0'" type="success" effect="plain" size="small">免审</el-tag>
        </div>
        <div class="ci-bottom">
          <span class="ci-money">{{ formatMoney(item.fundAmount) }} 元</span>
          <span v-if="item.auditStatus==='9'" class="ci-audit-pending">申请中</span>
          <span v-else-if="item.auditStatus==='1'" class="ci-audit-review">审核中</span>
        </div>
        <div class="ci-actions" @click.stop>
          <el-button v-if="item.status==='MATCHED'" type="primary" size="small" @click="handlePush(item)" round>推送</el-button>
          <span v-if="item.status==='PUSHED'" class="ci-waiting">等待确认</span>
          <template v-if="item.status==='CONFIRMED'">
            <el-tag v-if="item.riskLevel==='0'" type="success" effect="dark" size="small">免审通道</el-tag>
            <el-button :type="item.riskLevel==='0'?'success':'danger'" size="small" @click="handleFulfill(item)" round>{{ item.riskLevel==='0'?'免审拨付':'审核拨付' }}</el-button>
          </template>
          <el-button v-if="item.auditStatus==='1'" type="danger" size="small" @click="handleAudit(item)" round>审核</el-button>
          <el-button v-if="item.status==='FULFILLED'" type="info" size="small" @click="handleArchive(item)" round>归档</el-button>
          <span v-if="item.status==='ARCHIVED'" class="ci-done"><el-icon><CircleCheck /></el-icon> 已归档</span>
        </div>
      </div>
      <el-empty v-if="!loading && filteredList.length===0" description="暂无记录" :image-size="60" />
    </div>

    <el-dialog title="人工审核" v-model="auditDialogVisible" width="92%" append-to-body destroy-on-close>
      <div v-if="auditRow" class="audit-body">
        <div class="audit-row"><span class="ar-label">政策</span><span class="ar-val">{{ auditRow.policyName }}</span></div>
        <div class="audit-row"><span class="ar-label">对象</span><span class="ar-val">{{ auditRow.targetName }}</span></div>
        <div class="audit-row"><span class="ar-label">金额</span><span class="ar-val" style="color:#c2410c;font-weight:700">{{ formatMoney(auditRow.fundAmount) }} 元</span></div>
        <div class="audit-row"><span class="ar-label">风控</span><el-tag :type="getRiskTag(auditRow.riskLevel)" effect="dark" size="small">{{ getRiskLabel(auditRow.riskLevel) }}</el-tag></div>
        <el-divider content-position="left">佐证材料</el-divider>
        <div v-if="auditRow.proofFile" class="proof-card">
          <el-icon :size="24" color="#e6a23c"><Document /></el-icon>
          <span style="flex:1;font-size:13px">佐证材料.pdf</span>
          <el-button type="primary" size="small" @click="previewProof(auditRow.proofFile)">预览</el-button>
        </div>
        <div v-else class="no-proof"><el-icon :size="28" color="#94a3b8"><WarningFilled /></el-icon><span>未上传佐证材料</span></div>
        <el-divider content-position="left">审核意见</el-divider>
        <el-input v-model="auditRemark" type="textarea" :rows="2" placeholder="审核意见（选填）" />
      </div>
      <template #footer>
        <el-button @click="handleAuditReject">拒绝</el-button>
        <el-button type="primary" @click="handleAuditApprove">通过</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="EeMatchRecord">
import { listEeMatchRecord, triggerMatch, pushPolicy, confirmIntention, fulfillPayment, archiveRecord, pushBatch, archiveBatch, auditRecord } from "@/api/biz/matchRecord";
import { listEeFund } from "@/api/biz/fund"
import { getUnreadCount } from "@/api/biz/message";
import { ref, computed, onMounted, getCurrentInstance } from 'vue';

const { proxy } = getCurrentInstance();
const matchRecordList = ref([]);
const loading = ref(true);
const currentFilterStatus = ref("ALL");
const fundList = ref([]);

const steps = [
  { name:"已匹配", status:"MATCHED", color:"#4f46e5" },
  { name:"已推送", status:"PUSHED", color:"#06b6d4" },
  { name:"已确认", status:"CONFIRMED", color:"#f59e0b" },
  { name:"已兑付", status:"FULFILLED", color:"#10b981" },
  { name:"已归档", status:"ARCHIVED", color:"#8b5cf6" }
];

function getList() {
  loading.value = true;
  listEeMatchRecord().then(response => { matchRecordList.value = response.rows || []; loading.value = false; }).catch(() => { loading.value = false; });
  listEeFund().then(response => { fundList.value = response.rows || []; }).catch(() => {});
}

const total = computed(() => matchRecordList.value.length);
const totalFulfilledAmount = computed(() => matchRecordList.value.filter(r => r.status==='FULFILLED'||r.status==='ARCHIVED').reduce((s,r) => s+Number(r.fundAmount||0), 0));
const exemptRate = computed(() => { if (!matchRecordList.value.length) return 0; return Math.round(matchRecordList.value.filter(r => r.riskLevel==='0').length / matchRecordList.value.length * 100); });
const archivedCount = computed(() => matchRecordList.value.filter(r => r.status==='ARCHIVED').length);

const filteredList = computed(() => {
  if (currentFilterStatus.value === "ALL") return matchRecordList.value;
  if (currentFilterStatus.value === "APPLIED") return matchRecordList.value.filter(item => item.auditStatus === '9');
  return matchRecordList.value.filter(item => item.status === currentFilterStatus.value);
});

function getStepCount(status) { return matchRecordList.value.filter(r => r.status===status).length; }
function filterByStatus(status) { currentFilterStatus.value = status; }

function handleTriggerMatch() {
  proxy.$modal.loading("正在智能匹配...");
  triggerMatch().then(response => { proxy.$modal.closeLoading(); proxy.$modal.msgSuccess(response.msg || "匹配完成！"); getList(); }).catch(() => { proxy.$modal.closeLoading(); });
}

function handlePush(row) { pushPolicy(row.recordId).then(() => { proxy.$modal.msgSuccess("推送成功！"); getList(); }); }

function handleFulfill(row) {
  const isExempt = row.riskLevel === '0';
  const msg = isExempt ? `低风险免审，确认拨付 ${formatMoney(row.fundAmount)} 元？` : `${getRiskLabel(row.riskLevel)}，确认拨付 ${formatMoney(row.fundAmount)} 元？`;
  proxy.$modal.confirm(msg).then(() => {
    proxy.$modal.loading("正在拨付...");
    fulfillPayment(row.recordId).then(response => { proxy.$modal.closeLoading(); proxy.$modal.msgSuccess(response.msg || "拨付成功！"); getList(); }).catch(() => { proxy.$modal.closeLoading(); });
  }).catch(() => {});
}

function handleArchive(row) { archiveRecord(row.recordId).then(() => { proxy.$modal.msgSuccess("归档成功！"); getList(); }); }

const auditDialogVisible = ref(false);
const auditRow = ref(null);
const auditRemark = ref('');

function handleAudit(row) { auditRow.value = row; auditRemark.value = ''; auditDialogVisible.value = true; }
function handleAuditApprove() { auditRecord(auditRow.value.recordId, '2').then(() => { proxy.$modal.msgSuccess("审核通过！"); auditDialogVisible.value = false; getList(); }); }
function handleAuditReject() { proxy.$modal.confirm('确认拒绝？').then(() => { auditRecord(auditRow.value.recordId, '3').then(() => { proxy.$modal.msgSuccess("已拒绝"); auditDialogVisible.value = false; getList(); }); }).catch(() => {}); }
function previewProof(url) { window.open(url, '_blank'); }

function formatMoney(val) { if (val === undefined || val === null) return "0.00"; return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }); }
function formatMoneyShort(val) { if (!val) return '0'; return (Number(val)/10000).toFixed(1); }
function getStatusTag(s) { return {MATCHED:'primary',PUSHED:'info',CONFIRMED:'warning',FULFILLED:'success',ARCHIVED:'success'}[s]||''; }
function getStatusLabel(s) { return {MATCHED:'已匹配',PUSHED:'已推送',CONFIRMED:'已确认',FULFILLED:'已兑付',ARCHIVED:'已归档'}[s]||s; }
function getRiskTag(l) { return {'0':'success','1':'warning','2':'danger'}[l]||'info'; }
function getRiskLabel(l) { return {'0':'低风险','1':'中风险','2':'高风险'}[l]||'未评'; }

onMounted(() => { getList(); });
</script>

<style scoped>
.m-page { padding:12px; background:#f5f6fa; min-height:100%; }
.stat-row { display:flex; gap:6px; margin-bottom:10px; }
.stat-item { flex:1; text-align:center; padding:10px 4px; border-radius:10px; }
.si-val { font-size:16px; font-weight:700; color:#1e293b; font-family:'Outfit',sans-serif; }
.si-label { font-size:10px; color:#64748b; margin-top:2px; }

.pipeline-row { display:flex; flex-wrap:wrap; gap:6px; margin-bottom:10px; }
.pipe-chip { display:flex; align-items:center; gap:4px; padding:4px 8px; background:#fff; border:1px solid #e2e8f0; border-radius:16px; font-size:11px; color:#64748b; cursor:pointer; }
.pipe-chip.active { background:#1a3a5c; color:#fff; border-color:#1a3a5c; }
.pc-dot { width:8px; height:8px; border-radius:50%; }
.pc-count { font-weight:600; font-family:'Outfit',sans-serif; }

.action-bar { display:flex; gap:6px; margin-bottom:10px; flex-wrap:wrap; }
.filter-tip { background:#fff7e6; border:1px solid #ffd591; border-radius:8px; padding:6px 10px; margin-bottom:10px; font-size:12px; color:#ad6800; display:flex; align-items:center; gap:6px; }

.card-item { background:#fff; border:1px solid #e2e8f0; border-radius:10px; padding:12px; margin-bottom:8px; }
.ci-top { display:flex; justify-content:space-between; align-items:center; margin-bottom:6px; }
.ci-name { font-size:14px; font-weight:600; color:#1e293b; flex:1; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; margin-right:8px; }
.ci-mid { display:flex; align-items:center; gap:6px; margin-bottom:6px; }
.ci-target { font-size:12px; color:#64748b; flex:1; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.ci-bottom { display:flex; align-items:center; gap:8px; margin-bottom:8px; }
.ci-money { color:#e65100; font-weight:700; font-size:14px; font-family:Consolas,monospace; }
.ci-audit-pending { color:#e6a23c; font-size:11px; background:#fdf6ec; padding:2px 6px; border-radius:4px; }
.ci-audit-review { color:#f56c6c; font-size:11px; background:#fef0f0; padding:2px 6px; border-radius:4px; }
.ci-actions { display:flex; gap:6px; flex-wrap:wrap; padding-top:8px; border-top:1px solid #f1f5f9; }
.ci-waiting { color:#e6a23c; font-size:12px; display:flex; align-items:center; gap:4px; padding:4px 8px; background:#fdf6ec; border-radius:6px; }
.ci-done { color:#67c23a; font-size:12px; font-weight:600; display:flex; align-items:center; gap:4px; }

.audit-body { }
.audit-row { display:flex; align-items:center; gap:8px; margin-bottom:8px; }
.ar-label { font-size:12px; color:#64748b; min-width:40px; }
.ar-val { font-size:13px; color:#1e293b; font-weight:500; }
.proof-card { display:flex; align-items:center; gap:10px; padding:10px; background:#fffbeb; border:1px solid #fde68a; border-radius:8px; }
.no-proof { display:flex; flex-direction:column; align-items:center; gap:6px; padding:16px; background:#f8fafc; border-radius:8px; border:1px dashed #cbd5e1; color:#94a3b8; font-size:12px; }
</style>
