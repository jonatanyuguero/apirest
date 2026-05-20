package com.jonatanyuguero.apirest.error;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(Long id) {
        super("No hay una categoría con ese ID: %d".formatted(id));
    }
}
