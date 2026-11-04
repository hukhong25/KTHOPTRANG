# HỆ THỐNG WEBSITE THƯƠNG MẠI ĐIỆN TỬ TECHSTORE & KIỂM THỬ HỘP TRẮNG

- **Sinh viên thực hiện**: **Đào Văn Công**
- **Mã sinh viên**: **2023601884**
- **Lớp**: IT6013.2 - K18
- **Học phần**: Kiểm thử phần mềm (GVHD: TS. Trần Tiến Dũng - Trường CNTT & TT, Trường Đại học Công nghiệp Hà Nội)
- **Bài tập**: Bài 8 - Mục L2.1 (Logbook)
- **Công nghệ triển khai**: Java 17/21, Spring Boot 3.2.5, REST API, HTML5/CSS3/JavaScript, TailwindCSS, JUnit 5, JaCoCo Coverage.

---

## 1. TỔNG QUAN VỀ HỆ THỐNG WEBSITE TECHSTORE

Hệ thống được phát triển hoàn thiện với đầy đủ các nghiệp vụ thương mại điện tử thực tế dành cho cửa hàng thiết bị công nghệ (**TechStore**), bao gồm:

### 🛍️ 1. Cửa hàng & Danh mục sản phẩm (Product Catalog)
- Quản lý 12 sản phẩm công nghệ cao cấp thuộc 5 danh mục chính: **Laptop**, **Điện thoại/Tablet**, **Tai nghe & Âm thanh**, **Phụ kiện công nghệ**, **Màn hình**.
- Bộ lọc danh mục tức thì, thanh tìm kiếm thông minh theo từ khóa (tên, mô tả sản phẩm).
- Sắp xếp sản phẩm theo: Giá tăng dần, Giá giảm dần, Đánh giá sao cao nhất.
- Hiển thị tình trạng tồn kho theo thời gian thực (Cảnh báo hàng sắp hết, hiển thị hết hàng khi `stock = 0`).

### 🛒 2. Quản lý Giỏ hàng (Cart Management)
- Thêm sản phẩm vào giỏ, tăng/giảm số lượng với cơ chế chặn vượt quá tồn kho khả dụng.
- Tự động xóa sản phẩm khi số lượng về 0, hoặc xóa toàn bộ giỏ hàng trong 1 click.
- Tự động tính toán tạm tính (Subtotal) và phản hồi thay đổi ngay lập tức trên giao diện.

### 🎁 3. Hệ thống Mã giảm giá Voucher (Promotion Engine)
- Quản lý danh mục mã ưu đãi công khai: `TECH50`, `WELCOME`, `GAMINGVIP`.
- Kiểm tra tính hợp lệ: Hạn sử dụng (Expiry Date), Giá trị đơn hàng tối thiểu (Min Order Value).
- Tự động giới hạn mức giảm giá tối đa (Max Discount Cap) theo đúng quy tắc kinh doanh.

### 👤 4. Tài khoản & Phân hạng hội viên VIP (Customer Tier System)
- Hỗ trợ chuyển đổi nhanh giữa các tài khoản mẫu để demo chiết khấu:
  * **DIAMOND** (Đào Văn Công - 2023601884): Giảm **5%** tổng tiền hàng.
  * **GOLD** (Trần Minh Hiếu): Giảm **3%** tổng tiền hàng.
  * **SILVER** (Nguyễn Hoàng Long): Giảm **1%** tổng tiền hàng.
  * **STANDARD** (Khách vãng lai): Giảm **0%**.
- Tự động tích lũy điểm thưởng thành viên khi đơn hàng hoàn tất (10.000đ = 1 điểm).

### ⚡ 5. Xử lý Đơn hàng & Tính toán thanh toán (Order Processing Engine)
- Tích hợp trực tiếp với phương thức nghiệp vụ lõi `TechOrderService.processOrder(OrderRequest)`:
  * Kiểm tra hợp lệ dữ liệu khách hàng và sản phẩm.
  * Tính phí vận chuyển đa tầng (Tiered Shipping): **Miễn phí ship (0đ)** nếu đơn sau giảm $\ge 500.000đ$ và cự ly $\le 10km$. Nếu $> 10km$, cước cơ bản 30.000đ cộng phụ phí 5.000đ/km vượt quá.
  * Phụ phí dịch vụ giao hàng hỏa tốc trong 2h: **+25.000đ**.
  * Ưu đãi phương thức thanh toán trực tuyến: **Giảm 2%** khi thanh toán qua Chuyển khoản ngân hàng (Banking) hoặc Ví điện tử (E-Wallet).

### 📦 6. Quản lý & Lịch sử đơn hàng (Order History & Lifecycle)
- Xem danh sách toàn bộ các đơn hàng đã đặt với đầy đủ hóa đơn bóc tách chi tiết.
- Quản lý trạng thái vòng đời đơn: `PENDING` $\rightarrow$ `CONFIRMED` $\rightarrow$ `SHIPPING` $\rightarrow$ `DELIVERED` hoặc `CANCELLED`.
- **Cơ chế Hủy đơn hàng an toàn**: Khi khách hàng hoặc quản trị viên hủy đơn, hệ thống tự động hoàn trả số lượng sản phẩm vào kho theo thời gian thực.

### 📊 7. Bảng điều khiển Quản trị (Admin Dashboard)
- Thống kê các chỉ số KPI: Tổng doanh thu, Tổng đơn hàng, Số mặt hàng đang bán, Cảnh báo sản phẩm hết hàng.
- Bảng điều chỉnh nhanh số lượng tồn kho (Quick Stock Update), cập nhật đơn giá, xóa sản phẩm hoặc thêm mới sản phẩm vào kho.

### 🧪 8. Phòng thí nghiệm Kiểm thử Hộp Trắng trực quan (White-Box Testing Lab)
- Tích hợp riêng một Tab giao diện chuyên dụng phục vụ báo cáo môn học **Kiểm thử phần mềm**.
- Thống kê đầy đủ thông số CFG của hàm `processOrder`: **22 nút**, **29 cạnh**, **8 nút vị từ**, độ phức tạp McCabe **$V(G) = 9$**.
- Nút bấm **"Chạy 8 Ca Kiểm Thử Hộp Trắng"** gọi trực tiếp API để thực thi kịch bản và trả về kết quả 100% PASSED màu xanh lá cùng thời gian thực thi (ms).

---

## 2. HƯỚNG DẪN KHỞI CHẠY HỆ THỐNG

### 2.1. Khởi chạy Ứng dụng Web
Mở Terminal / PowerShell tại thư mục dự án và chạy lệnh:

```powershell
.\mvnw.cmd spring-boot:run
```

Sau khi ứng dụng khởi động thành công (chỉ mất ~2 giây), mở trình duyệt và truy cập:
👉 **`http://localhost:8088`**

### 2.2. Khởi chạy Bộ Kiểm Thử Tự Động (JUnit 5 & JaCoCo)
Để chạy toàn bộ các bài test hộp trắng độc lập bằng Maven:

```powershell
.\mvnw.cmd test
```

- **Kết quả**: `8/8 Tests PASSED (100%)`.
- **Báo cáo JaCoCo**: Được tạo tự động tại thư mục `target/site/jacoco/index.html`.

---

## 3. DANH SÁCH 8 ĐƯỜNG DẪN CƠ SỞ (BASIS PATHS) & TEST CASES

| Mã Test Case | Kịch bản kiểm thử | Đường dẫn cơ sở (Basis Path) | Kết quả mong đợi |
| :---: | :--- | :--- | :--- |
| **TC_CONG_01** | Dữ liệu đơn hàng null hoặc rỗng | `Node 1 -> Node 2` | Ném `IllegalArgumentException` |
| **TC_CONG_02** | Số lượng mua vượt quá tồn kho | `Node 1 -> Node 3 -> Node 4` | Ném `IllegalStateException` |
| **TC_CONG_03** | Voucher đã hết hạn sử dụng | `Node 1 -> Node 3 -> Node 5 -> Node 6` | Ném `IllegalArgumentException` |
| **TC_CONG_04** | Đơn chưa đạt giá trị tối thiểu voucher | `Node 1 -> Node 3 -> Node 5 -> Node 6 -> Node 7` | Ném `IllegalArgumentException` |
| **TC_CONG_05** | Khách Diamond, Voucher chạm trần, Freeship, COD | `1->3->5->6->7->8->9->10(Diamond)->14->15->19->21->22` | Tổng: 18.500.000đ, Ship: 0đ |
| **TC_CONG_06** | Khách Gold, KC 15km, Hỏa tốc, Banking | `1->3->5->9->11(Gold)->14->16->18->19->20->21->22` | Tổng: 14.339.000đ, Ship: 80.000đ |
| **TC_CONG_07** | Khách Silver, đơn < 500k, KC gần, Ví điện tử | `1->3->5->9->12(Silver)->14->16->19->20->21->22` | Tổng: 224.040đ, Ship: 30.000đ |
| **TC_CONG_08** | Khách Standard, không voucher, KC 12km, COD | `1->3->5->9->13(Standard)->14->16->19->21->22` | Tổng: 1.040.000đ, Ship: 40.000đ |
