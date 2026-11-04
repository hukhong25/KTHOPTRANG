# TÀI LIỆU KIỂM THỬ HỘP TRẮNG – TECHSTORE

- **Repository:** https://github.com/hukhong25/KTHOPTRANG
- **Nhánh khảo sát:** `main`
- **Phiên bản nền:** Spring Boot 3.2.5, Java 17, JUnit 5, JaCoCo 0.8.11
- **Mục đích:** Thống nhất giữa Tester và Developer về logic, nhánh chương trình, dữ liệu kiểm thử và tiêu chí hoàn thành.
- **Phạm vi ưu tiên:** `TechOrderService`, `OrderManagementService`, `ProductService`, `VoucherController`.

> Đây là tài liệu thiết kế kiểm thử/review tĩnh theo mã nguồn đọc được trên nhánh `main`, không phải báo cáo đã thực thi test. Tester phải chạy test và ghi nhận kết quả thực tế trước khi kết luận PASS/FAIL.

---

## 1. Mục tiêu và phương pháp

### 1.1 Mục tiêu
1. Kiểm tra các câu lệnh và nhánh điều kiện trong các hàm nghiệp vụ.
2. Bao phủ luồng hợp lệ, luồng ngoại lệ và các giá trị biên.
3. Phát hiện sai lệch giữa quy tắc mô tả trong README và hành vi thực tế của code.
4. Cung cấp bộ test có thể chuyển thành JUnit 5/Mockito.
5. Đặt tiêu chí Coverage đo bằng JaCoCo.

### 1.2 Kỹ thuật kiểm thử
- **Statement Coverage:** Mỗi câu lệnh thực thi ít nhất một lần.
- **Branch Coverage:** Mỗi nhánh True/False của điều kiện được thực thi.
- **Condition/Boundary Testing:** Kiểm tra các giá trị ngay dưới, bằng và ngay trên ngưỡng.
- **Exception Path Testing:** Kiểm tra đúng loại exception và thông điệp/response tương ứng.
- **State/Side-effect Testing:** Kiểm tra tồn kho, điểm thưởng, đơn hàng và trạng thái sau thao tác.

### 1.3 Ngoài phạm vi
- Kiểm thử tải, bảo mật, giao diện và khả năng chịu lỗi triển khai.
- Kiểm thử tích hợp với CSDL thật: hiện tại `DataStore` là kho dữ liệu in-memory dùng `ConcurrentHashMap`.
- Xác nhận nghiệp vụ chưa được Product Owner/Developer chốt (được đánh dấu **CẦN THỐNG NHẤT**).

---

## 2. Cấu trúc và đối tượng kiểm thử

| Thành phần | Hàm/đối tượng | Mức ưu tiên |
|---|---|---|
| `TechOrderService` | `processOrder(OrderRequest)` | P0 – lõi tính tiền |
| `OrderManagementService` | `checkout`, `cancelOrder`, `updateStatus`, `getOrders`, `getAdminStats` | P0 |
| `ProductService` | `getProducts`, `getProductById`, `createProduct`, `updateProduct`, `deleteProduct` | P1 |
| `VoucherController` | `validateVoucher`, `createVoucher` | P1 |
| `DataStore` | CRUD và khởi tạo dữ liệu | P2 |
| Controllers | HTTP mapping, response wrapper, exception mapping | P1 – integration/API |

---

## 3. Quy ước dữ liệu kiểm thử

Trừ khi test case nói khác, sử dụng:

- Sản phẩm P1: giá 1.000.000đ, tồn kho 10.
- Số điện thoại: `0987654321`.
- Khoảng cách: 5km.
- Giao thường, thanh toán COD.
- Khách hàng STANDARD.
- Không áp dụng voucher.
- Ngày kiểm thử voucher: `LocalDate.now()`.

Với các phép tính tiền, các con số trong tài liệu giả định kết quả hiện tại của code dùng `double`. Developer nên cân nhắc chuyển tiền sang `BigDecimal` và quy định cách làm tròn trước khi đưa vào môi trường thật.

---

## 4. Kiểm thử hộp trắng – TechOrderService.processOrder

### 4.1 Luồng xử lý cần bao phủ

1. Kiểm tra request, items, phone và distance.
2. Duyệt danh sách sản phẩm; kiểm tra quantity và stock; tính subtotal.
3. Nếu có voucher: kiểm tra expiry date, min order, tính discount và giới hạn max discount.
4. Xác định hạng khách hàng và member discount.
5. Tính totalAfterDiscounts.
6. Xác định freeship hay phí ship cơ bản/phụ phí khoảng cách.
7. Nếu giao nhanh, cộng phụ phí 25.000đ.
8. Nếu thanh toán Banking/E-Wallet, giảm 2%.
9. Tính finalAmount, loyaltyPoints và tạo OrderResult.

### 4.2 Bảng nhánh điều kiện

| Mã nhánh | Điều kiện trong code | Nhánh cần chạy |
|---|---|---|
| WB-ORD-B01 | request == null | True / False |
| WB-ORD-B02 | items == null hoặc empty | True / False |
| WB-ORD-B03 | phone == null hoặc blank | True / False |
| WB-ORD-B04 | distanceKm < 0 | True / False |
| WB-ORD-B05 | quantity <= 0 | True / False |
| WB-ORD-B06 | quantity > stock | True / False |
| WB-ORD-B07 | voucher != null | True / False |
| WB-ORD-B08 | expiryDate != null và before(today) | True / False |
| WB-ORD-B09 | subtotal < minOrderValue | True / False |
| WB-ORD-B10 | maxDiscount > 0 | True / False |
| WB-ORD-B11 | tier == DIAMOND / GOLD / SILVER / STANDARD | Từng nhánh |
| WB-ORD-B12 | totalAfterDiscounts >= 500000 | True / False |
| WB-ORD-B13 | distanceKm <= 10 | True / False |
| WB-ORD-B14 | distanceKm > 10 | True / False |
| WB-ORD-B15 | expressDelivery | True / False |
| WB-ORD-B16 | paymentMethod là BANKING hoặc E_WALLET | True / False |

Lưu ý: Bảng này là danh sách nhánh logic cần thiết kế test; một số biểu thức có nhiều điều kiện con nên cần thêm test riêng để đạt condition coverage.

### 4.3 Danh sách test case

| ID | Mục tiêu / dữ liệu chính | Kết quả mong đợi |
|---|---|---|
| WB-ORD-01 | request = null | Ném `IllegalArgumentException` |
| WB-ORD-02 | items = null | Ném `IllegalArgumentException` |
| WB-ORD-03 | items rỗng | Ném `IllegalArgumentException` |
| WB-ORD-04 | phone = null | Ném `IllegalArgumentException` |
| WB-ORD-05 | phone = chuỗi trắng | Ném `IllegalArgumentException` |
| WB-ORD-06 | distance = -0.01 | Ném `IllegalArgumentException` |
| WB-ORD-07 | quantity = 0 | Ném `IllegalStateException` |
| WB-ORD-08 | quantity = -1 | Ném `IllegalStateException` |
| WB-ORD-09 | quantity = stock | Được chấp nhận |
| WB-ORD-10 | quantity = stock + 1 | Ném `IllegalStateException` |
| WB-ORD-11 | voucher = null | voucherDiscount = 0 |
| WB-ORD-12 | voucher hết hạn hôm qua | Ném `IllegalArgumentException` |
| WB-ORD-13 | expiryDate = hôm nay | Không bị coi là hết hạn |
| WB-ORD-14 | expiryDate = null | Theo code hiện tại: không bị coi là hết hạn |
| WB-ORD-15 | subtotal = minOrderValue - 1 | Ném `IllegalArgumentException` |
| WB-ORD-16 | subtotal = minOrderValue | Voucher được xét áp dụng |
| WB-ORD-17 | discount 10%, maxDiscount = 0 | Dùng toàn bộ discount tính được |
| WB-ORD-18 | discount 10%, maxDiscount > discount tính được | Discount bằng discount tính được |
| WB-ORD-19 | discount 10%, maxDiscount < discount tính được | Discount bằng maxDiscount |
| WB-ORD-20 | tier DIAMOND, subtotal 1.000.000 | memberDiscount = 50.000 |
| WB-ORD-21 | tier GOLD, subtotal 1.000.000 | memberDiscount = 30.000 |
| WB-ORD-22 | tier SILVER, subtotal 1.000.000 | memberDiscount = 10.000 |
| WB-ORD-23 | tier STANDARD/null, subtotal 1.000.000 | memberDiscount = 0 |
| WB-ORD-24 | total sau giảm = 500.000, distance = 10 | shippingFee = 0 |
| WB-ORD-25 | total sau giảm = 499.999, distance = 10 | shippingFee = 30.000 |
| WB-ORD-26 | total sau giảm = 500.000, distance = 10.01 | Có phí cơ bản + phụ phí khoảng cách |
| WB-ORD-27 | distance = 10 | Không cộng phụ phí khoảng cách |
| WB-ORD-28 | distance = 11 | Phụ phí khoảng cách = 5.000 |
| WB-ORD-29 | expressDelivery = true | Cộng 25.000 |
| WB-ORD-30 | expressDelivery = false | Không cộng phụ phí nhanh |
| WB-ORD-31 | paymentMethod = COD | paymentDiscount = 0 |
| WB-ORD-32 | paymentMethod = ONLINE_BANKING | paymentDiscount = 2% totalAfterDiscounts |
| WB-ORD-33 | paymentMethod = E_WALLET | paymentDiscount = 2% totalAfterDiscounts |
| WB-ORD-34 | finalAmount = 9.999 | loyaltyPoints = 0 |
| WB-ORD-35 | finalAmount = 10.000 | loyaltyPoints = 1 |

### 4.4 Bộ dữ liệu tính toán chuẩn để đối chiếu

**Case A – Diamond + voucher chạm trần + freeship**

- Subtotal: 20.000.000đ.
- Voucher: 10%, max 500.000đ.
- Diamond: 5%.
- Distance: 5km, giao thường, COD.
- Kỳ vọng: voucher 500.000đ; member 1.000.000đ; ship 0đ; final 18.500.000đ; 1.850 điểm.

**Case B – Gold + giao xa + giao nhanh + Banking**

- Subtotal: 15.000.000đ.
- Gold: 3% = 450.000đ.
- Distance: 15km.
- Ship: 30.000 + (15 - 10) × 5.000 + 25.000 = 80.000đ.
- Banking: 2% × 14.550.000 = 291.000đ.
- Final: 14.339.000đ; 1.433 điểm.

**Case C – Silver + đơn nhỏ + E-Wallet**

- Subtotal: 200.000đ.
- Silver: 2.000đ.
- Ship: 30.000đ.
- E-Wallet discount: 3.960đ.
- Final: 224.040đ; 22 điểm.

---

## 5. Kiểm thử hộp trắng – OrderManagementService

### 5.1 checkout()

| ID | Điều kiện / dữ liệu | Kết quả mong đợi |
|---|---|---|
| WB-CHK-01 | req = null | IllegalArgumentException |
| WB-CHK-02 | items = null/rỗng | IllegalArgumentException |
| WB-CHK-03 | productId không tồn tại | IllegalArgumentException |
| WB-CHK-04 | quantity = 0 hoặc âm | IllegalStateException |
| WB-CHK-05 | quantity vượt stock | IllegalStateException |
| WB-CHK-06 | voucherCode null/rỗng | Không áp dụng voucher |
| WB-CHK-07 | voucherCode có khoảng trắng đầu/cuối | Trim trước khi tra cứu |
| WB-CHK-08 | voucherCode không tồn tại | IllegalArgumentException |
| WB-CHK-09 | customerId không tồn tại | Dùng tier được truyền lên hoặc STANDARD |
| WB-CHK-10 | customer tồn tại, tier request null | Lấy tier từ customer |
| WB-CHK-11 | paymentMethod null | Mặc định COD |
| WB-CHK-12 | checkout thành công | Trừ kho, cộng điểm (nếu có customer), lưu đúng một đơn |
| WB-CHK-13 | TechOrderService ném exception | Không được trừ kho/lưu đơn |
| WB-CHK-14 | hai dòng cart cùng productId, tổng quantity vượt stock | Phải từ chối checkout; kiểm tra lỗi hiện tại bên dưới |

**Kiểm tra side effect bắt buộc sau checkout thành công:**
- Stock mới = stock cũ - tổng quantity đã mua.
- Customer points mới = points cũ + loyaltyPoints của đơn.
- Đơn hàng có đủ thông tin, chiết khấu, phí ship, tổng tiền và trạng thái CONFIRMED.
- Các sản phẩm khác không bị thay đổi.

### 5.2 cancelOrder()

| ID | Điều kiện | Kết quả mong đợi |
|---|---|---|
| WB-CAN-01 | orderId không tồn tại | IllegalArgumentException |
| WB-CAN-02 | trạng thái CANCELLED | IllegalStateException |
| WB-CAN-03 | trạng thái DELIVERED | IllegalStateException |
| WB-CAN-04 | trạng thái CONFIRMED/SHIPPING | Hủy thành công |
| WB-CAN-05 | sản phẩm còn tồn tại | Hoàn lại đúng quantity |
| WB-CAN-06 | sản phẩm đã bị xóa | Bỏ qua sản phẩm null, vẫn hủy đơn |
| WB-CAN-07 | gọi hủy lần hai | Không được hoàn kho lần hai |

### 5.3 updateStatus(), getOrders(), getAdminStats()

| ID | Hàm | Kiểm tra |
|---|---|---|
| WB-OM-01 | updateStatus | order tồn tại, status hợp lệ |
| WB-OM-02 | updateStatus | order không tồn tại |
| WB-OM-03 | updateStatus | status chữ thường được chuyển thành chữ hoa |
| WB-OM-04 | updateStatus | status không hợp lệ / null – cần chốt quy tắc |
| WB-OM-05 | getOrders | customerId null hoặc rỗng: trả toàn bộ đơn |
| WB-OM-06 | getOrders | customerId hợp lệ: chỉ trả đơn của khách đó |
| WB-OM-07 | getAdminStats | Doanh thu loại trừ đơn CANCELLED |
| WB-OM-08 | getAdminStats | totalOrders vẫn tính cả đơn CANCELLED |
| WB-OM-09 | getAdminStats | outOfStockCount đếm stock <= 0 |
| WB-OM-10 | getAdminStats | statusCounts nhóm đúng theo trạng thái |

---

## 6. Kiểm thử hộp trắng – ProductService

| ID | Hàm | Dữ liệu / nhánh | Kết quả mong đợi |
|---|---|---|---|
| WB-PRO-01 | getProducts | category = null | Không lọc theo category |
| WB-PRO-02 | getProducts | category = ALL | Trả mọi category |
| WB-PRO-03 | getProducts | category đúng, khác hoa thường | Lọc không phân biệt hoa thường |
| WB-PRO-04 | getProducts | category không tồn tại | Danh sách rỗng |
| WB-PRO-05 | getProducts | search null/rỗng | Không lọc từ khóa |
| WB-PRO-06 | getProducts | search khớp tên | Trả sản phẩm phù hợp |
| WB-PRO-07 | getProducts | search khớp description | Trả sản phẩm phù hợp |
| WB-PRO-08 | getProducts | search không khớp | Danh sách rỗng |
| WB-PRO-09 | getProducts | sort = price_asc | Giá tăng dần |
| WB-PRO-10 | getProducts | sort = price_desc | Giá giảm dần |
| WB-PRO-11 | getProducts | sort = rating_desc | Rating giảm dần |
| WB-PRO-12 | getProducts | sort null/khác | Sắp xếp theo id |
| WB-PRO-13 | getProductById | id tồn tại | Trả đúng sản phẩm |
| WB-PRO-14 | getProductById | id không tồn tại | IllegalArgumentException |
| WB-PRO-15 | createProduct | id null/rỗng | Sinh id PROD-n |
| WB-PRO-16 | createProduct | id có sẵn | Giữ id theo code hiện tại |
| WB-PRO-17 | updateProduct | id tồn tại | Cập nhật trường |
| WB-PRO-18 | updateProduct | imageUrl null/rỗng | Giữ imageUrl cũ |
| WB-PRO-19 | updateProduct | id không tồn tại | IllegalArgumentException |
| WB-PRO-20 | deleteProduct | id tồn tại | Xóa khỏi DataStore |
| WB-PRO-21 | deleteProduct | id không tồn tại | Hiện tại không báo lỗi; cần chốt API contract |

---

## 7. Kiểm thử VoucherController

| ID | Điều kiện | Kết quả mong đợi |
|---|---|---|
| WB-VOU-01 | code không tồn tại | ApiResponse.error |
| WB-VOU-02 | voucher hết hạn | ApiResponse.error |
| WB-VOU-03 | voucher hết hạn đúng hôm nay | Không bị coi là hết hạn |
| WB-VOU-04 | subtotal dưới minOrderValue | ApiResponse.error |
| WB-VOU-05 | subtotal đúng minOrderValue | Voucher hợp lệ |
| WB-VOU-06 | discount vượt maxDiscount | discountAmount = maxDiscount |
| WB-VOU-07 | maxDiscount <= 0 | Dùng calculatedDiscount |
| WB-VOU-08 | createVoucher | Lưu voucher theo code |

Cần kiểm tra thêm request có subtotal âm, code rỗng và voucher có discountPercent/maxDiscount không hợp lệ. Hiện controller chưa có validate các trường hợp này.

---

## 8. Các vấn đề phát hiện qua review code – cần Developer xác nhận

Các mục dưới đây là phát hiện từ đọc code, chưa phải lỗi đã được tái hiện bằng chạy chương trình.

| Mã | Mức độ | Vị trí | Nhận xét / đề xuất |
|---|---|---|---|
| DEF-01 | Critical | OrderManagementService.checkout | Hai dòng cart trùng productId có thể cùng vượt qua kiểm tra riêng lẻ nhưng tổng quantity vượt stock. Cần gộp quantity theo productId trước khi kiểm tra và trừ kho. |
| DEF-02 | High | TechOrderService.processOrder | Chưa kiểm tra item null; phần tử null trong list gây NullPointerException thay vì exception nghiệp vụ. |
| DEF-03 | High | TechOrderService.processOrder | Chưa kiểm tra price âm, NaN/infinity và voucher discountPercent ngoài miền hợp lệ; có thể tạo tổng tiền/chiết khấu bất thường. |
| DEF-04 | High | ProductService.createProduct | ID trùng có thể ghi đè sản phẩm hiện có vì DataStore dùng Map.put. Cần quy định reject hay update. |
| DEF-05 | High | ProductService.updateProduct | Chưa validate updated null, giá âm, stock âm, tên/category null hoặc rỗng. |
| DEF-06 | Medium | ProductService.getProducts | category/name/description null có thể gây NullPointerException do gọi equalsIgnoreCase/toLowerCase trực tiếp. |
| DEF-07 | Medium | OrderManagementService.updateStatus | Cho phép cập nhật tùy ý status; chưa kiểm tra tập trạng thái và chuyển trạng thái hợp lệ (ví dụ DELIVERED -> CONFIRMED). |
| DEF-08 | Medium | ProductService.deleteProduct | Xóa ID không tồn tại vẫn kết thúc bình thường; cần thống nhất response 404 hay idempotent 200. |
| DEF-09 | Medium | VoucherController.validateVoucher | Chưa từ chối subtotal âm; logic validate voucher đang lặp lại với TechOrderService, có nguy cơ lệch quy tắc. |
| DEF-10 | Medium | TechOrderService.processOrder | Phone chỉ kiểm tra null/rỗng, chưa kiểm tra định dạng dù README mô tả là số điện thoại hợp lệ. |

### 8.1 Quyết định cần thống nhất trước khi sửa code

1. **Số điện thoại:** Chỉ cần không rỗng hay phải đúng định dạng Việt Nam? Có chấp nhận +84 không?
2. **Giá/stock:** Giá phải > 0 hay >= 0? Stock phải >= 0?
3. **Voucher:** discountPercent cho phép từ 0–100 hay lớn hơn 100? maxDiscount = 0 có nghĩa không giới hạn hay voucher không hợp lệ?
4. **Trạng thái đơn:** Tập trạng thái chính thức và các chuyển trạng thái hợp lệ là gì?
5. **Sản phẩm trùng ID:** Từ chối tạo mới hay cho phép ghi đè?
6. **Cart trùng productId:** Có tự gộp dòng hay trả lỗi? Dù chọn cách nào, tổng quantity không được vượt tồn kho.
7. **Tiền tệ:** Có làm tròn đến đồng không? Nên dùng BigDecimal thay double cho tiền.

---

## 9. Tiêu chuẩn triển khai JUnit 5

### 9.1 Quy ước tên test
Dùng mẫu:

`methodName_condition_expectedResult`

Ví dụ:
- `processOrder_WhenRequestIsNull_ShouldThrowIllegalArgumentException`
- `checkout_WhenDuplicateItemsExceedStock_ShouldRejectOrder`
- `cancelOrder_WhenAlreadyCancelled_ShouldNotRestoreStockAgain`

### 9.2 Quy tắc viết test
- Mỗi test tập trung vào một hành vi/nhánh chính.
- Dùng `@BeforeEach` để tạo fixture độc lập.
- Dùng `assertThrows` cho exception.
- Dùng `assertEquals`/AssertJ để kiểm tra từng thành phần kết quả.
- Với `OrderManagementService`, kiểm tra cả kết quả trả về và side effect trên DataStore.
- Không phụ thuộc thứ tự phần tử của `ConcurrentHashMap` nếu chưa sort.
- Test ngày hết hạn nên dùng `LocalDate.now()`, `minusDays(1)`, `plusDays(1)`; tránh hard-code ngày cố định.

### 9.3 Lệnh chạy và xem Coverage

PowerShell:

```powershell
.\mvnw.cmd clean test
```

Báo cáo JaCoCo sau khi chạy:

```
target/site/jacoco/index.html
```

Nếu muốn chạy một test class:

```powershell
.\mvnw.cmd -Dtest=TechOrderServiceTest test
```

### 9.4 Tiêu chí nghiệm thu đề xuất

| Tiêu chí | Mục tiêu |
|---|---|
| Unit test của các hàm P0 | 100% test case đã thống nhất được triển khai |
| Statement coverage của lớp nghiệp vụ P0 | >= 95% |
| Branch coverage của lớp nghiệp vụ P0 | >= 90% |
| Critical/High defect | Không còn lỗi chưa có phương án xử lý |
| Regression | Toàn bộ test cũ và mới đều PASS |
| Side effect | Stock, points, order status đúng sau checkout/cancel |

Coverage cao không đồng nghĩa không còn bug. Các tiêu chí trên là ngưỡng nghiệm thu đề xuất, cần Tester/Developer thống nhất.

---

## 10. Quy trình phối hợp Tester – Developer

| Bước | Tester | Developer |
|---|---|---|
| 1. Thống nhất nghiệp vụ | Gửi các câu hỏi tại mục 8.1 | Xác nhận quy tắc |
| 2. Chốt test design | Chốt ID, input, expected result | Review tính khả thi |
| 3. Implement | Viết JUnit theo test case | Sửa code nếu test phát hiện lỗi |
| 4. Chạy test | Ghi PASS/FAIL, log và coverage | Phân tích nguyên nhân |
| 5. Fix & regression | Chạy lại test liên quan và toàn bộ suite | Không làm hỏng hành vi đã chốt |
| 6. Nghiệm thu | Xác nhận Coverage và defect | Xác nhận hoàn tất |

### Mẫu ghi nhận lỗi

| Trường | Nội dung |
|---|---|
| Defect ID | DEF-xx |
| Test Case | WB-xxx |
| Module/Method | Tên lớp và hàm |
| Input | Dữ liệu tái hiện |
| Expected | Kết quả đã thống nhất |
| Actual | Kết quả thực tế |
| Evidence | Log, stack trace hoặc ảnh |
| Severity | Critical / High / Medium / Low |
| Status | Open / In Progress / Fixed / Retest / Closed |

---

## 11. Checklist thống nhất trước khi merge

- [ ] Developer xác nhận các quy tắc nghiệp vụ tại mục 8.1.
- [ ] Hai bên thống nhất danh sách test case và expected result.
- [ ] Test cho các nhánh True/False đã được triển khai.
- [ ] Các trường hợp biên và exception đã được kiểm tra.
- [ ] Các side effect của checkout/cancel đã được kiểm tra.
- [ ] Không còn lỗi Critical/High chưa được xử lý hoặc chấp thuận.
- [ ] `mvnw.cmd clean test` chạy thành công.
- [ ] Đã lưu báo cáo JaCoCo.
- [ ] Tester và Developer xác nhận kết quả regression.

---

**Kết luận:** Tài liệu này là baseline để Tester và Developer thống nhất cách hiểu nghiệp vụ và thiết kế kiểm thử hộp trắng cho TechStore. Khi quy tắc nghiệp vụ thay đổi, phải cập nhật test case và expected result trước hoặc đồng thời với việc sửa code.
