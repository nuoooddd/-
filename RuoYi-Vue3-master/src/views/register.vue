<template>
  <div class="register-page">
    <div class="register-left">
      <div class="left-content">
        <div class="left-badge">政务便民</div>
        <h1 class="left-title">免申即享<br/>智能服务平台</h1>
        <p class="left-desc">从"人找政策"到"政策找人"<br/>零申请 · 零材料 · 零跑腿 · 自动兑付</p>
        <div class="left-features">
          <div class="feature-item" v-for="f in features" :key="f.text">
            <div class="feature-dot"></div>
            <span>{{ f.text }}</span>
          </div>
        </div>
        <div class="left-decoration">
          <div class="deco-ring"></div>
          <div class="deco-ring deco-ring-2"></div>
        </div>
      </div>
    </div>
    <div class="register-right">
      <div class="right-inner">
        <div class="form-header">
          <h2>创建账号</h2>
          <p>请填写以下信息完成注册</p>
        </div>
        <div class="type-switch">
          <div :class="['type-option', { active: registerForm.userType === '1' }]" @click="registerForm.userType = '1'">
            <el-icon :size="18"><OfficeBuilding /></el-icon>
            <span>企业注册</span>
          </div>
          <div :class="['type-option', { active: registerForm.userType === '2' }]" @click="registerForm.userType = '2'">
            <el-icon :size="18"><User /></el-icon>
            <span>个人注册</span>
          </div>
        </div>
        <el-form ref="registerRef" :model="registerForm" :rules="registerRules" class="register-form" label-position="top">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="注册账号" prop="username">
                <el-input v-model="registerForm.username" size="large" placeholder="请输入账号" maxlength="20" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item :label="registerForm.userType === '1' ? '企业名称' : '真实姓名'" prop="nickName">
                <el-input v-model="registerForm.nickName" size="large" :placeholder="registerForm.userType === '1' ? '请输入企业名称' : '请输入姓名'" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="手机号码" prop="phonenumber">
            <el-input v-model="registerForm.phonenumber" size="large" placeholder="请输入手机号" maxlength="11" />
          </el-form-item>

          <div class="required-section">
            <div class="section-header">
              <span class="section-label">实名认证信息</span>
              <span class="section-tag">必填</span>
            </div>
            <div class="section-body">
              <el-form-item label="真实姓名" prop="realName">
                <el-input v-model="registerForm.realName" size="large" placeholder="请输入真实姓名" />
              </el-form-item>
              <el-form-item label="身份证号" prop="idCard">
                <el-input v-model="registerForm.idCard" size="large" placeholder="请输入身份证号" maxlength="18" />
              </el-form-item>
              <el-form-item label="身份证照片" prop="idCardFront" :rules="[{ required: true, message: '请上传身份证人像面', trigger: 'change' }]">
                <div class="id-card-upload-row">
                  <div class="upload-item">
                    <el-upload
                      class="card-uploader"
                      :action="uploadUrl"
                      :headers="uploadHeaders"
                      :show-file-list="false"
                      :on-success="handleFrontSuccess"
                      :before-upload="beforeUpload"
                      accept="image/*"
                    >
                      <img v-if="registerForm.idCardFront" :src="getImgUrl(registerForm.idCardFront)" class="card-preview" />
                      <div v-else class="card-placeholder">
                        <el-icon :size="20"><Plus /></el-icon>
                        <span>人像面</span>
                      </div>
                    </el-upload>
                  </div>
                  <div class="upload-item">
                    <el-upload
                      class="card-uploader"
                      :action="uploadUrl"
                      :headers="uploadHeaders"
                      :show-file-list="false"
                      :on-success="handleBackSuccess"
                      :before-upload="beforeUpload"
                      accept="image/*"
                    >
                      <img v-if="registerForm.idCardBack" :src="getImgUrl(registerForm.idCardBack)" class="card-preview" />
                      <div v-else class="card-placeholder">
                        <el-icon :size="20"><Plus /></el-icon>
                        <span>国徽面</span>
                      </div>
                    </el-upload>
                  </div>
                </div>
              </el-form-item>

            </div>
          </div>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="登录密码" prop="password">
                <el-input v-model="registerForm.password" type="password" size="large" placeholder="请输入密码" show-password />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="registerForm.confirmPassword" type="password" size="large" placeholder="请再次输入密码" show-password @keyup.enter="handleRegister" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item style="margin-top:8px;">
            <el-button :loading="loading" size="large" type="primary" class="submit-btn" @click.prevent="handleRegister">
              {{ loading ? '注册中...' : '立即注册' }}
            </el-button>
          </el-form-item>
        </el-form>
        <div class="form-footer">
          <span>已有账号？</span>
          <router-link to="/login" class="login-link">立即登录</router-link>
        </div>
      </div>
      <div class="copyright">Copyright &copy; 2026 免申即享政务便民系统</div>
    </div>
  </div>
</template>

<script setup>
import { register } from "@/api/login";
import { ElMessage } from "element-plus";
import { OfficeBuilding, User, Plus, ArrowRight } from "@element-plus/icons-vue";

const router = useRouter();
const { proxy } = getCurrentInstance();

const baseUrl = import.meta.env.VITE_APP_BASE_API;
const uploadUrl = ref(baseUrl + "/common/upload");
const uploadHeaders = ref({});

const registerForm = ref({
  userType: "1",
  username: "",
  nickName: "",
  phonenumber: "",
  idCard: "",
  realName: "",
  idCardFront: "",
  idCardBack: "",

  password: "",
  confirmPassword: ""
});

const features = [
  { text: '智能匹配，政策精准推送' },
  { text: '零材料申报，自动比对审核' },
  { text: '资金秒级直达，免审兑付' },
  { text: '全流程闭环，安全可追溯' }
];

const equalToPassword = (rule, value, callback) => {
  if (registerForm.value.password !== value) { callback(new Error("两次输入的密码不一致")); } else { callback(); }
};

const idCardValidator = (rule, value, callback) => {
  if (!value) { callback(new Error("请输入身份证号")); return; }
  const reg = /^\d{17}[\dXx]$/;
  if (!reg.test(value)) { callback(new Error("身份证号格式不正确")); return; }
  const weight = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];
  const checkCode = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'];
  let sum = 0;
  for (let i = 0; i < 17; i++) { sum += parseInt(value.charAt(i)) * weight[i]; }
  if (checkCode[sum % 11] !== value.charAt(17).toUpperCase()) { callback(new Error("身份证号校验码不正确")); return; }
  callback();
};

const registerRules = {
  username: [
    { required: true, trigger: "blur", message: "请输入注册账号" },
    { min: 2, max: 20, message: "账号长度在2到20个字符", trigger: "blur" }
  ],
  nickName: [{ required: true, trigger: "blur", message: "请输入名称" }],
  phonenumber: [
    { required: true, trigger: "blur", message: "请输入手机号" },
    { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号码", trigger: "blur" }
  ],
  realName: [{ required: true, trigger: "blur", message: "请输入真实姓名" }],
  idCard: [{ required: true, validator: idCardValidator, trigger: "blur" }],
  password: [
    { required: true, trigger: "blur", message: "请输入密码" },
    { min: 5, max: 20, message: "密码长度在5到20个字符", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, trigger: "blur", message: "请确认密码" },
    { validator: equalToPassword, trigger: "blur" }
  ]
};

const loading = ref(false);

function beforeUpload(file) {
  const isImage = file.type.startsWith("image/");
  const isLt5M = file.size / 1024 / 1024 < 5;
  if (!isImage) { ElMessage.error("只能上传图片文件"); return false; }
  if (!isLt5M) { ElMessage.error("图片大小不能超过5MB"); return false; }
  return true;
}

function handleFrontSuccess(res) {
  if (res.code === 200) { registerForm.value.idCardFront = res.fileName; }
  else { ElMessage.error(res.msg); }
}

function handleBackSuccess(res) {
  if (res.code === 200) { registerForm.value.idCardBack = res.fileName; }
  else { ElMessage.error(res.msg); }
}

function getImgUrl(path) {
  if (!path) return "";
  if (path.startsWith("http")) return path;
  return baseUrl + path;
}

function handleRegister() {
  proxy.$refs.registerRef.validate(valid => {
    if (valid) {
      loading.value = true;
      register(registerForm.value).then(res => {
        ElMessage.success(res.msg || "注册成功");
        router.push("/login");
      }).catch(() => { loading.value = false; });
    }
  });
}
</script>

<style lang="scss" scoped>
.register-page {
  display: flex; min-height: 100vh; background: #fafafa;
  font-family: "Plus Jakarta Sans", "PingFang SC", "Microsoft YaHei UI", sans-serif;
}

.register-left {
  flex: 0 0 42%; background: #0f172a; position: relative; overflow: hidden;
  display: flex; align-items: center; justify-content: center;
}
.left-content {
  position: relative; z-index: 2; padding: 60px 48px; max-width: 440px;
}
.left-badge {
  display: inline-block; padding: 4px 14px; border-radius: 6px;
  background: rgba(0, 82, 255, 0.15); color: #4d7cff; font-size: 12px;
  font-weight: 600; letter-spacing: 1px; margin-bottom: 24px;
}
.left-title {
  font-size: 36px; font-weight: 700; color: #f1f5f9; line-height: 1.3;
  margin: 0 0 16px 0; letter-spacing: -0.5px;
}
.left-desc {
  font-size: 15px; color: #64748b; line-height: 1.7; margin: 0 0 36px 0;
}
.left-features { display: flex; flex-direction: column; gap: 14px; }
.feature-item {
  display: flex; align-items: center; gap: 12px;
  font-size: 14px; color: #94a3b8;
}
.feature-dot {
  width: 6px; height: 6px; border-radius: 50%; background: #0052ff; flex-shrink: 0;
}
.left-decoration {
  position: absolute; inset: 0; pointer-events: none;
}
.deco-ring {
  position: absolute; width: 500px; height: 500px; border-radius: 50%;
  border: 1px solid rgba(0, 82, 255, 0.08);
  top: 50%; left: 50%; transform: translate(-50%, -50%);
}
.deco-ring-2 {
  width: 700px; height: 700px; border-color: rgba(0, 82, 255, 0.04);
}

.register-right {
  flex: 1; display: flex; flex-direction: column; align-items: center;
  justify-content: center; padding: 40px; position: relative; overflow-y: auto;
}
.right-inner {
  width: 100%; max-width: 480px;
}
.form-header {
  margin-bottom: 28px;
  h2 { font-size: 24px; font-weight: 700; color: #0f172a; margin: 0 0 6px 0; }
  p { font-size: 14px; color: #64748b; margin: 0; }
}

.type-switch {
  display: flex; gap: 8px; margin-bottom: 24px;
  background: #f1f5f9; border-radius: 10px; padding: 4px;
}
.type-option {
  flex: 1; display: flex; align-items: center; justify-content: center; gap: 6px;
  padding: 10px 0; border-radius: 8px; font-size: 14px; font-weight: 500;
  color: #64748b; cursor: pointer; transition: all 0.2s; user-select: none;
}
.type-option.active {
  background: #fff; color: #0f172a; box-shadow: 0 1px 3px rgba(0,0,0,0.08), 0 1px 2px rgba(0,0,0,0.06);
}

.register-form {
  :deep(.el-form-item__label) {
    font-size: 13px; font-weight: 500; color: #334155; padding-bottom: 4px;
  }
  :deep(.el-input__wrapper) {
    border-radius: 8px; box-shadow: 0 0 0 1px #e2e8f0 inset;
    transition: box-shadow 0.2s;
  }
  :deep(.el-input__wrapper:hover) {
    box-shadow: 0 0 0 1px #cbd5e1 inset;
  }
  :deep(.el-input__wrapper.is-focus) {
    box-shadow: 0 0 0 1px #0052ff inset, 0 0 0 3px rgba(0, 82, 255, 0.1);
  }
  :deep(.el-form-item) { margin-bottom: 18px; }
}

.required-section {
  background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 10px;
  margin-bottom: 18px; overflow: hidden;
}
.section-header {
  display: flex; align-items: center; gap: 8px; padding: 12px 16px;
  background: #f1f5f9; border-bottom: 1px solid #e2e8f0;
}
.section-label { font-size: 13px; font-weight: 600; color: #0f172a; }
.section-tag {
  font-size: 11px; padding: 1px 8px; border-radius: 4px;
  background: #0052ff; color: #fff; font-weight: 500;
}
.section-body { padding: 16px; }

.id-card-upload-row { display: flex; gap: 12px; width: 100%; }
.upload-item { flex: 1; }
.card-uploader {
  :deep(.el-upload) {
    width: 100%; height: 100px; border-radius: 8px;
    border: 1.5px dashed #cbd5e1; background: #fff;
    display: flex; align-items: center; justify-content: center;
    cursor: pointer; transition: all 0.2s; overflow: hidden;
    &:hover { border-color: #0052ff; background: #f8fafc; }
  }
}
.card-placeholder {
  display: flex; flex-direction: column; align-items: center; gap: 4px;
  color: #94a3b8; .el-icon { color: #cbd5e1; } span { font-size: 12px; }
}
.card-preview { width: 100%; height: 100px; object-fit: cover; border-radius: 8px; }


.submit-btn {
  width: 100%; height: 44px; border-radius: 8px; font-size: 15px; font-weight: 600;
  background: #0052ff; border-color: #0052ff; letter-spacing: 1px;
  &:hover { background: #0044dd; border-color: #0044dd; }
  &:active { transform: scale(0.99); }
}

.form-footer {
  text-align: center; margin-top: 16px; font-size: 14px; color: #64748b;
}
.login-link {
  color: #0052ff; font-weight: 500; text-decoration: none;
  &:hover { text-decoration: underline; }
}
.copyright {
  position: absolute; bottom: 20px; text-align: center; width: 100%;
  font-size: 12px; color: #94a3b8; letter-spacing: 0.5px;
}

@media (max-width: 900px) {
  .register-left { display: none; }
  .register-right { padding: 32px 24px; }
}
</style>
