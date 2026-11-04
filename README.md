# BÀI TẬP 8 - MỤC L2.1 (LOGBOOK): TẠO PHẦN MỀM BẰNG AI & KIỂM THỬ HỘP TRẮNG

- **Sinh viên thực hiện**: Đào Văn Công
- **Mã sinh viên**: 2023601884
- **Lớp**: IT6013.2 - K18
- **Học phần**: Kiểm thử phần mềm (GV: TS. Trần Tiến Dũng - Trường CNTT & TT, Đại học Công nghiệp Hà Nội)
- **Dự án**: **TechStore Service** - Hệ thống xử lý đơn hàng bán thiết bị công nghệ (Laptop, Phụ kiện, Điện thoại)

---

## 1. PHẦN 1: TẠO PHẦN MỀM BẰNG AI (PROMPT & SOURCE CODE)

### 1.1. Lời nhắc AI (Prompt)
```text
Hãy đóng vai một chuyên gia phát triển phần mềm Backend Java. 
Dựa trên mô hình thương mại điện tử e-commerce (tương tự đồ án Book Store của nhóm nhưng triển khai cho hệ thống bán thiết bị công nghệ TechGadget Store), hãy viết một hàm xử lý nghiệp vụ đặt hàng và tính toán thanh toán:
public OrderResult processOrder(OrderRequest request)

Yêu cầu nghiệp vụ chi tiết:
1. Kiểm tra tính hợp lệ của request: Nếu request null, danh sách sản phẩm rỗng, hoặc địa chỉ nhận hàng rỗng/khoảng trắng thì ném ra IllegalArgumentException.
2. Kiểm tra tồn kho và tính tổng tiền hàng (Subtotal):
   - Duyệt từng sản phẩm: số lượng đặt phải > 0 và <= tồn kho khả dụng; nếu vi phạm ném ra IllegalStateException.
   - Cộng dồn: subtotal += unitPrice * quantity.
3. Xử lý Voucher giảm giá (nếu có):
   - Kiểm tra hạn dùng: nếu voucher.isExpired() -> ném ra IllegalArgumentException.
   - Kiểm tra giá trị đơn tối thiểu: nếu subtotal < voucher.getMinOrderValue() -> ném ra IllegalArgumentException.
   - Tính tiền giảm voucher = subtotal * (voucher.getDiscountPercent() / 100.0). Nếu vượt quá voucher.getMaxDiscount() thì chỉ giảm bằng maxDiscount.
4. Ưu đãi hạng thành viên (Customer Tier):
   - DIAMOND: giảm thêm 5% subtotal.
   - GOLD: giảm thêm 3% subtotal.
   - SILVER: giảm thêm 1% subtotal.
   - STANDARD / null: giảm 0%.
5. Tính phí vận chuyển (Tiered Shipping Fee):
   - Miễn phí vận chuyển (0đ) nếu subtotal >= 500.000đ và khoảng cách giao hàng <= 10km.
   - Ngược lại: Phí cơ bản 30.000đ. Nếu khoảng cách > 10km, cộng thêm phụ phí 3.000đ/km cho mỗi km vượt quá 10km.
   - Nếu khách chọn giao hàng hỏa tốc (request.isExpress()): cộng thêm 25.000đ phí hỏa tốc.
6. Ưu đãi phương thức thanh toán:
   - Nếu thanh toán trực tuyến (BANKING hoặc E_WALLET): chiết khấu thêm 2% trên tổng tiền sau khi trừ giảm giá.
7. Tổng tiền thanh toán cuối cùng (Final Total):
   - finalTotal = (subtotal - discountAmount) + shippingFee.
   - Trả về đối tượng OrderResult chứa đầy đủ các thông tin tính toán chi tiết.
```

### 1.2. Mã nguồn triển khai
- File nguồn chính: [`TechOrderService.java`](src/main/java/com/daovancong/techstore/TechOrderService.java)
- Bộ dữ liệu đi kèm: `OrderItem`, `CustomerTier`, `PaymentMethod`, `Voucher`, `OrderRequest`, `OrderResult`.

---

## 2. PHẦN 2: THỰC HIỆN KIỂM THỬ HỘP TRẮNG (WHITE-BOX TESTING)

### 2.1. Đánh số nút và Đồ thị dòng điều khiển (CFG)
Phân tích đồ thị dòng điều khiển phương thức `processOrder`:

| Mã Nút | Dòng code / Đoạn mã logic | Ý nghĩa nghiệp vụ |
| :---: | :--- | :--- |
| **1** | `if (request == null \|\| items == null \|\| items.isEmpty() \|\| ...)` | Kiểm tra tính hợp lệ của request đầu vào |
| **2** | `throw new IllegalArgumentException("Dữ liệu đơn hàng không hợp lệ!");` | Ném ngoại lệ khi request lỗi |
| **3** | Vòng lặp `for (OrderItem item : request.getItems())`, kiểm tra `quantity <= 0 \|\| quantity > stock` | Kiểm tra số lượng tồn kho từng sản phẩm |
| **4** | `throw new IllegalStateException("Số lượng không hợp lệ hoặc vượt tồn kho");` | Ném ngoại lệ khi vi phạm tồn kho |
| **5** | `Voucher v = request.getVoucher(); if (v != null)` | Kiểm tra đơn hàng có áp dụng Voucher không |
| **6** | `if (v.isExpired())` | Kiểm tra Voucher hết hạn sử dụng |
| **7** | `if (subtotal < v.getMinOrderValue())` | Kiểm tra đơn hàng đạt giá trị tối thiểu của Voucher |
| **8** | `throw new IllegalArgumentException(...)` | Ném ngoại lệ khi Voucher không đủ điều kiện |
| **9** | `vouchDiscount = Math.min(...)` | Tính số tiền giảm giá của voucher hợp lệ |
| **10** | `switch (request.getTier()) { DIAMOND, GOLD, SILVER, default }` | Tính chiết khấu theo hạng khách hàng |
| **11** | `if (subtotal >= 500000 && request.getDistanceKm() <= 10.0)` | Kiểm tra điều kiện miễn phí vận chuyển |
| **12** | `shippingFee = 0.0;` | Gán phí vận chuyển 0đ (Freeship) |
| **13** | `shippingFee = 30000.0; if (request.getDistanceKm() > 10.0) ...` | Tính phí vận chuyển cơ bản và phụ phí quãng đường |
| **14** | `if (request.isExpress())` | Kiểm tra dịch vụ giao hàng hỏa tốc |
| **15** | `shippingFee += 25000.0;` | Cộng phụ phí giao hàng hỏa tốc |
| **16** | `if (payMethod == BANKING \|\| payMethod == E_WALLET)` | Kiểm tra thanh toán trực tuyến |
| **17** | `payDiscount = discountedSubtotal * 0.02;` | Áp dụng chiết khấu 2% thanh toán trực tuyến |
| **18** | `payDiscount = 0.0;` | Không giảm giá thanh toán (COD) |
| **19** | `finalTotal = Math.max(0, discountedSubtotal + shippingFee);` | Tính tổng tiền thanh toán cuối cùng |
| **20** | `return new OrderResult(...)` | Trả về kết quả đơn hàng thành công |

### 2.2. Tính toán độ phức tạp đồ thị McCabe (Cyclomatic Complexity)
- **Số nút (Nodes)**: $N = 22$
- **Số cạnh (Edges)**: $E = 29$
- **Số thành phần liên thông (P)**: $P = 1$
- **Công thức McCabe 1**:
  $$V(G) = E - N + 2P = 29 - 22 + 2(1) = 9$$
- **Công thức McCabe 2** (dựa trên số nút vị từ $P_r$):
  Phương thức có 8 nút điều kiện phân nhánh chính:
  $$V(G) = P_r + 1 = 8 + 1 = 9$$

### 2.3. Tập đường dẫn cơ sở (Basis Paths)
1. **Path 1**: `1 -> 2 (Kết thúc)`: Request không hợp lệ (bị null hoặc rỗng) -> Ném `IllegalArgumentException`.
2. **Path 2**: `1 -> 3 -> 4 (Kết thúc)`: Sản phẩm đặt có số lượng <= 0 hoặc vượt tồn kho -> Ném `IllegalStateException`.
3. **Path 3**: `1 -> 3 -> 5 -> 6 -> 8 (Kết thúc)`: Có voucher nhưng voucher đã hết hạn -> Ném `IllegalArgumentException`.
4. **Path 4**: `1 -> 3 -> 5 -> 6 -> 7 -> 8 (Kết thúc)`: Voucher còn hạn nhưng subtotal < minOrderValue -> Ném `IllegalArgumentException`.
5. **Path 5**: `1 -> 3 -> 5 -> 6 -> 7 -> 9 -> 10(Diamond) -> 11 -> 12 -> 14(False) -> 16(False) -> 18 -> 19 -> 20`: Khách Diamond, voucher hợp lệ, đơn hàng đủ điều kiện Freeship, giao tiêu chuẩn, COD.
6. **Path 6**: `1 -> 3 -> 5(False) -> 10(Gold) -> 11(False) -> 13(Cộng km) -> 14(True) -> 15 -> 16(True) -> 17 -> 19 -> 20`: Khách Gold, không dùng voucher, khoảng cách > 10km, giao hỏa tốc, thanh toán BANKING.
7. **Path 7**: `1 -> 3 -> 5(False) -> 10(Silver) -> 11(False) -> 13(Tiêu chuẩn) -> 14(False) -> 16(True) -> 17 -> 19 -> 20`: Khách Silver, đơn < 500k khoảng cách gần, giao thường, thanh toán ví điện tử E_WALLET.
8. **Path 8**: `1 -> 3 -> 5(False) -> 10(Default) -> 11(False) -> 13(Cộng km) -> 14(False) -> 16(False) -> 18 -> 19 -> 20`: Khách Vãng lai (Standard), không voucher, khoảng cách > 10km, giao thường, thanh toán tiền mặt COD.

### 2.4. Thiết kế các ca kiểm thử (Test Cases)

| Test Case ID | Tên kịch bản | Dữ liệu đầu vào | Kết quả mong đợi | Trạng thái |
| :---: | :--- | :--- | :--- | :---: |
| **TC_CONG_01** | Request không hợp lệ (null / items rỗng) | `request = null` | Ném `IllegalArgumentException` | **PASS** |
| **TC_CONG_02** | Số lượng mua vượt quá số lượng tồn kho | Laptop Dell XPS: qty=5, stock=2 | Ném `IllegalStateException` | **PASS** |
| **TC_CONG_03** | Voucher đã hết hạn sử dụng | `expired = true`, subtotal=20.000.000 | Ném `IllegalArgumentException` | **PASS** |
| **TC_CONG_04** | Đơn hàng không đạt giá trị tối thiểu của Voucher | subtotal=200.000, `minOrder=500.000` | Ném `IllegalArgumentException` | **PASS** |
| **TC_CONG_05** | Khách Diamond, Voucher MaxDiscount, Freeship, COD | Diamond, subtotal=20tr, voucher giảm tối đa 500k, 5km | `finalTotal = 18.500.000đ`, `shipping = 0đ` | **PASS** |
| **TC_CONG_06** | Khách Gold, không Voucher, KC > 10km, Hỏa tốc, Banking | Gold, subtotal=15tr, 15km, express=true, BANKING | `finalTotal = 14.332.000đ` | **PASS** |
| **TC_CONG_07** | Khách Silver, đơn < 500k, KC gần, E_WALLET | Silver, subtotal=200k, 5km, express=false, E_WALLET | `finalTotal = 224.040đ` | **PASS** |
| **TC_CONG_08** | Khách Standard, không voucher, KC > 10km, COD | Standard, subtotal=1.000k, 12km, express=false, COD | `finalTotal = 1.036.000đ` | **PASS** |

### 2.5. Kết quả thực thi kiểm thử JUnit 5 & JaCoCo Coverage
Lệnh thực thi:
```powershell
.\mvnw.cmd test
```

Kết quả:
```text
[INFO] Running com.daovancong.techstore.TechOrderServiceTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.088 s -- in com.daovancong.techstore.TechOrderServiceTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```
- **Tỷ lệ Pass**: 8/8 Tests (100% Passed).
- **Độ bao phủ dòng (Line Coverage)**: 100% trên phương thức `processOrder`.

---

## 3. CÁCH ĐẨY MÃ NGUỒN LÊN GITHUB REPOSITORY MỚI

Thư mục này đã sẵn sàng để đẩy lên một repository riêng của cá nhân:

```powershell
# Di chuyển vào thư mục bài làm
cd "D:\Tai lieu hoc\KiemThu\Bai8_DaoVanCong_2023601884"

# Kiểm tra trạng thái Git
git status

# Thêm remote dẫn tới repository GitHub mới tạo
git remote add origin https://github.com/<tai-khoan-github-cua-ban>/<ten-repo-moi>.git

# Đặt nhánh chính là main và push code
git branch -M main
git push -u origin main
```
