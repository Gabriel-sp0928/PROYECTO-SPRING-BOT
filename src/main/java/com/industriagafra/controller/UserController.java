package com.industriagafra.controller;

import com.industriagafra.entity.User;
import com.industriagafra.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAll() {
        List<User> users = userService.findAll();
        users.forEach(u -> u.setPassword(null));
        return users;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        Optional<User> u = userService.findById(id);
        return u.map(user -> {
            user.setPassword(null);
            return ResponseEntity.ok(user);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> create(@RequestBody User user) {
        User saved = userService.save(user);
        saved.setPassword(null);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> update(@PathVariable Long id, @RequestBody User incoming) {
        Optional<User> existing = userService.findById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();
        User u = existing.get();
        u.setEmail(incoming.getEmail());
        u.setName(incoming.getName());
        u.setRole(incoming.getRole());
        u.setEnabled(incoming.isEnabled());
        if (incoming.getPassword() != null && !incoming.getPassword().isBlank()) {
            u.setPassword(passwordEncoder.encode(incoming.getPassword()));
        }
        User saved = userService.save(u);
        saved.setPassword(null);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
