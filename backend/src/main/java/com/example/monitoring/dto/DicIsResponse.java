package com.example.monitoring.dto;

public record DicIsResponse(
        Long id,
        String nameEn,
        String nameKz,
        String nameRu,
        Long goId,
        String goNameRu,
        Integer sortOrder,
        Boolean includeInAvailability
) {
}
