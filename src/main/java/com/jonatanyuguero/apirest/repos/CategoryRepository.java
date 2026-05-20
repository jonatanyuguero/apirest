package com.jonatanyuguero.apirest.repos;

import com.jonatanyuguero.apirest.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByTitleIgnoreCase(String title);
}
