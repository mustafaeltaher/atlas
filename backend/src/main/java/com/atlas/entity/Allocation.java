package com.atlas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "allocations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Allocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "confirmed_assignment")
    private String confirmedAssignment;

    @Column(name = "prospect_assignment")
    private String prospectAssignment;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    // Monthly utilization values: 1, 0.5, 0.25, "B" (Bench), "P" (Prospect)
    @Column(name = "jan_util")
    private String janUtilization;

    @Column(name = "feb_util")
    private String febUtilization;

    @Column(name = "mar_util")
    private String marUtilization;

    @Column(name = "apr_util")
    private String aprUtilization;

    @Column(name = "may_util")
    private String mayUtilization;

    @Column(name = "jun_util")
    private String junUtilization;

    @Column(name = "jul_util")
    private String julUtilization;

    @Column(name = "aug_util")
    private String augUtilization;

    @Column(name = "sep_util")
    private String sepUtilization;

    @Column(name = "oct_util")
    private String octUtilization;

    @Column(name = "nov_util")
    private String novUtilization;

    @Column(name = "dec_util")
    private String decUtilization;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AllocationStatus status = AllocationStatus.ACTIVE;

    public enum AllocationStatus {
        ACTIVE, PENDING, COMPLETED
    }

    // Helper method to get utilization for a specific month (1-12)
    public String getUtilizationForMonth(int month) {
        return switch (month) {
            case 1 -> janUtilization;
            case 2 -> febUtilization;
            case 3 -> marUtilization;
            case 4 -> aprUtilization;
            case 5 -> mayUtilization;
            case 6 -> junUtilization;
            case 7 -> julUtilization;
            case 8 -> augUtilization;
            case 9 -> sepUtilization;
            case 10 -> octUtilization;
            case 11 -> novUtilization;
            case 12 -> decUtilization;
            default -> null;
        };
    }

    // Helper method to set utilization for a specific month (1-12)
    public void setUtilizationForMonth(int month, String value) {
        switch (month) {
            case 1 -> janUtilization = value;
            case 2 -> febUtilization = value;
            case 3 -> marUtilization = value;
            case 4 -> aprUtilization = value;
            case 5 -> mayUtilization = value;
            case 6 -> junUtilization = value;
            case 7 -> julUtilization = value;
            case 8 -> augUtilization = value;
            case 9 -> sepUtilization = value;
            case 10 -> octUtilization = value;
            case 11 -> novUtilization = value;
            case 12 -> decUtilization = value;
        }
    }
}
