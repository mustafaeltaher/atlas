package com.atlas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "monthly_allocations", uniqueConstraints = @UniqueConstraint(columnNames = { "allocation_id", "year",
        "month" }))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyAllocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "allocation_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Allocation allocation;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer month; // 1-12

    @Column(nullable = false)
    private Double percentage; // 0.25, 0.5, 0.75, 1.0
}
