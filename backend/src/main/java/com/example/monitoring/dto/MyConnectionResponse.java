package com.example.monitoring.dto;

import java.time.LocalDateTime;

public record MyConnectionResponse(
        Long id,
        LocalDateTime connectionDate,
        String serviceKey,
        String serviceOwner,
        String isOwner,
        String isClientMtzn,
        String smartBridgeTicket
) {}
