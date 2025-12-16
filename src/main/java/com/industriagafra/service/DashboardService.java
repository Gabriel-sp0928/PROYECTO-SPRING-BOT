package com.industriagafra.service;

import com.industriagafra.entity.QuoteStatus;
import com.industriagafra.repository.ProductRepository;
import com.industriagafra.repository.QuoteRepository;
import com.industriagafra.repository.SaleRepository;
import com.industriagafra.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.industriagafra.repository.SaleDetailRepository saleDetailRepository;

    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        // Total sales this month
        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = YearMonth.now().atEndOfMonth().atTime(23, 59, 59);
        BigDecimal monthlySales = saleRepository.getTotalSalesBetweenDates(startOfMonth, endOfMonth);
        data.put("monthlySales", monthlySales != null ? monthlySales : BigDecimal.ZERO);

        // Total quotes approved
        long approvedQuotes = quoteRepository.countByStatus(QuoteStatus.APPROVED);
        data.put("approvedQuotes", approvedQuotes);

        // Total quotes rejected
        long rejectedQuotes = quoteRepository.countByStatus(QuoteStatus.REJECTED);
        data.put("rejectedQuotes", rejectedQuotes);

        // Low stock products (less than 10)
        long lowStockProducts = productRepository.findByStockLessThan(10).size();
        data.put("lowStockProducts", lowStockProducts);

        // Total clients
        long totalClients = userRepository.findAll().stream()
                .filter(user -> user.getRole().name().equals("CLIENT"))
                .count();
        data.put("totalClients", totalClients);

        // Best selling products (aggregate by product name from sale details)
        var allDetails = saleDetailRepository.findAll();
        java.util.Map<String, Integer> counts = new java.util.HashMap<>();
        for(var d : allDetails){
            var name = (d.getProduct()!=null && d.getProduct().getName()!=null) ? d.getProduct().getName() : "";
            counts.put(name, counts.getOrDefault(name,0) + (d.getQuantity()==null?0:d.getQuantity()));
        }
        java.util.List<java.util.Map<String,Object>> top = new java.util.ArrayList<>();
        counts.entrySet().stream().sorted((a,b)->Integer.compare(b.getValue(), a.getValue())).limit(10).forEach(e->{
            var m = new java.util.HashMap<String,Object>();
            m.put("name", e.getKey());
            m.put("count", e.getValue());
            top.add(m);
        });
        data.put("bestSellingProducts", top);

        return data;
    }
}