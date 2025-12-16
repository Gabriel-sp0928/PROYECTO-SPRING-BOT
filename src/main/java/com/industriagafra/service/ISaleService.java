package com.industriagafra.service;

import com.industriagafra.entity.Sale;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ISaleService extends CrudService<Sale, Long> {
    List<Sale> findByDateRange(LocalDateTime start, LocalDateTime end);
}
