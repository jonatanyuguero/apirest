package com.jonatanyuguero.apirest.users;

public record EditProfileCommand(
        String email,
        String fullname,
        String password
) {
}
