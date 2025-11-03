package com.bits.school.equipment.repository;

import com.bits.school.equipment.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}

