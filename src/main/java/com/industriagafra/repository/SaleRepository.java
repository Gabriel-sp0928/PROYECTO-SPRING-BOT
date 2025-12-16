package com.industriagafra.repository;

import com.industriagafra.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    @Query("SELECT SUM(s.total) FROM Sale s WHERE s.date BETWEEN :startDate AND :endDate")
    BigDecimal getTotalSalesBetweenDates(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT s FROM Sale s WHERE s.date BETWEEN :startDate AND :endDate")
    List<Sale> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
}