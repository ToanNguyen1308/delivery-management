# Kịch bản demo

Kịch bản này đi hết một vòng đời đơn hàng, từ lúc khách hàng tạo đơn đến khi kế toán đối soát xong tiền thu hộ. Thời lượng khoảng 10-12 phút.

## Chuẩn bị

```bash
cp .env.example .env
docker compose up -d --build
```

Chờ khoảng một phút cho tới khi `docker compose ps` báo `backend` healthy, rồi mở http://localhost:3000.

Nên mở sẵn 3 phiên đăng nhập độc lập. Lưu ý mở nhiều cửa sổ ẩn danh của cùng một trình duyệt là **không** tách được phiên, vì Chrome và Brave dùng chung một phiên ẩn danh cho mọi cửa sổ ẩn danh:

| Tab | Mở bằng | Tài khoản | Mật khẩu |
| --- | --- | --- | --- |
| 1 | Chrome (cửa sổ thường) | `customer01` | `Customer@123` |
| 2 | Chrome ẩn danh (`Cmd + Shift + N`) | `dispatcher` | `Dispatch@123` |
| 3 | Brave hoặc Safari | `shipper02` | `Shipper@123` |

Trang đăng nhập có 4 nút điền nhanh tài khoản demo, không cần nhớ mật khẩu.

---

## Bước 1 — Phân quyền theo vai trò

Đăng nhập lần lượt bằng `admin` rồi `customer01` và so sánh menu bên trái.

`admin` thấy 11 mục: Tổng quan, Đơn hàng, Tạo đơn hàng, Điều phối, Đối soát COD, Thanh toán, Quản lý shipper, Người dùng, Nhóm quyền, Bảng phí, Voucher.

`customer01` chỉ thấy 5 mục: Tổng quan, Đơn hàng, Tạo đơn hàng, Thanh toán, Voucher.

`shipper02` thấy 4 mục: Tổng quan, Đơn hàng, Nhiệm vụ của tôi, Ví COD.

Lưu ý hai mục **Nhiệm vụ của tôi** và **Ví COD** chỉ hiện với tài khoản thuộc nhóm shipper, kể cả `admin` cũng không thấy. Đây là chủ ý: chúng là màn hình cá nhân gắn với một hồ sơ shipper cụ thể, mà `admin` tuy được cấp đủ 34 chức năng nhưng không phải là shipper nên không có hồ sơ nào để hiển thị.

Điểm cần nói rõ: menu được sinh từ danh sách `function_code` mà backend trả về khi đăng nhập, không hard-code theo tên vai trò. Vào trang **Thông tin tài khoản** để xem chính xác tài khoản đang có những chức năng nào.

Thử gõ trực tiếp `http://localhost:3000/users` khi đang đăng nhập bằng `customer01` — hệ thống chuyển sang trang 403. Và nếu gọi thẳng API thì backend cũng chặn:

```bash
curl -s -X POST http://localhost:8080/api/v1/users/search \
  -H "Authorization: Bearer <token_cua_customer01>" \
  -H 'Content-Type: application/json' -d '{}'
# => {"code":"error.common.accessDenied","message":"Bạn không có quyền thực hiện chức năng này"}
```

Tức là chặn ở cả hai lớp, không chỉ ẩn trên giao diện.

---

## Bước 2 — Khách hàng tạo đơn và xem cước minh bạch

Tab 1 (`customer01`), vào **Tạo đơn hàng**.

Điền thông tin người gửi, rồi ở ô "Chọn nhanh quận/huyện lấy hàng" chọn **Cầu Giấy, Hà Nội** — hệ thống tự điền quận/huyện và toạ độ. Làm tương tự cho điểm giao với **Hoàn Kiếm, Hà Nội**.

Thông tin kiện hàng: khối lượng `2.5` kg, mô tả "Quần áo".

Bên phải: dịch vụ **Giao tiêu chuẩn**, thanh toán **Thu tiền khi nhận**, tiền thu hộ `500000`, mã giảm giá `FREESHIP`.

Bấm **Tính cước tạm tính**. Bảng chi tiết hiện ra từng thành phần: phí cơ bản, phí quãng đường, phí khối lượng, phụ phí, phí thu hộ COD, tổng cước, số tiền giảm, số phải trả.

Đây là chỗ nên dừng lại giải thích: mỗi loại dịch vụ có một chiến lược tính cước riêng (Strategy pattern). Thử đổi dịch vụ sang **Giao nhanh** rồi tính lại — cước tăng vì cộng thêm 15% phí ưu tiên tuyến riêng. Đổi sang **Giao trong ngày** và nếu đang demo sau 15h, cước tăng thêm 20% phụ phí giờ cao điểm.

Đổi mã giảm giá sang `GIAM20` để thấy giảm 20% nhưng bị chặn ở mức tối đa 30.000đ. Nhập một mã không tồn tại — hệ thống không báo lỗi chặn màn hình, chỉ hiện cảnh báo "Mã giảm giá không tồn tại" và vẫn tính cước bình thường.

Quay lại `FREESHIP` và bấm **Tạo đơn hàng**. Hệ thống sinh mã vận đơn dạng `DH260911XXXXXX` và chuyển sang trang chi tiết. Ghi lại mã này để dùng ở các bước sau.

---

## Bước 3 — State machine chặn thao tác sai

Vẫn ở trang chi tiết đơn. Đơn đang ở trạng thái **Chờ xác nhận**, và các nút hành động chỉ hiện đúng những bước hợp lệ tiếp theo.

Chứng minh backend cũng chặn, không chỉ ẩn nút:

```bash
curl -s -X PUT http://localhost:8080/api/v1/orders/1/status \
  -H "Authorization: Bearer <token_dispatcher>" \
  -H 'Content-Type: application/json' -d '{"status":"DELIVERED"}'
# => {"code":"error.order.invalidTransition",
#     "message":"Không thể chuyển đơn hàng từ trạng thái Chờ xác nhận sang Giao thành công"}
```

Bảng chuyển tiếp hợp lệ khai báo tập trung trong enum `OrderStatus`, nên không thể nhảy bước dù gọi API trực tiếp.

---

## Bước 4 — Điều phối và phân công tự động

Tab 2 (`dispatcher`), vào **Đơn hàng**, mở đơn vừa tạo, bấm **Xác nhận đơn**. Đơn chuyển sang **Đã xác nhận**.

Vào **Điều phối**. Màn hình chia hai phần: bên trái là hàng chờ và đơn đang giao, bên phải là bản đồ vị trí shipper cùng danh sách shipper khả dụng (kèm tải hiện tại dạng `0/5`).

Bấm **Tự động** ở dòng đơn hàng. Thông báo hiện lên cho biết hệ thống đã chọn shipper nào, kiểu gán là tự động, và khoảng cách từ shipper tới điểm lấy hàng.

Giải thích: mặc định dùng chiến lược `NEAREST` — tính khoảng cách Haversine từ vị trí từng shipper tới điểm lấy hàng, chọn người gần nhất trong bán kính 15km. Trong dữ liệu mẫu, shipper02 ở Đống Đa gần Cầu Giấy hơn shipper01 ở Hoàn Kiếm, nên được chọn. Đổi `DISPATCH_STRATEGY` trong `.env` sang `LEAST_LOAD` hoặc `ROUND_ROBIN` rồi khởi động lại backend sẽ ra kết quả khác — cả ba chiến lược đều đã có sẵn.

Hệ thống cũng kiểm tra shipper còn đúng trạng thái nhận đơn và chưa vượt số đơn tối đa trước khi gán.

---

## Bước 5 — Shipper nhận đơn và giao hàng

Tab 3 (`shipper02`), vào **Nhiệm vụ của tôi**. Đơn mới hiện ở trạng thái **Chờ shipper xác nhận** với hai nút Nhận / Từ chối.

Có thể demo nhánh từ chối trước (bấm **Từ chối**, nhập lý do): đơn tự động quay về hàng chờ của điều phối viên và bộ đếm tải của shipper được trả lại. Sau đó điều phối viên gán lại.

Bấm **Nhận**. Tiếp theo bật công tắc **Chia sẻ vị trí** — trình duyệt xin quyền định vị, đồng ý. Từ lúc này cứ 15 giây hệ thống gửi toạ độ một lần.

Chuyển sang tab 1 (`customer01`), mở trang chi tiết đơn. Bản đồ hiện ba điểm màu: xanh dương là shipper, vàng là điểm lấy hàng, xanh lá là điểm giao. Vị trí shipper cập nhật mà **không cần bấm tải lại** — dữ liệu đẩy qua WebSocket kênh `/topic/orders/{orderCode}`.

Quay lại tab 3, bấm lần lượt **Đã lấy hàng** rồi **Đang vận chuyển**, mỗi bước nhập ghi chú. Mỗi lần chuyển, tab 1 có chuông thông báo nhảy số và timeline hành trình dài thêm một mục — tất cả realtime.

---

## Bước 6 — Hoàn tất giao hàng, bắt buộc có ảnh xác nhận

Tab 3, bấm **Giao thành công**. Hộp thoại yêu cầu tải ảnh xác nhận giao hàng. Thử bấm Xác nhận khi chưa có ảnh — hệ thống báo "Cần có ảnh xác nhận giao hàng trước khi hoàn tất".

Tải một ảnh bất kỳ lên. Ảnh được đưa lên MinIO theo cấu trúc `delivery-proof/2026/09/<uuid>.jpg`, database chỉ lưu metadata (object key, kích thước, content type, URL) chứ không lưu nội dung file. Có thể mở MinIO Console ở http://localhost:9001 để thấy file thật.

Bấm Xác nhận. Đơn chuyển sang **Giao thành công**, xuất hiện thêm trường "Đối soát COD: Shipper đang giữ tiền" và ảnh xác nhận hiển thị ngay trong trang chi tiết.

---

## Bước 7 — Đối soát tiền thu hộ

Tab 3, vào **Ví COD**. Ba thẻ số liệu cho thấy 500.000đ đang giữ của 1 đơn.

Bấm **Nộp toàn bộ**, xác nhận. Tiền chuyển sang trạng thái "Đã nộp, chờ xác nhận".

Tab 2 (`dispatcher`), vào **Đối soát COD**. Khoản vừa nộp hiện trong danh sách. Bấm **Xác nhận**, nhập ghi chú. Trạng thái chuyển thành "Kế toán đã xác nhận".

Quay lại ví COD của shipper: số tiền đã chuyển từ cột "đang giữ" sang cột "đã đối soát xong".

---

## Bước 8 — Tra cứu vận đơn công khai

Mở một trình duyệt chưa đăng nhập tài khoản nào (Safari, hoặc cửa sổ Guest của Chrome), vào http://localhost:3000/tracking và nhập mã vận đơn. Đừng dùng lại cửa sổ ẩn danh ở tab 2 vì nó đang đăng nhập bằng `dispatcher`.

Kết quả hiện đầy đủ trạng thái, timeline hành trình và bản đồ điểm giao. Nhưng dữ liệu cá nhân đã được che: tên người nhận thành `T*** T** B***`, địa chỉ chỉ còn `Hoàn Kiếm, Hà Nội` thay vì số nhà cụ thể, số điện thoại shipper bị ẩn, và lộ trình GPS chi tiết không được trả về.

Đây là điểm nên nhấn: cùng một service nhưng hai mức hiển thị khác nhau tùy người xem đã đăng nhập hay chưa.

---

## Bước 9 — Thanh toán online

Tab 1, tạo một đơn mới nhưng chọn phương thức **Thanh toán VNPay**.

Ở trang chi tiết đơn xuất hiện nút **Thanh toán**. Bấm vào:

- Nếu đã cấu hình `VNPAY_TMN_CODE`: trình duyệt chuyển sang cổng VNPay sandbox. Dùng thẻ test NCB (số thẻ `9704198526191432198`, OTP `123456`) để thanh toán. VNPay chuyển về `/payment/result`, frontend gửi toàn bộ tham số `vnp_*` cho backend xác thực chữ ký HMAC-SHA512 trước khi tin kết quả.
- Nếu chưa cấu hình: hệ thống dùng cổng giả lập, cho chọn thành công hoặc thất bại để demo cả hai nhánh.

Sau khi thanh toán, trạng thái đơn chuyển sang **Đã thanh toán**, khách nhận thông báo, và vào **Thanh toán** sẽ thấy giao dịch với mã `txn_ref`, mã phản hồi và thời điểm thanh toán.

Nói thêm về IPN: đây mới là nguồn tin cậy để chốt giao dịch trong môi trường thật. Endpoint `/payments/vnpay/ipn` được thiết kế bất biến — VNPay gọi lại bao nhiêu lần cũng không làm đổi kết quả, và trả về đúng bộ mã `RspCode` theo chuẩn.

---

## Bước 10 — Import và export Excel

Tab 2 (`dispatcher`), vào **Đơn hàng**.

Bấm **Tải file mẫu** — nhận về file Excel có dòng tiêu đề (cột bắt buộc đánh dấu `*`), một dòng ví dụ và dòng ghi chú giải thích các giá trị hợp lệ.

Điền thêm 2-3 dòng đơn hàng, cố ý để một dòng thiếu số điện thoại người nhận. Bấm **Import Excel** và chọn file.

Kết quả hiển thị: tổng số dòng, số thành công, số lỗi, danh sách mã vận đơn đã tạo và bảng chi tiết lỗi kèm số dòng. Các dòng đúng vẫn được tạo thành công — mỗi dòng commit độc lập nên một dòng lỗi không làm mất cả lô.

Bấm **Xuất Excel** để tải danh sách đơn hàng theo đúng bộ lọc đang áp dụng.

---

## Bước 11 — Dashboard

Tab 2 hoặc đăng nhập `admin`, vào **Tổng quan**.

Hàng thẻ trên: tổng đơn, giao thành công, đang xử lý, tỉ lệ thành công, doanh thu cước, doanh thu hôm nay, COD đã thu hộ, số shipper trực tuyến.

Bên dưới là 4 biểu đồ: số đơn và doanh thu theo ngày (hai trục), tỉ lệ đơn theo trạng thái (tròn), doanh thu theo tỉnh/thành (cột), và bảng xếp hạng top shipper.

Đổi khoảng ngày ở góc phải để số liệu tính lại.

Điểm kỹ thuật: các truy vấn thống kê viết bằng JPQL với DTO projection (`select new ...`) nên chỉ lấy đúng cột cần dùng, và kết quả được cache Redis 2 phút.

Cuối cùng, đăng nhập lại bằng `customer01` và vào **Tổng quan**: chỉ thấy các thẻ KPI của riêng mình, không có biểu đồ toàn hệ thống. Cùng một endpoint nhưng service tự nhận biết vai trò và giới hạn phạm vi dữ liệu.

---

## Kiểm chứng nhanh bằng script

Nếu cần chạy lại toàn bộ quy trình mà không bấm tay:

```bash
# 21 bước qua REST API, in kết quả từng bước
bash scripts/smoke-test.sh

# Kiểm chứng WebSocket: subscribe rồi đẩy GPS và xác nhận nhận được bản tin
node scripts/ws-check.mjs

# 55 unit test
cd backend && mvn test
```

## Những điểm nên nhấn khi trình bày

| Nội dung | Vì sao đáng chú ý |
| --- | --- |
| Phân quyền hai lớp theo `function_code` | Không hard-code vai trò; thêm nhóm quyền mới không cần sửa code |
| Phân quyền dữ liệu ở tầng query | Điều kiện được thêm vào Specification, không lọc ở frontend nên không thể lách |
| State machine tập trung trong enum | Một nguồn sự thật, chặn được cả khi gọi API trực tiếp |
| Ba chiến lược tính cước, ba chiến lược phân công | Strategy pattern, mở rộng bằng cách thêm class mới |
| Chữ ký HMAC-SHA512 và IPN bất biến | Làm đúng chuẩn bảo mật của cổng thanh toán |
| Tracking realtime qua WebSocket | Không polling, khách thấy shipper di chuyển ngay |
| Sự kiện đồng bộ và bất đồng bộ tách riêng | Dữ liệu nghiệp vụ nhất quán, gửi thông báo lỗi không làm rollback |
| Khóa lạc quan cho voucher | Chống hai giao dịch cùng tranh lượt cuối cùng |
| File qua MinIO, database chỉ giữ metadata | Đúng convention, dễ scale ngang |
| Liquibase quản lý toàn bộ schema | Dựng lại môi trường từ đầu chỉ bằng một lệnh |
