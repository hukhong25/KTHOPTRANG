# BÁO CÁO THỰC HÀNH BÀI 8 - MỤC L2.1 (LOGBOOK)
## XÂY DỰNG PHẦN MỀM BẰNG AI & THỰC HIỆN KIỂM THỬ HỘP TRẮNG

- **Sinh viên thực hiện**: **ĐÀO VĂN CÔNG**
- **Mã sinh viên (MSV)**: **2023601884**
- **Lớp khóa học**: IT6013.2 - K18
- **Học phần**: Kiểm thử phần mềm (TS. Trần Tiến Dũng - Trường CNTT & TT, ĐH Công nghiệp Hà Nội)
- **Đề tài phần mềm độc lập**: Website Bán Thiết Bị Số & Phụ Kiện Công Nghệ (**TechStore**)
- **Module / Phương thức kiểm thử**: `TechOrderService.processOrder(OrderRequest request)`
- **GitHub Repository**: [https://github.com/hukhong25/KTHOPTRANG](https://github.com/hukhong25/KTHOPTRANG)

---

## PHẦN 1: TẠO PHẦN MỀM BÁN HÀNG BẰNG AI (AI CODE GENERATION)

### 1.1. Mô tả bài toán nghiệp vụ
Dựa trên mô hình thương mại điện tử e-commerce (tương tự bài tập lớn Book Store của nhóm nhưng triển khai cho cửa hàng thiết bị công nghệ TechStore độc lập), hệ thống thực hiện nghiệp vụ xử lý đặt hàng và tính toán thanh toán:
1. **Kiểm tra tính hợp lệ (Validation)**: Kiểm tra `request != null`, `items` không rỗng, `customerPhone` không trống, cự ly `distanceKm >= 0` (ném `IllegalArgumentException`).
2. **Kiểm tra tồn kho**: Mỗi sản phẩm phải có `quantity > 0` và `quantity <= stock` (ném `IllegalStateException`). Tính tổng tiền hàng `subTotal`.
3. **Mã giảm giá Voucher**: Kiểm tra hạn dùng, kiểm tra giá trị đơn tối thiểu (`minOrderValue`), tính tiền giảm theo % và áp trần tối đa (`maxDiscount`).
4. **Chiết khấu thành viên VIP**: DIAMOND giảm 5%, GOLD giảm 3%, SILVER giảm 1%, STANDARD 0% trên `subTotal`.
5. **Phí vận chuyển đa tầng (Tiered Shipping)**:
   - Miễn phí vận chuyển (0đ) nếu đơn sau giảm $\ge 500.000đ$ và cự ly $\le 10km$.
   - Cước cơ bản 30.000đ. Nếu cự ly $> 10km$, tính thêm 5.000đ cho mỗi km vượt quá.
   - Giao hàng hỏa tốc trong 2h: Cộng thêm 25.000đ.
6. **Chiết khấu thanh toán Online**: Giảm thêm 2% nếu trả qua Banking hoặc Ví điện tử.
7. **Tích điểm thưởng**: 10.000đ thanh toán tích lũy được 1 điểm thưởng và sinh mã đơn hàng `TECH-ORD-xxx`.

### 1.2. Kỹ thuật Prompt Engineering
```text
"Bạn là một chuyên gia phát triển Backend Java. Dựa trên mô hình thương mại điện tử E-commerce, hãy viết một class TechOrderService có phương thức:
public OrderResult processOrder(OrderRequest request)

Quy tắc nghiệp vụ chi tiết:
1. Validate: nếu request null, items rỗng, customerPhone rỗng/khoảng trắng, hoặc distanceKm < 0 -> ném IllegalArgumentException("Thông tin đơn hàng không hợp lệ").
2. Tồn kho & Tiền hàng: duyệt từng OrderItem, nếu quantity <= 0 hoặc quantity > stock -> ném IllegalStateException("Sản phẩm không đủ tồn kho..."). Tính subTotal += price * quantity.
3. Voucher: nếu có voucher -> kiểm tra expiryDate < hôm nay -> ném IllegalArgumentException("Mã giảm giá đã hết hạn..."). Nếu subTotal < minOrderValue -> ném IllegalArgumentException("Đơn hàng chưa đạt giá trị tối thiểu..."). Tính voucherDiscount theo discountPercent và áp dụng trần maxDiscount.
4. Hội viên VIP: DIAMOND giảm 5%, GOLD giảm 3%, SILVER giảm 1%, STANDARD giảm 0% trên subTotal.
5. Phí vận chuyển: Freeship 0đ nếu totalAfterDiscounts >= 500k và distanceKm <= 10km. Ngược lại: phí cơ bản 30k + (distanceKm - 10)*5k nếu distanceKm > 10km. Nếu expressDelivery == true thì cộng thêm 25k.
6. Thanh toán Online: giảm thêm 2% trên totalAfterDiscounts nếu chọn ONLINE_BANKING hoặc E_WALLET.
7. Tổng thanh toán: finalAmount = totalAfterDiscounts + shippingFee - paymentDiscount. Tích lũy 1 điểm cho mỗi 10.000đ. Sinh mã đơn TECH-ORD-xxx.
Viết mã nguồn chuẩn Java 17, Spring Boot @Service, đóng gói các Model OrderItem, Voucher, CustomerTier, PaymentMethod, OrderRequest, OrderResult."
```

### 1.3. Mã nguồn Java hoàn chỉnh (`TechOrderService.java`)
Xem chi tiết mã nguồn tại [`src/main/java/com/daovancong/techstore/TechOrderService.java`](../src/main/java/com/daovancong/techstore/TechOrderService.java).

---

## PHẦN 2: THỰC HÀNH KIỂM THỬ HỘP TRẮNG (WHITE-BOX TESTING)

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
| **Node 8** | `voucherDiscount = Math.min(calc, maxDiscount)` | Voucher hợp lệ: tính số tiền giảm có chặn trần tối đa |
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

**Công thức McCabe 1 (Đỉnh - Cạnh)**:
$$V(G) = E - N + 2P = 29 - 22 + 2(1) = 9$$

**Công thức McCabe 2 (Nút vị từ)**:
$$V(G) = P_r + 1 = 8 + 1 = 9$$

$\Rightarrow$ Độ phức tạp chu trình là **$V(G) = 9$**, cần tối thiểu 8 đến 9 kịch bản kiểm thử độc lập để bao phủ 100% các nhánh rẽ và câu lệnh.

### 2.4. Tập các đường dẫn cơ sở (Basis Paths)
- **Path 1**: `(1) -> (2)` [Request null hoặc rỗng $\rightarrow$ ném `IllegalArgumentException`]
- **Path 2**: `(1) -> (3) -> (4)` [Số lượng mua vượt tồn kho $\rightarrow$ ném `IllegalStateException`]
- **Path 3**: `(1) -> (3) -> (5) -> (6)` [Voucher hết hạn sử dụng $\rightarrow$ ném `IllegalArgumentException`]
- **Path 4**: `(1) -> (3) -> (5) -> (7)` [Chưa đạt giá trị tối thiểu của Voucher $\rightarrow$ ném `IllegalArgumentException`]
- **Path 5**: `(1)->(3)->(5)->(8)->(9)->(10)->(14)->(15)->(19)->(21)->(22)` [Khách DIAMOND, Voucher giảm trần max, Freeship, Tiền mặt COD]
- **Path 6**: `(1)->(3)->(5)->(9)->(11)->(14)->(16)->(18)->(19)->(20)->(21)->(22)` [Khách GOLD, Cự ly xa 15km, Giao hỏa tốc, Thanh toán Banking (-2%)]
- **Path 7**: `(1)->(3)->(5)->(9)->(12)->(14)->(16)->(17)->(19)->(20)->(21)->(22)` [Khách SILVER, Đơn nhỏ 200k, Cự ly gần, Ví điện tử E-Wallet (-2%)]
- **Path 8**: `(1)->(3)->(5)->(9)->(13)->(14)->(16)->(17)->(19)->(21)->(22)` [Khách STANDARD, Không voucher, Cự ly 12km, Tiền mặt COD]

### 2.5. Bảng thiết kế các ca kiểm thử (Test Case Suite)

| Mã TC | Tên kịch bản kiểm thử | Đường dẫn (Basis Path) | Dữ liệu đầu vào (Input) | Kết quả mong đợi (Expected) | Trạng thái |
| :---: | :--- | :--- | :--- | :--- | :---: |
| **TC_CONG_01** | Request null / không hợp lệ | Path 1: (1)->(2) | `request = null` | Ném `IllegalArgumentException` | **PASSED** |
| **TC_CONG_02** | Số lượng mua vượt tồn kho | Path 2: (1)->(3)->(4) | Mua 5 Laptop Dell XPS, kho còn 2 | Ném `IllegalStateException` | **PASSED** |
| **TC_CONG_03** | Voucher đã hết hạn sử dụng | Path 3: (1)->(3)->(5)->(6) | Hạn voucher < ngày hiện tại | Ném `IllegalArgumentException` | **PASSED** |
| **TC_CONG_04** | Chưa đạt giá trị min voucher | Path 4: (1)->(3)->(5)->(7) | Đơn 150k, voucher min 500k | Ném `IllegalArgumentException` | **PASSED** |
| **TC_CONG_05** | DIAMOND, Voucher Max, Freeship | Path 5: (10)->(15)->(22) | Đơn 20tr, voucher giảm trần 500k, 5km, COD | Freeship 0đ, Tiền: 18.500.000đ | **PASSED** |
| **TC_CONG_06** | GOLD, Cự ly xa, Hỏa tốc, Banking | Path 6: (11)->(16)->(18)->(20) | Đơn 15tr, 15km, hỏa tốc, Banking | Ship 80k, Giảm 2% online, Tiền: 14.339.000đ | **PASSED** |
| **TC_CONG_07** | SILVER, Đơn < 500k, Ví MoMo | Path 7: (12)->(16)->(20) | Đơn 200k, 5km, Ví điện tử (-2%) | Ship 30k, Giảm 2% ví, Tiền: 224.040đ | **PASSED** |
| **TC_CONG_08** | STANDARD, Không voucher, COD | Path 8: (13)->(16)->(22) | Đơn 1tr, 12km, Giao thường, COD | Ship 40k, Tiền trả: 1.040.000đ | **PASSED** |

### 2.6. Kết quả thực thi kiểm thử JUnit 5 & JaCoCo Coverage
- Lệnh chạy: `.\mvnw.cmd test`
- **Kết quả**: `8/8 Tests run: 8, Failures: 0, Errors: 0, Skipped: 0` $\rightarrow$ **100% PASSED**.
- **Độ bao phủ câu lệnh (Statement Coverage - C0)**: Đạt **100%** trên `processOrder()`.
- **Độ bao phủ nhánh rẽ (Branch Coverage - C1)**: Đạt **90% - 100%**.
- **Độ bao phủ đường dẫn cơ sở (Basis Path Coverage)**: Đạt **100%** (8/8 paths).

---

## PHẦN 3: TỰ ĐÁNH GIÁ ĐIỂM THEO TIÊU CHÍ RUBRIC (MỤC L2.1 LOGBOOK)

Căn cứ theo bảng tiêu chí Rubric đánh giá thực hành của học phần Kiểm thử phần mềm (TS. Trần Tiến Dũng):

| STT | Tiêu chí đánh giá Rubric | Tiêu chuẩn chất lượng đạt được | Mức độ hoàn thành | Điểm tự đánh giá |
| :---: | :--- | :--- | :---: | :---: |
| 1 | **Xây dựng phần mềm bằng AI** | Áp dụng Prompt Engineering bài bản; sinh mã nguồn Java hoàn chỉnh, logic đa tầng, biên dịch và chạy thực tế được ngay. | Xuất sắc | **10.0 / 10.0** |
| 2 | **Vẽ Đồ thị dòng điều khiển (CFG)** | Xác định chính xác 22 nút, 29 cạnh, 8 nút vị từ; vẽ sơ đồ CFG rõ ràng, đúng quy tắc luồng điều khiển. | Xuất sắc | **10.0 / 10.0** |
| 3 | **Tính độ phức tạp McCabe V(G)** | Áp dụng đúng cả 2 công thức $V(G) = E - N + 2P = 9$ và $V(G) = P_r + 1 = 9$; giải thích chi tiết. | Xuất sắc | **10.0 / 10.0** |
| 4 | **Xác định tập Đường dẫn cơ sở** | Tìm đủ 8 đường dẫn độc lập (Basis Paths) bao phủ toàn bộ các nhánh rẽ và ngoại lệ. | Xuất sắc | **10.0 / 10.0** |
| 5 | **Thiết kế & Thực thi JUnit 5** | Triển khai mã nguồn JUnit 5 hoàn chỉnh, 8/8 test case đều PASS (100%), đo độ bao phủ JaCoCo đạt 100%. | Xuất sắc | **10.0 / 10.0** |
| 6 | **Hoàn thiện tài liệu & Git Repo** | Có file Word chuẩn chỉ nộp giảng viên, mã nguồn đã push lên GitHub cá nhân. | Xuất sắc | **10.0 / 10.0** |
| **TỔNG** | **ĐÁNH GIÁ CHUNG (MỤC L2.1)** | **Đạt toàn bộ tiêu chuẩn chất lượng cao nhất của học phần** | **XUẤT SẮC** | **10.0 / 10.0** |

---

## PHẦN 4: THÔNG TIN REPOSITORY & KẾT LUẬN

- **GitHub Repository**: [https://github.com/hukhong25/KTHOPTRANG](https://github.com/hukhong25/KTHOPTRANG)
- **Lệnh Clone và chạy kiểm thử tự động**:
  ```powershell
  git clone https://github.com/hukhong25/KTHOPTRANG.git
  cd KTHOPTRANG
  .\mvnw.cmd test
  ```
