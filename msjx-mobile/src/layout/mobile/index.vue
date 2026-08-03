<template>
  <div class="mobile-layout">
    <div class="mobile-header">
      <span class="header-title">{{ currentTitle }}</span>
      <el-dropdown @command="handleCommand" trigger="click">
        <div class="header-avatar">
          <img :src="userStore.avatar" class="avatar-img" />
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人中心</el-dropdown-item>
            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <div class="mobile-main">
      <router-view />
      <div class="main-bottom-safe"></div>
    </div>

    <div class="mobile-tabbar">
      <div v-for="tab in tabs" :key="tab.path" class="tabbar-item" :class="{ active: isTabActive(tab) }" @click="handleTabClick(tab)">
        <el-icon :size="22"><component :is="tab.icon" /></el-icon>
        <span class="tabbar-label">{{ tab.label }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import useUserStore from '@/store/modules/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const roles = computed(() => userStore.roles)

const tabs = computed(() => {
  if (roles.value.includes('admin')) {
    return [
      { label: '首页', icon: 'HomeFilled', path: '/dashboard' },
      { label: '政策', icon: 'Document', path: '/biz/policyCatalog' },
      { label: '公示', icon: 'Money', path: '/biz/fundPublic' },
      { label: '消息', icon: 'Bell', path: '/biz/message' },
      { label: '我的', icon: 'UserFilled', path: '/user/profile' }
    ]
  } else if (roles.value.includes('enterprise')) {
    return [
      { label: '首页', icon: 'HomeFilled', path: '/portal/enterpriseHome' },
      { label: '政策', icon: 'Document', path: '/portal/policyCatalog' },
      { label: '公示', icon: 'Money', path: '/portal/fundPublic' },
      { label: '消息', icon: 'Bell', path: '/portal/message' },
      { label: '我的', icon: 'UserFilled', path: '/user/profile' }
    ]
  } else {
    return [
      { label: '首页', icon: 'HomeFilled', path: '/personPortal/personHome' },
      { label: '政策', icon: 'Document', path: '/personPortal/policyCatalog' },
      { label: '公示', icon: 'Money', path: '/personPortal/fundPublic' },
      { label: '消息', icon: 'Bell', path: '/personPortal/message' },
      { label: '我的', icon: 'UserFilled', path: '/user/profile' }
    ]
  }
})

const currentTitle = computed(() => {
  const activeTab = tabs.value.find(t => isTabActive(t))
  return activeTab ? activeTab.label : '免申即享'
})

function isTabActive(tab) {
  return route.path.startsWith(tab.path)
}

function handleTabClick(tab) {
  if (route.path !== tab.path) {
    router.push(tab.path)
  }
}

function handleCommand(command) {
  if (command === 'logout') {
    ElMessageBox.confirm('确定退出登录？', '提示', { type: 'warning' }).then(() => {
      userStore.logOut().then(() => { location.href = '/index' })
    }).catch(() => {})
  } else if (command === 'profile') {
    router.push('/user/profile')
  }
}
</script>

<style scoped>
.mobile-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-width: 430px;
  margin: 0 auto;
  background: #f5f6fa;
  position: relative;
  box-shadow: 0 0 40px rgba(0,0,0,0.12);
}

.mobile-header {
  height: 48px;
  background: #1a3a5c;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-title {
  color: #fff;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 1px;
}

.header-avatar {
  cursor: pointer;
}

.avatar-img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 2px solid rgba(255,255,255,0.4);
}

.mobile-main {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  position: relative;
}

.main-bottom-safe {
  height: 10px;
  flex-shrink: 0;
}

.mobile-tabbar {
  height: 56px;
  background: #fff;
  border-top: 1px solid #e8eaed;
  display: flex;
  flex-shrink: 0;
  z-index: 100;
  box-shadow: 0 -2px 8px rgba(0,0,0,0.06);
}

.tabbar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  color: #999;
  cursor: pointer;
  transition: color 0.2s;
  -webkit-tap-highlight-color: transparent;
  user-select: none;
}

.tabbar-item.active {
  color: #1a3a5c;
}

.tabbar-item.active .tabbar-label {
  font-weight: 600;
}

.tabbar-label {
  font-size: 10px;
  line-height: 1;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.15s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>