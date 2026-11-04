package com.daovancong.techstore.dto;

import com.daovancong.techstore.TechOrderService.CustomerTier;
import com.daovancong.techstore.TechOrderService.PaymentMethod;
import java.util.List;

public class CheckoutRequest {
    public static class CartItemDto {
        private String productId;
        private int quantity;

        public CartItemDto() {}
        public CartItemDto(String productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    private String customerId;
    private String customerName;
    private String customerPhone;
    private String shippingAddress;
    private CustomerTier customerTier;
    private double distanceKm;
    private boolean expressDelivery;
    private PaymentMethod paymentMethod;
    private String voucherCode;
    private List<CartItemDto> items;

    public CheckoutRequest() {}

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public CustomerTier getCustomerTier() { return customerTier; }
    public void setCustomerTier(CustomerTier customerTier) { this.customerTier = customerTier; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public boolean isExpressDelivery() { return expressDelivery; }
    public void setExpressDelivery(boolean expressDelivery) { this.expressDelivery = expressDelivery; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }

    public List<CartItemDto> getItems() { return items; }
    public void setItems(List<CartItemDto> items) { this.items = items; }
}
