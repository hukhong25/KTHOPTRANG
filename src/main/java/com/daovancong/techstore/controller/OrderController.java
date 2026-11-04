package com.daovancong.techstore.controller;

import com.daovancong.techstore.dto.ApiResponse;
import com.daovancong.techstore.dto.CheckoutRequest;
import com.daovancong.techstore.model.Order;
import com.daovancong.techstore.service.OrderManagementService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    private final OrderManagementService orderService;

    public OrderController(OrderManagementService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ApiResponse<?> checkout(@RequestBody CheckoutRequest request) {
        try {
            Order order = orderService.checkout(request);
            return ApiResponse.ok("Đặt hàng thành công! Mã đơn: " + order.getOrderId(), order);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("Lỗi xử lý đơn hàng: " + e.getMessage());
        }
    }

    @GetMapping
    public ApiResponse<List<Order>> getOrders(@RequestParam(required = false) String customerId) {
        return ApiResponse.ok(orderService.getOrders(customerId));
    }

    @GetMapping("/{id}")
    public ApiResponse<Order> getOrderById(@PathVariable String id) {
        try {
            return ApiResponse.ok(orderService.getOrder(id));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Order> updateStatus(@PathVariable String id, @RequestParam String status) {
        try {
            return ApiResponse.ok("Cập nhật trạng thái thành công", orderService.updateStatus(id, status));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Order> cancelOrder(@PathVariable String id) {
        try {
            return ApiResponse.ok("Đã hủy đơn hàng thành công và hoàn trả số lượng vào kho", orderService.cancelOrder(id));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/admin/stats")
    public ApiResponse<Map<String, Object>> getAdminStats() {
        return ApiResponse.ok(orderService.getAdminStats());
    }
}
