package com.jonatanyuguero.apirest.users;

public record NewUserCommand(
        String username,
        String email,
        String password,
        String fullname
) {
}
