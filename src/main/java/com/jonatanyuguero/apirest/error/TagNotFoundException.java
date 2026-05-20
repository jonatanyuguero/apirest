package com.jonatanyuguero.apirest.error;

public class TagNotFoundException extends RuntimeException {
    public TagNotFoundException(Long id) {
        super("No hay un tag con ese ID: %d".formatted(id));
    }
}
