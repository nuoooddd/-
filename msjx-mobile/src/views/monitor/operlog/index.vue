<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="系统模块" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入系统模块" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="操作人员" prop="operName">
        <el-input v-model="queryParams.operName" placeholder="请输入操作人员" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="类型" prop="businessType">
        <el-select v-model="queryParams.businessType" placeholder="操作类型" clearable>
          <el-option v-for="dict in typeOptions" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="操作状态" clearable>
          <el-option v-for="dict in sys_common_status" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作时间">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" @click="handleClean">清空</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="operlogList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="日志编号" align="center" prop="operId" />
      <el-table-column label="系统模块" align="center" prop="title" show-overflow-tooltip />
      <el-table-column label="操作类型" align="center" prop="businessType">
        <template #default="scope">
          <dict-tag :options="typeOptions" :value="scope.row.businessType" />
        </template>
      </el-table-column>
      <el-table-column label="操作人员" align="center" prop="operName" width="100" />
      <el-table-column label="主机" align="center" prop="operIp" width="130" />
      <el-table-column label="操作状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :options="sys_common_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作日期" align="center" prop="operTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.operTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="100">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详细</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="操作日志详细" v-model="open" width="700px" append-to-body>
      <el-form :model="form" label-width="100px">
        <el-row>
          <el-col :span="12"><el-form-item label="操作模块">{{ form.title }} / {{ form.businessType | typeFormat }}</el-form-item></el-col>
          <el-col :span="12"><el-form-item label="请求方式">{{ form.requestMethod }}</el-form-item></el-col>
          <el-col :span="12"><el-form-item label="请求地址">{{ form.operUrl }}</el-form-item></el-col>
          <el-col :span="12"><el-form-item label="操作方法">{{ form.method }}</el-form-item></el-col>
          <el-col :span="12"><el-form-item label="操作人员">{{ form.operName }}</el-form-item></el-col>
          <el-col :span="12"><el-form-item label="操作IP">{{ form.operIp }} / {{ form.operLocation }}</el-form-item></el-col>
          <el-col :span="12"><el-form-item label="操作时间">{{ parseTime(form.operTime) }}</el-form-item></el-col>
          <el-col :span="12"><el-form-item label="消耗时间">{{ form.costTime }}毫秒</el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态">
            <dict-tag :options="sys_common_status" :value="form.status" />
          </el-form-item></el-col>
          <el-col :span="24"><el-form-item label="请求参数">{{ form.operParam }}</el-form-item></el-col>
          <el-col :span="24"><el-form-item label="返回参数">{{ form.jsonResult }}</el-form-item></el-col>
          <el-col :span="24"><el-form-item label="错误信息">{{ form.errorMsg }}</el-form-item></el-col>
        </el-row>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup name="Operlog">
import { list, delOperlog, cleanOperlog } from "@/api/monitor/operLog"
import { parseTime } from '@/utils/ruoyi'

const { proxy } = getCurrentInstance()
const { sys_common_status } = proxy.useDict("sys_common_status")

const operlogList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const dateRange = ref([])
const typeOptions = ref([
  { value: "0", label: "其它" },
  { value: "1", label: "新增" },
  { value: "2", label: "修改" },
  { value: "3", label: "删除" },
  { value: "4", label: "授权" },
  { value: "5", label: "导出" },
  { value: "6", label: "导入" },
  { value: "7", label: "强退" },
  { value: "8", label: "生成代码" },
  { value: "9", label: "清空数据" }
])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    title: null,
    operName: null,
    businessType: null,
    status: null
  }
})

const { queryParams, form } = toRefs(data)

function getList() {
  loading.value = true
  list(proxy.addDateRange(queryParams.value, dateRange.value)).then(res => {
    operlogList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { dateRange.value = []; proxy.resetForm("queryRef"); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.operId); multiple.value = !selection.length }
function handleDetail(row) { form.value = row; open.value = true }
function handleDelete() { proxy.$modal.confirm('确认删除所选操作日志吗？').then(() => delOperlog(ids.value).then(() => { getList(); proxy.$modal.msgSuccess("删除成功") })) }
function handleClean() { proxy.$modal.confirm('确认清空所有操作日志吗？').then(() => cleanOperlog().then(() => { getList(); proxy.$modal.msgSuccess("清空成功") })) }
function handleExport() { proxy.download("monitor/operlog/export", { ...queryParams.value }, `operlog_${new Date().getTime()}.xlsx`) }
getList()
</script>