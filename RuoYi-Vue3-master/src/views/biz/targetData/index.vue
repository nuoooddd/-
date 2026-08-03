<template>
  <div class="app-container" style="background: #f6f8fb; min-height: calc(100vh - 84px); padding: 20px;">
    
    <el-row :gutter="20" class="mb20">
      <el-col :span="6">
        <div class="glass-card stat-primary">
          <div class="card-icon"><el-icon><User /></el-icon></div>
          <div class="card-info">
            <div class="card-label">目标对象总数</div>
            <div class="card-value">{{ total }} <span class="unit">个</span></div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-card stat-success">
          <div class="card-icon"><el-icon><OfficeBuilding /></el-icon></div>
          <div class="card-info">
            <div class="card-label">企业数量</div>
            <div class="card-value">{{ enterpriseCount }} <span class="unit">家</span></div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-card stat-warning">
          <div class="card-icon"><el-icon><UserFilled /></el-icon></div>
          <div class="card-info">
            <div class="card-label">个人数量</div>
            <div class="card-value">{{ personCount }} <span class="unit">人</span></div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-card stat-info">
          <div class="card-icon"><el-icon><CircleCheck /></el-icon></div>
          <div class="card-info">
            <div class="card-label">正常状态</div>
            <div class="card-value">{{ activeCount }} <span class="unit">个</span></div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-card style="border-radius: 12px; box-shadow: 0 4px 12px 0 rgba(0,0,0,0.05);">
      <template #header>
        <div class="card-header-flex">
          <span class="header-title"><el-icon><Search /></el-icon> 目标数据检索</span>
        </div>
      </template>
      <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="68px">
        <el-form-item label="名称" prop="targetName">
          <el-input v-model="queryParams.targetName" placeholder="企业/个人名称" clearable @keyup.enter="handleQuery" style="width: 180px;" />
        </el-form-item>
        <el-form-item label="类型" prop="targetType">
          <el-select v-model="queryParams.targetType" placeholder="全部类型" clearable style="width: 120px;">
            <el-option v-for="dict in biz_target_type" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="证件号" prop="identifier">
          <el-input v-model="queryParams.identifier" placeholder="信用代码/身份证" clearable @keyup.enter="handleQuery" style="width: 200px;" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 100px;">
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
          <span class="header-title"><el-icon><List /></el-icon> 目标数据列表</span>
          <el-button type="primary" plain icon="Plus" @click="handleAdd" style="border-radius: 8px;">新增目标</el-button>
        </div>
      </template>
      <el-table v-loading="loading" :data="targetDataList" style="border-radius: 8px;" :header-cell-style="{ background: '#f5f7fa', color: '#606266', fontWeight: 'bold' }">
        <el-table-column label="ID" align="center" prop="targetId" width="60" />
        <el-table-column label="名称" align="left" prop="targetName" min-width="180">
          <template #default="scope">
            <div style="display: flex; align-items: center;">
              <el-icon :color="scope.row.targetType === '1' ? '#409EFF' : '#e6a23c'" style="margin-right: 8px;">
                <component :is="scope.row.targetType === '1' ? 'OfficeBuilding' : 'User'" />
              </el-icon>
              <span style="font-weight: 500;">{{ scope.row.targetName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="类型" align="center" prop="targetType" width="80">
          <template #default="scope">
            <dict-tag :options="biz_target_type" :value="scope.row.targetType" />
          </template>
        </el-table-column>
        <el-table-column label="证件号码" align="center" prop="identifier" min-width="180" />
        <el-table-column label="画像" align="center" width="130">
          <template #default="scope">
            <el-button v-if="scope.row.attributes" type="primary" link @click="handleViewPortrait(scope.row)">
              <el-icon><View /></el-icon> 查看画像
            </el-button>
            <span v-else style="color: #c0c4cc;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="联系电话" align="center" prop="contactPhone" width="130" />
        <el-table-column label="银行账号" align="center" prop="bankAccount" width="180" />
        <el-table-column label="状态" align="center" prop="status" width="70">
          <template #default="scope">
            <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="160">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
            <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="title" v-model="open" width="650px" append-to-body>
      <el-form ref="targetDataRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="名称" prop="targetName">
              <el-input v-model="form.targetName" placeholder="企业或个人名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类型" prop="targetType">
              <el-select v-model="form.targetType" placeholder="请选择类型" style="width: 100%;">
                <el-option v-for="dict in biz_target_type" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="证件号码" prop="identifier">
          <el-input v-model="form.identifier" placeholder="统一社会信用代码/身份证号" />
        </el-form-item>
        <el-form-item label="画像属性" prop="attributes">
          <el-input v-model="form.attributes" type="textarea" :rows="5" placeholder='JSON格式，如: {"industry":"高新技术","revenue":500000,"employees":50,"taxAmount":80000}' />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="form.contactPhone" placeholder="联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="银行账号" prop="bankAccount">
              <el-input v-model="form.bankAccount" placeholder="银行账号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm" style="border-radius: 8px;">确 定</el-button>
          <el-button @click="cancel" style="border-radius: 8px;">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 画像抽屉 -->
    <el-drawer v-model="portraitOpen" direction="rtl" size="560px" :before-close="handleClosePortrait">
      <template #header>
        <div class="portrait-header">
          <div class="portrait-avatar" :class="portraitTarget.targetType === '1' ? 'avatar-enterprise' : 'avatar-person'">
            <el-icon :size="28"><component :is="portraitTarget.targetType === '1' ? 'OfficeBuilding' : 'User'" /></el-icon>
          </div>
          <div class="portrait-header-info">
            <h3 class="portrait-name">{{ portraitTarget.targetName }}</h3>
            <span class="portrait-type-tag">{{ portraitTarget.targetType === '1' ? '企业' : '个人' }}</span>
          </div>
        </div>
      </template>

      <div class="portrait-body" v-if="hasAttributes">
        <!-- 每个分组 -->
        <div class="portrait-section" v-for="group in displayGroups" :key="group.name">
          <div class="section-title">
            <el-icon :size="18"><component :is="group.icon" /></el-icon>
            <span>{{ group.name }}</span>
            <span class="section-badge">{{ group.items.length }}</span>
          </div>
          <div class="section-items">
            <div class="section-item" v-for="item in group.items" :key="item.key">
              <span class="item-label">{{ item.label }}</span>
              <span class="item-value" :class="{ 'value-num': isNumeric(item.value) }">
                {{ formatAttrValue(item.value, item.key) }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="portrait-empty">
        <el-empty description="暂无画像数据">
          <el-button type="primary" @click="handleUpdate(portraitTarget); portraitOpen = false;">去添加画像属性</el-button>
        </el-empty>
      </div>

      <template #footer>
        <div class="portrait-footer">
          <el-button @click="portraitOpen = false">关 闭</el-button>
          <el-button type="primary" @click="handleUpdate(portraitTarget); portraitOpen = false;">
            <el-icon><Edit /></el-icon> 编辑画像
          </el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup name="EeTargetData">
import { listEeTargetData, getEeTargetData, delEeTargetData, addEeTargetData, updateEeTargetData } from "@/api/biz/targetData";
import { ref, reactive, toRefs, computed, getCurrentInstance, watch } from 'vue';

const { proxy } = getCurrentInstance();
const { biz_target_type, sys_normal_disable } = proxy.useDict("biz_target_type", "sys_normal_disable");

const targetDataList = ref([]);
const open = ref(false);
const loading = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, targetName: undefined, targetType: undefined, identifier: undefined, status: undefined }
});
const { queryParams, form } = toRefs(data);

const rules = {
  targetName: [{ required: true, message: "名称不能为空", trigger: "blur" }],
  targetType: [{ required: true, message: "请选择类型", trigger: "change" }],
  identifier: [{ required: true, message: "证件号码不能为空", trigger: "blur" }]
};

const enterpriseCount = computed(() => targetDataList.value.filter(t => t.targetType === '1').length);
const personCount = computed(() => targetDataList.value.filter(t => t.targetType === '2').length);
const activeCount = computed(() => targetDataList.value.filter(t => t.status === '0').length);

// ======== 画像抽屉相关 ========
const portraitOpen = ref(false);
const portraitTarget = ref({});

// 画像属性中文标签映射表（含驼峰和下划线两种命名风格）
const attrLabelMap = {
  // 基本信息
  industry: '所属行业',
  region: '所在地区',
  establishedYear: '成立年份',
  established_year: '成立年份',
  legalPerson: '法定代表人',
  legal_person: '法定代表人',
  registeredCapital: '注册资本',
  registered_capital: '注册资本',
  businessScope: '经营范围',
  business_scope: '经营范围',
  scale: '企业规模',
  // 经营信息
  revenue: '年营收',
  profit: '年利润',
  employees: '员工人数',
  assetSize: '资产规模',
  asset_size: '资产规模',
  researchSpend: '研发投入',
  research_spend: '研发投入',
  // 税务信息
  taxAmount: '年纳税额',
  tax_amount: '年纳税额',
  taxType: '纳税类型',
  tax_type: '纳税类型',
  taxStatus: '纳税状态',
  tax_status: '纳税状态',
  creditRating: '纳税信用等级',
  credit_rating: '纳税信用等级',
  // 信用评估
  creditScore: '信用评分',
  credit_score: '信用评分',
  violationCount: '违规次数',
  violation_count: '违规次数',
  socialSecurityPaid: '社保缴纳人数',
  social_security_paid: '社保缴纳人数',
  socialInsuranceCnt: '社保人数',
  social_insurance_cnt: '社保人数',
  greenCertified: '绿色认证',
  green_certified: '绿色认证',
  // 个人
  age: '年龄',
  gender: '性别',
  education: '学历',
  occupation: '职业',
  income: '年收入',
  householdType: '户籍类型',
  household_type: '户籍类型',
  idNumber: '身份证号',
  id_number: '身份证号',
  phone: '联系电话',
  address: '居住地址',
  businessStatus: '经营状态',
  business_status: '经营状态',
  disabled: '是否禁用',
  carbonGrade: '碳排放等级',
  carbon_grade: '碳排放等级',
};

// 属性分组配置
const attrGroupConfig = [
  { name: '基本信息', icon: 'Document', keys: ['industry', 'region', 'establishedYear', 'established_year', 'legalPerson', 'legal_person', 'registeredCapital', 'registered_capital', 'businessScope', 'business_scope', 'scale'] },
  { name: '经营状况', icon: 'TrendCharts', keys: ['revenue', 'profit', 'employees', 'assetSize', 'asset_size', 'researchSpend', 'research_spend'] },
  { name: '税务信息', icon: 'Coin', keys: ['taxAmount', 'tax_amount', 'taxType', 'tax_type', 'taxStatus', 'tax_status', 'creditRating', 'credit_rating'] },
  { name: '信用评估', icon: 'Star', keys: ['creditScore', 'credit_score', 'violationCount', 'violation_count', 'socialSecurityPaid', 'social_security_paid', 'socialInsuranceCnt', 'social_insurance_cnt', 'greenCertified', 'green_certified'] },
  { name: '个人信息', icon: 'Avatar', keys: ['age', 'gender', 'education', 'occupation', 'income', 'householdType', 'household_type', 'idNumber', 'id_number', 'phone', 'address'] },
  { name: '经营状态', icon: 'DataAnalysis', keys: ['businessStatus', 'business_status', 'disabled'] },
  { name: '环保信息', icon: 'Sunny', keys: ['carbonGrade', 'carbon_grade'] },
];

function parseJson(str) {
  try { return JSON.parse(str); } catch { return {}; }
}

const portraitAttrs = computed(() => parseJson(portraitTarget.value.attributes || '{}'));

const hasAttributes = computed(() => Object.keys(portraitAttrs.value).length > 0);

// 已匹配到分组的key集合
const displayGroups = computed(() => {
  const attrs = portraitAttrs.value;
  const matchedKeys = new Set();
  const groups = attrGroupConfig.map(g => {
    const items = g.keys
      .filter(k => attrs[k] !== undefined)
      .map(k => {
        matchedKeys.add(k);
        return { key: k, label: attrLabelMap[k] || k, value: attrs[k] };
      });
    return { name: g.name, icon: g.icon, items };
  }).filter(g => g.items.length > 0);

  // 未匹配的属性统一归入"其他属性"
  const unmatched = Object.keys(attrs).filter(k => !matchedKeys.has(k));
  if (unmatched.length > 0) {
    groups.push({
      name: '其他属性',
      icon: 'MoreFilled',
      items: unmatched.map(k => ({ key: k, label: k, value: attrs[k] }))
    });
  }
  return groups;
});

function isNumeric(val) {
  return !isNaN(parseFloat(val)) && isFinite(val);
}

// 属性值中文翻译表（key+value 联合匹配，同一英文值在不同上下文中翻译不同）
const attrValueTranslate = {
  scale: { large: '大型', medium: '中型', small: '小型', micro: '微型' },
  industry: { 'High-tech': '高新技术', 'Manufacturing': '制造业', 'Service': '服务业', 'Retail': '零售业', 'Finance': '金融业', 'Catering': '餐饮业', 'Heavy-industry': '重工业', 'Construction': '建筑业', 'Logistics': '物流业', 'Agriculture': '农业', 'Education': '教育业' },
  tax_status: { normal: '正常纳税', abnormal: '异常', overdue: '欠税', exempt: '免税', audited: '已稽查' },
  tax_type: { general: '一般纳税人', small: '小规模纳税人' },
  credit_rating: { A: 'A级', B: 'B级', C: 'C级', D: 'D级' },
  green_certified: { 'true': '已认证', true: '已认证', 'false': '未认证', false: '未认证' },
  gender: { male: '男', female: '女' },
  education: { highschool: '高中', bachelor: '本科', master: '硕士', doctor: '博士', junior: '初中' },
  household_type: { urban: '城镇', rural: '农村' },
  status: { normal: '正常', active: '活跃', inactive: '停用' },
  business_status: { active: '活跃', inactive: '停用', pending: '待审核', closed: '已注销' },
  carbon_grade: { A: 'A级', B: 'B级', C: 'C级', D: 'D级' },
};

function translateValue(key, val) {
  const table = attrValueTranslate[key];
  if (table && table[val] !== undefined) return table[val];
  // 通用布尔值翻译
  if (val === 'true' || val === true) return '是';
  if (val === 'false' || val === false) return '否';
  return val;
}

function formatAttrValue(val, key) {
  if (val === undefined || val === null || val === '') return '-';
  if (isNumeric(val)) {
    const num = parseFloat(val);
    if (num >= 10000) {
      return (num / 10000).toFixed(2) + ' 万';
    }
    return num.toLocaleString();
  }
  return translateValue(key, val);
}

function handleViewPortrait(row) {
  portraitTarget.value = { ...row };
  portraitOpen.value = true;
}

function handleClosePortrait() {
  portraitOpen.value = false;
}
// ======== 画像抽屉相关 END ========

function getList() {
  loading.value = true;
  listEeTargetData(queryParams.value).then(response => {
    targetDataList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

function handleQuery() { queryParams.value.pageNum = 1; getList(); }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery(); }
function cancel() { open.value = false; reset(); }
function reset() {
  Object.keys(form.value).forEach(k => delete form.value[k]);
  Object.assign(form.value, { targetId: undefined, targetName: undefined, targetType: undefined, identifier: undefined, attributes: undefined, contactPhone: undefined, bankAccount: undefined, status: "0" });
  proxy.resetForm("targetDataRef");
}

function handleAdd() { reset(); open.value = true; title.value = "新增目标数据"; }
function handleUpdate(row) {
  reset();
  getEeTargetData(row.targetId).then(response => {
    Object.assign(form.value, response.data);
    form.value.status = String(form.value.status);
    open.value = true;
    title.value = "修改目标数据";
  });
}

function submitForm() {
  proxy.$refs["targetDataRef"].validate(valid => {
    if (valid) {
      if (form.value.targetId != null) {
        updateEeTargetData(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); });
      } else {
        addEeTargetData(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); });
      }
    }
  });
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除"' + row.targetName + '"？').then(() => {
    return delEeTargetData(row.targetId);
  }).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {});
}

getList();
</script>

<style scoped>
.glass-card { background: rgba(255,255,255,0.7); backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.4); border-radius: 16px; padding: 20px; display: flex; flex-direction: column; box-shadow: 0 8px 32px 0 rgba(31,38,135,0.05); transition: all 0.3s; position: relative; overflow: hidden; }
.glass-card::before { content: ""; position: absolute; top: 0; left: 0; width: 100%; height: 5px; }
.glass-card:hover { transform: translateY(-5px); box-shadow: 0 12px 40px 0 rgba(31,38,135,0.1); }
.stat-primary::before { background: linear-gradient(90deg, #1890ff, #36cfc9); }
.stat-success::before { background: linear-gradient(90deg, #52c41a, #bae637); }
.stat-warning::before { background: linear-gradient(90deg, #fa8c16, #ffd666); }
.stat-info::before { background: linear-gradient(90deg, #722ed1, #efdbff); }
.card-icon { font-size: 24px; width: 44px; height: 44px; border-radius: 10px; display: flex; align-items: center; justify-content: center; margin-bottom: 15px; }
.stat-primary .card-icon { background: rgba(24,144,255,0.1); color: #1890ff; }
.stat-success .card-icon { background: rgba(82,196,26,0.1); color: #52c41a; }
.stat-warning .card-icon { background: rgba(250,140,22,0.1); color: #fa8c16; }
.stat-info .card-icon { background: rgba(114,46,209,0.1); color: #722ed1; }
.card-info { display: flex; flex-direction: column; margin-bottom: 10px; }
.card-label { font-size: 13px; color: #8c8c8c; margin-bottom: 5px; }
.card-value { font-size: 28px; font-weight: bold; color: #262626; font-family: 'Outfit', sans-serif; }
.card-value .unit { font-size: 14px; font-weight: normal; color: #8c8c8c; margin-left: 5px; }
.card-header-flex { display: flex; justify-content: space-between; align-items: center; }
.header-title { font-size: 16px; font-weight: bold; color: #303133; display: flex; align-items: center; gap: 8px; }
.mt20 { margin-top: 20px; }
.mb20 { margin-bottom: 20px; }

/* ========== 画像抽屉样式 ========== */
.portrait-header { display: flex; align-items: center; gap: 16px; }
.portrait-avatar { width: 56px; height: 56px; border-radius: 14px; display: flex; align-items: center; justify-content: center; color: #fff; }
.avatar-enterprise { background: linear-gradient(135deg, #409EFF, #36cfc9); }
.avatar-person { background: linear-gradient(135deg, #e6a23c, #f56c6c); }
.portrait-header-info { flex: 1; }
.portrait-name { margin: 0; font-size: 20px; font-weight: 700; color: #1d2129; }
.portrait-type-tag { display: inline-block; margin-top: 4px; padding: 2px 10px; border-radius: 10px; font-size: 12px; background: #f0f2f5; color: #86909c; }

.portrait-body { padding: 0 4px; }
.portrait-section { margin-bottom: 24px; background: #f7f8fa; border-radius: 12px; padding: 16px; }
.section-title { display: flex; align-items: center; gap: 8px; font-size: 15px; font-weight: 700; color: #1d2129; margin-bottom: 12px; padding-bottom: 10px; border-bottom: 2px solid #e5e6eb; }
.section-title .el-icon { color: #409EFF; }
.section-badge { margin-left: auto; background: #e8f3ff; color: #409EFF; font-size: 12px; padding: 2px 8px; border-radius: 10px; }
.section-items { display: flex; flex-direction: column; gap: 2px; }
.section-item { display: flex; justify-content: space-between; align-items: center; padding: 10px 12px; border-radius: 8px; transition: background 0.2s; }
.section-item:hover { background: rgba(64,158,255,0.04); }
.item-label { font-size: 14px; color: #86909c; min-width: 90px; }
.item-value { font-size: 14px; color: #1d2129; font-weight: 500; text-align: right; word-break: break-all; }
.item-value.value-num { color: #409EFF; font-weight: 600; font-family: 'Consolas', 'Courier New', monospace; }

.portrait-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 300px; color: #c0c4cc; }

.portrait-footer { display: flex; justify-content: flex-end; gap: 12px; }
/* ========== 画像抽屉样式 END ========== */
</style>











