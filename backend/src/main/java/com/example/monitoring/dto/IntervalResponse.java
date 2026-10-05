package com.example.monitoring.dto;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Builder
public class IntervalResponse {
    private Long id;
    private OffsetDateTime dateFrom;
    private OffsetDateTime dateTo;
    private Integer diffMinutes;
}
