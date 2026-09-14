I. Đề tài

Hệ thống quản lý giao hàng (Delivery Management): tạo đơn, tính cước, phân công shipper, theo dõi hành trình realtime, thanh toán VNPay/COD và đối soát tiền thu hộ.

II. Công nghệ

| Thành phần | Công nghệ |
| --- | --- |
| Backend | Java 21, Spring Boot 3.5, Spring Security + JWT, Spring Data JPA, Liquibase, WebSocket STOMP |
| Frontend | React 18, TypeScript, Vite, Ant Design, Leaflet, Recharts |
| Hạ tầng | PostgreSQL 16, Redis 7, MinIO, Docker Compose |

III. Chức năng

- Quản lý người dùng, đăng nhập JWT (refresh token, thu hồi token qua Redis) và phân quyền theo `function_code`
- Quản lý hồ sơ shipper, tải hiện tại và hiệu suất
- Quản lý đơn hàng (tìm kiếm, lịch sử trạng thái, import/export Excel)
- Điều phối tự động theo 3 chiến lược: gần nhất, ít tải nhất, luân phiên
- Thanh toán VNPay (HMAC-SHA512, IPN bất biến) và cổng giả lập khi chưa cấu hình sandbox
- Tracking GPS realtime qua WebSocket; trang tra cứu công khai che dữ liệu cá nhân
- Tính cước theo loại dịch vụ (Strategy) và voucher
- Thông báo realtime; ví COD và đối soát kế toán
- Dashboard KPI, phạm vi dữ liệu theo vai trò

Phân quyền 3 lớp: ẩn menu ở frontend, `@PreAuthorize` ở API, lọc dữ liệu ở tầng truy vấn.

IV. Chạy bằng Docker

```bash
cp .env.example .env
docker compose up -d --build
```

Postgres trên máy host được map ra **15432** (tránh đụng PostgreSQL 5432 có sẵn). Trong Docker, backend vẫn nối `postgres:5432`.

Nếu `.env` đã có thì **không** copy lại `.env.example`, chỉ cần `docker compose up -d --build`.

| Thành phần | Địa chỉ |
| --- | --- |
| Giao diện | http://localhost:3000 |
| Swagger UI | http://localhost:8080/api/v1/swagger-ui.html |
| MinIO Console | http://localhost:9001 (`minioadmin` / `minioadmin123`) |

Đợi `docker compose ps` báo `backend` là `healthy` trước khi đăng nhập.

V. Tài khoản demo

| Tài khoản | Mật khẩu | Vai trò |
| --- | --- | --- |
| `admin` | `Admin@123` | Quản trị |
| `dispatcher` | `Dispatch@123` | Điều phối |
| `shipper02` | `Shipper@123` | Shipper |
| `customer01` | `Customer@123` | Khách hàng |

VI. Tài liệu

- [Tài liệu kỹ thuật](docs/technical-design.md) — kiến trúc, ERD, state machine, luồng nghiệp vụ
- [API reference](docs/api-reference.md) — endpoint và quyền truy cập

VII. Cấu trúc

```
backend/            Spring Boot API
frontend/           React SPA
docs/               Tài liệu kỹ thuật và API
scripts/            Tiện ích vận hành
docker-compose.yml
```
