<template>
  <div class="app-container" style="background: #f6f8fb; min-height: calc(100vh - 84px); padding: 20px;">
    
    <el-row :gutter="20" class="mb20">
      <el-col :span="8">
        <div class="glass-card stat-primary">
          <div class="card-icon"><el-icon><Money /></el-icon></div>
          <div class="card-info">
            <div class="card-label">资金池总数</div>
            <div class="card-value">{{ total }} <span class="unit">个</span></div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="glass-card stat-success">
          <div class="card-icon"><el-icon><Wallet /></el-icon></div>
          <div class="card-info">
            <div class="card-label">总预算金额</div>
            <div class="card-value">{{ formatMoney(totalBudget) }} <span class="unit">元</span></div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="glass-card stat-warning">
          <div class="card-icon"><el-icon><Promotion /></el-icon></div>
          <div class="card-info">
            <div class="card-label">已拨付金额</div>
            <div class="card-value">{{ formatMoney(totalUsed) }} <span class="unit">元</span></div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-card style="border-radius: 12px; box-shadow: 0 4px 12px 0 rgba(0,0,0,0.05);">
      <template #header>
        <div class="card-header-flex">
          <span class="header-title"><el-icon><List /></el-icon> 资金池管理</span>
          <el-button type="primary" plain icon="Plus" @click="handleAdd" style="border-radius: 8px;">新增资金池</el-button>
        </div>
      </template>
      <el-table v-loading="loading" :data="fundList" style="border-radius: 8px;" :header-cell-style="{ background: '#f5f7fa', color: '#606266', fontWeight: 'bold' }">
        <el-table-column label="资金池ID" align="center" prop="fundId" width="90" />
        <el-table-column label="关联政策" align="left" prop="policyName" min-width="220" show-overflow-tooltip>
          <template #default="scope">
            <el-tag v-if="scope.row.policyName" type="primary" effect="plain" style="border-radius: 6px;">{{ scope.row.policyName }}</el-tag>
            <span v-else style="color: #c0c4cc;">未关联</span>
          </template>
        </el-table-column>
        <el-table-column label="总预算" align="right" prop="totalBudget" width="160">
          <template #default="scope">
            <span class="money-badge">{{ formatMoney(scope.row.totalBudget) }} 元</span>
          </template>
        </el-table-column>
        <el-table-column label="已拨付" align="right" prop="usedAmount" width="160">
          <template #default="scope">
            <span style="color: #e6a23c; font-weight: bold; font-family: 'Consolas', monospace;">{{ formatMoney(scope.row.usedAmount) }} 元</span>
          </template>
        </el-table-column>
        <el-table-column label="剩余额度" align="right" width="160">
          <template #default="scope">
            <span :style="{ color: getRemainColor(scope.row), fontWeight: 'bold', fontFamily: 'Consolas, monospace' }">
              {{ formatMoney(getRemain(scope.row)) }} 元
            </span>
          </template>
        </el-table-column>
        <el-table-column label="预算使用率" align="center" width="200">
          <template #default="scope">
            <el-progress :percentage="getUsageRate(scope.row)" :color="getProgressColor(scope.row)" :stroke-width="12" :text-inside="true" style="width: 100%;" />
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="160">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
            <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>

    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="fundRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="关联政策" prop="policyId">
          <el-select v-model="form.policyId" placeholder="请选择关联政策" style="width: 100%;" filterable>
            <el-option v-for="p in policyOptions" :key="p.policyId" :label="p.policyName" :value="p.policyId" />
          </el-select>
        </el-form-item>
        <el-form-item label="总预算金额" prop="totalBudget">
          <el-input-number v-model="form.totalBudget" :min="0" :precision="2" placeholder="请输入总预算" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="已拨付金额" prop="usedAmount">
          <el-input-number v-model="form.usedAmount" :min="0" :precision="2" placeholder="请输入已拨付金额" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm" style="border-radius: 8px;">确 定</el-button>
          <el-button @click="cancel" style="border-radius: 8px;">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="EeFund">
import { listEeFund, getEeFund, delEeFund, addEeFund, updateEeFund } from "@/api/biz/fund";
import { listEePolicy } from "@/api/biz/policy";
import { ref, reactive, toRefs, computed, getCurrentInstance } from 'vue';

const { proxy } = getCurrentInstance();

const fundList = ref([]);
const policyOptions = ref([]);
const open = ref(false);
const loading = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10 }
});
const { queryParams, form } = toRefs(data);

const rules = {
  policyId: [{ required: true, message: "请选择关联政策", trigger: "change" }],
  totalBudget: [{ required: true, message: "总预算不能为空", trigger: "blur" }]
};

const totalBudget = computed(() => fundList.value.reduce((sum, f) => sum + Number(f.totalBudget || 0), 0));
const totalUsed = computed(() => fundList.value.reduce((sum, f) => sum + Number(f.usedAmount || 0), 0));

function formatMoney(val) {
  if (val === undefined || val === null) return "0.00";
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function getRemain(row) {
  return Number(row.totalBudget || 0) - Number(row.usedAmount || 0);
}

function getRemainColor(row) {
  const rate = getUsageRate(row);
  if (rate >= 90) return '#f56c6c';
  if (rate >= 70) return '#e6a23c';
  return '#67c23a';
}

function getUsageRate(row) {
  const budget = Number(row.totalBudget || 0);
  if (budget === 0) return 0;
  return Math.min(Math.round((Number(row.usedAmount || 0) / budget) * 100), 100);
}

function getProgressColor(row) {
  const rate = getUsageRate(row);
  if (rate >= 90) return '#f56c6c';
  if (rate >= 70) return '#e6a23c';
  return '#67c23a';
}

function getList() {
  loading.value = true;
  listEeFund(queryParams.value).then(response => {
    fundList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

function loadPolicies() {
  listEePolicy().then(response => { policyOptions.value = response.rows || []; });
}

function cancel() { open.value = false; reset(); }
function reset() {
  form.value = { fundId: undefined, policyId: undefined, totalBudget: undefined, usedAmount: 0 };
  proxy.resetForm("fundRef");
}

function handleAdd() { reset(); loadPolicies(); open.value = true; title.value = "新增资金池"; }
function handleUpdate(row) {
  reset(); loadPolicies();
  getEeFund(row.fundId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改资金池";
  });
}

function submitForm() {
  proxy.$refs["fundRef"].validate(valid => {
    if (valid) {
      if (form.value.fundId != null) {
        updateEeFund(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); });
      } else {
        addEeFund(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); });
      }
    }
  });
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除该资金池？').then(() => {
    return delEeFund(row.fundId);
  }).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {});
}

getList();
</script>

<style scoped>
.glass-card { background: rgba(255,255,255,0.7); backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.4); border-radius: 16px; padding: 20px; display: flex; flex-direction: column; box-shadow: 0 8px 32px 0 rgba(31,38,135,0.05); transition: all 0.3s; position: relative; overflow: hidden; }
.glass-card::before { content: ""; position: absolute; top: 0; left: 0; width: 100%; height: 5px; }
.glass-card:hover { transform: translateY(-5px); box-shadow: 0 12px 40px 0 rgba(31,38,135,0.1); }
.stat-primary::before { background: linear-gradient(90deg, #1890ff, #36cfc9); }
.stat-success::before { background: linear-gradient(90deg, #52c41a, #bae637); }
.stat-warning::before { background: linear-gradient(90deg, #fa8c16, #ffd666); }
.card-icon { font-size: 24px; width: 44px; height: 44px; border-radius: 10px; display: flex; align-items: center; justify-content: center; margin-bottom: 15px; }
.stat-primary .card-icon { background: rgba(24,144,255,0.1); color: #1890ff; }
.stat-success .card-icon { background: rgba(82,196,26,0.1); color: #52c41a; }
.stat-warning .card-icon { background: rgba(250,140,22,0.1); color: #fa8c16; }
.card-info { display: flex; flex-direction: column; margin-bottom: 10px; }
.card-label { font-size: 13px; color: #8c8c8c; margin-bottom: 5px; }
.card-value { font-size: 28px; font-weight: bold; color: #262626; font-family: 'Outfit', sans-serif; }
.card-value .unit { font-size: 14px; font-weight: normal; color: #8c8c8c; margin-left: 5px; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.header-title { font-size: 16px; font-weight: bold; color: #303133; display: flex; align-items: center; gap: 8px; }
.money-badge { background: linear-gradient(135deg, #e6f7ff, #bae7ff); color: #1890ff; font-weight: bold; padding: 4px 10px; border-radius: 10px; border: 1px solid #91d5ff; font-family: 'Consolas', monospace; font-size: 13px; }
.mb20 { margin-bottom: 20px; }
</style>
