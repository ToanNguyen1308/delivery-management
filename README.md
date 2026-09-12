# Hệ thống quản lý giao hàng

Ứng dụng web quản lý giao hàng full-stack, xây dựng cho mini project thực tập Viettel Software.

| Thành phần | Công nghệ |
| --- | --- |
| Backend | Java 21, Spring Boot 3.5, Spring Security + JWT, Spring Data JPA, Liquibase, MapStruct, Apache POI, WebSocket STOMP |
| Frontend | React 18, TypeScript, Vite, Ant Design, TailwindCSS, Leaflet, Recharts, STOMP over SockJS |
| Dữ liệu | PostgreSQL 16, Redis 7 (cache + thu hồi token), MinIO (lưu file) |
| Triển khai | Docker + Docker Compose (một lệnh dựng toàn bộ hệ thống) |

## Chạy nhanh bằng Docker

```bash
cp .env.example .env
docker compose up -d --build
```

| Thành phần | Địa chỉ |
| --- | --- |
| Giao diện web | http://localhost:3000 |
| Swagger UI | http://localhost:8080/api/v1/swagger-ui.html |
| MinIO Console | http://localhost:9001 (minioadmin / minioadmin123) |

## Tài khoản demo

| Tài khoản | Mật khẩu | Vai trò | Số chức năng |
| --- | --- | --- | --- |
| `admin` | `Admin@123` | Quản trị hệ thống | 34 |
| `dispatcher` | `Dispatch@123` | Điều phối viên | 19 |
| `shipper01`, `shipper02`, `shipper03` | `Shipper@123` | Nhân viên giao hàng | 8 |
| `customer01`, `customer02` | `Customer@123` | Khách hàng | 10 |

## Mười nhóm chức năng theo đề bài

1. **Quản lý người dùng và đăng nhập** — đăng ký, đăng nhập JWT, refresh token, đăng xuất (thu hồi token qua Redis), đổi mật khẩu, CRUD người dùng, gán nhóm quyền.
2. **Quản lý shipper** — hồ sơ, phương tiện, khu vực, trạng thái trực tuyến, tải hiện tại, hiệu suất giao hàng, upload giấy tờ lên MinIO.
3. **Quản lý đơn hàng** — tạo/sửa/hủy, tìm kiếm đa điều kiện có phân trang, lịch sử trạng thái, import và export Excel.
4. **Điều phối phân công vận chuyển** — gán thủ công hoặc tự động theo 3 chiến lược (gần nhất, ít tải nhất, luân phiên), shipper nhận/từ chối, tự động trả đơn về hàng chờ.
5. **Thanh toán online** — VNPay sandbox với chữ ký HMAC-SHA512, xử lý ReturnUrl và IPN bất biến, ghi log giao dịch; có cổng giả lập để demo khi chưa có khóa sandbox.
6. **Tracking theo dõi** — shipper đẩy GPS, khách xem bản đồ Leaflet cập nhật realtime qua WebSocket, timeline sự kiện, trang tra cứu vận đơn công khai.
7. **Tính phí & voucher** — công thức phí theo loại dịch vụ (Strategy pattern), cấu hình bảng phí, 3 hình thức giảm giá, chống dùng vượt giới hạn bằng khóa lạc quan.
8. **Thông báo** — lưu database và đẩy realtime tới từng người dùng, sinh bất đồng bộ sau khi giao dịch commit, job nhắc đơn quá hạn.
9. **Thanh toán COD** — ví tiền thu hộ của shipper, nộp tiền, kế toán đối soát xác nhận.
10. **Dashboard & thống kê** — KPI, biểu đồ đơn theo ngày, tỉ lệ trạng thái, doanh thu theo tỉnh, top shipper; số liệu tự giới hạn theo vai trò.

Phân quyền theo `function_code` được kiểm tra hai lớp: `@PreAuthorize` ở backend và menu/route ở frontend. Ngoài ra còn phân quyền dữ liệu: khách hàng chỉ thấy đơn của mình, shipper chỉ thấy đơn được phân công.

## Tài liệu

| Tài liệu | Nội dung |
| --- | --- |
| [docs/01-technical-design.md](docs/01-technical-design.md) | Kiến trúc, sơ đồ ERD, state machine, luồng nghiệp vụ, chuẩn code |
| [docs/02-api-reference.md](docs/02-api-reference.md) | Danh sách endpoint và quyền yêu cầu |
| [docs/03-deployment.md](docs/03-deployment.md) | Cấu hình môi trường, chạy Docker, chạy local, cấu hình VNPay |
| [docs/04-demo-script.md](docs/04-demo-script.md) | Kịch bản demo đầy đủ 11 bước, có cả các nhánh phụ |
| [docs/05-huong-dan-cai-dat.md](docs/05-huong-dan-cai-dat.md) | **Bắt đầu từ đây** — cài đặt và chạy từng bước, trên máy mình và trên máy người khác |
| [docs/06-huong-dan-su-dung.md](docs/06-huong-dan-su-dung.md) | Hướng dẫn sử dụng 10 nhóm chức năng và phân quyền theo vai trò |
| [docs/07-demo-cho-mentor.md](docs/07-demo-cho-mentor.md) | Kịch bản demo gọn 10 phút để báo cáo, kèm phần chuẩn bị câu hỏi |

## Cấu trúc thư mục

```
.
├── backend/                 Spring Boot API
│   └── src/main/
│       ├── java/com/viettel/delivery/
│       │   ├── config/      Cấu hình Security, WebSocket, Redis, MinIO, Swagger
│       │   ├── constant/    Hằng số, mã lỗi, mã quyền, enum nghiệp vụ
│       │   ├── controller/  Nhận request, không chứa business logic
│       │   ├── dto/         request / response / search tách riêng
│       │   ├── entity/      Entity JPA kế thừa BaseEntity
│       │   ├── event/       Sự kiện nội bộ cho xử lý bất đồng bộ
│       │   ├── exception/   BusinessException và handler tập trung
│       │   ├── listener/    Xử lý sự kiện (tracking, thông báo)
│       │   ├── mapper/      MapStruct Entity <-> DTO
│       │   ├── model/       Model nội bộ (tính phí, kết quả thanh toán)
│       │   ├── repository/  Truy vấn dữ liệu, specification, projection
│       │   ├── scheduler/   Job định kỳ
│       │   ├── security/    JWT, principal, phân quyền
│       │   ├── service/     Business logic, chia theo module
│       │   └── util/        Tiện ích dùng chung
│       └── resources/
│           ├── db/changelog/  Liquibase (34 changeset)
│           └── i18n/          messages_vi / messages_en
├── frontend/                React SPA
├── docs/                    Tài liệu kỹ thuật
├── scripts/                 Script kiểm thử nhanh
└── docker-compose.yml
```

## Chạy không dùng Docker (khi phát triển)

```bash
# 1. Dựng một PostgreSQL riêng cho dự án ở cổng 55432 (không đụng tới PostgreSQL có sẵn trên máy)
bash scripts/dev-db.sh start

# 2. Backend — script ở bước 1 in ra sẵn lệnh này
cd backend
DB_URL="jdbc:postgresql://127.0.0.1:55432/delivery_db" \
DB_USERNAME=delivery_user DB_PASSWORD=delivery_pass_2026 \
SPRING_CACHE_TYPE=none MANAGEMENT_HEALTH_REDIS_ENABLED=false \
mvn spring-boot:run

# 3. Frontend (terminal khác)
cd frontend && npm install && npm run dev   # http://localhost:5173
```

Thiếu Redis hoặc MinIO thì ứng dụng vẫn khởi động, chỉ ghi cảnh báo và tắt các chức năng tương ứng (cache, upload file).

## Kiểm thử

```bash
# Unit test backend (55 test)
cd backend && mvn test

# Kiểm thử toàn bộ quy trình nghiệp vụ qua REST API (21 bước)
bash scripts/smoke-test.sh

# Kiểm thử luồng realtime WebSocket
node scripts/ws-check.mjs
```

## Khởi tạo Git

Thư mục chưa được đưa vào Git. Để bắt đầu quản lý phiên bản:

```bash
git init
git add .
git commit -m "Khoi tao he thong quan ly giao hang"
```

File `.env` đã được `.gitignore` loại trừ nên thông tin cấu hình nhạy cảm không bị commit.
