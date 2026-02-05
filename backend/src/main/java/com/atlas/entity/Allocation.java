package com.atlas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Project project;

    @Column(name = "confirmed_assignment")
    private String confirmedAssignment;

    @Column(name = "prospect_assignment")
    private String prospectAssignment;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    // Monthly allocation values: 1, 0.5, 0.25, "B" (Bench), "P" (Prospect)
    @Column(name = "jan_alloc")
    private String janAllocation;

    @Column(name = "feb_alloc")
    private String febAllocation;

    @Column(name = "mar_alloc")
    private String marAllocation;

    @Column(name = "apr_alloc")
    private String aprAllocation;

    @Column(name = "may_alloc")
    private String mayAllocation;

    @Column(name = "jun_alloc")
    private String junAllocation;

    @Column(name = "jul_alloc")
    private String julAllocation;

    @Column(name = "aug_alloc")
    private String augAllocation;

    @Column(name = "sep_alloc")
    private String sepAllocation;

    @Column(name = "oct_alloc")
    private String octAllocation;

    @Column(name = "nov_alloc")
    private String novAllocation;

    @Column(name = "dec_alloc")
    private String decAllocation;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AllocationStatus status = AllocationStatus.ACTIVE;

    public enum AllocationStatus {
        ACTIVE, PENDING, COMPLETED
    }

    // Helper method to get allocation for a specific month (1-12)
    public String getAllocationForMonth(int month) {
        return switch (month) {
            case 1 -> janAllocation;
            case 2 -> febAllocation;
            case 3 -> marAllocation;
            case 4 -> aprAllocation;
            case 5 -> mayAllocation;
            case 6 -> junAllocation;
            case 7 -> julAllocation;
            case 8 -> augAllocation;
            case 9 -> sepAllocation;
            case 10 -> octAllocation;
            case 11 -> novAllocation;
            case 12 -> decAllocation;
            default -> null;
        };
    }

    // Helper method to set allocation for a specific month (1-12)
    public void setAllocationForMonth(int month, String value) {
        switch (month) {
            case 1 -> janAllocation = value;
            case 2 -> febAllocation = value;
            case 3 -> marAllocation = value;
            case 4 -> aprAllocation = value;
            case 5 -> mayAllocation = value;
            case 6 -> junAllocation = value;
            case 7 -> julAllocation = value;
            case 8 -> augAllocation = value;
            case 9 -> sepAllocation = value;
            case 10 -> octAllocation = value;
            case 11 -> novAllocation = value;
            case 12 -> decAllocation = value;
        }
    }
}
