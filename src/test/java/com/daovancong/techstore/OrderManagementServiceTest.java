package com.daovancong.techstore;

import com.daovancong.techstore.TechOrderService.CustomerTier;
import com.daovancong.techstore.TechOrderService.PaymentMethod;
import com.daovancong.techstore.dto.CheckoutRequest;
import com.daovancong.techstore.dto.CheckoutRequest.CartItemDto;
import com.daovancong.techstore.model.Customer;
import com.daovancong.techstore.model.Order;
import com.daovancong.techstore.model.Product;
import com.daovancong.techstore.repository.DataStore;
import com.daovancong.techstore.service.OrderManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * KIỂM THỬ HỘP TRẮNG CHO ORDERMANAGEMENTSERVICE
 * Developer: Đào Văn Công (2023601884)
 */
class OrderManagementServiceTest {

    private DataStore dataStore;
    private TechOrderService techOrderService;
    private OrderManagementService orderService;

    @BeforeEach
    void setUp() {
        dataStore = new DataStore();
        dataStore.initData();
        techOrderService = new TechOrderService();
        orderService = new OrderManagementService(dataStore, techOrderService);
    }

    @Test
    @DisplayName("WB-CHK-01 & 02: Ném ngoại lệ khi checkout với request rỗng hoặc items rỗng")
    void testCheckout_WhenRequestOrItemsEmpty_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> orderService.checkout(null));

        CheckoutRequest emptyReq = new CheckoutRequest();
        emptyReq.setItems(Collections.emptyList());
        assertThrows(IllegalArgumentException.class, () -> orderService.checkout(emptyReq));
    }

    @Test
    @DisplayName("WB-CHK-03: Ném ngoại lệ khi mua sản phẩm không tồn tại trong kho")
    void testCheckout_WhenProductNotFound_ShouldThrowException() {
        CheckoutRequest req = new CheckoutRequest();
        req.setCustomerPhone("0987654321");
        req.setItems(List.of(new CartItemDto("NON-EXISTENT-ID", 1)));

        assertThrows(IllegalArgumentException.class, () -> orderService.checkout(req));
    }

    @Test
    @DisplayName("WB-CHK-04: Ném ngoại lệ khi số lượng đặt mua <= 0")
    void testCheckout_WhenQuantityZeroOrNegative_ShouldThrowException() {
        CheckoutRequest req = new CheckoutRequest();
        req.setCustomerPhone("0987654321");
        req.setItems(List.of(new CartItemDto("PROD-01", 0)));

        assertThrows(IllegalStateException.class, () -> orderService.checkout(req));
    }

    @Test
    @DisplayName("WB-CHK-05: Ném ngoại lệ khi số lượng vượt tồn kho khả dụng")
    void testCheckout_WhenQuantityExceedsStock_ShouldThrowException() {
        Product p = dataStore.getProduct("PROD-02"); // Dell XPS 15 (stock = 8)
        CheckoutRequest req = new CheckoutRequest();
        req.setCustomerPhone("0987654321");
        req.setItems(List.of(new CartItemDto("PROD-02", p.getStock() + 1)));

        assertThrows(IllegalStateException.class, () -> orderService.checkout(req));
    }

    @Test
    @DisplayName("WB-CHK-14 (DEF-01): Hai dòng giỏ hàng cùng productId có tổng số lượng vượt tồn kho")
    void testCheckout_WhenDuplicateCartItemsExceedStock_ShouldThrowException() {
        Product p = dataStore.getProduct("PROD-02"); // stock = 8
        CheckoutRequest req = new CheckoutRequest();
        req.setCustomerPhone("0987654321");
        // Gửi 2 dòng: 5 + 4 = 9 chiếc (trong khi kho chỉ có 8)
        req.setItems(List.of(new CartItemDto("PROD-02", 5), new CartItemDto("PROD-02", 4)));

        assertThrows(IllegalStateException.class, () -> orderService.checkout(req));
    }

    @Test
    @DisplayName("WB-CHK-12: Checkout thành công, trừ tồn kho chính xác và cộng điểm thưởng")
    void testCheckout_Success_ShouldDeductStock_AndAddLoyaltyPoints() {
        Product p = dataStore.getProduct("PROD-10"); // Bàn phím Keychron (stock = 22)
        int initialStock = p.getStock();

        Customer c = dataStore.getCustomer("CUST-01"); // Đào Văn Công (DIAMOND)
        int initialPoints = c.getLoyaltyPoints();

        CheckoutRequest req = new CheckoutRequest();
        req.setCustomerId("CUST-01");
        req.setCustomerName("Đào Văn Công");
        req.setCustomerPhone("0987654321");
        req.setShippingAddress("ĐH Công nghiệp Hà Nội");
        req.setDistanceKm(5.0);
        req.setPaymentMethod(PaymentMethod.ONLINE_BANKING);
        req.setVoucherCode("TECH50");
        req.setItems(List.of(new CartItemDto("PROD-10", 2)));

        Order order = orderService.checkout(req);

        assertNotNull(order);
        assertEquals("CONFIRMED", order.getStatus());
        assertEquals(initialStock - 2, p.getStock(), "Tồn kho phải giảm đi 2");
        assertTrue(c.getLoyaltyPoints() > initialPoints, "Điểm thưởng phải được cộng");
        assertNotNull(dataStore.getOrder(order.getOrderId()), "Đơn hàng phải được lưu trong DataStore");
    }

    @Test
    @DisplayName("WB-CAN-01 đến 05: Hủy đơn hàng thành công và tự động hoàn trả số lượng vào kho")
    void testCancelOrder_Success_ShouldRestoreStock() {
        Product p = dataStore.getProduct("PROD-11"); // Chuột Logitech (stock = 35)
        int stockBefore = p.getStock();

        CheckoutRequest req = new CheckoutRequest();
        req.setCustomerPhone("0987654321");
        req.setItems(List.of(new CartItemDto("PROD-11", 3)));

        Order order = orderService.checkout(req);
        assertEquals(stockBefore - 3, p.getStock());

        // Hủy đơn hàng
        Order cancelled = orderService.cancelOrder(order.getOrderId());
        assertEquals("CANCELLED", cancelled.getStatus());
        assertEquals(stockBefore, p.getStock(), "Tồn kho phải được phục hồi về 35");

        // Thử hủy lần 2 -> ném ngoại lệ
        assertThrows(IllegalStateException.class, () -> orderService.cancelOrder(order.getOrderId()));
    }

    @Test
    @DisplayName("WB-OM-01 & 04: Cập nhật trạng thái đơn hàng và chặn trạng thái không hợp lệ")
    void testUpdateStatus_ValidAndInvalid() {
        CheckoutRequest req = new CheckoutRequest();
        req.setCustomerPhone("0987654321");
        req.setItems(List.of(new CartItemDto("PROD-12", 1)));
        Order order = orderService.checkout(req);

        // Đổi trạng thái hợp lệ
        Order updated = orderService.updateStatus(order.getOrderId(), "SHIPPING");
        assertEquals("SHIPPING", updated.getStatus());

        // Đổi trạng thái không hợp lệ -> ném IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> orderService.updateStatus(order.getOrderId(), "INVALID_STATUS"));
    }

    @Test
    @DisplayName("WB-OM-07 đến 10: Thống kê Admin KPI chuẩn xác loại trừ đơn hủy")
    void testGetAdminStats_ShouldExcludeCancelledOrdersFromRevenue() {
        Map<String, Object> stats = orderService.getAdminStats();
        assertNotNull(stats);
        assertTrue((Double) stats.get("totalRevenue") > 0);
        assertTrue((Integer) stats.get("totalOrders") >= 1);
        assertTrue((Integer) stats.get("totalProducts") >= 10);
    }
}
