package com.daovancong.techstore.repository;

import com.daovancong.techstore.TechOrderService.CustomerTier;
import com.daovancong.techstore.TechOrderService.PaymentMethod;
import com.daovancong.techstore.TechOrderService.Voucher;
import com.daovancong.techstore.model.Customer;
import com.daovancong.techstore.model.Order;
import com.daovancong.techstore.model.Product;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class DataStore {
    private final Map<String, Product> products = new ConcurrentHashMap<>();
    private final Map<String, Voucher> vouchers = new ConcurrentHashMap<>();
    private final Map<String, Customer> customers = new ConcurrentHashMap<>();
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    @PostConstruct
    public void initData() {
        // 1. Kho sản phẩm công nghệ (TechGadget Store)
        addProduct(new Product("PROD-01", "MacBook Pro 14\" M3 Pro (18GB/512GB)", "LAPTOP", 49990000.0, 15, 4.9, 
                "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=500&auto=format&fit=crop&q=60", 
                "Chip M3 Pro cực mạnh, màn hình Liquid Retina XDR 120Hz, pin 18 giờ"));
        addProduct(new Product("PROD-02", "Dell XPS 15 9530 OLED (i7-13700H/RTX 4050)", "LAPTOP", 42500000.0, 8, 4.8, 
                "https://images.unsplash.com/photo-1593642632823-8f785ba67e45?w=500&auto=format&fit=crop&q=60", 
                "Thiết kế viền mỏng vô cực, màn hình OLED 3.5K cảm ứng đỉnh cao"));
        addProduct(new Product("PROD-03", "ASUS ROG Zephyrus G14 Gaming (Ryzen 9/RTX 4060)", "LAPTOP", 38990000.0, 12, 4.9, 
                "https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=500&auto=format&fit=crop&q=60", 
                "Laptop gaming siêu mỏng nhẹ, màn hình ROG Nebula 165Hz"));
        addProduct(new Product("PROD-04", "iPhone 16 Pro Max 256GB Titan Tự Nhiên", "SMARTPHONE", 34490000.0, 25, 5.0, 
                "https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=500&auto=format&fit=crop&q=60", 
                "Chip A18 Pro, Nút Điều Khiển Camera mới, Camera 48MP Zoom 5x"));
        addProduct(new Product("PROD-05", "Samsung Galaxy S24 Ultra 512GB AI Phone", "SMARTPHONE", 29990000.0, 18, 4.8, 
                "https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=500&auto=format&fit=crop&q=60", 
                "Khung Titan, Galaxy AI thông minh, bút S-Pen tích hợp"));
        addProduct(new Product("PROD-06", "iPad Pro M4 11\" Ultra Retina Tandem OLED", "SMARTPHONE", 27890000.0, 10, 4.9, 
                "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=500&auto=format&fit=crop&q=60", 
                "Độ mỏng kỷ lục 5.3mm, chip M4 siêu tốc độ kết hợp Apple Pencil Pro"));
        addProduct(new Product("PROD-07", "Màn hình Gaming LG UltraGear 27\" 2K 180Hz", "DISPLAY", 7890000.0, 14, 4.7, 
                "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=500&auto=format&fit=crop&q=60", 
                "Tấm nền Nano IPS 1ms G-Sync Compatible, hiển thị 98% DCI-P3"));
        addProduct(new Product("PROD-08", "Tai nghe Sony WH-1000XM5 Chống Ồn Đỉnh Cao", "AUDIO", 7290000.0, 20, 4.9, 
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&auto=format&fit=crop&q=60", 
                "Chống ồn chủ động kép với 8 micro, chất âm Hi-Res Audio Wireless"));
        addProduct(new Product("PROD-09", "Tai nghe Apple AirPods Pro 2 (USB-C MagSafe)", "AUDIO", 5490000.0, 30, 4.9, 
                "https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?w=500&auto=format&fit=crop&q=60", 
                "Chip H2 chống ồn gấp 2 lần, Adaptive Audio và cổng sạc USB-C"));
        addProduct(new Product("PROD-10", "Bàn phím cơ không dây Keychron K2 Pro QMK/VIA", "ACCESSORY", 2390000.0, 22, 4.8, 
                "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500&auto=format&fit=crop&q=60", 
                "Switch cơ hotswap, Bluetooth 5.1 & Type-C, tương thích Mac/Windows"));
        addProduct(new Product("PROD-11", "Chuột Logitech MX Master 3S Công Thái Học", "ACCESSORY", 2190000.0, 35, 4.9, 
                "https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=500&auto=format&fit=crop&q=60", 
                "Cảm biến 8000 DPI lướt trên mọi bề mặt, cuộn MagSpeed siêu êm"));
        addProduct(new Product("PROD-12", "Pin sạc dự phòng Anker MagGo Qi2 10.000mAh", "ACCESSORY", 1250000.0, 40, 4.7, 
                "https://images.unsplash.com/photo-1609091839311-d5365f9ff1c5?w=500&auto=format&fit=crop&q=60", 
                "Chuẩn sạc từ tính Qi2 không dây 15W, màn hình thông minh hiển thị pin"));

        // 2. Danh mục Mã giảm giá Voucher
        addVoucher(new Voucher("TECH50", 10.0, 500000.0, 2000000.0, LocalDate.now().plusDays(30)));
        addVoucher(new Voucher("WELCOME", 15.0, 200000.0, 500000.0, LocalDate.now().plusDays(15)));
        addVoucher(new Voucher("GAMINGVIP", 5.0, 1000000.0, 5000000.0, LocalDate.now().plusDays(60)));
        addVoucher(new Voucher("EXPIRED_DEMO", 20.0, 300000.0, 100000.0, LocalDate.now().minusDays(10)));

        // 3. Khách hàng mẫu theo từng phân hạng thành viên
        addCustomer(new Customer("CUST-01", "Đào Văn Công (2023601884)", "0987654321", "cong.daovan@haui.edu.vn", 
                "Nhà E4, KTX ĐH Công nghiệp Hà Nội, Bắc Từ Liêm, HN", CustomerTier.DIAMOND, 1850));
        addCustomer(new Customer("CUST-02", "Trần Minh Hiếu", "0912345678", "hieu.tm@gmail.com", 
                "Số 12 Cầu Giấy, Hà Nội", CustomerTier.GOLD, 920));
        addCustomer(new Customer("CUST-03", "Nguyễn Hoàng Long", "0909090909", "long.nh@gmail.com", 
                "Số 45 Lê Duẩn, Hà Nội", CustomerTier.SILVER, 340));
        addCustomer(new Customer("CUST-04", "Khách Hàng Vãng Lai", "0933333333", "guest@techstore.vn", 
                "Hà Nội", CustomerTier.STANDARD, 50));

        // 4. Khởi tạo đơn hàng mẫu ban đầu
        Order initialOrder = new Order();
        initialOrder.setId("ORD-DEMO-001");
        initialOrder.setOrderId("TECH-ORD-1710000001");
        initialOrder.setCustomerId("CUST-01");
        initialOrder.setCustomerName("Đào Văn Công (2023601884)");
        initialOrder.setCustomerPhone("0987654321");
        initialOrder.setShippingAddress("ĐH Công nghiệp Hà Nội, Bắc Từ Liêm, HN");
        initialOrder.setCustomerTier(CustomerTier.DIAMOND);
        initialOrder.setDistanceKm(5.0);
        initialOrder.setExpressDelivery(false);
        initialOrder.setPaymentMethod(PaymentMethod.ONLINE_BANKING);
        initialOrder.setVoucherCode("TECH50");
        initialOrder.setItems(List.of(
                new Order.OrderItemRecord("PROD-08", "Tai nghe Sony WH-1000XM5 Chống Ồn Đỉnh Cao", 7290000.0, 1),
                new Order.OrderItemRecord("PROD-10", "Bàn phím cơ không dây Keychron K2 Pro QMK/VIA", 2390000.0, 1)
        ));
        initialOrder.setSubTotal(9680000.0);
        initialOrder.setVoucherDiscount(500000.0);
        initialOrder.setMemberDiscount(484000.0);
        initialOrder.setShippingFee(0.0); // Freeship vì >= 500k & <= 10km
        initialOrder.setPaymentDiscount(173920.0); // Giảm 2% thanh toán Banking
        initialOrder.setFinalAmount(8522080.0);
        initialOrder.setLoyaltyPoints(852);
        initialOrder.setStatus("DELIVERED");
        orders.put(initialOrder.getOrderId(), initialOrder);
    }

    // Product operations
    public void addProduct(Product p) { products.put(p.getId(), p); }
    public Product getProduct(String id) { return products.get(id); }
    public List<Product> getAllProducts() { return new ArrayList<>(products.values()); }
    public void removeProduct(String id) { products.remove(id); }

    // Voucher operations
    public void addVoucher(Voucher v) { vouchers.put(v.getCode(), v); }
    public Voucher getVoucher(String code) { return vouchers.get(code); }
    public List<Voucher> getAllVouchers() { return new ArrayList<>(vouchers.values()); }

    // Customer operations
    public void addCustomer(Customer c) { customers.put(c.getId(), c); }
    public Customer getCustomer(String id) { return customers.get(id); }
    public List<Customer> getAllCustomers() { return new ArrayList<>(customers.values()); }

    // Order operations
    public void saveOrder(Order o) { orders.put(o.getOrderId(), o); }
    public Order getOrder(String orderId) { return orders.get(orderId); }
    public List<Order> getAllOrders() { 
        List<Order> list = new ArrayList<>(orders.values());
        list.sort((a, b) -> b.getOrderId().compareTo(a.getOrderId()));
        return list;
    }
}
