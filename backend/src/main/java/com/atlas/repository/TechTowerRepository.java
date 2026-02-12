package com.atlas.repository;

import com.atlas.entity.TechTower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechTowerRepository extends JpaRepository<TechTower, Integer> {

    List<TechTower> findByParentTowerIsNull();

    List<TechTower> findByParentTowerId(Integer parentTowerId);

    TechTower findByDescription(String description);
}
