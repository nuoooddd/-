<template>
  <div class="profile-page">
    <div class="profile-header">
      <div class="avatar-wrap">
        <img :src="state.user.avatar || avatarDefault" class="avatar-img" />
      </div>
      <div class="user-info">
        <div class="nick-name">{{ state.user.nickName || state.user.userName }}</div>
        <div class="role-tag">{{ state.roleGroup }}</div>
      </div>
    </div>

    <div class="info-card">
      <div class="info-row">
        <span class="info-label">用户名</span>
        <span class="info-value">{{ state.user.userName }}</span>
      </div>
      <div class="info-row">
        <span class="info-label">手机号</span>
        <span class="info-value">{{ state.user.phonenumber || '--' }}</span>
      </div>
      <div class="info-row">
        <span class="info-label">邮箱</span>
        <span class="info-value">{{ state.user.email || '--' }}</span>
      </div>
      <div class="info-row">
        <span class="info-label">部门</span>
        <span class="info-value">{{ state.user.dept?.deptName || '--' }}</span>
      </div>
      <div class="info-row">
        <span class="info-label">注册时间</span>
        <span class="info-value">{{ state.user.createTime || '--' }}</span>
      </div>
    </div>

    <div class="action-card">
      <div class="action-item" @click="showEditDialog = true">
        <el-icon><Edit /></el-icon>
        <span>修改资料</span>
        <el-icon class="arrow"><ArrowRight /></el-icon>
      </div>
      <div class="action-item" @click="showPwdDialog = true">
        <el-icon><Lock /></el-icon>
        <span>修改密码</span>
        <el-icon class="arrow"><ArrowRight /></el-icon>
      </div>
      <div class="action-item" @click="handleLogout">
        <el-icon><SwitchButton /></el-icon>
        <span>退出登录</span>
        <el-icon class="arrow"><ArrowRight /></el-icon>
      </div>
    </div>

    <el-dialog v-model="showEditDialog" title="修改资料" width="92%" append-to-body destroy-on-close>
      <el-form :model="editForm" label-width="70px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickName" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phonenumber" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="editForm.sex">
            <el-radio value="0">男</el-radio>
            <el-radio value="1">女</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveProfile">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showPwdDialog" title="修改密码" width="92%" append-to-body destroy-on-close>
      <el-form :model="pwdForm" label-width="80px">
        <el-form-item label="旧密码">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请确认新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPwdDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSavePwd">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Profile">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { getUserProfile, updateUserProfile, updateUserPwd } from '@/api/system/user'
import useUserStore from '@/store/modules/user'
import avatarDefault from '@/assets/images/profile.jpg'

const router = useRouter()
const userStore = useUserStore()

const showEditDialog = ref(false)
const showPwdDialog = ref(false)

const state = reactive({
  user: {},
  roleGroup: '',
  postGroup: ''
})

const editForm = ref({ nickName: '', phonenumber: '', email: '', sex: '0' })
const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })

function getUser() {
  getUserProfile().then(response => {
    state.user = response.data
    state.roleGroup = response.roleGroup
    state.postGroup = response.postGroup
    editForm.value = {
      nickName: response.data.nickName || '',
      phonenumber: response.data.phonenumber || '',
      email: response.data.email || '',
      sex: response.data.sex || '0'
    }
  })
}

function handleSaveProfile() {
  updateUserProfile(editForm.value).then(() => {
    ElMessage.success('修改成功')
    showEditDialog.value = false
    getUser()
  })
}

function handleSavePwd() {
  if (!pwdForm.value.oldPassword || !pwdForm.value.newPassword) {
    ElMessage.warning('请填写完整')
    return
  }
  if (pwdForm.value.newPassword !== pwdForm.value.confirmPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }
  updateUserPwd(pwdForm.value.oldPassword, pwdForm.value.newPassword).then(() => {
    ElMessage.success('密码修改成功，请重新登录')
    showPwdDialog.value = false
    userStore.logOut().then(() => { location.href = '/index' })
  })
}

function handleLogout() {
  ElMessageBox.confirm('确定退出登录？', '提示', { type: 'warning' }).then(() => {
    userStore.logOut().then(() => { location.href = '/index' })
  }).catch(() => {})
}

getUser()
</script>

<style scoped>
.profile-page { background:#f5f6fa; min-height:100%; padding:12px; }

.profile-header {
  display:flex; align-items:center; gap:14px;
  padding:20px 16px; margin-bottom:12px;
  background:linear-gradient(135deg, #1a3a5c, #2d5a8e);
  border-radius:14px; color:#fff;
}
.avatar-wrap { width:56px; height:56px; border-radius:50%; overflow:hidden; border:2px solid rgba(255,255,255,0.4); flex-shrink:0; }
.avatar-img { width:100%; height:100%; object-fit:cover; }
.nick-name { font-size:18px; font-weight:600; }
.role-tag { font-size:12px; background:rgba(255,255,255,0.15); padding:2px 10px; border-radius:10px; margin-top:4px; display:inline-block; }

.info-card {
  background:#fff; border-radius:12px; padding:4px 0; margin-bottom:12px;
  border:1px solid #e8ecf1;
}
.info-row {
  display:flex; justify-content:space-between; align-items:center;
  padding:12px 16px; border-bottom:1px solid #f5f5f5;
}
.info-row:last-child { border-bottom:none; }
.info-label { font-size:13px; color:#94a3b8; }
.info-value { font-size:13px; color:#1e293b; font-weight:500; }

.action-card {
  background:#fff; border-radius:12px; padding:4px 0;
  border:1px solid #e8ecf1;
}
.action-item {
  display:flex; align-items:center; gap:10px;
  padding:14px 16px; border-bottom:1px solid #f5f5f5;
  cursor:pointer; font-size:14px; color:#1e293b;
}
.action-item:last-child { border-bottom:none; color:#ef4444; }
.action-item .arrow { margin-left:auto; color:#cbd5e1; font-size:14px; }
</style>
