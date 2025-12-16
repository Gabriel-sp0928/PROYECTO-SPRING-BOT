package com.industriagafra.service;

import com.industriagafra.entity.*;
import com.industriagafra.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class QuoteService implements IQuoteService {

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private QuoteDetailRepository quoteDetailRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private SaleDetailRepository saleDetailRepository;

    @Transactional
    public Quote createQuote(User user, List<QuoteDetail> details) {
        Quote quote = new Quote();
        quote.setUser(user);
        quote.setDate(LocalDateTime.now());
        quote.setStatus(QuoteStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        for (QuoteDetail detail : details) {
            detail.setQuote(quote);
            total = total.add(detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));
        }
        quote.setTotal(total);

        // Calculate discount: 20% if more than 10 products
        int totalProducts = details.stream().mapToInt(QuoteDetail::getQuantity).sum();
        if (totalProducts > 10) {
            quote.setDiscount(total.multiply(BigDecimal.valueOf(0.20)));
        }

        // Calculate tax (IVA 19%)
        BigDecimal subtotalAfterDiscount = total.subtract(quote.getDiscount());
        quote.setTax(subtotalAfterDiscount.multiply(BigDecimal.valueOf(0.19)));

        quote = quoteRepository.save(quote);
        for (QuoteDetail detail : details) {
            quoteDetailRepository.save(detail);
        }

        return quote;
    }

    public List<Quote> findByUser(User user) {
        return quoteRepository.findByUser(user);
    }

    public Optional<Quote> findById(Long id) {
        return quoteRepository.findById(id);
    }

    @Transactional
    public Quote approveQuote(Long quoteId) {
        Optional<Quote> quoteOpt = quoteRepository.findById(quoteId);
        if (quoteOpt.isPresent()) {
            Quote quote = quoteOpt.get();
            // Validate stock before creating sale
            List<QuoteDetail> details = quoteDetailRepository.findByQuote(quote);
            for (QuoteDetail detail : details) {
                if (detail.getProduct() == null || detail.getProduct().getId() == null) {
                    throw new IllegalStateException("Producto inválido en la cotización");
                }
                var pOpt = productService.findById(detail.getProduct().getId());
                if (pOpt.isEmpty()) {
                    throw new IllegalStateException("Producto no encontrado: id=" + detail.getProduct().getId());
                }
                var product = pOpt.get();
                if (product.getStock() < detail.getQuantity()) {
                    throw new IllegalStateException("Stock insuficiente para '" + product.getName() + "' (disponible: " + product.getStock() + ", requerido: " + detail.getQuantity() + ")");
                }
            }

            quote.setStatus(QuoteStatus.APPROVED);
            quote = quoteRepository.save(quote);

            // Create sale and sale details
            // (details variable already obtained above)

            Sale sale = new Sale();
            sale.setQuote(quote);
            sale.setDate(LocalDateTime.now());

            // compute totals from details
            BigDecimal computed = BigDecimal.ZERO;
            for (QuoteDetail detail : details) {
                BigDecimal unit = detail.getPrice() == null ? BigDecimal.ZERO : detail.getPrice();
                BigDecimal subtotal = unit.multiply(BigDecimal.valueOf(detail.getQuantity()));
                computed = computed.add(subtotal);
            }

            BigDecimal finalTotal = computed.add(quote.getTax() == null ? BigDecimal.ZERO : quote.getTax())
                    .subtract(quote.getDiscount() == null ? BigDecimal.ZERO : quote.getDiscount());
            sale.setTotal(finalTotal);
            sale = saleRepository.save(sale);

            for (QuoteDetail detail : details) {
                // persist sale detail
                SaleDetail sd = new SaleDetail();
                sd.setSale(sale);
                sd.setProduct(detail.getProduct());
                sd.setQuantity(detail.getQuantity());
                sd.setUnitPrice(detail.getPrice());
                BigDecimal unit = detail.getPrice() == null ? BigDecimal.ZERO : detail.getPrice();
                sd.setSubtotal(unit.multiply(BigDecimal.valueOf(detail.getQuantity())));
                saleDetailRepository.save(sd);

                // Deduct inventory
                productService.updateStock(detail.getProduct().getId(), -detail.getQuantity());

                // Record inventory movement
                InventoryMovement movement = new InventoryMovement();
                movement.setProduct(detail.getProduct());
                movement.setType(MovementType.OUT);
                movement.setQuantity(detail.getQuantity());
                movement.setDate(LocalDateTime.now());
                movement.setReason("Sale from quote " + quote.getId());
                inventoryMovementRepository.save(movement);
            }

            return quote;
        }
        return null;
    }

    public Quote rejectQuote(Long quoteId) {
        Optional<Quote> quoteOpt = quoteRepository.findById(quoteId);
        if (quoteOpt.isPresent()) {
            Quote quote = quoteOpt.get();
            quote.setStatus(QuoteStatus.REJECTED);
            return quoteRepository.save(quote);
        }
        return null;
    }

    public List<Quote> findAll() {
        return quoteRepository.findAll();
    }

    @Override
    public Quote save(Quote entity) {
        return quoteRepository.save(entity);
    }

    @Override
    public void deleteById(Long id) {
        quoteRepository.deleteById(id);
    }
    @Override
    public List<Quote> findByStatus(java.lang.Enum status) {
        if (status instanceof QuoteStatus) {
            return quoteRepository.findByStatus((QuoteStatus) status);
        }
        return List.of();
    }
}