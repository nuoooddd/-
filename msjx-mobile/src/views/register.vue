<template>
  <div class="m-register">
    <div class="reg-header">
      <h1 class="reg-title">注册账号</h1>
      <p class="reg-subtitle">免申即享 · 政务便民服务平台</p>
    </div>

    <div class="reg-form-wrap">
      <el-form ref="registerRef" :model="registerForm" :rules="registerRules" label-position="top">
        <div class="type-tabs">
          <div :class="['tab', { active: registerForm.userType === '1' }]" @click="registerForm.userType = '1'">企业注册</div>
          <div :class="['tab', { active: registerForm.userType === '2' }]" @click="registerForm.userType = '2'">个人注册</div>
        </div>

        <div class="field">
          <el-form-item label="注册账号" prop="username">
            <el-input v-model="registerForm.username" placeholder="请输入注册账号" />
          </el-form-item>
        </div>
        <div class="field">
          <el-form-item :label="registerForm.userType === '1' ? '企业名称' : '真实姓名'" prop="nickName">
            <el-input v-model="registerForm.nickName" :placeholder="registerForm.userType === '1' ? '请输入企业名称' : '请输入真实姓名'" />
          </el-form-item>
        </div>
        <div class="field">
          <el-form-item label="手机号码" prop="phonenumber">
            <el-input v-model="registerForm.phonenumber" placeholder="请输入手机号" maxlength="11" />
          </el-form-item>
        </div>
        <div class="field">
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="registerForm.realName" placeholder="请输入真实姓名" />
          </el-form-item>
        </div>
        <div class="field">
          <el-form-item label="身份证号" prop="idCard">
            <el-input v-model="registerForm.idCard" placeholder="请输入身份证号" maxlength="18" />
          </el-form-item>
        </div>
        <div class="field">
          <el-form-item label="身份证照片">
            <div class="id-upload-row">
              <el-upload :action="uploadUrl" :headers="uploadHeaders" :show-file-list="false" :on-success="handleFrontSuccess" :before-upload="beforeUpload" accept="image/*">
                <img v-if="registerForm.idCardFront" :src="getImgUrl(registerForm.idCardFront)" class="id-preview" />
                <div v-else class="id-placeholder"><el-icon><Plus /></el-icon><span>人像面</span></div>
              </el-upload>
              <el-upload :action="uploadUrl" :headers="uploadHeaders" :show-file-list="false" :on-success="handleBackSuccess" :before-upload="beforeUpload" accept="image/*">
                <img v-if="registerForm.idCardBack" :src="getImgUrl(registerForm.idCardBack)" class="id-preview" />
                <div v-else class="id-placeholder"><el-icon><Plus /></el-icon><span>国徽面</span></div>
              </el-upload>
            </div>
          </el-form-item>
        </div>
        <div class="field">
          <el-form-item label="登录密码" prop="password">
            <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" show-password />
          </el-form-item>
        </div>
        <div class="field">
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password @keyup.enter="handleRegister" />
          </el-form-item>
        </div>

        <button class="reg-btn" @click="handleRegister" :disabled="loading">{{ loading ? '注册中...' : '立即注册' }}</button>
      </el-form>

      <div class="reg-footer">
        <span>已有账号？</span>
        <router-link to="/login" class="link">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { register } from "@/api/login"
import { ElMessage } from "element-plus"
import { Plus } from "@element-plus/icons-vue"

const router = useRouter()
const { proxy } = getCurrentInstance()

const baseUrl = import.meta.env.VITE_APP_BASE_API
const uploadUrl = ref(baseUrl + "/common/upload")
const uploadHeaders = ref({})

const registerForm = ref({
  userType: "1", username: "", nickName: "", phonenumber: "",
  idCard: "", realName: "", idCardFront: "", idCardBack: "",
  password: "", confirmPassword: ""
})

const equalToPassword = (rule, value, callback) => {
  if (registerForm.value.password !== value) { callback(new Error("两次输入的密码不一致")) } else { callback() }
}
const idCardValidator = (rule, value, callback) => {
  if (!value) { callback(new Error("请输入身份证号")); return }
  const reg = /^\d{17}[\dXx]$/
  if (!reg.test(value)) { callback(new Error("身份证号格式不正确")); return }
  const weight = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
  const checkCode = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2']
  let sum = 0
  for (let i = 0; i < 17; i++) { sum += parseInt(value.charAt(i)) * weight[i] }
  if (checkCode[sum % 11] !== value.charAt(17).toUpperCase()) { callback(new Error("身份证号校验码不正确")); return }
  callback()
}

const registerRules = {
  username: [{ required: true, trigger: "blur", message: "请输入注册账号" }, { min: 2, max: 20, message: "账号长度在2到20个字符", trigger: "blur" }],
  nickName: [{ required: true, trigger: "blur", message: "请输入名称" }],
  phonenumber: [{ required: true, trigger: "blur", message: "请输入手机号" }, { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号码", trigger: "blur" }],
  realName: [{ required: true, trigger: "blur", message: "请输入真实姓名" }],
  idCard: [{ required: true, validator: idCardValidator, trigger: "blur" }],
  password: [{ required: true, trigger: "blur", message: "请输入密码" }, { min: 5, max: 20, message: "密码长度在5到20个字符", trigger: "blur" }],
  confirmPassword: [{ required: true, trigger: "blur", message: "请确认密码" }, { validator: equalToPassword, trigger: "blur" }]
}

const loading = ref(false)

function beforeUpload(file) {
  const isImage = file.type.startsWith("image/")
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) { ElMessage.error("只能上传图片文件"); return false }
  if (!isLt5M) { ElMessage.error("图片大小不能超过5MB"); return false }
  return true
}

function handleFrontSuccess(res) { if (res.code === 200) { registerForm.value.idCardFront = res.fileName } else { ElMessage.error(res.msg) } }
function handleBackSuccess(res) { if (res.code === 200) { registerForm.value.idCardBack = res.fileName } else { ElMessage.error(res.msg) } }

function getImgUrl(path) {
  if (!path) return ""
  if (path.startsWith("http")) return path
  return baseUrl + path
}

function handleRegister() {
  proxy.$refs.registerRef.validate(valid => {
    if (valid) {
      loading.value = true
      register(registerForm.value).then(res => {
        ElMessage.success(res.msg || "注册成功")
        router.push("/login")
      }).catch(() => { loading.value = false })
    }
  })
}
</script>

<style scoped>
.m-register {
  min-height: 100vh; background: #f5f6fa; padding: 0 20px;
  max-width: 430px; margin: 0 auto;
  box-shadow: 0 0 40px rgba(0,0,0,0.12);
}
.reg-header { text-align: center; padding: 40px 0 20px; }
.reg-title { font-size: 22px; font-weight: 700; color: #1a3a5c; margin: 0 0 6px; }
.reg-subtitle { font-size: 13px; color: #999; margin: 0; }

.reg-form-wrap {
  background: #fff; border-radius: 16px; padding: 24px 18px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.06);
}

.type-tabs { display: flex; margin-bottom: 20px; border: 1px solid #e8eaed; border-radius: 8px; overflow: hidden; }
.tab { flex: 1; text-align: center; padding: 10px 0; font-size: 14px; color: #666; cursor: pointer; background: #fafafa; transition: all 0.2s; }
.tab.active { background: #fff; color: #1a3a5c; font-weight: 600; }

.field { margin-bottom: 4px; }

.id-upload-row { display: flex; gap: 12px; }
.id-upload-row .el-upload { flex: 1; }
.id-preview { width: 100%; height: 80px; object-fit: cover; border-radius: 8px; }
.id-placeholder {
  width: 100%; height: 80px; border: 1px dashed #dcdfe6; border-radius: 8px;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 4px; color: #999; font-size: 12px; cursor: pointer;
}

.reg-btn {
  width: 100%; height: 44px; border: none; border-radius: 10px;
  background: #1a3a5c; color: #fff; font-size: 15px; font-weight: 600;
  letter-spacing: 2px; cursor: pointer; margin-top: 12px;
}
.reg-btn:active { background: #1e4d7b; }
.reg-btn:disabled { background: #a0c4e8; cursor: not-allowed; }

.reg-footer { text-align: center; margin-top: 16px; font-size: 13px; color: #999; }
.link { color: #1a3a5c; font-weight: 500; text-decoration: none; }
</style>
