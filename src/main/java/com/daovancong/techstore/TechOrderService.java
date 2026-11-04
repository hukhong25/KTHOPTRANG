package com.daovancong.techstore;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * TECHSTORE - DỊCH VỤ XỬ LÝ ĐƠN HÀNG & THANH TOÁN
 *
 * Developer: Đào Văn Công
 * Mã sinh viên: 2023601884
 * Lớp: IT6013.2 - K18
 * Đề tài: Hệ thống thương mại điện tử thiết bị công nghệ (TechStore)
 */
@Service
public class TechOrderService {

    public static class OrderItem {
        private final String productId;
        private final String productName;
        private final double price;
        private final int quantity;
        private final int stock;

        public OrderItem(String productId, String productName, double price, int quantity, int stock) {
            this.productId = productId;
            this.productName = productName;
            this.price = price;
            this.quantity = quantity;
            this.stock = stock;
        }

        public String getProductId() { return productId; }
        public String getProductName() { return productName; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
        public int getStock() { return stock; }
    }

    public enum CustomerTier {
        STANDARD, SILVER, GOLD, DIAMOND
    }

    public enum PaymentMethod {
        COD, ONLINE_BANKING, E_WALLET
    }

    public static class Voucher {
        private final String code;
        private final double discountPercent; // Phần trăm giảm giá (VD: 10.0 = 10%)
        private final double maxDiscount;      // Trần giảm giá tối đa
        private final double minOrderValue;    // Giá trị đơn hàng tối thiểu
        private final LocalDate expiryDate;    // Ngày hết hạn

        public Voucher(String code, double discountPercent, double maxDiscount, double minOrderValue, LocalDate expiryDate) {
            this.code = code;
            this.discountPercent = discountPercent;
            this.maxDiscount = maxDiscount;
            this.minOrderValue = minOrderValue;
            this.expiryDate = expiryDate;
        }

        public String getCode() { return code; }
        public double getDiscountPercent() { return discountPercent; }
        public double getMaxDiscount() { return maxDiscount; }
        public double getMinOrderValue() { return minOrderValue; }
        public LocalDate getExpiryDate() { return expiryDate; }
    }

    public static class OrderRequest {
        private String customerId;
        private String customerPhone;
        private CustomerTier customerTier;
        private List<OrderItem> items;
        private Voucher voucher;
        private double distanceKm;
        private boolean expressDelivery;
        private PaymentMethod paymentMethod;

        public OrderRequest(String customerId, String customerPhone, CustomerTier customerTier,
                            List<OrderItem> items, Voucher voucher, double distanceKm,
                            boolean expressDelivery, PaymentMethod paymentMethod) {
            this.customerId = customerId;
            this.customerPhone = customerPhone;
            this.customerTier = customerTier;
            this.items = items;
            this.voucher = voucher;
            this.distanceKm = distanceKm;
            this.expressDelivery = expressDelivery;
            this.paymentMethod = paymentMethod;
        }

        public String getCustomerId() { return customerId; }
        public String getCustomerPhone() { return customerPhone; }
        public CustomerTier getCustomerTier() { return customerTier; }
        public List<OrderItem> getItems() { return items; }
        public Voucher getVoucher() { return voucher; }
        public double getDistanceKm() { return distanceKm; }
        public boolean isExpressDelivery() { return expressDelivery; }
        public PaymentMethod getPaymentMethod() { return paymentMethod; }
    }

    public static class OrderResult {
        private final String orderId;
        private final double subTotal;
        private final double voucherDiscount;
        private final double memberDiscount;
        private final double shippingFee;
        private final double paymentDiscount;
        private final double finalAmount;
        private final int loyaltyPoints;
        private final String status;

        public OrderResult(String orderId, double subTotal, double voucherDiscount, double memberDiscount,
                           double shippingFee, double paymentDiscount, double finalAmount,
                           int loyaltyPoints, String status) {
            this.orderId = orderId;
            this.subTotal = subTotal;
            this.voucherDiscount = voucherDiscount;
            this.memberDiscount = memberDiscount;
            this.shippingFee = shippingFee;
            this.paymentDiscount = paymentDiscount;
            this.finalAmount = finalAmount;
            this.loyaltyPoints = loyaltyPoints;
            this.status = status;
        }

        public String getOrderId() { return orderId; }
        public double getSubTotal() { return subTotal; }
        public double getVoucherDiscount() { return voucherDiscount; }
        public double getMemberDiscount() { return memberDiscount; }
        public double getShippingFee() { return shippingFee; }
        public double getPaymentDiscount() { return paymentDiscount; }
        public double getFinalAmount() { return finalAmount; }
        public int getLoyaltyPoints() { return loyaltyPoints; }
        public String getStatus() { return status; }
    }

    /**
     * Nghiệp vụ tính toán thanh toán đơn hàng toàn diện:
     * 1. Kiểm tra tính hợp lệ dữ liệu đầu vào.
     * 2. Kiểm tra tồn kho và tính tổng tiền hàng (subtotal).
     * 3. Áp dụng mã giảm giá voucher (kiểm tra hạn sử dụng, giá trị tối thiểu, trần giảm giá).
     * 4. Áp dụng chiết khấu hạng thành viên (Diamond 5%, Gold 3%, Silver 1%).
     * 5. Tính phí vận chuyển (Freeship nếu >= 500k & <= 10km, phụ phí cự ly > 10km, phụ phí hỏa tốc).
     * 6. Áp dụng chiết khấu 2% khi thanh toán trực tuyến (Banking, Ví điện tử).
     * 7. Tính tổng tiền thanh toán cuối cùng và tích lũy điểm thưởng.
     */
    public OrderResult processOrder(OrderRequest request) {
        // 1. Kiểm tra tính hợp lệ của request đầu vào
        if (request == null || request.getItems() == null || request.getItems().isEmpty()
                || request.getCustomerPhone() == null || request.getCustomerPhone().trim().isEmpty()
                || request.getDistanceKm() < 0.0) {
            throw new IllegalArgumentException("Thông tin đơn hàng không hợp lệ");
        }

        // 2. Duyệt từng sản phẩm: kiểm tra tồn kho và tính tổng tiền hàng
        double subTotal = 0.0;
        for (OrderItem item : request.getItems()) {
            if (item.getQuantity() <= 0 || item.getQuantity() > item.getStock()) {
                throw new IllegalStateException("Sản phẩm không đủ tồn kho hoặc số lượng không hợp lệ");
            }
            subTotal += item.getPrice() * item.getQuantity();
        }

        // 3. Xử lý mã giảm giá Voucher
        double voucherDiscount = 0.0;
        if (request.getVoucher() != null) {
            Voucher voucher = request.getVoucher();
            LocalDate today = LocalDate.now();
            if (voucher.getExpiryDate() != null && voucher.getExpiryDate().isBefore(today)) {
                throw new IllegalArgumentException("Mã giảm giá đã hết hạn sử dụng");
            }
            if (subTotal < voucher.getMinOrderValue()) {
                throw new IllegalArgumentException("Đơn hàng chưa đạt giá trị tối thiểu để áp dụng voucher");
            }
            double calculatedDiscount = subTotal * (voucher.getDiscountPercent() / 100.0);
            voucherDiscount = (voucher.getMaxDiscount() > 0)
                    ? Math.min(calculatedDiscount, voucher.getMaxDiscount())
                    : calculatedDiscount;
        }

        // 4. Xử lý chiết khấu thành viên VIP
        double memberDiscount = 0.0;
        CustomerTier tier = (request.getCustomerTier() != null) ? request.getCustomerTier() : CustomerTier.STANDARD;
        if (tier == CustomerTier.DIAMOND) {
            memberDiscount = subTotal * 0.05; // Giảm 5%
        } else if (tier == CustomerTier.GOLD) {
            memberDiscount = subTotal * 0.03; // Giảm 3%
        } else if (tier == CustomerTier.SILVER) {
            memberDiscount = subTotal * 0.01; // Giảm 1%
        }

        double totalAfterDiscounts = Math.max(0.0, subTotal - voucherDiscount - memberDiscount);

        // 5. Tính phí vận chuyển (Miễn phí ship nếu đơn sau giảm >= 500k và cự ly <= 10km)
        double shippingFee = 0.0;
        if (totalAfterDiscounts >= 500000.0 && request.getDistanceKm() <= 10.0) {
            shippingFee = 0.0; // Freeship
        } else {
            shippingFee = 30000.0; // Cước cơ bản
            if (request.getDistanceKm() > 10.0) {
                shippingFee += (request.getDistanceKm() - 10.0) * 5000.0; // Phụ phí cự ly
            }
        }

        if (request.isExpressDelivery()) {
            shippingFee += 25000.0; // Phụ phí giao hỏa tốc
        }

        // 6. Ưu đãi chiết khấu thanh toán trực tuyến (2%)
        double paymentDiscount = 0.0;
        if (request.getPaymentMethod() == PaymentMethod.ONLINE_BANKING || request.getPaymentMethod() == PaymentMethod.E_WALLET) {
            paymentDiscount = totalAfterDiscounts * 0.02;
        }

        // 7. Tổng tiền thanh toán cuối cùng và tích điểm thưởng
        double finalAmount = Math.max(0.0, totalAfterDiscounts + shippingFee - paymentDiscount);
        int loyaltyPoints = (int) (finalAmount / 10000.0); // 10.000đ = 1 điểm thưởng
        String orderId = "TECH-ORD-" + System.currentTimeMillis();

        return new OrderResult(orderId, subTotal, voucherDiscount, memberDiscount,
                shippingFee, paymentDiscount, finalAmount, loyaltyPoints, "SUCCESS");
    }
}
