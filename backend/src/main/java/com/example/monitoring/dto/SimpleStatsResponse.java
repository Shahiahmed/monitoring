package com.example.monitoring.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SimpleStatsResponse {
    private List<TypeCount> byType;
    private List<MonthCount> byMonth;

    @Data
    @Builder
    public static class TypeCount {
        private String name;
        private int count;
        private long totalMinutes;
    }

    @Data
    @Builder
    public static class MonthCount {
        private String month;
        private int count;
        private long totalMinutes;
    }
}
