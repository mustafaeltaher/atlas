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
    private String oracleId;

    @Column(nullable = false)
    private String name;

    private String gender;

    @Column(nullable = false)
    private String grade;

    @Column(name = "job_level")
    private String jobLevel;

    private String title;

    @Column(name = "primary_skill")
    private String primarySkill;

    @Column(name = "secondary_skill")
    private String secondarySkill;

    @Column(name = "hiring_type")
    private String hiringType;

    private String location;

    @Column(name = "legal_entity")
    private String legalEntity;

    @Column(name = "cost_center")
    private String costCenter;

    private String nationality;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "resignation_date")
    private LocalDate resignationDate;

    @Column(name = "reason_of_leave")
    private String reasonOfLeave;

    @Column(unique = true)
    private String email;

    @Column(name = "parent_tower")
    private String parentTower;

    private String tower;

    @Column(name = "future_manager")
    private String futureManager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Employee manager;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    public enum EmployeeStatus {
        ACTIVE, MATERNITY, LONG_LEAVE, RESIGNED
    }
}
