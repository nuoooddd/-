import router from './router'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { isHttp } from '@/utils/validate'
import { isRelogin } from '@/utils/request'
import useUserStore from '@/store/modules/user'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'

NProgress.configure({ showSpinner: false });

let hasAdded404 = false

const whiteList = ['/login', '/register'];

// 根据角色返回首页路径
function getHomePath(roles) {
  if (roles.includes('admin')) return '/dashboard'
  if (roles.includes('enterprise')) return '/portal/enterpriseHome'
  if (roles.includes('person')) return '/personPortal/personHome'
  return '/index'
}

router.beforeEach((to, from, next) => {
  NProgress.start()
  if (getToken()) {
    to.meta.title && useSettingsStore().setTitle(to.meta.title)
    /* has token*/
    if (to.path === '/login') {
      if (useUserStore().roles.length === 0) {
        useUserStore().getInfo().then(() => {
          isRelogin.show = true
          usePermissionStore().generateRoutes(useUserStore().roles).then(accessRoutes => {
            isRelogin.show = false
            accessRoutes.forEach(route => {
              if (!isHttp(route.path)) {
                router.addRoute(route)
              }
            })
            if (!hasAdded404) {
              router.addRoute({ path: '/:pathMatch(.*)*', component: () => import('@/views/error/404'), hidden: true })
              hasAdded404 = true
            }
            next({ path: getHomePath(useUserStore().roles), replace: true })
            NProgress.done()
          })
        })
      } else {
        next({ path: getHomePath(useUserStore().roles), replace: true })
        NProgress.done()
      }
    } else if (whiteList.indexOf(to.path) !== -1) {
      next()
    } else {
      if (useUserStore().roles.length === 0) {
        isRelogin.show = true
        useUserStore().getInfo().then(() => {
          isRelogin.show = false
          usePermissionStore().generateRoutes(useUserStore().roles).then(accessRoutes => {
            accessRoutes.forEach(route => {
              if (!isHttp(route.path)) {
                router.addRoute(route)
              }
            })
            if (!hasAdded404) {
              router.addRoute({ path: '/:pathMatch(.*)*', component: () => import('@/views/error/404'), hidden: true })
              hasAdded404 = true
            }
            next({ ...to, replace: true })
          })
        }).catch(err => {
          useUserStore().logOut().then(() => {
            ElMessage.error(err)
            next({ path: '/' })
          })
        })
      } else if (usePermissionStore().sidebarRouters.length === 0) {
        usePermissionStore().generateRoutes(useUserStore().roles).then(accessRoutes => {
          accessRoutes.forEach(route => {
            if (!isHttp(route.path)) {
              router.addRoute(route)
            }
          })
          if (!hasAdded404) {
            router.addRoute({ path: '/:pathMatch(.*)*', component: () => import('@/views/error/404'), hidden: true })
            hasAdded404 = true
          }
          next({ ...to, replace: true })
        })
      } else {
        next()
      }
    }
  } else {
    // 没有token
    if (whiteList.indexOf(to.path) !== -1) {
      // 在免登录白名单，直接进入
      next()
    } else {
      next(`/login?redirect=${to.fullPath}`) // 否则全部重定向到登录页
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})
