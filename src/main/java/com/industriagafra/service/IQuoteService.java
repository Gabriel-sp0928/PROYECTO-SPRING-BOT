package com.industriagafra.service;

import com.industriagafra.entity.Quote;
import com.industriagafra.entity.QuoteDetail;
import com.industriagafra.entity.User;
import java.util.List;
import java.util.Optional;

public interface IQuoteService extends CrudService<Quote, Long> {
    Quote createQuote(User user, List<QuoteDetail> details);
    List<Quote> findByUser(User user);
    Quote approveQuote(Long quoteId);
    Quote rejectQuote(Long quoteId);
    List<Quote> findByStatus(java.lang.Enum status);
}
