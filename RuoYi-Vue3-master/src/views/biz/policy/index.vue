<template>
  <div class="app-container" style="background: #f6f8fb; min-height: calc(100vh - 84px); padding: 20px;">
    
    <el-row :gutter="20" class="mb20">
      <el-col :span="8">
        <div class="glass-card stat-primary">
          <div class="card-icon"><el-icon><Memo /></el-icon></div>
          <div class="card-info">
            <div class="card-label">政策总数</div>
            <div class="card-value">{{ total }} <span class="unit">项</span></div>
          </div>
          <div class="card-bottom">
            <span class="trend">覆盖补贴、奖励、减免三大类型</span>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="glass-card stat-success">
          <div class="card-icon"><el-icon><CircleCheck /></el-icon></div>
          <div class="card-info">
            <div class="card-label">正常生效政策</div>
            <div class="card-value">{{ activeCount }} <span class="unit">项</span></div>
          </div>
          <div class="card-bottom">
            <span class="trend">自动匹配引擎已就绪</span>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="glass-card stat-warning">
          <div class="card-icon"><el-icon><Money /></el-icon></div>
          <div class="card-info">
            <div class="card-label">政策资金总预算</div>
            <div class="card-value">{{ formatMoney(totalBudget) }} <span class="unit">元</span></div>
          </div>
          <div class="card-bottom">
            <span class="trend">多部门财政资金池联动</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-card style="border-radius: 12px; box-shadow: 0 4px 12px 0 rgba(0,0,0,0.05);">
      <template #header>
        <div class="card-header-flex">
          <span class="header-title"><el-icon><Search /></el-icon> 政策检索</span>
        </div>
      </template>
      <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="68px">
        <el-form-item label="政策名称" prop="policyName">
          <el-input v-model="queryParams.policyName" placeholder="请输入政策名称" clearable @keyup.enter="handleQuery" style="width: 200px;" />
        </el-form-item>
        <el-form-item label="政策类型" prop="policyType">
          <el-select v-model="queryParams.policyType" placeholder="全部类型" clearable style="width: 140px;">
            <el-option v-for="dict in biz_policy_type" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="发布部门" prop="publishDept">
          <el-input v-model="queryParams.publishDept" placeholder="请输入发布部门" clearable @keyup.enter="handleQuery" style="width: 200px;" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 120px;">
            <el-option v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="mt20" style="border-radius: 12px; box-shadow: 0 4px 12px 0 rgba(0,0,0,0.05);">
      <template #header>
        <div class="card-header-flex">
          <span class="header-title"><el-icon><List /></el-icon> 政策列表</span>
          <div>
            <el-button type="primary" plain icon="Plus" @click="handleAdd" style="border-radius: 8px;">新增政策</el-button>
          </div>
        </div>
      </template>
      <el-table v-loading="loading" :data="policyList" style="border-radius: 8px;" :header-cell-style="{ background: '#f5f7fa', color: '#606266', fontWeight: 'bold' }">
        <el-table-column label="政策ID" align="center" prop="policyId" width="80" />
        <el-table-column label="政策名称" align="left" prop="policyName" min-width="240" show-overflow-tooltip>
          <template #default="scope">
            <div style="display: flex; align-items: center;">
              <el-icon color="#409EFF" style="margin-right: 8px;"><Memo /></el-icon>
              <span style="font-weight: 500;">{{ scope.row.policyName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="政策类型" align="center" prop="policyType" width="100">
          <template #default="scope">
            <dict-tag :options="biz_policy_type" :value="scope.row.policyType" />
          </template>
        </el-table-column>
        <el-table-column label="发放金额" align="right" prop="amount" width="140">
          <template #default="scope">
            <span class="money-badge">{{ formatMoney(scope.row.amount) }} 元</span>
          </template>
        </el-table-column>
        <el-table-column label="发布部门" align="center" prop="publishDept" min-width="160" show-overflow-tooltip />
        <el-table-column label="有效期" align="center" width="200">
          <template #default="scope">
            <span v-if="scope.row.startDate || scope.row.endDate" style="font-size: 12px; color: #606266;">
              {{ scope.row.startDate || '-' }} ~ {{ scope.row.endDate || '长期' }}
            </span>
            <span v-else style="color: #c0c4cc; font-size: 12px;">未设置</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="status" width="80">
          <template #default="scope">
            <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="160" />
        <el-table-column label="政策文件" align="center" width="120">
          <template #default="scope">
            <el-link v-if="scope.row.pdfName" type="primary" :underline="false" @click="previewPdf(scope.row)">
              <el-icon><Document /></el-icon> 查看
            </el-link>
            <span v-else style="color:#c0c4cc">无</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
            <el-button link type="success" icon="Cpu" @click="handleAiLearn(scope.row)">AI学习</el-button>
            <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>

    <el-dialog :title="title" v-model="open" width="600px" append-to-body style="border-radius: 12px;">
      <el-form ref="policyRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="政策名称" prop="policyName">
          <el-input v-model="form.policyName" placeholder="请输入政策名称" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="政策类型" prop="policyType">
              <el-select v-model="form.policyType" placeholder="请选择政策类型" style="width: 100%;">
                <el-option v-for="dict in biz_policy_type" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发放金额" prop="amount">
              <el-input-number v-model="form.amount" :min="0" :precision="2" placeholder="请输入金额" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="发布部门" prop="publishDept">
          <el-input v-model="form.publishDept" placeholder="请输入发布部门" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="生效日期" prop="startDate">
              <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="选择生效日期" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="到期日期" prop="endDate">
              <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="选择到期日期" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="政策文件PDF">
          <el-upload
            class="pdf-uploader"
            action="/dev-api/common/upload"
            :headers="uploadHeaders"
            :on-success="handlePdfSuccess"
            :on-remove="handlePdfRemove"
            :file-list="pdfFileList"
            accept=".pdf"
            :limit="1"
          >
            <el-button type="primary" plain><el-icon><Upload /></el-icon> 上传政策文件PDF</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
          <div v-if="form.status === '1'" style="margin-top: 8px; padding: 10px 14px; background: #fef0f0; border-left: 3px solid #f56c6c; border-radius: 6px; color: #c45656; font-size: 13px; line-height: 1.6;">
            ⚠ 停用该政策后，其关联的所有规则也将同步停用
          </div>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item label="AI学习内容">
          <el-input v-model="form.aiContent" type="textarea" :rows="6" placeholder="输入政策详细内容（申请条件、补贴标准、办理流程、申报材料等），AI助手将学习这些内容为用户解答问题" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm" style="border-radius: 8px;">确 定</el-button>
          <el-button @click="cancel" style="border-radius: 8px;">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="政策文件预览" v-model="pdfPreviewOpen" width="1100px" append-to-body>
      <iframe v-if="pdfPreviewUrl" :src="pdfPreviewUrl" style="width:100%;height:85vh;border:none" />
      <template #footer>
        <el-button type="primary" @click="downloadPdf" icon="Download">下载</el-button>
        <el-button @click="pdfPreviewOpen = false">关闭</el-button>
      </template>
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

function handlePdfRemove() {
  form.value.pdfUrl = null;
  form.value.pdfName = null;
  pdfFileList.value = [];
}

function previewPdf(row) {
  if (row.pdfUrl) {
    let url = row.pdfUrl;
    if (url.startsWith('http')) {
      const idx = url.indexOf('/profile/');
      if (idx !== -1) url = '/dev-api' + url.substring(idx);
    } else if (url.startsWith('/profile')) {
      url = '/dev-api' + url;
    }
    pdfPreviewUrl.value = url + '#toolbar=0';
    pdfPreviewOpen.value = true;
  }
}

function downloadPdf() {
  if (pdfPreviewUrl.value) {
    let url = pdfPreviewUrl.value.replace("#toolbar=0", "");
    const a = document.createElement("a");
    a.href = url;
    a.download = "";
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }
}

function getList() {
  loading.value = true;
  listEePolicy(queryParams.value).then(response => {
    policyList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

function formatMoney(val) {
  if (val === undefined || val === null) return "0.00";
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

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
    if (response.data.pdfName) {
      pdfFileList.value = [{ name: response.data.pdfName, url: response.data.pdfUrl }];
    }
    open.value = true;
    title.value = "修改政策";
  });
}

function submitForm() {
  proxy.$refs["policyRef"].validate(valid => {
    if (valid) {
      if (form.value.policyId != null) {
        updateEePolicy(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); });
      } else {
        addEePolicy(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); });
      }
    }
  });
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除政策"' + row.policyName + '"？').then(() => {
    return delEePolicy(row.policyId);
  }).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {});
}

function handleAiLearn(row) {
  proxy.$modal.confirm('确认让AI学习「' + row.policyName + '」的政策内容？学习后用户可通过AI助手提问相关问题。').then(() => {
    aiExtract(row.policyId).then(res => {
      if (res.code === 200) {
        proxy.$modal.msgSuccess("AI学习成功！用户现在可以就该政策提问了");
      } else {
        proxy.$modal.msgError(res.msg || "学习失败，请先填写政策详细内容");
      }
    });
  }).catch(() => {});
}

getList();
</script>

<style scoped>
.glass-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
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
.glass-card::before { content: ""; position: absolute; top: 0; left: 0; width: 100%; height: 5px; }
.glass-card:hover { transform: translateY(-5px); box-shadow: 0 12px 40px 0 rgba(31, 38, 135, 0.1); }
.stat-primary::before { background: linear-gradient(90deg, #1890ff, #36cfc9); }
.stat-success::before { background: linear-gradient(90deg, #52c41a, #bae637); }
.stat-warning::before { background: linear-gradient(90deg, #fa8c16, #ffd666); }
.card-icon { font-size: 24px; width: 44px; height: 44px; border-radius: 10px; display: flex; align-items: center; justify-content: center; margin-bottom: 15px; }
.stat-primary .card-icon { background: rgba(24, 144, 255, 0.1); color: #1890ff; }
.stat-success .card-icon { background: rgba(82, 196, 26, 0.1); color: #52c41a; }
.stat-warning .card-icon { background: rgba(250, 140, 22, 0.1); color: #fa8c16; }
.card-info { display: flex; flex-direction: column; margin-bottom: 10px; }
.card-label { font-size: 13px; color: #8c8c8c; margin-bottom: 5px; }
.card-value { font-size: 28px; font-weight: bold; color: #262626; font-family: 'Outfit', sans-serif; }
.card-value .unit { font-size: 14px; font-weight: normal; color: #8c8c8c; margin-left: 5px; }
.card-bottom { font-size: 12px; color: #8c8c8c; }
.card-bottom .trend { color: #52c41a; font-weight: bold; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.header-title { font-size: 16px; font-weight: bold; color: #303133; display: flex; align-items: center; gap: 8px; }
.money-badge { background: linear-gradient(135deg, #fff3e0, #ffe0b2); color: #e65100; font-weight: bold; padding: 4px 10px; border-radius: 10px; border: 1px solid #ffcc80; font-family: 'Consolas', monospace; font-size: 13px; }
.mt20 { margin-top: 20px; }
.mb20 { margin-bottom: 20px; }
</style>