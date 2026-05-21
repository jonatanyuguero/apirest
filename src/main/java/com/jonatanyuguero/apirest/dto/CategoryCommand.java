package com.jonatanyuguero.apirest.dto;

/** DTO de entrada para crear o editar una categoría. Recibe solo el campo editable (title), separando el contrato de entrada de la entidad persistida. */
public record CategoryCommand(String title) {
}
