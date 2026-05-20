package com.jonatanyuguero.apirest.dto;

import com.jonatanyuguero.apirest.model.Category;

public record CategoryDto(Long id, String title) {
    public static CategoryDto of(Category c) {
        return new CategoryDto(c.getId(), c.getTitle());
    }
}
