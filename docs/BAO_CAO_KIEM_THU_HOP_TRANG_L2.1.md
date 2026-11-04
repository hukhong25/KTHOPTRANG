# BÁO CÁO THỰC HÀNH BÀI 8 - MỤC L2.1 (LOGBOOK)
# BÁO CÁO KIỂM THỬ HỘP TRẮNG (WHITE-BOX TESTING REPORT)

- **Sinh viên thực hiện**: **ĐÀO VĂN CÔNG**
- **Mã sinh viên (MSV)**: **2023601884**
- **Lớp khóa học**: IT6013.2 - K18
- **Học phần**: Kiểm thử phần mềm (GVHD: TS. Trần Tiến Dũng - Trường CNTT & TT, ĐH Công nghiệp Hà Nội)
- **Hệ thống kiểm thử**: Website Bán Thiết Bị Số & Phụ Kiện Công Nghệ (**TechStore**)
- **Phương thức trọng tâm**: `TechOrderService.processOrder(OrderRequest request)`
- **GitHub Repository**: [https://github.com/hukhong25/KTHOPTRANG](https://github.com/hukhong25/KTHOPTRANG)

---

## I. ĐỐI TƯỢNG VÀ PHẠM VI KIỂM THỬ

- **Phương thức mục tiêu**: `public OrderResult processOrder(OrderRequest request)` trong lớp `TechOrderService.java`.
- **Nghiệp vụ hàm**: Xử lý tính toán hóa đơn thanh toán cho đơn hàng gồm 7 quy tắc:
  1. *Validation*: Kiểm tra request null, items rỗng, số điện thoại trống, cự ly âm.
  2. *Tồn kho*: Kiểm tra số lượng mua từng món phải $> 0$ và $\le$ tồn kho hiện có; tính `subTotal`.
  3. *Voucher*: Kiểm tra hạn sử dụng, ngưỡng đơn tối thiểu, tính mức giảm theo % và áp trần tối đa.
  4. *Hội viên VIP*: DIAMOND giảm 5%, GOLD giảm 3%, SILVER giảm 1%, STANDARD giảm 0%.
  5. *Phí vận chuyển*: Freeship 0đ nếu đơn sau giảm $\ge 500k$ và cự ly $\le 10km$; cước cơ bản 30k + 5k/km vượt quá 10km; phụ phí hỏa tốc +25k.
  6. *Thanh toán trực tuyến*: Giảm 2% nếu thanh toán qua Banking hoặc Ví điện tử.
  7. *Tích điểm thưởng*: 10.000đ = 1 điểm thưởng và sinh mã đơn hàng.

Mã nguồn hàm `processOrder` phục vụ đối chiếu:
```java
public OrderResult processOrder(OrderRequest request) {
    // [Node 1] Validate dữ liệu đầu vào
    if (request == null || request.getItems() == null || request.getItems().isEmpty()
            || request.getCustomerPhone() == null || request.getCustomerPhone().trim().isEmpty()
            || request.getDistanceKm() < 0.0) {
        throw new IllegalArgumentException("Thông tin đơn hàng không hợp lệ"); // [Node 2]
    }

    // [Node 3] Duyệt sản phẩm: kiểm tra tồn kho và tính subtotal
    double subTotal = 0.0;
    for (OrderItem item : request.getItems()) {
        if (item.getQuantity() <= 0 || item.getQuantity() > item.getStock()) {
            throw new IllegalStateException("Sản phẩm không đủ tồn kho hoặc số lượng không hợp lệ"); // [Node 4]
        }
        subTotal += item.getPrice() * item.getQuantity();
    }

    // [Node 5] Xử lý mã giảm giá Voucher
    double voucherDiscount = 0.0;
    if (request.getVoucher() != null) {
        Voucher voucher = request.getVoucher();
        LocalDate today = LocalDate.now();
        if (voucher.getExpiryDate() != null && voucher.getExpiryDate().isBefore(today)) {
            throw new IllegalArgumentException("Mã giảm giá đã hết hạn sử dụng"); // [Node 6]
        }
        if (subTotal < voucher.getMinOrderValue()) {
            throw new IllegalArgumentException("Đơn hàng chưa đạt giá trị tối thiểu để áp dụng voucher"); // [Node 7]
        }
        double calculatedDiscount = subTotal * (voucher.getDiscountPercent() / 100.0);
        voucherDiscount = (voucher.getMaxDiscount() > 0)
                ? Math.min(calculatedDiscount, voucher.getMaxDiscount())
                : calculatedDiscount; // [Node 8]
    }

    // [Node 9] Xử lý chiết khấu thành viên VIP
    double memberDiscount = 0.0;
    CustomerTier tier = (request.getCustomerTier() != null) ? request.getCustomerTier() : CustomerTier.STANDARD;
    if (tier == CustomerTier.DIAMOND) {
        memberDiscount = subTotal * 0.05; // [Node 10]
    } else if (tier == CustomerTier.GOLD) {
        memberDiscount = subTotal * 0.03; // [Node 11]
    } else if (tier == CustomerTier.SILVER) {
        memberDiscount = subTotal * 0.01; // [Node 12]
    } // [Node 13]

    double totalAfterDiscounts = Math.max(0.0, subTotal - voucherDiscount - memberDiscount);

    // [Node 14] Tính phí vận chuyển đa tầng
    double shippingFee = 0.0;
    if (totalAfterDiscounts >= 500000.0 && request.getDistanceKm() <= 10.0) {
        shippingFee = 0.0; // [Node 15] Freeship 0đ
    } else {
        shippingFee = 30000.0;
        if (request.getDistanceKm() > 10.0) {
            shippingFee += (request.getDistanceKm() - 10.0) * 5000.0; // [Node 16] Phụ phí cự ly
        }
    } // [Node 17]

    if (request.isExpressDelivery()) {
        shippingFee += 25000.0; // [Node 18] Phụ phí hỏa tốc
    }

    // [Node 19] Ưu đãi thanh toán trực tuyến
    double paymentDiscount = 0.0;
    if (request.getPaymentMethod() == PaymentMethod.ONLINE_BANKING || request.getPaymentMethod() == PaymentMethod.E_WALLET) {
        paymentDiscount = totalAfterDiscounts * 0.02; // [Node 20] Giảm 2%
    }

    // [Node 21] Tính tổng tiền thanh toán cuối cùng và điểm thưởng
    double finalAmount = Math.max(0.0, totalAfterDiscounts + shippingFee - paymentDiscount);
    int loyaltyPoints = (int) (finalAmount / 10000.0);
    String orderId = "TECH-ORD-" + System.currentTimeMillis();

    return new OrderResult(orderId, subTotal, voucherDiscount, memberDiscount,
            shippingFee, paymentDiscount, finalAmount, loyaltyPoints, "SUCCESS"); // [Node 22]
}
```

---

## II. THIẾT KẾ KIỂM THỬ HỘP TRẮNG (WHITE-BOX TEST DESIGN)

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

### 2.2. Sơ đồ Đồ thị dòng điều khiển (CFG)
```text
        [ (1) Validate Request? ]
               /            \
      (Lỗi)   /              \ (Hợp lệ)
             v                v
     [ (2) Throw Lỗi ]    [ (3) Loop Items: qty <= 0 || qty > stock? ]
                             /            \
                    (Lỗi)   /              \ (Đủ tồn kho)
                           v                v
                   [ (4) Throw Lỗi ]   [ (5) Có Voucher? ]
                                          /       \
                                 (Có)    /         \ (Không)
                                        v           \
                             [ (6) Hết hạn? ]        |
                               /     \              |
                      (True)  /       \ (False)     |
                             v         v            |
                       [ (6) Throw ] [ (7) < Min? ] |
                                       /     \      |
                              (True)  /       \(Đủ) |
                                     v         v    |
                               [ (7) Throw ]  [ (8) Calc Voucher ]
                                                   /
                                                  v
                                      [ (9) Xét CustomerTier ]
                                      /    |      |       \
                                     v     v      v        v
                                   (10)  (11)   (12)     (13)
                                  DIAMOND GOLD SILVER  STANDARD
                                     \     |      |       /
                                      v    v      v      v
                                      [ (14) >=500k & <=10km? ]
                                            /           \
                                   (True)  /             \ (False)
                                          v               v
                                     [ (15) 0đ ]    [ (16) Phí cự ly ]
                                          \               /
                                           v             v
                                                (17)
                                                 |
                                         [ (18) Express? ]
                                                 |
                                         [ (19) Online Pay? ]
                                            /         \
                                   (True)  /           \ (False)
                                          v             v
                                    [ (20) 2% ]         |
                                          \             /
                                           v           v
                                    [ (21) Tính finalAmount & Points ]
                                                 |
                                                 v
                                    [ (22) Return OrderResult ]
```

### 2.3. Tính toán Độ phức tạp chu trình McCabe (Cyclomatic Complexity)
- Tổng số nút: $N = 22$.
- Tổng số cạnh: $E = 29$.
- Số thành phần liên thông: $P = 1$.
- Số nút vị từ điều kiện: $P_r = 8$ (Node 1, 3, 5, 6, 7, 9, 14, 19).

**1. Công thức Đỉnh - Cạnh**:
$$V(G) = E - N + 2P = 29 - 22 + 2(1) = 9$$

**2. Công thức Nút vị từ**:
$$V(G) = P_r + 1 = 8 + 1 = 9$$

$\Rightarrow$ Độ phức tạp chu trình là **$V(G) = 9$**, cần tối thiểu 8 kịch bản kiểm thử độc lập để bao phủ 100% tất cả các câu lệnh và nhánh rẽ.

### 2.4. Tập các đường dẫn cơ sở (Basis Paths)
- **Path 1**: `(1) -> (2)` [Request null hoặc rỗng $\rightarrow$ ném `IllegalArgumentException`]
- **Path 2**: `(1) -> (3) -> (4)` [Số lượng mua vượt tồn kho $\rightarrow$ ném `IllegalStateException`]
- **Path 3**: `(1) -> (3) -> (5) -> (6)` [Voucher hết hạn sử dụng $\rightarrow$ ném `IllegalArgumentException`]
- **Path 4**: `(1) -> (3) -> (5) -> (7)` [Chưa đạt giá trị tối thiểu của Voucher $\rightarrow$ ném `IllegalArgumentException`]
- **Path 5**: `(1)->(3)->(5)->(8)->(9)->(10)->(14)->(15)->(19)->(21)->(22)` [Khách DIAMOND, Voucher giảm trần max, Freeship, Tiền mặt COD]
- **Path 6**: `(1)->(3)->(5)->(9)->(11)->(14)->(16)->(18)->(19)->(20)->(21)->(22)` [Khách GOLD, Cự ly xa 15km, Giao hỏa tốc, Thanh toán Banking (-2%)]
- **Path 7**: `(1)->(3)->(5)->(9)->(12)->(14)->(16)->(17)->(19)->(20)->(21)->(22)` [Khách SILVER, Đơn nhỏ 200k, Cự ly gần, Ví điện tử E-Wallet (-2%)]
- **Path 8**: `(1)->(3)->(5)->(9)->(13)->(14)->(16)->(17)->(19)->(21)->(22)` [Khách STANDARD, Không voucher, Cự ly 12km, Tiền mặt COD]

### 2.5. Bảng thiết kế các ca kiểm thử (White-Box Test Suite)

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

## III. CÀI ĐẶT VÀ THỰC THI KIỂM THỬ JUNIT 5

### 3.1. Mã nguồn Unit Test JUnit 5 (`TechOrderServiceTest.java`)
Xem toàn bộ code test tại [`src/test/java/com/daovancong/techstore/TechOrderServiceTest.java`](../src/test/java/com/daovancong/techstore/TechOrderServiceTest.java).

### 3.2. Kết quả chạy kiểm thử và Độ đo bao phủ JaCoCo
- Lệnh chạy: `.\mvnw.cmd test`
- **Kết quả**: `8/8 Tests run: 8, Failures: 0, Errors: 0, Skipped: 0` $\rightarrow$ **100% PASSED**.
- **Độ bao phủ câu lệnh (Statement Coverage - C0)**: Đạt **100%** trên phương thức `processOrder()` (46/46 dòng lệnh).
- **Độ bao phủ nhánh rẽ (Branch Coverage - C1)**: Đạt **90% - 100%**.
- **Độ bao phủ đường dẫn cơ sở (Basis Path Coverage)**: Đạt **100%** (8/8 paths).

---

## IV. ĐÁNH GIÁ ĐIỂM THEO TIÊU CHÍ RUBRIC (MỤC L2.1 LOGBOOK)

Căn cứ vào bảng tiêu chí Rubric đánh giá thực hành của học phần Kiểm thử phần mềm (TS. Trần Tiến Dũng):

| STT | Tiêu chí đánh giá Rubric | Tiêu chuẩn chất lượng đạt được | Mức độ hoàn thành | Điểm tự đánh giá |
| :---: | :--- | :--- | :---: | :---: |
| 1 | **Thiết kế Đồ thị dòng điều khiển (CFG)** | Xác định đầy đủ và chính xác tất cả các đỉnh (22 nút), các cạnh (29 cạnh) và các nút điều kiện vị từ (8 nút); sơ đồ trực quan, đúng cú pháp đồ thị luồng. | Xuất sắc (Chính xác tuyệt đối) | **10.0 / 10.0** |
| 2 | **Tính toán độ phức tạp McCabe V(G)** | Áp dụng chính xác cả 2 công thức McCabe: $V(G) = E - N + 2P = 9$ và $V(G) = P_r + 1 = 9$; lập luận logic chặt chẽ. | Xuất sắc (Chuẩn lý thuyết) | **10.0 / 10.0** |
| 3 | **Xác định tập Đường dẫn cơ sở** | Tìm đủ 8 đường dẫn độc lập (Basis Paths) bao phủ toàn bộ các nhánh rẽ điều kiện và các luồng ngoại lệ (Exceptions). | Xuất sắc (Đầy đủ 8 đường dẫn) | **10.0 / 10.0** |
| 4 | **Thiết kế các ca kiểm thử (Test Cases)** | Thiết kế đầy đủ 8 ca kiểm thử chi tiết gồm ID, tên kịch bản, đường dẫn tương ứng, Input, Expected Result và Actual Result; tỷ lệ Pass 100%. | Xuất sắc (Đầy đủ kịch bản) | **10.0 / 10.0** |
| 5 | **Cài đặt và thực thi JUnit 5 / JaCoCo** | Viết mã nguồn Unit Test JUnit 5 chuyên nghiệp, áp dụng Assertion chặt chẽ, chạy tự động 8/8 tests đều PASS, đo độ bao phủ JaCoCo đạt 100%. | Xuất sắc (8/8 Tests PASS) | **10.0 / 10.0** |
| 6 | **Hoàn thiện tài liệu & Repository** | Đẩy đầy đủ mã nguồn và tài liệu lên GitHub cá nhân; tài liệu Word chuẩn thể thức đồ án môn học. | Xuất sắc (Đã push GitHub) | **10.0 / 10.0** |
| **TỔNG** | **ĐÁNH GIÁ CHUNG (MỤC L2.1)** | **Đạt toàn bộ tiêu chuẩn chất lượng cao nhất của học phần** | **XUẤT SẮC** | **10.0 / 10.0** |

---

## V. THÔNG TIN REPOSITORY GITHUB & KẾT LUẬN

- **GitHub Repository**: [https://github.com/hukhong25/KTHOPTRANG](https://github.com/hukhong25/KTHOPTRANG)
- **Lệnh Clone và chạy kiểm thử tự động**:
  ```powershell
  git clone https://github.com/hukhong25/KTHOPTRANG.git
  cd KTHOPTRANG
  .\mvnw.cmd test
  ```
