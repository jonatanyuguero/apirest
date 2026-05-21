package com.jonatanyuguero.apirest.dto;

import com.jonatanyuguero.apirest.model.Priority;

import java.time.LocalDateTime;

/** DTO de entrada para crear o editar una tarea. Desacopla el contrato de entrada de la entidad JPA y permite que categoryId llegue como Long sin necesidad de enviar el objeto Category completo. */
public record EditTaskCommand(
        String title,
        String description,
        LocalDateTime deadline,
        Boolean completed,
        Priority priority,
        Long categoryId
) {
}
