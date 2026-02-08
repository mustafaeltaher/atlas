package com.atlas.specification;

import com.atlas.entity.Allocation;
import com.atlas.entity.Employee;
import com.atlas.entity.MonthlyAllocation;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmployeeSpecification {

    public static Specification<Employee> withFilters(
            String search,
            String tower,
            Long managerId,
            String status,
            List<Long> employeeIds) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Active Employees only
            predicates.add(cb.isTrue(root.get("isActive")));

            // Employee IDs filter (for non-admin RBAC)
            if (employeeIds != null && !employeeIds.isEmpty()) {
                predicates.add(root.get("id").in(employeeIds));
            }

            // Tower filter
            if (tower != null && !tower.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("tower"), tower));
            }

            // Manager filter
            if (managerId != null) {
                predicates.add(cb.equal(root.get("manager").get("id"), managerId));
            }

            // Search filter
            if (search != null && !search.trim().isEmpty()) {
                String searchLike = "%" + search.toLowerCase() + "%";
                Predicate nameLike = cb.like(cb.lower(root.get("name")), searchLike);
                Predicate skillLike = cb.like(cb.lower(root.get("primarySkill")), searchLike);
                Predicate towerLike = cb.like(cb.lower(root.get("tower")), searchLike);
                Predicate emailLike = cb.like(cb.lower(root.get("email")), searchLike);
                predicates.add(cb.or(nameLike, skillLike, towerLike, emailLike));
            }

            // Status filter (handles both Employee status and Allocation-based status)
            if (status != null && !status.trim().isEmpty()) {
                // Check if it's an employee status (MATERNITY, LONG_LEAVE, RESIGNED)
                if ("MATERNITY".equalsIgnoreCase(status)) {
                    predicates.add(cb.equal(root.get("status"), Employee.EmployeeStatus.MATERNITY));
                } else if ("LONG_LEAVE".equalsIgnoreCase(status)) {
                    predicates.add(cb.equal(root.get("status"), Employee.EmployeeStatus.LONG_LEAVE));
                } else if ("RESIGNED".equalsIgnoreCase(status)) {
                    predicates.add(cb.equal(root.get("status"), Employee.EmployeeStatus.RESIGNED));
                } else {
                    // For allocation-based statuses (ACTIVE, BENCH, PROSPECT),
                    // only consider employees with ACTIVE employee status
                    predicates.add(cb.equal(root.get("status"), Employee.EmployeeStatus.ACTIVE));

                    int currentYear = LocalDate.now().getYear();
                    int currentMonth = LocalDate.now().getMonthValue();

                    if ("BENCH".equalsIgnoreCase(status)) {
                        // BENCH = No active allocation (no positive percentage in current month/year)
                        Subquery<Long> activeAllocationSubquery = query.subquery(Long.class);
                        Root<Allocation> allocRoot = activeAllocationSubquery.from(Allocation.class);
                        Join<Allocation, MonthlyAllocation> maJoin = allocRoot.join("monthlyAllocations");
                        activeAllocationSubquery.select(allocRoot.get("employee").get("id"));

                        activeAllocationSubquery.where(cb.and(
                                cb.equal(allocRoot.get("employee"), root),
                                cb.equal(allocRoot.get("status"), Allocation.AllocationStatus.ACTIVE),
                                cb.equal(maJoin.get("year"), currentYear),
                                cb.equal(maJoin.get("month"), currentMonth),
                                cb.gt(maJoin.get("percentage"), 0.0)));

                        predicates.add(cb.not(cb.exists(activeAllocationSubquery)));

                    } else if ("PROSPECT".equalsIgnoreCase(status)) {
                        // PROSPECT = Has PROSPECT allocation status but no ACTIVE allocation with
                        // percentage > 0

                        // 1. Must have at least one PROSPECT allocation
                        Subquery<Long> prospectSubquery = query.subquery(Long.class);
                        Root<Allocation> pRoot = prospectSubquery.from(Allocation.class);
                        prospectSubquery.select(pRoot.get("employee").get("id"));
                        prospectSubquery.where(cb.and(
                                cb.equal(pRoot.get("employee"), root),
                                cb.equal(pRoot.get("status"), Allocation.AllocationStatus.PROSPECT)));

                        // 2. Must NOT have ACTIVE allocation with percentage > 0 in current month/year
                        Subquery<Long> activeSubquery = query.subquery(Long.class);
                        Root<Allocation> aRoot = activeSubquery.from(Allocation.class);
                        Join<Allocation, MonthlyAllocation> amaJoin = aRoot.join("monthlyAllocations");
                        activeSubquery.select(aRoot.get("employee").get("id"));
                        activeSubquery.where(cb.and(
                                cb.equal(aRoot.get("employee"), root),
                                cb.equal(aRoot.get("status"), Allocation.AllocationStatus.ACTIVE),
                                cb.equal(amaJoin.get("year"), currentYear),
                                cb.equal(amaJoin.get("month"), currentMonth),
                                cb.gt(amaJoin.get("percentage"), 0.0)));

                        predicates.add(cb.and(
                                cb.exists(prospectSubquery),
                                cb.not(cb.exists(activeSubquery))));

                    } else if ("ACTIVE".equalsIgnoreCase(status)) {
                        // ACTIVE = Has ACTIVE allocation with percentage > 0 in current month/year
                        Subquery<Long> allocationSubquery = query.subquery(Long.class);
                        Root<Allocation> allocRoot = allocationSubquery.from(Allocation.class);
                        Join<Allocation, MonthlyAllocation> maJoin = allocRoot.join("monthlyAllocations");
                        allocationSubquery.select(allocRoot.get("employee").get("id"));

                        allocationSubquery.where(cb.and(
                                cb.equal(allocRoot.get("employee"), root),
                                cb.equal(allocRoot.get("status"), Allocation.AllocationStatus.ACTIVE),
                                cb.equal(maJoin.get("year"), currentYear),
                                cb.equal(maJoin.get("month"), currentMonth),
                                cb.gt(maJoin.get("percentage"), 0.0)));

                        predicates.add(cb.exists(allocationSubquery));
                    }
                }
            }

            // Apply Distinct if necessary
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
