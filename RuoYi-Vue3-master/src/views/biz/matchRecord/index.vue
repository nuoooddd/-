<template>
  <div class="app-container" style="background-color: #f6f8fb; min-height: calc(100vh - 84px); padding: 20px;">
    
    <!-- 头部惊艳的政务科技风玻璃拟态统计卡片 -->
    <el-row :gutter="20" class="mb20">
      <el-col :span="6">
        <div class="glass-card stat-primary">
          <div class="card-icon"><el-icon><Cpu /></el-icon></div>
          <div class="card-info">
            <div class="card-label">智能匹配匹配成功数</div>
            <div class="card-value">{{ total }} <span class="unit">个</span></div>
          </div>
          <div class="card-bottom">
            <span class="trend"><el-icon><CaretTop /></el-icon> 政策找人直达率 100%</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-card stat-success">
          <div class="card-icon"><el-icon><Money /></el-icon></div>
          <div class="card-info">
            <div class="card-label">已自动兑付资金总额</div>
            <div class="card-value">{{ formatMoney(totalFulfilledAmount) }} <span class="unit">元</span></div>
          </div>
          <div class="card-bottom">
            <span class="trend"><el-icon><CaretTop /></el-icon> 资金扣减同步成功</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-card stat-warning">
          <div class="card-icon"><el-icon><Opportunity /></el-icon></div>
          <div class="card-info">
            <div class="card-label">绿色通道自动免审率</div>
            <div class="card-value">{{ exemptRate }} <span class="unit">%</span></div>
          </div>
          <div class="card-bottom">
            <span class="trend">低风险对象秒级兑现率</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-card stat-info">
          <div class="card-icon"><el-icon><Checked /></el-icon></div>
          <div class="card-info">
            <div class="card-label">已公示归档案例数</div>
            <div class="card-value">{{ archivedCount }} <span class="unit">个</span></div>
          </div>
          <div class="card-bottom">
            <span class="trend">社会公示闭环率 100%</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 交互式五步法闭环流转管道 (Stepper Pipeline) -->
    <el-card class="box-card mb20 border-gradient" style="border-radius: 12px; box-shadow: 0 4px 12px 0 rgba(0, 0, 0, 0.05);">
      <template #header>
        <div class="card-header-flex">
          <span class="header-title"><el-icon><Operation /></el-icon> “免申即享”政策补贴生命周期流转管道</span>
          <div class="header-actions">
            <el-button :type="currentFilterStatus==='APPLIED'?'danger':'warning'" size="small" @click="filterByApplied" style="border-radius: 8px;" :disabled="appliedCount===0" class="mr10">
              📋 待处理申请 <el-tag v-if="appliedCount>0" :type="currentFilterStatus==='APPLIED'?'warning':'danger'" size="small" style="margin-left:4px">{{ appliedCount }}</el-tag>
            </el-button>
            
            <el-button v-if="currentFilterStatus==='APPLIED'" size="small" @click="currentFilterStatus='ALL'" style="border-radius:8px;margin-right:8px">✕ 清除筛选</el-button>
            <el-button size="small" @click="getList" style="border-radius:8px;margin-right:8px"><el-icon><Refresh /></el-icon> 刷新</el-button>
            <el-button type="primary" size="small" :disabled="selectedRows.length===0" @click="handleBatchPush" style="border-radius:8px;margin-right:8px"><el-icon><Promotion /></el-icon> 批量推送通知 ({{ selectedRows.length }})</el-button>
            <el-button type="warning" size="small" :disabled="selectedRows.length===0" @click="handleBatchArchive" style="border-radius:8px;margin-right:8px"><el-icon><FolderChecked /></el-icon> 批量归档 ({{ selectedRows.length }})</el-button>
            <el-button type="success" icon="Lightning" @click="handleTriggerMatch" class="action-btnpulse" style="border-radius: 8px;">
              🔍 增量匹配 (仅新增)
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="pipeline-container">
        <div 
          v-for="step in steps" 
          :key="step.status"
          class="pipeline-node"
          :class="{ active: currentFilterStatus === step.status, completed: getStepCount(step.status) > 0 }"
          @click="filterByStatus(step.status)"
        >
          <div class="node-icon-wrapper">
            <el-icon><component :is="step.icon" /></el-icon>
          </div>
          <div class="node-details">
            <div class="node-title">{{ step.name }}</div>
            <div class="node-badge">{{ getStepCount(step.status) }} 笔</div>
          </div>
          <div v-if="step.hasNext" class="pipeline-arrow">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M8.59 16.59L13.17 12L8.59 7.41L10 6L16 12L10 18L8.59 16.59Z" fill="#C0C4CC"/>
            </svg>
          </div>
        </div>
        
        <!-- 全览节点 -->
        <div class="pipeline-node node-all" :class="{ active: currentFilterStatus === 'ALL' }" @click="filterByStatus('ALL')">
          <div class="node-icon-wrapper">
            <el-icon><Grid /></el-icon>
          </div>
          <div class="node-details">
            <div class="node-title">全部记录</div>
            <div class="node-badge">{{ matchRecordList.length }} 笔</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 匹配记录数据主表格 -->
    <!-- 筛选状态提示 -->
    <div v-if="currentFilterStatus==='APPLIED'" style="background:#fff7e6;border:1px solid #ffd591;border-radius:8px;padding:8px 16px;margin-bottom:12px;display:flex;align-items:center;gap:8px;font-size:13px;color:#ad6800">
      <el-icon><Warning /></el-icon> 已筛选「申请中」记录，共 <b>{{ filteredList.length }}</b> 条
    </div>
    
    <el-card style="border-radius: 12px; box-shadow: 0 4px 12px 0 rgba(0, 0, 0, 0.05);">
      <el-table 
        v-loading="loading" 
        :data="filteredList"
        :row-class-name="tableRowClassName" 
        @selection-change="handleSelectionChange"
        style="width: 100%; border-radius: 8px;"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266', fontWeight: 'bold' }"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="记录ID" align="center" prop="recordId" width="70" />
        
        <el-table-column label="政策名称" align="left" prop="policyName" min-width="200">
          <template #default="scope">
            <div style="display: flex; align-items: center;">
              <el-icon color="#409EFF" style="margin-right: 8px;"><Memo /></el-icon>
              <span style="font-weight: 500;">{{ scope.row.policyName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="目标对象" align="left" prop="targetName" min-width="180">
          <template #default="scope">
            <div style="display: flex; flex-direction: column;">
              <span style="font-weight: bold; color: #303133;">{{ scope.row.targetName }}</span>
              <span style="font-size: 11px; color: #909399; margin-top: 3px;">
                <el-icon><Postcard /></el-icon> {{ scope.row.identifier }}
              </span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="拟兑现金额" align="right" prop="fundAmount" width="130">
          <template #default="scope">
            <span class="money-badge">{{ formatMoney(scope.row.fundAmount) }} 元</span>
          </template>
        </el-table-column>

        <el-table-column label="比对时间" align="center" prop="matchTime" width="160" />

        <el-table-column label="流程阶段" align="center" prop="status" width="130">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.status)" effect="light" style="border-radius: 6px; font-weight: bold;">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="风控等级" align="center" prop="riskLevel" width="120">
          <template #default="scope">
            <el-tooltip :content="getRiskTooltip(scope.row.riskLevel, scope.row.targetName)" placement="top">
              <el-tag :type="getRiskTag(scope.row.riskLevel)" effect="dark" style="border-radius: 4px; cursor: pointer;">
                {{ getRiskLabel(scope.row.riskLevel) }}
              </el-tag>
            </el-tooltip>
            <el-tag v-if="scope.row.riskLevel === '0'" type="success" effect="plain" size="small" style="margin-left:4px;border-radius:4px">免审</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="审核状态" align="center" prop="auditStatus" width="110">
          <template #default="scope">
            <el-tag :type="scope.row.auditStatus==='9'?'warning':getAuditTag(scope.row.auditStatus)" style="border-radius: 4px;">
              {{ scope.row.auditStatus==='9'?'申请中':getAuditLabel(scope.row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="直达操作" align="center" class-name="small-padding fixed-width" width="180">
          <template #default="scope">
            <div style="display: flex; justify-content: center; gap: 8px;">
              <!-- 阶段1: MATCHED -> PUSH -->
              <el-button 
                v-if="scope.row.status === 'MATCHED'"
                type="primary" 
                size="small" 
                icon="Share" 
                @click="handlePush(scope.row)"
                style="border-radius: 6px;"
              >
                推送通知
              </el-button>

              <!-- 阶段2: PUSHED -> CONFIRMED (仅企业端操作，管理端不显示确认按钮) -->
              <span v-if="scope.row.status === 'PUSHED'" style="color:#e6a23c;font-size:13px;display:inline-flex;align-items:center;gap:4px;padding:4px 10px;background:#fdf6ec;border-radius:6px;border:1px solid #faecd8">
                <el-icon><Clock /></el-icon> 等待企业确认
              </span>

              <!-- 阶段3: CONFIRMED -> FULFILL -->
              <template v-if="scope.row.status === 'CONFIRMED'">
                <el-tag v-if="scope.row.riskLevel === '0'" type="success" effect="dark" size="small" style="border-radius:4px;animation:exemptPulse 1.5s infinite">
                  免审通道
                </el-tag>
                <el-button 
                  :type="scope.row.riskLevel === '0' ? 'success' : 'danger'"
                  size="small" 
                  icon="Money" 
                  @click="handleFulfill(scope.row)"
                  style="border-radius: 6px;"
                >
                  {{ scope.row.riskLevel === '0' ? '免审拨付' : '审核拨付' }}
                </el-button>
              </template>

              <!-- 人工审核按钮 (审核中的记录) -->
              <el-button 
                v-if="scope.row.auditStatus === '1' && (scope.row.status === 'MATCHED' || scope.row.status === 'PUSHED' || scope.row.status === 'CONFIRMED')"
                type="danger"
                size="small"
                icon="Edit"
                @click="handleAudit(scope.row)"
                style="border-radius: 6px;"
              >
                审核
              </el-button>

              <!-- 阶段4: FULFILLED -> ARCHIVE -->
              <el-button 
                v-if="scope.row.status === 'FULFILLED'"
                type="info" 
                size="small" 
                icon="FolderChecked" 
                @click="handleArchive(scope.row)"
                style="border-radius: 6px;"
              >
                公示归档
              </el-button>

              <!-- 阶段5: ARCHIVED -> COMPLETED -->
              <span v-if="scope.row.status === 'ARCHIVED'" class="completed-text">
                <el-icon><CircleCheck /></el-icon> 闭环归档
              </span>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="empty-state" v-if="filteredList.length === 0">
        <el-empty description="当前阶段暂无记录，请点击上方“智能条件比对”开始计算政策匹配。" />
      </div>
    </el-card>

    <!-- “免申即享”政务数字化改革效能统计图表 (SVG Visualization) -->
    <el-row :gutter="20" class="mt20">
      <el-col :span="12">
        <el-card style="border-radius: 12px; height: 350px;">
          <template #header>
            <span style="font-weight: bold;"><el-icon><DataLine /></el-icon> 财政补贴直达预算支出分析 (资金池明细)</span>
          </template>
          <div class="custom-chart-container">
            <!-- 手动实现一个非常漂亮的SVG比例条与展示 -->
            <div class="budget-progress-header">
              <span>兑付预算消耗率：</span>
              <span style="font-weight: bold; color: #67C23A;">{{ totalBudgetUsageRate }}%</span>
            </div>
            <div class="progress-bar-glow">
              <div class="progress-bar-fill" :style="{ width: totalBudgetUsageRate + '%' }"></div>
            </div>
            
            <div class="fund-detail-list">
              <div class="fund-item" v-for="fund in fundList" :key="fund.fundId">
                <span class="fund-name">💰 {{ fund.policyName || '政策资金池' }}</span>
                <span class="fund-amount">已拨: {{ formatMoney(fund.usedAmount) }} / {{ formatMoney(fund.totalBudget) }}元</span>
              </div>
              <div class="fund-item" v-if="fundList.length === 0" style="color: #909399; justify-content: center;">
                暂无资金池数据
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card style="border-radius: 12px; height: 350px;">
          <template #header>
            <span style="font-weight: bold;"><el-icon><Histogram /></el-icon> 政策直达效能对比分析（传统 vs 免申即享）</span>
          </template>
          <div class="performance-comparison">
            <div class="metric-row">
              <div class="metric-col">
                <div class="metric-box box-red">
                  <div class="title">传统“人找政策”流程</div>
                  <div class="number">15 ~ 30 <span class="unit">天</span></div>
                  <div class="desc">多轮报送材料、现场跑腿、多部门人工盖章</div>
                </div>
              </div>
              <div class="metric-col">
                <div class="metric-box box-green animate-pulse">
                  <div class="title">免申即享“政策找人”</div>
                  <div class="number">&lt; 1 <span class="unit">秒</span></div>
                  <div class="desc">零材料申报、智能匹配比对、资金秒级直达</div>
                </div>
              </div>
            </div>
            
            <div class="feature-checklist">
              <div class="check-item"><el-icon color="#67C23A"><CircleCheck /></el-icon> 共享画像数据比对</div>
              <div class="check-item"><el-icon color="#67C23A"><CircleCheck /></el-icon> 秒级智能风控预警</div>
              <div class="check-item"><el-icon color="#67C23A"><CircleCheck /></el-icon> 联动扣减政策资金池</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

  </div>

  <el-dialog title="人工审核" v-model="auditDialogVisible" width="650px" append-to-body destroy-on-close>
    <div v-if="auditRow" class="audit-dialog-body">
      <div class="audit-info-section">
        <div class="audit-info-row"><span class="ai-label">政策名称</span><span class="ai-value">{{ auditRow.policyName }}</span></div>
        <div class="audit-info-row"><span class="ai-label">目标对象</span><span class="ai-value">{{ auditRow.targetName }}</span></div>
        <div class="audit-info-row"><span class="ai-label">补贴金额</span><span class="ai-value" style="color:#c2410c;font-weight:bold">{{ formatMoney(auditRow.fundAmount) }} 元</span></div>
        <div class="audit-info-row"><span class="ai-label">风控等级</span><el-tag :type="getRiskTag(auditRow.riskLevel)" effect="dark" size="small">{{ getRiskLabel(auditRow.riskLevel) }}</el-tag></div>
      </div>
      <el-divider content-position="left">佐证材料</el-divider>
      <div class="audit-proof-section">
        <div v-if="auditRow.proofFile" class="proof-file-card">
          <el-icon :size="32" color="#e6a23c"><Document /></el-icon>
          <div class="proof-info">
            <div class="proof-name">佐证材料.pdf</div>
            <div class="proof-hint">用户上传的PDF佐证材料</div>
          </div>
          <el-button type="primary" size="small" @click="previewProof(auditRow.proofFile)"><el-icon><View /></el-icon> 预览</el-button>
        </div>
        <div v-else class="no-proof">
          <el-icon :size="36" color="#94a3b8"><WarningFilled /></el-icon>
          <span>用户未上传佐证材料</span>
        </div>
      </div>
      <el-divider content-position="left">审核意见</el-divider>
      <el-input v-model="auditRemark" type="textarea" :rows="3" placeholder="请输入审核意见（选填）" />
    </div>
    <template #footer>
      <el-button @click="handleAuditReject">审核拒绝</el-button>
      <el-button type="primary" @click="handleAuditApprove">审核通过</el-button>
    </template>
  </el-dialog>
</template>

<script setup name="EeMatchRecord">
import { 
  listEeMatchRecord, 
  triggerMatch, 
  pushPolicy, 
  confirmIntention, 
  fulfillPayment, 
  archiveRecord,
  pushBatch,
  archiveBatch,
  auditRecord
} from "@/api/biz/matchRecord";
import { listEeFund } from "@/api/biz/fund"
import { getUnreadCount } from "@/api/biz/message";
import { ref, computed, onMounted, getCurrentInstance } from 'vue';

const { proxy } = getCurrentInstance();
const matchRecordList = ref([]);
const loading = ref(true);
const currentFilterStatus = ref("ALL");
const selectedRows = ref([]);
const fundList = ref([])
const unreadMsg = ref(0);

// 定义流转节点
const steps = [
  { name: "系统已匹配", status: "MATCHED", icon: "Cpu", hasNext: true },
  { name: "已推送通知", status: "PUSHED", icon: "Share", hasNext: true },
  { name: "意愿已确认", status: "CONFIRMED", icon: "Checked", hasNext: true },
  { name: "自动已兑付", status: "FULFILLED", icon: "Money", hasNext: true },
  { name: "已公示归档", status: "ARCHIVED", icon: "FolderChecked", hasNext: false }
];

// 初始化数据加载
function getList() {
  loading.value = true;
  listEeMatchRecord().then(response => {
    matchRecordList.value = response.rows || [];
    loading.value = false;
  }).catch(() => {
    loading.value = false;
  });
  listEeFund().then(response => {
    fundList.value = response.rows || [];
  }).catch(() => {});
  getUnreadCount().then(function(r) { unreadMsg.value = r.data || 0 }).catch(function(){});
}

// 统计数据计算
const total = computed(() => matchRecordList.value.length);

const totalFulfilledAmount = computed(() => {
  return matchRecordList.value
    .filter(r => r.status === 'FULFILLED' || r.status === 'ARCHIVED')
    .reduce((sum, r) => sum + Number(r.fundAmount || 0), 0);
});

const exemptRate = computed(() => {
  if (matchRecordList.value.length === 0) return 0;
  const exemptCount = matchRecordList.value.filter(r => r.riskLevel === '0').length;
  return Math.round((exemptCount / matchRecordList.value.length) * 100);
});

const appliedCount = computed(() => {
  return matchRecordList.value.filter(r => r.auditStatus === '9').length;
});

const archivedCount = computed(() => {
  return matchRecordList.value.filter(r => r.status === 'ARCHIVED').length;
});

// 资金池使用百分比计算
const totalBudgetUsageRate = computed(() => {
  if (fundList.value.length === 0) return 0;
  const totalBudget = fundList.value.reduce((sum, f) => sum + Number(f.totalBudget || 0), 0);
  const totalUsed = fundList.value.reduce((sum, f) => sum + Number(f.usedAmount || 0), 0);
  if (totalBudget === 0) return 0;
  return Math.min(Math.round((totalUsed / totalBudget) * 100), 100);
});

// 根据状态过滤列表
const filteredList = computed(() => {
  if (currentFilterStatus.value === "ALL") {
    return matchRecordList.value;
  }
  if (currentFilterStatus.value === "APPLIED") {
    return matchRecordList.value.filter(item => item.auditStatus === '9');
  }
  return matchRecordList.value.filter(item => item.status === currentFilterStatus.value);
});

// 获取各阶段记录数量
function getStepCount(status) {
  return matchRecordList.value.filter(r => r.status === status).length;
}

// 切换过滤节点
function filterByStatus(status) {
  currentFilterStatus.value = status;
}

function filterByApplied() {
  currentFilterStatus.value = 'APPLIED';
}

// 一键智能匹配动作
function handleTriggerMatch() {
  proxy.$modal.loading("正在调取多部门数据，进行智能规则匹配，请稍候...");
  triggerMatch().then(response => {
    proxy.$modal.closeLoading();
    proxy.$modal.msgSuccess(response.msg || "匹配比对完成！");
    getList();
  }).catch(() => {
    proxy.$modal.closeLoading();
  });
}

// 推送动作
function handlePush(row) {
  pushPolicy(row.recordId).then(() => {
    proxy.$modal.msgSuccess(`已成功将“${row.policyName}”推送至“${row.targetName}”联系人！`);
    getList();
  });
}

// 确认意愿动作
function handleConfirm(row) {
  confirmIntention(row.recordId).then(() => {
    proxy.$modal.msgSuccess(`目标对象“${row.targetName}”已在线确认补贴意愿，进入资金自动拨付流转！`);
    getList();
  });
}

// 拨付自动免审兑现动作
function handleFulfill(row) {
  const isExempt = row.riskLevel === '0';
  const confirmMsg = isExempt
    ? `当前对象风险评级为【低风险】，系统已开启【绿色通道免审拨付】。是否确认拨付 ${formatMoney(row.fundAmount)} 元至对象银行账户？`
    : `当前对象风险评级为【${getRiskLabel(row.riskLevel)}】，需要审计合规。是否确认人工核准拨付 ${formatMoney(row.fundAmount)} 元？`;
  
  proxy.$modal.confirm(confirmMsg).then(() => {
    proxy.$modal.loading("正在执行资金交易安全保障扣款并下发汇款指令...");
    fulfillPayment(row.recordId).then(response => {
      proxy.$modal.closeLoading();
      proxy.$modal.msgSuccess(response.msg || "资金下拨成功！已实现秒级直达。");
      getList();
    }).catch(() => {
      proxy.$modal.closeLoading();
    });
  }).catch(() => {});
}

// 公示归档动作
function handleArchive(row) {
  archiveRecord(row.recordId).then(() => {
    proxy.$modal.msgSuccess(`"${row.targetName}"政策兑现卷宗公示成功并正式安全归档！`);
    getList();
  });
}

// 多选变更
function handleSelectionChange(selection) {
  selectedRows.value = selection;
}

// 批量推送
function handleBatchPush() {
  const pushableRows = selectedRows.value.filter(r => r.status === 'MATCHED');
  if (pushableRows.length === 0) {
    proxy.$modal.msgWarning('所选记录中没有可推送的记录（仅「系统比对成功」状态可推送）');
    return;
  }
  const ids = pushableRows.map(r => r.recordId);
  proxy.$modal.confirm(`确认批量推送通知 ${ids.length} 条匹配记录至对应目标对象？`).then(() => {
    pushBatch(ids).then(() => {
      proxy.$modal.msgSuccess(`已成功推送通知 ${ids.length} 条记录！`);
      getList();
    });
  }).catch(() => {});
}

// 批量归档
function handleBatchArchive() {
  const archivableRows = selectedRows.value.filter(r => r.status === 'FULFILLED');
  if (archivableRows.length === 0) {
    proxy.$modal.msgWarning('所选记录中没有可归档的记录（仅「免审资金已兑付」状态可归档）');
    return;
  }
  const ids = archivableRows.map(r => r.recordId);
  proxy.$modal.confirm(`确认批量公示归档 ${ids.length} 条已兑付记录？`).then(() => {
    archiveBatch(ids).then(() => {
      proxy.$modal.msgSuccess(`已成功归档 ${ids.length} 条记录！`);
      getList();
    });
  }).catch(() => {});
}

// 人工审核
const auditDialogVisible = ref(false)
const auditRow = ref(null)
const auditRemark = ref('')

function handleAudit(row) {
  auditRow.value = row
  auditRemark.value = ''
  auditDialogVisible.value = true
}

function handleAuditApprove() {
  auditRecord(auditRow.value.recordId, '2').then(() => {
    proxy.$modal.msgSuccess(`"${auditRow.value.targetName}"审核已通过！`)
    auditDialogVisible.value = false
    getList()
  })
}

function handleAuditReject() {
  proxy.$modal.confirm('确认拒绝该审核？').then(() => {
    auditRecord(auditRow.value.recordId, '3').then(() => {
      proxy.$modal.msgSuccess(`"${auditRow.value.targetName}"审核已拒绝`)
      auditDialogVisible.value = false
      getList()
    })
  }).catch(() => {})
}

function previewProof(url) {
  window.open(url, '_blank')
}

// 格式化货币
function formatMoney(val) {
  if (val === undefined || val === null) return "0.00";
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

// 获取各状态标签样式
function getStatusTag(status) {
  switch (status) {
    case "MATCHED": return "primary";
    case "PUSHED": return "warning";
    case "CONFIRMED": return "warning";
    case "FULFILLED": return "success";
    case "ARCHIVED": return "success";
    default: return "";
  }
}

// 获取各状态描述
function getStatusLabel(status) {
  switch (status) {
    case "MATCHED": return "系统比对成功";
    case "PUSHED": return "已推送·待确认";
    case "CONFIRMED": return "意愿已确认";
    case "FULFILLED": return "免审资金已兑付";
    case "ARCHIVED": return "已公示归档";
    default: return status;
  }
}

// 获取风险标签样式
function getRiskTag(level) {
  switch (level) {
    case "0": return "success";
    case "1": return "warning";
    case "2": return "danger";
    default: return "info";
  }
}

// 获取风险描述
function getRiskLabel(level) {
  switch (level) {
    case "0": return "低风险";
    case "1": return "中风险";
    case "2": return "高风险";
    default: return "未评级";
  }
}

// 获取审核标签样式
function getAuditTag(status) {
  switch (status) {
    case "0": return "success";
    case "1": return "warning";
    case "2": return "success";
    case "3": return "danger";
    default: return "info";
  }
}

// 获取审核描述
function getAuditLabel(status) {
  switch (status) {
    case "0": return "自动免审直达";
    case "1": return "人工审核中";
    case "2": return "核准通过";
    case "3": return "核准拒绝";
    default: return "待审核";
  }
}

// 获取风控原因
function getRiskTooltip(level, name) {
  switch (level) {
    case "0": return `对象信用良好，画像规则完美比对通过。安全级别高，开启免审绿色通道。`;
    case "1": return `中度预警：信用评分中等。进入半自动人工审计流程。`;
    case "2": return `高度预警：${name}在多部门共享中被标记为“税收征管异常/信用惩戒”，触发人工审核拦截！`;
    default: return "点击可查看风控详情";
  }
}

function tableRowClassName({ row }) {
  if (row.auditStatus === '9') return 'applied-row';
  return '';
}

onMounted(() => {
  getList();
});
</script>

<style scoped>
.audit-dialog-body { padding: 0 8px; }
.audit-info-section { display: flex; flex-direction: column; gap: 10px; }
.audit-info-row { display: flex; align-items: center; gap: 12px; }
.ai-label { font-size: 13px; color: #64748b; min-width: 70px; text-align: right; }
.ai-value { font-size: 14px; color: #1e293b; font-weight: 500; }
.audit-proof-section { margin-bottom: 8px; }
.proof-file-card { display: flex; align-items: center; gap: 14px; padding: 16px; background: #fffbeb; border: 1px solid #fde68a; border-radius: 10px; }
.proof-info { flex: 1; }
.proof-name { font-size: 14px; font-weight: 600; color: #92400e; }
.proof-hint { font-size: 12px; color: #a16207; margin-top: 2px; }
.no-proof { display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 24px; background: #f8fafc; border-radius: 10px; border: 1px dashed #cbd5e1; color: #94a3b8; font-size: 13px; }

@keyframes exemptPulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(103, 194, 58, 0.4); }
  50% { box-shadow: 0 0 0 6px rgba(103, 194, 58, 0); }
}

/* 华丽的玻璃拟态卡片效果 */
.glass-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.05);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.glass-card::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 5px;
}

.glass-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px 0 rgba(31, 38, 135, 0.1);
}

.stat-primary::before { background: linear-gradient(90deg, #1890ff, #36cfc9); }
.stat-success::before { background: linear-gradient(90deg, #52c41a, #bae637); }
.stat-warning::before { background: linear-gradient(90deg, #fa8c16, #ffd666); }
.stat-info::before { background: linear-gradient(90deg, #722ed1, #efdbff); }

.card-icon {
  font-size: 24px;
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 15px;
}

.stat-primary .card-icon { background: rgba(24, 144, 255, 0.1); color: #1890ff; }
.stat-success .card-icon { background: rgba(82, 196, 26, 0.1); color: #52c41a; }
.stat-warning .card-icon { background: rgba(250, 140, 22, 0.1); color: #fa8c16; }
.stat-info .card-icon { background: rgba(114, 46, 209, 0.1); color: #722ed1; }

.card-info {
  display: flex;
  flex-direction: column;
  margin-bottom: 10px;
}

.card-label {
  font-size: 13px;
  color: #8c8c8c;
  margin-bottom: 5px;
}

.card-value {
  font-size: 28px;
  font-weight: bold;
  color: #262626;
  font-family: 'Outfit', sans-serif;
}

.card-value .unit {
  font-size: 14px;
  font-weight: normal;
  color: #8c8c8c;
  margin-left: 5px;
}

.card-bottom {
  font-size: 12px;
  color: #8c8c8c;
  display: flex;
  align-items: center;
}

.card-bottom .trend {
  color: #52c41a;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 2px;
}

/* 渐变边框卡片 */
.border-gradient {
  position: relative;
  background: #fff;
  border: 1px solid rgba(64, 158, 255, 0.12);
}

.card-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 动感一键比对按钮脉冲 */
.action-btnpulse {
  animation: pulse-glow 2s infinite;
  box-shadow: 0 0 0 0 rgba(103, 194, 58, 0.7);
  background: linear-gradient(135deg, #67c23a, #52c41a);
  border: none;
}

@keyframes pulse-glow {
  0% {
    box-shadow: 0 0 0 0 rgba(103, 194, 58, 0.4);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(103, 194, 58, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(103, 194, 58, 0);
  }
}

/* 闭环管道流程样式 (Stepper Pipeline) */
.pipeline-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fdfdfd;
  padding: 15px 20px;
  border-radius: 10px;
  border: 1px solid #ebeef5;
}

.pipeline-node {
  flex: 1;
  display: flex;
  align-items: center;
  padding: 10px 15px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
  position: relative;
}

.pipeline-node:hover {
  background: rgba(64, 158, 255, 0.05);
}

.pipeline-node.completed .node-icon-wrapper {
  background: rgba(103, 194, 58, 0.15);
  color: #67c23a;
  border-color: #67c23a;
}

.pipeline-node.active {
  background: #ecf5ff;
  border-color: #b3d8ff;
}

.pipeline-node.active .node-icon-wrapper {
  background: #409eff;
  color: #fff;
  border-color: #409eff;
}

.node-icon-wrapper {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 1px solid #dcdfe6;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: #909399;
  background: #fff;
  transition: all 0.3s;
}

.node-details {
  margin-left: 10px;
  display: flex;
  flex-direction: column;
}

.node-title {
  font-size: 13px;
  font-weight: bold;
  color: #606266;
}

.node-badge {
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
}

.pipeline-arrow {
  position: absolute;
  right: -15px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 10;
}

.node-all {
  border-left: 1px solid #ebeef5;
  margin-left: 10px;
  padding-left: 20px;
  flex: 0 0 120px;
}

.node-all.active {
  background: #f0f9eb;
  border-color: #c2e7b0;
}

.node-all.active .node-icon-wrapper {
  background: #67c23a;
  color: #fff;
}

/* 拟兑现金额样式 */
.money-badge {
  background: linear-gradient(135deg, #fff3e0, #ffe0b2);
  color: #e65100;
  font-weight: bold;
  padding: 6px 12px;
  border-radius: 12px;
  border: 1px solid #ffcc80;
  font-family: 'Consolas', monospace;
  box-shadow: 0 2px 4px rgba(230, 81, 0, 0.05);
}

.completed-text {
  color: #67c23a;
  font-size: 13px;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 预算支出图表样式 */
.custom-chart-container {
  padding: 10px 0;
}

.budget-progress-header {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  margin-bottom: 8px;
  color: #606266;
}

.progress-bar-glow {
  background-color: #ebeef5;
  height: 20px;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.05);
  margin-bottom: 25px;
}

.progress-bar-fill {
  background: linear-gradient(90deg, #67c23a, #409eff);
  height: 100%;
  border-radius: 10px;
  box-shadow: 0 0 10px rgba(103, 194, 58, 0.3);
  transition: width 1s ease-in-out;
}

.fund-detail-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.fund-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 15px;
  background: #f8f9fc;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.fund-name {
  font-weight: 500;
  color: #303133;
}

.fund-amount {
  color: #606266;
  font-size: 13px;
  font-family: monospace;
}

/* 效能比较样式 */
.performance-comparison {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.metric-row {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.metric-col {
  flex: 1;
}

.metric-box {
  padding: 15px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
}

.box-red {
  background-color: #fff1f0;
  border: 1px solid #ffa39e;
}

.box-red .title { color: #cf1322; font-size: 13px; font-weight: bold; }
.box-red .number { color: #f5222d; font-size: 28px; font-weight: bold; margin: 8px 0; }
.box-red .desc { color: #8c8c8c; font-size: 11px; }

.box-green {
  background-color: #f6ffed;
  border: 1px solid #b7eb8f;
}

.box-green .title { color: #389e0d; font-size: 13px; font-weight: bold; }
.box-green .number { color: #52c41a; font-size: 28px; font-weight: bold; margin: 8px 0; }
.box-green .desc { color: #8c8c8c; font-size: 11px; }

.animate-pulse {
  animation: box-glow 2.5s infinite alternate;
}

@keyframes box-glow {
  0% { box-shadow: 0 0 4px rgba(82, 196, 26, 0.2); }
  100% { box-shadow: 0 0 16px rgba(82, 196, 26, 0.5); }
}

.feature-checklist {
  display: flex;
  justify-content: space-around;
  background: #f8f9fc;
  padding: 12px;
  border-radius: 8px;
}

.check-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #303133;
}

.empty-state {
  padding: 40px 0;
}
.applied-row { background: #fff7e6 !important; }
.applied-row:hover { background: #ffe7ba !important; }
.mr10 { margin-right: 10px; }
</style>
