package com.jonatanyuguero.apirest.dto;

import com.jonatanyuguero.apirest.model.Tag;

/** DTO de respuesta para Tag. Evita exponer la relación ManyToOne con User (author) en las respuestas, devolviendo solo id y name. */
public record TagDto(Long id, String name) {
    public static TagDto of(Tag t) {
        return new TagDto(t.getId(), t.getName());
    }
}
