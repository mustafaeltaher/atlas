package com.atlas.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class DelegateResponse {
    private Long id;
    private String delegatorName;
    private String delegatorUsername;
    private String delegateName;
    private String delegateUsername;
    private LocalDateTime createdAt;
}
