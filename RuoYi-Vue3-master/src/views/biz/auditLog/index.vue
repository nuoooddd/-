<template>
  <div class="app-container" style="background:#f6f8fb;min-height:calc(100vh-84px);padding:20px">
    <div class="audit-hero animate-fade-in-up">
      <h1 class="hero-title">操作审计日志</h1>
      <p class="hero-desc">全流程操作留痕 · 安全可追溯</p>
    </div>


    <div class="biz-section animate-fade-in-up stagger-2">
      <div class="card-header-flex">
        <span class="header-title"><el-icon><List /></el-icon> 审计日志列表</span>
        <div class="filter-bar">
          <el-input v-model="searchUserName" placeholder="搜索操作人" clearable size="default" style="width: 160px;" @keyup.enter="getList" prefix-icon="Search" />
          <el-select v-model="searchModule" placeholder="模块" clearable size="default" style="width: 130px;" @change="getList">
            <el-option label="政策管理" value="政策管理" />
            <el-option label="规则管理" value="规则管理" />
            <el-option label="匹配兑现" value="匹配兑现" />
            <el-option label="资金管理" value="资金管理" />
            <el-option label="目标数据" value="目标数据" />
          </el-select>
          <el-button type="primary" icon="Search" @click="getList" size="default">搜索</el-button>
        </div>
      </div>

      <el-table :data="logList" style="width: 100%;" :header-cell-style="{ background: '#f8fafc', color: '#475569', fontWeight: '600', fontSize: '13px' }" stripe>
        <el-table-column label="时间" align="center" prop="createTime" width="170">
          <template #default="{ row }">
            <span style="color: #64748b; font-size: 13px; font-family: 'Consolas', monospace;">{{ row.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作人" align="center" prop="userName" width="120">
          <template #default="{ row }">
            <span style="font-weight: 500; color: #1e293b;">{{ row.userName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="模块" align="center" width="120">
          <template #default="{ row }">
            <span class="module-tag" :class="'tag-' + row.module">{{ getModuleLabel(row.module) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" prop="operation" width="120">
          <template #default="{ row }">
            <span style="font-weight: 500;">{{ row.operation }}</span>
          </template>
        </el-table-column>
        <el-table-column label="目标ID" align="center" prop="targetId" width="100">
          <template #default="{ row }">
            <span style="color: #94a3b8; font-family: 'Consolas', monospace; font-size: 12px;">{{ row.targetId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="详情" align="left" prop="detail" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span style="color: #475569; font-size: 13px;">{{ row.detail }}</span>
          </template>
        </el-table-column>
        <el-table-column label="IP" align="center" prop="ip" width="130">
          <template #default="{ row }">
            <span style="color: #94a3b8; font-family: 'Consolas', monospace; font-size: 12px;">{{ row.ip }}</span>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="pageNum" v-model:limit="pageSize" @pagination="getList" />
    </div>
  </div>
</template>

<script setup name="EeAuditLog">
import { ref, onMounted } from 'vue'
import { listEeAuditLog } from '@/api/biz/auditLog'

const logList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const searchUserName = ref('')
const searchModule = ref('')

function getList() {
  listEeAuditLog({
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    userName: searchUserName.value || undefined,
    module: searchModule.value || undefined
  }).then(res => {
    logList.value = res.rows || []
    total.value = res.total || 0
  })
}

function getModuleLabel(m) { return { policy: '政策管理', rule: '规则管理', matchRecord: '匹配兑现', fund: '资金管理', targetData: '目标数据' }[m] || m }

onMounted(() => { getList() })
</script>

<style lang="scss" scoped>


.audit-hero {
  margin-bottom: 24px;
}

.hero-title {
  font-size: 24px;
  font-weight: 700;
  background: linear-gradient(135deg, #409EFF, #7c3aed);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0 0 4px 0;
}

.hero-desc {
  font-size: 14px;
  color: #94a3b8;
  margin: 0;
}

.glass-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.05);
  transition: all 0.3s;
  position: relative;
  overflow: hidden;

  &::before {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 4px;
  }

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 40px 0 rgba(31, 38, 135, 0.1);
  }
}

.stat-total::before { background: linear-gradient(90deg, #409EFF, #66b1ff); }
.stat-policy::before { background: linear-gradient(90deg, #7c3aed, #a78bfa); }
.stat-match::before { background: linear-gradient(90deg, #67C23A, #85ce61); }

.card-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.stat-total .card-icon-wrap { background: rgba(64, 158, 255, 0.1); color: #409EFF; }
.stat-policy .card-icon-wrap { background: rgba(124, 58, 237, 0.1); color: #7c3aed; }
.stat-match .card-icon-wrap { background: rgba(103, 194, 58, 0.1); color: #67C23A; }

.stat-num {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1;
}

.stat-label {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}

.filter-bar {
  display: flex;
  gap: 10px;
  align-items: center;
}

.module-tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;

  &.tag-policy { background: rgba(64, 158, 255, 0.1); color: #409EFF; }
  &.tag-rule { background: rgba(230, 162, 60, 0.1); color: #E6A23C; }
  &.tag-matchRecord { background: rgba(103, 194, 58, 0.1); color: #67C23A; }
  &.tag-fund { background: rgba(245, 108, 108, 0.1); color: #F56C6C; }
  &.tag-targetData { background: rgba(144, 147, 153, 0.1); color: #909399; }
}

.mb20 { margin-bottom: 20px; }
</style>