package com.isitempty.backend.parkinglot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "of")
public class MappingStatusRes {
    private long totalMappings;
    private long highConfidenceMappings;
    private long mediumConfidenceMappings;
    private long lowConfidenceMappings;
} 