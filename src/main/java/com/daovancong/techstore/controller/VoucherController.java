package com.daovancong.techstore.controller;

import com.daovancong.techstore.TechOrderService.Voucher;
import com.daovancong.techstore.dto.ApiResponse;
import com.daovancong.techstore.repository.DataStore;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vouchers")
@CrossOrigin(origins = "*")
public class VoucherController {
    private final DataStore dataStore;

    public VoucherController(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @GetMapping
    public ApiResponse<List<Voucher>> getAllVouchers() {
        return ApiResponse.ok(dataStore.getAllVouchers());
    }

    @PostMapping("/validate")
    public ApiResponse<Map<String, Object>> validateVoucher(
            @RequestParam String code,
            @RequestParam double subTotal) {
        Voucher voucher = dataStore.getVoucher(code);
        if (voucher == null) {
            return ApiResponse.error("Mã voucher '" + code + "' không tồn tại");
        }
        if (voucher.getExpiryDate() != null && voucher.getExpiryDate().isBefore(LocalDate.now())) {
            return ApiResponse.error("Mã voucher đã hết hạn sử dụng");
        }
        if (subTotal < voucher.getMinOrderValue()) {
            return ApiResponse.error(String.format("Đơn hàng chưa đạt giá trị tối thiểu %,.0fđ để áp dụng voucher", voucher.getMinOrderValue()));
        }

        double calculatedDiscount = subTotal * (voucher.getDiscountPercent() / 100.0);
        double actualDiscount = (voucher.getMaxDiscount() > 0)
                ? Math.min(calculatedDiscount, voucher.getMaxDiscount())
                : calculatedDiscount;

        Map<String, Object> res = new HashMap<>();
        res.put("valid", true);
        res.put("voucher", voucher);
        res.put("discountAmount", actualDiscount);
        return ApiResponse.ok("Voucher hợp lệ", res);
    }

    @PostMapping
    public ApiResponse<Voucher> createVoucher(@RequestBody Voucher voucher) {
        dataStore.addVoucher(voucher);
        return ApiResponse.ok("Tạo voucher thành công", voucher);
    }
}
