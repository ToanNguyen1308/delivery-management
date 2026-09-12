import apiClient, { unwrap } from './client';
import type {
  Assignment,
  CodSettlement,
  CodWallet,
  DashboardOverview,
  ExcelImportResult,
  FeePreview,
  FunctionItem,
  LoginResponse,
  NotificationItem,
  Order,
  OrderCountByDate,
  OrderCountByStatus,
  OrderSearchRequest,
  OrderStatusHistory,
  OrderSummary,
  OrderTracking,
  PageResponse,
  Payment,
  PaymentInit,
  PaymentResult,
  PricingRule,
  RevenueByProvince,
  RoleGroup,
  Shipper,
  ShipperLocation,
  ShipperPerformance,
  StoredFile,
  TopShipper,
  User,
  Voucher,
} from '@/types';

/* ================= Xac thuc ================= */
export const authApi = {
  login: (username: string, password: string) =>
    apiClient.post('/auth/login', { username, password }).then(unwrap<LoginResponse>),
  register: (payload: Record<string, unknown>) =>
    apiClient.post('/auth/register', payload).then(unwrap<User>),
  logout: () => apiClient.post('/auth/logout').then(() => undefined),
  me: () => apiClient.get('/auth/me').then(unwrap<User>),
  changePassword: (oldPassword: string, newPassword: string) =>
    apiClient.post('/auth/change-password', { oldPassword, newPassword }).then(() => undefined),
};

/* ================= Nguoi dung & nhom quyen ================= */
export const userApi = {
  search: (payload: Record<string, unknown>) =>
    apiClient.post('/users/search', payload).then(unwrap<PageResponse<User>>),
  getById: (id: number) => apiClient.get(`/users/${id}`).then(unwrap<User>),
  create: (payload: Record<string, unknown>) => apiClient.post('/users', payload).then(unwrap<User>),
  update: (id: number, payload: Record<string, unknown>) =>
    apiClient.put(`/users/${id}`, payload).then(unwrap<User>),
  remove: (id: number) => apiClient.delete(`/users/${id}`).then(() => undefined),
  resetPassword: (id: number, newPassword: string) =>
    apiClient.put(`/users/${id}/reset-password`, null, { params: { newPassword } }).then(() => undefined),
};

export const roleApi = {
  list: () => apiClient.get('/role-groups').then(unwrap<RoleGroup[]>),
  functions: () => apiClient.get('/role-groups/functions').then(unwrap<FunctionItem[]>),
  create: (payload: Record<string, unknown>) => apiClient.post('/role-groups', payload).then(unwrap<RoleGroup>),
  update: (id: number, payload: Record<string, unknown>) =>
    apiClient.put(`/role-groups/${id}`, payload).then(unwrap<RoleGroup>),
  remove: (id: number) => apiClient.delete(`/role-groups/${id}`).then(() => undefined),
};

/* ================= Shipper ================= */
export const shipperApi = {
  search: (payload: Record<string, unknown>) =>
    apiClient.post('/shippers/search', payload).then(unwrap<PageResponse<Shipper>>),
  available: () => apiClient.get('/shippers/available').then(unwrap<Shipper[]>),
  getById: (id: number) => apiClient.get(`/shippers/${id}`).then(unwrap<Shipper>),
  myProfile: () => apiClient.get('/shippers/me').then(unwrap<Shipper>),
  myPerformance: () => apiClient.get('/shippers/me/performance').then(unwrap<ShipperPerformance>),
  updateMyStatus: (status: string) =>
    apiClient.put('/shippers/me/status', null, { params: { status } }).then(unwrap<Shipper>),
  create: (payload: Record<string, unknown>) => apiClient.post('/shippers', payload).then(unwrap<Shipper>),
  update: (id: number, payload: Record<string, unknown>) =>
    apiClient.put(`/shippers/${id}`, payload).then(unwrap<Shipper>),
  remove: (id: number) => apiClient.delete(`/shippers/${id}`).then(() => undefined),
};

/* ================= Don hang ================= */
export const orderApi = {
  search: (payload: OrderSearchRequest) =>
    apiClient.post('/orders/search', payload).then(unwrap<PageResponse<OrderSummary>>),
  getById: (id: number) => apiClient.get(`/orders/${id}`).then(unwrap<Order>),
  getByCode: (code: string) => apiClient.get(`/orders/code/${code}`).then(unwrap<Order>),
  history: (id: number) => apiClient.get(`/orders/${id}/history`).then(unwrap<OrderStatusHistory[]>),
  create: (payload: Record<string, unknown>) => apiClient.post('/orders', payload).then(unwrap<Order>),
  update: (id: number, payload: Record<string, unknown>) =>
    apiClient.put(`/orders/${id}`, payload).then(unwrap<Order>),
  confirm: (id: number) => apiClient.put(`/orders/${id}/confirm`).then(unwrap<Order>),
  updateStatus: (id: number, payload: Record<string, unknown>) =>
    apiClient.put(`/orders/${id}/status`, payload).then(unwrap<Order>),
  cancel: (id: number, reason?: string) =>
    apiClient.delete(`/orders/${id}`, { params: { reason } }).then(() => undefined),
  exportExcel: (payload: OrderSearchRequest) =>
    apiClient.post('/orders/export', payload, { responseType: 'blob' }).then((r) => r.data as Blob),
  downloadTemplate: () =>
    apiClient.get('/orders/import-template', { responseType: 'blob' }).then((r) => r.data as Blob),
  importExcel: (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return apiClient
      .post('/orders/import', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
      .then(unwrap<ExcelImportResult>);
  },
};

/* ================= Dieu phoi ================= */
export const dispatchApi = {
  assign: (orderId: number, shipperId?: number, note?: string) =>
    apiClient.post('/dispatch/assign', { orderId, shipperId, note }).then(unwrap<Assignment>),
  respond: (assignmentId: number, accepted: boolean, rejectReason?: string) =>
    apiClient
      .put(`/dispatch/assignments/${assignmentId}/respond`, { accepted, rejectReason })
      .then(unwrap<Assignment>),
  myAssignments: (payload: Record<string, unknown>) =>
    apiClient.post('/dispatch/my-assignments', payload).then(unwrap<PageResponse<Assignment>>),
  byOrder: (orderId: number) =>
    apiClient.get(`/dispatch/orders/${orderId}/assignments`).then(unwrap<Assignment[]>),
};

/* ================= Phi va voucher ================= */
export const pricingApi = {
  preview: (payload: Record<string, unknown>) =>
    apiClient.post('/pricing/preview', payload).then(unwrap<FeePreview>),
  rules: () => apiClient.get('/pricing/rules').then(unwrap<PricingRule[]>),
  updateRule: (id: number, payload: Record<string, unknown>) =>
    apiClient.put(`/pricing/rules/${id}`, payload).then(unwrap<PricingRule>),
};

export const voucherApi = {
  search: (payload: Record<string, unknown>) =>
    apiClient.post('/vouchers/search', payload).then(unwrap<PageResponse<Voucher>>),
  create: (payload: Record<string, unknown>) => apiClient.post('/vouchers', payload).then(unwrap<Voucher>),
  update: (id: number, payload: Record<string, unknown>) =>
    apiClient.put(`/vouchers/${id}`, payload).then(unwrap<Voucher>),
  remove: (id: number) => apiClient.delete(`/vouchers/${id}`).then(() => undefined),
};

/* ================= Thanh toan ================= */
export const paymentApi = {
  create: (orderId: number, bankCode?: string) =>
    apiClient.post('/payments/create', { orderId, bankCode }).then(unwrap<PaymentInit>),
  handleReturn: (params: Record<string, string>) =>
    apiClient.get('/payments/vnpay/return', { params }).then(unwrap<PaymentResult>),
  completeMock: (txnRef: string, success = true) =>
    apiClient.post('/payments/mock/complete', null, { params: { txnRef, success } }).then(unwrap<PaymentResult>),
  search: (payload: Record<string, unknown>) =>
    apiClient.post('/payments/search', payload).then(unwrap<PageResponse<Payment>>),
  byOrder: (orderId: number) => apiClient.get(`/payments/order/${orderId}`).then(unwrap<Payment[]>),
};

export const codApi = {
  myWallet: () => apiClient.get('/cod-settlements/my-wallet').then(unwrap<CodWallet>),
  mySettlements: (payload: Record<string, unknown>) =>
    apiClient.post('/cod-settlements/my-settlements', payload).then(unwrap<PageResponse<CodSettlement>>),
  search: (payload: Record<string, unknown>) =>
    apiClient.post('/cod-settlements/search', payload).then(unwrap<PageResponse<CodSettlement>>),
  submitAll: () => apiClient.post('/cod-settlements/submit-all').then(unwrap<number>),
  confirm: (id: number, note?: string) =>
    apiClient.put(`/cod-settlements/${id}/confirm`, null, { params: { note } }).then(unwrap<CodSettlement>),
};

/* ================= Tracking & thong bao ================= */
export const trackingApi = {
  pushLocation: (payload: Record<string, unknown>) =>
    apiClient.post('/tracking/location', payload).then(unwrap<ShipperLocation>),
  byOrder: (orderId: number) => apiClient.get(`/tracking/orders/${orderId}`).then(unwrap<OrderTracking>),
  publicTracking: (orderCode: string) =>
    apiClient.get(`/public/tracking/${orderCode}`).then(unwrap<OrderTracking>),
};

export const notificationApi = {
  search: (payload: Record<string, unknown>) =>
    apiClient.post('/notifications/search', payload).then(unwrap<PageResponse<NotificationItem>>),
  unreadCount: () => apiClient.get('/notifications/unread-count').then(unwrap<number>),
  markRead: (id: number) => apiClient.put(`/notifications/${id}/read`).then(() => undefined),
  markAllRead: () => apiClient.put('/notifications/read-all').then(unwrap<number>),
};

/* ================= Dashboard ================= */
export const dashboardApi = {
  overview: (payload: Record<string, unknown>) =>
    apiClient.post('/dashboard/overview', payload).then(unwrap<DashboardOverview>),
  ordersByStatus: (payload: Record<string, unknown>) =>
    apiClient.post('/dashboard/orders-by-status', payload).then(unwrap<OrderCountByStatus[]>),
  ordersByDate: (payload: Record<string, unknown>) =>
    apiClient.post('/dashboard/orders-by-date', payload).then(unwrap<OrderCountByDate[]>),
  revenueByProvince: (payload: Record<string, unknown>) =>
    apiClient.post('/dashboard/revenue-by-province', payload).then(unwrap<RevenueByProvince[]>),
  topShippers: (payload: Record<string, unknown>) =>
    apiClient.post('/dashboard/top-shippers', payload).then(unwrap<TopShipper[]>),
};

/* ================= File ================= */
export const fileApi = {
  upload: (file: File, feature: string, referenceId?: number) => {
    const formData = new FormData();
    formData.append('file', file);
    return apiClient
      .post('/files/upload', formData, {
        params: { feature, referenceId },
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      .then(unwrap<StoredFile>);
  },
};
