package com.jonatanyuguero.apirest.users;

/** DTO de entrada para el registro. Recibe solo los campos necesarios; el rol se asigna automáticamente como USER y el id lo genera la BD. */
public record NewUserCommand(
        String username,
        String email,
        String password,
        String fullname
) {
}
