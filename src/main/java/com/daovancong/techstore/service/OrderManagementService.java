package com.daovancong.techstore.service;

import com.daovancong.techstore.TechOrderService;
import com.daovancong.techstore.TechOrderService.*;
import com.daovancong.techstore.dto.CheckoutRequest;
import com.daovancong.techstore.model.Customer;
import com.daovancong.techstore.model.Order;
import com.daovancong.techstore.model.Order.OrderItemRecord;
import com.daovancong.techstore.model.Product;
import com.daovancong.techstore.repository.DataStore;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderManagementService {
    private final DataStore dataStore;
    private final TechOrderService techOrderService;

    public OrderManagementService(DataStore dataStore, TechOrderService techOrderService) {
        this.dataStore = dataStore;
        this.techOrderService = techOrderService;
    }

    public synchronized Order checkout(CheckoutRequest req) {
        if (req == null || req.getItems() == null || req.getItems().isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng rỗng hoặc thông tin đơn hàng không hợp lệ");
        }

        // 1. Kiểm tra và chuẩn bị danh sách OrderItem cho TechOrderService
        List<OrderItem> serviceItems = new ArrayList<>();
        List<OrderItemRecord> orderItemRecords = new ArrayList<>();

        for (CheckoutRequest.CartItemDto cartItem : req.getItems()) {
            Product prod = dataStore.getProduct(cartItem.getProductId());
            if (prod == null) {
                throw new IllegalArgumentException("Sản phẩm không tồn tại: " + cartItem.getProductId());
            }
            if (cartItem.getQuantity() <= 0) {
                throw new IllegalStateException("Số lượng sản phẩm " + prod.getName() + " phải lớn hơn 0");
            }
            if (cartItem.getQuantity() > prod.getStock()) {
                throw new IllegalStateException("Sản phẩm " + prod.getName() + " không đủ tồn kho (Còn: " + prod.getStock() + ")");
            }

            serviceItems.add(new OrderItem(prod.getId(), prod.getName(), prod.getPrice(), cartItem.getQuantity(), prod.getStock()));
            orderItemRecords.add(new OrderItemRecord(prod.getId(), prod.getName(), prod.getPrice(), cartItem.getQuantity()));
        }

        // 2. Tìm voucher nếu có
        Voucher voucher = null;
        if (req.getVoucherCode() != null && !req.getVoucherCode().trim().isEmpty()) {
            voucher = dataStore.getVoucher(req.getVoucherCode().trim());
            if (voucher == null) {
                throw new IllegalArgumentException("Mã giảm giá không hợp lệ: " + req.getVoucherCode());
            }
        }

        // 3. Xác định hạng thành viên
        CustomerTier tier = req.getCustomerTier();
        Customer customer = null;
        if (req.getCustomerId() != null) {
            customer = dataStore.getCustomer(req.getCustomerId());
            if (customer != null && tier == null) {
                tier = customer.getTier();
            }
        }
        if (tier == null) tier = CustomerTier.STANDARD;

        // 4. Gọi lõi xử lý TechOrderService (đáp ứng đúng bài tập kiểm thử hộp trắng L2.1)
        OrderRequest orderReq = new OrderRequest(
                req.getCustomerId(),
                req.getCustomerPhone(),
                tier,
                serviceItems,
                voucher,
                req.getDistanceKm(),
                req.isExpressDelivery(),
                req.getPaymentMethod() != null ? req.getPaymentMethod() : PaymentMethod.COD
        );

        OrderResult result = techOrderService.processOrder(orderReq);

        // 5. Trừ số lượng tồn kho của các sản phẩm đã mua
        for (CheckoutRequest.CartItemDto item : req.getItems()) {
            Product prod = dataStore.getProduct(item.getProductId());
            prod.setStock(prod.getStock() - item.getQuantity());
        }

        // 6. Cộng điểm thưởng cho khách hàng nếu có
        if (customer != null) {
            customer.addPoints(result.getLoyaltyPoints());
        }

        // 7. Lưu đơn hàng vào DataStore
        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setOrderId(result.getOrderId());
        order.setCustomerId(req.getCustomerId());
        order.setCustomerName(req.getCustomerName() != null ? req.getCustomerName() : (customer != null ? customer.getName() : "Khách Vãng Lai"));
        order.setCustomerPhone(req.getCustomerPhone());
        order.setShippingAddress(req.getShippingAddress());
        order.setCustomerTier(tier);
        order.setDistanceKm(req.getDistanceKm());
        order.setExpressDelivery(req.isExpressDelivery());
        order.setPaymentMethod(orderReq.getPaymentMethod());
        order.setVoucherCode(voucher != null ? voucher.getCode() : null);
        order.setItems(orderItemRecords);
        order.setSubTotal(result.getSubTotal());
        order.setVoucherDiscount(result.getVoucherDiscount());
        order.setMemberDiscount(result.getMemberDiscount());
        order.setShippingFee(result.getShippingFee());
        order.setPaymentDiscount(result.getPaymentDiscount());
        order.setFinalAmount(result.getFinalAmount());
        order.setLoyaltyPoints(result.getLoyaltyPoints());
        order.setStatus("CONFIRMED");

        dataStore.saveOrder(order);
        return order;
    }

    public List<Order> getOrders(String customerId) {
        return dataStore.getAllOrders().stream()
                .filter(o -> customerId == null || customerId.isEmpty() || customerId.equals(o.getCustomerId()))
                .collect(Collectors.toList());
    }

    public Order getOrder(String orderId) {
        Order o = dataStore.getOrder(orderId);
        if (o == null) {
            throw new IllegalArgumentException("Không tìm thấy đơn hàng: " + orderId);
        }
        return o;
    }

    public Order updateStatus(String orderId, String newStatus) {
        Order o = getOrder(orderId);
        o.setStatus(newStatus.toUpperCase());
        return o;
    }

    public synchronized Order cancelOrder(String orderId) {
        Order o = getOrder(orderId);
        if ("CANCELLED".equalsIgnoreCase(o.getStatus())) {
            throw new IllegalStateException("Đơn hàng đã được hủy trước đó");
        }
        if ("DELIVERED".equalsIgnoreCase(o.getStatus())) {
            throw new IllegalStateException("Đơn hàng đã giao thành công, không thể hủy");
        }

        // Phục hồi lại số lượng tồn kho
        for (OrderItemRecord item : o.getItems()) {
            Product p = dataStore.getProduct(item.getProductId());
            if (p != null) {
                p.setStock(p.getStock() + item.getQuantity());
            }
        }

        o.setStatus("CANCELLED");
        return o;
    }

    public Map<String, Object> getAdminStats() {
        List<Order> orders = dataStore.getAllOrders();
        double totalRevenue = orders.stream()
                .filter(o -> !"CANCELLED".equalsIgnoreCase(o.getStatus()))
                .mapToDouble(Order::getFinalAmount)
                .sum();

        long outOfStock = dataStore.getAllProducts().stream()
                .filter(p -> p.getStock() <= 0)
                .count();

        Map<String, Long> statusCounts = orders.stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalOrders", orders.size());
        stats.put("totalProducts", dataStore.getAllProducts().size());
        stats.put("outOfStockCount", outOfStock);
        stats.put("statusCounts", statusCounts);
        return stats;
    }
}
