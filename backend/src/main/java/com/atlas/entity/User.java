package com.atlas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Manager level derived from role (N1=highest to N4=lowest)
    @Column(name = "manager_level")
    private Integer managerLevel;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    public enum Role {
        SYSTEM_ADMIN(0), // Full system access
        EXECUTIVE(1), // N1 - Company-wide visibility
        HEAD(2), // N2 - Parent Tower visibility
        DEPARTMENT_MANAGER(3), // N3 - Tower visibility
        TEAM_LEAD(4); // N4 - Project visibility

        private final int level;

        Role(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }
}
