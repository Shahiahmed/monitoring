package com.example.monitoring.dto;

public record DicServerRequest(
        Long id,
        Boolean active,
        String description,
        String ip,
        Long envId,
        Integer warnRam,
        Integer warnDisk,
        Integer critRam,
        Integer critDisk
) {
}
