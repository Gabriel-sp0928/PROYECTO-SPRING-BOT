package com.industriagafra.controller;

import com.industriagafra.entity.Quote;
import com.industriagafra.entity.QuoteDetail;
import com.industriagafra.entity.User;
import com.industriagafra.service.IQuoteService;
import com.industriagafra.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/quotes")
public class QuoteController {

    @Autowired
    private IQuoteService quoteService;

    @Autowired
    private IUserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LOGISTICS')")
    public List<Quote> getAllQuotes() {
        return quoteService.findAll();
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CLIENT')")
    public List<Quote> getMyQuotes(Authentication authentication) {
        Optional<User> user = userService.findByUsername(authentication.getName());
        return user.map(quoteService::findByUser).orElse(List.of());
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public Quote createQuote(@RequestBody CreateQuoteRequest request, Authentication authentication) {
        Optional<User> user = userService.findByUsername(authentication.getName());
        if (user.isPresent()) {
            return quoteService.createQuote(user.get(), request.getDetails());
        }
        throw new RuntimeException("User not found");
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Quote> approveQuote(@PathVariable Long id, Authentication authentication) {
        Optional<User> user = userService.findByUsername(authentication.getName());
        if (user.isPresent()) {
            Optional<Quote> quote = quoteService.findById(id);
            if (quote.isPresent() && quote.get().getUser().getId().equals(user.get().getId())) {
                return ResponseEntity.ok(quoteService.approveQuote(id));
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Quote> rejectQuote(@PathVariable Long id, Authentication authentication) {
        Optional<User> user = userService.findByUsername(authentication.getName());
        if (user.isPresent()) {
            Optional<Quote> quote = quoteService.findById(id);
            if (quote.isPresent() && quote.get().getUser().getId().equals(user.get().getId())) {
                return ResponseEntity.ok(quoteService.rejectQuote(id));
            }
        }
        return ResponseEntity.badRequest().build();
    }

    public static class CreateQuoteRequest {
        private List<QuoteDetail> details;

        public List<QuoteDetail> getDetails() { return details; }
        public void setDetails(List<QuoteDetail> details) { this.details = details; }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LOGISTICS') or hasRole('CLIENT')")
    public ResponseEntity<Quote> getQuoteById(@PathVariable Long id, Authentication authentication) {
        var opt = quoteService.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Quote q = opt.get();
        // If client, ensure ownership
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"))) {
            Optional<User> user = userService.findByUsername(authentication.getName());
            if (user.isEmpty() || !q.getUser().getId().equals(user.get().getId())) {
                return ResponseEntity.status(403).build();
            }
        }
        return ResponseEntity.ok(q);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LOGISTICS')")
    public ResponseEntity<Quote> updateQuote(@PathVariable Long id, @RequestBody Quote quote) {
        return quoteService.findById(id).map(existing -> {
            existing.setStatus(quote.getStatus());
            existing.setDiscount(quote.getDiscount());
            existing.setTax(quote.getTax());
            existing.setTotal(quote.getTotal());
            existing.setDate(quote.getDate());
            Quote saved = quoteService.save(existing);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENT')")
    public ResponseEntity<Void> deleteQuote(@PathVariable Long id, Authentication authentication) {
        return quoteService.findById(id).map(q -> {
            // if client, ensure ownership
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"))) {
                Optional<User> user = userService.findByUsername(authentication.getName());
                if (user.isEmpty() || !q.getUser().getId().equals(user.get().getId())) {
                    return ResponseEntity.status(403).<Void>build();
                }
            }
            quoteService.deleteById(id);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}