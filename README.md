# TECHSTORE - HỆ THỐNG THƯƠNG MẠI ĐIỆN TỬ THIẾT BỊ CÔNG NGHỆ

- **Developer**: **Đào Văn Công**
- **Mã sinh viên**: **2023601884**
- **Lớp**: IT6013.2 - K18
- **Khoa/Trường**: Trường Công nghệ Thông tin & Truyền thông - Đại học Công nghiệp Hà Nội (HaUI)
- **Công nghệ**: Java 17/21, Spring Boot 3.2.5, Spring MVC REST API, HTML5/TailwindCSS (SPA), JUnit 5.

---

## 1. GIỚI THIỆU DỰ ÁN

**TechStore** là ứng dụng web bán lẻ thiết bị công nghệ (Laptop, Điện thoại, Phụ kiện, Âm thanh, Màn hình) được thiết kế theo kiến trúc phân tầng (Layered Architecture) hiện đại. Hệ thống cung cấp đầy đủ các quy trình nghiệp vụ mua sắm từ chọn sản phẩm, áp dụng voucher, tính chiết khấu thành viên, tính phí vận chuyển theo khoảng cách đến tạo đơn và quản lý kho hàng.

---

## 2. KIẾN TRÚC MÃ NGUỒN (PROJECT ARCHITECTURE)

```text
com.daovancong.techstore
├── TechStoreApplication.java     # Entry point khởi chạy Spring Boot
├── TechOrderService.java         # Dịch vụ nghiệp vụ cốt lõi (Tính toán thanh toán, voucher, phí ship)
├── model/                        # Thực thể dữ liệu
│   ├── Product.java              # Thông tin sản phẩm & tồn kho
│   ├── Customer.java             # Khách hàng & hạng thành viên VIP
│   └── Order.java                # Thông tin hóa đơn đơn hàng
├── dto/                          # Đối tượng truyền dữ liệu qua REST API
│   ├── ApiResponse.java          # Wrapper phản hồi chuẩn { success, message, data }
│   └── CheckoutRequest.java      # Dữ liệu khách gửi lên khi đặt hàng
├── repository/
│   └── DataStore.java            # Kho lưu trữ dữ liệu in-memory thread-safe
├── service/
│   ├── ProductService.java       # Quản lý danh mục & kho hàng
│   └── OrderManagementService.java # Điều phối giỏ hàng, đặt hàng & hoàn trả kho khi hủy đơn
└── controller/                   # REST API Controllers
    ├── ProductController.java    # /api/products
    ├── OrderController.java      # /api/orders
    ├── VoucherController.java    # /api/vouchers
    └── CustomerController.java   # /api/customers
```

---

## 3. TÀI LIỆU REST API (DÀNH CHO TESTER)

| Phương thức | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `GET` | `/api/products` | Lấy danh sách sản phẩm (hỗ trợ query `?category=`, `?search=`, `?sort=`) |
| `GET` | `/api/products/{id}` | Lấy chi tiết sản phẩm theo mã |
| `POST` | `/api/products` | Thêm sản phẩm mới vào kho |
| `PUT` | `/api/products/{id}` | Cập nhật thông tin / tồn kho sản phẩm |
| `DELETE`| `/api/products/{id}` | Xóa sản phẩm khỏi kho |
| `GET` | `/api/vouchers` | Lấy danh sách mã giảm giá còn hiệu lực |
| `POST` | `/api/vouchers/validate` | Kiểm tra điều kiện áp dụng voucher cho đơn hàng |
| `GET` | `/api/customers` | Lấy danh sách thông tin khách hàng mẫu |
| `POST` | `/api/orders/checkout` | Tạo đơn hàng mới (Gọi qua lõi `TechOrderService.processOrder`) |
| `GET` | `/api/orders` | Lấy danh sách lịch sử đơn hàng |
| `GET` | `/api/orders/{id}` | Xem chi tiết hóa đơn đơn hàng |
| `POST` | `/api/orders/{id}/cancel` | Hủy đơn hàng và tự động hoàn trả số lượng vào kho |
| `GET` | `/api/orders/admin/stats` | Thống kê doanh thu, số đơn và tình trạng kho hàng |

---

## 4. QUY TẮC NGHIỆP VỤ CỐT LÕI (`TechOrderService.processOrder`)

1. **Validation**: Kiểm tra request, danh sách sản phẩm không rỗng, số điện thoại hợp lệ, cự ly $\ge 0$.
2. **Kiểm tra tồn kho**: Số lượng mua mỗi món phải $> 0$ và $\le$ tồn kho hiện có (`item.quantity <= item.stock`).
3. **Áp dụng Voucher**:
   - Voucher không được hết hạn (`expiryDate >= today`).
   - Tổng tiền hàng phải $\ge$ giá trị tối thiểu (`minOrderValue`).
   - Mức giảm = $\min(\text{subtotal} \times \text{discountPercent}, \text{maxDiscount})$.
4. **Chiết khấu thành viên VIP**:
   - `DIAMOND`: Giảm 5% subtotal.
   - `GOLD`: Giảm 3% subtotal.
   - `SILVER`: Giảm 1% subtotal.
   - `STANDARD`: Giảm 0%.
5. **Tính phí giao hàng (Tiered Shipping)**:
   - Miễn phí vận chuyển (0đ) nếu: Tiền hàng sau giảm $\ge 500.000đ$ và cự ly giao $\le 10km$.
   - Cước tiêu chuẩn: 30.000đ. Nếu cự ly $> 10km$, cộng thêm 5.000đ cho mỗi km vượt quá.
   - Giao hàng hỏa tốc trong 2h: Cộng thêm 25.000đ.
6. **Chiết khấu thanh toán trực tuyến**: Giảm thêm 2% khi khách hàng chọn thanh toán chuyển khoản ngân hàng (`ONLINE_BANKING`) hoặc ví điện tử (`E_WALLET`).
7. **Tích điểm thưởng**: 10.000đ thanh toán = 1 điểm thưởng (`loyaltyPoints`).

---

## 5. HƯỚNG DẪN KHỞI CHẠY ỨNG DỤNG

### 5.1. Khởi chạy Web Server:
```powershell
.\mvnw.cmd spring-boot:run
```
Mở trình duyệt truy cập: 👉 **`http://localhost:8088`**

### 5.2. Chạy Unit Test:
```powershell
.\mvnw.cmd test
```
