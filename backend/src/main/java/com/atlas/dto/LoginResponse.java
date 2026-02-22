package com.atlas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String username;
    private String email;
    private Boolean isTopLevel;
    private String employeeName;
    private Long employeeId;
    private Boolean isImpersonating;
    private String impersonatorUsername;
}
