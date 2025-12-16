package com.industriagafra.repository;

import com.industriagafra.entity.InventoryMovement;
import com.industriagafra.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
    List<InventoryMovement> findByProduct(Product product);
}