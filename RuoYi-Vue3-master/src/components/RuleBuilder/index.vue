<template>
  <div class="rule-builder">
    <div class="builder-toolbar">
      <div class="mode-tabs">
        <span :class="['mode-tab', { active: mode === 'visual' }]" @click="switchMode('visual')">可视化配置</span>
        <span :class="['mode-tab', { active: mode === 'code' }]" @click="switchMode('code')">代码模式</span>
      </div>
    </div>

    <!-- 可视化模式 -->
    <div v-if="mode === 'visual'" class="visual-mode">
      <div class="condition-rows">
        <div v-for="(cond, index) in conditions" :key="index" class="condition-row">
          <!-- AND/OR 连接符 -->
          <div v-if="index > 0" class="connector-row">
            <el-select v-model="cond.connector" size="small" style="width: 80px;">
              <el-option label="并且" value="and" />
              <el-option label="或者" value="or" />
            </el-select>
          </div>

          <div class="cond-fields">
            <!-- 属性选择 -->
            <el-select v-model="cond.field" placeholder="选择属性" filterable style="width: 180px;" @change="updateExpr">
              <el-option-group v-for="group in attrGroups" :key="group.name" :label="group.name">
                <el-option v-for="attr in group.attrs" :key="attr.key" :label="attr.label" :value="attr.key" />
              </el-option-group>
            </el-select>

            <!-- 操作符 -->
            <el-select v-model="cond.op" style="width: 90px;" @change="updateExpr">
              <el-option label="等于" value="==" />
              <el-option label="不等于" value="!=" />
              <el-option label="大于等于" value=">=" />
              <el-option label="小于等于" value="<=" />
              <el-option label="大于" value=">" />
              <el-option label="小于" value="<" />
            </el-select>

            <!-- 值输入 -->
            <el-select v-if="cond.field === 'industry'" v-model="cond.value" placeholder="选择行业" style="width: 160px;" @change="updateExpr" clearable filterable allow-create>
              <el-option label="高新技术" value="High-tech" />
              <el-option label="制造业" value="Manufacturing" />
              <el-option label="餐饮业" value="Catering" />
              <el-option label="重工业" value="Heavy-industry" />
              <el-option label="服务业" value="Service" />
              <el-option label="零售业" value="Retail" />
              <el-option label="金融业" value="Finance" />
              <el-option label="建筑业" value="Construction" />
              <el-option label="物流业" value="Logistics" />
            </el-select>
            <el-select v-else-if="cond.field === 'scale'" v-model="cond.value" placeholder="选择规模" style="width: 140px;" @change="updateExpr" clearable filterable allow-create>
              <el-option label="大型" value="large" />
              <el-option label="中型" value="medium" />
              <el-option label="小型" value="small" />
              <el-option label="微型" value="micro" />
            </el-select>
            <el-select v-else-if="cond.field === 'tax_status'" v-model="cond.value" placeholder="选择状态" style="width: 140px;" @change="updateExpr" clearable filterable allow-create>
              <el-option label="正常纳税" value="normal" />
              <el-option label="异常" value="abnormal" />
              <el-option label="欠税" value="overdue" />
              <el-option label="免税" value="exempt" />
            </el-select>
            <el-select v-else-if="cond.field === 'credit_rating'" v-model="cond.value" placeholder="选择等级" style="width: 120px;" @change="updateExpr" clearable filterable allow-create>
              <el-option label="A级" value="A" />
              <el-option label="B级" value="B" />
              <el-option label="C级" value="C" />
              <el-option label="D级" value="D" />
            </el-select>
            <el-select v-else-if="cond.field === 'high_tech'" v-model="cond.value" placeholder="选择" style="width: 120px;" @change="updateExpr" clearable>
              <el-option label="是" value="true" />
              <el-option label="否" value="false" />
            </el-select>
            <el-select v-else-if="cond.field === 'high_tech'" v-model="cond.value" placeholder="选择" style="width: 120px;" @change="updateExpr" clearable>
              <el-option label="是" value="true" />
              <el-option label="否" value="false" />
            </el-select>
                        <el-select v-else-if="cond.field === 'green_certified'" v-model="cond.value" placeholder="选择" style="width: 120px;" @change="updateExpr" clearable>
              <el-option label="是" value="true" />
              <el-option label="否" value="false" />
            </el-select>
            <el-input v-else v-model="cond.value" :placeholder="cond.op === '==' ? '输入值，文本加引号' : '输入数值'" style="width: 180px;" @input="updateExpr" />

            <!-- 删除按钮 -->
            <el-button type="danger" :icon="Delete" circle size="small" @click="removeCondition(index)" v-if="conditions.length > 1" />
          </div>
        </div>
      </div>

      <el-button type="primary" plain icon="Plus" @click="addCondition" style="margin-top: 12px; border-radius: 8px;">
        添加条件
      </el-button>

      <!-- 表达式预览 -->
      <div class="expr-preview" v-if="expression">
        <span class="preview-label">生成表达式：</span>
        <code class="preview-code">{{ expression }}</code>
      </div>
    </div>

    <!-- 代码模式 -->
    <div v-else class="code-mode">
      <el-input v-model="expression" type="textarea" :rows="3" placeholder="例: industry == 'High-tech' and research_spend >= 1000000" @input="onCodeInput" />
      <div class="expr-hint">
        <el-icon><InfoFilled /></el-icon>
        支持 ==, !=, >=, <=, >, < 连接 and / or 逻辑
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Delete, Plus, InfoFilled } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])

const mode = ref('visual')
const expression = ref('')

// 属性分组（使用中文标签）
const attrGroups = [
  { name: '基本信息', attrs: [
    { key: 'industry', label: '所属行业' },
    { key: 'scale', label: '企业规模' },
    { key: 'registered_capital', label: '注册资本' },
    { key: 'high_tech', label: '高新技术企业' },
    { key: 'business_status', label: '经营状态' },
  ]},
  { name: '经营状况', attrs: [
    { key: 'revenue', label: '年营收' },
    { key: 'research_spend', label: '研发投入' },
    { key: 'growth_rate', label: '年增长率(%)' },
    { key: 'disabled', label: '残疾人员工数' },
  ]},
  { name: '税务信用', attrs: [
    { key: 'tax_status', label: '纳税状态' },
    { key: 'credit_score', label: '信用评分' },
    { key: 'green_certified', label: '绿色认证' },
    { key: 'social_insurance_cnt', label: '社保人数' },
    { key: 'carbon_grade', label: '碳排放等级' },
  ]},
  { name: '个人信息', attrs: [
    { key: 'age', label: '年龄' },
  ]},
]

const conditions = ref([])

// 监听外部值变化，同步到内部状态
watch(() => props.modelValue, (val) => {
  expression.value = val || ''
  if (mode.value === 'visual') {
    parseExpression(val || '')
  }
}, { immediate: true })

// 监听模式切换
watch(mode, (newMode, oldMode) => {
  if (newMode === 'code' && oldMode === 'visual') {
    expression.value = props.modelValue || ''
  } else if (newMode === 'visual' && oldMode === 'code') {
    // 代码模式输入的内容先同步到外部，再解析
    if (expression.value !== props.modelValue) {
      emit('update:modelValue', expression.value)
    }
    parseExpression(expression.value || props.modelValue || '')
  }
})

function addCondition() {
  conditions.value.push({ field: '', op: '==', value: '', connector: 'and' })
}

function removeCondition(index) {
  conditions.value.splice(index, 1)
  updateExpr()
}

function updateExpr() {
  const parts = []
  for (let i = 0; i < conditions.value.length; i++) {
    const c = conditions.value[i]
    if (!c.field || c.value === '') continue
    let val = c.value
    const isNum = !isNaN(val) && val !== '' && val !== 'true' && val !== 'false'
    const isBool = val === 'true' || val === 'false'
    if (!isNum && !isBool) {
      val = "'" + val + "'"
    }
    if (i > 0) {
      parts.push(c.connector)
    }
    parts.push(c.field + ' ' + c.op + ' ' + val)
  }
  expression.value = parts.join(' ')
  emit('update:modelValue', expression.value)
}

function onCodeInput() {
  emit('update:modelValue', expression.value)
}

function switchMode(newMode) {
  if (newMode === mode.value) return
  if (newMode === 'code') {
    // 同步当前表达式到代码框
    expression.value = props.modelValue || ''
  } else {
    // 切换到可视化，先同步代码内容到外部
    if (expression.value && expression.value !== props.modelValue) {
      emit('update:modelValue', expression.value)
    }
  }
  mode.value = newMode
}

// 解析表达式 → 可视化
function parseExpression(expr) {
  conditions.value = []
  if (!expr || !expr.trim()) {
    conditions.value.push({ field: '', op: '==', value: '', connector: 'and' })
    return
  }

  // 按 or 或 and 分割
  const tokens = tokenize(expr)
  for (const token of tokens) {
    const match = token.cond.match(/^(\w+)\s*(==|!=|>=|<=|>|<)\s*(.+)$/)
    if (match) {
      let val = match[3].trim()
      // 去掉引号
      if ((val.startsWith("'") && val.endsWith("'")) || (val.startsWith('"') && val.endsWith('"'))) {
        val = val.slice(1, -1)
      }
      conditions.value.push({
        field: match[1],
        op: match[2],
        value: val,
        connector: token.connector || 'and'
      })
    }
  }

  if (conditions.value.length === 0) {
    conditions.value.push({ field: '', op: '==', value: '', connector: 'and' })
  }
}

// 简单分词：按 and/or 分割
function tokenize(expr) {
  const tokens = []
  const regex = /\s+(and|or)\s+/gi
  let lastIdx = 0
  let match
  while ((match = regex.exec(expr)) !== null) {
    tokens.push({ cond: expr.substring(lastIdx, match.index).trim(), connector: match[1].toLowerCase() })
    lastIdx = regex.lastIndex
  }
  tokens.push({ cond: expr.substring(lastIdx).trim(), connector: null })
  return tokens
}
</script>

<style scoped>
.rule-builder { border: 1px solid #e4e7ed; border-radius: 8px; padding: 16px; background: #fafafa; }
.builder-toolbar { margin-bottom: 16px; }
.mode-tabs { display: flex; border-bottom: 2px solid #e4e7ed; }
.mode-tab { padding: 8px 20px; cursor: pointer; color: #909399; font-size: 14px; border-bottom: 2px solid transparent; margin-bottom: -2px; transition: all 0.2s; user-select: none; }
.mode-tab:hover { color: #409EFF; }
.mode-tab.active { color: #409EFF; border-bottom-color: #409EFF; font-weight: 600; }
.condition-rows { display: flex; flex-direction: column; gap: 8px; }
.condition-row { display: flex; flex-direction: column; }
.connector-row { margin: 4px 0 4px 60px; }
.connector-row .el-select { --el-select-width: 80px; }
.cond-fields { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.expr-preview { margin-top: 14px; padding: 10px 14px; background: #ecf5ff; border-radius: 6px; display: flex; align-items: center; gap: 10px; }
.preview-label { font-size: 12px; color: #409EFF; white-space: nowrap; font-weight: 600; }
.preview-code { font-family: 'Consolas', 'Courier New', monospace; font-size: 13px; color: #303133; word-break: break-all; }
.expr-hint { margin-top: 6px; font-size: 12px; color: #909399; display: flex; align-items: center; gap: 4px; }
</style>



