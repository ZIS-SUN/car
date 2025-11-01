import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const url = config.url || ''
    const userToken = localStorage.getItem('token')
    const shopToken = localStorage.getItem('shopToken')

    const forceShopToken = config.headers && config.headers['X-Use-Shop-Token'] === 'true'
    if (forceShopToken && config.headers) {
      config.__useShopToken = true
      delete config.headers['X-Use-Shop-Token']
    }

    const isShopEndpoint = url.startsWith('/shop') || forceShopToken

    if (isShopEndpoint) {
      if (shopToken) {
        config.headers['Authorization'] = `Bearer ${shopToken}`
      }
    } else if (userToken) {
      config.headers['Authorization'] = `Bearer ${userToken}`
    }

    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  error => {
    const { response: errorResponse, config: errorConfig } = error || {}

    if (errorResponse && errorResponse.status === 401) {
      const requestUrl = errorConfig?.url || ''
      const isShopEndpoint = requestUrl.startsWith('/shop') || errorConfig?.__useShopToken

      ElMessage.error('登录已过期，请重新登录')

      if (isShopEndpoint) {
        localStorage.removeItem('shopToken')
        localStorage.removeItem('shopInfo')
        if (window.location.pathname.startsWith('/shop')) {
          window.location.href = '/shop/login'
        }
      } else {
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        localStorage.removeItem('userRole')
        localStorage.removeItem('userInfo')
        if (!window.location.pathname.startsWith('/shop')) {
          window.location.href = '/login'
        }
      }

      return Promise.reject(error)
    }

    ElMessage.error(errorResponse?.data?.message || error.message || '网络错误')
    return Promise.reject(error)
  }
)

// 用户相关API
export const authAPI = {
  login: (data) => request.post('/auth/login', data),
  register: (data) => request.post('/auth/register', data),
  logout: () => request.post('/auth/logout')
}

// 用户信息相关
export const userAPI = {
  getProfile: () => request.get('/user/profile'),
  updateProfile: (data) => request.put('/user/profile', data)
}

// 会员信息相关
export const memberAPI = {
  getMyInfo: () => request.get('/user-member/my'),
  getLevelInfo: (userId) => request.get(`/user-member/${userId}/check-upgrade`)
}

// 门店相关API
export const shopAPI = {
  getShopList: (params) => request.get('/shop/list', { params }),
  getShopDetail: (id) => request.get(`/shop/${id}`),
  getAvailableTimes: (shopId, date) => request.get('/appointment/available-slots', {
    params: {
      shopId,
      date
    }
  }),
  getServiceItems: (shopId, params) => request.get(`/service-item/shop/${shopId}`, { params }),
  getServicePackages: (shopId) => request.get(`/service-package/shop/${shopId}`),
  getReviewStats: (shopId) => request.get(`/review/stats/${shopId}`),
  getLatestReviews: (shopId, limit = 5) => request.get(`/review/latest/${shopId}`, { params: { limit } })
}

// 订单相关API
export const orderAPI = {
  createOrder: (data) => request.post('/orders', data),
  getOrderList: (params) => request.get('/orders/my', { params }),
  getOrderDetail: (id) => request.get(`/orders/${id}`),
  cancelOrder: (id, reason) => request.put(`/orders/${id}/cancel`, null, { params: { reason } }),
  payOrder: (id, paymentMethod) => request.put(`/orders/${id}/pay`, null, { params: { paymentMethod } })
}

// 评价相关API
export const reviewAPI = {
  createReview: (data) => request.post('/review', data),
  getMyReviews: (params) => request.get('/review/my', { params }),
  getReviewDetail: (id) => request.get(`/review/${id}`),
  updateReview: (id, data) => request.put(`/review/${id}`, data),
  deleteReview: (id) => request.delete(`/review/${id}`),
  checkReviewed: (orderId) => request.get('/review/check', { params: { orderId } })
}

// 车辆相关API
export const vehicleAPI = {
  getVehicleList: () => request.get('/vehicle'),
  createVehicle: (data) => request.post('/vehicle', data),
  updateVehicle: (id, data) => request.put(`/vehicle/${id}`, data),
  deleteVehicle: (id) => request.delete(`/vehicle/${id}`)
}

export default request
