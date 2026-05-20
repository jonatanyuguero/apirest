package com.jonatanyuguero.apirest.error;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("No hay un usuario con ese ID: %d".formatted(id));
    }

    public UserNotFoundException(String username) {
        super("No hay un usuario con ese username: " + username);
    }
}
