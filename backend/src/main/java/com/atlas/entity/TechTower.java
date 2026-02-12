package com.atlas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "tech_towers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechTower {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_tower_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TechTower parentTower;
}
