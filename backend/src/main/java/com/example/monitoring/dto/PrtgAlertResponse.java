package com.example.monitoring.dto;

import com.example.monitoring.entity.PrtgAlert;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class PrtgAlertResponse {
    private Long id;
    private String prtgStatus;
    private String inMessage;
    private String solution;
    private OffsetDateTime createdAt;
    private List<IntervalResponse> intervals;
    private Integer totalDiffMinutes;
    private Integer fileCount;

    public static PrtgAlertResponse from(PrtgAlert e, int fileCount) {
        List<IntervalResponse> ivs = e.getIntervals().stream()
            .map(i -> IntervalResponse.builder()
                .id(i.getId()).dateFrom(i.getDateFrom())
                .dateTo(i.getDateTo()).diffMinutes(i.getDiffMinutes())
                .build())
            .collect(Collectors.toList());
        int total = ivs.stream()
            .filter(i -> i.getDiffMinutes() != null)
            .mapToInt(IntervalResponse::getDiffMinutes).sum();
        return PrtgAlertResponse.builder()
            .id(e.getId()).prtgStatus(e.getPrtgStatus())
            .inMessage(e.getInMessage()).solution(e.getSolution())
            .createdAt(e.getCreatedAt())
            .intervals(ivs).totalDiffMinutes(total).fileCount(fileCount)
            .build();
    }
}
