package com.atlas.repository;

import com.atlas.entity.MonthlyAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyAllocationRepository extends JpaRepository<MonthlyAllocation, Long> {

    Optional<MonthlyAllocation> findByAllocationIdAndYearAndMonth(Long allocationId, Integer year, Integer month);

    @Query("SELECT ma FROM MonthlyAllocation ma WHERE ma.allocation.id IN :allocationIds AND ma.year = :year AND ma.month = :month")
    List<MonthlyAllocation> findByAllocationIdsAndYearAndMonth(
            @Param("allocationIds") List<Long> allocationIds,
            @Param("year") Integer year,
            @Param("month") Integer month);
}
