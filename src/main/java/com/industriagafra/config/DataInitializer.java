package com.industriagafra.config;

import com.industriagafra.entity.Product;
import com.industriagafra.entity.Quote;
import com.industriagafra.entity.QuoteDetail;
import com.industriagafra.entity.Sale;
import com.industriagafra.entity.SaleDetail;
import com.industriagafra.entity.InventoryMovement;
import com.industriagafra.entity.Role;
import com.industriagafra.entity.User;
import java.util.List;
import com.industriagafra.repository.ProductRepository;
import com.industriagafra.repository.QuoteDetailRepository;
import com.industriagafra.repository.QuoteRepository;
import com.industriagafra.repository.SaleDetailRepository;
import com.industriagafra.repository.SaleRepository;
import com.industriagafra.repository.InventoryMovementRepository;
import com.industriagafra.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private QuoteDetailRepository quoteDetailRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleDetailRepository saleDetailRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Override
    public void run(String... args) throws Exception {
        // Ensure professional users exist (admin, client, logistics).
        upsertProfessional("admin", "admin@example.com", "Administrator", Role.ADMIN, "00000000", "Head Office", "CARD", "admin123");
        upsertProfessional("cliente", "client@example.com", "Cliente Demo", Role.CLIENT, "11111111", "Client Address", "CASH", "client123");
        upsertProfessional("logistics", "logistics@example.com", "Logistics", Role.LOGISTICS, "22222222", "Warehouse", "TRANSFER", "logi123");
    }

    private void upsertProfessional(String username, String email, String name, Role role, String document, String address, String paymentMethod, String rawPassword){
        var opt = userRepository.findByUsername(username);
        User u;
        if(opt.isPresent()){
            u = opt.get();
            // Update fields and ensure password is bcrypt-encoded for these professional accounts
            u.setEmail(email);
            u.setName(name);
            u.setRole(role);
            u.setEnabled(true);
            u.setDocumentNumber(document);
            u.setAddress(address);
            u.setPaymentMethod(paymentMethod);
            u.setPassword(passwordEncoder.encode(rawPassword));
        } else {
            u = new User();
            u.setUsername(username);
            u.setEmail(email);
            u.setName(name);
            u.setRole(role);
            u.setEnabled(true);
            u.setDocumentNumber(document);
            u.setAddress(address);
            u.setPaymentMethod(paymentMethod);
            u.setPassword(passwordEncoder.encode(rawPassword));
        }
        userRepository.save(u);
    }
}