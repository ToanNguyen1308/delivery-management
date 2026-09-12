# Hướng dẫn triển khai

## 1. Yêu cầu môi trường

### Chạy bằng Docker (khuyến nghị)

| Thành phần | Phiên bản tối thiểu |
| --- | --- |
| Docker Engine | 24 trở lên |
| Docker Compose | v2 (lệnh `docker compose`) |
| RAM khả dụng | 4 GB |

Không cần cài Java, Node hay PostgreSQL trên máy — mọi thứ chạy trong container.

### Chạy trực tiếp trên máy (khi phát triển)

| Thành phần | Phiên bản |
| --- | --- |
| JDK | 21 |
| Maven | 3.9 trở lên |
| Node.js | 20 trở lên |
| PostgreSQL | 16 |
| Redis | 7 (không bắt buộc, thiếu thì tắt cache) |
| MinIO | Không bắt buộc, thiếu thì chỉ không upload được file |

## 2. Cấu hình biến môi trường

Toàn bộ cấu hình đọc từ biến môi trường, không có giá trị nhạy cảm nào nằm trong source code. File `.env` nằm trong `.gitignore`, chỉ `.env.example` được commit.

```bash
cp .env.example .env
```

| Nhóm | Biến | Ý nghĩa |
| --- | --- | --- |
| Database | `DB_URL`, `DB_DRIVER`, `DB_USERNAME`, `DB_PASSWORD`, `DB_NAME` | Kết nối cơ sở dữ liệu. Hibernate tự nhận diện dialect từ kết nối JDBC nên không cần khai báo |
| Redis | `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD` | Cache và thu hồi token |
| MinIO | `MINIO_ENDPOINT`, `MINIO_PUBLIC_ENDPOINT`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`, `MINIO_BUCKET` | Lưu trữ file |
| JWT | `JWT_SECRET`, `JWT_ACCESS_EXPIRATION_MS`, `JWT_REFRESH_EXPIRATION_MS` | Secret phải đủ 32 ký tự cho HS256 |
| Ứng dụng | `APP_PORT`, `APP_CONTEXT_PATH`, `FRONTEND_PORT`, `CORS_ALLOWED_ORIGINS` | Cổng và CORS |
| VNPay | `VNPAY_ENABLED`, `VNPAY_TMN_CODE`, `VNPAY_HASH_SECRET`, `VNPAY_PAY_URL`, `VNPAY_RETURN_URL`, `VNPAY_IPN_URL` | Cổng thanh toán |
| Điều phối | `DISPATCH_STRATEGY`, `DISPATCH_MAX_RADIUS_KM` | `NEAREST`, `LEAST_LOAD` hoặc `ROUND_ROBIN` |

Trước khi đưa lên môi trường thật, bắt buộc đổi `JWT_SECRET`, `DB_PASSWORD` và `MINIO_SECRET_KEY`.

## 3. Chạy bằng Docker Compose

```bash
cp .env.example .env
docker compose up -d --build
```

Compose dựng 6 service, backend chỉ khởi động sau khi PostgreSQL, Redis và MinIO đã báo healthy:

| Service | Image | Cổng | Vai trò |
| --- | --- | --- | --- |
| `postgres` | postgres:16-alpine | 5432 | Cơ sở dữ liệu |
| `redis` | redis:7-alpine | 6379 | Cache, thu hồi token |
| `minio` | minio/minio | 9000, 9001 | Lưu trữ file |
| `minio-init` | minio/mc | — | Tạo bucket rồi tự thoát |
| `backend` | build từ `backend/Dockerfile` | 8080 | Spring Boot API |
| `frontend` | build từ `frontend/Dockerfile` | 3000 | React SPA sau Nginx |

Kiểm tra tình trạng và theo dõi log:

```bash
docker compose ps
docker compose logs -f backend
```

Sau khoảng một phút, truy cập:

| Thành phần | Địa chỉ |
| --- | --- |
| Giao diện web | http://localhost:3000 |
| Swagger UI | http://localhost:8080/api/v1/swagger-ui.html |
| Healthcheck | http://localhost:8080/api/v1/actuator/health |
| MinIO Console | http://localhost:9001 |

Lần đầu khởi động, Liquibase tự tạo 34 changeset gồm toàn bộ schema, nhóm quyền, chức năng, bảng phí, voucher và 7 tài khoản demo. Không cần chạy script SQL thủ công.

Dừng hệ thống:

```bash
docker compose down          # giữ lại dữ liệu
docker compose down -v       # xóa luôn dữ liệu để chạy lại từ đầu
```

## 4. Chạy trực tiếp khi phát triển

### Backend

Cách nhanh nhất là dùng script dựng sẵn một PostgreSQL riêng cho dự án ở cổng 55432. Cách này không đụng tới PostgreSQL đang có trên máy (dữ liệu nằm trong `.tmp-pg/`, đã được gitignore):

```bash
bash scripts/dev-db.sh start    # khởi tạo và bật
bash scripts/dev-db.sh reset    # xóa sạch, dựng lại từ đầu
bash scripts/dev-db.sh stop     # tắt
```

Script in ra sẵn lệnh chạy backend tương ứng. Nếu muốn dùng PostgreSQL có sẵn trên máy:

```bash
createdb delivery_db
psql -d postgres -c "CREATE ROLE delivery_user LOGIN PASSWORD 'delivery_pass_2026';"
psql -d postgres -c "ALTER DATABASE delivery_db OWNER TO delivery_user;"

cd backend
DB_URL="jdbc:postgresql://localhost:5432/delivery_db" \
DB_USERNAME=delivery_user \
DB_PASSWORD=delivery_pass_2026 \
mvn spring-boot:run
```

Nếu chưa có Redis, thêm `SPRING_CACHE_TYPE=none MANAGEMENT_HEALTH_REDIS_ENABLED=false` để tắt cache và bỏ Redis khỏi healthcheck. Thiếu MinIO thì ứng dụng vẫn khởi động bình thường, chỉ ghi một dòng cảnh báo và không dùng được chức năng upload file.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Mở http://localhost:5173. Vite đã cấu hình proxy `/api` (kèm WebSocket) sang `http://localhost:8080` nên không cần cấu hình CORS riêng khi phát triển.

## 5. Cấu hình VNPay sandbox

Hệ thống chạy được ngay mà không cần VNPay: khi `VNPAY_TMN_CODE` để trống, cổng giả lập sẽ được dùng và quy trình thanh toán vẫn hoàn chỉnh để demo.

Để dùng sandbox thật:

1. Đăng ký tại https://sandbox.vnpayment.vn/devreg/ để nhận `vnp_TmnCode` và `vnp_HashSecret` qua email.
2. Điền vào `.env`:

```env
VNPAY_TMN_CODE=ma_tmn_cua_ban
VNPAY_HASH_SECRET=hash_secret_cua_ban
VNPAY_RETURN_URL=http://localhost:3000/payment/result
```

3. Khởi động lại backend: `docker compose restart backend`.

Thẻ test của VNPay sandbox:

| Thông tin | Giá trị |
| --- | --- |
| Ngân hàng | NCB |
| Số thẻ | 9704198526191432198 |
| Tên chủ thẻ | NGUYEN VAN A |
| Ngày phát hành | 07/15 |
| Mật khẩu OTP | 123456 |

### Về IPN

VNPay gọi IPN từ server của họ nên không tới được `localhost`. Với môi trường local, trạng thái thanh toán được cập nhật qua ReturnUrl (vẫn xác thực chữ ký HMAC-SHA512 đầy đủ trước khi tin).

Muốn thử IPN thật, mở một đường hầm công khai:

```bash
ngrok http 8080
# Khai báo URL ngrok vào cấu hình merchant trên cổng VNPay:
# https://<subdomain>.ngrok-free.app/api/v1/payments/vnpay/ipn
```

## 6. Đổi sang MariaDB

Code không phụ thuộc DBMS vì toàn bộ truy vấn dùng JPQL và schema do Liquibase sinh. Driver MariaDB đã có sẵn trong `pom.xml`, chỉ cần đổi hai biến:

```env
DB_URL=jdbc:mariadb://mariadb:3306/delivery_db
DB_DRIVER=org.mariadb.jdbc.Driver
```

Dialect không cần khai báo vì Hibernate tự nhận diện từ kết nối JDBC.

Và thay service `postgres` trong `docker-compose.yml` bằng:

```yaml
  mariadb:
    image: mariadb:11
    environment:
      MARIADB_DATABASE: ${DB_NAME}
      MARIADB_USER: ${DB_USERNAME}
      MARIADB_PASSWORD: ${DB_PASSWORD}
      MARIADB_ROOT_PASSWORD: ${DB_PASSWORD}
    ports:
      - "3306:3306"
    volumes:
      - mariadb-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "healthcheck.sh", "--connect", "--innodb_initialized"]
      interval: 10s
      timeout: 5s
      retries: 10
    networks:
      - delivery-net
```

Không cần sửa một dòng code Java nào.

## 7. Xử lý sự cố thường gặp

| Hiện tượng | Nguyên nhân và cách xử lý |
| --- | --- |
| Backend dừng với lỗi `Schema-validation` | Entity lệch với schema Liquibase. Chạy `docker compose down -v` rồi `up` lại để dựng schema mới |
| `Failed to connect to localhost:9000` | Chưa chạy MinIO. Ứng dụng vẫn hoạt động, chỉ không upload được file |
| Login trả 401 | Token hết hạn hoặc `JWT_SECRET` đã đổi sau khi phát token. Đăng nhập lại |
| Bản đồ không hiển thị | Máy cần kết nối Internet để tải tile từ OpenStreetMap |
| Không nhận được cập nhật realtime | Kiểm tra `GET /api/v1/ws/info` trả 200. Nếu dùng proxy riêng, phải cho phép header `Upgrade` |
| Cổng 3000, 8080, 5432 bị chiếm | Đổi `FRONTEND_PORT`, `APP_PORT` trong `.env` hoặc phần `ports` của compose |
| Import Excel báo lỗi từng dòng | Đúng thiết kế: mỗi dòng được commit độc lập, response trả về chi tiết dòng lỗi để sửa và import lại |

## 8. Về endpoint healthcheck

Actuator phơi ra hai nhóm health với mục đích khác nhau:

| Endpoint | Kiểm tra | Dùng cho |
| --- | --- | --- |
| `/actuator/health/liveness` | Ứng dụng và database | Healthcheck của Docker |
| `/actuator/health/readiness` | Ứng dụng, database và Redis | Kiểm tra sẵn sàng nhận tải |
| `/actuator/health` | Toàn bộ thành phần | Giám sát tổng thể |

Docker healthcheck cố tình dùng `liveness` thay vì `health`: nếu Redis hoặc MinIO gián đoạn tạm thời, container backend không bị đánh dấu unhealthy và restart oan, trong khi ứng dụng vẫn phục vụ được các chức năng chính.

## 9. Kiểm tra sau khi triển khai

```bash
# 1. Backend sống
curl http://localhost:8080/api/v1/actuator/health

# 2. Đăng nhập được
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"Admin@123"}'

# 3. Toàn bộ quy trình nghiệp vụ (21 bước)
bash scripts/smoke-test.sh

# 4. Luồng realtime WebSocket
node scripts/ws-check.mjs

# 5. Unit test
cd backend && mvn test
```
