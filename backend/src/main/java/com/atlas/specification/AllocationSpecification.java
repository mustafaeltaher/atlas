package com.atlas.specification;

import com.atlas.entity.Allocation;
import com.atlas.entity.Employee;
import com.atlas.entity.Project;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AllocationSpecification {

    public static Specification<Allocation> withFilters(
            Allocation.AllocationType allocationType,
            Long managerId,
            String search,
            List<Long> accessibleEmployeeIds) {
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
            Join<Allocation, Project> projectJoin = root.join("project", JoinType.LEFT);

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

            // Order by? Typically handled by Pageable, but we can add default sorting if
            // needed.

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
