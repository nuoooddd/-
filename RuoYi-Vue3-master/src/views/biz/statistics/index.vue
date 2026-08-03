<template>
  <div class="app-container" style="background: #f6f8fb; min-height: calc(100vh - 84px); padding: 20px;">
    <div class="stat-header">
      <h2>数据统计分析</h2>
      <p>政策趋势 · 匹配率 · 资金流向 · 多维对比</p>
    </div>

    <el-row :gutter="20" class="mb20">
      <el-col :span="12">
        <el-card style="border-radius: 12px; box-shadow: 0 4px 12px 0 rgba(0,0,0,0.05);">
          <template #header>
            <span class="header-title"><el-icon><TrendCharts /></el-icon> 匹配状态分布</span>
          </template>
          <div ref="statusChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card style="border-radius: 12px; box-shadow: 0 4px 12px 0 rgba(0,0,0,0.05);">
          <template #header>
            <span class="header-title"><el-icon><PieChart /></el-icon> 风控等级分布</span>
          </template>
          <div ref="riskChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mb20">
      <el-col :span="12">
        <el-card style="border-radius: 12px; box-shadow: 0 4px 12px 0 rgba(0,0,0,0.05);">
          <template #header>
            <span class="header-title"><el-icon><Wallet /></el-icon> 资金池消耗对比</span>
          </template>
          <div ref="fundChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card style="border-radius: 12px; box-shadow: 0 4px 12px 0 rgba(0,0,0,0.05);">
          <template #header>
            <span class="header-title"><el-icon><Histogram /></el-icon> 政策类型统计</span>
          </template>
          <div ref="policyTypeChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card style="border-radius: 12px; box-shadow: 0 4px 12px 0 rgba(0,0,0,0.05);">
          <template #header>
            <span class="header-title"><el-icon><DataLine /></el-icon> 目标对象类型分布</span>
          </template>
          <div ref="targetTypeChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card style="border-radius: 12px; box-shadow: 0 4px 12px 0 rgba(0,0,0,0.05);">
          <template #header>
            <span class="header-title"><el-icon><Gauge /></el-icon> 免审率与匹配率</span>
          </template>
          <div ref="gaugeChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="EeStatistics">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { listEePolicy } from '@/api/biz/policy'
import { listEeRule } from '@/api/biz/rule'
import { listEeTargetData } from '@/api/biz/targetData'
import { listEeFund } from '@/api/biz/fund'
import { listEeMatchRecord } from '@/api/biz/matchRecord'

const statusChartRef = ref(null)
const riskChartRef = ref(null)
const fundChartRef = ref(null)
const policyTypeChartRef = ref(null)
const targetTypeChartRef = ref(null)
const gaugeChartRef = ref(null)

let charts = []
let refreshTimer = null
let chartInstances = {}

function getChart(key, domRef) {
  if (!chartInstances[key]) {
    chartInstances[key] = echarts.init(domRef)
    charts.push(chartInstances[key])
  }
  return chartInstances[key]
}

function updateChart(key, domRef, option) {
  const chart = getChart(key, domRef)
  chart.setOption(option, true)
}

function loadData() {
  const largePage = { pageNum: 1, pageSize: 9999 }
  Promise.all([
    listEePolicy(largePage),
    listEeRule(largePage),
    listEeTargetData(largePage),
    listEeFund(largePage),
    listEeMatchRecord(largePage)
  ]).then(([pRes, rRes, tRes, fRes, mRes]) => {
    const policies = pRes.rows || []
    const rules = rRes.rows || []
    const targets = tRes.rows || []
    const funds = fRes.rows || []
    const records = mRes.rows || []

    nextTick(() => {
      renderStatusChart(records)
      renderRiskChart(records)
      renderFundChart(funds)
      renderPolicyTypeChart(policies)
      renderTargetTypeChart(targets)
      renderGaugeChart(records, targets)
    })
  })
}

function renderStatusChart(records) {
  const statusMap = { MATCHED: '已匹配', PUSHED: '已推送', CONFIRMED: '意愿确认', FULFILLED: '已兑付', ARCHIVED: '已归档' }
  const colorMap = { MATCHED: '#1890ff', PUSHED: '#36cfc9', CONFIRMED: '#fa8c16', FULFILLED: '#52c41a', ARCHIVED: '#722ed1' }
  const data = Object.entries(statusMap).map(([k, v]) => ({
    name: v, value: records.filter(r => r.status === k).length, itemStyle: { color: colorMap[k] }
  }))
  updateChart('status', statusChartRef.value, {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, textStyle: { fontSize: 12 } },
    series: [{ type: 'pie', radius: ['40%', '70%'], center: ['50%', '45%'], data, label: { formatter: '{b}\n{c}笔' }, emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.5)' } } }]
  })
}

function renderRiskChart(records) {
  const riskMap = { '0': '低风险', '1': '中风险', '2': '高风险' }
  const colorMap = { '0': '#52c41a', '1': '#e6a23c', '2': '#f56c6c' }
  const data = Object.entries(riskMap).map(([k, v]) => ({
    name: v, value: records.filter(r => r.riskLevel === k).length, itemStyle: { color: colorMap[k] }
  }))
  updateChart('risk', riskChartRef.value, {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0 },
    series: [{ type: 'pie', radius: ['40%', '70%'], center: ['50%', '45%'], data, label: { formatter: '{b}\n{c}笔' }, roseType: 'radius' }]
  })
}

function renderFundChart(funds) {
  const names = funds.map(f => f.policyName || '政策#' + f.policyId)
  const used = funds.map(f => Number(f.usedAmount || 0))
  const remain = funds.map(f => Math.max(0, Number(f.totalBudget || 0) - Number(f.usedAmount || 0)))
  updateChart('fund', fundChartRef.value, {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['已拨付', '剩余额度'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
    xAxis: { type: 'category', data: names, axisLabel: { rotate: 15, fontSize: 11 } },
    yAxis: { type: 'value', name: '金额(元)' },
    series: [
      { name: '已拨付', type: 'bar', stack: 'total', data: used, itemStyle: { color: '#1890ff' } },
      { name: '剩余额度', type: 'bar', stack: 'total', data: remain, itemStyle: { color: '#e8e8e8' } }
    ]
  })
}

function renderPolicyTypeChart(policies) {
  const typeMap = { '1': '补贴', '2': '奖励', '3': '减免' }
  const colorMap = { '1': '#1890ff', '2': '#52c41a', '3': '#e6a23c' }
  const data = Object.entries(typeMap).map(([k, v]) => ({
    name: v, value: policies.filter(p => p.policyType === k).length, itemStyle: { color: colorMap[k] }
  }))
  updateChart('policyType', policyTypeChartRef.value, {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true },
    xAxis: { type: 'category', data: data.map(d => d.name) },
    yAxis: { type: 'value', name: '数量' },
    series: [{ type: 'bar', data: data.map(d => ({ value: d.value, itemStyle: d.itemStyle })), barWidth: 50, label: { show: true, position: 'top' } }]
  })
}

function renderTargetTypeChart(targets) {
  const typeMap = { '1': '企业', '2': '个人' }
  const data = Object.entries(typeMap).map(([k, v]) => ({
    name: v, value: targets.filter(t => t.targetType === k).length
  }))
  updateChart('targetType', targetTypeChartRef.value, {
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{ type: 'pie', radius: '65%', center: ['50%', '45%'], data, label: { formatter: '{b}\n{c}个' }, itemStyle: { borderRadius: 8 } }]
  })
}

function renderGaugeChart(records, targets) {
  const exemptRate = records.length > 0 ? Math.round(records.filter(r => r.riskLevel === '0').length / records.length * 100) : 0
  const uniqueMatchedTargets = new Set(records.map(r => r.targetId)).size
  const matchRate = targets.length > 0 ? Math.round(uniqueMatchedTargets / targets.length * 100) : 0
  updateChart('gauge', gaugeChartRef.value, {
    series: [
      { type: 'gauge', center: ['30%', '55%'], radius: '65%', startAngle: 200, endAngle: -20, min: 0, max: 100,
        axisLine: { lineStyle: { width: 15, color: [[0.3, '#f56c6c'], [0.7, '#e6a23c'], [1, '#52c41a']] } },
        pointer: { width: 4 }, axisTick: { show: false }, splitLine: { length: 10, lineStyle: { width: 2 } },
        axisLabel: { distance: 15, fontSize: 10 }, detail: { offsetCenter: [0, '70%'], formatter: '{value}%', fontSize: 18, color: '#52c41a' },
        title: { offsetCenter: [0, '90%'], fontSize: 13 }, data: [{ value: exemptRate, name: '免审率' }] },
      { type: 'gauge', center: ['70%', '55%'], radius: '65%', startAngle: 200, endAngle: -20, min: 0, max: 100,
        axisLine: { lineStyle: { width: 15, color: [[0.3, '#c0c4cc'], [0.7, '#1890ff'], [1, '#722ed1']] } },
        pointer: { width: 4 }, axisTick: { show: false }, splitLine: { length: 10, lineStyle: { width: 2 } },
        axisLabel: { distance: 15, fontSize: 10 }, detail: { offsetCenter: [0, '70%'], formatter: '{value}%', fontSize: 18, color: '#1890ff' },
        title: { offsetCenter: [0, '90%'], fontSize: 13 }, data: [{ value: matchRate, name: '匹配率' }] }
    ]
  })
}

function handleResize() { charts.forEach(c => c && c.resize()) }

onMounted(() => {
  loadData()
  refreshTimer = setInterval(() => { loadData() }, 30000)
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  clearInterval(refreshTimer)
  Object.keys(chartInstances).forEach(k => chartInstances[k] && chartInstances[k].dispose())
  charts = []
  chartInstances = {}
})
</script>

<style scoped>
.stat-header { text-align: center; margin-bottom: 24px; }
.stat-header h2 { font-size: 24px; font-weight: bold; background: linear-gradient(90deg, #1890ff, #722ed1); -webkit-background-clip: text; -webkit-text-fill-color: transparent; margin: 0 0 8px 0; }
.stat-header p { font-size: 14px; color: #8c8c8c; margin: 0; }
.header-title { font-size: 16px; font-weight: bold; color: #303133; display: flex; align-items: center; gap: 8px; }
.mb20 { margin-bottom: 20px; }
</style>