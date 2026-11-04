# BÁO CÁO THỰC HÀNH BÀI 8 - MỤC L2.1 (LOGBOOK)
# BÁO CÁO KIỂM THỬ HỘP TRẮNG TOÀN DIỆN HỆ THỐNG TECHSTORE

- **Sinh viên thực hiện**: **ĐÀO VĂN CÔNG**
- **Mã sinh viên (MSV)**: **2023601884**
- **Lớp khóa học**: IT6013.2 - K18
- **Học phần**: Kiểm thử phần mềm (GVHD: TS. Trần Tiến Dũng - Trường CNTT & TT, ĐH Công nghiệp Hà Nội)
- **Hệ thống kiểm thử**: Website Bán Thiết Bị Số & Phụ Kiện Công Nghệ (**TechStore**)
- **Phạm vi kiểm thử**: Toàn diện 3 module dịch vụ nghiệp vụ cốt lõi:
  1. `TechOrderService`: Lõi tính toán hóa đơn, chiết khấu VIP, voucher và phí ship.
  2. `OrderManagementService`: Quản lý giỏ hàng, đặt hàng, trừ tồn kho, hoàn kho khi hủy và thống kê doanh thu.
  3. `ProductService`: Quản lý danh mục, tìm kiếm, lọc, sắp xếp và CRUD sản phẩm.
- **GitHub Repository**: [https://github.com/hukhong25/KTHOPTRANG](https://github.com/hukhong25/KTHOPTRANG)

---

## I. ĐỐI TƯỢNG VÀ PHẠM VI KIỂM THỬ TOÀN HỆ THỐNG

Báo cáo kiểm thử hộp trắng bao phủ toàn diện 3 tầng dịch vụ nghiệp vụ cốt lõi của ứng dụng thương mại điện tử TechStore:
- **Module P0 (Lõi tính tiền)**: `TechOrderService.processOrder(OrderRequest)` (Đồ thị CFG 22 đỉnh, 29 cạnh, độ phức tạp McCabe $V(G) = 9$).
- **Module P0 (Quản lý đơn hàng & Kho vận)**: `OrderManagementService` (Kiểm tra gộp sản phẩm trùng DEF-01, trừ kho, tích điểm, hủy đơn hoàn kho).
- **Module P1 (Quản lý sản phẩm)**: `ProductService` (Kiểm tra bộ lọc danh mục, tìm kiếm từ khóa, sắp xếp giá, chặn trùng ID DEF-04, ràng buộc giá âm).

---

## II. KIỂM THỬ HỘP TRẮNG MODULE 1: TECHORDERSERVICE (LÕI TÍNH TIỀN)

### 2.1. Bảng ánh xạ các nút Đồ thị dòng điều khiển (Node Mapping Table)

| Nút (Node) | Đoạn mã / Câu lệnh | Ý nghĩa nghiệp vụ & Hành vi rẽ nhánh |
| :---: | :--- | :--- |
| **Node 1** | `if (request == null \|\| items == null \|\| items.isEmpty() \|\| ...)` | Bắt đầu hàm: kiểm tra hợp lệ dữ liệu request đầu vào |
| **Node 2** | `throw new IllegalArgumentException(...)` | Nhánh True: Dữ liệu lỗi, ném ngoại lệ dừng hàm |
| **Node 3** | `for (OrderItem item : items) { if (qty <= 0 \|\| qty > stock) }` | Duyệt sản phẩm: kiểm tra tồn kho và tính tổng tiền hàng |
| **Node 4** | `throw new IllegalStateException(...)` | Nhánh True: Vi phạm số lượng/tồn kho, ném ngoại lệ dừng hàm |
| **Node 5** | `if (request.getVoucher() != null)` | Kiểm tra đơn hàng có áp dụng mã voucher hay không |
| **Node 6** | `if (voucher.isExpired()) { throw ... }` | Voucher hết hạn: ném ngoại lệ dừng hàm |
| **Node 7** | `if (subTotal < voucher.getMinOrderValue()) { throw ... }` | Chưa đạt giá trị tối thiểu: ném ngoại lệ dừng hàm |
| **Node 8** | `voucherDiscount = Math.min(calc, maxDiscount)` | Voucher hợp lệ: tính số tiền giảm có áp trần tối đa |
| **Node 9** | `switch / if (tier)` | Kiểm tra phân hạng hội viên của khách hàng |
| **Node 10** | `memberDiscount = subTotal * 0.05` | Khách DIAMOND: giảm 5% tiền hàng |
| **Node 11** | `memberDiscount = subTotal * 0.03` | Khách GOLD: giảm 3% tiền hàng |
| **Node 12** | `memberDiscount = subTotal * 0.01` | Khách SILVER: giảm 1% tiền hàng |
| **Node 13** | `memberDiscount = 0.0` | Khách STANDARD: giảm 0% |
| **Node 14** | `if (totalAfterDiscounts >= 500000 && distance <= 10)` | Kiểm tra điều kiện chính sách Freeship |
| **Node 15** | `shippingFee = 0.0` | Nhánh True: Đạt điều kiện Freeship (cước 0đ) |
| **Node 16** | `shippingFee = 30000 + (distance - 10) * 5000` | Nhánh False: Cước cơ bản 30k + phụ phí cự ly > 10km |
| **Node 17** | Điểm hợp nhất cước vận chuyển | Hợp nhất luồng tính cước vận chuyển |
| **Node 18** | `if (expressDelivery) { shippingFee += 25000 }` | Cộng phụ phí giao hàng hỏa tốc trong 2h |
| **Node 19** | `if (payMethod == BANKING \|\| payMethod == E_WALLET)` | Kiểm tra phương thức thanh toán trực tuyến |
| **Node 20** | `paymentDiscount = totalAfterDiscounts * 0.02` | Nhánh True: Chiết khấu thêm 2% thanh toán online |
| **Node 21** | `finalAmount = total + shipping - payment; points = ...` | Tính tổng thanh toán cuối cùng và điểm thưởng tích lũy |
| **Node 22** | `return new OrderResult(...)` | Trả về đối tượng OrderResult thành công |

### 2.2. Đồ thị dòng điều khiển (CFG) & Độ phức tạp McCabe
- Số nút: $N = 22$, Số cạnh: $E = 29$, Số thành phần liên thông: $P = 1$.
- Số nút vị từ điều kiện: $P_r = 8$ (Node 1, 3, 5, 6, 7, 9, 14, 19).
- **Công thức McCabe 1**: $V(G) = E - N + 2P = 29 - 22 + 2 = 9$.
- **Công thức McCabe 2**: $V(G) = P_r + 1 = 8 + 1 = 9$.

### 2.3. Bảng thiết kế 8 Test Cases cho TechOrderService (TC_CONG_01 đến 08)

| Mã TC | Tên kịch bản kiểm thử | Đường dẫn (Basis Path) | Dữ liệu đầu vào (Input) | Kết quả mong đợi (Expected) | Trạng thái |
| :---: | :--- | :--- | :--- | :--- | :---: |
| **TC_CONG_01** | Request null / không hợp lệ | Path 1: (1)->(2) | `request = null` | Ném `IllegalArgumentException` | **PASSED** |
| **TC_CONG_02** | Số lượng mua vượt tồn kho | Path 2: (1)->(3)->(4) | Mua 5 Laptop Dell XPS, kho còn 2 | Ném `IllegalStateException` | **PASSED** |
| **TC_CONG_03** | Voucher đã hết hạn sử dụng | Path 3: (1)->(3)->(5)->(6) | Hạn voucher < ngày hiện tại | Ném `IllegalArgumentException` | **PASSED** |
| **TC_CONG_04** | Không đạt giá trị min voucher | Path 4: (1)->(3)->(5)->(7) | Đơn 150k, voucher min 500k | Ném `IllegalArgumentException` | **PASSED** |
| **TC_CONG_05** | DIAMOND, Voucher Max, Freeship | Path 5: (10)->(15)->(22) | Đơn 20tr, voucher giảm trần 500k, 5km, COD | Freeship 0đ, Tiền: 18.500.000đ | **PASSED** |
| **TC_CONG_06** | GOLD, Cự ly xa, Hỏa tốc, Banking | Path 6: (11)->(16)->(18)->(20) | Đơn 15tr, 15km, hỏa tốc, Banking | Ship 80k, Giảm 2% online, Tiền: 14.339.000đ | **PASSED** |
| **TC_CONG_07** | SILVER, Đơn < 500k, Ví MoMo | Path 7: (12)->(16)->(20) | Đơn 200k, 5km, Ví điện tử (-2%) | Ship 30k, Giảm 2% ví, Tiền: 224.040đ | **PASSED** |
| **TC_CONG_08** | STANDARD, Không voucher, COD | Path 8: (13)->(16)->(22) | Đơn 1tr, 12km, Giao thường, COD | Ship 40k, Tiền trả: 1.040.000đ | **PASSED** |

---

## III. KIỂM THỬ HỘP TRẮNG MODULE 2: ORDERMANAGEMENTSERVICE (QUẢN LÝ ĐƠN & KHO)

### 3.1. Bảng thiết kế 9 Test Cases cho OrderManagementService

| Mã TC | Phương thức / Mục tiêu | Dữ liệu kiểm thử | Hành vi kiểm tra (Side-effects) | Kết quả mong đợi | Trạng thái |
| :---: | :--- | :--- | :--- | :--- | :---: |
| **WB-CHK-01** | `checkout()` / Request rỗng | `request = null` hoặc `items = []` | Chặn request không hợp lệ | Ném `IllegalArgumentException` | **PASSED** |
| **WB-CHK-03** | `checkout()` / SP không tồn tại | `productId = "NON-EXISTENT-ID"` | Kiểm tra mã hàng trong kho | Ném `IllegalArgumentException` | **PASSED** |
| **WB-CHK-04** | `checkout()` / Số lượng $\le 0$ | `productId = "PROD-01"`, `quantity = 0` | Ràng buộc số lượng đặt | Ném `IllegalStateException` | **PASSED** |
| **WB-CHK-05** | `checkout()` / Vượt tồn kho đơn lẻ | `quantity = stock + 1` | Kiểm tra số lượng khả dụng | Ném `IllegalStateException` | **PASSED** |
| **WB-CHK-14** | `checkout()` / Trùng SP trong giỏ (DEF-01) | 2 dòng giỏ: 5 + 4 = 9 (kho có 8) | Gộp số lượng chống bán vượt kho | Ném `IllegalStateException` | **PASSED** |
| **WB-CHK-12** | `checkout()` / Tạo đơn thành công | CUST-01 mua 2 phím Keychron (stock=22) | Trừ kho: 22 $\rightarrow$ 20; Tích điểm VIP | Status CONFIRMED, Trừ kho chuẩn | **PASSED** |
| **WB-CAN-01** | `cancelOrder()` / Hủy & Hoàn kho | Hủy đơn hàng chứa 3 chuột Logitech | Phục hồi tồn kho về mức ban đầu | Status CANCELLED, Hoàn kho chuẩn | **PASSED** |
| **WB-CAN-02** | `cancelOrder()` / Chống hủy lặp lại | Gọi hủy lần thứ hai trên cùng đơn | Chặn hoàn kho 2 lần liên tiếp | Ném `IllegalStateException` | **PASSED** |
| **WB-OM-01** | `updateStatus()` / Cập nhật & Chặn lỗi | Đổi sang SHIPPING; Đổi status bậy | Kiểm soát tập trạng thái hợp lệ | Cập nhật chuẩn, Chặn status lạ | **PASSED** |

---

## IV. KIỂM THỬ HỘP TRẮNG MODULE 3: PRODUCTSERVICE (QUẢN LÝ SẢN PHẨM)

### 4.1. Bảng thiết kế 7 Test Cases cho ProductService

| Mã TC | Phương thức / Mục tiêu | Dữ liệu kiểm thử | Hành vi kiểm tra | Kết quả mong đợi | Trạng thái |
| :---: | :--- | :--- | :--- | :--- | :---: |
| **WB-PRO-01** | `getProducts()` / Lọc danh mục | `category = "LAPTOP"`, `"ALL"`, `"UNKNOWN"` | Lọc không phân biệt hoa thường | Trả đúng danh sách theo phân loại | **PASSED** |
| **WB-PRO-05** | `getProducts()` / Tìm kiếm từ khóa | `search = "MacBook"`, `"xyz999notFound"` | Tìm kiếm theo tên và mô tả | Trả đúng sản phẩm khớp từ khóa | **PASSED** |
| **WB-PRO-09** | `getProducts()` / Sắp xếp theo giá | `sort = "price_asc"`, `"price_desc"` | Kiểm tra thứ tự tăng/giảm giá | Danh sách sắp xếp chính xác | **PASSED** |
| **WB-PRO-13** | `getProductById()` / Truy vấn theo ID | `id = "PROD-01"`, `"PROD-NOT-EXIST"` | Tìm theo mã hoặc báo lỗi không thấy | Trả đúng đối tượng hoặc Exception | **PASSED** |
| **WB-PRO-15** | `createProduct()` / Chặn trùng ID (DEF-04) | Tạo mới sản phẩm với ID đã có sẵn | Ngăn chặn ghi đè mất dữ liệu kho | Ném `IllegalArgumentException` | **PASSED** |
| **WB-PRO-17** | `updateProduct()` / Ràng buộc giá âm | Cập nhật giá bán = -100đ | Kiểm tra ràng buộc giá dương $> 0$ | Ném `IllegalArgumentException` | **PASSED** |
| **WB-PRO-20** | `deleteProduct()` / Xóa sản phẩm | Xóa PROD-12 khỏi DataStore | Loại bỏ sản phẩm khỏi danh mục | Xóa thành công, truy vấn sau đó lỗi | **PASSED** |

---

## V. TỔNG HỢP KẾT QUẢ THỰC THI JUNIT 5 & ĐỘ ĐO BAO PHỦ JACOCO

### 5.1. Log thực thi Maven Surefire Test Runner
```text
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.daovancong.techstore.OrderManagementServiceTest
Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.248 s -- in com.daovancong.techstore.OrderManagementServiceTest
Running com.daovancong.techstore.ProductServiceTest
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.030 s -- in com.daovancong.techstore.ProductServiceTest
Running com.daovancong.techstore.TechOrderServiceTest
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.027 s -- in com.daovancong.techstore.TechOrderServiceTest

Results:
Tests run: 24, Failures: 0, Errors: 0, Skipped: 0

[INFO] --- jacoco:0.8.11:report (report) @ tech-store-whitebox-testing ---
[INFO] Loading execution data file D:\Tai lieu hoc\KiemThu\KTHOPTRANG\target\jacoco.exec
[INFO] Analyzed bundle 'TechGadget Store - E-Commerce Web Application - Dao Van Cong' with 22 classes
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### 5.2. Bảng tổng hợp độ bao phủ kiểm thử toàn diện
| Module kiểm thử | Số ca kiểm thử | Số ca thành công | Số ca thất bại | Tỷ lệ thành công | Độ bao phủ dòng (C0) |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **TechOrderServiceTest** | 8 ca | 8 ca | 0 ca | **100% PASSED** | **100% Coverage (46/46 lines)** |
| **OrderManagementServiceTest** | 9 ca | 9 ca | 0 ca | **100% PASSED** | **96% Coverage** |
| **ProductServiceTest** | 7 ca | 7 ca | 0 ca | **100% PASSED** | **94% Coverage** |
| **TỔNG TOÀN HỆ THỐNG** | **24 ca kiểm thử** | **24 ca** | **0 ca** | **100% PASSED** | **Đạt tiêu chuẩn nghiệm thu P0/P1** |

---

## VI. TỰ ĐÁNH GIÁ ĐIỂM THEO TIÊU CHÍ RUBRIC (MỤC L2.1 LOGBOOK)

| STT | Tiêu chí đánh giá Rubric | Tiêu chuẩn chất lượng đạt được | Mức độ hoàn thành | Điểm tự đánh giá |
| :---: | :--- | :--- | :---: | :---: |
| 1 | **Thiết kế Đồ thị dòng điều khiển (CFG)** | Xác định chính xác 22 nút, 29 cạnh, 8 nút vị từ; vẽ sơ đồ CFG trực quan, đúng cú pháp đồ thị luồng. | Xuất sắc (Chính xác tuyệt đối) | **10.0 / 10.0** |
| 2 | **Tính toán độ phức tạp McCabe V(G)** | Áp dụng đúng cả 2 công thức: $V(G) = E - N + 2P = 9$ và $V(G) = P_r + 1 = 9$; lập luận chặt chẽ. | Xuất sắc (Chuẩn lý thuyết) | **10.0 / 10.0** |
| 3 | **Xác định tập Đường dẫn cơ sở** | Tìm đủ 8 đường dẫn độc lập (Basis Paths) bao phủ toàn bộ các nhánh rẽ và ngoại lệ. | Xuất sắc (Đầy đủ 8 đường dẫn) | **10.0 / 10.0** |
| 4 | **Thiết kế các ca kiểm thử (Test Cases)** | Thiết kế trọn vẹn 24 ca kiểm thử chi tiết bao phủ cả 3 tầng nghiệp vụ: Tính tiền (8 TCs), Quản lý đơn/Kho (9 TCs), Quản lý sản phẩm (7 TCs); tỷ lệ Pass 100%. | Xuất sắc (24 Test Cases) | **10.0 / 10.0** |
| 5 | **Cài đặt & Thực thi JUnit 5 / JaCoCo** | Triển khai 3 bộ test suite JUnit 5, chạy tự động 24/24 tests đều PASS, đo độ bao phủ JaCoCo đạt mức cao. | Xuất sắc (24/24 Tests PASS) | **10.0 / 10.0** |
| 6 | **Hoàn thiện tài liệu & Repository** | Có file Word chuẩn chỉ nộp giảng viên, toàn bộ test và code đã push lên GitHub cá nhân. | Xuất sắc (Đã push GitHub) | **10.0 / 10.0** |
| **TỔNG** | **ĐÁNH GIÁ CHUNG (MỤC L2.1)** | **Đạt toàn bộ tiêu chuẩn chất lượng cao nhất của học phần** | **XUẤT SẮC** | **10.0 / 10.0** |

---

## VII. THÔNG TIN REPOSITORY GITHUB & KẾT LUẬN

- **GitHub Repository**: [https://github.com/hukhong25/KTHOPTRANG](https://github.com/hukhong25/KTHOPTRANG)
- **Lệnh Clone và chạy kiểm thử tự động toàn bộ 24 test cases**:
  ```powershell
  git clone https://github.com/hukhong25/KTHOPTRANG.git
  cd KTHOPTRANG
  .\mvnw.cmd test
  ```
