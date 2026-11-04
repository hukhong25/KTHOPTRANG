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
                .filter(p -> category == null || category.equalsIgnoreCase("ALL") || p.getCategory().equalsIgnoreCase(category))
                .filter(p -> search == null || search.trim().isEmpty() || 
                        p.getName().toLowerCase().contains(search.toLowerCase()) || 
                        p.getDescription().toLowerCase().contains(search.toLowerCase()))
                .sorted((p1, p2) -> {
                    if ("price_asc".equalsIgnoreCase(sort)) return Double.compare(p1.getPrice(), p2.getPrice());
                    if ("price_desc".equalsIgnoreCase(sort)) return Double.compare(p2.getPrice(), p1.getPrice());
                    if ("rating_desc".equalsIgnoreCase(sort)) return Double.compare(p2.getRating(), p1.getRating());
                    return p1.getId().compareTo(p2.getId());
                })
                .collect(Collectors.toList());
    }

    public Product getProductById(String id) {
        Product p = dataStore.getProduct(id);
        if (p == null) {
            throw new IllegalArgumentException("Không tìm thấy sản phẩm mã: " + id);
        }
        return p;
    }

    public Product createProduct(Product product) {
        if (product.getId() == null || product.getId().trim().isEmpty()) {
            product.setId("PROD-" + (dataStore.getAllProducts().size() + 1));
        }
        dataStore.addProduct(product);
        return product;
    }

    public Product updateProduct(String id, Product updated) {
        Product existing = getProductById(id);
        existing.setName(updated.getName());
        existing.setCategory(updated.getCategory());
        existing.setPrice(updated.getPrice());
        existing.setStock(updated.getStock());
        existing.setDescription(updated.getDescription());
        if (updated.getImageUrl() != null && !updated.getImageUrl().isEmpty()) {
            existing.setImageUrl(updated.getImageUrl());
        }
        return existing;
    }

    public void deleteProduct(String id) {
        dataStore.removeProduct(id);
    }
}
