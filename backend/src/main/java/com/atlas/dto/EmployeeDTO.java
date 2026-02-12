package com.atlas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    private Long id;
    private Integer oracleId;
    private String name;
    private String gender;
    private String grade;
    private String jobLevel;
    private String title;
    private String hiringType;
    private String location;
    private String legalEntity;
    private String costCenter;
    private String nationality;
    private LocalDate hireDate;
    private LocalDate resignationDate;
    private String reasonOfLeave;
    private String email;
    private Integer towerId;
    private String towerName;
    private String parentTowerName;
    private Long managerId;
    private String managerName;

    // Skills
    private List<EmployeeSkillDTO> skills;

    // Derived from allocation types
    private String status; // ACTIVE, MATERNITY, VACATION, RESIGNED
    // Total allocation for current month (sum across all projects)
    private Double totalAllocation;
    private String allocationStatus; // Derived: ACTIVE, BENCH, PROSPECT

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeSkillDTO {
        private String skillName;
        private String skillLevel; // PRIMARY, SECONDARY
        private String skillGrade; // ADVANCED, INTERMEDIATE, BEGINNER
    }
}
