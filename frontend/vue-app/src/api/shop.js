import request from './index'

const withShopToken = (config = {}) => {
  const headers = { ...(config.headers || {}), 'X-Use-Shop-Token': 'true' }
  return { ...config, headers }
}

// 门店端API - 调用真实后端API
export const shopAPI = {
  // ==================== 认证相关 ====================
  login: (data) => request.post('/shop/auth/login', data),
  logout: () => request.post('/shop/auth/logout', null, withShopToken()),
  getShopInfo: () => request.get('/shop/auth/info', withShopToken()),
  getShopProfile: () => request.get('/shop/my', withShopToken()),
  updateShopProfile: (data) => request.put('/shop/my', data, withShopToken()),

  // ==================== 统计数据 ====================
  getOrderStats: () => request.get('/orders/stats', withShopToken()),
  getAppointmentStats: () => request.get('/appointment/stats', withShopToken()),

  // ==================== 订单管理 ====================
  getOrderList: (params) => request.get('/orders/shop', withShopToken({ params })),
  getOrderDetail: (orderId) => request.get(`/orders/${orderId}`, withShopToken()),
  startOrder: (orderId, technicianId) => request.put(`/orders/${orderId}/start`, null, withShopToken(technicianId ? { params: { technicianId } } : {})),
  completeOrder: (orderId) => request.put(`/orders/${orderId}/complete`, null, withShopToken()),
  updateOrderStatus: (orderId, status) => request.put(`/orders/${orderId}/status`, null, withShopToken({ params: { status } })),
  assignTechnician: (orderId, technicianId) => request.put(`/orders/${orderId}/technician`, null, withShopToken({ params: { technicianId } })),
  getShopAppointments: (params) => request.get('/appointment/shop', withShopToken({ params })),
  getAppointmentDetail: (appointmentId) => request.get(`/appointment/${appointmentId}`, withShopToken()),
  updateAppointmentStatus: (appointmentId, status) => request.put(`/appointment/${appointmentId}/status`, null, withShopToken({ params: { status } })),
  assignAppointmentBay: (appointmentId, bayNumber) => request.put(`/appointment/${appointmentId}/bay`, null, withShopToken({ params: { bayNumber } })),
  getReviewStats: (shopId) => request.get(`/review/stats/${shopId}`, withShopToken()),

  // ==================== 服务项目管理 ====================
  getServiceList: (params) => request.get('/service-item/my', withShopToken({ params })),
  getServiceDetail: (serviceId) => request.get(`/service-item/${serviceId}`, withShopToken()),
  createService: (data) => request.post('/service-item', data, withShopToken()),
  updateService: (serviceId, data) => request.put(`/service-item/${serviceId}`, data, withShopToken()),
  deleteService: (serviceId) => request.delete(`/service-item/${serviceId}`, withShopToken()),
  updateServiceStatus: (serviceIds, status) => request.put('/service-item/batch-status', serviceIds, withShopToken({ params: { status } })),
  getServiceStats: () => request.get('/service-item/stats', withShopToken()),

  // ==================== 套餐管理 ====================
  getPackageList: () => request.get('/service-package/my', withShopToken()),
  getPackageDetail: (packageId) => request.get(`/service-package/${packageId}`, withShopToken()),
  createPackage: (data) => request.post('/service-package', data, withShopToken()),
  updatePackage: (packageId, data) => request.put(`/service-package/${packageId}`, data, withShopToken()),
  deletePackage: (packageId) => request.delete(`/service-package/${packageId}`, withShopToken()),
  updatePackageStatus: (packageIds, status) => request.put('/service-package/batch-status', packageIds, withShopToken({ params: { status } })),
  getPackageStats: () => request.get('/service-package/stats', withShopToken())
}

export default shopAPI
