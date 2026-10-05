package com.example.monitoring.dto;

import com.example.monitoring.entity.DicFailureType;

public record DicFailureTypeResponse(
        Long id,
        String nameRu,
        String nameKz,
        String nameEn
) {
    public static DicFailureTypeResponse from(DicFailureType e) {
        return new DicFailureTypeResponse(e.getId(), e.getNameRu(), e.getNameKz(), e.getNameEn());
    }
}
