<template>
  <div class="login-page">
    <div class="login-left">
      <div class="left-content">
        <div class="left-emblem">
          <svg viewBox="0 0 40 40" width="40" height="40"><circle cx="20" cy="20" r="18" fill="none" stroke="rgba(255,255,255,0.7)" stroke-width="1.5"/><path d="M20 8 L20 32 M12 16 L20 8 L28 16" fill="none" stroke="rgba(255,255,255,0.9)" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </div>
        <h1 class="left-title">免申即享</h1>
        <p class="left-subtitle">政务便民服务平台</p>
        <div class="left-divider"></div>
        <p class="left-desc">从"人找政策"到"政策找人"<br/>零申请 / 零材料 / 零跑腿 / 自动兑付</p>
        <div class="left-features">
          <div class="feature-item" v-for="f in features" :key="f">
            <span class="feature-dot"></span>
            <span>{{ f }}</span>
          </div>
        </div>
      </div>
      <div class="left-footer">市政务服务管理中心</div>
    </div>
    <div class="login-right">
      <div class="right-inner">
        <h2 class="form-title">用户登录</h2>
        <p class="form-desc">请输入账号密码登录系统</p>
        <div class="type-switch">
          <div :class="['type-option', { active: loginType === 'pwd' }]" @click="switchLoginType('pwd')">
            <el-icon :size="14"><Key /></el-icon>
            <span>密码登录</span>
          </div>
          <div :class="['type-option', { active: loginType === 'sms' }]" @click="switchLoginType('sms')">
            <el-icon :size="14"><Iphone /></el-icon>
            <span>验证码登录</span>
          </div>
        </div>

        <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="login-form" label-position="top">
          <template v-if="loginType === 'pwd'">
            <el-form-item label="账号" prop="username">
              <el-input v-model="loginForm.username" size="large" placeholder="请输入账号" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="loginForm.password" type="password" size="large" placeholder="请输入密码" show-password @keyup.enter="handleLogin" />
            </el-form-item>
            <el-form-item v-if="captchaEnabled" label="验证码" prop="code">
              <div class="code-row">
                <el-input v-model="loginForm.code" size="large" placeholder="请输入验证码" @keyup.enter="handleLogin" />
                <img :src="codeUrl" @click="getCode" class="code-img" alt="验证码" />
              </div>
            </el-form-item>
            <div class="login-options">
              <el-checkbox v-model="loginForm.rememberMe">记住密码</el-checkbox>
            </div>
          </template>
          <template v-else>
            <el-form-item label="手机号码" prop="phonenumber">
              <el-input v-model="loginForm.phonenumber" size="large" placeholder="请输入手机号" maxlength="11" />
            </el-form-item>
            <el-form-item label="验证码" prop="smsCode">
              <div class="code-row">
                <el-input v-model="loginForm.smsCode" size="large" placeholder="请输入验证码" maxlength="6" @keyup.enter="handleLogin" />
                <el-button size="large" :disabled="smsCountdown > 0" @click="handleSendSms" class="sms-btn">
                  {{ smsCountdown > 0 ? `${smsCountdown}s` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
          </template>
          <el-form-item>
            <el-button :loading="loading" size="large" type="primary" class="submit-btn" @click.prevent="handleLogin">
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          <span>还没有账号？</span>
          <router-link to="/register" class="register-link">立即注册</router-link>
        </div>
      </div>
      <div class="copyright">Copyright &copy; 2026 免申即享政务便民系统</div>
    </div>
  </div>
</template>

<script setup>
import { getCodeImg, sendSmsCode } from "@/api/login";
import Cookies from "js-cookie";
import { encrypt, decrypt } from "@/utils/jsencrypt";
import useUserStore from '@/store/modules/user'
import { Key, Iphone } from '@element-plus/icons-vue'

const userStore = useUserStore()
const route = useRoute();
const router = useRouter();
const { proxy } = getCurrentInstance();

const loginType = ref("pwd");

const features = [
  '智能匹配，政策精准推送',
  '零材料申报，自动比对审核',
  '资金直达，免审兑付',
  '全流程闭环，安全可追溯'
];

const loginForm = ref({
  username: "admin",
  password: "admin123",
  rememberMe: false,
  code: "",
  uuid: "",
  phonenumber: "",
  smsCode: ""
});

const loginRules = computed(() => {
  if (loginType.value === 'pwd') {
    const rules = {
      username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
      password: [{ required: true, trigger: "blur", message: "请输入您的密码" }]
    };
    if (captchaEnabled.value) {
      rules.code = [{ required: true, trigger: "change", message: "请输入验证码" }];
    }
    return rules;
  } else {
    return {
      phonenumber: [
        { required: true, trigger: "blur", message: "请输入手机号" },
        { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号", trigger: "blur" }
      ],
      smsCode: [
        { required: true, trigger: "blur", message: "请输入验证码" },
        { len: 6, message: "验证码为6位数字", trigger: "blur" }
      ]
    };
  }
});

const codeUrl = ref("");
const loading = ref(false);
const captchaEnabled = ref(true);
const redirect = ref(undefined);
const smsCountdown = ref(0);
let smsTimer = null;

watch(route, (newRoute) => {
    redirect.value = newRoute.query && newRoute.query.redirect;
}, { immediate: true });

function switchLoginType(type) {
  loginType.value = type;
}

function handleLogin() {
  proxy.$refs.loginRef.validate(valid => {
    if (valid) {
      loading.value = true;
      if (loginType.value === 'pwd') {
        if (loginForm.value.rememberMe) {
          Cookies.set("username", loginForm.value.username, { expires: 30 });
          Cookies.set("password", encrypt(loginForm.value.password), { expires: 30 });
          Cookies.set("rememberMe", loginForm.value.rememberMe, { expires: 30 });
        } else {
          Cookies.remove("username");
          Cookies.remove("password");
          Cookies.remove("rememberMe");
        }
        userStore.login(loginForm.value).then(() => {
          userStore.getInfo().then(() => {
            const roles = userStore.roles;
            let homePath = '/dashboard';
            if (roles.includes('admin')) homePath = '/dashboard';
            else if (roles.includes('enterprise')) homePath = '/portal/enterpriseHome';
            else if (roles.includes('person')) homePath = '/personPortal/personHome';
            window.location.href = homePath
          });
        }).catch(() => {
          loading.value = false;
          if (captchaEnabled.value) { getCode(); }
        });
      } else {
        userStore.login({
          loginType: 'sms',
          phonenumber: loginForm.value.phonenumber,
          smsCode: loginForm.value.smsCode
        }).then(() => {
          userStore.getInfo().then(() => {
            const roles = userStore.roles;
            let homePath = '/dashboard';
            if (roles.includes('admin')) homePath = '/dashboard';
            else if (roles.includes('enterprise')) homePath = '/portal/enterpriseHome';
            else if (roles.includes('person')) homePath = '/personPortal/personHome';
            window.location.href = homePath
          });
        }).catch(() => { loading.value = false; });
      }
    }
  });
}

function handleSendSms() {
  if (!loginForm.value.phonenumber || !/^1[3-9]\d{9}$/.test(loginForm.value.phonenumber)) {
    proxy.$modal.msgWarning("请输入正确的手机号"); return;
  }
  sendSmsCode(loginForm.value.phonenumber).then(() => {
    proxy.$modal.msgSuccess("验证码已发送");
    smsCountdown.value = 60;
    smsTimer = setInterval(() => {
      smsCountdown.value--;
      if (smsCountdown.value <= 0) clearInterval(smsTimer);
    }, 1000);
  }).catch(() => {});
}

function getCode() {
  getCodeImg().then(res => {
    captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled;
    if (captchaEnabled.value) {
      codeUrl.value = "data:image/gif;base64," + res.img;
      loginForm.value.uuid = res.uuid;
    }
  });
}

function getCookie() {
  const username = Cookies.get("username");
  const password = Cookies.get("password");
  const rememberMe = Cookies.get("rememberMe");
  loginForm.value = {
    username: username === undefined ? loginForm.value.username : username,
    password: password === undefined ? loginForm.value.password : decrypt(password),
    rememberMe: rememberMe === undefined ? false : Boolean(rememberMe),
    code: "", uuid: "", phonenumber: "", smsCode: ""
  };
}

getCode();
getCookie();
</script>

<style lang="scss" scoped>
.login-page {
  display: flex; min-height: 100vh; background: #f5f6fa;
  font-family: "PingFang SC", "Microsoft YaHei UI", sans-serif;
}

.login-left {
  flex: 0 0 42%; background: linear-gradient(160deg, #1a3a5c 0%, #1e4d7b 50%, #1a3a5c 100%);
  position: relative; display: flex; flex-direction: column;
  align-items: center; justify-content: center;
}
.left-content {
  padding: 40px; max-width: 380px; text-align: center;
}
.left-emblem {
  margin-bottom: 24px;
}
.left-title {
  font-size: 34px; font-weight: 700; color: #fff; margin: 0 0 8px 0;
  letter-spacing: 6px;
}
.left-subtitle {
  font-size: 16px; color: rgba(255,255,255,0.75); margin: 0 0 28px 0;
  letter-spacing: 3px;
}
.left-divider {
  width: 48px; height: 2px; background: rgba(255,255,255,0.3);
  margin: 0 auto 28px auto; border-radius: 1px;
}
.left-desc {
  font-size: 14px; color: rgba(255,255,255,0.6); line-height: 1.8; margin: 0 0 32px 0;
}
.left-features {
  display: flex; flex-direction: column; gap: 14px; text-align: left;
}
.feature-item {
  display: flex; align-items: center; gap: 12px;
  font-size: 13px; color: rgba(255,255,255,0.7);
}
.feature-dot {
  width: 5px; height: 5px; border-radius: 50%; background: rgba(255,255,255,0.5);
  flex-shrink: 0;
}
.left-footer {
  position: absolute; bottom: 28px; font-size: 12px;
  color: rgba(255,255,255,0.35); letter-spacing: 1px;
}

.login-right {
  flex: 1; display: flex; flex-direction: column; align-items: center;
  justify-content: center; padding: 40px; position: relative;
  background: #f5f6fa;
}
.right-inner {
  width: 100%; max-width: 400px;
  background: #fff; border-radius: 8px; padding: 40px 36px;
  border: 1px solid #e8eaed;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.form-title {
  font-size: 22px; font-weight: 600; color: #1a1a1a; margin: 0 0 6px 0;
}
.form-desc {
  font-size: 13px; color: #888; margin: 0 0 24px 0;
}

.type-switch {
  display: flex; margin-bottom: 24px;
  border: 1px solid #e8eaed; border-radius: 6px; overflow: hidden;
}
.type-option {
  flex: 1; display: flex; align-items: center; justify-content: center; gap: 5px;
  padding: 10px 0; font-size: 13px; font-weight: 500;
  color: #666; cursor: pointer; transition: all 0.2s; user-select: none;
  background: #fafafa;
}
.type-option.active {
  background: #fff; color: #1a3a5c; font-weight: 600;
}

.login-form {
  :deep(.el-form-item__label) {
    font-size: 13px; font-weight: 500; color: #333; padding-bottom: 4px;
  }
  :deep(.el-input__wrapper) {
    border-radius: 6px; box-shadow: 0 0 0 1px #dcdfe6 inset;
    padding: 4px 12px;
  }
  :deep(.el-input__wrapper.is-focus) {
    box-shadow: 0 0 0 1px #1a3a5c inset;
  }
  :deep(.el-form-item) { margin-bottom: 20px; }
}

.code-row { display: flex; gap: 10px; width: 100%; .el-input { flex: 1; } }
.code-img {
  height: 40px; border-radius: 6px; cursor: pointer; border: 1px solid #dcdfe6;
}
.sms-btn {
  height: 40px; border-radius: 6px; min-width: 110px; font-size: 13px;
}

.login-options {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
  :deep(.el-checkbox__label) { color: #666; font-size: 13px; }
}

.submit-btn {
  width: 100%; height: 44px; border-radius: 6px; font-size: 15px; font-weight: 600;
  background: #1a3a5c; border: none; letter-spacing: 4px;
  &:hover { background: #1e4d7b; }
}

.form-footer {
  text-align: center; margin-top: 20px; font-size: 13px; color: #888;
}
.register-link {
  color: #1a3a5c; font-weight: 500; text-decoration: none;
  &:hover { text-decoration: underline; }
}
.copyright {
  position: absolute; bottom: 20px; text-align: center; width: 100%;
  font-size: 12px; color: #bbb;
}

@media (max-width: 900px) {
  .login-left { display: none; }
  .login-right { padding: 32px 24px; }
  .right-inner { padding: 28px 20px; }
}
@media (max-width: 480px) {
  .login-right { padding: 20px 16px; }
  .right-inner { padding: 24px 16px; }
  .form-title { font-size: 18px; }
  .form-desc { font-size: 12px; }
  .submit-btn { height: 40px; font-size: 14px; letter-spacing: 2px; }
}
</style>
