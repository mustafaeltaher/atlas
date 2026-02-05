package com.atlas.specification;

import com.atlas.entity.Allocation;
import com.atlas.entity.Employee;
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

            // Status filter (Complex Logic)
            if (status != null && !status.trim().isEmpty()) {
                int currentMonth = LocalDate.now().getMonthValue();
                String monthColumn = getMonthColumn(currentMonth);

                // Join with Allocations
                Subquery<Long> allocationSubquery = query.subquery(Long.class);
                Root<Allocation> allocRoot = allocationSubquery.from(Allocation.class);
                allocationSubquery.select(allocRoot.get("employee").get("id"));

                // Correlated subquery
                Predicate employeeMatch = cb.equal(allocRoot.get("employee"), root);

                // Get the monthly allocation value
                Expression<String> monthValue = allocRoot.get(monthColumn);

                if ("BENCH".equalsIgnoreCase(status)) {
                    // BENCH = No allocation OR allocation is 'B'
                    // Implementation: NOT EXISTS (allocations where value is numeric OR value is
                    // 'P')

                    // Subquery for employees who have ANY active/prospect allocation
                    Subquery<Long> activeOrProspectSubquery = query.subquery(Long.class);
                    Root<Allocation> apRoot = activeOrProspectSubquery.from(Allocation.class);
                    activeOrProspectSubquery.select(apRoot.get("employee").get("id"));

                    Expression<String> apMonthValue = apRoot.get(monthColumn);
                    Predicate apEmployeeMatch = cb.equal(apRoot.get("employee"), root);

                    // Is Numeric (Active) OR 'P' (Prospect)
                    // Note: Since we store as String, checking "not B" and "not null" is easier but
                    // "P" is prospect.
                    // Let's define "Non-Bench" as: (value != 'B' AND value != null)
                    Predicate notBench = cb.and(
                            cb.isNotNull(apMonthValue),
                            cb.notEqual(apMonthValue, "B"));

                    activeOrProspectSubquery.where(cb.and(apEmployeeMatch, notBench));

                    predicates.add(cb.not(cb.exists(activeOrProspectSubquery)));

                } else if ("PROSPECT".equalsIgnoreCase(status)) {
                    // PROSPECT = Has 'P' AND Does NOT have Numeric

                    // 1. Must have 'P'
                    Subquery<Long> prospectSubquery = query.subquery(Long.class);
                    Root<Allocation> pRoot = prospectSubquery.from(Allocation.class);
                    prospectSubquery.select(pRoot.get("employee").get("id"));
                    prospectSubquery.where(cb.and(
                            cb.equal(pRoot.get("employee"), root),
                            cb.equal(pRoot.get(monthColumn), "P")));

                    // 2. Must NOT have Numeric (Active)
                    Subquery<Long> activeSubquery = query.subquery(Long.class);
                    Root<Allocation> aRoot = activeSubquery.from(Allocation.class);
                    activeSubquery.select(aRoot.get("employee").get("id"));

                    // Numeric check is hard in standard JPA/SQL without native queries if column is
                    // string.
                    // Assuming standard format: 'B', 'P', or numbers.
                    // "Active" means NOT 'B' AND NOT 'P' AND NOT NULL
                    activeSubquery.where(cb.and(
                            cb.equal(aRoot.get("employee"), root),
                            cb.isNotNull(aRoot.get(monthColumn)),
                            cb.notEqual(aRoot.get(monthColumn), "B"),
                            cb.notEqual(aRoot.get(monthColumn), "P")));

                    predicates.add(cb.and(
                            cb.exists(prospectSubquery),
                            cb.not(cb.exists(activeSubquery))));

                } else if ("ACTIVE".equalsIgnoreCase(status)) {
                    // ACTIVE = Has ANY Numeric allocation
                    allocationSubquery.where(cb.and(
                            employeeMatch,
                            cb.isNotNull(monthValue),
                            cb.notEqual(monthValue, "B"),
                            cb.notEqual(monthValue, "P")));
                    predicates.add(cb.exists(allocationSubquery));
                }
            }

            // Apply Distinct if necessary (though usually handled by repository/query
            // config)
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String getMonthColumn(int month) {
        String[] columns = {
                "janAllocation", "febAllocation", "marAllocation", "aprAllocation",
                "mayAllocation", "junAllocation", "julAllocation", "augAllocation",
                "sepAllocation", "octAllocation", "novAllocation", "decAllocation"
        };
        return columns[month - 1];
    }
}
