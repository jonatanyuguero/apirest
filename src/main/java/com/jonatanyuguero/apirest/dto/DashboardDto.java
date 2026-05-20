package com.jonatanyuguero.apirest.dto;

import java.util.Map;

public record DashboardDto(
        long total,
        long completed,
        long pending,
        long overdue,
        Map<String, Long> byCategory,
        Map<String, Long> byTag,
        Map<String, Long> byPriority
) {
}
