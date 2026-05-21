package com.jonatanyuguero.apirest.dto;

import java.util.Map;

/** DTO de respuesta del dashboard. Agrega estadísticas calculadas en el servicio (totales, agrupaciones) sin exponer colecciones de entidades JPA. */
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
