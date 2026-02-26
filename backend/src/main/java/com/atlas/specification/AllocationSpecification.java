package com.atlas.specification;

import com.atlas.entity.Allocation;
import com.atlas.entity.Employee;
import com.atlas.entity.MonthlyAllocation;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AllocationSpecification {

    public static Specification<Allocation> withFilters(
            Allocation.AllocationType allocationType,
            Long managerId,
            String search,
            List<Long> accessibleEmployeeIds,
            Integer year,
            Integer month) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Join necessary tables for filtering and search
            // We use fetch joins in the repository or separate standard joins here?
            // Specifications use joins for predicates.
            // Note: findAll(Spec, Pageable) doesn't automatically do FETCH joins for the
            // result.
            // However, the original query did: "JOIN FETCH a.employee LEFT JOIN FETCH
            // a.project"
            // To maintain performance (N+1 prob), we should ideally ensure fetches happen.
            // In JPA Criteria, we can use root.fetch(...) but it counts as a join.
            // Caveat: Fetch joins with pagination in Hibernate can lead to "fetching in
            // memory" warnings.
            // But the original query was paginated and had fetches? Yes.
            // Let's implement the predicates first.

            Join<Allocation, Employee> employeeJoin = root.join("employee", JoinType.INNER);
            // Access Control (ABAC)
            if (accessibleEmployeeIds != null) {
                if (accessibleEmployeeIds.isEmpty()) {
                    predicates.add(cb.disjunction()); // 1=0
                } else {
                    predicates.add(employeeJoin.get("id").in(accessibleEmployeeIds));
                }
            }

            // Allocation Type filter
            if (allocationType != null) {
                predicates.add(cb.equal(root.get("allocationType"), allocationType));
            }

            // Manager Filter
            if (managerId != null) {
                predicates.add(cb.equal(employeeJoin.get("manager").get("id"), managerId));
            }

            // Search Filter (Employee Name or Email)
            if (search != null && !search.trim().isEmpty()) {
                String searchLike = "%" + search.trim().toLowerCase() + "%";
                Predicate employeeNameLike = cb.like(cb.lower(employeeJoin.get("name")), searchLike);
                Predicate employeeEmailLike = cb.like(cb.lower(employeeJoin.get("email")), searchLike);
                predicates.add(cb.or(employeeNameLike, employeeEmailLike));
            }

            // Month/Year Filter (Date Range)
            if (year != null && month != null) {
                LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
                LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());

                // Basic date overlap valid across all types
                Predicate startDateCheck = cb.lessThanOrEqualTo(root.get("startDate"), lastDayOfMonth);
                Predicate endDateNull = cb.isNull(root.get("endDate"));
                Predicate endDateCheck = cb.greaterThanOrEqualTo(root.get("endDate"), firstDayOfMonth);
                Predicate dateOverlap = cb.and(startDateCheck, cb.or(endDateNull, endDateCheck));

                // Join with MonthlyAllocation to enforce DB-level percentage > 0 checks
                Join<Allocation, MonthlyAllocation> monthlyJoin = root.join("monthlyAllocations", JoinType.LEFT);
                monthlyJoin.on(cb.and(
                        cb.equal(monthlyJoin.get("year"), year),
                        cb.equal(monthlyJoin.get("month"), month)));

                Predicate hasPositivePercentage = cb.and(
                        cb.isNotNull(monthlyJoin.get("id")),
                        cb.greaterThan(monthlyJoin.get("percentage"), 0));

                // PROJECT uniquely requires a positive percentage record
                Predicate isProject = cb.equal(root.get("allocationType"), Allocation.AllocationType.PROJECT);
                Predicate projectCondition = cb.and(isProject, hasPositivePercentage);

                // PROSPECT requires either a positive percentage record, OR date overlap if no
                // monthly record exists
                Predicate isProspect = cb.equal(root.get("allocationType"), Allocation.AllocationType.PROSPECT);
                Predicate prospectCondition = cb.and(
                        isProspect,
                        cb.or(
                                hasPositivePercentage,
                                cb.and(cb.isNull(monthlyJoin.get("id")), dateOverlap)));

                // NON-CLIENT allocations (MATERNITY, VACATION) strictly rely on date
                // bounds
                Predicate isOther = cb.or(
                        cb.equal(root.get("allocationType"), Allocation.AllocationType.MATERNITY),
                        cb.equal(root.get("allocationType"), Allocation.AllocationType.VACATION));
                Predicate otherCondition = cb.and(isOther, dateOverlap);

                predicates.add(cb.or(projectCondition, prospectCondition, otherCondition));
            }

            // Order by? Typically handled by Pageable, but we can add default sorting if
            // needed.

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
