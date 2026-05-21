package com.jonatanyuguero.apirest.users;

/** DTO de respuesta para User. Evita exponer la contraseña hasheada y campos internos de la entidad JPA en las respuestas de la API. */
public record NewUserResponse(
        Long id,
        String username,
        String email,
        String fullname,
        UserRole role
) {

    public static NewUserResponse of(User user) {
        if (user == null) return null;
        return new NewUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullname(),
                user.getRole()
        );
    }
}
