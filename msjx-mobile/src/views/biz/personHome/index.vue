<template>
  <div class="ph-home">
    <div class="ph-hero">
      <div class="hero-wave">
        <svg viewBox="0 0 1440 120" preserveAspectRatio="none"><path d="M0,60 C360,120 720,0 1080,60 C1260,90 1380,40 1440,60 L1440,120 L0,120 Z" fill="rgba(255,255,255,0.06)"/></svg>
      </div>
      <div class="hero-inner">
        <div class="hero-avatar">
          <el-icon :size="48"><User /></el-icon>
        </div>
        <div class="hero-text">
          <h1>{{ timeGreeting }}，<span class="hl">{{ nickName }}</span></h1>
          <p>个人惠民政策智能匹配 · 补贴直达 · 免申即享</p>
        </div>
        <div class="hero-stats">
          <div class="hs-item" v-for="m in heroMetrics" :key="m.key">
            <div class="hs-val" :style="{color:m.color}">{{ m.value }}</div>
            <div class="hs-label">{{ m.label }}</div>
          </div>
        </div>
      </div>
    </div>

    <div class="ph-body">
          <div class="sec-card">
            <div class="sec-head"><h3><el-icon><Bell /></el-icon> 待办事项</h3><el-tag v-if="pendingCount>0" type="danger" round size="small">{{ pendingCount }}项</el-tag></div>
            <div class="todo-list" v-if="pendingRecords.length">
              <div class="todo-item" v-for="item in pendingRecords" :key="item.recordId">
                <div class="todo-left">
                  <div class="todo-dot" :class="'dot-'+item.status.toLowerCase()"></div>
                  <div class="todo-info">
                    <div class="todo-name">{{ item.policyName }}</div>
                    <div class="todo-hint">{{ getStatusHint(item.status) }}</div>
                    <div class="todo-meta">
                       <el-tag :type="riskTag(item.riskLevel)" effect="plain" size="small">{{ riskLabel(item.riskLevel) }}</el-tag>
                       <el-tag v-if="item.riskLevel==='0'" type="success" effect="dark" size="small" style="border-radius:4px">免审</el-tag>
                       <span class="todo-amount">{{ formatMoney(item.fundAmount) }} 元</span>
                     </div>
                  </div>
                </div>
                  <div class="todo-action">
                   <el-button v-if="item.status==='PUSHED' && (item.riskLevel==='0' || item.riskLevel==='1')" type="success" size="small" round @click="handleConfirm(item)"><el-icon><Select /></el-icon> 确认</el-button>
                   <el-tag v-else-if="item.status==='PUSHED' && item.riskLevel==='2' && item.auditStatus==='1'" type="warning" effect="plain" size="small"><el-icon><Clock /></el-icon> 待审核</el-tag>
                   <el-button v-else-if="item.status==='PUSHED' && item.riskLevel==='2' && item.auditStatus==='2'" type="success" size="small" round @click="handleConfirm(item)"><el-icon><Select /></el-icon> 确认</el-button>
                   <el-tag v-else-if="item.status==='PUSHED' && item.auditStatus==='3'" type="danger" effect="plain" size="small"><el-icon><Close /></el-icon> 拒绝</el-tag>
                   <el-tag v-else-if="item.status==='CONFIRMED'" type="success" effect="plain" size="small">已确认</el-tag>
                   <el-tag v-else-if="item.status==='FULFILLED'" type="success" effect="dark" size="small">已兑付</el-tag>
                   <el-tag v-else-if="item.status==='ARCHIVED'" type="info" effect="plain" size="small">已归档</el-tag>
                   <el-tag v-else :type="statusTag(item.status)" effect="plain" size="small">{{ statusShort(item.status) }}</el-tag>
                  </div>
              </div>
            </div>
            <div v-else class="empty-box"><el-icon><CircleCheck /></el-icon><span>暂无待办事项</span></div>
          </div>

          <div class="sec-card">
            <div class="sec-head"><h3><el-icon><List /></el-icon> 匹配记录</h3></div>
            <div class="record-cards" v-if="allRecords.length">
              <div class="record-card" v-for="item in allRecords" :key="item.recordId">
                <div class="rc-top">
                  <span class="rc-name">{{ item.policyName }}</span>
                  <el-tag :type="statusTag(item.status)" effect="plain" size="small">{{ statusShort(item.status) }}</el-tag>
                </div>
                <div class="rc-bottom">
                  <span class="rc-amount">{{ formatMoney(item.fundAmount) }} 元</span>
                  <span class="rc-risk" :style="{color:riskColor(item.riskLevel)}">{{ {'0':'低风险','1':'中风险','2':'高风险'}[item.riskLevel] }}</span>
                  <span class="rc-time">{{ item.matchTime || '--' }}</span>
                </div>
              </div>
            </div>
            <div v-else class="empty-box">暂无匹配记录</div>
          </div>

          <div class="sec-card">
            <div class="sec-head"><h3><el-icon><Connection /></el-icon> 政策匹配详情</h3></div>
            <div class="match-list" v-if="matchPolicies.length">
              <div class="match-item" v-for="p in matchPolicies" :key="p.policyId" :class="{open:p._expanded}" @click="p._expanded=!p._expanded">
                <div class="mi-row">
                  <el-tag :type="p.matched?'success':'danger'" effect="dark" size="small">{{ p.matched?'匹配':'不匹配' }}</el-tag>
                  <span class="mi-name">{{ p.policyName }}</span>
                  <el-icon class="mi-arrow" :class="{open:p._expanded}"><ArrowDown /></el-icon>
                </div>
                <div class="mi-detail" v-if="p._expanded">
                  <div class="mi-rule" v-for="(rule,ri) in (p.rules||[])" :key="ri">
                    <div class="mr-head"><span class="mr-name">{{ rule.ruleName }}</span><el-tag :type="rule.passed?'success':'danger'" size="small" effect="plain">{{ rule.passed?'通过':'未通过' }}</el-tag></div>
                    <div class="mr-conds">
                      <div class="mr-cond" v-for="(cond,ci) in (rule.conditions||[])" :key="ci">
                        <span class="mc-field">{{ attrLabel(cond.field) }}</span>
                        <span class="mc-op">{{ cond.operator }}</span>
                        <span class="mc-exp">{{ cond.expectedValue }}</span>
                        <span class="mc-vs">vs</span>
                        <span class="mc-actual">{{ cond.actualValue !== undefined ? cond.actualValue : '--' }}</span>
                        <el-tag :type="cond.passed?'success':'danger'" size="small" effect="plain">{{ cond.passed?'✓':'✗' }}</el-tag>
                      </div>
                    </div>
                  </div>
                  <div v-if="!p.rules||!p.rules.length" class="mi-empty">暂无规则详情</div>
                </div>
              </div>
            </div>
            <div v-else class="empty-box">暂无匹配政策</div>
          </div>

          <div class="sec-card profile-card">
            <div class="sec-head"><h3><el-icon><User /></el-icon> 个人画像</h3><el-button type="primary" plain size="small" round @click="initEditForm();showEditDialog=true"><el-icon><Edit /></el-icon> 修改</el-button></div>
            <div v-if="myProfile" class="profile-body">
              <div class="pf-row"><span class="pf-k">联系电话</span><span class="pf-v">{{ maskPhone(myProfile.contactPhone) }}</span></div>
              <div class="pf-row"><span class="pf-k">银行账号</span><span class="pf-v">{{ maskBank(myProfile.bankAccount) }}</span></div>
              <div class="pf-grid">
                <div class="pf-cell" v-for="(v,k) in parseAttrs(myProfile.attributes)" :key="k">
                  <span class="pf-ck">{{ attrLabel(k) }}</span>
                  <span class="pf-cv">{{ formatVal(k,v) }}</span>
                </div>
              </div>
            </div>
            <div v-else class="profile-guide">
              <el-icon :size="48" color="#a855f7"><WarningFilled /></el-icon>
              <h3>完善个人信息</h3>
              <p>填写个人画像，系统将为您精准匹配惠民政策</p>
              <el-button type="primary" size="large" round @click="initEditForm();showEditDialog=true"><el-icon><Edit /></el-icon> 立即填写</el-button>
            </div>
          </div>

          <div class="sec-card">
            <div class="sec-head"><h3><el-icon><Memo /></el-icon> 我的政策</h3></div>
            <div class="pol-list" v-if="myPolicies.length">
              <div class="pol-item" v-for="p in myPolicies" :key="p.policyId">
                <div class="pol-dot" :class="'dot-'+p.policyType"></div>
                <div class="pol-body">
                  <div class="pol-name">{{ p.policyName }}</div>
                  <div class="pol-meta">
                    <el-tag size="small" effect="plain">{{ policyTypeLabel(p.policyType) }}</el-tag>
                    <span class="pol-amount">{{ formatMoneyShort(p.fundAmount) }}万元</span>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="empty-box">暂无匹配政策</div>
          </div>

          <div class="sec-card tips-card">
            <div class="sec-head"><h3><el-icon><InfoFilled /></el-icon> 温馨提示</h3></div>
            <div class="tip-list">
              <div class="tip" v-for="(t,i) in tips" :key="i"><el-icon color="#a855f7"><SuccessFilled /></el-icon><span>{{ t }}</span></div>
            </div>
          </div>

    </div>

    <el-dialog title="完善个人画像" v-model="showEditDialog" width="92%" append-to-body destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" label-width="110px">
        <el-divider content-position="left">联系方式</el-divider>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="联系电话"><el-input v-model="editForm.contactPhone" placeholder="请输入手机号" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="银行账号"><el-input v-model="editForm.bankAccount" placeholder="请输入银行账号" /></el-form-item></el-col>
        </el-row>
        <el-divider content-position="left">基本信息</el-divider>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="年龄"><el-input-number v-model="editForm.age" :min="0" :max="150" style="width:100%" controls-position="right" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="性别"><el-select v-model="editForm.gender" placeholder="请选择" style="width:100%"><el-option label="男" value="male" /><el-option label="女" value="female" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="学历"><el-select v-model="editForm.education" placeholder="请选择" style="width:100%"><el-option label="初中及以下" value="junior" /><el-option label="高中" value="high" /><el-option label="大专" value="college" /><el-option label="本科" value="bachelor" /><el-option label="硕士" value="master" /><el-option label="博士" value="doctor" /></el-select></el-form-item></el-col>
        </el-row>
        <el-divider content-position="left">收入与信用</el-divider>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="年收入(万)"><el-input-number v-model="editForm.income" :min="0" style="width:100%" controls-position="right" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="信用评分"><el-input-number v-model="editForm.creditScore" :min="0" :max="100" style="width:100%" controls-position="right" /></el-form-item></el-col>
        </el-row>
        <el-divider content-position="left">个人情况</el-divider>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="残疾"><el-switch v-model="editForm.disabled" active-text="是" inactive-text="否" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="税务状态"><el-select v-model="editForm.taxStatus" placeholder="请选择" style="width:100%"><el-option label="正常" value="normal" /><el-option label="异常" value="abnormal" /></el-select></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><el-button @click="showEditDialog=false">取消</el-button><el-button type="primary" @click="handleSaveProfile">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="PersonHome">
import { getUserDashboard, getMyRecords, getMyProfile, getMyPolicies, userConfirm, updateProfile, getMatchDetails, applyForPush } from '@/api/biz/user'
import { ref, computed, onActivated, onMounted, getCurrentInstance } from 'vue'

const { proxy } = getCurrentInstance()
const nickName = ref(proxy.$store?.getters?.nickName || '用户')
const dashData = ref({})
const allRecords = ref([])
const myProfile = ref(null)
const myPolicies = ref([])
const matchPolicies = ref([])
const showEditDialog = ref(false)
const editForm = ref({contactPhone:'',bankAccount:'',age:0,gender:'',education:'',income:0,creditScore:80,disabled:false,taxStatus:'normal'})
const editFormRef = ref(null)
const tips = ['系统每日自动匹配最新惠民政策','推送后请及时确认意愿','低风险享免审绿色通道','资金拨付秒级直达银行账户']

const timeGreeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '凌晨好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const heroMetrics = computed(() => {
  const d = dashData.value
  return [
    { key:'pushed', label:'待确认', value: d.pushedCount||0, color:'#f59e0b' },
    { key:'matched', label:'已匹配', value: d.matchedCount||0, color:'#a855f7' },
    { key:'fulfilled', label:'已兑付', value: d.fulfilledCount||0, color:'#10b981' },
    { key:'amount', label:'兑付额(万)', value: formatMoneyShort(d.fulfilledAmount||0), color:'#ec4899' }
  ]
})

const pendingRecords = computed(() => allRecords.value.filter(r => ['MATCHED','PUSHED','CONFIRMED','FULFILLED'].includes(r.status)))
const pendingCount = computed(() => allRecords.value.filter(r => r.status === 'PUSHED').length)

function formatMoney(v) { if(!v) return '0.00'; return Number(v).toLocaleString('zh-CN',{minimumFractionDigits:2,maximumFractionDigits:2}) }
function formatMoneyShort(v) { if(!v) return '0'; return (Number(v)/10000).toFixed(1) }
function parseAttrs(s) { try{return JSON.parse(s)}catch{return {}} }
function maskPhone(s) { if(!s) return '--'; return s.substring(0,3)+'****'+s.substring(s.length-4) }
function maskBank(s) { if(!s) return '--'; return '**** **** **** '+s.substring(s.length-4) }
function getStatusHint(s) { return {MATCHED:'系统已自动推送，请确认意愿',PUSHED:'政策已推送，请确认意愿',CONFIRMED:'意愿已确认，等待资金拨付',FULFILLED:'资金已拨付到账'}[s]||'' }
function statusShort(s) { return {MATCHED:'等待推送',PUSHED:'待确认',CONFIRMED:'拨付中',FULFILLED:'已到账',ARCHIVED:'已归档'}[s]||'' }
function statusTag(s) { return {MATCHED:'info',PUSHED:'',CONFIRMED:'warning',FULFILLED:'success',ARCHIVED:''}[s]||'info' }
function riskTag(l) { return {'0':'success','1':'warning','2':'danger'}[l]||'info' }
function riskLabel(l) { return {'0':'低风险·免审','1':'中风险·审核','2':'高风险·拦截'}[l]||'--' }
function riskColor(l) { return {'0':'#10b981','1':'#f59e0b','2':'#ef4444'}[l]||'#94a3b8' }
function policyTypeLabel(t) { return {'1':'补贴','2':'奖励','3':'减免'}[t]||'--' }

const attrMap = {age:'年龄',gender:'性别',education:'学历',income:'年收入(万)',credit_score:'信用分',tax_status:'税务状态',disabled:'残疾人'}
function attrLabel(k) { return attrMap[k] || k }
function formatVal(k,v) { if(typeof v==='number' && v>=10000) return (v/10000).toFixed(0)+'万'; return String(v) }

function initEditForm() {
  if (!myProfile.value) return
  editForm.value.contactPhone = myProfile.value.contactPhone || ''
  editForm.value.bankAccount = myProfile.value.bankAccount || ''
  var attrs = {}
  try { attrs = JSON.parse(myProfile.value.attributes || '{}') } catch(e) { attrs = {} }
  editForm.value.age = attrs.age || 0
  editForm.value.gender = attrs.gender || ''
  editForm.value.education = attrs.education || ''
  editForm.value.income = Math.round((attrs.income || 0) / 10000)
  editForm.value.disabled = attrs.disabled || false
  editForm.value.creditScore = attrs.credit_score || 80
  editForm.value.taxStatus = attrs.tax_status || 'normal'
}

function handleSaveProfile() {
  var attrs = {}
  if (myProfile.value) { try { attrs = JSON.parse(myProfile.value.attributes || '{}') } catch(e) { attrs = {} } }
  attrs.credit_score = editForm.value.creditScore
  attrs.tax_status = editForm.value.taxStatus || 'normal'
  attrs.age = editForm.value.age
  attrs.gender = editForm.value.gender || ''
  attrs.education = editForm.value.education || ''
  attrs.income = editForm.value.income * 10000
  attrs.disabled = editForm.value.disabled
  updateProfile({ targetType:'2', contactPhone:editForm.value.contactPhone, bankAccount:editForm.value.bankAccount, attributes:JSON.stringify(attrs) }).then(function(res) {
    if (res.code === 200) { showEditDialog.value = false; loadAll() }
  })
}

function handleConfirm(row) {
  proxy.$modal.confirm('确认接受「'+row.policyName+'」？\n金额 '+formatMoney(row.fundAmount)+' 元将拨付至您的银行账户。').then(() => {
    userConfirm(row.recordId).then(() => { proxy.$modal.msgSuccess('意愿确认成功！'); loadAll() })
  }).catch(()=>{})
}

function handlePush(row) {
  proxy.$modal.confirm('确认申请推送「'+row.policyName+'」？').then(() => {
    applyForPush(row.recordId).then(res => {
      if (res.code === 200) { proxy.$modal.msgSuccess('申请已提交'); loadAll() }
      else { proxy.$modal.msgError(res.msg || '申请失败') }
    }).catch(() => { proxy.$modal.msgError('网络异常') })
  }).catch(()=>{})
}

function loadAll() {
  getUserDashboard().then(r => { dashData.value = r.data||{} })
  getMyRecords().then(r => { allRecords.value = r.rows||[] })
  getMyProfile().then(r => { myProfile.value = r.data||null; if (myProfile.value) initEditForm() })
  getMyPolicies().then(r => { myPolicies.value = r.data||[] })
  getMatchDetails().then(r => { matchPolicies.value = (r.data||[]).map(p => { p._expanded = false; return p }) })
}
onMounted(() => loadAll())
onActivated(() => loadAll())
</script>

<style lang="scss" scoped>
.ph-home { background:#fdf4ff; min-height:100%; }

.ph-hero {
  position:relative; padding:20px 16px 16px; overflow:hidden;
  background:linear-gradient(135deg, #faf5ff 0%, #f3e8ff 40%, #ede9fe 100%);
  .hero-wave { display:none; }
  .hero-inner { display:flex; align-items:center; gap:12px; }
  .hero-avatar { width:48px; height:48px; border-radius:50%; background:rgba(168,85,247,0.1); display:flex; align-items:center; justify-content:center; color:#7c3aed; flex-shrink:0; border:2px solid rgba(168,85,247,0.2); }
  .hero-text { flex:1; }
  .hero-text h1 { font-size:17px; font-weight:700; color:#0f172a; margin:0 0 2px; }
  .hl { color:#7c3aed; }
  .hero-text p { font-size:12px; color:#64748b; margin:0; }
  .hero-stats { display:none; }
}

.ph-body { padding:12px 12px 20px; }

.sec-card {
  background:#fff; border-radius:12px; padding:14px 14px; margin-bottom:12px;
  box-shadow:0 1px 6px rgba(168,85,247,0.06); border:1px solid #f3e8ff;
  .sec-head { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; }
  .sec-head h3 { font-size:14px; font-weight:600; color:#581c87; margin:0; display:flex; align-items:center; gap:6px; }
}

.todo-list { display:flex; flex-direction:column; gap:8px; }
.todo-item { display:flex; align-items:center; justify-content:space-between; padding:10px 12px; background:#fdf4ff; border-radius:10px; border:1px solid #f3e8ff; }
.todo-left { display:flex; align-items:center; gap:10px; flex:1; min-width:0; }
.todo-dot { width:8px; height:8px; border-radius:50%; flex-shrink:0; }
.dot-matched { background:#a855f7; box-shadow:0 0 0 3px rgba(168,85,247,0.2); }
.dot-pushed { background:#f59e0b; box-shadow:0 0 0 3px rgba(245,158,11,0.2); }
.dot-confirmed { background:#8b5cf6; box-shadow:0 0 0 3px rgba(139,92,246,0.2); }
.dot-fulfilled { background:#10b981; box-shadow:0 0 0 3px rgba(16,185,129,0.2); }
.todo-info { flex:1; min-width:0; }
.todo-name { font-size:13px; font-weight:600; color:#1e293b; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.todo-hint { font-size:11px; color:#94a3b8; margin-top:1px; }
.todo-meta { display:flex; align-items:center; gap:6px; margin-top:4px; flex-wrap:wrap; }
.todo-amount { font-size:11px; color:#c2410c; font-weight:bold; font-family:Consolas,monospace; }
.todo-action { flex-shrink:0; margin-left:8px; }

.record-cards { display:flex; flex-direction:column; gap:8px; }
.record-card { padding:10px 12px; background:#fdf4ff; border-radius:10px; border:1px solid #f3e8ff; }
.rc-top { display:flex; justify-content:space-between; align-items:center; margin-bottom:6px; }
.rc-name { font-size:13px; font-weight:600; color:#1e293b; flex:1; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; margin-right:8px; }
.rc-bottom { display:flex; align-items:center; gap:10px; font-size:11px; }
.rc-amount { color:#c2410c; font-weight:bold; font-family:Consolas,monospace; }
.rc-risk { font-weight:600; }
.rc-time { color:#94a3b8; }

.empty-box { padding:24px 16px; text-align:center; color:#94a3b8; display:flex; flex-direction:column; align-items:center; gap:6px; font-size:13px; }
.empty-box .el-icon { font-size:28px; color:#10b981; }

.match-list { display:flex; flex-direction:column; gap:8px; }
.match-item { border:1px solid #f3e8ff; border-radius:10px; overflow:hidden; cursor:pointer; }
.match-item.open { border-color:#c084fc; }
.mi-row { display:flex; align-items:center; gap:8px; padding:10px 12px; }
.mi-name { font-size:13px; font-weight:600; color:#1e293b; flex:1; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.mi-arrow { color:#94a3b8; transition:transform 0.2s; }
.mi-arrow.open { transform:rotate(180deg); color:#a855f7; }
.mi-detail { padding:0 12px 12px; border-top:1px solid #f3e8ff; }
.mi-rule { background:#fdf4ff; border-radius:8px; padding:10px 10px; margin-top:8px; }
.mr-head { display:flex; align-items:center; gap:6px; margin-bottom:6px; flex-wrap:wrap; }
.mr-name { font-size:11px; font-weight:600; color:#581c87; }
.mr-conds { display:flex; flex-direction:column; gap:4px; }
.mr-cond { display:flex; align-items:center; gap:6px; padding:4px 8px; background:#fff; border-radius:6px; border:1px solid #f3e8ff; flex-wrap:wrap; font-size:11px; }
.mc-field { font-weight:600; color:#581c87; background:#f3e8ff; padding:1px 4px; border-radius:3px; font-size:10px; }
.mc-op { color:#64748b; font-size:10px; }
.mc-exp { color:#7c3aed; font-weight:500; font-size:10px; }
.mc-vs { color:#d1d5db; font-size:9px; }
.mc-actual { color:#1e293b; font-weight:600; }
.mi-empty { padding:10px; text-align:center; color:#94a3b8; font-size:11px; }

.profile-body { display:flex; flex-direction:column; gap:8px; }
.pf-row { display:flex; justify-content:space-between; padding:6px 10px; background:#fdf4ff; border-radius:6px; }
.pf-k { font-size:11px; color:#a855f7; }
.pf-v { font-size:12px; font-weight:600; color:#1e293b; }
.pf-grid { display:grid; grid-template-columns:1fr 1fr; gap:6px; margin-top:4px; }
.pf-cell { padding:6px 8px; background:#fdf4ff; border-radius:6px; }
.pf-ck { font-size:9px; color:#a855f7; display:block; margin-bottom:1px; }
.pf-cv { font-size:12px; font-weight:600; color:#1e293b; }

.profile-guide { padding:24px 16px; text-align:center; display:flex; flex-direction:column; align-items:center; gap:8px; }
.profile-guide h3 { font-size:15px; font-weight:700; color:#581c87; margin:0; }
.profile-guide p { font-size:12px; color:#94a3b8; margin:0; line-height:1.5; }

.pol-list { display:flex; flex-direction:column; gap:6px; }
.pol-item { display:flex; align-items:stretch; border-radius:8px; overflow:hidden; border:1px solid #f3e8ff; }
.pol-dot { width:3px; flex-shrink:0; }
.dot-1 { background:#a855f7; }
.dot-2 { background:#10b981; }
.dot-3 { background:#f59e0b; }
.pol-body { padding:8px 10px; flex:1; }
.pol-name { font-size:12px; font-weight:600; color:#1e293b; margin-bottom:2px; }
.pol-meta { display:flex; align-items:center; gap:6px; }
.pol-amount { font-size:11px; font-weight:700; color:#c2410c; }

.tip-list { display:flex; flex-direction:column; gap:6px; }
.tip { display:flex; align-items:center; gap:6px; font-size:12px; color:#475569; }
</style>