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
            java.time.LocalDate firstDayOfMonth = java.time.LocalDate.of(currentYear, currentMonth, 1);
            java.time.LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());

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
                    cb.equal(prRoot.get("allocationType"), Allocation.AllocationType.PROSPECT),
                    cb.lessThanOrEqualTo(prRoot.get("startDate"), lastDayOfMonth),
                    cb.or(
                            cb.isNull(prRoot.get("endDate")),
                            cb.greaterThanOrEqualTo(prRoot.get("endDate"), firstDayOfMonth))));
            predicates.add(cb.not(cb.exists(prospectSub)));

            // Exclude MATERNITY
            Subquery<Long> maternitySub = query.subquery(Long.class);
            Root<Allocation> matRoot = maternitySub.from(Allocation.class);
            maternitySub.select(matRoot.get("employee").get("id"));
            maternitySub.where(cb.and(
                    cb.equal(matRoot.get("employee"), root),
                    cb.equal(matRoot.get("allocationType"), Allocation.AllocationType.MATERNITY),
                    cb.lessThanOrEqualTo(matRoot.get("startDate"), lastDayOfMonth),
                    cb.or(
                            cb.isNull(matRoot.get("endDate")),
                            cb.greaterThanOrEqualTo(matRoot.get("endDate"), firstDayOfMonth))));
            predicates.add(cb.not(cb.exists(maternitySub)));

            // Exclude VACATION
            Subquery<Long> vacationSub = query.subquery(Long.class);
            Root<Allocation> vacRoot = vacationSub.from(Allocation.class);
            vacationSub.select(vacRoot.get("employee").get("id"));
            vacationSub.where(cb.and(
                    cb.equal(vacRoot.get("employee"), root),
                    cb.equal(vacRoot.get("allocationType"), Allocation.AllocationType.VACATION),
                    cb.lessThanOrEqualTo(vacRoot.get("startDate"), lastDayOfMonth),
                    cb.or(
                            cb.isNull(vacRoot.get("endDate")),
                            cb.greaterThanOrEqualTo(vacRoot.get("endDate"), firstDayOfMonth))));
            predicates.add(cb.not(cb.exists(vacationSub)));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Employees with PROSPECT allocation but no active PROJECT (PROSPECT status)
     * Checks for PROSPECT allocations active in the specified month
     */
    public static Specification<Employee> isProspect(int currentYear, int currentMonth) {
        return (root, query, cb) -> {
            // Calculate first and last day of the month for date range overlap
            java.time.LocalDate firstDayOfMonth = java.time.LocalDate.of(currentYear, currentMonth, 1);
            java.time.LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());

            // Has PROSPECT allocation active in the selected month
            // Date range overlap: startDate <= lastDayOfMonth AND (endDate IS NULL OR
            // endDate >= firstDayOfMonth)
            Subquery<Long> prospectSubquery = query.subquery(Long.class);
            Root<Allocation> pRoot = prospectSubquery.from(Allocation.class);
            prospectSubquery.select(pRoot.get("employee").get("id"));
            prospectSubquery.where(cb.and(
                    cb.equal(pRoot.get("employee"), root),
                    cb.equal(pRoot.get("allocationType"), Allocation.AllocationType.PROSPECT),
                    cb.lessThanOrEqualTo(pRoot.get("startDate"), lastDayOfMonth),
                    cb.or(
                            cb.isNull(pRoot.get("endDate")),
                            cb.greaterThanOrEqualTo(pRoot.get("endDate"), firstDayOfMonth))));

            // No active PROJECT allocation in the selected month
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
            if (employeeIds != null) {
                if (employeeIds.isEmpty()) {
                    predicates.add(cb.or()); // Force false predicate
                } else {
                    predicates.add(root.get("id").in(employeeIds));
                }
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
     * Base filters with allocation type existence check.
     * Returns employees who have at least one allocation of the specified type in the given month.
     * This is used for allocation type filtering where we want to show anyone with that type,
     * regardless of other allocation types they may have.
     */
    public static Specification<Employee> baseFiltersWithAllocationType(
            String search, Long managerId, List<Long> employeeIds,
            Allocation.AllocationType allocationType, int currentYear, int currentMonth) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Active employees only (not resigned)
            predicates.add(cb.isNull(root.get("resignationDate")));

            // Employee IDs filter (for ABAC)
            if (employeeIds != null) {
                if (employeeIds.isEmpty()) {
                    predicates.add(cb.or()); // Force false predicate
                } else {
                    predicates.add(root.get("id").in(employeeIds));
                }
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

            // Allocation type existence check
            // For PROJECT/PROSPECT: check if employee has at least one allocation with percentage > 0 in the selected month
            // For MATERNITY/VACATION: check if allocation period overlaps with the selected month
            if (allocationType == Allocation.AllocationType.PROJECT || allocationType == Allocation.AllocationType.PROSPECT) {
                Subquery<Long> allocationSubquery = query.subquery(Long.class);
                Root<Allocation> allocRoot = allocationSubquery.from(Allocation.class);
                Join<Allocation, MonthlyAllocation> maJoin = allocRoot.join("monthlyAllocations");
                allocationSubquery.select(allocRoot.get("employee").get("id"));
                allocationSubquery.where(cb.and(
                        cb.equal(allocRoot.get("employee"), root),
                        cb.equal(allocRoot.get("allocationType"), allocationType),
                        cb.equal(maJoin.get("year"), currentYear),
                        cb.equal(maJoin.get("month"), currentMonth),
                        cb.gt(maJoin.get("percentage"), 0)));
                predicates.add(cb.exists(allocationSubquery));
            } else {
                // MATERNITY or VACATION: check date overlap with selected month
                java.time.LocalDate firstDayOfMonth = java.time.LocalDate.of(currentYear, currentMonth, 1);
                java.time.LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());

                Subquery<Long> allocationSubquery = query.subquery(Long.class);
                Root<Allocation> allocRoot = allocationSubquery.from(Allocation.class);
                allocationSubquery.select(allocRoot.get("employee").get("id"));
                allocationSubquery.where(cb.and(
                        cb.equal(allocRoot.get("employee"), root),
                        cb.equal(allocRoot.get("allocationType"), allocationType),
                        cb.lessThanOrEqualTo(allocRoot.get("startDate"), lastDayOfMonth),
                        cb.or(
                                cb.isNull(allocRoot.get("endDate")),
                                cb.greaterThanOrEqualTo(allocRoot.get("endDate"), firstDayOfMonth))));
                predicates.add(cb.exists(allocationSubquery));
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
        // Default to current month if not specified
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();
        return withFilters(search, tower, managerId, status, employeeIds, managerName, currentYear, currentMonth);
    }

    public static Specification<Employee> withFilters(
            String search,
            String tower,
            Long managerId,
            String status,
            List<Long> employeeIds,
            String managerName,
            Integer year,
            Integer month) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Resignation date logic depends on status filter
            if ("RESIGNED".equalsIgnoreCase(status)) {
                predicates.add(cb.isNotNull(root.get("resignationDate")));
            } else {
                predicates.add(cb.isNull(root.get("resignationDate")));
            }

            // Employee IDs filter (for ABAC hierarchy-based access)
            if (employeeIds != null) {
                if (employeeIds.isEmpty()) {
                    predicates.add(cb.or()); // Force false predicate
                } else {
                    predicates.add(root.get("id").in(employeeIds));
                }
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

                // Use provided year/month for status checks (defaults to current month)
                int currentYear = year != null ? year : LocalDate.now().getYear();
                int currentMonth = month != null ? month : LocalDate.now().getMonthValue();

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
                    // MATERNITY = Has allocation of type MATERNITY active in the selected month
                    // Date range overlap: startDate <= lastDayOfMonth AND (endDate IS NULL OR
                    // endDate >= firstDayOfMonth)
                    LocalDate firstDayOfMonth = LocalDate.of(currentYear, currentMonth, 1);
                    LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());

                    Subquery<Long> maternitySubquery = query.subquery(Long.class);
                    Root<Allocation> mRoot = maternitySubquery.from(Allocation.class);
                    maternitySubquery.select(mRoot.get("employee").get("id"));
                    maternitySubquery.where(cb.and(
                            cb.equal(mRoot.get("employee"), root),
                            cb.equal(mRoot.get("allocationType"), Allocation.AllocationType.MATERNITY),
                            cb.lessThanOrEqualTo(mRoot.get("startDate"), lastDayOfMonth),
                            cb.or(
                                    cb.isNull(mRoot.get("endDate")),
                                    cb.greaterThanOrEqualTo(mRoot.get("endDate"), firstDayOfMonth))));

                    predicates.add(cb.exists(maternitySubquery));

                } else if ("VACATION".equalsIgnoreCase(status)) {
                    // VACATION = Has allocation of type VACATION active in the selected month
                    // Date range overlap: startDate <= lastDayOfMonth AND (endDate IS NULL OR
                    // endDate >= firstDayOfMonth)
                    LocalDate firstDayOfMonth = LocalDate.of(currentYear, currentMonth, 1);
                    LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());

                    Subquery<Long> vacationSubquery = query.subquery(Long.class);
                    Root<Allocation> vRoot = vacationSubquery.from(Allocation.class);
                    vacationSubquery.select(vRoot.get("employee").get("id"));
                    vacationSubquery.where(cb.and(
                            cb.equal(vRoot.get("employee"), root),
                            cb.equal(vRoot.get("allocationType"), Allocation.AllocationType.VACATION),
                            cb.lessThanOrEqualTo(vRoot.get("startDate"), lastDayOfMonth),
                            cb.or(
                                    cb.isNull(vRoot.get("endDate")),
                                    cb.greaterThanOrEqualTo(vRoot.get("endDate"), firstDayOfMonth))));

                    predicates.add(cb.exists(vacationSubquery));
                }
                // RESIGNED is handled above (resignationDate IS NOT NULL)
            }

            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
