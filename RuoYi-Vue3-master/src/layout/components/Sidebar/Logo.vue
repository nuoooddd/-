<template>
  <div class="sidebar-logo-container" :class="{ 'collapse': collapse }">
    <transition name="sidebarLogoFade">
      <router-link v-if="collapse" key="collapse" class="sidebar-logo-link" to="/">
        <div class="logo-icon-sm">免</div>
      </router-link>
      <router-link v-else key="expand" class="sidebar-logo-link" to="/">
        <div class="logo-icon">免</div>
        <div class="logo-text">
          <span class="logo-title">免申即享</span>
          <span class="logo-sub">政务便民平台</span>
        </div>
      </router-link>
    </transition>
  </div>
</template>

<script setup>
defineProps({
  collapse: {
    type: Boolean,
    required: true
  }
})
</script>

<style lang="scss" scoped>
.sidebarLogoFade-enter-active {
  transition: opacity 1.5s;
}

.sidebarLogoFade-enter,
.sidebarLogoFade-leave-to {
  opacity: 0;
}

.sidebar-logo-container {
  position: relative;
  width: 100%;
  height: 56px;
  background: #fff;
  text-align: center;
  overflow: hidden;
  border-bottom: 1px solid #e2e8f0;

  .sidebar-logo-link {
    height: 100%;
    width: 100%;
    display: flex;
    align-items: center;
    padding: 0 16px;
    text-decoration: none;
  }

  .logo-icon {
    width: 34px;
    height: 34px;
    border-radius: 8px;
    background: linear-gradient(135deg, #0052ff, #3b82f6);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 17px;
    font-weight: 700;
    flex-shrink: 0;
    box-shadow: 0 2px 8px rgba(0, 82, 255, 0.25);
  }

  .logo-icon-sm {
    width: 30px;
    height: 30px;
    border-radius: 6px;
    background: linear-gradient(135deg, #0052ff, #3b82f6);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 15px;
    font-weight: 700;
    margin: 0 auto;
    box-shadow: 0 2px 8px rgba(0, 82, 255, 0.25);
  }

  .logo-text {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    margin-left: 12px;
  }

  .logo-title {
    font-size: 16px;
    font-weight: 700;
    color: #0f172a;
    line-height: 1.2;
    letter-spacing: 1px;
  }

  .logo-sub {
    font-size: 10px;
    color: #94a3b8;
    line-height: 1.2;
    margin-top: 3px;
    letter-spacing: 0.5px;
  }

  &.collapse {
    .sidebar-logo-link {
      justify-content: center;
      padding: 0;
    }
  }
}
</style>