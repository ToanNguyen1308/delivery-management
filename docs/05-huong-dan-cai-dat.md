# Hướng dẫn cài đặt và chạy project

Tài liệu này viết theo kiểu làm theo từng bước, dành cho hai tình huống:

- **Cách A** — chạy trên máy bạn ngay bây giờ, không cần Docker: Postgres riêng qua `scripts/dev-db.sh`, MinIO local qua `scripts/dev-minio.sh`, backend Maven, frontend Vite (`http://localhost:5173`).
- **Cách B** — chạy bằng Docker, chỉ một lệnh duy nhất. Đây là cách để đưa project cho người khác (mentor, bạn cùng nhóm, máy chấm bài) chạy được mà không phải cài Java, Node hay PostgreSQL. Giao diện ở `http://localhost:3000`.

Nếu chỉ cần tra cứu nhanh biến môi trường, cấu hình VNPay hay cách đổi sang MariaDB, xem [03-deployment.md](03-deployment.md).

---

## 0. Lấy mã nguồn

Nếu nhận project qua Git:

```bash
git clone <đường-dẫn-repository> delivery-management
cd delivery-management
```

Nếu nhận qua file nén, giải nén rồi mở terminal tại thư mục gốc (thư mục có chứa `docker-compose.yml`).

**Lưu ý quan trọng về tên thư mục:** nên đặt project ở đường dẫn **không có dấu tiếng Việt và không có khoảng trắng**, ví dụ `~/projects/delivery-management`. Một số công cụ (Docker bind mount, Java Language Server) hoạt động không ổn định với đường dẫn kiểu `/Users/ductwan/Developer/Thực tập Viettel Software/Project Giao Hang`.

Việc bắt buộc đầu tiên, dù chạy theo cách nào:

```bash
cp .env.example .env
```

File `.env` chứa toàn bộ cấu hình (mật khẩu database, JWT secret, khóa MinIO...). File này nằm trong `.gitignore` nên không bao giờ được commit — đúng quy định "không hard-code thông tin nhạy cảm". Chỉ `.env.example` đi theo source code, và nó chỉ chứa giá trị mặc định dùng cho môi trường phát triển.

### Lưu ý khi copy lệnh trong tài liệu này (quan trọng với zsh)

Các khối lệnh bên dưới có chú thích đặt sau dấu `#`. Trên **zsh** (shell mặc định của macOS), dấu `#` **không được hiểu là chú thích** khi gõ trực tiếp ngoài terminal, nên dán nguyên dòng vào sẽ lỗi. Ví dụ dán `npm install       # chỉ cần chạy lần đầu` sẽ nhận được:

```
npm error code EINVALIDTAGNAME
npm error Invalid tag name "#" of package "#"
```

Có hai cách xử lý, chọn một:

1. **Bật chú thích cho zsh** (khuyến nghị, làm một lần rồi dùng mãi):

```bash
echo 'setopt interactive_comments' >> ~/.zshrc
source ~/.zshrc
```

2. Hoặc khi copy thì **bỏ phần từ dấu `#` trở đi**, chỉ giữ lại lệnh.

---

## Cách A — Chạy trên máy bạn, không cần Docker

### A.1. Kiểm tra công cụ đã có

Chạy lệnh này để xem máy còn thiếu gì:

```bash
java -version     # cần 21
mvn -version      # cần 3.9 trở lên
node -version     # cần 20 trở lên
psql --version    # cần 16 (chỉ cần nếu muốn dùng PostgreSQL có sẵn)
```

Máy bạn hiện đã có đủ: JDK 21 (Homebrew), Maven 3.9.10, Node 22.20, PostgreSQL 16.13. **Không thiếu gì cả.**

Nếu máy khác còn thiếu, cài bằng Homebrew trên macOS:

```bash
brew install openjdk@21 maven node postgresql@16
```

Nếu muốn demo **upload ảnh xác nhận giao hàng** (không dùng Docker), cài thêm MinIO local:

```bash
brew install minio minio-mc
```

Redis **không bắt buộc**. Thiếu Redis thì tắt cache (thêm `SPRING_CACHE_TYPE=none`). MinIO cũng không chặn ứng dụng khởi động, nhưng **không bật MinIO thì không tải được ảnh giao hàng** — toast trên giao diện là "Tải file lên thất bại", log backend là `Failed to connect to localhost:9000`. Không phải lỗi định dạng JPG.

### A.2. Chuẩn bị database

Có hai lựa chọn. **Lựa chọn 1 được khuyến nghị** vì nó không đụng gì tới PostgreSQL đang có trên máy bạn.

#### Lựa chọn 1 — Dùng script dựng database riêng (khuyến nghị)

Script `scripts/dev-db.sh` tạo một PostgreSQL hoàn toàn độc lập ở **cổng 55432**, dữ liệu nằm trong thư mục `.tmp-pg/` của project (đã được gitignore). PostgreSQL 5432 có sẵn của bạn không bị ảnh hưởng, và bạn cũng không cần biết mật khẩu của nó.

```bash
bash scripts/dev-db.sh start
```

Các lệnh khác:

| Lệnh | Tác dụng |
| --- | --- |
| `bash scripts/dev-db.sh status` | Xem đang chạy hay không |
| `bash scripts/dev-db.sh stop` | Tắt |
| `bash scripts/dev-db.sh reset` | Xóa sạch dữ liệu, dựng lại từ đầu (hữu ích khi muốn demo lại từ số 0) |

#### Lựa chọn 2 — Dùng PostgreSQL có sẵn ở cổng 5432

Mở SQL Shell (psql) bằng tài khoản `postgres` rồi chạy:

```sql
CREATE ROLE delivery_user LOGIN PASSWORD 'delivery_pass_2026';
CREATE DATABASE delivery_db OWNER delivery_user;
\c delivery_db
GRANT ALL ON SCHEMA public TO delivery_user;
```

Dòng `GRANT ALL ON SCHEMA public` là bắt buộc với PostgreSQL 15 trở lên, vì từ bản 15 người dùng thường không còn quyền tạo bảng trong schema `public` mặc định.

Bạn **không cần tạo bảng thủ công**. Liquibase tự chạy 34 changeset khi backend khởi động lần đầu, tạo toàn bộ schema cộng dữ liệu mẫu (nhóm quyền, danh sách chức năng, bảng phí, voucher, 7 tài khoản demo).

### A.3. Chạy MinIO local (cần khi upload ảnh)

Ảnh xác nhận giao hàng được lưu trên MinIO, database chỉ giữ metadata. Cách A không dựng MinIO bằng Docker, nên phải bật một process MinIO ngay trên máy.

```bash
bash scripts/dev-minio.sh start
```

Script này bật MinIO ở cổng **9000**, console ở **9001**, tạo bucket `delivery-files` và cho phép đọc công khai (để xem ảnh trên giao diện). Dữ liệu nằm trong `.tmp-minio/` (đã gitignore).

| Lệnh | Tác dụng |
| --- | --- |
| `bash scripts/dev-minio.sh status` | Xem đang chạy hay không |
| `bash scripts/dev-minio.sh stop` | Tắt |
| `bash scripts/dev-minio.sh start` | Bật (đã chạy thì bỏ qua, chỉ bảo đảm bucket còn) |

Console: http://localhost:9001 (`minioadmin` / `minioadmin123`).

Nên chạy bước này **trước** khi bật backend. Nếu backend đã chạy rồi mới bật MinIO thì **không cần tắt backend** — script đã tạo sẵn bucket, chỉ việc tải ảnh lại trên giao diện.

Định dạng ảnh được nhận: `jpg`, `jpeg`, `png`, `webp`. Ảnh iPhone gốc `HEIC` hoặc file không có đuôi sẽ bị báo "Định dạng file không được hỗ trợ".

### A.4. Chạy backend

Mở **terminal thứ nhất**:

```bash
cd backend

DB_URL="jdbc:postgresql://127.0.0.1:55432/delivery_db" \
DB_USERNAME=delivery_user \
DB_PASSWORD=delivery_pass_2026 \
SPRING_CACHE_TYPE=none \
MANAGEMENT_HEALTH_REDIS_ENABLED=false \
mvn spring-boot:run
```

Nếu bạn chọn Lựa chọn 2 ở trên thì đổi `55432` thành `5432`. Nếu máy có Redis đang chạy thì bỏ hai dòng `SPRING_CACHE_TYPE` và `MANAGEMENT_HEALTH_REDIS_ENABLED` để bật cache trở lại.

**Mỗi lần chạy backend phải dán nguyên khối lệnh trên.** Chỉ gõ `mvn spring-boot:run` thì ứng dụng nối vào Postgres mặc định ở cổng **5432** (không phải instance demo 55432) và thường chết với `password authentication failed for user "delivery_user"`.

Backend sẵn sàng khi log hiện hai dòng này ở gần cuối:

```
Tomcat started on port 8080 (http) with context path '/api/v1'
Started DeliveryApplication in 5.006 seconds
```

Kiểm tra bằng lệnh ở terminal khác:

```bash
curl http://localhost:8080/api/v1/actuator/health/liveness
# {"status":"UP"}
```

### A.4.1. Dòng log trông giống lỗi nhưng không phải lỗi

Nếu **chưa** chạy `bash scripts/dev-minio.sh start`, ở cuối log backend sẽ có một dòng `WARN`:

```
WARN  com.viettel.delivery.config.MinioConfig
      - Khong the khoi tao bucket MinIO (http://localhost:9000): Failed to connect ...
```

Đây là **hành vi cố ý**, không phải lỗi khởi động. Ứng dụng vẫn chạy các chức năng khác. Chỉ upload ảnh bị hỏng. **Không cần chuyển sang Docker.** Chạy `bash scripts/dev-minio.sh start` rồi tải ảnh lại.

Khi MinIO đã chạy trước lúc backend khởi động, dòng WARN này không còn.

Về healthcheck: hai biến `SPRING_CACHE_TYPE=none` và `MANAGEMENT_HEALTH_REDIS_ENABLED=false` trong lệnh trên là để báo cho ứng dụng biết môi trường này không có Redis. Thiếu chúng, ứng dụng vẫn chạy được nhưng `curl /actuator/health` sẽ trả `DOWN` vì endpoint tổng hợp có kiểm tra Redis — khi đó hãy dùng `/actuator/health/liveness` (chỉ kiểm tra ứng dụng và database), cũng chính là endpoint mà Docker healthcheck dùng.

Cách chắc chắn nhất để biết hệ thống chạy đúng là thử đăng nhập:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"Admin@123"}'
# {"code":"success","message":"Đăng nhập thành công","data":{"accessToken":"eyJ..."}}
```

### A.5. Chạy frontend

Mở **terminal thứ hai**:

Chạy `npm install` trước (chỉ cần một lần duy nhất, những lần sau bỏ qua):

```bash
cd frontend
npm install
```

Lệnh này phải kết thúc bằng dòng dạng `added ... packages`. Nếu thấy `npm error code EINVALIDTAGNAME` thì bạn đã dán kèm phần chú thích `#` — xem lại mục lưu ý ở đầu tài liệu.

Sau đó bật dev server:

```bash
npm run dev
```

Mở trình duyệt tại **http://localhost:5173**. Màn hình đầu tiên phải là trang đăng nhập có 4 nút điền nhanh tài khoản demo.

Vite đã cấu hình sẵn proxy: mọi request `/api` (kể cả WebSocket) được chuyển tiếp sang `http://localhost:8080`, nên bạn không cần cấu hình CORS gì thêm khi phát triển.

### A.6. Tắt hệ thống

Nhấn `Ctrl + C` ở terminal backend và frontend, sau đó:

```bash
bash scripts/dev-minio.sh stop
bash scripts/dev-db.sh stop
```

---

## Cách B — Chạy bằng Docker (dùng khi đưa cho người khác)

Đây là cách được yêu cầu trong đề bài: đóng gói được và triển khai bằng Docker, bao gồm cả cơ sở dữ liệu lẫn ứng dụng.

### B.1. Cài Docker

| Hệ điều hành | Cách cài |
| --- | --- |
| macOS | Tải Docker Desktop tại https://www.docker.com/products/docker-desktop/, chọn đúng bản Apple Silicon hoặc Intel. Hoặc `brew install --cask docker` |
| Windows 10/11 | Tải Docker Desktop, bật WSL 2 khi trình cài đặt yêu cầu |
| Ubuntu/Debian | `curl -fsSL https://get.docker.com \| sh` rồi `sudo usermod -aG docker $USER` và đăng nhập lại |

Sau khi cài, **mở ứng dụng Docker Desktop lên** và chờ biểu tượng con cá voi báo "Docker Desktop is running". Kiểm tra:

```bash
docker --version
docker compose version
```

> **Nếu gặp `zsh: command not found: docker` dù Docker Desktop đang chạy (macOS)**
>
> Docker Desktop cài CLI vào `~/.docker/bin` và chỉ thêm dòng `export PATH` vào `~/.zprofile`.
> File đó chỉ được zsh đọc khi mở *login shell*, nên terminal tích hợp của VS Code / Cursor
> (interactive nhưng không login) sẽ không thấy lệnh `docker`. Khắc phục một lần:
>
> ```bash
> echo 'export PATH="$PATH:$HOME/.docker/bin"' >> ~/.zshrc
> ```
>
> Sau đó **mở terminal mới** rồi chạy lại hai lệnh kiểm tra ở trên. Lưu ý PATH được cố định
> lúc shell khởi động, nên terminal đang mở từ trước khi cài Docker luôn cần mở lại.

Cần cấp cho Docker ít nhất **4 GB RAM** (Docker Desktop → Settings → Resources).

### B.2. Chạy

Tại thư mục gốc của project:

```bash
cp .env.example .env
docker compose up -d --build
```

Lần đầu sẽ mất khoảng **5–10 phút** vì Docker phải tải image, tải dependency Maven và build frontend. Những lần sau chỉ mất vài chục giây nhờ cache.

Compose dựng 6 service:

| Service | Vai trò | Cổng |
| --- | --- | --- |
| `postgres` | Cơ sở dữ liệu | 5432 (đổi được bằng `DB_HOST_PORT`) |
| `redis` | Cache và thu hồi token | 6379 (đổi được bằng `REDIS_HOST_PORT`) |
| `minio` | Lưu ảnh xác nhận giao hàng | 9000, 9001 (đổi được bằng `MINIO_HOST_PORT`, `MINIO_CONSOLE_HOST_PORT`) |
| `minio-init` | Tạo bucket rồi tự thoát (chạy xong sẽ ở trạng thái `Exited (0)` — đây là bình thường) | — |
| `backend` | Spring Boot API | 8080 |
| `frontend` | React SPA sau Nginx | 3000 |

Backend chỉ khởi động sau khi PostgreSQL, Redis và MinIO đã báo healthy, nên không xảy ra lỗi "connection refused" lúc mới lên.

> **Nếu gặp `ports are not available: ... address already in use`**
>
> Máy bạn đã có sẵn một dịch vụ chiếm cổng đó — thường là PostgreSQL cài trực tiếp trên máy
> giữ cổng 5432. Không cần tắt dịch vụ đó, chỉ cần đổi cổng mà container mở ra ngoài bằng
> cách sửa trong `.env`:
>
> ```bash
> DB_HOST_PORT=55432          # thay cho 5432
> REDIS_HOST_PORT=6379
> MINIO_HOST_PORT=9000
> MINIO_CONSOLE_HOST_PORT=9001
> ```
>
> Các container gọi nhau qua tên service trong mạng nội bộ của Docker, nên đổi mấy biến này
> hoàn toàn không ảnh hưởng tới ứng dụng. Nó chỉ đổi cổng khi bạn muốn kết nối từ máy thật
> vào database, ví dụ dùng pgAdmin hay DBeaver.
>
> Cách tìm thủ phạm đang giữ cổng:
>
> ```bash
> sudo lsof -nP -iTCP:5432 -sTCP:LISTEN
> ```
>
> Cần `sudo` vì PostgreSQL thường chạy dưới tài khoản `postgres`, không có quyền root thì
> `lsof` sẽ không nhìn thấy và báo trống dù cổng vẫn đang bận.

### B.3. Theo dõi quá trình khởi động

```bash
docker compose ps              # xem trạng thái, chờ tới khi backend là "healthy"
docker compose logs -f backend # xem log backend, Ctrl+C để thoát khỏi chế độ theo dõi
```

Trong log backend bạn sẽ thấy Liquibase chạy lần lượt 34 changeset. Đây là lúc schema và dữ liệu mẫu được tạo.

### B.4. Truy cập

| Thành phần | Địa chỉ | Tài khoản |
| --- | --- | --- |
| Giao diện web | http://localhost:3000 | xem bảng tài khoản demo bên dưới |
| Swagger UI (thử API trực tiếp) | http://localhost:8080/api/v1/swagger-ui.html | — |
| Healthcheck | http://localhost:8080/api/v1/actuator/health | — |
| MinIO Console (xem file đã upload) | http://localhost:9001 | `minioadmin` / `minioadmin123` |

### B.5. Các lệnh quản lý thường dùng

```bash
docker compose stop            # tạm dừng, giữ nguyên dữ liệu
docker compose start           # bật lại
docker compose restart backend # khởi động lại riêng backend (sau khi sửa .env)
docker compose down            # xóa container nhưng giữ dữ liệu
docker compose down -v         # xóa cả dữ liệu, lần sau chạy sẽ sạch từ đầu
```

Lệnh `docker compose down -v` rất hữu ích trước khi demo: nó trả hệ thống về đúng trạng thái dữ liệu mẫu ban đầu.

---

## Tài khoản demo

Bảy tài khoản này được Liquibase tạo sẵn. Trang đăng nhập có **4 nút điền nhanh**, không cần gõ tay.

| Tài khoản | Mật khẩu | Vai trò | Số chức năng được cấp |
| --- | --- | --- | --- |
| `admin` | `Admin@123` | Quản trị hệ thống | 34 |
| `dispatcher` | `Dispatch@123` | Điều phối viên | 19 |
| `shipper01`, `shipper02`, `shipper03` | `Shipper@123` | Nhân viên giao hàng | 8 |
| `customer01`, `customer02` | `Customer@123` | Khách hàng | 10 |

Đây là mật khẩu dành cho môi trường demo. Khi triển khai thật, bắt buộc đổi `JWT_SECRET`, `DB_PASSWORD`, `MINIO_SECRET_KEY` trong `.env` và đổi mật khẩu các tài khoản này.

---

## Kiểm tra hệ thống chạy đúng

Chạy lần lượt bốn lệnh sau, nếu tất cả đều xanh nghĩa là hệ thống hoàn chỉnh:

```bash
# 1. Backend sống
curl http://localhost:8080/api/v1/actuator/health

# 2. Đăng nhập được
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"Admin@123"}'

# 3. Chạy hết quy trình nghiệp vụ qua REST API — 21 bước, từ tạo đơn tới đối soát COD
bash scripts/smoke-test.sh

# 4. Kiểm chứng luồng realtime WebSocket
node scripts/ws-check.mjs
```

Và chạy bộ unit test:

```bash
cd backend && mvn test     # 55 test, phải pass hết
```

Lưu ý: `scripts/smoke-test.sh` mặc định gọi vào `http://localhost:8080`. Nếu bạn chạy bằng Docker thì địa chỉ vẫn vậy, không cần sửa gì.

---

## Bàn giao project cho người khác

### Cần gửi những gì

Gửi toàn bộ thư mục project, **trừ** các thư mục sinh ra trong quá trình build (nếu gửi qua file nén thì xóa trước cho nhẹ):

```
backend/target/      — sản phẩm build Maven
frontend/node_modules/ — thư viện Node, người nhận tự cài lại
frontend/dist/       — sản phẩm build Vite
.tmp-pg/             — database tạm của riêng máy bạn
.env                 — chứa cấu hình riêng, người nhận tự tạo từ .env.example
```

Nếu dùng Git thì không phải lo, `.gitignore` đã loại trừ sẵn tất cả những thứ trên. Khởi tạo repository lần đầu:

```bash
git init
git add .
git commit -m "Khoi tao he thong quan ly giao hang"
```

### Người nhận cần làm gì

Chỉ ba bước, và **không cần cài Java, Maven, Node hay PostgreSQL** — tất cả đã nằm trong container:

```bash
# 1. Cài Docker Desktop và mở lên
# 2. Vào thư mục project
cp .env.example .env
# 3. Chạy
docker compose up -d --build
```

Chờ khoảng 5–10 phút cho lần build đầu, rồi mở http://localhost:3000 và đăng nhập bằng tài khoản demo.

### Nếu máy người nhận đã dùng các cổng đó

Ví dụ họ đã có PostgreSQL chạy ở 5432, hoặc một ứng dụng khác ở 3000. Chỉ cần sửa `.env`:

```env
APP_PORT=18080
FRONTEND_PORT=13000
```

và sửa phần `ports` của service `postgres` trong `docker-compose.yml` thành `"15432:5432"`. Không cần đụng tới code.

---

## Xử lý sự cố

| Hiện tượng | Nguyên nhân và cách xử lý |
| --- | --- |
| `zsh: command not found: docker` nhưng Docker Desktop vẫn báo "Engine running" | CLI nằm ở `~/.docker/bin` mà đường dẫn này chỉ được khai báo trong `~/.zprofile` — terminal của IDE không đọc file đó. Chạy `echo 'export PATH="$PATH:$HOME/.docker/bin"' >> ~/.zshrc` rồi mở terminal mới. Xem mục B.1 |
| `docker: command not found` | Chưa cài Docker, hoặc đã cài nhưng chưa mở Docker Desktop |
| `Cannot connect to the Docker daemon` | Docker Desktop chưa khởi động xong, chờ tới khi biểu tượng cá voi đứng yên |
| `port is already allocated` | Cổng đang bị ứng dụng khác chiếm. Tìm bằng `lsof -i :8080` rồi tắt, hoặc đổi cổng trong `.env` |
| Backend restart liên tục | Xem `docker compose logs backend`. Thường do database chưa sẵn sàng — chờ thêm một phút |
| Lỗi `Schema-validation` khi khởi động | Schema cũ lệch với entity. Chạy `docker compose down -v` rồi `up` lại |
| `Failed to connect to localhost:9000` hoặc toast "Tải file lên thất bại" | Cách A chưa bật MinIO. Chạy `bash scripts/dev-minio.sh start` rồi tải ảnh lại. Không cần Docker, không phải lỗi file JPG |
| `Định dạng file không được hỗ trợ` | Chỉ nhận `jpg`, `jpeg`, `png`, `webp`. Ảnh iPhone `HEIC` phải xuất sang JPG trước |
| `password authentication failed for user "delivery_user"` | Đã chạy `mvn spring-boot:run` thiếu biến `DB_URL` / `DB_USERNAME` / `DB_PASSWORD`, nên nối nhầm Postgres cổng 5432. Dán lại nguyên khối lệnh ở mục A.4 |
| `ws proxy socket error` / `ECONNREFUSED` trên terminal frontend | Backend chưa chạy hoặc vừa restart. Đợi log `Started DeliveryApplication` rồi tải lại trang trình duyệt |
| Đăng nhập trả 401 | Token hết hạn hoặc `JWT_SECRET` đã bị đổi sau khi phát token. Đăng nhập lại |
| Bản đồ trắng, không hiện đường | Máy cần Internet để tải tile bản đồ từ OpenStreetMap |
| Không thấy cập nhật realtime | Kiểm tra `curl http://localhost:8080/api/v1/ws/info` phải trả về 200 |
| `npm install` báo lỗi quyền | Không dùng `sudo`. Xóa `node_modules` và `package-lock.json` rồi cài lại |
| `npm error code EINVALIDTAGNAME` | Đã dán kèm chú thích `#` vào lệnh. Xem mục lưu ý về zsh ở đầu tài liệu |
| Trình duyệt hiện **màn hình trắng trơn** | Mở DevTools (`Cmd + Option + J` trên macOS, `F12` trên Windows) và đọc tab Console — lỗi luôn hiện ở đó. Nếu vừa `git pull` về bản mới, xóa cache dependency của Vite rồi chạy lại: `rm -rf node_modules/.vite && npm run dev`, sau đó tải lại trang bằng `Cmd + Shift + R` |
| Maven tải dependency rất chậm lần đầu | Bình thường, khoảng 200 MB. Những lần sau dùng cache ở `~/.m2` |

---

## Cấu hình IDE (Cursor / VS Code)

Nếu IDE báo đỏ hàng loạt file Java kiểu `The import org.springframework cannot be resolved` hoặc `The method getId() is undefined`, trong khi `mvn clean compile` vẫn chạy thành công, thì **code không có lỗi** — chỉ là Java Language Server chưa import Maven project.

Cách xử lý:

1. Cài extension **Extension Pack for Java** (`vscjava.vscode-java-pack`). IDE sẽ tự gợi ý vì project đã có sẵn file `.vscode/extensions.json`.
2. Mở Command Palette (`Cmd + Shift + P`) → chạy **Java: Clean Java Language Server Workspace** → chọn **Restart and delete**.
3. Chờ thanh trạng thái chạy xong phần "Importing Maven project". Sau bước này các dấu đỏ sẽ biến mất.

Project đã có sẵn `.vscode/settings.json` bật tự động import Maven và bật hỗ trợ Lombok, cùng `backend/lombok.config` để IDE hiểu các getter/setter do Lombok sinh ra.

Cách kiểm tra chắc chắn nhất xem code có lỗi thật hay không, không phụ thuộc IDE:

```bash
cd backend && mvn clean compile      # phải ra BUILD SUCCESS
cd frontend && npx tsc --noEmit      # không in ra gì là sạch
```
