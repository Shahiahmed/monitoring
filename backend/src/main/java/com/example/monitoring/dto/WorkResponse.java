package com.example.monitoring.dto;

import com.example.monitoring.entity.Work;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class WorkResponse {
    private Long id;
    private Long dicJobId;
    private String dicJobNameRu;
    private Long locationId;
    private String locationNameRu;
    private List<Long> isIds;
    private List<String> isNamesRu;
    private Boolean emptyTime;
    private Boolean includeAvailability;
    private String inMessage;
    private String outMessage;
    private String solution;
    private Long sourcePrtgId;
    private OffsetDateTime createdAt;
    private List<IntervalResponse> intervals;
    private Integer totalDiffMinutes;
    private Integer fileCount;

    public static WorkResponse from(Work e, int fileCount) {
        List<IntervalResponse> ivs = e.getIntervals().stream()
            .map(i -> IntervalResponse.builder()
                .id(i.getId()).dateFrom(i.getDateFrom())
                .dateTo(i.getDateTo()).diffMinutes(i.getDiffMinutes())
                .build())
            .collect(Collectors.toList());
        int total = ivs.stream()
            .filter(i -> i.getDiffMinutes() != null)
            .mapToInt(IntervalResponse::getDiffMinutes).sum();
        return WorkResponse.builder()
            .id(e.getId())
            .dicJobId(e.getDicJob() != null ? e.getDicJob().getId() : null)
            .dicJobNameRu(e.getDicJob() != null ? e.getDicJob().getNameRu() : null)
            .locationId(e.getLocation() != null ? e.getLocation().getId() : null)
            .locationNameRu(e.getLocation() != null ? e.getLocation().getNameRu() : null)
            .isIds(e.getInformationSystems().stream().map(is -> is.getId()).collect(Collectors.toList()))
            .isNamesRu(e.getInformationSystems().stream().map(is -> is.getNameRu()).collect(Collectors.toList()))
            .emptyTime(e.getEmptyTime()).includeAvailability(e.getIncludeAvailability())
            .inMessage(e.getInMessage()).outMessage(e.getOutMessage()).solution(e.getSolution())
            .sourcePrtgId(e.getSourcePrtgId()).createdAt(e.getCreatedAt())
            .intervals(ivs).totalDiffMinutes(total).fileCount(fileCount)
            .build();
    }
}
