package com.atlas.repository;

import com.atlas.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Integer> {

    List<Skill> findByTowerId(Integer towerId);

    Skill findByDescription(String description);
}
