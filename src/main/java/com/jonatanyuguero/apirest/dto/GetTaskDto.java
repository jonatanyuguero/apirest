package com.jonatanyuguero.apirest.dto;

import com.jonatanyuguero.apirest.model.Priority;
import com.jonatanyuguero.apirest.model.Task;
import com.jonatanyuguero.apirest.users.NewUserResponse;

import java.time.LocalDateTime;
import java.util.List;

/** DTO de respuesta para Task. Evita exponer la entidad JPA directamente (proxies Lazy, relaciones circulares) y controla exactamente qué campos recibe el cliente, incluyendo author como NewUserResponse en lugar de User. */
public record GetTaskDto(
        Long id,
        String title,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deadline,
        Boolean completed,
        Priority priority,
        NewUserResponse author,
        CategoryDto category,
        List<TagDto> tags
) {
    public static GetTaskDto of(Task t) {
        return new GetTaskDto(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getCreatedAt(),
                t.getUpdatedAt(),
                t.getDeadline(),
                t.isCompleted(),
                t.getPriority(),
                NewUserResponse.of(t.getAuthor()),
                t.getCategory() == null ? null : CategoryDto.of(t.getCategory()),
                t.getTags() == null ? List.of() : t.getTags().stream().map(TagDto::of).toList()
        );
    }
}
