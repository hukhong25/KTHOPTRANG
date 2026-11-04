package com.daovancong.techstore;

import com.daovancong.techstore.model.Product;
import com.daovancong.techstore.repository.DataStore;
import com.daovancong.techstore.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * KIỂM THỬ HỘP TRẮNG CHO PRODUCTSERVICE
 * Developer: Đào Văn Công (2023601884)
 */
class ProductServiceTest {

    private DataStore dataStore;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        dataStore = new DataStore();
        dataStore.initData();
        productService = new ProductService(dataStore);
    }

    @Test
    @DisplayName("WB-PRO-01 đến 04: Lọc sản phẩm theo danh mục hợp lệ và không phân biệt hoa thường")
    void testGetProducts_FilterCategory() {
        List<Product> laptops = productService.getProducts("LAPTOP", null, null);
        assertFalse(laptops.isEmpty());
        assertTrue(laptops.stream().allMatch(p -> p.getCategory().equalsIgnoreCase("LAPTOP")));

        List<Product> all = productService.getProducts("ALL", null, null);
        assertEquals(dataStore.getAllProducts().size(), all.size());

        List<Product> nonExistent = productService.getProducts("UNKNOWN_CATEGORY", null, null);
        assertTrue(nonExistent.isEmpty());
    }

    @Test
    @DisplayName("WB-PRO-05 đến 08: Tìm kiếm sản phẩm theo từ khóa tên hoặc mô tả")
    void testGetProducts_SearchKeyword() {
        List<Product> searchMac = productService.getProducts(null, "MacBook", null);
        assertFalse(searchMac.isEmpty());
        assertTrue(searchMac.get(0).getName().contains("MacBook"));

        List<Product> searchNotFound = productService.getProducts(null, "xyz999nonexistent", null);
        assertTrue(searchNotFound.isEmpty());
    }

    @Test
    @DisplayName("WB-PRO-09 & 10: Sắp xếp theo giá tăng dần và giảm dần")
    void testGetProducts_Sorting() {
        List<Product> sortedAsc = productService.getProducts("ALL", null, "price_asc");
        for (int i = 0; i < sortedAsc.size() - 1; i++) {
            assertTrue(sortedAsc.get(i).getPrice() <= sortedAsc.get(i + 1).getPrice());
        }

        List<Product> sortedDesc = productService.getProducts("ALL", null, "price_desc");
        for (int i = 0; i < sortedDesc.size() - 1; i++) {
            assertTrue(sortedDesc.get(i).getPrice() >= sortedDesc.get(i + 1).getPrice());
        }
    }

    @Test
    @DisplayName("WB-PRO-13 & 14: Lấy chi tiết sản phẩm theo ID hoặc ném ngoại lệ nếu không tìm thấy")
    void testGetProductById() {
        Product p = productService.getProductById("PROD-01");
        assertNotNull(p);
        assertEquals("PROD-01", p.getId());

        assertThrows(IllegalArgumentException.class, () -> productService.getProductById("PROD-NOT-EXIST"));
        assertThrows(IllegalArgumentException.class, () -> productService.getProductById("  "));
    }

    @Test
    @DisplayName("WB-PRO-15 & 16: Tạo sản phẩm mới và chặn trùng ID (DEF-04)")
    void testCreateProduct_Success_And_DuplicateId() {
        Product newP = new Product("PROD-TEST-99", "Chuột Dareu", "ACCESSORY", 300000.0, 50, 4.5, "", "Mô tả");
        Product created = productService.createProduct(newP);
        assertEquals("PROD-TEST-99", created.getId());

        // Thử tạo trùng ID -> ném IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(newP));
    }

    @Test
    @DisplayName("WB-PRO-17 & 19: Cập nhật thông tin sản phẩm và kiểm tra ràng buộc giá/kho")
    void testUpdateProduct() {
        Product updateInfo = new Product();
        updateInfo.setName("MacBook Pro M3 Pro 2024");
        updateInfo.setPrice(51000000.0);
        updateInfo.setStock(20);
        updateInfo.setCategory("LAPTOP");

        Product updated = productService.updateProduct("PROD-01", updateInfo);
        assertEquals("MacBook Pro M3 Pro 2024", updated.getName());
        assertEquals(51000000.0, updated.getPrice());
        assertEquals(20, updated.getStock());

        // Giá âm -> ném IllegalArgumentException
        updateInfo.setPrice(-100.0);
        assertThrows(IllegalArgumentException.class, () -> productService.updateProduct("PROD-01", updateInfo));
    }

    @Test
    @DisplayName("WB-PRO-20: Xóa sản phẩm khỏi kho hàng")
    void testDeleteProduct() {
        productService.deleteProduct("PROD-12");
        assertThrows(IllegalArgumentException.class, () -> productService.getProductById("PROD-12"));
    }
}
