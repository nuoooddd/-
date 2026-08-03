<template>
  <div class="m-page">
    <div class="stat-row">
      <div class="stat-item" style="background:#e0e7ff"><div class="si-val">{{ total }}</div><div class="si-label">总数</div></div>
      <div class="stat-item" style="background:#d1fae5"><div class="si-val">{{ enterpriseCount }}</div><div class="si-label">企业</div></div>
      <div class="stat-item" style="background:#fef3c7"><div class="si-val">{{ personCount }}</div><div class="si-label">个人</div></div>
      <div class="stat-item" style="background:#ede9fe"><div class="si-val">{{ activeCount }}</div><div class="si-label">正常</div></div>
    </div>

    <div class="search-bar">
      <el-input v-model="queryParams.targetName" placeholder="搜索名称" clearable @keyup.enter="handleQuery" prefix-icon="Search" />
      <el-select v-model="queryParams.targetType" placeholder="类型" clearable style="width:80px">
        <el-option v-for="dict in biz_target_type" :key="dict.value" :label="dict.label" :value="dict.value" />
      </el-select>
      <el-button type="primary" size="small" @click="handleQuery">搜索</el-button>
    </div>

    <div class="list-header">
      <span>目标对象</span>
      <el-button type="primary" size="small" @click="handleAdd" round>新增</el-button>
    </div>

    <div v-loading="loading">
      <div v-for="item in targetDataList" :key="item.targetId" class="card-item">
        <div class="ci-top">
          <div class="ci-left">
            <el-icon :color="item.targetType==='1'?'#409EFF':'#e6a23c'" :size="18"><component :is="item.targetType==='1'?'OfficeBuilding':'User'" /></el-icon>
            <span class="ci-name">{{ item.targetName }}</span>
          </div>
          <dict-tag :options="sys_normal_disable" :value="item.status" />
        </div>
        <div class="ci-mid">
          <dict-tag :options="biz_target_type" :value="item.targetType" />
          <span class="ci-id">{{ item.identifier }}</span>
        </div>
        <div class="ci-bottom">
          <span v-if="item.contactPhone">{{ item.contactPhone }}</span>
          <span v-if="item.bankAccount">{{ item.bankAccount }}</span>
        </div>
        <div class="ci-actions" @click.stop>
          <el-button link type="primary" size="small" @click="handleViewPortrait(item)" v-if="item.attributes">画像</el-button>
          <el-button link type="primary" size="small" @click="handleUpdate(item)">编辑</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(item)">删除</el-button>
        </div>
      </div>
      <el-empty v-if="!loading && targetDataList.length===0" description="暂无数据" :image-size="60" />
    </div>

    <div class="pager-row" v-if="total > 0">
      <el-button :disabled="queryParams.pageNum<=1" @click="queryParams.pageNum--;getList()" size="small">上一页</el-button>
      <span>{{ queryParams.pageNum }} / {{ Math.ceil(total/queryParams.pageSize) }}</span>
      <el-button :disabled="queryParams.pageNum>=Math.ceil(total/queryParams.pageSize)" @click="queryParams.pageNum++;getList()" size="small">下一页</el-button>
    </div>

    <el-dialog :title="title" v-model="open" width="92%" append-to-body destroy-on-close>
      <el-form ref="targetDataRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="targetName"><el-input v-model="form.targetName" placeholder="企业或个人名称" /></el-form-item>
        <el-form-item label="类型" prop="targetType">
          <el-select v-model="form.targetType" placeholder="请选择" style="width:100%">
            <el-option v-for="dict in biz_target_type" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="证件号" prop="identifier"><el-input v-model="form.identifier" placeholder="信用代码/身份证" /></el-form-item>
        <el-form-item label="画像属性"><el-input v-model="form.attributes" type="textarea" :rows="4" placeholder='JSON格式画像数据' /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.contactPhone" /></el-form-item>
        <el-form-item label="银行账号"><el-input v-model="form.bankAccount" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status"><el-radio v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio></el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="cancel">取消</el-button><el-button type="primary" @click="submitForm">确定</el-button></template>
    </el-dialog>

    <el-drawer v-model="portraitOpen" direction="btt" size="75%" :with-header="false">
      <div class="portrait-header">
        <div class="portrait-avatar" :class="portraitTarget.targetType==='1'?'avatar-enterprise':'avatar-person'">
          <el-icon :size="22"><component :is="portraitTarget.targetType==='1'?'OfficeBuilding':'User'" /></el-icon>
        </div>
        <div>
          <div class="portrait-name">{{ portraitTarget.targetName }}</div>
          <span class="portrait-type-tag">{{ portraitTarget.targetType==='1'?'企业':'个人' }}</span>
        </div>
      </div>
      <div v-if="hasAttributes" class="portrait-body">
        <div class="portrait-section" v-for="group in displayGroups" :key="group.name">
          <div class="section-title"><el-icon :size="14"><component :is="group.icon" /></el-icon><span>{{ group.name }}</span><span class="section-badge">{{ group.items.length }}</span></div>
          <div class="section-items">
            <div class="section-item" v-for="item in group.items" :key="item.key">
              <span class="item-label">{{ item.label }}</span>
              <span class="item-value" :class="{'value-num':isNumeric(item.value)}">{{ formatAttrValue(item.value, item.key) }}</span>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无画像数据" :image-size="50" />
    </el-drawer>
  </div>
</template>

<script setup name="EeTargetData">
import { listEeTargetData, getEeTargetData, delEeTargetData, addEeTargetData, updateEeTargetData } from "@/api/biz/targetData";
import { ref, reactive, toRefs, computed, getCurrentInstance } from 'vue';

const { proxy } = getCurrentInstance();
const { biz_target_type, sys_normal_disable } = proxy.useDict("biz_target_type", "sys_normal_disable");

const targetDataList = ref([]);
const open = ref(false);
const loading = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10, targetName: undefined, targetType: undefined, identifier: undefined, status: undefined } });
const { queryParams, form } = toRefs(data);

const rules = {
  targetName: [{ required: true, message: "名称不能为空", trigger: "blur" }],
  targetType: [{ required: true, message: "请选择类型", trigger: "change" }],
  identifier: [{ required: true, message: "证件号码不能为空", trigger: "blur" }]
};

const enterpriseCount = computed(() => targetDataList.value.filter(t => t.targetType === '1').length);
const personCount = computed(() => targetDataList.value.filter(t => t.targetType === '2').length);
const activeCount = computed(() => targetDataList.value.filter(t => t.status === '0').length);

const portraitOpen = ref(false);
const portraitTarget = ref({});

const attrLabelMap = {
  industry:'所属行业',region:'所在地区',establishedYear:'成立年份',established_year:'成立年份',
  legalPerson:'法定代表人',legal_person:'法定代表人',registeredCapital:'注册资本',registered_capital:'注册资本',
  businessScope:'经营范围',business_scope:'经营范围',scale:'企业规模',
  revenue:'年营收',profit:'年利润',employees:'员工人数',assetSize:'资产规模',asset_size:'资产规模',
  researchSpend:'研发投入',research_spend:'研发投入',
  taxAmount:'年纳税额',tax_amount:'年纳税额',taxType:'纳税类型',tax_type:'纳税类型',
  taxStatus:'纳税状态',tax_status:'纳税状态',creditRating:'纳税信用等级',credit_rating:'纳税信用等级',
  creditScore:'信用评分',credit_score:'信用评分',violationCount:'违规次数',violation_count:'违规次数',
  socialSecurityPaid:'社保缴纳人数',social_security_paid:'社保缴纳人数',
  socialInsuranceCnt:'社保人数',social_insurance_cnt:'社保人数',
  greenCertified:'绿色认证',green_certified:'绿色认证',
  age:'年龄',gender:'性别',education:'学历',occupation:'职业',income:'年收入',
  householdType:'户籍类型',household_type:'户籍类型',idNumber:'身份证号',id_number:'身份证号',
  phone:'联系电话',address:'居住地址',businessStatus:'经营状态',business_status:'经营状态',
  disabled:'是否禁用',carbonGrade:'碳排放等级',carbon_grade:'碳排放等级',
};

const attrGroupConfig = [
  { name:'基本信息',icon:'Document',keys:['industry','region','establishedYear','established_year','legalPerson','legal_person','registeredCapital','registered_capital','businessScope','business_scope','scale'] },
  { name:'经营状况',icon:'TrendCharts',keys:['revenue','profit','employees','assetSize','asset_size','researchSpend','research_spend'] },
  { name:'税务信息',icon:'Coin',keys:['taxAmount','tax_amount','taxType','tax_type','taxStatus','tax_status','creditRating','credit_rating'] },
  { name:'信用评估',icon:'Star',keys:['creditScore','credit_score','violationCount','violation_count','socialSecurityPaid','social_security_paid','socialInsuranceCnt','social_insurance_cnt','greenCertified','green_certified'] },
  { name:'个人信息',icon:'Avatar',keys:['age','gender','education','occupation','income','householdType','household_type','idNumber','id_number','phone','address'] },
  { name:'经营状态',icon:'DataAnalysis',keys:['businessStatus','business_status','disabled'] },
  { name:'环保信息',icon:'Sunny',keys:['carbonGrade','carbon_grade'] },
];

const attrValueTranslate = {
  scale:{large:'大型',medium:'中型',small:'小型',micro:'微型'},
  industry:{'High-tech':'高新技术','Manufacturing':'制造业','Service':'服务业','Retail':'零售业','Finance':'金融业','Catering':'餐饮业','Heavy-industry':'重工业','Construction':'建筑业','Logistics':'物流业','Agriculture':'农业','Education':'教育业'},
  tax_status:{normal:'正常纳税',abnormal:'异常',overdue:'欠税',exempt:'免税',audited:'已稽查'},
  tax_type:{general:'一般纳税人',small:'小规模纳税人'},
  credit_rating:{A:'A级',B:'B级',C:'C级',D:'D级'},
  green_certified:{'true':'已认证',true:'已认证','false':'未认证',false:'未认证'},
  gender:{male:'男',female:'女'},
  education:{highschool:'高中',bachelor:'本科',master:'硕士',doctor:'博士',junior:'初中'},
  household_type:{urban:'城镇',rural:'农村'},
  business_status:{active:'活跃',inactive:'停用',pending:'待审核',closed:'已注销'},
  carbon_grade:{A:'A级',B:'B级',C:'C级',D:'D级'},
};

function parseJson(str) { try { return JSON.parse(str); } catch { return {}; } }
const portraitAttrs = computed(() => parseJson(portraitTarget.value.attributes || '{}'));
const hasAttributes = computed(() => Object.keys(portraitAttrs.value).length > 0);

const displayGroups = computed(() => {
  const attrs = portraitAttrs.value;
  const matchedKeys = new Set();
  const groups = attrGroupConfig.map(g => {
    const items = g.keys.filter(k => attrs[k] !== undefined).map(k => { matchedKeys.add(k); return { key:k, label:attrLabelMap[k]||k, value:attrs[k] }; });
    return { name:g.name, icon:g.icon, items };
  }).filter(g => g.items.length > 0);
  const unmatched = Object.keys(attrs).filter(k => !matchedKeys.has(k));
  if (unmatched.length > 0) { groups.push({ name:'其他属性', icon:'MoreFilled', items:unmatched.map(k => ({ key:k, label:k, value:attrs[k] })) }); }
  return groups;
});

function isNumeric(val) { return !isNaN(parseFloat(val)) && isFinite(val); }
function translateValue(key, val) { const t = attrValueTranslate[key]; if (t && t[val] !== undefined) return t[val]; if (val === 'true' || val === true) return '是'; if (val === 'false' || val === false) return '否'; return val; }
function formatAttrValue(val, key) { if (val === undefined || val === null || val === '') return '-'; if (isNumeric(val)) { const num = parseFloat(val); if (num >= 10000) return (num/10000).toFixed(2)+'万'; return num.toLocaleString(); } return translateValue(key, val); }
function handleViewPortrait(row) { portraitTarget.value = {...row}; portraitOpen.value = true; }

function getList() { loading.value = true; listEeTargetData(queryParams.value).then(response => { targetDataList.value = response.rows; total.value = response.total; loading.value = false; }); }
function handleQuery() { queryParams.value.pageNum = 1; getList(); }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery(); }
function cancel() { open.value = false; reset(); }
function reset() { Object.keys(form.value).forEach(k => delete form.value[k]); Object.assign(form.value, { targetId:undefined, targetName:undefined, targetType:undefined, identifier:undefined, attributes:undefined, contactPhone:undefined, bankAccount:undefined, status:"0" }); proxy.resetForm("targetDataRef"); }
function handleAdd() { reset(); open.value = true; title.value = "新增目标数据"; }
function handleUpdate(row) { reset(); getEeTargetData(row.targetId).then(response => { Object.assign(form.value, response.data); form.value.status = String(form.value.status); open.value = true; title.value = "修改目标数据"; }); }
function submitForm() {
  proxy.$refs["targetDataRef"].validate(valid => {
    if (valid) {
      if (form.value.targetId != null) { updateEeTargetData(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); }); }
      else { addEeTargetData(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); }); }
    }
  });
}
function handleDelete(row) { proxy.$modal.confirm('是否确认删除"'+row.targetName+'"？').then(() => { return delEeTargetData(row.targetId); }).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {}); }
getList();
</script>

<style scoped>
.m-page { padding:12px; background:#f5f6fa; min-height:100%; }
.stat-row { display:flex; gap:6px; margin-bottom:12px; }
.stat-item { flex:1; text-align:center; padding:10px 4px; border-radius:10px; }
.si-val { font-size:16px; font-weight:700; color:#1e293b; font-family:'Outfit',sans-serif; }
.si-label { font-size:10px; color:#64748b; margin-top:2px; }
.search-bar { display:flex; gap:6px; margin-bottom:10px; }
.search-bar .el-input { flex:1; }
.list-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:8px; font-size:14px; font-weight:600; color:#0f172a; }
.card-item { background:#fff; border:1px solid #e2e8f0; border-radius:10px; padding:12px; margin-bottom:8px; }
.ci-top { display:flex; justify-content:space-between; align-items:center; margin-bottom:6px; }
.ci-left { display:flex; align-items:center; gap:6px; flex:1; }
.ci-name { font-size:14px; font-weight:600; color:#1e293b; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.ci-mid { display:flex; align-items:center; gap:8px; margin-bottom:6px; }
.ci-id { font-size:11px; color:#94a3b8; font-family:Consolas,monospace; }
.ci-bottom { display:flex; gap:12px; font-size:11px; color:#94a3b8; }
.ci-actions { display:flex; gap:4px; margin-top:8px; padding-top:8px; border-top:1px solid #f1f5f9; }
.pager-row { display:flex; justify-content:center; align-items:center; gap:12px; padding:10px 0; font-size:13px; color:#64748b; }

.portrait-header { display:flex; align-items:center; gap:12px; padding:16px; background:linear-gradient(135deg,#1a3a5c,#2d5a8e); border-radius:14px 14px 0 0; color:#fff; }
.portrait-avatar { width:44px; height:44px; border-radius:50%; display:flex; align-items:center; justify-content:center; }
.avatar-enterprise { background:rgba(64,158,255,0.3); }
.avatar-person { background:rgba(230,162,60,0.3); }
.portrait-name { font-size:16px; font-weight:700; }
.portrait-type-tag { font-size:11px; background:rgba(255,255,255,0.15); padding:2px 8px; border-radius:8px; }
.portrait-body { padding:12px; }
.portrait-section { margin-bottom:12px; background:#f8fafc; border-radius:10px; padding:10px; }
.section-title { display:flex; align-items:center; gap:6px; font-size:13px; font-weight:700; color:#1e293b; margin-bottom:8px; padding-bottom:6px; border-bottom:1px solid #e2e8f0; }
.section-badge { margin-left:auto; background:#e0e7ff; color:#4f46e5; font-size:11px; padding:1px 6px; border-radius:8px; }
.section-items { display:flex; flex-direction:column; gap:2px; }
.section-item { display:flex; justify-content:space-between; align-items:center; padding:6px 8px; }
.item-label { font-size:12px; color:#64748b; }
.item-value { font-size:12px; color:#1e293b; font-weight:500; text-align:right; word-break:break-all; }
.item-value.value-num { color:#4f46e5; font-weight:600; font-family:Consolas,monospace; }
</style>
