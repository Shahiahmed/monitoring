package com.example.monitoring.dto;

import lombok.Data;
import java.util.List;

@Data
public class WorkRequest {
    private Long dicJobId;
    private Long locationId;
    private List<Long> isIds;
    private Boolean emptyTime;
    private Boolean includeAvailability;
    private String inMessage;
    private String outMessage;
    private String solution;
    private Long sourcePrtgId;
    private List<IntervalRequest> intervals;
}
