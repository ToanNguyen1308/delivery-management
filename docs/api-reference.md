# Danh sách API

Base URL: `http://localhost:8080/api/v1`. Tài liệu tương tác đầy đủ (có thể gọi thử trực tiếp) tại `http://localhost:8080/api/v1/swagger-ui.html`.

## Quy ước chung

Mọi response đều theo cùng một cấu trúc:

```json
{
  "code": "success",
  "message": "Success",
  "data": { },
  "timestamp": "2026-09-11T15:08:34"
}
```

Khi lỗi, `code` là mã lỗi định danh và `message` đã được dịch theo header `Accept-Language` (`vi` mặc định, hỗ trợ `en`):

```json
{
  "code": "error.order.invalidTransition",
  "message": "Không thể chuyển đơn hàng từ trạng thái Chờ xác nhận sang Giao thành công",
  "data": null
}
```

Lỗi validate trả thêm chi tiết từng trường trong `errors`.

| Quy ước | Chi tiết |
| --- | --- |
| Xác thực | Header `Authorization: Bearer <accessToken>` |
| Tìm kiếm | Dùng `POST .../search` với body chứa điều kiện, tránh URL quá dài |
| Phân trang | `page` (từ 0), `size`, `sortBy`, `sortDirection` trong body |
| Enum | Trả về dạng `{ "code": "DELIVERED", "description": "Giao thành công" }` |
| Mã HTTP | 200 thành công, 201 tạo mới, 400 sai dữ liệu, 401 chưa xác thực, 403 không đủ quyền, 404 không tìm thấy, 500 lỗi hệ thống |

## 00. Công khai (không cần đăng nhập)

| Method | Endpoint | Mô tả |
| --- | --- | --- |
| GET | `/public/tracking/{orderCode}` | Tra cứu vận đơn, tên người nhận và địa chỉ được che một phần |
| GET | `/public/enums` | Danh mục enum cho dropdown |
| GET | `/payments/vnpay/ipn` | Nhận IPN từ VNPay (server-to-server) |

## 01. Xác thực

| Method | Endpoint | Quyền | Mô tả |
| --- | --- | --- | --- |
| POST | `/auth/login` | Công khai | Trả access token, refresh token và danh sách chức năng |
| POST | `/auth/register` | Công khai | Đăng ký tài khoản khách hàng |
| POST | `/auth/refresh` | Công khai | Làm mới access token, token cũ bị thu hồi ngay |
| POST | `/auth/logout` | Đã đăng nhập | Thu hồi access token hiện tại và toàn bộ refresh token |
| GET | `/auth/me` | Đã đăng nhập | Thông tin tài khoản đang đăng nhập |
| POST | `/auth/change-password` | Đã đăng nhập | Đổi mật khẩu |

## 02. Quản lý người dùng

| Method | Endpoint | Quyền |
| --- | --- | --- |
| POST | `/users/search` | `ROLE_USER_VIEW` |
| GET | `/users/{id}` | `ROLE_USER_VIEW` |
| POST | `/users` | `ROLE_USER_CREATE` |
| PUT | `/users/{id}` | `ROLE_USER_UPDATE` |
| PUT | `/users/{id}/reset-password` | `ROLE_USER_UPDATE` |
| DELETE | `/users/{id}` | `ROLE_USER_DELETE` |

Điều kiện tìm kiếm: `keyword`, `status`, `roleGroupCode`, `fromDate`, `toDate`.

## 03. Nhóm quyền

| Method | Endpoint | Quyền |
| --- | --- | --- |
| GET | `/role-groups` | `ROLE_ROLE_VIEW` |
| GET | `/role-groups/functions` | `ROLE_ROLE_VIEW` |
| GET | `/role-groups/{id}` | `ROLE_ROLE_VIEW` |
| POST | `/role-groups` | `ROLE_ROLE_MANAGE` |
| PUT | `/role-groups/{id}` | `ROLE_ROLE_MANAGE` |
| DELETE | `/role-groups/{id}` | `ROLE_ROLE_MANAGE` |

## 04. Quản lý shipper

| Method | Endpoint | Quyền | Mô tả |
| --- | --- | --- | --- |
| POST | `/shippers/search` | `ROLE_SHIPPER_VIEW` | Lọc theo từ khóa, trạng thái, phương tiện, khu vực, còn chỗ nhận đơn |
| GET | `/shippers/available` | `ROLE_DISPATCH_VIEW` | Shipper còn chỗ nhận đơn, dùng cho màn hình điều phối |
| GET | `/shippers/{id}` | `ROLE_SHIPPER_VIEW` | |
| GET | `/shippers/{id}/performance` | `ROLE_SHIPPER_VIEW` | |
| GET | `/shippers/me` | `ROLE_SHIPPER_SELF` | Hồ sơ của shipper đang đăng nhập |
| GET | `/shippers/me/performance` | `ROLE_SHIPPER_SELF` | |
| PUT | `/shippers/me/status` | `ROLE_SHIPPER_SELF` | Tự đổi trạng thái trực tuyến |
| POST | `/shippers` | `ROLE_SHIPPER_CREATE` | |
| PUT | `/shippers/{id}` | `ROLE_SHIPPER_UPDATE` | |
| DELETE | `/shippers/{id}` | `ROLE_SHIPPER_DELETE` | |

## 05. Tính phí

| Method | Endpoint | Quyền | Mô tả |
| --- | --- | --- | --- |
| POST | `/pricing/preview` | `ROLE_PRICING_VIEW` | Trả chi tiết từng thành phần phí và số tiền được giảm |
| GET | `/pricing/rules` | `ROLE_PRICING_VIEW` | |
| PUT | `/pricing/rules/{id}` | `ROLE_PRICING_MANAGE` | |

Ví dụ request:

```json
{
  "serviceType": "EXPRESS",
  "weightKg": 5,
  "pickupLatitude": 21.0285,
  "pickupLongitude": 105.8542,
  "deliveryLatitude": 21.0136,
  "deliveryLongitude": 105.8290,
  "codAmount": 500000,
  "voucherCode": "GIAM20"
}
```

Ví dụ response:

```json
{
  "distanceKm": 4.03,
  "baseFee": 25000,
  "distanceFee": 6180,
  "weightFee": 14000,
  "surcharge": 927,
  "codFee": 7500,
  "shippingFee": 53607,
  "discountAmount": 10721,
  "totalAmount": 42886,
  "voucherApplied": true
}
```

## 06. Voucher

| Method | Endpoint | Quyền |
| --- | --- | --- |
| POST | `/vouchers/search` | `ROLE_VOUCHER_VIEW` |
| GET | `/vouchers/{id}` | `ROLE_VOUCHER_VIEW` |
| POST | `/vouchers` | `ROLE_VOUCHER_MANAGE` |
| PUT | `/vouchers/{id}` | `ROLE_VOUCHER_MANAGE` |
| DELETE | `/vouchers/{id}` | `ROLE_VOUCHER_MANAGE` |

## 07. Quản lý đơn hàng

| Method | Endpoint | Quyền | Mô tả |
| --- | --- | --- | --- |
| POST | `/orders/search` | `ROLE_ORDER_VIEW` | Kết quả tự giới hạn theo quyền dữ liệu |
| GET | `/orders/{id}` | `ROLE_ORDER_VIEW` | |
| GET | `/orders/code/{orderCode}` | `ROLE_ORDER_VIEW` | |
| GET | `/orders/{id}/history` | `ROLE_ORDER_VIEW` | Lịch sử chuyển trạng thái |
| POST | `/orders` | `ROLE_ORDER_CREATE` | Tự tính cước và áp voucher |
| PUT | `/orders/{id}` | `ROLE_ORDER_UPDATE` | Chỉ khi đơn còn ở trạng thái cho phép sửa |
| PUT | `/orders/{id}/confirm` | `ROLE_ORDER_CONFIRM` | |
| PUT | `/orders/{id}/status` | `ROLE_ORDER_VIEW` | Kiểm tra theo state machine |
| DELETE | `/orders/{id}` | `ROLE_ORDER_CANCEL` | Hủy đơn, hoàn lại lượt voucher |
| POST | `/orders/export` | `ROLE_ORDER_EXPORT` | Trả file `.xlsx` |
| GET | `/orders/import-template` | `ROLE_ORDER_IMPORT` | File Excel mẫu kèm dòng ví dụ |
| POST | `/orders/import` | `ROLE_ORDER_IMPORT` | Trả số dòng thành công, số dòng lỗi và chi tiết lỗi từng dòng |

Điều kiện tìm kiếm đơn hàng: `keyword`, `statuses` (nhiều giá trị), `paymentStatus`, `paymentMethod`, `serviceType`, `shipperId`, `customerId`, `deliveryProvince`, `deliveryDistrict`, `unassignedOnly`, `fromDate`, `toDate`.

## 08. Điều phối

| Method | Endpoint | Quyền | Mô tả |
| --- | --- | --- | --- |
| POST | `/dispatch/assign` | `ROLE_DISPATCH_ASSIGN` | Bỏ trống `shipperId` để hệ thống tự chọn |
| PUT | `/dispatch/assignments/{id}/respond` | `ROLE_DISPATCH_RESPOND` | Shipper nhận hoặc từ chối |
| POST | `/dispatch/my-assignments` | `ROLE_DISPATCH_RESPOND` | Nhiệm vụ của shipper đang đăng nhập |
| GET | `/dispatch/orders/{orderId}/assignments` | `ROLE_DISPATCH_VIEW` | Lịch sử phân công của một đơn |

## 09. Thanh toán

| Method | Endpoint | Quyền | Mô tả |
| --- | --- | --- | --- |
| POST | `/payments/create` | `ROLE_PAYMENT_CREATE` | Trả `payUrl` đã ký để chuyển hướng |
| GET | `/payments/vnpay/return` | Đã đăng nhập | Xác thực chữ ký ReturnUrl |
| GET | `/payments/vnpay/ipn` | Công khai | Trả `RspCode` theo chuẩn VNPay |
| POST | `/payments/mock/complete` | `ROLE_PAYMENT_CREATE` | Hoàn tất giao dịch giả lập khi chưa cấu hình sandbox |
| POST | `/payments/search` | `ROLE_PAYMENT_VIEW` | |
| GET | `/payments/order/{orderId}` | `ROLE_PAYMENT_VIEW` | |

Bộ mã trả về cho IPN: `00` thành công, `01` không tìm thấy đơn, `02` đã xử lý trước đó, `04` số tiền không khớp, `97` chữ ký không hợp lệ, `99` lỗi không xác định.

## 10. Đối soát COD

| Method | Endpoint | Quyền |
| --- | --- | --- |
| GET | `/cod-settlements/my-wallet` | `ROLE_COD_VIEW` |
| POST | `/cod-settlements/my-settlements` | `ROLE_COD_VIEW` |
| POST | `/cod-settlements/search` | `ROLE_COD_CONFIRM` |
| POST | `/cod-settlements/submit-all` | `ROLE_COD_SUBMIT` |
| PUT | `/cod-settlements/{id}/confirm` | `ROLE_COD_CONFIRM` |

## 11. File

| Method | Endpoint | Mô tả |
| --- | --- | --- |
| POST | `/files/upload-temp` | Upload vào `temp/`, chờ nghiệp vụ xác nhận |
| POST | `/files/upload` | Upload thẳng vào thư mục nghiệp vụ |

Định dạng cho phép: `jpg`, `jpeg`, `png`, `webp`, `pdf`, `xlsx`, `xls`. Giới hạn 15MB mỗi file.

## 12. Tracking

| Method | Endpoint | Quyền | Mô tả |
| --- | --- | --- | --- |
| POST | `/tracking/location` | `ROLE_TRACKING_PUSH` | Shipper đẩy GPS, hệ thống broadcast realtime |
| GET | `/tracking/orders/{orderId}` | `ROLE_TRACKING_VIEW` | Timeline sự kiện và toàn bộ điểm GPS |

## 13. Thông báo

| Method | Endpoint | Mô tả |
| --- | --- | --- |
| POST | `/notifications/search` | Danh sách thông báo của tôi |
| GET | `/notifications/unread-count` | Số thông báo chưa đọc |
| PUT | `/notifications/{id}/read` | Đánh dấu một thông báo đã đọc |
| PUT | `/notifications/read-all` | Đánh dấu tất cả đã đọc |

## 14. Dashboard

| Method | Endpoint | Quyền | Mô tả |
| --- | --- | --- | --- |
| POST | `/dashboard/overview` | `ROLE_DASHBOARD_VIEW` | KPI, tự giới hạn theo vai trò |
| POST | `/dashboard/orders-by-status` | `ROLE_DASHBOARD_ADMIN` | |
| POST | `/dashboard/orders-by-date` | `ROLE_DASHBOARD_ADMIN` | |
| POST | `/dashboard/revenue-by-province` | `ROLE_DASHBOARD_ADMIN` | |
| POST | `/dashboard/top-shippers` | `ROLE_DASHBOARD_ADMIN` | |

Tất cả nhận chung body `{ "fromDate": "2026-08-12", "toDate": "2026-09-11", "topSize": 5 }`. Bỏ trống khoảng ngày sẽ lấy 30 ngày gần nhất.

## WebSocket

Endpoint STOMP: `/api/v1/ws` (SockJS). Token truyền qua query param `access_token` vì handshake SockJS không đặt được header.

| Kênh | Nội dung |
| --- | --- |
| `/topic/orders/{orderCode}` | Vị trí shipper và sự kiện mới của đơn hàng |
| `/topic/shippers/location` | Vị trí toàn bộ shipper, dùng cho màn hình điều phối |
| `/user/queue/notifications` | Thông báo riêng của từng người dùng |
