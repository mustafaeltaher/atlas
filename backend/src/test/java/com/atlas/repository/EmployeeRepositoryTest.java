package com.atlas.repository;

import com.atlas.entity.Employee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive tests for EmployeeRepository filtration logic.
 * Tests cover all allocation status queries (BENCH, ACTIVE, PROSPECT) and faceted search.
 */
@DisplayName("Employee Repository Tests")
public class EmployeeRepositoryTest extends RepositoryTestBase {

    @Nested
    @DisplayName("BENCH Employee Filtration")
    class BenchEmployeeTests {

        @Test
        @DisplayName("Should find BENCH employees (no allocations)")
        void shouldFindBenchEmployees() {
            // Given: employeeBench has no allocations
            Pageable pageable = PageRequest.of(0, 10);

            // When: Query for BENCH employees
            Page<Employee> result = employeeRepository.findBenchEmployees(
                    null, null, currentYear, currentMonth, pageable
            );

            // Then: Only BENCH employee is returned
            assertThat(result.getContent())
                    .hasSize(1)
                    .extracting(Employee::getEmail)
                    .containsExactly("bench@atlas.com");
        }

        @Test
        @DisplayName("Should filter BENCH employees by search term")
        void shouldFilterBenchEmployeesBySearch() {
            // Given: employeeBench with name "Bench Employee"
            Pageable pageable = PageRequest.of(0, 10);

            // When: Search for "bench"
            Page<Employee> result = employeeRepository.findBenchEmployees(
                    "%bench%", null, currentYear, currentMonth, pageable
            );

            // Then: Find the BENCH employee
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getEmail()).isEqualTo("bench@atlas.com");

            // When: Search for non-matching term
            result = employeeRepository.findBenchEmployees(
                    "%nomatch%", null, currentYear, currentMonth, pageable
            );

            // Then: No results
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should filter BENCH employees by manager")
        void shouldFilterBenchEmployeesByManager() {
            // Given: employeeBench reports to manager1
            Pageable pageable = PageRequest.of(0, 10);

            // When: Filter by manager1
            Page<Employee> result = employeeRepository.findBenchEmployees(
                    null, manager1.getId(), currentYear, currentMonth, pageable
            );

            // Then: Find the BENCH employee
            assertThat(result.getContent()).hasSize(1);

            // When: Filter by manager2
            result = employeeRepository.findBenchEmployees(
                    null, manager2.getId(), currentYear, currentMonth, pageable
            );

            // Then: No results (employeeBench doesn't report to manager2)
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should filter BENCH employees by accessible IDs (ABAC)")
        void shouldFilterBenchEmployeesByAccessibleIds() {
            // Given: manager1's accessible employee IDs
            List<Long> accessibleIds = getAccessibleEmployeeIds(manager1);
            Pageable pageable = PageRequest.of(0, 10);

            // When: Filter by accessible IDs
            Page<Employee> result = employeeRepository.findBenchEmployeesByIds(
                    accessibleIds, null, null, currentYear, currentMonth, pageable
            );

            // Then: Only BENCH employees in accessible list
            assertThat(result.getContent())
                    .allMatch(e -> accessibleIds.contains(e.getId()));
        }

        @Test
        @DisplayName("Should count BENCH employees correctly")
        void shouldCountBenchEmployees() {
            // When: Count BENCH employees
            long count = employeeRepository.countBenchEmployees(currentYear, currentMonth);

            // Then: Count is 1 (only employeeBench)
            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("Should count BENCH employees with accessible IDs filter")
        void shouldCountBenchEmployeesWithAccessibleIds() {
            // Given: manager1's accessible employee IDs
            List<Long> accessibleIds = getAccessibleEmployeeIds(manager1);

            // When: Count BENCH employees in accessible list
            long count = employeeRepository.countBenchEmployeesByIds(
                    accessibleIds, currentYear, currentMonth
            );

            // Then: Count matches filter
            assertThat(count).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("ACTIVE Employee Filtration")
    class ActiveEmployeeTests {

        @Test
        @DisplayName("Should find ACTIVE employees (with PROJECT allocation > 0%)")
        void shouldFindActiveEmployees() {
            // Given: employeeActive and employeeMultipleProjects have active PROJECT allocations
            Pageable pageable = PageRequest.of(0, 10);

            // When: Query for ACTIVE employees
            Page<Employee> result = employeeRepository.findActiveAllocatedEmployees(
                    null, null, currentYear, currentMonth, pageable
            );

            // Then: Only ACTIVE employees are returned
            assertThat(result.getContent())
                    .hasSize(2)
                    .extracting(Employee::getEmail)
                    .containsExactlyInAnyOrder("active@atlas.com", "multi@atlas.com");
        }

        @Test
        @DisplayName("Should filter ACTIVE employees by search term")
        void shouldFilterActiveEmployeesBySearch() {
            // Given: employeeActive with name "Active Employee"
            Pageable pageable = PageRequest.of(0, 10);

            // When: Search for "active"
            Page<Employee> result = employeeRepository.findActiveAllocatedEmployees(
                    "%active%", null, currentYear, currentMonth, pageable
            );

            // Then: Find the ACTIVE employee
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getEmail()).isEqualTo("active@atlas.com");
        }

        @Test
        @DisplayName("Should filter ACTIVE employees by manager")
        void shouldFilterActiveEmployeesByManager() {
            // Given: employeeActive and employeeMultipleProjects report to manager1
            Pageable pageable = PageRequest.of(0, 10);

            // When: Filter by manager1
            Page<Employee> result = employeeRepository.findActiveAllocatedEmployees(
                    null, manager1.getId(), currentYear, currentMonth, pageable
            );

            // Then: Find both ACTIVE employees
            assertThat(result.getContent()).hasSize(2);

            // When: Filter by manager2
            result = employeeRepository.findActiveAllocatedEmployees(
                    null, manager2.getId(), currentYear, currentMonth, pageable
            );

            // Then: No results
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should count ACTIVE employees correctly")
        void shouldCountActiveEmployees() {
            // When: Count ACTIVE employees
            long count = employeeRepository.countActiveAllocatedEmployees(currentYear, currentMonth);

            // Then: Count is 2 (employeeActive + employeeMultipleProjects)
            assertThat(count).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("PROSPECT Employee Filtration")
    class ProspectEmployeeTests {

        @Test
        @DisplayName("Should find PROSPECT employees (with PROSPECT allocation, no active PROJECT)")
        void shouldFindProspectEmployees() {
            // Given: employeeProspect has PROSPECT allocation, no active PROJECT
            Pageable pageable = PageRequest.of(0, 10);

            // When: Query for PROSPECT employees
            Page<Employee> result = employeeRepository.findProspectEmployees(
                    null, null, currentYear, currentMonth, pageable
            );

            // Then: Only PROSPECT employee is returned
            assertThat(result.getContent())
                    .hasSize(1)
                    .extracting(Employee::getEmail)
                    .containsExactly("prospect@atlas.com");
        }

        @Test
        @DisplayName("Should filter PROSPECT employees by search term")
        void shouldFilterProspectEmployeesBySearch() {
            // Given: employeeProspect with name "Prospect Employee"
            Pageable pageable = PageRequest.of(0, 10);

            // When: Search for "prospect"
            Page<Employee> result = employeeRepository.findProspectEmployees(
                    "%prospect%", null, currentYear, currentMonth, pageable
            );

            // Then: Find the PROSPECT employee
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getEmail()).isEqualTo("prospect@atlas.com");
        }

        @Test
        @DisplayName("Should count PROSPECT employees correctly")
        void shouldCountProspectEmployees() {
            // When: Count PROSPECT employees
            long count = employeeRepository.countProspectEmployees(currentYear, currentMonth);

            // Then: Count is 1
            assertThat(count).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Allocation Type Filtration")
    class AllocationTypeTests {

        @Test
        @DisplayName("Should find employees by MATERNITY allocation type")
        void shouldFindEmployeesByMaternityType() {
            // Given: employeeMaternity has MATERNITY allocation
            Pageable pageable = PageRequest.of(0, 10);

            // When: Query for MATERNITY employees
            Page<Employee> result = employeeRepository.findEmployeesByAllocationType(
                    null, null, "MATERNITY", pageable
            );

            // Then: Only MATERNITY employee is returned
            assertThat(result.getContent())
                    .hasSize(1)
                    .extracting(Employee::getEmail)
                    .containsExactly("maternity@atlas.com");
        }

        @Test
        @DisplayName("Should find employees by VACATION allocation type")
        void shouldFindEmployeesByVacationType() {
            // Given: employeeVacation has VACATION allocation
            Pageable pageable = PageRequest.of(0, 10);

            // When: Query for VACATION employees
            Page<Employee> result = employeeRepository.findEmployeesByAllocationType(
                    null, null, "VACATION", pageable
            );

            // Then: Only VACATION employee is returned
            assertThat(result.getContent())
                    .hasSize(1)
                    .extracting(Employee::getEmail)
                    .containsExactly("vacation@atlas.com");
        }

        @Test
        @DisplayName("Should find employees by PROJECT allocation type")
        void shouldFindEmployeesByProjectType() {
            // Given: employeeActive and employeeMultipleProjects have PROJECT allocations
            Pageable pageable = PageRequest.of(0, 10);

            // When: Query for PROJECT employees
            Page<Employee> result = employeeRepository.findEmployeesByAllocationType(
                    null, null, "PROJECT", pageable
            );

            // Then: All employees with PROJECT allocations are returned
            assertThat(result.getContent())
                    .hasSizeGreaterThanOrEqualTo(2)
                    .extracting(Employee::getEmail)
                    .contains("active@atlas.com", "multi@atlas.com");
        }
    }

    @Nested
    @DisplayName("Manager Dropdown Queries (Faceted Search)")
    class ManagerDropdownTests {

        @Test
        @DisplayName("Should find distinct managers of all active employees")
        void shouldFindDistinctManagers() {
            // When: Get all managers with search wildcard
            List<Employee> managers = employeeRepository.findDistinctManagers("%");

            // Then: Both manager1 and manager2 are returned
            assertThat(managers)
                    .hasSizeGreaterThanOrEqualTo(2)
                    .extracting(Employee::getEmail)
                    .contains("manager1@atlas.com", "manager2@atlas.com");
        }

        @Test
        @DisplayName("Should find managers by allocation type (PROJECT)")
        void shouldFindManagersByAllocationType() {
            // When: Get managers of employees with PROJECT allocations
            List<Employee> managers = employeeRepository.findDistinctManagersByAllocationType(
                    "PROJECT", "%"
            );

            // Then: manager1 is returned (has employees with PROJECT allocations)
            assertThat(managers)
                    .extracting(Employee::getEmail)
                    .contains("manager1@atlas.com");
        }

        @Test
        @DisplayName("Should find managers of BENCH employees")
        void shouldFindManagersOfBenchEmployees() {
            // When: Get managers of BENCH employees
            List<Employee> managers = employeeRepository.findDistinctManagersOfBenchEmployees(
                    currentYear, currentMonth, "%"
            );

            // Then: manager1 is returned (has employeeBench)
            assertThat(managers)
                    .extracting(Employee::getEmail)
                    .contains("manager1@atlas.com");
        }

        @Test
        @DisplayName("Should find managers of ACTIVE employees")
        void shouldFindManagersOfActiveEmployees() {
            // When: Get managers of ACTIVE employees
            List<Employee> managers = employeeRepository.findDistinctManagersOfActiveEmployees(
                    currentYear, currentMonth, "%"
            );

            // Then: manager1 is returned (has employeeActive and employeeMultipleProjects)
            assertThat(managers)
                    .extracting(Employee::getEmail)
                    .contains("manager1@atlas.com");
        }

        @Test
        @DisplayName("Should filter managers by search term")
        void shouldFilterManagersBySearch() {
            // When: Search for "manager one"
            List<Employee> managers = employeeRepository.findDistinctManagers("%manager one%");

            // Then: Only manager1 is returned
            assertThat(managers)
                    .hasSize(1)
                    .extracting(Employee::getEmail)
                    .containsExactly("manager1@atlas.com");
        }
    }

    @Nested
    @DisplayName("Tech Tower Dropdown Queries")
    class TowerDropdownTests {

        @Test
        @DisplayName("Should find distinct tech towers")
        void shouldFindDistinctTowers() {
            // When: Get all distinct towers
            List<String> towers = employeeRepository.findDistinctTowers(null, null);

            // Then: Both towers are returned
            assertThat(towers)
                    .hasSizeGreaterThanOrEqualTo(2)
                    .contains("EPIS", "Application");
        }

        @Test
        @DisplayName("Should filter towers by manager")
        void shouldFilterTowersByManager() {
            // When: Get towers for manager1's employees
            List<String> towers = employeeRepository.findDistinctTowers(manager1.getId(), null);

            // Then: EPIS tower is returned
            assertThat(towers)
                    .contains("EPIS");
        }

        @Test
        @DisplayName("Should filter towers by employee search")
        void shouldFilterTowersBySearch() {
            // When: Search for employees with "bench" in name
            List<String> towers = employeeRepository.findDistinctTowers(null, "%bench%");

            // Then: EPIS tower is returned (employeeBench is in EPIS)
            assertThat(towers)
                    .contains("EPIS");
        }
    }

    @Nested
    @DisplayName("Dashboard Statistics")
    class DashboardTests {

        @Test
        @DisplayName("Should count active employees correctly")
        void shouldCountActiveEmployees() {
            // When: Count all active (non-resigned) employees
            long count = employeeRepository.countActiveEmployees();

            // Then: Count excludes resigned employee
            assertThat(count).isEqualTo(9); // All except employeeResigned
        }

        @Test
        @DisplayName("Should calculate average allocation percentage")
        void shouldCalculateAverageAllocation() {
            // When: Calculate average allocation percentage
            double avg = employeeRepository.averageAllocationPercentage(currentYear, currentMonth);

            // Then: Average is 100% (employeeActive=100%, employeeMultipleProjects=100% total)
            assertThat(avg).isEqualTo(100.0);
        }
    }

    @Nested
    @DisplayName("Default Routing Methods")
    class DefaultRoutingTests {

        @Test
        @DisplayName("findBenchEmployeesFiltered should route to correct query without IDs")
        void shouldRouteBenchWithoutIds() {
            // When: Call filtered method with null IDs
            Pageable pageable = PageRequest.of(0, 10);
            Page<Employee> result = employeeRepository.findBenchEmployeesFiltered(
                    null, null, null, currentYear, currentMonth, pageable
            );

            // Then: Results are returned
            assertThat(result.getContent()).isNotEmpty();
        }

        @Test
        @DisplayName("findBenchEmployeesFiltered should route to correct query with IDs")
        void shouldRouteBenchWithIds() {
            // Given: Accessible IDs
            List<Long> ids = getAccessibleEmployeeIds(manager1);

            // When: Call filtered method with IDs
            Pageable pageable = PageRequest.of(0, 10);
            Page<Employee> result = employeeRepository.findBenchEmployeesFiltered(
                    ids, null, null, currentYear, currentMonth, pageable
            );

            // Then: Results are filtered by IDs
            assertThat(result.getContent())
                    .allMatch(e -> ids.contains(e.getId()));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should exclude resigned employees from all queries")
        void shouldExcludeResignedEmployees() {
            // Given: employeeResigned has resignationDate set
            Pageable pageable = PageRequest.of(0, 10);

            // When: Query BENCH employees
            Page<Employee> benchResult = employeeRepository.findBenchEmployees(
                    null, null, currentYear, currentMonth, pageable
            );

            // Then: Resigned employee is not included
            assertThat(benchResult.getContent())
                    .extracting(Employee::getEmail)
                    .doesNotContain("resigned@atlas.com");

            // When: Query ACTIVE employees
            Page<Employee> activeResult = employeeRepository.findActiveAllocatedEmployees(
                    null, null, currentYear, currentMonth, pageable
            );

            // Then: Resigned employee is not included
            assertThat(activeResult.getContent())
                    .extracting(Employee::getEmail)
                    .doesNotContain("resigned@atlas.com");
        }

        @Test
        @DisplayName("Should handle empty accessible IDs list")
        void shouldHandleEmptyAccessibleIds() {
            // Given: Empty list of accessible IDs
            List<Long> emptyIds = List.of();
            Pageable pageable = PageRequest.of(0, 10);

            // When: Query with empty IDs list
            Page<Employee> result = employeeRepository.findBenchEmployeesByIds(
                    emptyIds, null, null, currentYear, currentMonth, pageable
            );

            // Then: No results (as per MEMORY.md - native SQL can't handle IN ())
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should handle NULL search parameter correctly")
        void shouldHandleNullSearch() {
            // When: Query with NULL search
            Pageable pageable = PageRequest.of(0, 10);
            Page<Employee> result = employeeRepository.findBenchEmployees(
                    null, null, currentYear, currentMonth, pageable
            );

            // Then: All BENCH employees are returned (NULL means no filter)
            assertThat(result.getContent()).isNotEmpty();
        }
    }
}
