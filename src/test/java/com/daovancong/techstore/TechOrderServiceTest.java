package com.daovancong.techstore;

import com.daovancong.techstore.TechOrderService.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UNIT TEST SUITE CHO TECHORDERSERVICE
 *
 * Developer: Đào Văn Công (2023601884)
 */
class TechOrderServiceTest {

    private TechOrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new TechOrderService();
    }

    @Test
    @DisplayName("Ném ngoại lệ khi request rỗng hoặc không hợp lệ")
    void testProcessOrder_WhenRequestIsInvalid_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> orderService.processOrder(null));

        OrderRequest emptyItems = new OrderRequest("C1", "0987654321", CustomerTier.STANDARD,
                Collections.emptyList(), null, 5.0, false, PaymentMethod.COD);
        assertThrows(IllegalArgumentException.class, () -> orderService.processOrder(emptyItems));

        OrderRequest invalidPhone = new OrderRequest("C1", "   ", CustomerTier.STANDARD,
                List.of(new OrderItem("P1", "Item", 100000, 1, 10)), null, 5.0, false, PaymentMethod.COD);
        assertThrows(IllegalArgumentException.class, () -> orderService.processOrder(invalidPhone));

        OrderRequest negativeDistance = new OrderRequest("C1", "0987654321", CustomerTier.STANDARD,
                List.of(new OrderItem("P1", "Item", 100000, 1, 10)), null, -1.0, false, PaymentMethod.COD);
        assertThrows(IllegalArgumentException.class, () -> orderService.processOrder(negativeDistance));
    }

    @Test
    @DisplayName("Ném ngoại lệ khi số lượng đặt không hợp lệ hoặc vượt tồn kho")
    void testProcessOrder_WhenStockExceeded_ShouldThrowException() {
        OrderItem itemZero = new OrderItem("P1", "Chuột Logitech", 500000, 0, 10);
        OrderRequest reqZero = new OrderRequest("C1", "0987654321", CustomerTier.STANDARD,
                List.of(itemZero), null, 5.0, false, PaymentMethod.COD);
        assertThrows(IllegalStateException.class, () -> orderService.processOrder(reqZero));

        OrderItem itemExceed = new OrderItem("P2", "Dell XPS 15", 42000000, 5, 2);
        OrderRequest reqExceed = new OrderRequest("C1", "0987654321", CustomerTier.STANDARD,
                List.of(itemExceed), null, 5.0, false, PaymentMethod.COD);
        assertThrows(IllegalStateException.class, () -> orderService.processOrder(reqExceed));
    }

    @Test
    @DisplayName("Ném ngoại lệ khi mã Voucher áp dụng đã hết hạn")
    void testProcessOrder_WhenVoucherIsExpired_ShouldThrowException() {
        OrderItem item = new OrderItem("P1", "Laptop MacBook Pro", 30000000, 1, 10);
        Voucher expiredVoucher = new Voucher("OLD_VOUCHER", 10.0, 500000, 1000000, LocalDate.now().minusDays(3));
        OrderRequest req = new OrderRequest("C1", "0987654321", CustomerTier.STANDARD,
                List.of(item), expiredVoucher, 5.0, false, PaymentMethod.COD);

        assertThrows(IllegalArgumentException.class, () -> orderService.processOrder(req));
    }

    @Test
    @DisplayName("Ném ngoại lệ khi đơn hàng chưa đạt giá trị tối thiểu của Voucher")
    void testProcessOrder_WhenSubtotalBelowVoucherMinOrder_ShouldThrowException() {
        OrderItem item = new OrderItem("P1", "Cáp sạc Type-C", 150000, 1, 20);
        Voucher voucher = new Voucher("MIN_VOUCHER", 10.0, 50000, 500000, LocalDate.now().plusDays(10));
        OrderRequest req = new OrderRequest("C1", "0987654321", CustomerTier.STANDARD,
                List.of(item), voucher, 5.0, false, PaymentMethod.COD);

        assertThrows(IllegalArgumentException.class, () -> orderService.processOrder(req));
    }

    @Test
    @DisplayName("Tính tiền cho khách Diamond có Voucher chạm trần tối đa và Freeship")
    void testProcessOrder_DiamondTier_WithCappedVoucher_AndFreeship() {
        OrderItem item = new OrderItem("P1", "MacBook Pro M3", 20000000, 1, 10);
        Voucher voucher = new Voucher("TECH50", 10.0, 500000, 2000000, LocalDate.now().plusDays(30));
        OrderRequest req = new OrderRequest("CUST-01", "0987654321", CustomerTier.DIAMOND,
                List.of(item), voucher, 5.0, false, PaymentMethod.COD);

        OrderResult result = orderService.processOrder(req);

        assertEquals(20000000.0, result.getSubTotal());
        assertEquals(500000.0, result.getVoucherDiscount());
        assertEquals(1000000.0, result.getMemberDiscount()); // 5% của 20tr
        assertEquals(0.0, result.getShippingFee()); // Freeship vì >= 500k và cự ly <= 10km
        assertEquals(0.0, result.getPaymentDiscount()); // COD không giảm
        assertEquals(18500000.0, result.getFinalAmount());
        assertEquals(1850, result.getLoyaltyPoints());
    }

    @Test
    @DisplayName("Tính tiền cho khách Gold có khoảng cách xa >10km, giao hỏa tốc và thanh toán Banking")
    void testProcessOrder_GoldTier_LongDistance_Express_Banking() {
        OrderItem item = new OrderItem("P1", "iPhone 16 Pro Max", 15000000, 1, 10);
        OrderRequest req = new OrderRequest("CUST-02", "0912345678", CustomerTier.GOLD,
                List.of(item), null, 15.0, true, PaymentMethod.ONLINE_BANKING);

        OrderResult result = orderService.processOrder(req);

        assertEquals(15000000.0, result.getSubTotal());
        assertEquals(0.0, result.getVoucherDiscount());
        assertEquals(450000.0, result.getMemberDiscount()); // 3% của 15tr = 450k
        // Cước ship: 30k + (15 - 10) * 5k + 25k hỏa tốc = 80k
        assertEquals(80000.0, result.getShippingFee());
        // Giảm thanh toán online: 2% của (15tr - 450k = 14.550.000) = 291k
        assertEquals(291000.0, result.getPaymentDiscount());
        assertEquals(14339000.0, result.getFinalAmount());
        assertEquals(1433, result.getLoyaltyPoints());
    }

    @Test
    @DisplayName("Tính tiền cho khách Silver đơn nhỏ hơn 500k và thanh toán ví điện tử")
    void testProcessOrder_SilverTier_SmallOrder_EWallet() {
        OrderItem item = new OrderItem("P1", "Củ sạc nhanh Anker", 200000, 1, 30);
        OrderRequest req = new OrderRequest("CUST-03", "0909090909", CustomerTier.SILVER,
                List.of(item), null, 5.0, false, PaymentMethod.E_WALLET);

        OrderResult result = orderService.processOrder(req);

        assertEquals(200000.0, result.getSubTotal());
        assertEquals(2000.0, result.getMemberDiscount()); // 1% của 200k = 2k
        assertEquals(30000.0, result.getShippingFee()); // Đơn < 500k không được freeship
        assertEquals(3960.0, result.getPaymentDiscount()); // 2% của (200k - 2k = 198k) = 3960đ
        assertEquals(224040.0, result.getFinalAmount());
        assertEquals(22, result.getLoyaltyPoints());
    }

    @Test
    @DisplayName("Tính tiền cho khách Standard không voucher, khoảng cách >10km và trả tiền mặt COD")
    void testProcessOrder_StandardTier_NoVoucher_LongDistance_COD() {
        OrderItem item = new OrderItem("P1", "Tai nghe Gaming", 1000000, 1, 10);
        OrderRequest req = new OrderRequest("CUST-04", "0933333333", CustomerTier.STANDARD,
                List.of(item), null, 12.0, false, PaymentMethod.COD);

        OrderResult result = orderService.processOrder(req);

        assertEquals(1000000.0, result.getSubTotal());
        assertEquals(0.0, result.getMemberDiscount());
        // 30k + (12 - 10) * 5k = 40k
        assertEquals(40000.0, result.getShippingFee());
        assertEquals(0.0, result.getPaymentDiscount());
        assertEquals(1040000.0, result.getFinalAmount());
        assertEquals(104, result.getLoyaltyPoints());
    }
}
