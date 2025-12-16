package com.industriagafra.service;

import java.util.List;
import java.util.Optional;

public interface CrudService<T, ID> {
    T save(T entity);
    List<T> findAll();
    Optional<T> findById(ID id);
    void deleteById(ID id);
}
