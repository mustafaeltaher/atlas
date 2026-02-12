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

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "skill_level", columnDefinition = "skill_level_type")
    private SkillLevel skillLevel;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "skill_grade", columnDefinition = "skill_grade_type")
    private SkillGrade skillGrade;

    public enum SkillLevel {
        PRIMARY, SECONDARY
    }

    public enum SkillGrade {
        ADVANCED, INTERMEDIATE, BEGINNER
    }
}
