package com.atlas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "employees_skills")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Skill skill;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_level")
    private SkillLevel skillLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_grade")
    private SkillGrade skillGrade;

    public enum SkillLevel {
        PRIMARY, SECONDARY
    }

    public enum SkillGrade {
        ADVANCED, INTERMEDIATE, BEGINNER
    }
}
