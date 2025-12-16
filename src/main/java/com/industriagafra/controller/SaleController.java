package com.industriagafra.controller;

import com.industriagafra.entity.Sale;
import com.industriagafra.entity.SaleDetail;
import com.industriagafra.service.ISaleService;
import com.industriagafra.service.IUserService;
import com.industriagafra.service.IQuoteService;
import com.industriagafra.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    @Autowired
    private ISaleService saleService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IQuoteService quoteService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LOGISTICS') or hasRole('CLIENT')")
    public List<Sale> getAll(Authentication authentication) {
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"))) {
            List<Sale> all = saleService.findAll();
            return all.stream().filter(s -> s.getQuote() != null && s.getQuote().getUser() != null && s.getQuote().getUser().getUsername().equals(authentication.getName())).toList();
        }
        return saleService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LOGISTICS') or hasRole('CLIENT')")
    public ResponseEntity<Sale> getById(@PathVariable Long id, Authentication authentication) {
        Optional<Sale> s = saleService.findById(id);
        if (s.isEmpty()) return ResponseEntity.notFound().build();
        Sale sale = s.get();
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"))) {
            Optional<User> user = userService.findByUsername(authentication.getName());
            if (user.isEmpty() || sale.getQuote() == null || sale.getQuote().getUser() == null || !sale.getQuote().getUser().getId().equals(user.get().getId())) {
                return ResponseEntity.status(403).build();
            }
        }
        return ResponseEntity.ok(sale);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','LOGISTICS','CLIENT')")
    public ResponseEntity<Sale> create(@RequestBody Sale sale, Authentication authentication) {
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"))) {
            // client can only create sale for their own quote (provide quote.id in payload)
            if (sale.getQuote() == null || sale.getQuote().getId() == null) return ResponseEntity.badRequest().build();
            Optional<User> user = userService.findByUsername(authentication.getName());
            if (user.isEmpty()) return ResponseEntity.status(403).build();
            var qOpt = quoteService.findById(sale.getQuote().getId());
            if (qOpt.isEmpty() || qOpt.get().getUser() == null || !qOpt.get().getUser().getId().equals(user.get().getId())) {
                return ResponseEntity.status(403).build();
            }
            // ensure sale references the full quote entity
            sale.setQuote(qOpt.get());
        }
        if (sale.getDate() == null) sale.setDate(LocalDateTime.now());
        Sale saved = saleService.save(sale);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LOGISTICS')")
    public ResponseEntity<Sale> update(@PathVariable Long id, @RequestBody Sale sale) {
        return saleService.findById(id).map(existing -> {
            existing.setDate(sale.getDate() == null ? existing.getDate() : sale.getDate());
            existing.setTotal(sale.getTotal());
            Sale saved = saleService.save(existing);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LOGISTICS','CLIENT')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        Optional<Sale> s = saleService.findById(id);
        if (s.isEmpty()) return ResponseEntity.notFound().build();
        Sale sale = s.get();
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"))) {
            Optional<User> user = userService.findByUsername(authentication.getName());
            if (user.isEmpty() || sale.getQuote() == null || sale.getQuote().getUser() == null || !sale.getQuote().getUser().getId().equals(user.get().getId())) {
                return ResponseEntity.status(403).build();
            }
        }
        saleService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
