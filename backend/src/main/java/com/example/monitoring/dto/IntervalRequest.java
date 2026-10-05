package com.example.monitoring.dto;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class IntervalRequest {
    private Long id;
    private OffsetDateTime dateFrom;
    private OffsetDateTime dateTo;
}
