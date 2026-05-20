package com.jonatanyuguero.apirest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jonatanyuguero.apirest.model.Priority;

import java.time.LocalDateTime;

public record EditTaskCommand(
        String title,
        String description,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime deadline,
        Boolean completed,
        Priority priority,
        Long categoryId
) {
}
