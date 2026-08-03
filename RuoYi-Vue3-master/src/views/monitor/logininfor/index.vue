<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="登录地址" prop="ipaddr">
        <el-input v-model="queryParams.ipaddr" placeholder="请输入登录地址" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="用户名称" prop="userName">
        <el-input v-model="queryParams.userName" placeholder="请输入用户名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="登录状态" clearable>
          <el-option v-for="dict in sys_common_status" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="登录时间">
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

    <el-table v-loading="loading" :data="logininforList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="访问编号" align="center" prop="infoId" />
      <el-table-column label="用户名称" align="center" prop="userName" />
      <el-table-column label="登录地址" align="center" prop="ipaddr" width="130" />
      <el-table-column label="登录地点" align="center" prop="loginLocation" />
      <el-table-column label="浏览器" align="center" prop="browser" />
      <el-table-column label="操作系统" align="center" prop="os" />
      <el-table-column label="登录状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :options="sys_common_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作信息" align="center" prop="msg" show-overflow-tooltip />
      <el-table-column label="登录日期" align="center" prop="loginTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.loginTime) }}</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="Logininfor">
import { list, delLogininfor, cleanLogininfor } from "@/api/monitor/logininfor"
import { parseTime } from '@/utils/ruoyi'

const { proxy } = getCurrentInstance()
const { sys_common_status } = proxy.useDict("sys_common_status")

const logininforList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const dateRange = ref([])

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    ipaddr: null,
    userName: null,
    status: null
  }
})

const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  list(proxy.addDateRange(queryParams.value, dateRange.value)).then(res => {
    logininforList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { dateRange.value = []; proxy.resetForm("queryRef"); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.infoId); multiple.value = !selection.length }
function handleDelete() { proxy.$modal.confirm('确认删除所选登录日志吗？').then(() => delLogininfor(ids.value).then(() => { getList(); proxy.$modal.msgSuccess("删除成功") })) }
function handleClean() { proxy.$modal.confirm('确认清空所有登录日志吗？').then(() => cleanLogininfor().then(() => { getList(); proxy.$modal.msgSuccess("清空成功") })) }
function handleExport() { proxy.download("monitor/logininfor/export", { ...queryParams.value }, `logininfor_${new Date().getTime()}.xlsx`) }
getList()
</script>