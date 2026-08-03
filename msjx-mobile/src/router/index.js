import { createWebHistory, createRouter } from 'vue-router'
/* Layout */
import Layout from '@/layout/mobile/index'

/**
 * Note: 路由配置项
 *
 * hidden: true                     // 当设置 true 的时候该路由不会再侧边栏出现 如401，login等页面，或者如一些编辑页面/edit/1
 * alwaysShow: true                 // 当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式--如组件页面
 *                                  // 只有一个时，会将那个子路由当做根路由显示在侧边栏--如引导页面
 *                                  // 若你想不管路由下面的 children 声明的个数都显示你的根路由
 *                                  // 你可以设置 alwaysShow: true，这样它就会忽略之前定义的规则，一直显示根路由
 * redirect: noRedirect             // 当设置 noRedirect 的时候该路由在面包屑导航中不可被点击
 * name:'router-name'               // 设定路由的名字，一定要填写不然使用<keep-alive>时会出现各种问题
 * query: '{"id": 1, "name": "ry"}' // 访问路由的默认传递参数
 * roles: ['admin', 'common']       // 访问路由的角色权限
 * permissions: ['a:a:a', 'b:b:b']  // 访问路由的菜单权限
 * meta : {
    noCache: true                   // 如果设置为true，则不会被 <keep-alive> 缓存(默认 false)
    title: 'title'                  // 设置该路由在侧边栏和面包屑中展示的名字
    icon: 'svg-name'                // 设置该路由的图标，对应路径src/assets/icons/svg
    breadcrumb: false               // 如果设置为false，则不会在breadcrumb面包屑中显示
    activeMenu: '/system/user'      // 当路由设置了该属性，则会高亮相对应的侧边栏。
  }
 */

// 公共路由
export const constantRoutes = [
  {
    path: '/redirect',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect/index.vue')
      }
    ]
  },
  {
    path: '/login',
    component: () => import('@/views/login'),
    hidden: true
  },
  {
    path: '/register',
    component: () => import('@/views/register'),
    hidden: true
  },
  {

    path: '/401',
    component: () => import('@/views/error/401'),
    hidden: true
  },
  {
    path: '',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: '/dashboard',
        component: () => import('@/views/biz/dashboard/index'),
        name: 'Dashboard',
        meta: { title: '管理首页', icon: 'dashboard', roles: ['admin'] }
      }
    ]
  },
  {
    path: '/biz',
    component: Layout,
    hidden: true,
    children: [
      {
        path: 'matchRecord',
        component: () => import('@/views/biz/matchRecord/index'),
        name: 'MatchRecord',
        meta: { title: '匹配兑现', icon: 'connection' }
      },
      {
        path: 'policy',
        component: () => import('@/views/biz/policy/index'),
        name: 'Policy',
        meta: { title: '政策管理', icon: 'document' }
      },
      {
        path: 'fundPublic',
        component: () => import('@/views/biz/fundPublic/index'),
        name: 'FundPublic',
        meta: { title: '资金公示', icon: 'money' }
      },
      {
        path: 'fund',
        component: () => import('@/views/biz/fund/index'),
        name: 'Fund',
        meta: { title: '资金池', icon: 'money' }
      },
      {
        path: 'targetData',
        component: () => import('@/views/biz/targetData/index'),
        name: 'TargetData',
        meta: { title: '目标对象', icon: 'user' }
      },
      {
        path: 'message',
        component: () => import('@/views/biz/message/index'),
        name: 'Message',
        meta: { title: '消息中心', icon: 'bell' }
      },
      {
        path: 'policyCatalog',
        component: () => import('@/views/biz/policyCatalog/index'),
        name: 'PolicyCatalog',
        meta: { title: '政策超市', icon: 'document' }
      },
      {
        path: 'userFulfill',
        component: () => import('@/views/biz/userFulfill/index'),
        name: 'UserFulfill',
        meta: { title: '我的兑付', icon: 'money' }
      },
      {
        path: 'audit',
        component: () => import('@/views/biz/audit/index'),
        name: 'Audit',
        meta: { title: '实名审核', icon: 'check' }
      },
      {
        path: 'auditLog',
        component: () => import('@/views/biz/auditLog/index'),
        name: 'AuditLog',
        meta: { title: '操作日志', icon: 'list' }
      },
      {
        path: 'dataScreen',
        component: () => import('@/views/biz/dataScreen/index'),
        name: 'DataScreen',
        meta: { title: '数据大屏', icon: 'data-line' }
      },
      {
        path: 'aiAssistant',
        component: () => import('@/views/biz/aiAssistant/index'),
        name: 'AiAssistant',
        meta: { title: 'AI助手', icon: 'chat-dot-square' }
      },
      {
        path: 'rule',
        component: () => import('@/views/biz/rule/index'),
        name: 'Rule',
        meta: { title: '规则管理', icon: 'set-up' }
      },
      {
        path: 'statistics',
        component: () => import('@/views/biz/statistics/index'),
        name: 'Statistics',
        meta: { title: '数据统计', icon: 'data-line' }
      },
      {
        path: 'policyCalendar',
        component: () => import('@/views/biz/policyCalendar/index'),
        name: 'PolicyCalendar',
        meta: { title: '政策日历', icon: 'calendar' }
      }
    ]
  },
  {
    path: '/portal',
    component: Layout,
    hidden: true,
    children: [
      {
        path: 'enterpriseHome',
        component: () => import('@/views/biz/enterpriseHome/index'),
        name: 'EnterpriseHome',
        meta: { title: '企业首页', icon: 'home-filled' }
      },
      {
        path: 'policyCatalog',
        component: () => import('@/views/biz/policyCatalog/index'),
        name: 'PortalPolicyCatalog',
        meta: { title: '政策超市', icon: 'document' }
      },
      {
        path: 'fundPublic',
        component: () => import('@/views/biz/fundPublic/index'),
        name: 'PortalFundPublic',
        meta: { title: '资金公示', icon: 'money' }
      },
      {
        path: 'message',
        component: () => import('@/views/biz/message/index'),
        name: 'PortalMessage',
        meta: { title: '消息中心', icon: 'bell' }
      },
      {
        path: 'userFulfill',
        component: () => import('@/views/biz/userFulfill/index'),
        name: 'PortalUserFulfill',
        meta: { title: '我的兑付', icon: 'money' }
      }
    ]
  },
  {
    path: '/personPortal',
    component: Layout,
    hidden: true,
    children: [
      {
        path: 'personHome',
        component: () => import('@/views/biz/personHome/index'),
        name: 'PersonHome',
        meta: { title: '个人首页', icon: 'home-filled' }
      },
      {
        path: 'policyCatalog',
        component: () => import('@/views/biz/policyCatalog/index'),
        name: 'PersonPolicyCatalog',
        meta: { title: '政策超市', icon: 'document' }
      },
      {
        path: 'fundPublic',
        component: () => import('@/views/biz/fundPublic/index'),
        name: 'PersonFundPublic',
        meta: { title: '资金公示', icon: 'money' }
      },
      {
        path: 'message',
        component: () => import('@/views/biz/message/index'),
        name: 'PersonMessage',
        meta: { title: '消息中心', icon: 'bell' }
      }
    ]
  },
  {

    path: '/user',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'profile',
        component: () => import('@/views/system/user/profile/index'),
        name: 'Profile',
        meta: { title: '个人中心', icon: 'user' }
      }
    ]
  }
]

// 动态路由，基于用户权限动态去加载
export const dynamicRoutes = [
  {
    path: '/operlog',
    permissions: ['monitor:operlog:list'],
    component: Layout,
    meta: { title: '操作日志', icon: 'list' },
    children: [
      {
        path: '',
        component: () => import('@/views/monitor/operlog/index'),
        name: 'Operlog',
        meta: { title: '操作日志' }
      }
    ]
  },
  {
    path: '/logininfor',
    permissions: ['monitor:logininfor:list'],
    component: Layout,
    meta: { title: '登录日志', icon: 'list' },
    children: [
      {
        path: '',
        component: () => import('@/views/monitor/logininfor/index'),
        name: 'Logininfor',
        meta: { title: '登录日志' }
      }
    ]
  },
  {
    path: '/system/user-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:user:edit'],
    children: [
      {
        path: 'role/:userId(\\d+)',
        component: () => import('@/views/system/user/authRole'),
        name: 'AuthRole',
        meta: { title: '分配角色', activeMenu: '/system/user' }
      }
    ]
  },
  {
    path: '/system/role-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:role:edit'],
    children: [
      {
        path: 'user/:roleId(\\d+)',
        component: () => import('@/views/system/role/authUser'),
        name: 'AuthUser',
        meta: { title: '分配用户', activeMenu: '/system/role' }
      }
    ]
  },
  {
    path: '/system/dict-data',
    component: Layout,
    hidden: true,
    permissions: ['system:dict:list'],
    children: [
      {
        path: 'index/:dictId(\\d+)',
        component: () => import('@/views/system/dict/data'),
        name: 'Data',
        meta: { title: '字典数据', activeMenu: '/system/dict' }
      }
    ]
  },


]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  },
});

export default router;


