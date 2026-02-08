package com.atlas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @OneToMany(mappedBy = "allocation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<MonthlyAllocation> monthlyAllocations = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AllocationStatus status = AllocationStatus.ACTIVE;

    public enum AllocationStatus {
        ACTIVE, PROSPECT
    }

    // Helper method to add a monthly allocation
    public void addMonthlyAllocation(MonthlyAllocation monthlyAllocation) {
        monthlyAllocations.add(monthlyAllocation);
        monthlyAllocation.setAllocation(this);
    }

    // Helper method to remove a monthly allocation
    public void removeMonthlyAllocation(MonthlyAllocation monthlyAllocation) {
        monthlyAllocations.remove(monthlyAllocation);
        monthlyAllocation.setAllocation(null);
    }

    // Helper method to get allocation for a specific year and month
    public Double getAllocationForYearMonth(int year, int month) {
        return monthlyAllocations.stream()
                .filter(ma -> ma.getYear() == year && ma.getMonth() == month)
                .map(MonthlyAllocation::getPercentage)
                .findFirst()
                .orElse(null);
    }

    // Helper method to set allocation for a specific year and month
    public void setAllocationForYearMonth(int year, int month, Double percentage) {
        MonthlyAllocation existing = monthlyAllocations.stream()
                .filter(ma -> ma.getYear() == year && ma.getMonth() == month)
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setPercentage(percentage);
        } else {
            MonthlyAllocation newAllocation = MonthlyAllocation.builder()
                    .allocation(this)
                    .year(year)
                    .month(month)
                    .percentage(percentage)
                    .build();
            monthlyAllocations.add(newAllocation);
        }
    }
}
