package com.atlas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    private Long id;
    private String oracleId;
    private String name;
    private String gender;
    private String grade;
    private String jobLevel;
    private String title;
    private String primarySkill;
    private String secondarySkill;
    private String hiringType;
    private String location;
    private String legalEntity;
    private String costCenter;
    private String nationality;
    private LocalDate hireDate;
    private LocalDate resignationDate;
    private String email;
    private String parentTower;
    private String tower;
    private String futureManager;
    private Long managerId;
    private String managerName;
    private Boolean isActive;

    // Current month allocation (calculated)
    private Double currentAllocation;
    private String allocationStatus; // ACTIVE, BENCH, PROSPECT
}
