package com.industriagafra.service;

import com.industriagafra.entity.Product;
import java.util.List;

public interface IProductService extends CrudService<Product, Long> {
    List<Product> findAvailableProducts();
    List<Product> findLowStockProducts(int threshold);
    void updateStock(Long productId, int quantity);
}
