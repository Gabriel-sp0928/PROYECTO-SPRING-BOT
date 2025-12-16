package com.industriagafra.service;

import com.industriagafra.entity.User;
import java.util.Optional;

public interface IUserService extends CrudService<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
