package com.daovancong.techstore.controller;

import com.daovancong.techstore.TechOrderService.CustomerTier;
import com.daovancong.techstore.dto.ApiResponse;
import com.daovancong.techstore.model.Customer;
import com.daovancong.techstore.repository.DataStore;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {
    private final DataStore dataStore;

    public CustomerController(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @GetMapping
    public ApiResponse<List<Customer>> getAllCustomers() {
        return ApiResponse.ok(dataStore.getAllCustomers());
    }

    @GetMapping("/{id}")
    public ApiResponse<Customer> getCustomer(@PathVariable String id) {
        Customer c = dataStore.getCustomer(id);
        if (c == null) {
            return ApiResponse.error("Không tìm thấy khách hàng");
        }
        return ApiResponse.ok(c);
    }

    @PutMapping("/{id}/tier")
    public ApiResponse<Customer> updateTier(@PathVariable String id, @RequestParam CustomerTier tier) {
        Customer c = dataStore.getCustomer(id);
        if (c == null) {
            return ApiResponse.error("Không tìm thấy khách hàng");
        }
        c.setTier(tier);
        return ApiResponse.ok("Đã cập nhật phân hạng thành công", c);
    }
}
