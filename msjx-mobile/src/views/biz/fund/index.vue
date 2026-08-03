<template>
  <div class="m-page">
    <div class="stat-row">
      <div class="stat-item" style="background:#e0e7ff"><div class="si-val">{{ total }}</div><div class="si-label">资金池</div></div>
      <div class="stat-item" style="background:#d1fae5"><div class="si-val">{{ formatMoneyShort(totalBudget) }}</div><div class="si-label">总预算(万)</div></div>
      <div class="stat-item" style="background:#fef3c7"><div class="si-val">{{ formatMoneyShort(totalUsed) }}</div><div class="si-label">已拨付(万)</div></div>
    </div>

    <div class="list-header">
      <span>资金池管理</span>
      <el-button type="primary" size="small" @click="handleAdd" round>新增</el-button>
    </div>

    <div v-loading="loading">
      <div v-for="item in fundList" :key="item.fundId" class="card-item">
        <div class="ci-top">
          <el-tag v-if="item.policyName" type="primary" effect="plain" size="small">{{ item.policyName }}</el-tag>
          <span v-else class="ci-empty">未关联政策</span>
        </div>
        <div class="ci-amounts">
          <div class="amt-row"><span class="amt-label">总预算</span><span class="amt-val">{{ formatMoney(item.totalBudget) }} 元</span></div>
          <div class="amt-row"><span class="amt-label">已拨付</span><span class="amt-val" style="color:#e6a23c">{{ formatMoney(item.usedAmount) }} 元</span></div>
          <div class="amt-row"><span class="amt-label">剩余</span><span class="amt-val" :style="{color:getRemainColor(item)}">{{ formatMoney(getRemain(item)) }} 元</span></div>
        </div>
        <el-progress :percentage="getUsageRate(item)" :color="getProgressColor(item)" :stroke-width="8" style="margin:8px 0" />
        <div class="ci-actions" @click.stop>
          <el-button link type="primary" size="small" @click="handleUpdate(item)">编辑</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(item)">删除</el-button>
        </div>
      </div>
      <el-empty v-if="!loading && fundList.length===0" description="暂无资金池" :image-size="60" />
    </div>

    <el-dialog :title="title" v-model="open" width="92%" append-to-body destroy-on-close>
      <el-form ref="fundRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="关联政策" prop="policyId">
          <el-select v-model="form.policyId" placeholder="请选择关联政策" style="width:100%" filterable>
            <el-option v-for="p in policyOptions" :key="p.policyId" :label="p.policyName" :value="p.policyId" />
          </el-select>
        </el-form-item>
        <el-form-item label="总预算" prop="totalBudget"><el-input-number v-model="form.totalBudget" :min="0" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="已拨付" prop="usedAmount"><el-input-number v-model="form.usedAmount" :min="0" :precision="2" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="cancel">取消</el-button><el-button type="primary" @click="submitForm">确定</el-button></template>
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

const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10 } });
const { queryParams, form } = toRefs(data);

const rules = {
  policyId: [{ required: true, message: "请选择关联政策", trigger: "change" }],
  totalBudget: [{ required: true, message: "总预算不能为空", trigger: "blur" }]
};

const totalBudget = computed(() => fundList.value.reduce((sum, f) => sum + Number(f.totalBudget || 0), 0));
const totalUsed = computed(() => fundList.value.reduce((sum, f) => sum + Number(f.usedAmount || 0), 0));

function formatMoney(val) { if (val === undefined || val === null) return "0.00"; return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }); }
function formatMoneyShort(val) { if (!val) return '0'; return (Number(val)/10000).toFixed(1); }
function getRemain(row) { return Number(row.totalBudget || 0) - Number(row.usedAmount || 0); }
function getRemainColor(row) { const r = getUsageRate(row); if (r >= 90) return '#f56c6c'; if (r >= 70) return '#e6a23c'; return '#67c23a'; }
function getUsageRate(row) { const b = Number(row.totalBudget || 0); if (b === 0) return 0; return Math.min(Math.round((Number(row.usedAmount || 0) / b) * 100), 100); }
function getProgressColor(row) { const r = getUsageRate(row); if (r >= 90) return '#f56c6c'; if (r >= 70) return '#e6a23c'; return '#67c23a'; }

function getList() { loading.value = true; listEeFund(queryParams.value).then(response => { fundList.value = response.rows; total.value = response.total; loading.value = false; }); }
function loadPolicies() { listEePolicy().then(response => { policyOptions.value = response.rows || []; }); }
function cancel() { open.value = false; reset(); }
function reset() { form.value = { fundId: undefined, policyId: undefined, totalBudget: undefined, usedAmount: 0 }; proxy.resetForm("fundRef"); }
function handleAdd() { reset(); loadPolicies(); open.value = true; title.value = "新增资金池"; }
function handleUpdate(row) { reset(); loadPolicies(); getEeFund(row.fundId).then(response => { form.value = response.data; open.value = true; title.value = "修改资金池"; }); }
function submitForm() {
  proxy.$refs["fundRef"].validate(valid => {
    if (valid) {
      if (form.value.fundId != null) { updateEeFund(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); }); }
      else { addEeFund(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); }); }
    }
  });
}
function handleDelete(row) { proxy.$modal.confirm('是否确认删除该资金池？').then(() => { return delEeFund(row.fundId); }).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {}); }
getList();
</script>

<style scoped>
.m-page { padding:12px; background:#f5f6fa; min-height:100%; }
.stat-row { display:flex; gap:8px; margin-bottom:12px; }
.stat-item { flex:1; text-align:center; padding:10px 6px; border-radius:10px; }
.si-val { font-size:18px; font-weight:700; color:#1e293b; font-family:'Outfit',sans-serif; }
.si-label { font-size:10px; color:#64748b; margin-top:2px; }
.list-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:8px; font-size:14px; font-weight:600; color:#0f172a; }
.card-item { background:#fff; border:1px solid #e2e8f0; border-radius:10px; padding:12px; margin-bottom:8px; }
.ci-top { margin-bottom:8px; }
.ci-empty { font-size:12px; color:#c0c4cc; }
.ci-amounts { display:flex; flex-direction:column; gap:4px; }
.amt-row { display:flex; justify-content:space-between; align-items:center; }
.amt-label { font-size:12px; color:#64748b; }
.amt-val { font-size:13px; color:#1e293b; font-weight:600; font-family:Consolas,monospace; }
.ci-actions { display:flex; gap:4px; margin-top:8px; padding-top:8px; border-top:1px solid #f1f5f9; }
</style>
