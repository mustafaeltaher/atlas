package com.atlas.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class DelegateRequest {
    @NotEmpty
    private String delegateUsername; // Username or Email of the person to grant access to
}
