package com.bits.school.equipment.dao;

import com.bits.school.equipment.entity.User;

import java.util.Optional;
import java.util.List;

public interface UserDao {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    List<User> findAll();
}

