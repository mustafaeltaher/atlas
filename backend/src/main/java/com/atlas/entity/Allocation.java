package com.atlas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    @JoinColumn(name = "project_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Project project;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "allocation_type", columnDefinition = "allocation_type")
    @Builder.Default
    private AllocationType allocationType = AllocationType.PROJECT;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @OneToMany(mappedBy = "allocation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<MonthlyAllocation> monthlyAllocations = new ArrayList<>();

    public enum AllocationType {
        PROJECT, PROSPECT, VACATION, MATERNITY
    }

    // Helper method to get allocation for a specific year and month
    public Integer getAllocationForYearMonth(int year, int month) {
        return monthlyAllocations.stream()
                .filter(ma -> ma.getYear() == year && ma.getMonth() == month)
                .map(MonthlyAllocation::getPercentage)
                .findFirst()
                .orElse(null);
    }

    // Helper method to set allocation for a specific year and month
    public void setAllocationForYearMonth(int year, int month, Integer percentage) {
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
