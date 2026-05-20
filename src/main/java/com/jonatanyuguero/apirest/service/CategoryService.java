package com.jonatanyuguero.apirest.service;

import com.jonatanyuguero.apirest.dto.CategoryCommand;
import com.jonatanyuguero.apirest.error.CategoryNotFoundException;
import com.jonatanyuguero.apirest.model.Category;
import com.jonatanyuguero.apirest.repos.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    public Category save(CategoryCommand cmd) {
        return categoryRepository.save(
                Category.builder().title(cmd.title()).build()
        );
    }

    public Category edit(Long id, CategoryCommand cmd) {
        return categoryRepository.findById(id)
                .map(c -> {
                    c.setTitle(cmd.title());
                    return categoryRepository.save(c);
                })
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    public void delete(Long id) {
        if (!categoryRepository.existsById(id))
            throw new CategoryNotFoundException(id);
        categoryRepository.deleteById(id);
    }
}
