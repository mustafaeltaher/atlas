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

            // Resignation date logic depends on status filter
            if ("RESIGNED".equalsIgnoreCase(status)) {
                predicates.add(cb.isNotNull(root.get("resignationDate")));
            } else {
                predicates.add(cb.isNull(root.get("resignationDate")));
            }

            // Employee IDs filter (for ABAC hierarchy-based access)
            if (employeeIds != null && !employeeIds.isEmpty()) {
                predicates.add(root.get("id").in(employeeIds));
            }

            // Tower filter (match by tower description)
            if (tower != null && !tower.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("tower").get("description"), tower));
            }

            // Manager filter
            if (managerId != null) {
                predicates.add(cb.equal(root.get("manager").get("id"), managerId));
            }

            // Search filter (name and email only)
            if (search != null && !search.trim().isEmpty()) {
                String searchLike = "%" + search.toLowerCase() + "%";
                Predicate nameLike = cb.like(cb.lower(root.get("name")), searchLike);
                Predicate emailLike = cb.like(cb.lower(root.get("email")), searchLike);
                predicates.add(cb.or(nameLike, emailLike));
            }

            // Status filter
            if (status != null && !status.trim().isEmpty()) {

                int currentYear = LocalDate.now().getYear();
                int currentMonth = LocalDate.now().getMonthValue();

                if ("BENCH".equalsIgnoreCase(status)) {
                    // BENCH = No active PROJECT allocation AND no PROSPECT/MATERNITY/VACATION
                    Subquery<Long> activeAllocationSubquery = query.subquery(Long.class);
                    Root<Allocation> allocRoot = activeAllocationSubquery.from(Allocation.class);
                    Join<Allocation, MonthlyAllocation> maJoin = allocRoot.join("monthlyAllocations");
                    activeAllocationSubquery.select(allocRoot.get("employee").get("id"));

                    activeAllocationSubquery.where(cb.and(
                            cb.equal(allocRoot.get("employee"), root),
                            cb.equal(allocRoot.get("allocationType"), Allocation.AllocationType.PROJECT),
                            cb.equal(maJoin.get("year"), currentYear),
                            cb.equal(maJoin.get("month"), currentMonth),
                            cb.gt(maJoin.get("percentage"), 0)));

                    predicates.add(cb.not(cb.exists(activeAllocationSubquery)));

                    // Exclude PROSPECT
                    Subquery<Long> prospectSub = query.subquery(Long.class);
                    Root<Allocation> prRoot = prospectSub.from(Allocation.class);
                    prospectSub.select(prRoot.get("employee").get("id"));
                    prospectSub.where(cb.and(
                            cb.equal(prRoot.get("employee"), root),
                            cb.equal(prRoot.get("allocationType"), Allocation.AllocationType.PROSPECT)));
                    predicates.add(cb.not(cb.exists(prospectSub)));

                    // Exclude MATERNITY
                    Subquery<Long> maternitySub = query.subquery(Long.class);
                    Root<Allocation> matRoot = maternitySub.from(Allocation.class);
                    maternitySub.select(matRoot.get("employee").get("id"));
                    maternitySub.where(cb.and(
                            cb.equal(matRoot.get("employee"), root),
                            cb.equal(matRoot.get("allocationType"), Allocation.AllocationType.MATERNITY)));
                    predicates.add(cb.not(cb.exists(maternitySub)));

                    // Exclude VACATION
                    Subquery<Long> vacationSub = query.subquery(Long.class);
                    Root<Allocation> vacRoot = vacationSub.from(Allocation.class);
                    vacationSub.select(vacRoot.get("employee").get("id"));
                    vacationSub.where(cb.and(
                            cb.equal(vacRoot.get("employee"), root),
                            cb.equal(vacRoot.get("allocationType"), Allocation.AllocationType.VACATION)));
                    predicates.add(cb.not(cb.exists(vacationSub)));

                } else if ("PROSPECT".equalsIgnoreCase(status)) {
                    // PROSPECT = Has PROSPECT allocation but no PROJECT allocation with % > 0

                    Subquery<Long> prospectSubquery = query.subquery(Long.class);
                    Root<Allocation> pRoot = prospectSubquery.from(Allocation.class);
                    prospectSubquery.select(pRoot.get("employee").get("id"));
                    prospectSubquery.where(cb.and(
                            cb.equal(pRoot.get("employee"), root),
                            cb.equal(pRoot.get("allocationType"), Allocation.AllocationType.PROSPECT)));

                    Subquery<Long> activeSubquery = query.subquery(Long.class);
                    Root<Allocation> aRoot = activeSubquery.from(Allocation.class);
                    Join<Allocation, MonthlyAllocation> amaJoin = aRoot.join("monthlyAllocations");
                    activeSubquery.select(aRoot.get("employee").get("id"));
                    activeSubquery.where(cb.and(
                            cb.equal(aRoot.get("employee"), root),
                            cb.equal(aRoot.get("allocationType"), Allocation.AllocationType.PROJECT),
                            cb.equal(amaJoin.get("year"), currentYear),
                            cb.equal(amaJoin.get("month"), currentMonth),
                            cb.gt(amaJoin.get("percentage"), 0)));

                    predicates.add(cb.and(
                            cb.exists(prospectSubquery),
                            cb.not(cb.exists(activeSubquery))));

                } else if ("ACTIVE".equalsIgnoreCase(status)) {
                    // ACTIVE = Has PROJECT allocation with percentage > 0 in current month/year
                    Subquery<Long> allocationSubquery = query.subquery(Long.class);
                    Root<Allocation> allocRoot = allocationSubquery.from(Allocation.class);
                    Join<Allocation, MonthlyAllocation> maJoin = allocRoot.join("monthlyAllocations");
                    allocationSubquery.select(allocRoot.get("employee").get("id"));

                    allocationSubquery.where(cb.and(
                            cb.equal(allocRoot.get("employee"), root),
                            cb.equal(allocRoot.get("allocationType"), Allocation.AllocationType.PROJECT),
                            cb.equal(maJoin.get("year"), currentYear),
                            cb.equal(maJoin.get("month"), currentMonth),
                            cb.gt(maJoin.get("percentage"), 0)));

                    predicates.add(cb.exists(allocationSubquery));

                } else if ("MATERNITY".equalsIgnoreCase(status)) {
                    // MATERNITY = Has allocation of type MATERNITY
                    Subquery<Long> maternitySubquery = query.subquery(Long.class);
                    Root<Allocation> mRoot = maternitySubquery.from(Allocation.class);
                    maternitySubquery.select(mRoot.get("employee").get("id"));
                    maternitySubquery.where(cb.and(
                            cb.equal(mRoot.get("employee"), root),
                            cb.equal(mRoot.get("allocationType"), Allocation.AllocationType.MATERNITY)));

                    predicates.add(cb.exists(maternitySubquery));

                } else if ("VACATION".equalsIgnoreCase(status)) {
                    // VACATION = Has allocation of type VACATION
                    Subquery<Long> vacationSubquery = query.subquery(Long.class);
                    Root<Allocation> vRoot = vacationSubquery.from(Allocation.class);
                    vacationSubquery.select(vRoot.get("employee").get("id"));
                    vacationSubquery.where(cb.and(
                            cb.equal(vRoot.get("employee"), root),
                            cb.equal(vRoot.get("allocationType"), Allocation.AllocationType.VACATION)));

                    predicates.add(cb.exists(vacationSubquery));
                }
                // RESIGNED is handled above (resignationDate IS NOT NULL)
            }

            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
