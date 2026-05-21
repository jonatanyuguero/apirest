package com.jonatanyuguero.apirest.dto;

import com.jonatanyuguero.apirest.model.Category;

/** DTO de respuesta para Category. Desacopla la capa de presentación del modelo JPA y evita exponer anotaciones o relaciones internas de la entidad. */
public record CategoryDto(Long id, String title) {
    public static CategoryDto of(Category c) {
        return new CategoryDto(c.getId(), c.getTitle());
    }
}
