package com.atlas.repository;

import com.atlas.entity.MonthlyAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyAllocationRepository extends JpaRepository<MonthlyAllocation, Long> {

    List<MonthlyAllocation> findByAllocationId(Long allocationId);

    List<MonthlyAllocation> findByAllocationIdAndYear(Long allocationId, Integer year);

    Optional<MonthlyAllocation> findByAllocationIdAndYearAndMonth(Long allocationId, Integer year, Integer month);

    @Query("SELECT ma FROM MonthlyAllocation ma WHERE ma.allocation.id IN :allocationIds")
    List<MonthlyAllocation> findByAllocationIdIn(@Param("allocationIds") List<Long> allocationIds);

    @Query("SELECT ma FROM MonthlyAllocation ma WHERE ma.allocation.id IN :allocationIds AND ma.year = :year AND ma.month = :month")
    List<MonthlyAllocation> findByAllocationIdsAndYearAndMonth(
            @Param("allocationIds") List<Long> allocationIds,
            @Param("year") Integer year,
            @Param("month") Integer month);

    @Query("SELECT ma FROM MonthlyAllocation ma WHERE ma.year = :year AND ma.month = :month")
    List<MonthlyAllocation> findByYearAndMonth(@Param("year") Integer year, @Param("month") Integer month);

    @Modifying
    @Transactional
    void deleteByAllocationId(Long allocationId);
}
