<template>
  <div :class="classObj" class="app-wrapper" :style="{ '--current-color': theme }">
    <div v-if="device === 'mobile' && sidebar.opened" class="drawer-bg" @click="handleClickOutside"/>
    <sidebar v-if="!sidebar.hide" class="sidebar-container" />
    <div :class="{ hasTagsView: needTagsView, sidebarHide: sidebar.hide }" class="main-container">
      <div :class="{ 'fixed-header': fixedHeader }">
        <navbar @setLayout="setLayout" />
        <tags-view v-if="needTagsView && device !== 'mobile'" />
      </div>
      <app-main :style="{ paddingBottom: device === 'mobile' ? '60px' : '0' }" />
      <settings ref="settingRef" />
    </div>
    <ai-assistant />
    <div v-if="device === 'mobile'" class="mobile-tabbar">
      <div v-for="tab in mobileTabs" :key="tab.path" class="tabbar-item" :class="{ active: isTabActive(tab) }" @click="handleTabClick(tab)">
        <el-icon :size="20"><component :is="tab.icon" /></el-icon>
        <span class="tabbar-label">{{ tab.label }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useWindowSize } from '@vueuse/core'
import { useRoute, useRouter } from 'vue-router'
import Sidebar from './components/Sidebar/index.vue'
import { AppMain, Navbar, Settings, TagsView } from './components'
import AiAssistant from '@/components/AiAssistant/index.vue'
import defaultSettings from '@/settings'
import useAppStore from '@/store/modules/app'
import useSettingsStore from '@/store/modules/settings'
import useUserStore from '@/store/modules/user'

const settingsStore = useSettingsStore()
const appStore = useAppStore()
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

const theme = computed(() => settingsStore.theme);
const sideTheme = computed(() => settingsStore.sideTheme);
const sidebar = computed(() => appStore.sidebar);
const device = computed(() => appStore.device);
const needTagsView = computed(() => settingsStore.tagsView);
const fixedHeader = computed(() => settingsStore.fixedHeader);

const roles = computed(() => userStore.roles)

const mobileTabs = computed(() => {
  if (roles.value.includes('admin')) {
    return [
      { label: '首页', icon: 'HomeFilled', path: '/dashboard' },
      { label: '匹配', icon: 'Connection', path: '/biz/matchRecord' },
      { label: '政策', icon: 'Document', path: '/biz/policy' },
      { label: '公示', icon: 'Money', path: '/biz/fundPublic' },
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

function isTabActive(tab) {
  return route.path.startsWith(tab.path)
}

function handleTabClick(tab) {
  router.push(tab.path)
}

const classObj = computed(() => ({
  hideSidebar: !sidebar.value.opened,
  openSidebar: sidebar.value.opened,
  withoutAnimation: sidebar.value.withoutAnimation,
  mobile: device.value === 'mobile'
}))

const { width, height } = useWindowSize();
const WIDTH = 992;

watchEffect(() => {
  if (device.value === 'mobile' && sidebar.value.opened) {
    appStore.closeSideBar({ withoutAnimation: false })
  }
  if (width.value - 1 < WIDTH) {
    appStore.toggleDevice('mobile')
    appStore.closeSideBar({ withoutAnimation: true })
  } else {
    appStore.toggleDevice('desktop')
    if (!sidebar.value.opened) {
      appStore.toggleSideBar(false)
    }
  }
})

function handleClickOutside() {
  appStore.closeSideBar({ withoutAnimation: false })
}

const settingRef = ref(null);
function setLayout() {
  settingRef.value.openSetting();
}
</script>

<style lang="scss" scoped>
  @import "@/assets/styles/mixin.scss";
  @import "@/assets/styles/variables.module.scss";

.app-wrapper {
  @include clearfix;
  position: relative;
  height: 100%;
  width: 100%;

  &.mobile.openSidebar {
    position: fixed;
    top: 0;
  }
}

.drawer-bg {
  background: #000;
  opacity: 0.3;
  width: 100%;
  top: 0;
  height: 100%;
  position: absolute;
  z-index: 999;
}

.fixed-header {
  position: fixed;
  top: 0;
  right: 0;
  z-index: 9;
  width: calc(100% - #{$base-sidebar-width});
  transition: width 0.28s;
}

.hideSidebar .fixed-header {
  width: calc(100% - 54px);
}

.sidebarHide .fixed-header {
  width: 100%;
}

.mobile .fixed-header {
  width: 100%;
}

.mobile-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 56px;
  background: #fff;
  border-top: 1px solid #e8eaed;
  display: flex;
  z-index: 1000;
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
  font-size: 10px;
  cursor: pointer;
  transition: color 0.2s;
  -webkit-tap-highlight-color: transparent;
}

.tabbar-item.active {
  color: #1a3a5c;
}

.tabbar-label {
  font-size: 10px;
  line-height: 1;
}
</style>