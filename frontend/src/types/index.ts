/** Cau truc response thong nhat cua backend: {code, message, data}. */
export interface ApiResponse<T> {
  code: string;
  message: string;
  data: T;
  errors?: Record<string, string>;
  timestamp?: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

/** Enum tu backend luon kem mo ta de hien thi truc tiep. */
export interface EnumValue {
  code: string;
  description: string;
}

export interface BaseSearchRequest {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'ASC' | 'DESC';
}

/* ---------- Nguoi dung & phan quyen ---------- */
export interface RoleGroupSummary {
  id: number;
  roleGroupCode: string;
  roleGroupName: string;
}

export interface FunctionItem {
  id: number;
  functionCode: string;
  functionName: string;
  module: string;
  description?: string;
}

export interface RoleGroup extends RoleGroupSummary {
  description?: string;
  functions: FunctionItem[];
}

export interface User {
  id: number;
  username: string;
  fullName: string;
  email?: string;
  phoneNumber?: string;
  identityNumber?: string;
  birthday?: string;
  address?: string;
  avatarUrl?: string;
  status: EnumValue;
  lastLoginAt?: string;
  createdDate?: string;
  roleGroups?: RoleGroupSummary[];
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
  permissions: string[];
  roleGroups: string[];
}

/* ---------- Shipper ---------- */
export interface Shipper {
  id: number;
  shipperCode: string;
  userId: number;
  username: string;
  fullName: string;
  phoneNumber?: string;
  email?: string;
  vehicleType: EnumValue;
  licensePlate?: string;
  maxLoadKg?: number;
  zone?: string;
  status: EnumValue;
  currentLatitude?: number;
  currentLongitude?: number;
  lastLocationAt?: string;
  rating?: number;
  totalDelivered: number;
  totalFailed: number;
  currentLoad: number;
  maxConcurrentOrders: number;
  idCardUrl?: string;
  driverLicenseUrl?: string;
  note?: string;
}

export interface ShipperPerformance {
  shipperId: number;
  shipperCode: string;
  fullName: string;
  totalAssigned: number;
  totalDelivered: number;
  totalFailed: number;
  inProgress: number;
  successRate: number;
  rating: number;
}

/* ---------- Don hang ---------- */
export interface OrderItem {
  id?: number;
  itemName: string;
  quantity: number;
  unitPrice?: number;
  weightKg?: number;
}

export interface OrderSummary {
  id: number;
  orderCode: string;
  receiverName: string;
  receiverPhone: string;
  deliveryAddress: string;
  deliveryProvince?: string;
  serviceType: EnumValue;
  status: EnumValue;
  paymentMethod: EnumValue;
  paymentStatus: EnumValue;
  shippingFee: number;
  codAmount: number;
  totalAmount: number;
  customerName?: string;
  shipperName?: string;
  expectedDeliveryAt?: string;
  createdDate: string;
}

export interface Order extends OrderSummary {
  customerId: number;
  customerPhone?: string;
  senderName: string;
  senderPhone: string;
  pickupAddress: string;
  pickupDistrict?: string;
  pickupProvince?: string;
  pickupLatitude?: number;
  pickupLongitude?: number;
  deliveryDistrict?: string;
  deliveryLatitude?: number;
  deliveryLongitude?: number;
  packageDescription?: string;
  weightKg: number;
  lengthCm?: number;
  widthCm?: number;
  heightCm?: number;
  fragile?: boolean;
  distanceKm?: number;
  surcharge?: number;
  discountAmount?: number;
  voucherCode?: string;
  codSettlementStatus?: EnumValue;
  pickedUpAt?: string;
  deliveredAt?: string;
  shipperId?: number;
  shipperCode?: string;
  shipperPhone?: string;
  proofImageUrl?: string;
  cancelReason?: string;
  failureReason?: string;
  note?: string;
  createdBy?: string;
  items: OrderItem[];
  nextStatuses: EnumValue[];
}

export interface OrderStatusHistory {
  id: number;
  fromStatus?: EnumValue;
  toStatus: EnumValue;
  note?: string;
  changedByName?: string;
  changedAt: string;
}

export interface OrderSearchRequest extends BaseSearchRequest {
  keyword?: string;
  statuses?: string[];
  paymentStatus?: string;
  paymentMethod?: string;
  serviceType?: string;
  shipperId?: number;
  customerId?: number;
  deliveryProvince?: string;
  deliveryDistrict?: string;
  unassignedOnly?: boolean;
  fromDate?: string;
  toDate?: string;
}

/* ---------- Dieu phoi ---------- */
export interface Assignment {
  id: number;
  orderId: number;
  orderCode: string;
  receiverName: string;
  receiverPhone: string;
  pickupAddress: string;
  deliveryAddress: string;
  codAmount: number;
  weightKg: number;
  orderStatus: EnumValue;
  serviceType: EnumValue;
  shipperId: number;
  shipperCode: string;
  shipperName: string;
  status: EnumValue;
  assignType: EnumValue;
  distanceKm?: number;
  assignedAt: string;
  respondedAt?: string;
  expectedDeliveryAt?: string;
  rejectReason?: string;
  note?: string;
}

/* ---------- Phi va voucher ---------- */
export interface FeePreview {
  serviceType: EnumValue;
  distanceKm: number;
  baseFee: number;
  distanceFee: number;
  weightFee: number;
  surcharge: number;
  codFee: number;
  shippingFee: number;
  discountAmount: number;
  totalAmount: number;
  voucherCode?: string;
  voucherApplied: boolean;
  voucherMessage?: string;
}

export interface PricingRule {
  id: number;
  serviceType: EnumValue;
  baseFee: number;
  baseDistanceKm: number;
  perKmFee: number;
  baseWeightKg: number;
  perKgFee: number;
  remoteAreaSurcharge: number;
  codFeePercent: number;
  minFee: number;
  active: boolean;
}

export interface Voucher {
  id: number;
  code: string;
  name: string;
  discountType: EnumValue;
  discountValue: number;
  minOrderAmount: number;
  maxDiscountAmount?: number;
  quantity: number;
  usedCount: number;
  usageLimitPerUser: number;
  validFrom: string;
  validTo: string;
  status: EnumValue;
  description?: string;
}

/* ---------- Thanh toan ---------- */
export interface PaymentInit {
  paymentId: number;
  txnRef: string;
  orderCode: string;
  amount: number;
  payUrl: string;
  mockMode: boolean;
}

export interface Payment {
  id: number;
  txnRef: string;
  orderId: number;
  orderCode: string;
  provider: EnumValue;
  amount: number;
  status: EnumValue;
  transactionNo?: string;
  bankCode?: string;
  responseCode?: string;
  paidAt?: string;
  createdDate: string;
}

export interface PaymentResult {
  success: boolean;
  txnRef: string;
  orderCode: string;
  orderId: number;
  amount: number;
  responseCode?: string;
  message: string;
}

export interface CodWallet {
  shipperId: number;
  shipperCode: string;
  holdingAmount: number;
  submittedAmount: number;
  confirmedAmount: number;
  holdingCount: number;
  submittedCount: number;
}

export interface CodSettlement {
  id: number;
  orderId: number;
  orderCode: string;
  receiverName: string;
  shipperId: number;
  shipperCode: string;
  shipperName: string;
  amount: number;
  status: EnumValue;
  collectedAt?: string;
  submittedAt?: string;
  confirmedAt?: string;
  note?: string;
}

/* ---------- Tracking & thong bao ---------- */
export interface TrackingEvent {
  id: number;
  eventType: EnumValue;
  description?: string;
  latitude?: number;
  longitude?: number;
  locationName?: string;
  occurredAt: string;
}

export interface RoutePoint {
  latitude: number;
  longitude: number;
  recordedAt: string;
}

export interface OrderTracking {
  orderId: number;
  orderCode: string;
  status: EnumValue;
  serviceType: EnumValue;
  receiverName: string;
  deliveryAddress: string;
  deliveryLatitude?: number;
  deliveryLongitude?: number;
  pickupLatitude?: number;
  pickupLongitude?: number;
  expectedDeliveryAt?: string;
  pickedUpAt?: string;
  deliveredAt?: string;
  shipperName?: string;
  shipperPhone?: string;
  currentLatitude?: number;
  currentLongitude?: number;
  lastLocationAt?: string;
  proofImageUrl?: string;
  events: TrackingEvent[];
  route: RoutePoint[];
}

export interface ShipperLocation {
  shipperId: number;
  shipperCode: string;
  shipperName: string;
  orderId?: number;
  orderCode?: string;
  latitude: number;
  longitude: number;
  speedKmh?: number;
  recordedAt: string;
}

export interface NotificationItem {
  id: number;
  type: EnumValue;
  title: string;
  content: string;
  referenceType?: string;
  referenceId?: number;
  referenceCode?: string;
  isRead: boolean;
  readAt?: string;
  createdDate: string;
}

/* ---------- Dashboard ---------- */
export interface DashboardOverview {
  totalOrders: number;
  ordersToday: number;
  deliveredOrders: number;
  inProgressOrders: number;
  cancelledOrders: number;
  successRate: number;
  totalRevenue: number;
  revenueToday: number;
  totalCodCollected: number;
  totalShippers: number;
  onlineShippers: number;
  averageDeliveryHours: number;
}

export interface OrderCountByStatus {
  status: EnumValue;
  quantity: number;
}

export interface OrderCountByDate {
  date: string;
  quantity: number;
  revenue: number;
}

export interface RevenueByProvince {
  province: string;
  quantity: number;
  revenue: number;
}

export interface TopShipper {
  shipperId: number;
  shipperCode: string;
  fullName: string;
  deliveredCount: number;
  rating: number;
}

export interface ExcelImportResult {
  totalRows: number;
  successCount: number;
  failedCount: number;
  createdOrderCodes: string[];
  errors: { rowNumber: number; message: string }[];
}

export interface StoredFile {
  id: number;
  objectKey: string;
  bucket: string;
  originalName: string;
  contentType: string;
  sizeBytes: number;
  url: string;
}
