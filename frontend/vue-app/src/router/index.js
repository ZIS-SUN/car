import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import ShopList from '../views/ShopList.vue'
import ShopDetail from '../views/ShopDetail.vue'
import Appointment from '../views/Appointment.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import MemberCenter from '../views/MemberCenter.vue'
import MyOrders from '../views/MyOrders.vue'
import MyReviews from '../views/MyReviews.vue'
import CreateReview from '../views/CreateReview.vue'
import Services from '../views/Services.vue'
import Announcements from '../views/Announcements.vue'
import About from '../views/About.vue'
import OrderDetail from '../views/OrderDetail.vue'

// 后台管理组件
import AdminLayout from '../components/AdminLayout.vue'
import AdminDashboard from '../views/admin/AdminDashboard.vue'
import UserManagement from '../views/admin/UserManagement.vue'

// 门店端组件
import ShopLayout from '../components/ShopLayout.vue'
import ShopLogin from '../views/shop/ShopLogin.vue'
import ShopDashboard from '../views/shop/ShopDashboard.vue'
import OrderManagement from '../views/shop/OrderManagement.vue'
import ServiceManagement from '../views/shop/ServiceManagement.vue'
import PackageManagement from '../views/shop/PackageManagement.vue'
import ShopProfile from '../views/shop/ShopProfile.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/shops',
    name: 'ShopList',
    component: ShopList
  },
  {
    path: '/shops/:id',
    name: 'ShopDetail',
    component: ShopDetail
  },
  {
    path: '/appointment',
    name: 'Appointment',
    component: Appointment,
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/member-center',
    name: 'MemberCenter',
    component: MemberCenter,
    meta: { requiresAuth: true }
  },
  {
    path: '/my-orders',
    name: 'MyOrders',
    component: MyOrders,
    meta: { requiresAuth: true }
  },
  {
    path: '/my-reviews',
    name: 'MyReviews',
    component: MyReviews,
    meta: { requiresAuth: true }
  },
  {
    path: '/create-review',
    name: 'CreateReview',
    component: CreateReview,
    meta: { requiresAuth: true }
  },
  {
    path: '/order-detail',
    name: 'OrderDetail',
    component: OrderDetail,
    meta: { requiresAuth: true }
  },
  {
    path: '/services',
    name: 'Services',
    component: Services
  },
  {
    path: '/announcements',
    name: 'Announcements',
    component: Announcements
  },
  {
    path: '/about',
    name: 'About',
    component: About
  },
  // 门店端登录
  {
    path: '/shop/login',
    name: 'ShopLogin',
    component: ShopLogin
  },
  // 门店端路由
  {
    path: '/shop',
    component: ShopLayout,
    meta: { requiresAuth: true, requiresShop: true },
    children: [
      {
        path: '',
        redirect: '/shop/dashboard'
      },
      {
        path: 'dashboard',
        name: 'ShopDashboard',
        component: ShopDashboard,
        meta: { title: '仪表板' }
      },
      {
        path: 'orders',
        name: 'ShopOrderManagement',
        component: OrderManagement,
        meta: { title: '订单管理' }
      },
      {
        path: 'orders/:id',
        name: 'ShopOrderDetail',
        component: OrderDetail,
        meta: { title: '订单详情' }
      },
      {
        path: 'services',
        name: 'ServiceManagement',
        component: ServiceManagement,
        meta: { title: '服务项目管理' }
      },
      {
        path: 'packages',
        name: 'PackageManagement',
        component: PackageManagement,
        meta: { title: '套餐管理' }
      },
      {
        path: 'profile',
        name: 'ShopProfile',
        component: ShopProfile,
        meta: { title: '门店信息' }
      }
    ]
  },
  // 后台管理路由
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: AdminDashboard
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: UserManagement
      },
      {
        path: 'shops',
        name: 'ShopManagement',
        component: () => import('../views/admin/ShopManagement.vue')
      },
      {
        path: 'orders',
        name: 'OrderManagement',
        component: () => import('../views/admin/OrderManagement.vue')
      },
      {
        path: 'reviews',
        name: 'ReviewManagement',
        component: () => import('../views/admin/ReviewManagement.vue')
      },
      {
        path: 'members',
        name: 'MemberManagement',
        component: () => import('../views/admin/MemberManagement.vue')
      },
      {
        path: 'announcements',
        name: 'AnnouncementManagement',
        component: () => import('../views/admin/AnnouncementManagement.vue')
      },
      {
        path: 'price-monitor',
        name: 'PriceMonitor',
        component: () => import('../views/admin/PriceMonitor.vue')
      },
      {
        path: 'data-export',
        name: 'DataExport',
        component: () => import('../views/admin/DataExport.vue')
      },
      {
        path: 'data-statistics',
        name: 'DataStatistics',
        component: () => import('../views/admin/DataStatistics.vue')
      },
      {
        path: 'settings',
        name: 'SystemSettings',
        component: () => import('../views/admin/SystemSettings.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userToken = localStorage.getItem('token')
  const shopToken = localStorage.getItem('shopToken')
  const userRole = localStorage.getItem('userRole')

  // 如果是门店端路径（/shop/ 开头，不包括 /shops）
  if (to.path.startsWith('/shop/')) {
    // 如果不是登录页且没有shop token，重定向到门店登录
    if (to.path !== '/shop/login' && to.meta.requiresShop && !shopToken) {
      next('/shop/login')
      return
    }
    // 如果已有门店token，访问登录页，跳到dashboard
    if (to.path === '/shop/login' && shopToken) {
      next('/shop/dashboard')
      return
    }
    next()
    return
  }

  // 如果是管理员端路径
  if (to.path.startsWith('/admin')) {
    if (!userToken) {
      next('/login')
      return
    }
    if (to.meta.requiresAdmin && userRole !== 'admin') {
      next('/')
      return
    }
    next()
    return
  }

  // 普通用户端路径
  // 如果有门店token但访问用户端页面，跳回门店端（但允许访问/shops门店列表）
  if (shopToken && !to.path.startsWith('/shop') && to.path !== '/login' && to.path !== '/register' && to.path !== '/shops') {
    next('/shop/dashboard')
    return
  }

  // 检查是否需要用户登录
  if (to.meta.requiresAuth && !userToken) {
    next('/login')
    return
  }

  next()
})

export default router