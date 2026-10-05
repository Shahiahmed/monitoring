package com.example.monitoring.dto;

import lombok.Data;
import java.util.List;

@Data
public class IncidentRequest {
    private Long failureTypeId;
    private Long locationId;
    private List<Long> isIds;
    private Boolean fixed;
    private Boolean emptyTime;
    private Boolean includeAvailability;
    private String inMessage;
    private String outMessage;
    private String act;
    private String problem;
    private String solution;
    private Long sourcePrtgId;
    private List<IntervalRequest> intervals;
}
