import request from './index'

// 管理员API - 调用真实后端API
export const adminAPI = {
  // ==================== 系统统计 ====================
  getSystemStats: () => request.get('/admin/statistics'),

  // ==================== 用户管理 ====================
  getUserList: (params) => request.get('/admin/users', { params }),
  updateUserStatus: (userId, status) => request.put(`/admin/users/${userId}/status`, null, { params: { status } }),
  deleteUser: (userId) => request.delete(`/admin/users/${userId}`),
  getUserStats: () => request.get('/admin/users/stats'),

  // ==================== 门店管理 ====================
  getShopList: (params) => request.get('/admin/shops', { params }),
  getShopStats: () => request.get('/admin/shops/stats'),
  approveShop: (shopId) => request.put(`/admin/shops/${shopId}/approve`),
  rejectShop: (shopId, reason) => request.put(`/admin/shops/${shopId}/reject`, { reason }),
  createShop: (data) => request.post('/admin/shops', data),
  updateShop: (shopId, data) => request.put(`/admin/shops/${shopId}`, data),
  deleteShop: (shopId) => request.delete(`/admin/shops/${shopId}`),
  suspendShop: (shopId) => request.put(`/admin/shops/${shopId}/suspend`),
  activateShop: (shopId) => request.put(`/admin/shops/${shopId}/activate`),
  updateShopStatus: (shopId, status) => request.put(`/admin/shops/${shopId}/status`, null, { params: { status } }),

  // ==================== 订单管理 ====================
  getOrderList: (params) => request.get('/admin/orders', { params }),
  getOrderStats: (params) => request.get('/admin/orders/stats', { params }),
  getRecentOrders: (params) => request.get('/admin/orders/recent', { params }),

  // ==================== 评价管理 ====================
  getReviewList: (params) => request.get('/admin/reviews', { params }),
  deleteReview: (reviewId) => request.delete(`/admin/reviews/${reviewId}`),

  // ==================== 公告管理 ====================
  getAnnouncementList: (params) => request.get('/announcement/list', { params }),
  createAnnouncement: (data) => request.post('/announcement', data),
  updateAnnouncement: (id, data) => request.put(`/announcement/${id}`, data),
  deleteAnnouncement: (id) => request.delete(`/announcement/${id}`),
  publishAnnouncement: (id) => request.put(`/announcement/${id}/publish`),
  offlineAnnouncement: (id) => request.put(`/announcement/${id}/offline`),

  // ==================== 会员等级管理 ====================
  getMemberLevels: () => request.get('/member-level/all'),
  createMemberLevel: (data) => request.post('/member-level', data),
  updateMemberLevel: (level, data) => request.put(`/member-level/${level}`, data),
  deleteMemberLevel: (level) => request.delete(`/member-level/${level}`),
  getMemberStats: () => request.get('/member-level/stats'),

  // ==================== 系统配置 ====================
  getSystemConfigs: () => request.get('/admin/configs'),
  updateSystemConfig: (key, value) => request.put('/admin/configs', null, { params: { key, value } }),

  // ==================== 数据导出 ====================
  exportUsers: (userType) => request.get('/admin/export/users', { params: { userType }, responseType: 'blob' }),
  exportOrders: (params) => request.get('/admin/export/orders', { params, responseType: 'blob' }),
  exportFinancialData: (params) => request.get('/admin/export/financial', { params, responseType: 'blob' })
}

export default adminAPI
