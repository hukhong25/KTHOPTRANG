package com.daovancong.techstore.controller;

import com.daovancong.techstore.dto.ApiResponse;
import com.daovancong.techstore.model.Product;
import com.daovancong.techstore.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ApiResponse<List<Product>> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort) {
        return ApiResponse.ok(productService.getProducts(category, search, sort));
    }

    @GetMapping("/{id}")
    public ApiResponse<Product> getProductById(@PathVariable String id) {
        return ApiResponse.ok(productService.getProductById(id));
    }

    @PostMapping
    public ApiResponse<Product> createProduct(@RequestBody Product product) {
        return ApiResponse.ok("Thêm sản phẩm thành công", productService.createProduct(product));
    }

    @PutMapping("/{id}")
    public ApiResponse<Product> updateProduct(@PathVariable String id, @RequestBody Product product) {
        return ApiResponse.ok("Cập nhật sản phẩm thành công", productService.updateProduct(id, product));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ApiResponse.ok("Xóa sản phẩm thành công", null);
    }
}
