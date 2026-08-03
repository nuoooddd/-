<template>
  <div style="display:flex;align-items:center;justify-content:center;height:100vh;background:#f6f8fb;">
    <p style="color:#94a3b8;">正在跳转...</p>
  </div>
</template>

<script setup name="Index">
import { useRouter } from 'vue-router'
import useUserStore from '@/store/modules/user'

const router = useRouter()
const userStore = useUserStore()

const roles = userStore.roles
if (roles.includes('admin')) {
  router.replace('/dashboard')
} else if (roles.includes('enterprise')) {
  router.replace('/portal/enterpriseHome')
} else if (roles.includes('person')) {
  router.replace('/personPortal/personHome')
} else {
  router.replace('/dashboard')
}
</script>
