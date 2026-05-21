package com.jonatanyuguero.apirest.users;

/** DTO de entrada para editar el perfil. Permite actualizar solo email, fullname y password, sin exponer ni permitir cambiar el username ni el rol. */
public record EditProfileCommand(
        String email,
        String fullname,
        String password
) {
}
