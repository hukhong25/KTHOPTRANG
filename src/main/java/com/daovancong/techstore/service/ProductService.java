package com.daovancong.techstore.service;

import com.daovancong.techstore.model.Product;
import com.daovancong.techstore.repository.DataStore;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final DataStore dataStore;

    public ProductService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public List<Product> getProducts(String category, String search, String sort) {
        return dataStore.getAllProducts().stream()
                .filter(p -> category == null || category.equalsIgnoreCase("ALL") || 
                        (p.getCategory() != null && p.getCategory().equalsIgnoreCase(category)))
                .filter(p -> search == null || search.trim().isEmpty() || 
                        (p.getName() != null && p.getName().toLowerCase().contains(search.toLowerCase())) || 
                        (p.getDescription() != null && p.getDescription().toLowerCase().contains(search.toLowerCase())))
                .sorted((p1, p2) -> {
                    if ("price_asc".equalsIgnoreCase(sort)) return Double.compare(p1.getPrice(), p2.getPrice());
                    if ("price_desc".equalsIgnoreCase(sort)) return Double.compare(p2.getPrice(), p1.getPrice());
                    if ("rating_desc".equalsIgnoreCase(sort)) return Double.compare(p2.getRating(), p1.getRating());
                    return p1.getId().compareTo(p2.getId());
                })
                .collect(Collectors.toList());
    }

    public Product getProductById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã sản phẩm không được rỗng");
        }
        Product p = dataStore.getProduct(id.trim());
        if (p == null) {
            throw new IllegalArgumentException("Không tìm thấy sản phẩm mã: " + id);
        }
        return p;
    }

    public Product createProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Thông tin sản phẩm không được null");
        }
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống");
        }
        if (product.getPrice() <= 0) {
            throw new IllegalArgumentException("Giá sản phẩm phải lớn hơn 0");
        }
        if (product.getStock() < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không được âm");
        }

        // Tự động sinh ID nếu chưa có
        if (product.getId() == null || product.getId().trim().isEmpty()) {
            product.setId("PROD-" + (dataStore.getAllProducts().size() + 1));
        } else {
            // Chặn trùng ID (khắc phục DEF-04 do Tester phát hiện)
            if (dataStore.getProduct(product.getId().trim()) != null) {
                throw new IllegalArgumentException("Mã sản phẩm đã tồn tại: " + product.getId());
            }
        }
        dataStore.addProduct(product);
        return product;
    }

    public Product updateProduct(String id, Product updated) {
        if (updated == null) {
            throw new IllegalArgumentException("Dữ liệu cập nhật không được null");
        }
        if (updated.getPrice() <= 0) {
            throw new IllegalArgumentException("Giá sản phẩm phải lớn hơn 0");
        }
        if (updated.getStock() < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không được âm");
        }

        Product existing = getProductById(id);
        if (updated.getName() != null && !updated.getName().trim().isEmpty()) {
            existing.setName(updated.getName().trim());
        }
        if (updated.getCategory() != null && !updated.getCategory().trim().isEmpty()) {
            existing.setCategory(updated.getCategory().trim());
        }
        existing.setPrice(updated.getPrice());
        existing.setStock(updated.getStock());
        if (updated.getDescription() != null) {
            existing.setDescription(updated.getDescription());
        }
        if (updated.getImageUrl() != null && !updated.getImageUrl().trim().isEmpty()) {
            existing.setImageUrl(updated.getImageUrl().trim());
        }
        return existing;
    }

    public void deleteProduct(String id) {
        Product existing = getProductById(id);
        dataStore.removeProduct(existing.getId());
    }
}
