package com.industriagafra.repository;

import com.industriagafra.entity.Quote;
import com.industriagafra.entity.QuoteStatus;
import com.industriagafra.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface QuoteRepository extends JpaRepository<Quote, Long> {
    List<Quote> findByUser(User user);
    List<Quote> findByStatus(QuoteStatus status);

    @Query("SELECT q FROM Quote q WHERE q.date BETWEEN :startDate AND :endDate")
    List<Quote> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COUNT(q) FROM Quote q WHERE q.status = :status")
    long countByStatus(QuoteStatus status);
}