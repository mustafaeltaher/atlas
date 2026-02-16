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
@Table(name = "employees")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "oracle_id", unique = true)
    private Integer oracleId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String email;

    private String location;

    private String nationality;

    @Enumerated(EnumType.STRING)
    @Column(name = "hiring_type")
    private HiringType hiringType;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "resignation_date")
    private LocalDate resignationDate;

    @Column(name = "reason_of_leave")
    private String reasonOfLeave;

    private String grade;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_level")
    private JobLevel jobLevel;

    private String title;

    @Column(name = "legal_entity")
    private String legalEntity;

    @Column(name = "cost_center")
    private String costCenter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tower")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TechTower tower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Employee manager;

    public enum Gender {
        MALE, FEMALE
    }

    public enum HiringType {
        FULL_TIME, PART_TIME
    }

    public enum JobLevel {
        ENTRY_LEVEL, MID_LEVEL, ADVANCED_MANAGER_LEVEL, EXECUTIVE_LEVEL
    }
}
