package com.jonatanyuguero.apirest.dto;

/** DTO de entrada para crear o editar un tag. Recibe solo el nombre; el author se asigna automáticamente desde el usuario autenticado en el servicio. */
public record TagCommand(String name) {
}
