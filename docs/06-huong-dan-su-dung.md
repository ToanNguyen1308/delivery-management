# Hướng dẫn sử dụng từng chức năng

Tài liệu này đi qua đúng 10 nhóm chức năng của đề bài cộng thêm phần phân quyền theo vai trò. Mỗi phần nói rõ: **ai dùng được**, **vào đâu trên giao diện**, **thao tác thế nào**, và **điểm kỹ thuật đáng nói khi trình bày**.

Nếu chỉ cần một kịch bản demo ngắn liền mạch để báo cáo mentor, xem [07-demo-cho-mentor.md](07-demo-cho-mentor.md).

## Chuẩn bị trước khi thao tác

Khởi động hệ thống theo [05-huong-dan-cai-dat.md](05-huong-dan-cai-dat.md), rồi mở giao diện web:

- Chạy bằng Docker: http://localhost:3000 (MinIO đã có trong compose)
- Chạy trực tiếp khi phát triển: http://localhost:5173 — phải `bash scripts/dev-minio.sh start` thì mới tải được ảnh xác nhận giao hàng. Chi tiết ở [05-huong-dan-cai-dat.md](05-huong-dan-cai-dat.md) mục A.3

### Đăng nhập ba vai trò cùng lúc

Để thấy được cập nhật realtime chạy qua lại giữa các vai trò, bạn cần **ba phiên đăng nhập độc lập**.

Điểm dễ nhầm: mở nhiều **cửa sổ ẩn danh trong cùng một trình duyệt là không được**. Chrome và Brave chỉ tạo một phiên ẩn danh duy nhất dùng chung cho mọi cửa sổ ẩn danh, mà hệ thống lưu token trong `localStorage`, nên đăng nhập tài khoản thứ hai sẽ đá văng tài khoản thứ nhất.

Ba cách đúng, chọn một:

**Cách 1 — Dùng ba trình duyệt khác nhau (đơn giản nhất).** Mỗi trình duyệt có kho lưu trữ riêng:

| Cửa sổ | Mở bằng | Tài khoản | Mật khẩu | Vai trò |
| --- | --- | --- | --- | --- |
| 1 | Chrome (cửa sổ thường) | `customer01` | `Customer@123` | Khách hàng |
| 2 | Chrome ẩn danh (`Cmd + Shift + N`) | `dispatcher` | `Dispatch@123` | Điều phối viên |
| 3 | Brave hoặc Safari | `shipper02` | `Shipper@123` | Shipper |

Một trình duyệt có thể cho **hai** phiên: cửa sổ thường và cửa sổ ẩn danh. Nên chỉ cần hai trình duyệt là đủ ba phiên.

**Cách 2 — Dùng nhiều hồ sơ (profile) của Chrome.** Bấm vào ảnh đại diện góc trên bên phải → **Add** → tạo profile mới, lặp lại cho đủ ba. Mỗi profile mở một cửa sổ riêng và hoàn toàn cách ly nhau.

**Cách 3 — Dùng Firefox với Multi-Account Containers.** Cài tiện ích này rồi mở mỗi vai trò trong một container. Cách này gọn nhất vì cả ba phiên nằm chung một cửa sổ, chỉ khác tab.

Trang đăng nhập có 4 nút điền nhanh tài khoản demo nên không cần nhớ mật khẩu.

Muốn kiểm tra đã tách phiên đúng chưa: đăng nhập cửa sổ 1 bằng `customer01`, rồi tải lại cửa sổ 2 — nếu cửa sổ 2 vẫn ở trang đăng nhập (không tự nhảy vào tài khoản `customer01`) là đã tách đúng.

---

## 1. Quản lý người dùng và đăng nhập

**Ai dùng:** mọi người đều đăng nhập được; phần quản lý người dùng chỉ `admin` thấy.

### Đăng nhập, đăng ký, đăng xuất

| Thao tác | Đường dẫn | Cách làm |
| --- | --- | --- |
| Đăng nhập | `/login` | Nhập tài khoản, hoặc bấm một trong 4 nút điền nhanh |
| Đăng ký khách hàng mới | `/register` | Điền thông tin, hệ thống tự gán nhóm quyền `CUSTOMER` |
| Xem thông tin cá nhân | Menu góc phải → **Thông tin tài khoản** | Xem hồ sơ và **danh sách chức năng đang được cấp** |
| Đổi mật khẩu | Trang Thông tin tài khoản | Nhập mật khẩu cũ và mới |
| Đăng xuất | Menu góc phải → **Đăng xuất** | Token bị đưa vào danh sách thu hồi trên Redis |

Thử nghiệm đáng làm: đăng xuất rồi lấy access token cũ gọi lại API — hệ thống trả 401 dù token chưa hết hạn. Lý do là token đã bị đưa vào blacklist trên Redis, chứ không phải chỉ xóa ở phía trình duyệt.

### Quản lý người dùng (chỉ `admin`)

Vào menu **Người dùng** (`/users`):

- Tìm kiếm theo tên, tài khoản, email, số điện thoại, nhóm quyền, trạng thái — có phân trang.
- **Thêm người dùng**: điền thông tin và chọn nhóm quyền.
- **Sửa**: đổi thông tin hoặc **đổi nhóm quyền**. Người dùng đó đăng nhập lại sẽ thấy menu khác ngay.
- **Khóa / mở khóa** tài khoản.
- **Xóa**: đây là xóa mềm — bản ghi chỉ được đánh dấu `is_deleted`, dữ liệu lịch sử không mất.

Vào menu **Nhóm quyền** (`/roles`) để xem 4 nhóm quyền và 34 chức năng, và tự tạo nhóm quyền mới bằng cách tick chọn các chức năng.

**Điểm kỹ thuật:** xác thực bằng JWT với cặp access token (ngắn hạn) và refresh token (dài hạn). Khi access token hết hạn, frontend tự động gọi `/auth/refresh` và chạy lại request vừa lỗi, người dùng không bị đá ra ngoài. Nếu đúng lúc có nhiều request cùng lỗi 401, chúng được xếp hàng chờ một lần refresh duy nhất rồi chạy tiếp.

---

## 2. Quản lý shipper

**Ai dùng:** `admin` toàn quyền, `dispatcher` xem và sửa, shipper chỉ quản lý hồ sơ của chính mình.

Vào menu **Quản lý shipper** (`/shippers`):

| Thao tác | Cách làm |
| --- | --- |
| Tìm kiếm | Lọc theo tên, mã shipper, loại phương tiện, khu vực hoạt động, trạng thái |
| Thêm shipper | Bấm **Thêm shipper**, chọn tài khoản người dùng, nhập biển số, loại xe, khu vực, số đơn tối đa |
| Đổi trạng thái | `ONLINE` (sẵn sàng nhận đơn) / `OFFLINE` / `BUSY` |
| Xem hiệu suất | Bấm vào một shipper để xem tổng đơn, số đơn thành công, tỉ lệ thành công, tải hiện tại |
| Upload giấy tờ | Tải ảnh CMND/bằng lái — file được đẩy lên MinIO |

Cột **Tải hiện tại** hiển thị dạng `2/5`, nghĩa là đang giữ 2 đơn trên tối đa 5. Con số này được bộ điều phối dùng để quyết định có gán thêm đơn hay không.

**Điểm kỹ thuật:** file không lưu trong database. Ảnh được đẩy lên MinIO theo cấu trúc thư mục theo năm/tháng, database chỉ giữ metadata (object key, dung lượng, content type). Mở MinIO Console tại http://localhost:9001 (`minioadmin` / `minioadmin123`) để thấy file thật. Khi chạy local không Docker, bật MinIO bằng `bash scripts/dev-minio.sh start`; thiếu bước này thì toast "Tải file lên thất bại" dù ảnh là JPG hợp lệ. Chỉ nhận `jpg`, `jpeg`, `png`, `webp`.

---

## 3. Quản lý đơn hàng

**Ai dùng:** khách hàng tạo và theo dõi đơn của mình; `dispatcher` và `admin` thấy toàn bộ đơn.

### Tạo đơn

Vào **Tạo đơn hàng** (`/orders/create`):

1. **Thông tin người gửi và người nhận** — họ tên, số điện thoại, địa chỉ.
2. Dùng ô **"Chọn nhanh quận/huyện"** để hệ thống tự điền quận/huyện kèm toạ độ. Toạ độ này rất quan trọng vì nó được dùng để tính quãng đường (tính cước) và tính khoảng cách tới shipper (điều phối).
3. **Thông tin kiện hàng** — khối lượng, kích thước, mô tả, giá trị hàng.
4. **Dịch vụ và thanh toán** — chọn loại dịch vụ, phương thức thanh toán, tiền thu hộ (COD), mã giảm giá.
5. Bấm **Tính cước tạm tính** để xem trước chi phí, rồi bấm **Tạo đơn hàng**.

Hệ thống sinh mã vận đơn dạng `DH` + ngày tháng + số ngẫu nhiên, ví dụ `DH260911A3F7C2`.

### Danh sách và tìm kiếm

Vào **Đơn hàng** (`/orders`). Bộ lọc gồm: mã vận đơn, tên/số điện thoại người nhận, trạng thái, loại dịch vụ, phương thức thanh toán, tỉnh/thành, khoảng ngày tạo. Kết quả có phân trang và sắp xếp.

### Chi tiết đơn

Bấm vào một dòng để mở `/orders/:id`. Trang này gom đủ mọi thứ: thông tin đơn, bảng chi tiết cước, bản đồ hành trình, timeline sự kiện, lịch sử đổi trạng thái, ảnh xác nhận giao hàng, và các nút hành động hợp lệ **theo vai trò**. Khách hàng chỉ theo dõi và hủy đơn khi còn cho phép. Nút **Đã lấy hàng**, **Đang vận chuyển**, **Giao thành công**, **Giao thất bại** chỉ hiện với shipper được phân công đơn đó — backend cũng chặn nếu gọi thẳng API.

### Vòng đời trạng thái

```
CREATED → CONFIRMED → ASSIGNED → PICKED_UP → IN_TRANSIT → DELIVERED
   ↓          ↓           ↓            ↓            ↓
CANCELLED  CANCELLED  CANCELLED      FAILED      FAILED → RETURNED
```

Giao diện chỉ hiện đúng những nút hợp lệ. Nhưng quan trọng hơn: backend cũng chặn. Gọi thẳng API để nhảy từ `CREATED` sang `DELIVERED` sẽ nhận lỗi `error.order.invalidTransition` với thông báo tiếng Việt rõ ràng. Bảng chuyển tiếp hợp lệ khai báo tập trung một chỗ trong enum `OrderStatus`, nên không thể lách bằng cách gọi API trực tiếp.

### Import và export Excel

Ở trang **Đơn hàng** (cần quyền `ROLE_ORDER_IMPORT` / `ROLE_ORDER_EXPORT`, có ở `dispatcher` và `admin`):

- **Tải file mẫu** — nhận file Excel có dòng tiêu đề (cột bắt buộc đánh dấu `*`), một dòng ví dụ và dòng ghi chú giải thích giá trị hợp lệ.
- **Import Excel** — chọn file đã điền. Kết quả trả về: tổng số dòng, số thành công, số lỗi, danh sách mã vận đơn đã tạo và bảng chi tiết lỗi kèm **số dòng cụ thể**.
- **Xuất Excel** — tải danh sách đơn theo đúng bộ lọc đang áp dụng.

Nên demo bằng cách cố ý để một dòng thiếu số điện thoại người nhận. Các dòng đúng vẫn được tạo thành công, chỉ dòng sai bị báo lỗi. Đây là chủ ý thiết kế: mỗi dòng được commit độc lập nên một dòng lỗi không làm mất cả lô.

---

## 4. Điều phối phân công vận chuyển

**Ai dùng:** `dispatcher` và `admin` phân công; shipper nhận hoặc từ chối.

### Màn hình điều phối

Vào **Điều phối** (`/dispatch`). Màn hình chia hai phần:

- **Bên trái**: danh sách đơn chờ phân công và đơn đang giao.
- **Bên phải**: bản đồ vị trí các shipper cùng danh sách shipper khả dụng, kèm tải hiện tại dạng `0/5`.

Chỉ đơn ở trạng thái `CONFIRMED` mới vào hàng chờ phân công, nên bước đầu là mở đơn và bấm **Xác nhận đơn**.

### Hai cách phân công

| Cách | Thao tác | Khi nào dùng |
| --- | --- | --- |
| **Thủ công** | Bấm **Gán**, chọn shipper trong danh sách | Khi muốn chỉ định người cụ thể |
| **Tự động** | Bấm **Tự động** | Hệ thống tự chọn theo chiến lược đã cấu hình |

Khi gán tự động, thông báo trả về cho biết đã chọn shipper nào và **khoảng cách từ shipper tới điểm lấy hàng**.

### Ba chiến lược phân công

Đổi bằng biến `DISPATCH_STRATEGY` trong `.env` rồi khởi động lại backend:

| Giá trị | Cách chọn shipper |
| --- | --- |
| `NEAREST` (mặc định) | Tính khoảng cách Haversine từ vị trí từng shipper tới điểm lấy hàng, chọn người gần nhất trong bán kính `DISPATCH_MAX_RADIUS_KM` (mặc định 15 km). Nếu đơn không có toạ độ thì tự chuyển sang cách tính ít tải nhất |
| `LEAST_LOAD` | Chọn shipper đang giữ ít đơn nhất |
| `ROUND_ROBIN` | Chia lượt luân phiên |

Với dữ liệu mẫu, đơn lấy hàng ở Cầu Giấy sẽ được gán cho `shipper02` (ở Đống Đa) thay vì `shipper01` (ở Hoàn Kiếm), vì gần hơn.

Trước khi gán, hệ thống còn kiểm tra shipper có đúng trạng thái nhận đơn và chưa vượt số đơn tối đa.

### Shipper nhận hoặc từ chối

Shipper vào **Nhiệm vụ của tôi** (`/my-tasks`). Đơn mới hiện ở trạng thái chờ xác nhận với hai nút:

- **Nhận** — đơn chuyển sang trạng thái đang giao, shipper bắt đầu thực hiện.
- **Từ chối** (phải nhập lý do) — đơn **tự động quay về hàng chờ** của điều phối viên và bộ đếm tải của shipper được trả lại.

Nhánh từ chối rất đáng demo vì nó cho thấy hệ thống xử lý được cả trường hợp không thuận lợi.

**Điểm kỹ thuật:** ba chiến lược là ba class cùng cài đặt một interface (Strategy pattern), được nạp vào một map theo tên. Thêm chiến lược thứ tư chỉ cần viết thêm một class, không sửa dòng code nào đang có.

---

## 5. Thanh toán online

**Ai dùng:** khách hàng thanh toán; `dispatcher` và `admin` xem lịch sử giao dịch.

### Thực hiện thanh toán

1. Khách hàng tạo đơn với phương thức **Thanh toán VNPay** (thay vì thu tiền khi nhận).
2. Ở trang chi tiết đơn xuất hiện nút **Thanh toán**, bấm vào.
3. Tùy cấu hình:
   - **Đã cấu hình `VNPAY_TMN_CODE`**: trình duyệt chuyển sang cổng VNPay sandbox. Dùng thẻ test ngân hàng NCB, số thẻ `9704198526191432198`, tên `NGUYEN VAN A`, ngày phát hành `07/15`, OTP `123456`.
   - **Chưa cấu hình**: hệ thống dùng cổng giả lập, cho phép chọn thành công hoặc thất bại để demo được cả hai nhánh. Hệ thống chạy đầy đủ mà không cần đăng ký VNPay.
4. Thanh toán xong, VNPay chuyển về `/payment/result`. Frontend gửi toàn bộ tham số `vnp_*` cho backend **xác thực chữ ký HMAC-SHA512** trước khi tin kết quả.

### Lịch sử giao dịch

Vào **Thanh toán** (`/payments`) để xem danh sách giao dịch: mã tham chiếu `txn_ref`, số tiền, mã phản hồi từ cổng, trạng thái và thời điểm thanh toán.

**Điểm kỹ thuật cần nói với mentor:**

- Chữ ký được tạo bằng HMAC-SHA512 trên chuỗi tham số đã sắp xếp theo thứ tự bảng chữ cái và URL-encode, số tiền nhân 100 — đúng đặc tả VNPay 2.1.0.
- **ReturnUrl chỉ để hiển thị cho người dùng, IPN mới là nguồn tin cậy để chốt giao dịch.** Endpoint `/payments/vnpay/ipn` được thiết kế bất biến: VNPay gọi lại bao nhiêu lần cũng không làm đổi kết quả, và trả về đúng bộ mã `RspCode` theo chuẩn (`00` thành công, `01` không tìm thấy đơn, `02` đã xử lý, `04` sai số tiền, `97` sai chữ ký, `99` lỗi khác).
- Ở môi trường local, VNPay không gọi được vào `localhost` nên trạng thái cập nhật qua ReturnUrl. Muốn thử IPN thật thì mở đường hầm bằng `ngrok http 8080`.
- Cổng thanh toán được định nghĩa qua interface `PaymentGateway`, nên thêm Momo hay ZaloPay về sau không phải sửa logic đơn hàng.

---

## 6. Tracking theo dõi

**Ai dùng:** shipper gửi vị trí; khách hàng, điều phối viên và cả người không đăng nhập đều xem được hành trình.

### Shipper chia sẻ vị trí

Ở trang **Nhiệm vụ của tôi**, bật công tắc **Chia sẻ vị trí**. Trình duyệt xin quyền định vị, bấm đồng ý. Từ lúc đó cứ **15 giây** hệ thống gửi toạ độ một lần lên server.

### Khách hàng xem realtime

Khách hàng mở trang chi tiết đơn. Bản đồ hiện ba điểm màu:

- Xanh dương — vị trí shipper
- Vàng — điểm lấy hàng
- Xanh lá — điểm giao hàng

Vị trí shipper di chuyển **mà không cần bấm tải lại trang**. Dữ liệu được đẩy qua WebSocket trên kênh `/topic/orders/{orderCode}`.

Bên dưới bản đồ là **timeline hành trình**: mỗi lần đơn đổi trạng thái, một mốc mới được thêm vào kèm thời gian, người thực hiện và ghi chú.

### Ảnh xác nhận giao hàng

Khi shipper bấm **Giao thành công**, hệ thống **bắt buộc phải có ảnh xác nhận**. Thử bấm xác nhận khi chưa tải ảnh — hệ thống chặn lại. Ảnh sau khi tải lên được lưu vào MinIO và hiển thị ngay trong trang chi tiết đơn. Chạy local không Docker thì MinIO phải đã bật (`bash scripts/dev-minio.sh start`); file phải là `jpg` / `jpeg` / `png` / `webp`.

### Tra cứu vận đơn công khai

Mở một cửa sổ **không đăng nhập**, vào `/tracking` và nhập mã vận đơn.

Kết quả hiện đầy đủ trạng thái, timeline hành trình và bản đồ điểm giao. Nhưng dữ liệu cá nhân đã được che:

| Thông tin | Người đã đăng nhập | Người tra cứu công khai |
| --- | --- | --- |
| Tên người nhận | Trần Thị Bình | `T*** T** B***` |
| Địa chỉ | Số 15 ngõ 20 Hàng Bài, Hoàn Kiếm, Hà Nội | Hoàn Kiếm, Hà Nội |
| Số điện thoại shipper | Hiển thị | Ẩn |
| Lộ trình GPS chi tiết | Hiển thị | Không trả về |

Đây là điểm đáng nhấn: cùng một nghiệp vụ nhưng hai mức hiển thị khác nhau tùy người xem đã đăng nhập hay chưa.

---

## 7. Tính phí và voucher

**Ai dùng:** khách hàng xem cước và nhập mã giảm giá; `admin` cấu hình bảng phí và voucher.

### Xem cước minh bạch

Ở trang tạo đơn, bấm **Tính cước tạm tính**. Bảng chi tiết hiện từng thành phần chứ không chỉ một con số tổng:

| Thành phần | Ý nghĩa |
| --- | --- |
| Phí cơ bản | Phí khởi điểm theo loại dịch vụ |
| Phí quãng đường | Phần vượt quá số km miễn phí |
| Phí khối lượng | Phần vượt quá số kg miễn phí |
| Phụ phí | Phụ phí vùng xa, phụ phí giờ cao điểm |
| Phí thu hộ COD | Tính theo phần trăm số tiền thu hộ |
| **Tổng cước** | Cộng tất cả các mục trên |
| Số tiền giảm | Phần voucher giảm được |
| **Số phải trả** | Tổng cước trừ số tiền giảm |

### Ba loại dịch vụ, ba cách tính

Đổi loại dịch vụ rồi bấm tính lại để thấy cước thay đổi:

| Dịch vụ | Phí cơ bản | Mỗi km vượt | Mỗi kg vượt | Phí COD | Đặc thù |
| --- | --- | --- | --- | --- | --- |
| Giao tiêu chuẩn | 15.000đ | 4.000đ | 5.000đ | 1,0% | Miễn phí 3 km và 3 kg đầu |
| Giao nhanh | 25.000đ | 6.000đ | 7.000đ | 1,5% | Cộng thêm **15% phí quãng đường** cho tuyến ưu tiên |
| Giao trong ngày | 40.000đ | 8.000đ | 10.000đ | 2,0% | Cộng thêm **20% phụ phí** nếu đặt sau 15 giờ |

Cấu hình các con số này ở menu **Bảng phí** (`/pricing`), sửa xong áp dụng ngay không cần khởi động lại.

### Voucher

Ba mã có sẵn trong dữ liệu mẫu, đại diện cho ba hình thức giảm giá khác nhau:

| Mã | Hình thức | Giá trị | Điều kiện | Giới hạn mỗi người |
| --- | --- | --- | --- | --- |
| `FREESHIP` | Miễn phí ship | Giảm tối đa 50.000đ | Không yêu cầu | 1 lần |
| `GIAM20` | Giảm phần trăm | 20%, tối đa 30.000đ | Đơn từ 20.000đ | 3 lần |
| `GIAM10K` | Giảm số tiền cố định | 10.000đ | Đơn từ 30.000đ | 5 lần |

Nên demo `GIAM20`: nếu cước 200.000đ thì 20% là 40.000đ, nhưng hệ thống chỉ giảm 30.000đ vì chạm mức giảm tối đa.

Thử nhập một mã không tồn tại: hệ thống **không chặn màn hình**, chỉ hiện cảnh báo "Mã giảm giá không tồn tại" và vẫn tính cước bình thường — trải nghiệm người dùng tốt hơn là báo lỗi rồi dừng.

Quản lý voucher ở menu **Voucher** (`/vouchers`): tạo mã mới, đặt số lượng, thời gian hiệu lực, giới hạn mỗi người, bật/tắt.

**Điểm kỹ thuật:** mỗi loại dịch vụ là một chiến lược tính cước riêng (Strategy pattern), tất cả dùng `BigDecimal` chứ không dùng `double` để tránh sai số tiền tệ. Voucher dùng **khóa lạc quan** (`@Version`): nếu hai người cùng tranh lượt sử dụng cuối cùng, chỉ một người thành công, người còn lại nhận lỗi thay vì cả hai cùng dùng được. Bảng `voucher_usages` ghi lại từng lượt để chặn dùng vượt giới hạn.

---

## 8. Thông báo

**Ai dùng:** mọi vai trò.

Biểu tượng **chuông** ở thanh trên cùng hiển thị số thông báo chưa đọc. Số này **nhảy ngay lập tức** khi có sự kiện mới, không cần tải lại trang.

Vào `/notifications` để xem danh sách đầy đủ, đánh dấu đã đọc từng cái hoặc đánh dấu tất cả.

Các sự kiện sinh thông báo:

| Sự kiện | Ai nhận |
| --- | --- |
| Đơn hàng đổi trạng thái | Khách hàng đặt đơn |
| Được phân công đơn mới | Shipper được gán |
| Shipper từ chối đơn | Điều phối viên |
| Thanh toán thành công hoặc thất bại | Khách hàng |
| Đối soát COD được xác nhận | Shipper |
| Đơn quá hạn giao | Điều phối viên |

Thông báo vừa lưu vào database vừa đẩy realtime qua WebSocket tới đúng người nhận. Toàn bộ nội dung là tiếng Việt có dấu, lấy từ file i18n chứ không viết cứng trong code.

**Điểm kỹ thuật:** thông báo được sinh **bất đồng bộ và chỉ sau khi giao dịch đã commit thành công**. Tách như vậy để hai việc không ảnh hưởng nhau: nếu gửi thông báo lỗi, dữ liệu nghiệp vụ đã lưu vẫn nguyên vẹn, không bị rollback oan. Ngược lại, các sự kiện phải ghi vào cùng giao dịch (như ghi timeline hành trình) thì chạy đồng bộ.

Ngoài ra có một job chạy định kỳ quét các đơn quá hạn giao và nhắc điều phối viên.

---

## 9. Thanh toán COD (đối soát tiền thu hộ)

**Ai dùng:** shipper nộp tiền; `dispatcher` và `admin` xác nhận đối soát.

Đây là bài toán thực tế: shipper thu hộ tiền mặt của khách, giữ trong người, rồi phải nộp lại cho công ty và kế toán xác nhận.

### Quy trình ba bước

**Bước 1 — Hệ thống tự ghi nhận.** Ngay khi đơn có tiền thu hộ chuyển sang trạng thái *Giao thành công*, hệ thống tự tạo một khoản đối soát ở trạng thái *Shipper đang giữ tiền*. Shipper không phải khai báo thủ công.

**Bước 2 — Shipper nộp tiền.** Shipper vào **Ví COD** (`/my-wallet`). Ba thẻ số liệu cho thấy: số tiền đang giữ, số tiền đã nộp chờ xác nhận, số tiền đã đối soát xong. Bấm **Nộp toàn bộ** (hoặc chọn từng khoản), xác nhận. Tiền chuyển sang trạng thái *Đã nộp, chờ xác nhận*.

**Bước 3 — Kế toán xác nhận.** `dispatcher` vào **Đối soát COD** (`/cod-settlements`). Khoản vừa nộp hiện trong danh sách. Bấm **Xác nhận**, nhập ghi chú. Trạng thái chuyển thành *Kế toán đã xác nhận*.

Quay lại Ví COD của shipper: số tiền đã chuyển từ cột "đang giữ" sang cột "đã đối soát xong", và shipper nhận được một thông báo.

Nếu số tiền thực nộp không khớp, kế toán có thể ghi nhận chênh lệch kèm lý do thay vì xác nhận thẳng.

---

## 10. Dashboard và thống kê

**Ai dùng:** mọi vai trò đều vào được **Tổng quan** (`/dashboard`), nhưng **thấy dữ liệu khác nhau**.

### Với `admin` và `dispatcher`

Hàng thẻ KPI phía trên: tổng đơn, giao thành công, đang xử lý, tỉ lệ thành công, doanh thu cước, doanh thu hôm nay, COD đã thu hộ, số shipper đang trực tuyến.

Bốn biểu đồ bên dưới:

1. **Số đơn và doanh thu theo ngày** — biểu đồ đường hai trục
2. **Tỉ lệ đơn theo trạng thái** — biểu đồ tròn
3. **Doanh thu theo tỉnh/thành** — biểu đồ cột
4. **Xếp hạng top shipper** — bảng theo số đơn và tỉ lệ thành công

Đổi khoảng ngày ở góc phải để số liệu tính lại.

### Với `customer01`

Cùng một trang, nhưng chỉ hiện các thẻ KPI của **riêng đơn hàng của mình**, không có biểu đồ toàn hệ thống. Đây là minh chứng rõ nhất cho phân quyền dữ liệu: cùng một endpoint, service tự nhận biết vai trò của người gọi và giới hạn phạm vi dữ liệu trả về.

### Với shipper

Thấy số liệu hiệu suất của chính mình: số đơn đã giao, tỉ lệ thành công, số tiền COD đang giữ.

**Điểm kỹ thuật:** các truy vấn thống kê viết bằng JPQL với DTO projection (`select new ...`), chỉ lấy đúng những cột cần dùng thay vì nạp cả entity rồi tính trong Java. Kết quả được cache Redis 2 phút để không phải quét lại bảng đơn hàng mỗi lần mở trang.

---

## 11. Phân quyền theo vai trò

Đây là tính năng xuyên suốt cả 10 nhóm chức năng ở trên, và là phần nên trình bày kỹ nhất.

### Mô hình phân quyền

Hệ thống **không gán quyền trực tiếp theo tên vai trò**, mà đi qua ba bảng:

```
users  →  role_groups  →  functions
        (nhóm quyền)     (34 chức năng, mỗi cái có function_code)
```

Nhờ vậy, tạo một nhóm quyền mới (ví dụ "Kế toán" chỉ được xem và đối soát COD) chỉ là thao tác trên giao diện, **không phải sửa và build lại code**.

### Bảng phân quyền bốn nhóm mẫu

| Chức năng | Admin | Dispatcher | Shipper | Customer |
| --- | :---: | :---: | :---: | :---: |
| Quản lý người dùng | Toàn quyền | Chỉ xem | — | — |
| Quản lý nhóm quyền | Toàn quyền | — | — | — |
| Quản lý shipper | Toàn quyền | Xem, sửa | Hồ sơ của mình | — |
| Xem đơn hàng | Tất cả đơn | Tất cả đơn | Đơn được giao | Đơn của mình |
| Tạo, sửa, hủy đơn | Có | Có | — | Có |
| Xác nhận đơn | Có | Có | — | — |
| Import / Export Excel | Có | Có | — | — |
| Phân công shipper | Có | Có | — | — |
| Nhận / từ chối đơn | — | — | Có | — |
| Gửi vị trí GPS | — | — | Có | — |
| Xem hành trình | Có | Có | Có | Có |
| Cấu hình bảng phí | Có | Chỉ xem | — | Chỉ xem |
| Quản lý voucher | Có | Chỉ xem | — | Chỉ xem |
| Xem giao dịch thanh toán | Có | Có | — | Có |
| Tạo giao dịch thanh toán | Có | — | — | Có |
| Nộp tiền COD | — | — | Có | — |
| Xác nhận đối soát COD | Có | Có | — | — |
| Dashboard | Toàn hệ thống | Toàn hệ thống | Của mình | Của mình |
| **Tổng số chức năng** | **34** | **19** | **8** | **10** |

### Cách demo phân quyền

**Thứ nhất — menu tự sinh theo quyền.** Đăng nhập lần lượt ba tài khoản và so sánh menu bên trái:

| Tài khoản | Số mục | Các mục nhìn thấy |
| --- | --- | --- |
| `admin` | 11 | Tổng quan, Đơn hàng, Tạo đơn hàng, Điều phối, Đối soát COD, Thanh toán, Quản lý shipper, Người dùng, Nhóm quyền, Bảng phí, Voucher |
| `customer01` | 5 | Tổng quan, Đơn hàng, Tạo đơn hàng, Thanh toán, Voucher |
| `shipper02` | 4 | Tổng quan, Đơn hàng, Nhiệm vụ của tôi, Ví COD |

Menu được dựng từ danh sách `function_code` mà backend trả về lúc đăng nhập, **không viết cứng theo tên vai trò**. Vào trang **Thông tin tài khoản** để xem chính xác tài khoản đang có những chức năng nào.

Riêng **Nhiệm vụ của tôi** và **Ví COD** còn có thêm một điều kiện nữa là tài khoản phải thuộc nhóm shipper. Lý do: đây là màn hình cá nhân gắn với một hồ sơ shipper cụ thể, nên dù `admin` được cấp đủ 34 chức năng thì cũng không có hồ sơ shipper nào để hiển thị. Gõ thẳng `/my-tasks` bằng tài khoản `admin` sẽ bị đưa sang trang 403.

**Thứ hai — chặn ở cả hai lớp.** Đang đăng nhập bằng `customer01`, gõ thẳng `/users` lên thanh địa chỉ (Docker: `http://localhost:3000/users`, Cách A: `http://localhost:5173/users`). Hệ thống chuyển sang trang 403. Nhưng ẩn giao diện chưa đủ, nên backend cũng chặn độc lập:

```bash
curl -s -X POST http://localhost:8080/api/v1/users/search \
  -H "Authorization: Bearer <token_cua_customer01>" \
  -H 'Content-Type: application/json' -d '{}'
# {"code":"error.common.accessDenied","message":"Bạn không có quyền thực hiện chức năng này"}
```

**Thứ ba — phân quyền tới từng dòng dữ liệu.** Đây là lớp sâu nhất và thường bị bỏ sót. Cùng gọi `GET /api/v1/orders`, nhưng:

- `admin` và `dispatcher` nhận về tất cả đơn.
- `customer01` chỉ nhận về đơn do chính mình tạo.
- `shipper02` chỉ nhận về đơn được phân công cho mình.

Điều kiện lọc này **được ghép thẳng vào câu truy vấn ở tầng repository** (qua JPA Specification), không phải lọc ở frontend. Nghĩa là kể cả gọi API trực tiếp bằng curl cũng không lấy được dữ liệu của người khác. Thử mở đơn của `customer01` bằng token của `customer02` sẽ nhận lỗi từ chối truy cập.

---

## Phụ lục — Thử API trực tiếp bằng Swagger

Mở http://localhost:8080/api/v1/swagger-ui.html để xem và gọi thử toàn bộ endpoint.

1. Gọi `POST /auth/login` với `admin` / `Admin@123`, copy giá trị `accessToken` trong kết quả.
2. Bấm nút **Authorize** ở góc phải, dán token vào.
3. Từ đó gọi được mọi endpoint mà tài khoản đó có quyền.

Danh sách đầy đủ endpoint kèm quyền yêu cầu nằm ở [02-api-reference.md](02-api-reference.md).

Muốn chạy lại toàn bộ quy trình mà không bấm tay:

```bash
bash scripts/smoke-test.sh   # 21 bước, từ đăng nhập tới đối soát COD
node scripts/ws-check.mjs    # kiểm chứng luồng realtime
```
