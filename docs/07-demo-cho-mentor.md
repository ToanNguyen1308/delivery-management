# Kịch bản demo một quy trình đơn giản để báo cáo mentor

Đây là bản demo **gọn, chạy liền mạch trong khoảng 10 phút**, đi hết một vòng đời đơn hàng từ lúc khách đặt tới lúc kế toán đối soát xong tiền. Chỉ một luồng duy nhất, không rẽ nhánh, để mentor thấy được hệ thống hoạt động thật chứ không phải các màn hình rời rạc.

Nếu cần đi sâu từng chức năng một, dùng [06-huong-dan-su-dung.md](06-huong-dan-su-dung.md). Nếu cần bản demo dài hơn có đủ các nhánh phụ, dùng [04-demo-script.md](04-demo-script.md).

---

## Chuẩn bị trước khi gặp mentor (làm trước 15 phút)

### 1. Dựng lại hệ thống từ dữ liệu sạch

```bash
docker compose down -v        # xóa sạch dữ liệu cũ
docker compose up -d --build
```

Chờ tới khi `docker compose ps` báo `backend` là `healthy` (khoảng một phút, lần build đầu lâu hơn).

Nếu máy chưa có Docker, chạy theo Cách A trong [05-huong-dan-cai-dat.md](05-huong-dan-cai-dat.md):

```bash
bash scripts/dev-db.sh reset && bash scripts/dev-db.sh start
# terminal 1
cd backend && DB_URL="jdbc:postgresql://127.0.0.1:55432/delivery_db" \
  DB_USERNAME=delivery_user DB_PASSWORD=delivery_pass_2026 \
  SPRING_CACHE_TYPE=none MANAGEMENT_HEALTH_REDIS_ENABLED=false \
  mvn spring-boot:run
# terminal 2
cd frontend && npm run dev
```

### 2. Chạy thử một lượt để chắc chắn không hỏng

```bash
bash scripts/smoke-test.sh
```

Script này chạy đúng quy trình bạn sắp demo, qua 21 bước REST API. Nếu tất cả các bước đều báo thành công thì buổi demo gần như chắc chắn suôn sẻ. **Sau khi chạy script, nhớ reset lại dữ liệu** để màn hình demo sạch sẽ.

### 3. Mở sẵn ba phiên đăng nhập độc lập

Ba vai trò phải đăng nhập cùng lúc thì mới demo được phần realtime. Sắp xếp ba cửa sổ cạnh nhau, hoặc ít nhất là chuyển qua lại thật nhanh:

| Cửa sổ | Mở bằng | Tài khoản | Mật khẩu | Vai trò |
| --- | --- | --- | --- | --- |
| A | Chrome (cửa sổ thường) | `customer01` | `Customer@123` | Khách hàng |
| B | Chrome ẩn danh (`Cmd + Shift + N`) | `dispatcher` | `Dispatch@123` | Điều phối viên |
| C | Brave hoặc Safari | `shipper02` | `Shipper@123` | Shipper |

**Đừng mở ba cửa sổ ẩn danh của cùng một trình duyệt.** Chrome và Brave chỉ tạo một phiên ẩn danh duy nhất dùng chung cho mọi cửa sổ ẩn danh, nên đăng nhập tài khoản thứ hai sẽ đá văng tài khoản thứ nhất — lỗi này rất dễ xảy ra ngay giữa buổi demo. Mỗi trình duyệt cho được hai phiên (cửa sổ thường và cửa sổ ẩn danh), nên hai trình duyệt là đủ ba phiên. Cách khác: tạo ba profile riêng trong Chrome qua ảnh đại diện góc trên bên phải → **Add**.

Kiểm tra đã tách phiên đúng chưa: đăng nhập cửa sổ A, rồi tải lại cửa sổ B. Nếu B vẫn ở trang đăng nhập chứ không tự vào tài khoản của A là đạt.

### 4. Chuẩn bị sẵn

- Một file ảnh bất kỳ trên desktop (dùng làm ảnh xác nhận giao hàng).
- Cho phép trình duyệt truy cập vị trí (bước chia sẻ GPS cần quyền này).
- Kiểm tra máy có Internet — bản đồ cần tải tile từ OpenStreetMap.
- Mở sẵn một tab Swagger: http://localhost:8080/api/v1/swagger-ui.html

---

## Mở đầu (1 phút)

Nói ngắn gọn trước khi bấm gì:

> "Em làm hệ thống quản lý giao hàng, gồm backend Spring Boot 3.5 với Java 21 và frontend React TypeScript, đóng gói bằng Docker Compose nên chỉ một lệnh là dựng được cả database, Redis, MinIO và ứng dụng. Hệ thống có 4 vai trò: khách hàng, điều phối viên, shipper và quản trị. Em sẽ demo một đơn hàng đi hết vòng đời, từ lúc khách đặt đến lúc kế toán đối soát xong tiền thu hộ."

---

## Bước 1 — Phân quyền theo vai trò (1,5 phút)

**Làm:** Ở cửa sổ A (`customer01`) và cửa sổ B (`dispatcher`), chỉ vào menu bên trái của hai màn hình.

**Nói:**

> "Hai tài khoản này thấy menu hoàn toàn khác nhau. Khách hàng có 5 mục, shipper có 4 mục, còn admin có 11 mục. Menu không được viết cứng theo tên vai trò, mà sinh ra từ danh sách mã chức năng backend trả về lúc đăng nhập. Em thiết kế ba bảng: người dùng thuộc nhóm quyền, nhóm quyền chứa các chức năng. Nên muốn thêm một nhóm quyền mới, ví dụ 'Kế toán' chỉ được đối soát COD, thì chỉ cần thao tác trên giao diện, không phải sửa code."

Nếu mentor thắc mắc sao admin không thấy mục "Nhiệm vụ của tôi" và "Ví COD":

> "Hai màn hình đó là màn hình cá nhân của shipper, gắn với một hồ sơ shipper cụ thể. Admin tuy được cấp đủ 34 chức năng nhưng không phải là shipper nên không có hồ sơ nào để hiển thị. Vì vậy ngoài điều kiện về mã chức năng, em đặt thêm điều kiện về nhóm quyền cho riêng hai mục này, và chặn ở cả tầng route chứ không chỉ ẩn menu."

**Làm tiếp:** Ở cửa sổ A, gõ thẳng `http://localhost:3000/users` lên thanh địa chỉ.

**Nói:**

> "Hệ thống chuyển sang trang 403. Nhưng ẩn giao diện thôi thì chưa đủ, nên backend chặn độc lập bằng `@PreAuthorize` theo mã chức năng. Nếu gọi thẳng API bằng curl cũng bị từ chối."

Nếu mentor muốn xem tận mắt, mở terminal chạy lệnh này (thay token của `customer01` lấy từ DevTools):

```bash
curl -s -X POST http://localhost:8080/api/v1/users/search \
  -H "Authorization: Bearer <token>" -H 'Content-Type: application/json' -d '{}'
# {"code":"error.common.accessDenied","message":"Bạn không có quyền thực hiện chức năng này"}
```

---

## Bước 2 — Khách hàng đặt đơn, xem cước minh bạch (2 phút)

**Làm:** Cửa sổ A → **Tạo đơn hàng**.

1. Điền người gửi bất kỳ, ở ô "Chọn nhanh quận/huyện lấy hàng" chọn **Cầu Giấy, Hà Nội**.
2. Điền người nhận, chọn điểm giao là **Hoàn Kiếm, Hà Nội**.
3. Khối lượng `2.5` kg, mô tả "Quần áo".
4. Dịch vụ **Giao tiêu chuẩn**, thanh toán **Thu tiền khi nhận**, tiền thu hộ `500000`, mã giảm giá `FREESHIP`.
5. Bấm **Tính cước tạm tính**.

**Nói khi bảng cước hiện ra:**

> "Cước được tách rõ từng thành phần: phí cơ bản, phí quãng đường, phí khối lượng, phụ phí, phí thu hộ COD, rồi trừ đi số tiền giảm của voucher. Quãng đường được tính từ toạ độ hai điểm. Mỗi loại dịch vụ là một chiến lược tính cước riêng theo Strategy pattern, và toàn bộ tính bằng BigDecimal chứ không dùng double để tránh sai số tiền."

**Làm nhanh để chứng minh:** đổi dịch vụ sang **Giao nhanh**, bấm tính lại — cước tăng vì cộng thêm 15% phí tuyến ưu tiên. Đổi mã sang `GIAM20` — giảm 20% nhưng bị chặn ở mức tối đa 30.000đ.

**Làm:** Quay lại **Giao tiêu chuẩn** + `FREESHIP`, bấm **Tạo đơn hàng**.

Hệ thống sinh mã vận đơn dạng `DH260911XXXXXX`. **Ghi mã này ra giấy**, các bước sau cần dùng.

---

## Bước 3 — Điều phối phân công tự động (1,5 phút)

**Làm:** Cửa sổ B (`dispatcher`) → **Đơn hàng** → mở đơn vừa tạo → bấm **Xác nhận đơn**.

**Làm tiếp:** Vào **Điều phối**. Bên trái là hàng chờ, bên phải là bản đồ shipper. Bấm **Tự động** ở dòng đơn hàng.

**Nói khi thông báo hiện ra:**

> "Hệ thống chọn shipper02 và báo luôn khoảng cách khoảng 3,9 km tới điểm lấy hàng. Mặc định em dùng chiến lược chọn shipper gần nhất: tính khoảng cách Haversine từ vị trí từng shipper tới điểm lấy hàng, lọc trong bán kính 15 km. Shipper02 ở Đống Đa gần Cầu Giấy hơn shipper01 ở Hoàn Kiếm nên được chọn. Em cài sẵn ba chiến lược: gần nhất, ít tải nhất và luân phiên, đổi bằng một biến môi trường. Trước khi gán hệ thống còn kiểm tra shipper đang online và chưa vượt số đơn tối đa."

---

## Bước 4 — Shipper nhận đơn và giao hàng realtime (2,5 phút)

Đây là phần ấn tượng nhất, nên làm chậm và để mentor nhìn hai màn hình cùng lúc.

**Làm:** Cửa sổ C (`shipper02`) → **Nhiệm vụ của tôi** → bấm **Nhận**.

**Làm tiếp:** Bật công tắc **Chia sẻ vị trí**, cho phép trình duyệt truy cập định vị.

**Làm tiếp:** Chuyển sang cửa sổ A (`customer01`), mở trang chi tiết đơn.

**Nói:**

> "Bản đồ hiện ba điểm: xanh dương là shipper, vàng là điểm lấy hàng, xanh lá là điểm giao. Shipper cứ 15 giây gửi toạ độ một lần, và màn hình khách hàng cập nhật ngay mà em không bấm tải lại trang. Dữ liệu được đẩy qua WebSocket theo kênh riêng của từng mã vận đơn, không phải frontend gọi API liên tục để hỏi."

**Làm:** Quay lại cửa sổ C, bấm **Đã lấy hàng**, rồi **Đang vận chuyển**.

**Nói (chỉ sang cửa sổ A):**

> "Mỗi lần đổi trạng thái, bên khách hàng chuông thông báo nhảy số và timeline hành trình dài thêm một mục, cũng là realtime."

---

## Bước 5 — Hoàn tất giao hàng, bắt buộc có ảnh xác nhận (1,5 phút)

**Làm:** Cửa sổ C → bấm **Giao thành công**. Khi hộp thoại hiện ra, **cố ý bấm Xác nhận khi chưa tải ảnh**.

**Nói:**

> "Hệ thống chặn lại, bắt buộc phải có ảnh xác nhận giao hàng."

**Làm:** Tải ảnh chuẩn bị sẵn lên, bấm Xác nhận.

**Nói:**

> "Ảnh được đẩy lên MinIO theo cấu trúc thư mục năm/tháng, database chỉ lưu metadata là object key, dung lượng và content type, không lưu nội dung file. Đơn chuyển sang Giao thành công và xuất hiện thêm dòng đối soát COD báo shipper đang giữ 500.000đ."

Nếu mentor muốn kiểm chứng, mở http://localhost:9001 (`minioadmin` / `minioadmin123`) để thấy file thật.

**Làm thêm nếu còn thời gian:** Chứng minh state machine chặn thao tác sai. Ở terminal:

```bash
curl -s -X PUT http://localhost:8080/api/v1/orders/1/status \
  -H "Authorization: Bearer <token_dispatcher>" \
  -H 'Content-Type: application/json' -d '{"status":"CREATED"}'
# {"code":"error.order.invalidTransition","message":"Không thể chuyển đơn hàng từ trạng thái ..."}
```

> "Bảng chuyển tiếp trạng thái hợp lệ khai báo tập trung trong enum, nên không nhảy bước được kể cả khi gọi thẳng API."

---

## Bước 6 — Đối soát tiền thu hộ (1,5 phút)

**Làm:** Cửa sổ C → **Ví COD**. Ba thẻ số liệu cho thấy 500.000đ đang giữ của 1 đơn. Bấm **Nộp toàn bộ**, xác nhận.

**Làm tiếp:** Cửa sổ B → **Đối soát COD**. Khoản vừa nộp nằm trong danh sách. Bấm **Xác nhận**, nhập ghi chú.

**Làm tiếp:** Quay lại cửa sổ C, làm mới Ví COD.

**Nói:**

> "Tiền đã chuyển từ cột đang giữ sang cột đã đối soát xong, và shipper nhận được thông báo. Khoản đối soát này không phải nhập tay: ngay khi đơn có tiền thu hộ chuyển sang Giao thành công, hệ thống tự sinh ra nó bằng một sự kiện nội bộ."

---

## Bước 7 — Tra cứu công khai và dashboard (1,5 phút)

**Làm:** Mở một trình duyệt **chưa đăng nhập tài khoản nào** (Safari, hoặc cửa sổ Guest của Chrome qua ảnh đại diện góc trên bên phải → **Guest**), vào http://localhost:3000/tracking và nhập mã vận đơn.

Đừng dùng lại cửa sổ ẩn danh B vì nó đang đăng nhập bằng `dispatcher`. Trang tra cứu vẫn che dữ liệu đúng trong mọi trường hợp vì nó gọi API công khai, nhưng demo bằng cửa sổ chưa đăng nhập thì thuyết phục hơn hẳn.

**Nói:**

> "Người không có tài khoản vẫn tra cứu được đơn, thấy trạng thái và hành trình. Nhưng dữ liệu cá nhân bị che: tên người nhận chỉ còn chữ cái đầu, địa chỉ chỉ tới cấp quận huyện chứ không có số nhà, số điện thoại shipper bị ẩn và lộ trình GPS chi tiết không được trả về. Cùng một nghiệp vụ nhưng hai mức hiển thị tùy người xem đã đăng nhập hay chưa."

**Làm:** Cửa sổ B → **Tổng quan**.

**Nói:**

> "Dashboard gồm các thẻ KPI và bốn biểu đồ: đơn và doanh thu theo ngày, tỉ lệ trạng thái, doanh thu theo tỉnh, và xếp hạng shipper. Truy vấn thống kê viết bằng JPQL với DTO projection nên chỉ lấy đúng cột cần, kết quả cache Redis 2 phút."

**Làm:** Chuyển sang cửa sổ A (`customer01`) → **Tổng quan**.

**Nói:**

> "Cùng một endpoint, nhưng khách hàng chỉ thấy số liệu đơn của mình, không có biểu đồ toàn hệ thống. Đây là lớp phân quyền sâu nhất: điều kiện lọc được ghép thẳng vào câu truy vấn ở tầng repository, nên gọi API trực tiếp cũng không lấy được dữ liệu của người khác."

---

## Kết thúc (1 phút)

> "Tóm lại, hệ thống có đủ 10 nhóm chức năng theo đề bài, phân quyền ba lớp gồm ẩn menu ở frontend, chặn bằng `@PreAuthorize` ở backend và giới hạn phạm vi dữ liệu ở tầng truy vấn. Toàn bộ schema do Liquibase quản lý nên dựng lại môi trường chỉ bằng một lệnh, cấu hình nhạy cảm đọc từ biến môi trường chứ không nằm trong code, và có 55 unit test cho các phần logic quan trọng như tính cước, voucher, state machine, chọn shipper và chữ ký VNPay."

Nếu mentor hỏi về kiểm thử, chạy tại chỗ:

```bash
cd backend && mvn test        # 55 test
bash scripts/smoke-test.sh    # 21 bước nghiệp vụ end-to-end
```

---

## Chuẩn bị cho câu hỏi của mentor

| Câu hỏi có thể gặp | Trả lời |
| --- | --- |
| Sao không dùng MariaDB theo quy định? | Code không phụ thuộc DBMS: toàn bộ truy vấn viết bằng JPQL, schema do Liquibase sinh, driver MariaDB đã có sẵn trong `pom.xml`. Chuyển đổi chỉ cần sửa 3 biến môi trường, không sửa một dòng code Java nào |
| Xử lý trường hợp hai người cùng dùng lượt voucher cuối? | Dùng khóa lạc quan qua trường `@Version`. Người thứ hai nhận lỗi thay vì cả hai cùng dùng được. Có unit test cho trường hợp này |
| Nếu gửi thông báo bị lỗi thì đơn hàng có bị rollback không? | Không. Thông báo chạy bất đồng bộ và chỉ sau khi giao dịch đã commit. Ngược lại, những việc phải nhất quán với đơn hàng như ghi timeline thì chạy đồng bộ trong cùng giao dịch |
| Thanh toán VNPay có an toàn không? | Chữ ký HMAC-SHA512 trên tham số đã sắp xếp và URL-encode theo đúng đặc tả 2.1.0. ReturnUrl chỉ để hiển thị, IPN mới là nguồn chốt giao dịch, và endpoint IPN được thiết kế bất biến, gọi lại nhiều lần không đổi kết quả |
| Sao không cho frontend lọc dữ liệu theo vai trò? | Lọc ở frontend có thể lách bằng cách gọi thẳng API. Nên điều kiện phân quyền dữ liệu được ghép vào Specification ở tầng repository |
| File ảnh lưu ở đâu? | MinIO, database chỉ giữ metadata. Cách này đúng convention và dễ mở rộng theo chiều ngang |
| Muốn thêm chiến lược phân công mới thì sao? | Viết thêm một class cài đặt interface `ShipperAssignmentStrategy`, Spring tự nạp vào map. Không sửa code đang có |
| Có test không? | 55 unit test bằng JUnit 5 và Mockito cho tính cước, voucher, state machine, chọn shipper, chữ ký VNPay và tiện ích tính khoảng cách. Ngoài ra có script kiểm thử 21 bước nghiệp vụ qua REST API thật |

---

## Nếu có sự cố ngay lúc demo

| Tình huống | Xử lý nhanh |
| --- | --- |
| Bản đồ trắng | Mất Internet. Bỏ qua bản đồ, chuyển sang nói về timeline hành trình vẫn cập nhật realtime |
| Trình duyệt không cho chia sẻ vị trí | Dùng nút đẩy toạ độ thủ công ở màn hình nhiệm vụ, hoặc chạy `node scripts/ws-check.mjs` để chứng minh luồng realtime |
| Không thấy cập nhật realtime | Tải lại trang chi tiết đơn để STOMP kết nối lại |
| Backend không phản hồi | `docker compose restart backend`, chờ khoảng 30 giây |
| Quên mật khẩu tài khoản demo | Trang đăng nhập có 4 nút điền nhanh |
| Muốn làm lại từ đầu | `docker compose down -v && docker compose up -d` |
