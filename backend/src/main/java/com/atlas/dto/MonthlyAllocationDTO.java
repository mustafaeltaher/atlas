package com.atlas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyAllocationDTO {
    private Long id;
    private Long allocationId;
    private Integer year;
    private Integer month;
    private Integer percentage;
}
