<template>
  <div class="data-screen">
    <div class="screen-header">
      <div class="header-decor"></div>
      <h1>免申即享 · 政务数字化改革数据大屏</h1>
      <div class="header-time">{{ currentTime }}</div>
    </div>
    <div class="screen-body">
      <div class="screen-col left">
        <div class="screen-card">
          <div class="card-title"><span class="dot blue"></span>政策匹配状态分布</div>
          <div class="chart-box" ref="statusChartRef"></div>
        </div>
        <div class="screen-card">
          <div class="card-title"><span class="dot green"></span>资金池消耗对比</div>
          <div class="chart-box" ref="fundChartRef"></div>
        </div>
        <div class="screen-card">
          <div class="card-title"><span class="dot purple"></span>风控等级分布</div>
          <div class="chart-box" ref="riskChartRef"></div>
        </div>
      </div>
      <div class="screen-col center">
        <div class="center-top">
          <div class="big-num" v-for="m in centerMetrics" :key="m.key">
            <div class="bn-value" :style="{color:m.color}">{{ m.value }}</div>
            <div class="bn-label">{{ m.label }}</div>
          </div>
        </div>
        <div class="screen-card flex-1">
          <div class="card-title"><span class="dot cyan"></span>五步闭环流转管道</div>
          <div class="pipeline">
            <div class="pipe-node" v-for="(step,i) in pipelineSteps" :key="step.status">
              <div class="pn-circle" :style="{background:step.color,border:step.count>0?'none':'2px solid rgba(255,255,255,0.15)'}">
                <span class="pn-num">{{ step.count }}</span>
              </div>
              <div class="pn-name">{{ step.name }}</div>
              <div v-if="i < pipelineSteps.length-1" class="pn-arrow">
                <div class="arrow-line"></div>
                <div class="arrow-dot" :style="{animationDelay: i*0.3+'s'}"></div>
              </div>
            </div>
          </div>
        </div>
        <div class="screen-card">
          <div class="card-title"><span class="dot orange"></span>政策类型资金分布</div>
          <div class="chart-box" ref="typeChartRef"></div>
        </div>
      </div>
      <div class="screen-col right">
        <div class="screen-card">
          <div class="card-title"><span class="dot red"></span>免审率与匹配率</div>
          <div class="gauge-row">
            <div class="gauge-box" ref="gaugeExemptRef"></div>
            <div class="gauge-box" ref="gaugeMatchRef"></div>
          </div>
        </div>
        <div class="screen-card">
          <div class="card-title"><span class="dot yellow"></span>目标对象类型分布</div>
          <div class="chart-box" ref="targetChartRef"></div>
        </div>
        <div class="screen-card">
          <div class="card-title"><span class="dot pink"></span>近期匹配记录</div>
          <div class="record-scroll">
            <div class="record-row" v-for="r in recentRecords" :key="r.recordId">
              <span class="rr-name">{{ r.targetName }}</span>
              <span class="rr-policy">{{ r.policyName }}</span>
              <span class="rr-amount">{{ formatMoney(r.fundAmount) }}元</span>
              <span :class="['rr-status','st-'+r.status.toLowerCase()]">{{ statusLabel(r.status) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="DataScreen">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { listEePolicy } from '@/api/biz/policy'
import { listEeRule } from '@/api/biz/rule'
import { listEeTargetData } from '@/api/biz/targetData'
import { listEeFund } from '@/api/biz/fund'
import { listEeMatchRecord } from '@/api/biz/matchRecord'

const currentTime = ref('')
const policies = ref([])
const rules = ref([])
const targets = ref([])
const funds = ref([])
const records = ref([])

const statusChartRef = ref(null)
const fundChartRef = ref(null)
const riskChartRef = ref(null)
const typeChartRef = ref(null)
const targetChartRef = ref(null)
const gaugeExemptRef = ref(null)
const gaugeMatchRef = ref(null)

let charts = []
let timer = null

const centerMetrics = ref([])
const pipelineSteps = ref([])
const recentRecords = ref([])

function formatMoney(v) { if(!v) return '0'; return Number(v).toLocaleString('zh-CN',{minimumFractionDigits:0,maximumFractionDigits:0}) }
function statusLabel(s) { return {MATCHED:'已匹配',PUSHED:'已推送',CONFIRMED:'已确认',FULFILLED:'已兑付',ARCHIVED:'已归档'}[s]||s }

function updateTime() {
  const d = new Date()
  currentTime.value = d.getFullYear()+'-'+String(d.getMonth()+1).padStart(2,'0')+'-'+String(d.getDate()).padStart(2,'0')+' '+String(d.getHours()).padStart(2,'0')+':'+String(d.getMinutes()).padStart(2,'0')+':'+String(d.getSeconds()).padStart(2,'0')
}

async function loadData() {
  const [pRes, rRes, tRes, fRes, mRes] = await Promise.all([
    listEePolicy(), listEeRule(), listEeTargetData(), listEeFund(), listEeMatchRecord()
  ])
  policies.value = pRes.rows || []
  rules.value = rRes.rows || []
  targets.value = tRes.rows || []
  funds.value = fRes.rows || []
  records.value = mRes.rows || []
  renderAll()
}

function renderAll() {
  const activePolicies = policies.value.filter(p => p.status === '0')
  const activeRules = rules.value.filter(r => r.status === '0')
  const activeTargets = targets.value.filter(t => t.status === '0')
  const fulfilled = records.value.filter(r => r.status === 'FULFILLED' || r.status === 'ARCHIVED')
  const fulfilledAmount = fulfilled.reduce((s,r) => s + Number(r.fundAmount||0), 0)

  centerMetrics.value = [
    { key:'policy', label:'生效政策', value: activePolicies.length, color:'#3b82f6' },
    { key:'rule', label:'匹配规则', value: activeRules.length, color:'#06b6d4' },
    { key:'target', label:'目标对象', value: activeTargets.length, color:'#10b981' },
    { key:'record', label:'匹配记录', value: records.value.length, color:'#f59e0b' },
    { key:'amount', label:'已兑付(万)', value: (fulfilledAmount/10000).toFixed(1), color:'#ef4444' },
  ]

  const statusCounts = {MATCHED:0,PUSHED:0,CONFIRMED:0,FULFILLED:0,ARCHIVED:0}
  records.value.forEach(r => { if(statusCounts[r.status]!==undefined) statusCounts[r.status]++ })

  pipelineSteps.value = [
    { name:'系统比对', status:'MATCHED', color:'#3b82f6', count: statusCounts.MATCHED },
    { name:'精准推送', status:'PUSHED', color:'#06b6d4', count: statusCounts.PUSHED },
    { name:'意愿确认', status:'CONFIRMED', color:'#f59e0b', count: statusCounts.CONFIRMED },
    { name:'自动兑付', status:'FULFILLED', color:'#10b981', count: statusCounts.FULFILLED },
    { name:'公示归档', status:'ARCHIVED', color:'#8b5cf6', count: statusCounts.ARCHIVED },
  ]

  recentRecords.value = records.value.slice(0, 8)

  nextTick(() => {
    renderStatusChart(statusCounts)
    renderFundChart()
    renderRiskChart()
    renderTypeChart(activePolicies)
    renderTargetChart(activeTargets)
    renderGauges()
  })
}

function renderStatusChart(statusCounts) {
  if(!statusChartRef.value) return
  const chart = echarts.init(statusChartRef.value)
  charts.push(chart)
  chart.setOption({
    backgroundColor:'transparent',
    tooltip:{trigger:'item',backgroundColor:'rgba(0,0,0,0.8)',borderColor:'transparent',textStyle:{color:'#fff'}},
    series:[{type:'pie',radius:['45%','70%'],center:['50%','50%'],
      label:{color:'#94a3b8',fontSize:11},
      labelLine:{lineStyle:{color:'#334155'}},
      data:[
        {value:statusCounts.MATCHED,name:'已匹配',itemStyle:{color:'#3b82f6'}},
        {value:statusCounts.PUSHED,name:'已推送',itemStyle:{color:'#06b6d4'}},
        {value:statusCounts.CONFIRMED,name:'已确认',itemStyle:{color:'#f59e0b'}},
        {value:statusCounts.FULFILLED,name:'已兑付',itemStyle:{color:'#10b981'}},
        {value:statusCounts.ARCHIVED,name:'已归档',itemStyle:{color:'#8b5cf6'}},
      ]
    }]
  })
}

function renderFundChart() {
  if(!fundChartRef.value) return
  const chart = echarts.init(fundChartRef.value)
  charts.push(chart)
  const names = funds.value.map(f => (f.policyName||'').substring(0,6))
  const used = funds.value.map(f => Number(f.usedAmount||0)/10000)
  const total = funds.value.map(f => Number(f.totalBudget||0)/10000)
  chart.setOption({
    backgroundColor:'transparent',
    tooltip:{trigger:'axis',backgroundColor:'rgba(0,0,0,0.8)',borderColor:'transparent',textStyle:{color:'#fff'}},
    grid:{left:60,right:20,top:20,bottom:30},
    xAxis:{type:'category',data:names,axisLabel:{color:'#64748b',fontSize:10},axisLine:{lineStyle:{color:'#cbd5e1'}}},
    yAxis:{type:'value',axisLabel:{color:'#64748b',fontSize:10},splitLine:{lineStyle:{color:'#e2e8f0'}},axisLine:{lineStyle:{color:'#cbd5e1'}}},
    series:[
      {name:'已拨付',type:'bar',stack:'total',data:used,itemStyle:{color:'#3b82f6',borderRadius:[0,0,0,0]}},
      {name:'剩余',type:'bar',stack:'total',data:total.map((t,i)=>Math.max(0,t-used[i])),itemStyle:{color:'rgba(59,130,246,0.2)',borderRadius:[4,4,0,0]}}
    ]
  })
}

function renderRiskChart() {
  if(!riskChartRef.value) return
  const chart = echarts.init(riskChartRef.value)
  charts.push(chart)
  const riskCounts = {'0':0,'1':0,'2':0}
  records.value.forEach(r => { if(riskCounts[r.riskLevel]!==undefined) riskCounts[r.riskLevel]++ })
  chart.setOption({
    backgroundColor:'transparent',
    tooltip:{trigger:'item',backgroundColor:'rgba(0,0,0,0.8)',borderColor:'transparent',textStyle:{color:'#fff'}},
    series:[{type:'pie',radius:['30%','65%'],center:['50%','50%'],roseType:'area',
      label:{color:'#94a3b8',fontSize:11},
      labelLine:{lineStyle:{color:'#334155'}},
      data:[
        {value:riskCounts['0'],name:'低风险·免审',itemStyle:{color:'#10b981'}},
        {value:riskCounts['1'],name:'中风险·审核',itemStyle:{color:'#f59e0b'}},
        {value:riskCounts['2'],name:'高风险·拦截',itemStyle:{color:'#ef4444'}},
      ]
    }]
  })
}

function renderTypeChart(activePolicies) {
  if(!typeChartRef.value) return
  const chart = echarts.init(typeChartRef.value)
  charts.push(chart)
  const typeData = {}
  activePolicies.forEach(p => {
    const t = {'1':'补贴','2':'奖励','3':'减免'}[p.policyType]||'其他'
    if(!typeData[t]) typeData[t] = {count:0,amount:0}
    typeData[t].count++
    typeData[t].amount += Number(p.amount||0)
  })
  chart.setOption({
    backgroundColor:'transparent',
    tooltip:{trigger:'axis',backgroundColor:'rgba(0,0,0,0.8)',borderColor:'transparent',textStyle:{color:'#fff'}},
    grid:{left:60,right:40,top:20,bottom:30},
    xAxis:{type:'category',data:Object.keys(typeData),axisLabel:{color:'#64748b'},axisLine:{lineStyle:{color:'#cbd5e1'}}},
    yAxis:[{type:'value',axisLabel:{color:'#64748b'},splitLine:{lineStyle:{color:'#e2e8f0'}},axisLine:{lineStyle:{color:'#cbd5e1'}}},{type:'value',axisLabel:{color:'#64748b'},splitLine:{show:false},axisLine:{lineStyle:{color:'#cbd5e1'}}}],
    series:[
      {name:'政策数',type:'bar',data:Object.values(typeData).map(v=>v.count),itemStyle:{color:'#3b82f6',borderRadius:[4,4,0,0]},barWidth:30},
      {name:'资金(万)',type:'line',yAxisIndex:1,data:Object.values(typeData).map(v=>(v.amount/10000).toFixed(0)),itemStyle:{color:'#f59e0b'},lineStyle:{color:'#f59e0b'},symbol:'circle',symbolSize:8}
    ]
  })
}

function renderTargetChart(activeTargets) {
  if(!targetChartRef.value) return
  const chart = echarts.init(targetChartRef.value)
  charts.push(chart)
  const entCount = activeTargets.filter(t => t.targetType==='1').length
  const perCount = activeTargets.filter(t => t.targetType==='2').length
  chart.setOption({
    backgroundColor:'transparent',
    tooltip:{trigger:'item',backgroundColor:'rgba(0,0,0,0.8)',borderColor:'transparent',textStyle:{color:'#fff'}},
    series:[{type:'pie',radius:['45%','70%'],center:['50%','50%'],
      label:{color:'#94a3b8',fontSize:11},
      labelLine:{lineStyle:{color:'#334155'}},
      data:[
        {value:entCount,name:'企业('+entCount+')',itemStyle:{color:'#3b82f6'}},
        {value:perCount,name:'个人('+perCount+')',itemStyle:{color:'#a855f7'}},
      ]
    }]
  })
}

function renderGauges() {
  if(gaugeExemptRef.value) {
    const chart = echarts.init(gaugeExemptRef.value)
    charts.push(chart)
    const total = records.value.length || 1
    const exemptRate = Math.round(records.value.filter(r=>r.riskLevel==='0').length / total * 100)
    chart.setOption({
      backgroundColor:'transparent',
      series:[{type:'gauge',startAngle:200,endAngle:-20,min:0,max:100,
        pointer:{show:true,length:'60%',width:4,itemStyle:{color:'#10b981'}},
        progress:{show:true,width:12,itemStyle:{color:'#10b981'}},
        axisLine:{lineStyle:{width:12,color:[[1,'#e2e8f0']]}},
        axisTick:{show:false},
        splitLine:{show:false},
        axisLabel:{show:false},
        detail:{valueAnimation:true,formatter:'{value}%',color:'#10b981',fontSize:20,offsetCenter:[0,'70%']},
        title:{show:true,offsetCenter:[0,'95%'],color:'#64748b',fontSize:12},
        data:[{value:exemptRate,name:'免审率'}]
      }]
    })
  }
  if(gaugeMatchRef.value) {
    const chart = echarts.init(gaugeMatchRef.value)
    charts.push(chart)
    const total = records.value.length || 1
    const matchRate = Math.round(records.value.filter(r=>r.status!=='MATCHED'||r.auditStatus==='9').length / total * 100)
    chart.setOption({
      backgroundColor:'transparent',
      series:[{type:'gauge',startAngle:200,endAngle:-20,min:0,max:100,
        pointer:{show:true,length:'60%',width:4,itemStyle:{color:'#3b82f6'}},
        progress:{show:true,width:12,itemStyle:{color:'#3b82f6'}},
        axisLine:{lineStyle:{width:12,color:[[1,'#e2e8f0']]}},
        axisTick:{show:false},
        splitLine:{show:false},
        axisLabel:{show:false},
        detail:{valueAnimation:true,formatter:'{value}%',color:'#3b82f6',fontSize:20,offsetCenter:[0,'70%']},
        title:{show:true,offsetCenter:[0,'95%'],color:'#64748b',fontSize:12},
        data:[{value:matchRate,name:'处理率'}]
      }]
    })
  }
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
  loadData()
  setInterval(loadData, 30000)
  window.addEventListener('resize', () => charts.forEach(c => c.resize()))
})

onUnmounted(() => {
  if(timer) clearInterval(timer)
  charts.forEach(c => c.dispose())
})
</script>

<style scoped>
.data-screen {
  background: #f5f7fa;
  min-height: 100vh;
  color: #1e293b;
  padding: 0;
  overflow: hidden;
}
.screen-header {
  position: relative;
  text-align: center;
  padding: 16px 0 12px;
  background: linear-gradient(180deg, rgba(59,130,246,0.15) 0%, transparent 100%);
  border-bottom: 1px solid rgba(59,130,246,0.2);
}
.screen-header h1 {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 6px;
  background: linear-gradient(90deg, #60a5fa, #a78bfa, #60a5fa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0;
}
.header-time {
  position: absolute;
  right: 24px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 14px;
  color: #64748b;
  font-family: 'Consolas', monospace;
}
.header-decor {
  position: absolute;
  left: 24px;
  top: 50%;
  transform: translateY(-50%);
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #3b82f6;
  box-shadow: 0 0 12px #3b82f6;
  animation: pulse-dot 2s infinite;
}
@keyframes pulse-dot { 0%,100%{opacity:1} 50%{opacity:0.4} }

.screen-body {
  display: flex;
  gap: 12px;
  padding: 12px;
  height: calc(100vh - 60px);
}
.screen-col {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.screen-col.left { width: 28%; }
.screen-col.center { width: 44%; }
.screen-col.right { width: 28%; }

.screen-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 14px;
  flex: 1;
  min-height: 0;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.screen-card.flex-1 { flex: 1.5; }
.card-title {
  font-size: 13px;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}
.dot.blue { background: #3b82f6; box-shadow: 0 0 6px #3b82f6; }
.dot.green { background: #10b981; box-shadow: 0 0 6px #10b981; }
.dot.purple { background: #8b5cf6; box-shadow: 0 0 6px #8b5cf6; }
.dot.cyan { background: #06b6d4; box-shadow: 0 0 6px #06b6d4; }
.dot.orange { background: #f59e0b; box-shadow: 0 0 6px #f59e0b; }
.dot.red { background: #ef4444; box-shadow: 0 0 6px #ef4444; }
.dot.yellow { background: #eab308; box-shadow: 0 0 6px #eab308; }
.dot.pink { background: #ec4899; box-shadow: 0 0 6px #ec4899; }

.chart-box { width: 100%; height: calc(100% - 30px); min-height: 120px; }

.center-top {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.big-num {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  border-right: 1px solid #e2e8f0;
}
.big-num:last-child { border-right: none; }
.bn-value {
  font-size: 28px;
  font-weight: 700;
  font-family: 'Outfit', sans-serif;
  line-height: 1.2;
}
.bn-label { font-size: 11px; color: #64748b; margin-top: 4px; }

.pipeline {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px 10px;
  gap: 0;
}
.pipe-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}
.pn-circle {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}
.pn-num {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  font-family: 'Outfit', sans-serif;
}
.pn-name { font-size: 11px; color: #94a3b8; }
.pn-arrow {
  position: absolute;
  right: -20px;
  top: 28px;
  width: 40px;
  display: flex;
  align-items: center;
}
.arrow-line {
  position: absolute;
  width: 30px;
  height: 2px;
  background: #e2e8f0;
}
.arrow-dot {
  position: absolute;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #3b82f6;
  box-shadow: 0 0 6px #3b82f6;
  animation: flowDot 1.5s ease-in-out infinite;
}
@keyframes flowDot { 0%{left:0;opacity:0} 20%{opacity:1} 80%{opacity:1} 100%{left:26px;opacity:0} }

.gauge-row { display: flex; height: calc(100% - 30px); }
.gauge-box { flex: 1; min-height: 120px; }

.record-scroll {
  overflow-y: auto;
  height: calc(100% - 30px);
  max-height: 200px;
}
.record-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  border-bottom: 1px solid #e2e8f0;
  font-size: 12px;
}
.rr-name { color: #0f172a; width: 60px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.rr-policy { color: #94a3b8; flex: 1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.rr-amount { color: #f59e0b; font-family: Consolas, monospace; font-weight: 600; }
.rr-status { font-size: 10px; padding: 2px 6px; border-radius: 4px; }
.st-matched { background: rgba(59,130,246,0.2); color: #60a5fa; }
.st-pushed { background: rgba(6,182,212,0.2); color: #22d3ee; }
.st-confirmed { background: rgba(245,158,11,0.2); color: #fbbf24; }
.st-fulfilled { background: rgba(16,185,129,0.2); color: #34d399; }
.st-archived { background: rgba(139,92,246,0.2); color: #a78bfa; }
</style>