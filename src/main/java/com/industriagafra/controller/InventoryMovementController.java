package com.industriagafra.controller;

import com.industriagafra.entity.InventoryMovement;
import com.industriagafra.entity.Product;
import com.industriagafra.repository.InventoryMovementRepository;
import com.industriagafra.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/inventory-movements")
public class InventoryMovementController {

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LOGISTICS')")
    public List<InventoryMovement> getAll() {
        return inventoryMovementRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LOGISTICS')")
    public ResponseEntity<InventoryMovement> getById(@PathVariable Long id) {
        Optional<InventoryMovement> m = inventoryMovementRepository.findById(id);
        return m.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    public static class CreateMovementRequest {
        public Long productId;
        public String type;
        public int quantity;
        public String reason;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LOGISTICS')")
    public ResponseEntity<InventoryMovement> create(@RequestBody CreateMovementRequest req) {
        Optional<Product> p = productRepository.findById(req.productId);
        if (p.isEmpty()) return ResponseEntity.badRequest().build();
        InventoryMovement m = new InventoryMovement();
        m.setProduct(p.get());
        m.setType(req.type == null ? null : Enum.valueOf(com.industriagafra.entity.MovementType.class, req.type));
        m.setQuantity(req.quantity);
        m.setReason(req.reason);
        m.setDate(LocalDateTime.now());
        InventoryMovement saved = inventoryMovementRepository.save(m);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LOGISTICS')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        inventoryMovementRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
