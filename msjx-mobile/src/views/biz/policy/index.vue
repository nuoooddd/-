<template>
  <div class="m-page">
    <div class="stat-row">
      <div class="stat-item" style="background:#e0e7ff"><div class="si-val">{{ total }}</div><div class="si-label">政策总数</div></div>
      <div class="stat-item" style="background:#d1fae5"><div class="si-val">{{ activeCount }}</div><div class="si-label">生效中</div></div>
      <div class="stat-item" style="background:#fef3c7"><div class="si-val">{{ formatMoneyShort(totalBudget) }}</div><div class="si-label">预算(万)</div></div>
    </div>

    <div class="search-bar">
      <el-input v-model="queryParams.policyName" placeholder="搜索政策名称" clearable @keyup.enter="handleQuery" prefix-icon="Search" />
      <el-select v-model="queryParams.policyType" placeholder="类型" clearable style="width:90px">
        <el-option v-for="dict in biz_policy_type" :key="dict.value" :label="dict.label" :value="dict.value" />
      </el-select>
      <el-select v-model="queryParams.status" placeholder="状态" clearable style="width:80px">
        <el-option v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
      </el-select>
    </div>

    <div class="list-header">
      <span>政策列表</span>
      <el-button type="primary" size="small" @click="handleAdd" round>新增</el-button>
    </div>

    <div v-loading="loading">
      <div v-for="item in policyList" :key="item.policyId" class="card-item" @click="handleUpdate(item)">
        <div class="ci-top">
          <span class="ci-name">{{ item.policyName }}</span>
          <dict-tag :options="sys_normal_disable" :value="item.status" />
        </div>
        <div class="ci-mid">
          <dict-tag :options="biz_policy_type" :value="item.policyType" />
          <span class="ci-money">{{ formatMoney(item.amount) }} 元</span>
        </div>
        <div class="ci-bottom">
          <span>{{ item.publishDept || '--' }}</span>
          <span>{{ item.startDate || '?' }} ~ {{ item.endDate || '长期' }}</span>
        </div>
        <div class="ci-actions" @click.stop>
          <el-button link type="primary" size="small" @click="handleUpdate(item)">编辑</el-button>
          <el-button link type="success" size="small" @click="handleAiLearn(item)">AI学习</el-button>
          <el-button link v-if="item.pdfName" type="primary" size="small" @click="previewPdf(item)">PDF</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(item)">删除</el-button>
        </div>
      </div>
      <el-empty v-if="!loading && policyList.length===0" description="暂无政策" :image-size="60" />
    </div>

    <div class="pager-row" v-if="total > 0">
      <el-button :disabled="queryParams.pageNum<=1" @click="queryParams.pageNum--;getList()" size="small">上一页</el-button>
      <span>{{ queryParams.pageNum }} / {{ Math.ceil(total/queryParams.pageSize) }}</span>
      <el-button :disabled="queryParams.pageNum>=Math.ceil(total/queryParams.pageSize)" @click="queryParams.pageNum++;getList()" size="small">下一页</el-button>
    </div>

    <el-dialog :title="title" v-model="open" width="92%" append-to-body destroy-on-close>
      <el-form ref="policyRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="政策名称" prop="policyName"><el-input v-model="form.policyName" placeholder="请输入政策名称" /></el-form-item>
        <el-form-item label="政策类型" prop="policyType">
          <el-select v-model="form.policyType" placeholder="请选择" style="width:100%">
            <el-option v-for="dict in biz_policy_type" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="发放金额" prop="amount"><el-input-number v-model="form.amount" :min="0" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="发布部门" prop="publishDept"><el-input v-model="form.publishDept" placeholder="请输入发布部门" /></el-form-item>
        <el-form-item label="生效日期"><el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="到期日期"><el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="政策文件">
          <el-upload action="/dev-api/common/upload" :headers="uploadHeaders" :on-success="handlePdfSuccess" :on-remove="handlePdfRemove" :file-list="pdfFileList" accept=".pdf" :limit="1">
            <el-button type="primary" plain size="small"><el-icon><Upload /></el-icon> 上传PDF</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status"><el-radio v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio></el-radio-group>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="AI学习"><el-input v-model="form.aiContent" type="textarea" :rows="4" placeholder="输入政策详细内容，AI将学习后为用户解答" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="cancel">取消</el-button><el-button type="primary" @click="submitForm">确定</el-button></template>
    </el-dialog>

    <el-dialog title="PDF预览" v-model="pdfPreviewOpen" width="92%" append-to-body>
      <iframe v-if="pdfPreviewUrl" :src="pdfPreviewUrl" style="width:100%;height:60vh;border:none" />
    </el-dialog>
  </div>
</template>

<script setup name="EePolicy">
import { listEePolicy, getEePolicy, delEePolicy, addEePolicy, updateEePolicy } from "@/api/biz/policy";
import { aiExtract } from "@/api/biz/ai";
import { ref, reactive, toRefs, computed, getCurrentInstance } from 'vue';
import { getToken } from '@/utils/auth'

const { proxy } = getCurrentInstance();
const { biz_policy_type, sys_normal_disable } = proxy.useDict("biz_policy_type", "sys_normal_disable");

const policyList = ref([]);
const open = ref(false);
const loading = ref(true);
const total = ref(0);
const title = ref("");
const pdfPreviewOpen = ref(false);
const pdfPreviewUrl = ref("");
const pdfFileList = ref([]);
const uploadHeaders = ref({ Authorization: 'Bearer ' + getToken() });

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, policyName: undefined, policyType: undefined, publishDept: undefined, status: undefined }
});
const { queryParams, form } = toRefs(data);

const rules = {
  policyName: [{ required: true, message: "政策名称不能为空", trigger: "blur" }],
  policyType: [{ required: true, message: "请选择政策类型", trigger: "change" }],
  amount: [{ required: true, message: "发放金额不能为空", trigger: "blur" }],
  publishDept: [{ required: true, message: "发布部门不能为空", trigger: "blur" }]
};

const activeCount = computed(() => policyList.value.filter(p => p.status === '0').length);
const totalBudget = computed(() => policyList.value.reduce((sum, p) => sum + Number(p.amount || 0), 0));

function handlePdfSuccess(res, file) {
  if (res.code === 200) {
    form.value.pdfUrl = res.url || res.fileName;
    form.value.pdfName = file.name;
    pdfFileList.value = [{ name: file.name, url: res.url || res.fileName }];
    proxy.$modal.msgSuccess("PDF上传成功！");
  }
}
function handlePdfRemove() { form.value.pdfUrl = null; form.value.pdfName = null; pdfFileList.value = []; }
function previewPdf(row) {
  if (row.pdfUrl) {
    let url = row.pdfUrl;
    if (url.startsWith('http')) { const idx = url.indexOf('/profile/'); if (idx !== -1) url = '/dev-api' + url.substring(idx); }
    else if (url.startsWith('/profile')) { url = '/dev-api' + url; }
    pdfPreviewUrl.value = url + '#toolbar=0';
    pdfPreviewOpen.value = true;
  }
}
function getList() {
  loading.value = true;
  listEePolicy(queryParams.value).then(response => { policyList.value = response.rows; total.value = response.total; loading.value = false; });
}
function formatMoney(val) { if (val === undefined || val === null) return "0.00"; return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }); }
function formatMoneyShort(val) { if (!val) return '0'; return (Number(val)/10000).toFixed(1); }
function handleQuery() { queryParams.value.pageNum = 1; getList(); }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery(); }
function cancel() { open.value = false; reset(); }
function reset() {
  Object.keys(form.value).forEach(k => delete form.value[k]);
  Object.assign(form.value, { policyId: undefined, policyName: undefined, policyType: undefined, amount: undefined, publishDept: undefined, status: "0", remark: undefined, startDate: undefined, endDate: undefined, pdfUrl: undefined, pdfName: undefined });
  pdfFileList.value = [];
  proxy.resetForm("policyRef");
}
function handleAdd() { reset(); open.value = true; title.value = "新增政策"; }
function handleUpdate(row) {
  reset();
  getEePolicy(row.policyId).then(response => {
    Object.assign(form.value, response.data);
    form.value.status = String(form.value.status);
    if (response.data.pdfName) { pdfFileList.value = [{ name: response.data.pdfName, url: response.data.pdfUrl }]; }
    open.value = true; title.value = "修改政策";
  });
}
function submitForm() {
  proxy.$refs["policyRef"].validate(valid => {
    if (valid) {
      if (form.value.policyId != null) { updateEePolicy(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); }); }
      else { addEePolicy(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); }); }
    }
  });
}
function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除政策"' + row.policyName + '"？').then(() => { return delEePolicy(row.policyId); }).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {});
}
function handleAiLearn(row) {
  proxy.$modal.confirm('确认让AI学习「' + row.policyName + '」？').then(() => {
    aiExtract(row.policyId).then(res => { if (res.code === 200) { proxy.$modal.msgSuccess("AI学习成功！"); } else { proxy.$modal.msgError(res.msg || "学习失败"); } });
  }).catch(() => {});
}
getList();
</script>

<style scoped>
.m-page { padding: 12px; background: #f5f6fa; min-height: 100%; }
.stat-row { display: flex; gap: 8px; margin-bottom: 12px; }
.stat-item { flex: 1; text-align: center; padding: 10px 6px; border-radius: 10px; }
.si-val { font-size: 18px; font-weight: 700; color: #1e293b; font-family: 'Outfit', sans-serif; }
.si-label { font-size: 10px; color: #64748b; margin-top: 2px; }

.search-bar { display: flex; gap: 6px; margin-bottom: 10px; }
.search-bar .el-input { flex: 1; }

.list-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; font-size: 14px; font-weight: 600; color: #0f172a; }

.card-item { background: #fff; border: 1px solid #e2e8f0; border-radius: 10px; padding: 12px; margin-bottom: 8px; }
.ci-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.ci-name { font-size: 14px; font-weight: 600; color: #1e293b; flex: 1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-right: 8px; }
.ci-mid { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.ci-money { color: #e65100; font-weight: 600; font-size: 13px; font-family: Consolas, monospace; }
.ci-bottom { display: flex; justify-content: space-between; font-size: 11px; color: #94a3b8; }
.ci-actions { display: flex; gap: 4px; margin-top: 8px; padding-top: 8px; border-top: 1px solid #f1f5f9; }

.pager-row { display: flex; justify-content: center; align-items: center; gap: 12px; padding: 10px 0; font-size: 13px; color: #64748b; }
</style>
