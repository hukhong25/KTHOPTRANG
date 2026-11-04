package com.daovancong.techstore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BÀI TẬP LỚN: KIỂM THỬ PHẦN MỀM (IT6013.2 - K18)
 * BÀI 8 - MỤC L2.1 (LOGBOOK): BỘ KIỂM THỬ HỘP TRẮNG (WHITE-BOX TEST SUITE)
 *
 * Tác giả: Đào Văn Công
 * Mã sinh viên: 2023601884
 * Lớp: IT6013.2 - K18
 *
 * Tiêu chí bao phủ:
 * - Bao phủ câu lệnh (Statement Coverage - C0): 100%
 * - Bao phủ nhánh rẽ (Branch Coverage - C1): 100%
 * - Kiểm thử đường dẫn cơ sở (Basis Path Testing - McCabe Cyclomatic Complexity V(G)=8)
 */
public class TechOrderServiceTest {

    private TechOrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new TechOrderService();
    }

    @Test
    @DisplayName("TC_CONG_01: Đơn hàng không hợp lệ (request null, items rỗng, phone trống, cự ly âm) -> ném IllegalArgumentException")
    void processOrder_InvalidRequest_ThrowsException() {
        // Path 1: (1) -> (2)
        assertThrows(IllegalArgumentException.class, () -> orderService.processOrder(null));

        TechOrderService.OrderRequest emptyItemsReq = new TechOrderService.OrderRequest(
                "CUST01", "0987654321", TechOrderService.CustomerTier.STANDARD,
                Collections.emptyList(), null, 5.0, false, TechOrderService.PaymentMethod.COD
        );
        assertThrows(IllegalArgumentException.class, () -> orderService.processOrder(emptyItemsReq));

        TechOrderService.OrderItem validItem = new TechOrderService.OrderItem("P01", "Chuột Logitech", 300000.0, 1, 10);
        TechOrderService.OrderRequest blankPhoneReq = new TechOrderService.OrderRequest(
                "CUST01", "   ", TechOrderService.CustomerTier.STANDARD,
                List.of(validItem), null, 5.0, false, TechOrderService.PaymentMethod.COD
        );
        assertThrows(IllegalArgumentException.class, () -> orderService.processOrder(blankPhoneReq));

        TechOrderService.OrderRequest negativeDistReq = new TechOrderService.OrderRequest(
                "CUST01", "0987654321", TechOrderService.CustomerTier.STANDARD,
                List.of(validItem), null, -2.0, false, TechOrderService.PaymentMethod.COD
        );
        assertThrows(IllegalArgumentException.class, () -> orderService.processOrder(negativeDistReq));
    }

    @Test
    @DisplayName("TC_CONG_02: Số lượng mua không hợp lệ hoặc vượt tồn kho kho hàng -> ném IllegalStateException")
    void processOrder_InsufficientStock_ThrowsException() {
        // Path 2: (1) -> (3) -> (4)
        TechOrderService.OrderItem itemOverStock = new TechOrderService.OrderItem("P02", "Bàn phím cơ", 1500000.0, 10, 5); // mua 10, kho 5
        TechOrderService.OrderRequest req1 = new TechOrderService.OrderRequest(
                "CUST01", "0987654321", TechOrderService.CustomerTier.STANDARD,
                List.of(itemOverStock), null, 5.0, false, TechOrderService.PaymentMethod.COD
        );
        assertThrows(IllegalStateException.class, () -> orderService.processOrder(req1));

        TechOrderService.OrderItem itemZeroQty = new TechOrderService.OrderItem("P02", "Bàn phím cơ", 1500000.0, 0, 5); // số lượng 0
        TechOrderService.OrderRequest req2 = new TechOrderService.OrderRequest(
                "CUST01", "0987654321", TechOrderService.CustomerTier.STANDARD,
                List.of(itemZeroQty), null, 5.0, false, TechOrderService.PaymentMethod.COD
        );
        assertThrows(IllegalStateException.class, () -> orderService.processOrder(req2));
    }

    @Test
    @DisplayName("TC_CONG_03: Mã giảm giá Voucher đã hết hạn sử dụng -> ném IllegalArgumentException")
    void processOrder_ExpiredVoucher_ThrowsException() {
        // Path 3: (1) -> (3) -> (5) -> (6)
        TechOrderService.OrderItem item = new TechOrderService.OrderItem("P03", "Tai nghe Sony", 2000000.0, 1, 10);
        TechOrderService.Voucher expiredVoucher = new TechOrderService.Voucher(
                "EXPIRED10", 10.0, 100000.0, 500000.0, LocalDate.now().minusDays(1)
        );
        TechOrderService.OrderRequest request = new TechOrderService.OrderRequest(
                "CUST01", "0987654321", TechOrderService.CustomerTier.STANDARD,
                List.of(item), expiredVoucher, 5.0, false, TechOrderService.PaymentMethod.COD
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> orderService.processOrder(request));
        assertTrue(ex.getMessage().contains("Mã giảm giá đã hết hạn"));
    }

    @Test
    @DisplayName("TC_CONG_04: Tổng giá trị đơn hàng chưa đạt giá trị tối thiểu của Voucher -> ném IllegalArgumentException")
    void processOrder_VoucherBelowMinOrder_ThrowsException() {
        // Path 4: (1) -> (3) -> (5) -> (7)
        TechOrderService.OrderItem item = new TechOrderService.OrderItem("P04", "Cáp sạc Type-C", 150000.0, 1, 20); // tổng 150k
        TechOrderService.Voucher voucher = new TechOrderService.Voucher(
                "MIN500K", 15.0, 100000.0, 500000.0, LocalDate.now().plusDays(5) // yêu cầu tối thiểu 500k
        );
        TechOrderService.OrderRequest request = new TechOrderService.OrderRequest(
                "CUST01", "0987654321", TechOrderService.CustomerTier.STANDARD,
                List.of(item), voucher, 5.0, false, TechOrderService.PaymentMethod.COD
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> orderService.processOrder(request));
        assertTrue(ex.getMessage().contains("chưa đạt giá trị tối thiểu"));
    }

    @Test
    @DisplayName("TC_CONG_05: Đơn hàng giá trị cao, hội viên DIAMOND (5%), áp voucher hợp lệ, Freeship (cự ly 5km <= 10km), COD")
    void processOrder_DiamondTier_VoucherValid_Freeship_Success() {
        // Path 5: (1)->(3)->(5)->(8)->(9)->(10)->(14)->(15)->(19)->(21)->(22)
        // subTotal: 10.000.000đ (Tai nghe 2tr x 5)
        // Voucher giảm 10% = 1.000.000đ, nhưng maxDiscount là 500.000đ -> voucherDiscount = 500.000đ
        // Diamond tier: 10.000.000 * 5% = 500.000đ
        // TotalAfterDiscount: 10.000.000 - 500k - 500k = 9.000.000đ (>= 500k & distance 5km <= 10km) -> Freeship = 0đ
        // COD -> paymentDiscount = 0đ
        // FinalAmount = 9.000.000đ, points = 900
        TechOrderService.OrderItem item = new TechOrderService.OrderItem("P05", "Màn hình 4K", 2000000.0, 5, 10);
        TechOrderService.Voucher voucher = new TechOrderService.Voucher(
                "TECH500", 10.0, 500000.0, 1000000.0, LocalDate.now().plusDays(10)
        );
        TechOrderService.OrderRequest request = new TechOrderService.OrderRequest(
                "CUST01", "0987654321", TechOrderService.CustomerTier.DIAMOND,
                List.of(item), voucher, 5.0, false, TechOrderService.PaymentMethod.COD
        );

        TechOrderService.OrderResult result = orderService.processOrder(request);

        assertNotNull(result);
        assertEquals(10000000.0, result.getSubTotal());
        assertEquals(500000.0, result.getVoucherDiscount());
        assertEquals(500000.0, result.getMemberDiscount());
        assertEquals(0.0, result.getShippingFee()); // Freeship
        assertEquals(0.0, result.getPaymentDiscount());
        assertEquals(9000000.0, result.getFinalAmount());
        assertEquals(900, result.getLoyaltyPoints());
        assertEquals("SUCCESS", result.getStatus());
    }

    @Test
    @DisplayName("TC_CONG_06: Hội viên GOLD (3%), cự ly xa (18km > 10km), Giao hỏa tốc (+25k), Thanh toán Online Banking (-2%)")
    void processOrder_GoldTier_LongDistance_Express_OnlinePayment_Success() {
        // Path 6: (1)->(3)->(5)->(9)->(11)->(14)->(16)->(18)->(19)->(20)->(21)->(22)
        // subTotal: 2.000.000đ, ko voucher -> voucherDiscount = 0đ
        // Gold tier: 2.000.000 * 3% = 60.000đ -> totalAfterDiscount = 1.940.000đ
        // Distance 18km (> 10km) -> cước: 30.000 + (18 - 10) * 5.000 = 70.000đ
        // Express delivery: + 25.000đ -> shippingFee = 95.000đ
        // Online banking: 1.940.000 * 2% = 38.800đ
        // FinalAmount: 1.940.000 + 95.000 - 38.800 = 1.996.200đ, points = 199
        TechOrderService.OrderItem item = new TechOrderService.OrderItem("P06", "Ổ cứng SSD 1TB", 2000000.0, 1, 10);
        TechOrderService.OrderRequest request = new TechOrderService.OrderRequest(
                "CUST02", "0912345678", TechOrderService.CustomerTier.GOLD,
                List.of(item), null, 18.0, true, TechOrderService.PaymentMethod.ONLINE_BANKING
        );

        TechOrderService.OrderResult result = orderService.processOrder(request);

        assertNotNull(result);
        assertEquals(2000000.0, result.getSubTotal());
        assertEquals(0.0, result.getVoucherDiscount());
        assertEquals(600000.0 * 0.1, result.getMemberDiscount()); // 60.000đ
        assertEquals(95000.0, result.getShippingFee()); // 70k ship + 25k express
        assertEquals(38800.0, result.getPaymentDiscount()); // 2%
        assertEquals(1996200.0, result.getFinalAmount(), 0.01);
        assertEquals(199, result.getLoyaltyPoints());
    }

    @Test
    @DisplayName("TC_CONG_07: Hội viên SILVER (1%), đơn nhỏ (< 500k), cự ly gần (4km), Thanh toán ví điện tử E_WALLET (-2%)")
    void processOrder_SilverTier_LowAmount_EWallet_Success() {
        // Path 7: (1)->(3)->(5)->(9)->(12)->(14)->(16)->(17)->(19)->(20)->(21)->(22)
        // subTotal: 400.000đ, ko voucher
        // Silver tier: 400.000 * 1% = 4.000đ -> totalAfterDiscount = 396.000đ (< 500k nên ko freeship)
        // Distance 4km <= 10km -> cước cơ sở 30.000đ, ko express
        // E-wallet payment: 396.000 * 2% = 7.920đ
        // FinalAmount = 396.000 + 30.000 - 7.920 = 418.080đ, points = 41
        TechOrderService.OrderItem item = new TechOrderService.OrderItem("P07", "Chuột không dây", 200000.0, 2, 15);
        TechOrderService.OrderRequest request = new TechOrderService.OrderRequest(
                "CUST03", "0905123456", TechOrderService.CustomerTier.SILVER,
                List.of(item), null, 4.0, false, TechOrderService.PaymentMethod.E_WALLET
        );

        TechOrderService.OrderResult result = orderService.processOrder(request);

        assertNotNull(result);
        assertEquals(400000.0, result.getSubTotal());
        assertEquals(4000.0, result.getMemberDiscount());
        assertEquals(30000.0, result.getShippingFee());
        assertEquals(7920.0, result.getPaymentDiscount());
        assertEquals(418080.0, result.getFinalAmount(), 0.01);
        assertEquals(41, result.getLoyaltyPoints());
    }

    @Test
    @DisplayName("TC_CONG_08: Khách hàng STANDARD (0%), không voucher, cự ly xa (12km), giao thường, COD")
    void processOrder_StandardTier_LongDistance_COD_Success() {
        // Path 8: (1)->(3)->(5)->(9)->(13)->(14)->(16)->(17)->(19)->(21)->(22)
        // subTotal: 800.000đ (>= 500k nhưng cự ly 12km > 10km nên ko được freeship)
        // cước: 30.000 + (12 - 10) * 5.000 = 40.000đ
        // FinalAmount = 800.000 + 40.000 = 840.000đ, points = 84
        TechOrderService.OrderItem item = new TechOrderService.OrderItem("P08", "Webcam Full HD", 800000.0, 1, 10);
        TechOrderService.OrderRequest request = new TechOrderService.OrderRequest(
                "CUST04", "0933112233", TechOrderService.CustomerTier.STANDARD,
                List.of(item), null, 12.0, false, TechOrderService.PaymentMethod.COD
        );

        TechOrderService.OrderResult result = orderService.processOrder(request);

        assertNotNull(result);
        assertEquals(800000.0, result.getSubTotal());
        assertEquals(0.0, result.getVoucherDiscount());
        assertEquals(0.0, result.getMemberDiscount());
        assertEquals(40000.0, result.getShippingFee());
        assertEquals(0.0, result.getPaymentDiscount());
        assertEquals(840000.0, result.getFinalAmount());
        assertEquals(84, result.getLoyaltyPoints());
    }
}
