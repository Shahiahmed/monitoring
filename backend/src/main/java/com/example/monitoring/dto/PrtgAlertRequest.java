package com.example.monitoring.dto;

import lombok.Data;
import java.util.List;

@Data
public class PrtgAlertRequest {
    private String prtgStatus;
    private String inMessage;
    private String solution;
    private List<IntervalRequest> intervals;
}
