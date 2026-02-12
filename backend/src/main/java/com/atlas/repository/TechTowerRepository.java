package com.atlas.repository;

import com.atlas.entity.TechTower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TechTowerRepository extends JpaRepository<TechTower, Integer> {
}
