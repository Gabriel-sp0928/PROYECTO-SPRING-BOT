package com.industriagafra.repository;

import com.industriagafra.entity.QuoteDetail;
import com.industriagafra.entity.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuoteDetailRepository extends JpaRepository<QuoteDetail, Long> {
    List<QuoteDetail> findByQuote(Quote quote);
}