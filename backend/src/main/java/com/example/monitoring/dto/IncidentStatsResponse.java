package com.example.monitoring.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class IncidentStatsResponse {

    private List<TypeCount> byType;
    private List<MonthCount> byMonth;
    private List<IsAvailability> byIsAvailability;
    private long totalDowntimeMinutes;

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

    @Data
    @Builder
    public static class IsAvailability {
        private long id;
        private String nameRu;
        private int count;
        private long totalDowntimeMinutes;
        private double availabilityPercent;
    }
}
