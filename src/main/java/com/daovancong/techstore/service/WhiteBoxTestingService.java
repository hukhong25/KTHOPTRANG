package com.daovancong.techstore.service;

import com.daovancong.techstore.TechOrderService;
import com.daovancong.techstore.TechOrderService.*;
import com.daovancong.techstore.dto.TestCaseResult;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class WhiteBoxTestingService {
    private final TechOrderService techOrderService;

    public WhiteBoxTestingService(TechOrderService techOrderService) {
        this.techOrderService = techOrderService;
    }

    public List<TestCaseResult> runAllTestCases() {
        List<TestCaseResult> results = new ArrayList<>();

        // TC_CONG_01: Path 1
        results.add(runTest("TC_CONG_01", "Dữ liệu đơn hàng null hoặc rỗng",
                "Path 1: Node 1 -> Node 2 (Ném ngoại lệ)",
                "OrderRequest = null",
                "Ném IllegalArgumentException('Thông tin đơn hàng không hợp lệ')",
                () -> {
                    try {
                        techOrderService.processOrder(null);
                        return new TestOutcome(false, "Không ném ngoại lệ", List.of(1));
                    } catch (IllegalArgumentException ex) {
                        return new TestOutcome(true, "Bắt được IllegalArgumentException: " + ex.getMessage(), List.of(1, 2));
                    }
                }));

        // TC_CONG_02: Path 2
        results.add(runTest("TC_CONG_02", "Sản phẩm đặt mua vượt quá tồn kho khả dụng",
                "Path 2: Node 1 -> Node 3 -> Node 4 (Ném ngoại lệ)",
                "Laptop Dell XPS: qty = 5, stock = 2",
                "Ném IllegalStateException('Sản phẩm không đủ tồn kho')",
                () -> {
                    try {
                        OrderItem item = new OrderItem("P1", "Laptop Dell XPS", 42500000.0, 5, 2);
                        OrderRequest req = new OrderRequest("C1", "0987654321", CustomerTier.STANDARD,
                                List.of(item), null, 5.0, false, PaymentMethod.COD);
                        techOrderService.processOrder(req);
                        return new TestOutcome(false, "Không ném ngoại lệ", List.of(1, 3));
                    } catch (IllegalStateException ex) {
                        return new TestOutcome(true, "Bắt được IllegalStateException: " + ex.getMessage(), List.of(1, 3, 4));
                    }
                }));

        // TC_CONG_03: Path 3
        results.add(runTest("TC_CONG_03", "Voucher áp dụng đã hết hạn",
                "Path 3: Node 1 -> Node 3 -> Node 5 -> Node 6 (Ném ngoại lệ)",
                "Voucher TECH50 hết hạn 5 ngày trước, subtotal = 20.000.000đ",
                "Ném IllegalArgumentException('Mã giảm giá đã hết hạn sử dụng')",
                () -> {
                    try {
                        OrderItem item = new OrderItem("P1", "MacBook Pro", 20000000.0, 1, 10);
                        Voucher expired = new Voucher("EXPIRED", 10.0, 500000.0, 1000000.0, LocalDate.now().minusDays(5));
                        OrderRequest req = new OrderRequest("C1", "0987654321", CustomerTier.STANDARD,
                                List.of(item), expired, 5.0, false, PaymentMethod.COD);
                        techOrderService.processOrder(req);
                        return new TestOutcome(false, "Không ném ngoại lệ", List.of(1, 3, 5));
                    } catch (IllegalArgumentException ex) {
                        return new TestOutcome(true, "Bắt được IllegalArgumentException: " + ex.getMessage(), List.of(1, 3, 5, 6));
                    }
                }));

        // TC_CONG_04: Path 4
        results.add(runTest("TC_CONG_04", "Đơn hàng chưa đạt giá trị tối thiểu của Voucher",
                "Path 4: Node 1 -> Node 3 -> Node 5 -> Node 6 -> Node 7 (Ném ngoại lệ)",
                "Đơn 200.000đ, Voucher yêu cầu đơn tối thiểu 500.000đ",
                "Ném IllegalArgumentException('Đơn hàng chưa đạt giá trị tối thiểu')",
                () -> {
                    try {
                        OrderItem item = new OrderItem("P1", "Cáp sạc USB-C", 200000.0, 1, 50);
                        Voucher v = new Voucher("V500", 10.0, 100000.0, 500000.0, LocalDate.now().plusDays(10));
                        OrderRequest req = new OrderRequest("C1", "0987654321", CustomerTier.STANDARD,
                                List.of(item), v, 5.0, false, PaymentMethod.COD);
                        techOrderService.processOrder(req);
                        return new TestOutcome(false, "Không ném ngoại lệ", List.of(1, 3, 5, 6));
                    } catch (IllegalArgumentException ex) {
                        return new TestOutcome(true, "Bắt được IllegalArgumentException: " + ex.getMessage(), List.of(1, 3, 5, 6, 7));
                    }
                }));

        // TC_CONG_05: Path 5
        results.add(runTest("TC_CONG_05", "Khách Diamond, Voucher chạm trần tối đa, Freeship, COD",
                "Path 5: 1->3->5->6->7->8->9->10(Diamond)->14->15(Freeship)->19(COD)->21->22",
                "Diamond, Đơn 20.000.000đ, Voucher giảm trần 500.000đ, KC 5km, COD",
                "Giảm voucher: 500.000đ, Giảm VIP 5%: 1.000.000đ, Ship: 0đ, Tổng: 18.500.000đ",
                () -> {
                    OrderItem item = new OrderItem("P1", "MacBook", 20000000.0, 1, 10);
                    Voucher v = new Voucher("VMAX", 10.0, 500000.0, 1000000.0, LocalDate.now().plusDays(10));
                    OrderRequest req = new OrderRequest("C1", "0987654321", CustomerTier.DIAMOND,
                            List.of(item), v, 5.0, false, PaymentMethod.COD);
                    OrderResult res = techOrderService.processOrder(req);
                    boolean ok = (res.getFinalAmount() == 18500000.0 && res.getShippingFee() == 0.0);
                    return new TestOutcome(ok, "Thành tiền: " + String.format("%,.0fđ", res.getFinalAmount()) + ", Phí ship: " + res.getShippingFee() + "đ", 
                            List.of(1, 3, 5, 6, 7, 8, 9, 10, 14, 15, 19, 21, 22));
                }));

        // TC_CONG_06: Path 6
        results.add(runTest("TC_CONG_06", "Khách Gold, KC 15km (>10km), Giao hỏa tốc, Thanh toán Banking",
                "Path 6: 1->3->5->9->11(Gold)->14->16(Cộng km)->18(Hỏa tốc)->19->20(Banking)->21->22",
                "Gold, Đơn 15.000.000đ, KC 15km, Hỏa tốc, Thanh toán ONLINE_BANKING",
                "Giảm VIP 3%: 450.000đ, Ship: 80.000đ, Giảm 2% Banking: 291.000đ, Tổng: 14.339.000đ",
                () -> {
                    OrderItem item = new OrderItem("P1", "Phone", 15000000.0, 1, 10);
                    OrderRequest req = new OrderRequest("C1", "0987654321", CustomerTier.GOLD,
                            List.of(item), null, 15.0, true, PaymentMethod.ONLINE_BANKING);
                    OrderResult res = techOrderService.processOrder(req);
                    // subtotal = 15m, vip 3% = 450k -> totalAfter = 14.550.000
                    // shipping = 30k + (15-10)*5k + 25k = 80k
                    // payment 2% = 14.550.000 * 0.02 = 291.000
                    // final = 14.550.000 + 80.000 - 291.000 = 14.339.000
                    boolean ok = (res.getFinalAmount() == 14339000.0 && res.getShippingFee() == 80000.0);
                    return new TestOutcome(ok, "Thành tiền: " + String.format("%,.0fđ", res.getFinalAmount()) + ", Phí ship: " + res.getShippingFee() + "đ",
                            List.of(1, 3, 5, 9, 11, 14, 16, 18, 19, 20, 21, 22));
                }));

        // TC_CONG_07: Path 7
        results.add(runTest("TC_CONG_07", "Khách Silver, Đơn < 500k, KC gần, Ví E_WALLET",
                "Path 7: 1->3->5->9->12(Silver)->14->16(Cơ bản)->19->20(E_Wallet)->21->22",
                "Silver, Đơn 200.000đ, KC 5km, Thường, Thanh toán Ví E_WALLET",
                "Giảm VIP 1%: 2.000đ, Ship: 30.000đ, Giảm 2% Ví: 3.960đ, Tổng: 224.040đ",
                () -> {
                    OrderItem item = new OrderItem("P1", "Cáp", 200000.0, 1, 10);
                    OrderRequest req = new OrderRequest("C1", "0987654321", CustomerTier.SILVER,
                            List.of(item), null, 5.0, false, PaymentMethod.E_WALLET);
                    OrderResult res = techOrderService.processOrder(req);
                    boolean ok = (res.getFinalAmount() == 224040.0 && res.getShippingFee() == 30000.0);
                    return new TestOutcome(ok, "Thành tiền: " + String.format("%,.0fđ", res.getFinalAmount()) + ", Phí ship: " + res.getShippingFee() + "đ",
                            List.of(1, 3, 5, 9, 12, 14, 16, 19, 20, 21, 22));
                }));

        // TC_CONG_08: Path 8
        results.add(runTest("TC_CONG_08", "Khách Standard, Không voucher, KC 12km, Tiền mặt COD",
                "Path 8: 1->3->5->9->13(Standard)->14->16(Cộng km)->19(COD)->21->22",
                "Standard, Đơn 1.000.000đ, KC 12km, Thường, COD",
                "Giảm VIP: 0đ, Ship: 40.000đ (30k + 2*5k), Tổng: 1.040.000đ",
                () -> {
                    OrderItem item = new OrderItem("P1", "Sạc", 1000000.0, 1, 10);
                    OrderRequest req = new OrderRequest("C1", "0987654321", CustomerTier.STANDARD,
                            List.of(item), null, 12.0, false, PaymentMethod.COD);
                    OrderResult res = techOrderService.processOrder(req);
                    boolean ok = (res.getFinalAmount() == 1040000.0 && res.getShippingFee() == 40000.0);
                    return new TestOutcome(ok, "Thành tiền: " + String.format("%,.0fđ", res.getFinalAmount()) + ", Phí ship: " + res.getShippingFee() + "đ",
                            List.of(1, 3, 5, 9, 13, 14, 16, 19, 21, 22));
                }));

        return results;
    }

    private static class TestOutcome {
        boolean passed;
        String message;
        List<Integer> nodes;
        TestOutcome(boolean p, String m, List<Integer> n) { this.passed = p; this.message = m; this.nodes = n; }
    }

    private interface TestRunner { TestOutcome execute() throws Exception; }

    private TestCaseResult runTest(String id, String name, String path, String input, String expected, TestRunner runner) {
        long start = System.nanoTime();
        try {
            TestOutcome outcome = runner.execute();
            long timeMs = (System.nanoTime() - start) / 1_000_000;
            return new TestCaseResult(id, name, path, input, expected, outcome.message, outcome.passed, timeMs, outcome.nodes);
        } catch (Exception e) {
            long timeMs = (System.nanoTime() - start) / 1_000_000;
            return new TestCaseResult(id, name, path, input, expected, "Lỗi bất thường: " + e.getMessage(), false, timeMs, List.of());
        }
    }
}
