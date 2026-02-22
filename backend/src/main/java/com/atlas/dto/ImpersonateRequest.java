package com.atlas.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ImpersonateRequest {
    @NotEmpty
    private String targetUsername; // The user I want to become
}
