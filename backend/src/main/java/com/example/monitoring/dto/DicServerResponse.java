package com.example.monitoring.dto;

public record DicServerResponse(
        Long id,
        Boolean active,
        String description,
        String ip,
        Long envId,
        String envNameRu,
        Integer warnRam,
        Integer warnDisk,
        Integer critRam,
        Integer critDisk
) {
}
