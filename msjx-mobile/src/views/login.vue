<template>
  <div class="m-login">
    <div class="login-header">
      <div class="login-emblem">
        <svg viewBox="0 0 40 40" width="36" height="36"><circle cx="20" cy="20" r="18" fill="none" stroke="rgba(255,255,255,0.7)" stroke-width="1.5"/><path d="M20 8 L20 32 M12 16 L20 8 L28 16" fill="none" stroke="rgba(255,255,255,0.9)" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
      </div>
      <h1 class="login-title">免申即享</h1>
      <p class="login-subtitle">政务便民服务平台</p>
    </div>

    <div class="login-form-wrap">
      <div class="type-tabs">
        <div :class="['tab', { active: loginType === 'pwd' }]" @click="loginType = 'pwd'">密码登录</div>
        <div :class="['tab', { active: loginType === 'sms' }]" @click="loginType = 'sms'">验证码登录</div>
      </div>

      <template v-if="loginType === 'pwd'">
        <div class="field">
          <el-input v-model="loginForm.username" size="large" placeholder="请输入账号" prefix-icon="User" />
        </div>
        <div class="field">
          <el-input v-model="loginForm.password" type="password" size="large" placeholder="请输入密码" prefix-icon="Lock" show-password @keyup.enter="handleLogin" />
        </div>
        <div class="field" v-if="captchaEnabled">
          <div class="code-row">
            <el-input v-model="loginForm.code" size="large" placeholder="验证码" @keyup.enter="handleLogin" />
            <img :src="codeUrl" @click="getCode" class="code-img" />
          </div>
        </div>
        <div class="remember-row">
          <el-checkbox v-model="loginForm.rememberMe">记住密码</el-checkbox>
        </div>
      </template>

      <template v-else>
        <div class="field">
          <el-input v-model="loginForm.phonenumber" size="large" placeholder="请输入手机号" prefix-icon="Iphone" maxlength="11" />
        </div>
        <div class="field">
          <div class="code-row">
            <el-input v-model="loginForm.smsCode" size="large" placeholder="验证码" maxlength="6" @keyup.enter="handleLogin" />
            <el-button size="large" :disabled="smsCountdown > 0" @click="handleSendSms" class="sms-btn">{{ smsCountdown > 0 ? `${smsCountdown}s` : '获取验证码' }}</el-button>
          </div>
        </div>
      </template>

      <button class="login-btn" @click="handleLogin" :disabled="loading">{{ loading ? '登录中...' : '登 录' }}</button>

      <div class="login-footer">
        <span>还没有账号？</span>
        <router-link to="/register" class="link">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { getCodeImg, sendSmsCode } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from "@/utils/jsencrypt"
import useUserStore from '@/store/modules/user'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()

const loginType = ref("pwd")
const loginForm = ref({ username: "admin", password: "admin123", rememberMe: false, code: "", uuid: "", phonenumber: "", smsCode: "" })
const codeUrl = ref("")
const loading = ref(false)
const captchaEnabled = ref(true)
const smsCountdown = ref(0)
let smsTimer = null

function handleLogin() {
  loading.value = true
  if (loginType.value === 'pwd') {
    if (loginForm.value.rememberMe) {
      Cookies.set("username", loginForm.value.username, { expires: 30 })
      Cookies.set("password", encrypt(loginForm.value.password), { expires: 30 })
    } else {
      Cookies.remove("username"); Cookies.remove("password")
    }
    userStore.login(loginForm.value).then(() => {
      userStore.getInfo().then(() => {
        const roles = userStore.roles
        let homePath = '/dashboard'
        if (roles.includes('admin')) homePath = '/dashboard'
        else if (roles.includes('enterprise')) homePath = '/portal/enterpriseHome'
        else if (roles.includes('person')) homePath = '/personPortal/personHome'
        window.location.href = homePath
      })
    }).catch(() => { loading.value = false; if (captchaEnabled.value) getCode() })
  } else {
    userStore.login({ loginType: 'sms', phonenumber: loginForm.value.phonenumber, smsCode: loginForm.value.smsCode }).then(() => {
      userStore.getInfo().then(() => {
        const roles = userStore.roles
        let homePath = '/dashboard'
        if (roles.includes('admin')) homePath = '/dashboard'
        else if (roles.includes('enterprise')) homePath = '/portal/enterpriseHome'
        else if (roles.includes('person')) homePath = '/personPortal/personHome'
        window.location.href = homePath
      })
    }).catch(() => { loading.value = false })
  }
}

function handleSendSms() {
  if (!loginForm.value.phonenumber || !/^1[3-9]\d{9}$/.test(loginForm.value.phonenumber)) {
    proxy.$modal.msgWarning("请输入正确的手机号"); return
  }
  sendSmsCode(loginForm.value.phonenumber).then(() => {
    proxy.$modal.msgSuccess("验证码已发送")
    smsCountdown.value = 60
    smsTimer = setInterval(() => { smsCountdown.value--; if (smsCountdown.value <= 0) clearInterval(smsTimer) }, 1000)
  }).catch(() => {})
}

function getCode() {
  getCodeImg().then(res => {
    captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled
    if (captchaEnabled.value) { codeUrl.value = "data:image/gif;base64," + res.img; loginForm.value.uuid = res.uuid }
  })
}

getCode()
</script>

<style scoped>
.m-login {
  min-height: 100vh; background: linear-gradient(160deg, #1a3a5c 0%, #1e4d7b 50%, #1a3a5c 100%);
  display: flex; flex-direction: column; padding: 0 20px;
  max-width: 430px; margin: 0 auto;
  box-shadow: 0 0 40px rgba(0,0,0,0.12);
}
.login-header { text-align: center; padding: 48px 0 32px; }
.login-emblem { margin-bottom: 12px; }
.login-title { font-size: 24px; font-weight: 700; color: #fff; letter-spacing: 4px; margin: 0 0 6px; }
.login-subtitle { font-size: 13px; color: rgba(255,255,255,0.6); letter-spacing: 2px; margin: 0; }
.login-form-wrap {
  background: #fff; border-radius: 14px; padding: 24px 18px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.1);
}
.type-tabs { display: flex; margin-bottom: 20px; border: 1px solid #e8eaed; border-radius: 8px; overflow: hidden; }
.tab { flex: 1; text-align: center; padding: 9px 0; font-size: 13px; color: #666; cursor: pointer; background: #fafafa; transition: all 0.2s; }
.tab.active { background: #fff; color: #1a3a5c; font-weight: 600; }
.field { margin-bottom: 14px; }
.code-row { display: flex; gap: 8px; }
.code-row .el-input { flex: 1; }
.code-img { height: 38px; border-radius: 8px; cursor: pointer; border: 1px solid #dcdfe6; }
.sms-btn { height: 38px; border-radius: 8px; min-width: 90px; font-size: 12px; }
.remember-row { margin-bottom: 14px; }
.remember-row :deep(.el-checkbox__label) { font-size: 12px; color: #666; }
.login-btn {
  width: 100%; height: 44px; border: none; border-radius: 10px;
  background: #1a3a5c; color: #fff; font-size: 15px; font-weight: 600;
  letter-spacing: 4px; cursor: pointer; transition: background 0.2s;
}
.login-btn:active { background: #1e4d7b; }
.login-btn:disabled { background: #a0c4e8; cursor: not-allowed; }
.login-footer { text-align: center; margin-top: 18px; font-size: 12px; color: #999; }
.link { color: #1a3a5c; font-weight: 500; text-decoration: none; }
</style>
