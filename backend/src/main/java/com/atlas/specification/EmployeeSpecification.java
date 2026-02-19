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

    /**
     * Employees with no allocations (BENCH status)
     * BENCH = No active PROJECT AND no PROSPECT/MATERNITY/VACATION
     */
    public static Specification<Employee> isBench(int currentYear, int currentMonth) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // No active PROJECT allocation
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

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Employees with PROSPECT allocation but no active PROJECT (PROSPECT status)
     */
    public static Specification<Employee> isProspect(int currentYear, int currentMonth) {
        return (root, query, cb) -> {
            // Has PROSPECT allocation
            Subquery<Long> prospectSubquery = query.subquery(Long.class);
            Root<Allocation> pRoot = prospectSubquery.from(Allocation.class);
            prospectSubquery.select(pRoot.get("employee").get("id"));
            prospectSubquery.where(cb.and(
                    cb.equal(pRoot.get("employee"), root),
                    cb.equal(pRoot.get("allocationType"), Allocation.AllocationType.PROSPECT)));

            // No active PROJECT allocation
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

            return cb.and(
                    cb.exists(prospectSubquery),
                    cb.not(cb.exists(activeSubquery)));
        };
    }

    /**
     * Employees with active PROJECT allocation (ACTIVE status)
     */
    public static Specification<Employee> isActive(int currentYear, int currentMonth) {
        return (root, query, cb) -> {
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

            return cb.exists(allocationSubquery);
        };
    }

    /**
     * Base filters (search, managerId, employeeIds, active only)
     */
    private static Specification<Employee> baseFilters(String search, Long managerId, List<Long> employeeIds) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Active employees only (not resigned)
            predicates.add(cb.isNull(root.get("resignationDate")));

            // Employee IDs filter (for ABAC)
            if (employeeIds != null && !employeeIds.isEmpty()) {
                predicates.add(root.get("id").in(employeeIds));
            }

            // Search filter (name or email)
            if (search != null && !search.trim().isEmpty()) {
                String searchLike = "%" + search.toLowerCase() + "%";
                Predicate nameLike = cb.like(cb.lower(root.get("name")), searchLike);
                Predicate emailLike = cb.like(cb.lower(root.get("email")), searchLike);
                predicates.add(cb.or(nameLike, emailLike));
            }

            // Manager filter
            if (managerId != null) {
                predicates.add(cb.equal(root.get("manager").get("id"), managerId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Complete filter for BENCH employees (composable)
     */
    public static Specification<Employee> benchEmployees(String search, Long managerId, List<Long> employeeIds,
                                                         int currentYear, int currentMonth) {
        return Specification.where(baseFilters(search, managerId, employeeIds))
                .and(isBench(currentYear, currentMonth));
    }

    /**
     * Complete filter for ACTIVE employees (composable)
     */
    public static Specification<Employee> activeEmployees(String search, Long managerId, List<Long> employeeIds,
                                                          int currentYear, int currentMonth) {
        return Specification.where(baseFilters(search, managerId, employeeIds))
                .and(isActive(currentYear, currentMonth));
    }

    /**
     * Complete filter for PROSPECT employees (composable)
     */
    public static Specification<Employee> prospectEmployees(String search, Long managerId, List<Long> employeeIds,
                                                            int currentYear, int currentMonth) {
        return Specification.where(baseFilters(search, managerId, employeeIds))
                .and(isProspect(currentYear, currentMonth));
    }

    public static Specification<Employee> withFilters(
            String search,
            String tower,
            Long managerId,
            String status,
            List<Long> employeeIds,
            String managerName) {
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

            // Manager Name filter (for manager dropdown search)
            if (managerName != null && !managerName.trim().isEmpty()) {
                String searchLike = "%" + managerName.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("manager").get("name")), searchLike));
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
                    // Use composable specification
                    Specification<Employee> benchSpec = isBench(currentYear, currentMonth);
                    predicates.add(benchSpec.toPredicate(root, query, cb));

                } else if ("PROSPECT".equalsIgnoreCase(status)) {
                    // Use composable specification
                    Specification<Employee> prospectSpec = isProspect(currentYear, currentMonth);
                    predicates.add(prospectSpec.toPredicate(root, query, cb));

                } else if ("ACTIVE".equalsIgnoreCase(status)) {
                    // Use composable specification
                    Specification<Employee> activeSpec = isActive(currentYear, currentMonth);
                    predicates.add(activeSpec.toPredicate(root, query, cb));

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
