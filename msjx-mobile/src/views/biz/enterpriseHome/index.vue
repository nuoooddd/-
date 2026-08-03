<template>
  <div class="ep-home">
    <div class="ep-hero">
      <div class="hero-badge"><el-icon><OfficeBuilding /></el-icon> 企业服务专区</div>
      <h1 class="hero-title">{{ timeGreeting }}，<span class="hl">{{ nickName }}</span></h1>
      <p class="hero-sub">智能匹配惠企政策 · 补贴直达 · 零申请零跑腿</p>
      <div class="hero-stats">
        <div class="hs-item" v-for="m in heroMetrics" :key="m.key">
          <div class="hs-val" :style="{color:m.color}">{{ m.value }}</div>
          <div class="hs-label">{{ m.label }}</div>
        </div>
      </div>
      <div class="hero-actions">
        <el-button type="primary" size="small" round @click="$router.push('/portal/userFulfill')"><el-icon><Money /></el-icon> 我的兑付</el-button>
        <el-button size="small" round plain @click="showEditDialog=true"><el-icon><Edit /></el-icon> 完善画像</el-button>
      </div>
    </div>

    <div class="ep-body">

          <div class="section-card">
            <div class="sec-head">
              <h3><el-icon><Bell /></el-icon> 待办事项</h3>
              <el-tag v-if="pendingCount>0" type="danger" round size="small">{{ pendingCount }}项待处理</el-tag>
            </div>
            <div class="todo-list" v-if="pendingRecords.length">
              <div class="todo-item" v-for="item in pendingRecords" :key="item.recordId">
                <div class="todo-left">
                  <div class="todo-step" :class="'step-'+item.status.toLowerCase()">
                    <el-icon><component :is="stepIcon(item.status)" /></el-icon>
                  </div>
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
                   <el-button v-if="item.status==='PUSHED' && (item.riskLevel==='0' || item.riskLevel==='1')" type="success" size="small" round @click="handleConfirm(item)"><el-icon><Select /></el-icon> 确认意愿</el-button>
                   <el-tag v-else-if="item.status==='PUSHED' && item.riskLevel==='2' && item.auditStatus==='1'" type="warning" effect="plain" size="small"><el-icon><Clock /></el-icon> 待审核</el-tag>
                   <el-button v-else-if="item.status==='PUSHED' && item.riskLevel==='2' && item.auditStatus==='2'" type="success" size="small" round @click="handleConfirm(item)"><el-icon><Select /></el-icon> 审核通过·确认</el-button>
                   <el-tag v-else-if="item.status==='PUSHED' && item.auditStatus==='3'" type="danger" effect="plain" size="small"><el-icon><Close /></el-icon> 审核拒绝</el-tag>
                   <el-tag v-else-if="item.status==='CONFIRMED'" type="success" effect="plain" size="small">已确认</el-tag>
                   <el-tag v-else-if="item.status==='FULFILLED'" type="success" effect="dark" size="small">已兑付</el-tag>
                   <el-tag v-else-if="item.status==='ARCHIVED'" type="info" effect="plain" size="small">已归档</el-tag>
                   <el-tag v-else :type="statusTag(item.status)" effect="plain" size="small">{{ statusShort(item.status) }}</el-tag>
                  </div>
              </div>
            </div>
            <div v-else class="empty-box"><el-icon><CircleCheck /></el-icon><span>暂无待办事项</span></div>
          </div>

          <div class="section-card">
            <div class="sec-head"><h3><el-icon><Connection /></el-icon> 政策匹配详情</h3></div>
            <div class="match-list" v-if="matchPolicies.length">
              <div class="match-item" v-for="p in matchPolicies" :key="p.policyId" :class="{open:p._expanded}" @click="p._expanded=!p._expanded">
                <div class="mi-row">
                  <el-tag :type="p.matched?'success':'danger'" effect="dark" size="small">{{ p.matched?'匹配':'不匹配' }}</el-tag>
                  <span class="mi-name">{{ p.policyName }}</span>
                  <span class="mi-meta">{{ p.policyNo }} · {{ policyTypeLabel(p.policyType) }}</span>
                  <el-icon class="mi-arrow" :class="{open:p._expanded}"><ArrowDown /></el-icon>
                </div>
                <div class="mi-detail" v-if="p._expanded">
                  <div class="mi-rule" v-for="(rule,ri) in (p.rules||[])" :key="ri">
                    <div class="mr-head"><span class="mr-name">{{ rule.ruleName }}</span><code class="mr-expr">{{ rule.conditionExpr }}</code><el-tag :type="rule.passed?'success':'danger'" size="small" effect="plain">{{ rule.passed?'通过':'未通过' }}</el-tag></div>
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

          <div class="section-card profile-card">
            <div class="sec-head"><h3><el-icon><OfficeBuilding /></el-icon> 企业画像</h3><el-button type="primary" plain size="small" round @click="initEditForm();showEditDialog=true"><el-icon><Edit /></el-icon> 修改</el-button></div>
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
              <el-icon :size="48" color="#e6a23c"><WarningFilled /></el-icon>
              <h3>完善企业信息</h3>
              <p>填写企业画像，系统将为您精准匹配惠企政策</p>
              <el-button type="primary" size="large" round @click="initEditForm();showEditDialog=true"><el-icon><Edit /></el-icon> 立即填写</el-button>
            </div>
          </div>

          <div class="section-card">
            <div class="sec-head"><h3><el-icon><Memo /></el-icon> 已匹配政策</h3></div>
            <div class="pol-list" v-if="myPolicies.length">
              <div class="pol-item" v-for="p in myPolicies" :key="p.policyId">
                <div class="pol-bar" :class="'bar-'+p.policyType"></div>
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

          <div class="section-card tips-card">
            <div class="sec-head"><h3><el-icon><InfoFilled /></el-icon> 温馨提示</h3></div>
            <div class="tip-list">
              <div class="tip" v-for="(t,i) in tips" :key="i"><el-icon color="#52c41a"><SuccessFilled /></el-icon><span>{{ t }}</span></div>
            </div>
          </div>

    </div>

    <el-dialog title="完善企业画像" v-model="showEditDialog" width="92%" append-to-body destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" label-width="110px">
        <el-divider content-position="left">联系方式</el-divider>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="联系电话"><el-input v-model="editForm.contactPhone" placeholder="请输入手机号" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="银行账号"><el-input v-model="editForm.bankAccount" placeholder="请输入银行账号" /></el-form-item></el-col>
        </el-row>
        <el-divider content-position="left">基本信息</el-divider>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="行业类型"><el-select v-model="editForm.industry" placeholder="请选择行业" style="width:100%"><el-option label="高科技" value="High-tech" /><el-option label="餐饮" value="Catering" /><el-option label="建筑" value="Construction" /><el-option label="制造" value="Manufacturing" /><el-option label="金融" value="Finance" /><el-option label="零售" value="Retail" /><el-option label="教育" value="Education" /><el-option label="医疗" value="Healthcare" /><el-option label="农业" value="Agriculture" /><el-option label="物流" value="Logistics" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="企业规模"><el-select v-model="editForm.scale" placeholder="请选择规模" style="width:100%"><el-option label="小型" value="small" /><el-option label="中型" value="medium" /><el-option label="大型" value="large" /></el-select></el-form-item></el-col>
        </el-row>
        <el-divider content-position="left">经营数据</el-divider>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="年营收(万)"><el-input-number v-model="editForm.revenue" :min="0" style="width:100%" controls-position="right" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="社保人数"><el-input-number v-model="editForm.socialInsuranceCnt" :min="0" style="width:100%" controls-position="right" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="注册资本(万)"><el-input-number v-model="editForm.registeredCapital" :min="0" style="width:100%" controls-position="right" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="研发投入(万)"><el-input-number v-model="editForm.researchSpend" :min="0" style="width:100%" controls-position="right" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="增长率(%)"><el-input-number v-model="editForm.growthRate" :min="0" :max="1000" style="width:100%" controls-position="right" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="信用评分"><el-input-number v-model="editForm.creditScore" :min="0" :max="100" style="width:100%" controls-position="right" /></el-form-item></el-col>
        </el-row>
        <el-divider content-position="left">资质认证</el-divider>
        <el-row :gutter="16">
          <el-col :span="6"><el-form-item label="高新技术"><el-switch v-model="editForm.highTech" active-text="是" inactive-text="否" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="绿色认证"><el-switch v-model="editForm.greenCertified" active-text="是" inactive-text="否" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="税务状态"><el-select v-model="editForm.taxStatus" placeholder="请选择" style="width:100%"><el-option label="正常" value="normal" /><el-option label="异常" value="abnormal" /></el-select></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><el-button @click="showEditDialog=false">取消</el-button><el-button type="primary" @click="handleSaveProfile">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="EnterpriseHome">
import { getUserDashboard, getMyRecords, getMyProfile, getMyPolicies, userConfirm, updateProfile, getMatchDetails, applyForPush } from '@/api/biz/user'
import { ref, computed, onActivated, onMounted, getCurrentInstance } from 'vue'

const { proxy } = getCurrentInstance()
const nickName = ref(proxy.$store?.getters?.nickName || '企业用户')
const dashData = ref({})
const allRecords = ref([])
const myProfile = ref(null)
const myPolicies = ref([])
const matchPolicies = ref([])
const showEditDialog = ref(false)
const editForm = ref({contactPhone:'',bankAccount:'',revenue:0,socialInsuranceCnt:0,creditScore:80,industry:'',scale:'',taxStatus:'normal',researchSpend:0,highTech:false,greenCertified:false,registeredCapital:0,growthRate:0})
const editFormRef = ref(null)
const tips = ['系统每日自动匹配最新惠企政策','推送后请及时确认意愿','低风险享免审绿色通道','资金拨付秒级直达银行账户']

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
    { key:'matched', label:'已匹配', value: d.matchedCount||0, color:'#3b82f6' },
    { key:'fulfilled', label:'已兑付', value: d.fulfilledCount||0, color:'#10b981' },
    { key:'amount', label:'兑付额(万)', value: formatMoneyShort(d.fulfilledAmount||0), color:'#8b5cf6' }
  ]
})

const pendingRecords = computed(() => allRecords.value.filter(r => ['MATCHED','PUSHED','CONFIRMED','FULFILLED'].includes(r.status)))
const pendingCount = computed(() => allRecords.value.filter(r => r.status === 'PUSHED').length)

function formatMoney(v) { if(!v) return '0.00'; return Number(v).toLocaleString('zh-CN',{minimumFractionDigits:2,maximumFractionDigits:2}) }
function formatMoneyShort(v) { if(!v) return '0'; return (Number(v)/10000).toFixed(1) }
function parseAttrs(s) { try{return JSON.parse(s)}catch{return {}} }
function maskPhone(s) { if(!s) return '--'; return s.substring(0,3)+'****'+s.substring(s.length-4) }
function maskBank(s) { if(!s) return '--'; return '**** **** **** '+s.substring(s.length-4) }
function stepIcon(s) { return {MATCHED:'Cpu',PUSHED:'Bell',CONFIRMED:'Check',FULFILLED:'CircleCheck',ARCHIVED:'FolderChecked'}[s]||'Cpu' }
function getStatusHint(s) { return {MATCHED:'系统已自动推送，请确认意愿',PUSHED:'政策已推送，请确认意愿',CONFIRMED:'意愿已确认，等待资金拨付',FULFILLED:'资金已拨付到账'}[s]||'' }
function statusShort(s) { return {MATCHED:'等待推送',CONFIRMED:'拨付中',FULFILLED:'已到账',ARCHIVED:'已归档'}[s]||'' }
function statusTag(s) { return {MATCHED:'info',CONFIRMED:'warning',FULFILLED:'success',ARCHIVED:''}[s]||'info' }
function riskTag(l) { return {'0':'success','1':'warning','2':'danger'}[l]||'info' }
function riskLabel(l) { return {'0':'低风险·免审','1':'中风险·审核','2':'高风险·拦截'}[l]||'--' }
function policyTypeLabel(t) { return {'1':'补贴','2':'奖励','3':'减免'}[t]||'--' }

const attrMap = {industry:'行业',scale:'规模',revenue:'营收(万)',credit_score:'信用分',tax_status:'税务状态',research_spend:'研发投入(万)',social_insurance_cnt:'社保人数',high_tech:'高新技术企业',registered_capital:'注册资本(万)',green_certified:'绿色认证',growth_rate:'增长率(%)'}
function attrLabel(k) { return attrMap[k] || k }
function formatVal(k,v) { if(typeof v==='number' && v>=10000) return (v/10000).toFixed(0)+'万'; return String(v) }

function initEditForm() {
  if (!myProfile.value) return
  editForm.value.contactPhone = myProfile.value.contactPhone || ''
  editForm.value.bankAccount = myProfile.value.bankAccount || ''
  var attrs = {}
  try { attrs = JSON.parse(myProfile.value.attributes || '{}') } catch(e) { attrs = {} }
  editForm.value.industry = attrs.industry || ''
  editForm.value.scale = attrs.scale || ''
  editForm.value.revenue = Math.round((attrs.revenue || 0) / 10000)
  editForm.value.socialInsuranceCnt = attrs.social_insurance_cnt || 0
  editForm.value.registeredCapital = Math.round((attrs.registered_capital || 0) / 10000)
  editForm.value.researchSpend = Math.round((attrs.research_spend || 0) / 10000)
  editForm.value.growthRate = attrs.growth_rate || 0
  editForm.value.highTech = attrs.high_tech || false
  editForm.value.greenCertified = attrs.green_certified || false
  editForm.value.creditScore = attrs.credit_score || 80
  editForm.value.taxStatus = attrs.tax_status || 'normal'
}

function handleSaveProfile() {
  var attrs = {}
  if (myProfile.value) { try { attrs = JSON.parse(myProfile.value.attributes || '{}') } catch(e) { attrs = {} } }
  attrs.credit_score = editForm.value.creditScore
  attrs.tax_status = editForm.value.taxStatus || 'normal'
  attrs.industry = editForm.value.industry || ''
  attrs.scale = editForm.value.scale || ''
  attrs.revenue = editForm.value.revenue * 10000
  attrs.social_insurance_cnt = editForm.value.socialInsuranceCnt
  attrs.research_spend = editForm.value.researchSpend * 10000
  attrs.high_tech = editForm.value.highTech
  attrs.green_certified = editForm.value.greenCertified
  attrs.registered_capital = editForm.value.registeredCapital * 10000
  attrs.growth_rate = editForm.value.growthRate
  updateProfile({ targetType:'1', contactPhone:editForm.value.contactPhone, bankAccount:editForm.value.bankAccount, attributes:JSON.stringify(attrs) }).then(function(res) {
    if (res.code === 200) { showEditDialog.value = false; loadAll() }
  })
}

function handleConfirm(row) {
  proxy.$modal.confirm('确认接受「'+row.policyName+'」？\n金额 '+formatMoney(row.fundAmount)+' 元将拨付至您的银行账户。').then(() => {
    userConfirm(row.recordId).then(() => { proxy.$modal.msgSuccess('意愿确认成功！资金即将到账。'); loadAll() })
  }).catch(()=>{})
}

function handlePush(row) {
  proxy.$modal.confirm('确认申请推送「'+row.policyName+'」？\n管理员将收到通知并处理。').then(() => {
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
.ep-home { background:#f0f4f8; min-height:100%; }

.ep-hero {
  padding:24px 16px 20px;
  background:linear-gradient(135deg, #eff6ff 0%, #dbeafe 50%, #e0f2fe 100%);
  .hero-badge { display:inline-flex; align-items:center; gap:6px; padding:4px 12px; border-radius:16px; background:rgba(59,130,246,0.1); border:1px solid rgba(59,130,246,0.2); color:#2563eb; font-size:12px; margin-bottom:12px; }
  .hero-title { font-size:20px; font-weight:700; color:#0f172a; margin:0 0 6px; }
  .hl { color:#2563eb; }
  .hero-sub { font-size:13px; color:#64748b; margin:0 0 16px; }
  .hero-stats { display:flex; gap:0; margin-bottom:16px; background:rgba(255,255,255,0.6); border-radius:12px; overflow:hidden; }
  .hs-item { flex:1; text-align:center; padding:10px 4px; border-right:1px solid rgba(0,0,0,0.04); }
  .hs-item:last-child { border-right:none; }
  .hs-val { font-size:18px; font-weight:700; font-family:'Outfit',sans-serif; }
  .hs-label { font-size:10px; color:#64748b; margin-top:2px; }
  .hero-actions { display:flex; gap:10px; }
}

.ep-body { padding:12px 12px 20px; }

.section-card {
  background:#fff; border-radius:12px; padding:14px 14px; margin-bottom:12px;
  box-shadow:0 1px 6px rgba(0,0,0,0.04); border:1px solid #e8ecf1;
  .sec-head { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; }
  .sec-head h3 { font-size:14px; font-weight:600; color:#1e293b; margin:0; display:flex; align-items:center; gap:6px; }
}

.todo-list { display:flex; flex-direction:column; gap:8px; }
.todo-item { display:flex; align-items:center; justify-content:space-between; padding:10px 12px; background:#f8fafc; border-radius:10px; border:1px solid #e8ecf1; }
.todo-left { display:flex; align-items:center; gap:10px; flex:1; min-width:0; }
.todo-step { width:32px; height:32px; border-radius:50%; display:flex; align-items:center; justify-content:center; font-size:16px; color:#fff; flex-shrink:0; }
.step-matched { background:#3b82f6; }
.step-pushed { background:#f59e0b; }
.step-confirmed { background:#8b5cf6; }
.step-fulfilled { background:#10b981; }
.todo-info { flex:1; min-width:0; }
.todo-name { font-size:13px; font-weight:600; color:#1e293b; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.todo-hint { font-size:11px; color:#94a3b8; margin-top:1px; }
.todo-meta { display:flex; align-items:center; gap:6px; margin-top:4px; flex-wrap:wrap; }
.todo-amount { font-size:11px; color:#c2410c; font-weight:bold; font-family:Consolas,monospace; }
.todo-action { flex-shrink:0; margin-left:8px; }

.empty-box { padding:24px 16px; text-align:center; color:#94a3b8; display:flex; flex-direction:column; align-items:center; gap:6px; font-size:13px; }
.empty-box .el-icon { font-size:28px; color:#10b981; }

.match-list { display:flex; flex-direction:column; gap:8px; }
.match-item { border:1px solid #e8ecf1; border-radius:10px; overflow:hidden; cursor:pointer; }
.match-item.open { border-color:#93c5fd; }
.mi-row { display:flex; align-items:center; gap:8px; padding:10px 12px; }
.mi-name { font-size:13px; font-weight:600; color:#1e293b; flex:1; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.mi-meta { display:none; }
.mi-arrow { color:#94a3b8; transition:transform 0.2s; }
.mi-arrow.open { transform:rotate(180deg); color:#3b82f6; }
.mi-detail { padding:0 12px 12px; border-top:1px solid #f1f5f9; }
.mi-rule { background:#f8fafc; border-radius:8px; padding:10px 10px; margin-top:8px; }
.mr-head { display:flex; align-items:center; gap:6px; margin-bottom:6px; flex-wrap:wrap; }
.mr-name { font-size:11px; font-weight:600; color:#334155; }
.mr-expr { font-size:10px; color:#94a3b8; font-family:'Courier New',monospace; background:#f1f5f9; padding:1px 4px; border-radius:3px; }
.mr-conds { display:flex; flex-direction:column; gap:4px; }
.mr-cond { display:flex; align-items:center; gap:6px; padding:4px 8px; background:#fff; border-radius:6px; border:1px solid #f1f5f9; flex-wrap:wrap; font-size:11px; }
.mc-field { font-weight:600; color:#334155; background:#eff6ff; padding:1px 4px; border-radius:3px; font-size:10px; }
.mc-op { color:#64748b; font-size:10px; }
.mc-exp { color:#6366f1; font-weight:500; font-size:10px; }
.mc-vs { color:#d1d5db; font-size:9px; }
.mc-actual { color:#1e293b; font-weight:600; }
.mi-empty { padding:10px; text-align:center; color:#94a3b8; font-size:11px; }

.profile-body { display:flex; flex-direction:column; gap:8px; }
.pf-row { display:flex; justify-content:space-between; padding:6px 10px; background:#f8fafc; border-radius:6px; }
.pf-k { font-size:11px; color:#94a3b8; }
.pf-v { font-size:12px; font-weight:600; color:#1e293b; }
.pf-grid { display:grid; grid-template-columns:1fr 1fr; gap:6px; margin-top:4px; }
.pf-cell { padding:6px 8px; background:#f8fafc; border-radius:6px; }
.pf-ck { font-size:9px; color:#94a3b8; display:block; margin-bottom:1px; }
.pf-cv { font-size:12px; font-weight:600; color:#1e293b; }

.profile-guide { padding:24px 16px; text-align:center; display:flex; flex-direction:column; align-items:center; gap:8px; }
.profile-guide h3 { font-size:15px; font-weight:700; color:#1e293b; margin:0; }
.profile-guide p { font-size:12px; color:#94a3b8; margin:0; line-height:1.5; }

.pol-list { display:flex; flex-direction:column; gap:6px; }
.pol-item { display:flex; align-items:stretch; border-radius:8px; overflow:hidden; border:1px solid #e8ecf1; }
.pol-bar { width:3px; flex-shrink:0; }
.bar-1 { background:#3b82f6; }
.bar-2 { background:#10b981; }
.bar-3 { background:#f59e0b; }
.pol-body { padding:8px 10px; flex:1; }
.pol-name { font-size:12px; font-weight:600; color:#1e293b; margin-bottom:2px; }
.pol-meta { display:flex; align-items:center; gap:6px; }
.pol-amount { font-size:11px; font-weight:700; color:#c2410c; }

.tip-list { display:flex; flex-direction:column; gap:6px; }
.tip { display:flex; align-items:center; gap:6px; font-size:12px; color:#475569; }
</style>