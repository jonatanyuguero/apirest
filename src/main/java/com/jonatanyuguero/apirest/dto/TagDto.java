package com.jonatanyuguero.apirest.dto;

import com.jonatanyuguero.apirest.model.Tag;

public record TagDto(Long id, String name) {
    public static TagDto of(Tag t) {
        return new TagDto(t.getId(), t.getName());
    }
}
