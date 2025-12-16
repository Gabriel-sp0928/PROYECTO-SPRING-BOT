package com.industriagafra.service;

import com.industriagafra.entity.Sale;
import com.industriagafra.repository.SaleDetailRepository;
import com.industriagafra.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SaleService implements ISaleService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleDetailRepository saleDetailRepository;

    @Override
    public Sale save(Sale entity) {
        return saleRepository.save(entity);
    }

    @Override
    public List<Sale> findAll() {
        return saleRepository.findAll();
    }

    @Override
    public Optional<Sale> findById(Long id) {
        return saleRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        saleDetailRepository.findAll().stream()
                .filter(d -> d.getSale() != null && d.getSale().getId().equals(id))
                .forEach(d -> saleDetailRepository.deleteById(d.getId()));
        saleRepository.deleteById(id);
    }

    @Override
    public List<Sale> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return saleRepository.findByDateRange(start, end);
    }
}
