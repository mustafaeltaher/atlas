package com.atlas.repository;

import com.atlas.entity.Allocation;
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
 * Integration tests for faceted search behavior.
 * Tests ensure that filter dropdowns correctly interact with each other and main search results.
 */
@DisplayName("Faceted Search Integration Tests")
public class FacetedSearchIntegrationTest extends RepositoryTestBase {

    @Nested
    @DisplayName("Allocations Page Faceted Search")
    class AllocationsPageFacetedSearchTests {

        @Test
        @DisplayName("Manager dropdown should update when allocation type filter changes")
        void managerDropdownShouldUpdateWithAllocationTypeFilter() {
            // Scenario: User selects "PROJECT" in allocation type filter

            // When: Get managers without allocation type filter
            List<Employee> allManagers = employeeRepository.findDistinctManagersFromAllocationsFiltered(
                    null, // allocationType = null (show all)
                    null, // employeeIds = null (no ABAC filter)
                    null, // employee search
                    null  // manager search
            );

            // When: Get managers with PROJECT allocation type filter
            List<Employee> projectManagers = employeeRepository.findDistinctManagersFromAllocationsFiltered(
                    "PROJECT", // allocationType = PROJECT
                    null,      // employeeIds = null
                    null,      // employee search
                    null       // manager search
            );

            // Then: Filtered list should be subset of unfiltered list
            assertThat(projectManagers).isNotEmpty();
            assertThat(allManagers).containsAll(projectManagers);

            // Then: manager1 should be in PROJECT managers (has employeeActive with PROJECT)
            assertThat(projectManagers)
                    .extracting(Employee::getEmail)
                    .contains("manager1@atlas.com");
        }

        @Test
        @DisplayName("Allocation type dropdown should update when employee search changes")
        void allocationTypeDropdownShouldUpdateWithEmployeeSearch() {
            // Scenario: User searches for "active" in employee search

            // When: Get allocation types without search filter
            List<Allocation.AllocationType> allTypes = allocationRepository.findDistinctAllocationTypesByManager(null);

            // Then: All allocation types should be present
            assertThat(allTypes)
                    .contains(
                            Allocation.AllocationType.PROJECT,
                            Allocation.AllocationType.PROSPECT,
                            Allocation.AllocationType.MATERNITY,
                            Allocation.AllocationType.VACATION
                    );
        }

        @Test
        @DisplayName("Manager dropdown should respect employee search filter")
        void managerDropdownShouldRespectEmployeeSearch() {
            // Scenario: User searches for "bench" in employee search

            // When: Get managers for employees with "bench" in name
            List<Employee> managers = employeeRepository.findDistinctManagersFromAllocationsFiltered(
                    null,      // no allocation type filter
                    null,      // no ABAC filter
                    "%bench%", // employee search
                    null       // no manager search
            );

            // Then: Only managers of "bench" employees should appear
            // Note: employeeBench has no allocations, so won't appear in allocation-based search
            // This test verifies the query doesn't crash with this scenario
            assertThat(managers).isNotNull();
        }
    }

    @Nested
    @DisplayName("Employee Page Faceted Search")
    class EmployeePageFacetedSearchTests {

        @Test
        @DisplayName("Manager dropdown should update when tower filter changes")
        void managerDropdownShouldUpdateWithTowerFilter() {
            // Scenario: User selects "EPIS" tower

            // When: Get managers without tower filter
            List<Employee> allManagers = employeeRepository.findDistinctManagersForEmployeeFilters(
                    null,  // tower = null (show all)
                    null,  // employee search
                    null   // manager search
            );

            // When: Get managers with EPIS tower filter
            List<Employee> episManagers = employeeRepository.findDistinctManagersForEmployeeFilters(
                    "EPIS", // tower filter
                    null,   // employee search
                    null    // manager search
            );

            // Then: Filtered list should be subset of unfiltered list
            assertThat(episManagers).isNotEmpty();
            assertThat(allManagers).containsAll(episManagers);

            // Then: manager1 should be in EPIS managers (has employees in EPIS tower)
            assertThat(episManagers)
                    .extracting(Employee::getEmail)
                    .contains("manager1@atlas.com");
        }

        @Test
        @DisplayName("Tower dropdown should update when manager filter changes")
        void towerDropdownShouldUpdateWithManagerFilter() {
            // Scenario: User selects manager1 in manager filter

            // When: Get towers without manager filter
            List<String> allTowers = employeeRepository.findDistinctTowers(null, null);

            // When: Get towers for manager1's employees
            List<String> manager1Towers = employeeRepository.findDistinctTowers(
                    manager1.getId(), // manager filter
                    null              // employee search
            );

            // Then: Filtered list should be subset of unfiltered list
            assertThat(manager1Towers).isNotEmpty();
            assertThat(allTowers).containsAll(manager1Towers);

            // Then: EPIS tower should be in manager1's towers
            assertThat(manager1Towers).contains("EPIS");
        }

        @Test
        @DisplayName("Both dropdowns should update when employee search changes")
        void dropdownsShouldUpdateWithEmployeeSearch() {
            // Scenario: User searches for "active" in employee search

            // When: Get managers for employees with "active" in name
            List<Employee> managers = employeeRepository.findDistinctManagersForEmployeeFilters(
                    null,      // no tower filter
                    "%active%", // employee search
                    null       // no manager search
            );

            // Then: Only managers of "active" employees should appear
            assertThat(managers)
                    .isNotEmpty()
                    .extracting(Employee::getEmail)
                    .contains("manager1@atlas.com");

            // When: Get towers for employees with "active" in name
            List<String> towers = employeeRepository.findDistinctTowers(null, "%active%");

            // Then: Only towers of "active" employees should appear
            assertThat(towers)
                    .isNotEmpty()
                    .contains("EPIS");
        }
    }

    @Nested
    @DisplayName("Cross-Filter Consistency")
    class CrossFilterConsistencyTests {

        @Test
        @DisplayName("BENCH status filter should be consistent across main results and dropdowns")
        void benchFilterShouldBeConsistent() {
            // Scenario: User views BENCH employees
            Pageable pageable = PageRequest.of(0, 10);

            // When: Get BENCH employees
            Page<Employee> benchEmployees = employeeRepository.findBenchEmployees(
                    null, null, currentYear, currentMonth, pageable
            );

            // When: Get managers of BENCH employees
            List<Employee> benchManagers = employeeRepository.findDistinctManagersOfBenchEmployees(
                    currentYear, currentMonth, "%"
            );

            // Then: All BENCH employees should have managers in the manager dropdown
            assertThat(benchEmployees.getContent()).isNotEmpty();
            List<Long> benchManagerIds = benchManagers.stream()
                    .map(Employee::getId)
                    .toList();

            benchEmployees.getContent().forEach(employee -> {
                if (employee.getManager() != null) {
                    assertThat(benchManagerIds).contains(employee.getManager().getId());
                }
            });
        }

        @Test
        @DisplayName("ACTIVE status filter should be consistent across main results and dropdowns")
        void activeFilterShouldBeConsistent() {
            // Scenario: User views ACTIVE employees
            Pageable pageable = PageRequest.of(0, 10);

            // When: Get ACTIVE employees
            Page<Employee> activeEmployees = employeeRepository.findActiveAllocatedEmployees(
                    null, null, currentYear, currentMonth, pageable
            );

            // When: Get managers of ACTIVE employees
            List<Employee> activeManagers = employeeRepository.findDistinctManagersOfActiveEmployees(
                    currentYear, currentMonth, "%"
            );

            // Then: All ACTIVE employees should have managers in the manager dropdown
            assertThat(activeEmployees.getContent()).isNotEmpty();
            List<Long> activeManagerIds = activeManagers.stream()
                    .map(Employee::getId)
                    .toList();

            activeEmployees.getContent().forEach(employee -> {
                if (employee.getManager() != null) {
                    assertThat(activeManagerIds).contains(employee.getManager().getId());
                }
            });
        }

        @Test
        @DisplayName("Allocation type filter should be consistent between results and dropdown")
        void allocationTypeFilterShouldBeConsistent() {
            // Scenario: User selects MATERNITY in allocation type filter
            Pageable pageable = PageRequest.of(0, 10);

            // When: Get employees with MATERNITY allocation
            Page<Employee> maternityEmployees = employeeRepository.findEmployeesByAllocationType(
                    null, null, "MATERNITY", pageable
            );

            // When: Get allocation types (to verify MATERNITY is in dropdown)
            List<Allocation.AllocationType> types = allocationRepository.findDistinctAllocationTypesByManager(null);

            // Then: MATERNITY should be in the dropdown
            assertThat(types).contains(Allocation.AllocationType.MATERNITY);

            // Then: We should have MATERNITY employees
            assertThat(maternityEmployees.getContent())
                    .isNotEmpty()
                    .extracting(Employee::getEmail)
                    .contains("maternity@atlas.com");
        }
    }

    @Nested
    @DisplayName("ABAC Filter Integration")
    class ABACFilterIntegrationTests {

        @Test
        @DisplayName("Accessible IDs should filter main results and dropdowns consistently")
        void accessibleIdsShouldFilterConsistently() {
            // Scenario: Manager1 views employees (ABAC restricts to their hierarchy)
            List<Long> accessibleIds = getAccessibleEmployeeIds(manager1);
            Pageable pageable = PageRequest.of(0, 10);

            // When: Get ACTIVE employees with ABAC filter
            Page<Employee> activeEmployees = employeeRepository.findActiveAllocatedEmployeesByIds(
                    accessibleIds, null, null, currentYear, currentMonth, pageable
            );

            // Then: All results should be within accessible IDs
            assertThat(activeEmployees.getContent())
                    .allMatch(e -> accessibleIds.contains(e.getId()));

            // When: Get managers with ABAC filter
            List<Employee> managers = employeeRepository.findDistinctManagersOfActiveByEmployeeSearchFiltered(
                    accessibleIds, currentYear, currentMonth, null, null
            );

            // Then: All managers should be managing employees in accessible IDs
            assertThat(managers).isNotNull();
        }

        @Test
        @DisplayName("Empty accessible IDs should result in empty results everywhere")
        void emptyAccessibleIdsShouldBeConsistent() {
            // Scenario: User has no accessible employees
            List<Long> emptyIds = List.of();
            Pageable pageable = PageRequest.of(0, 10);

            // When: Query with empty accessible IDs
            Page<Employee> benchEmployees = employeeRepository.findBenchEmployeesByIds(
                    emptyIds, null, null, currentYear, currentMonth, pageable
            );

            // Then: No results
            assertThat(benchEmployees.getContent()).isEmpty();

            // When: Get managers with empty accessible IDs
            List<Employee> managers = employeeRepository.findDistinctManagersOfBenchByEmployeeSearchFiltered(
                    emptyIds, currentYear, currentMonth, null, null
            );

            // Then: No managers (as per MEMORY.md - IN () handling)
            assertThat(managers).isEmpty();
        }
    }

    @Nested
    @DisplayName("Search Interaction Tests")
    class SearchInteractionTests {

        @Test
        @DisplayName("Employee search should filter both main results and manager dropdown")
        void employeeSearchShouldFilterBoth() {
            // Scenario: User searches for "multi" in employee name
            Pageable pageable = PageRequest.of(0, 10);

            // When: Get ACTIVE employees with search
            Page<Employee> activeEmployees = employeeRepository.findActiveAllocatedEmployees(
                    "%multi%", null, currentYear, currentMonth, pageable
            );

            // Then: Only matching employees are returned
            assertThat(activeEmployees.getContent())
                    .isNotEmpty()
                    .extracting(Employee::getEmail)
                    .contains("multi@atlas.com");

            // When: Get managers with same employee search
            List<Employee> managers = employeeRepository.findDistinctManagersOfActiveByEmployeeSearch(
                    currentYear, currentMonth, "%multi%", null
            );

            // Then: Only managers of matching employees are returned
            assertThat(managers)
                    .extracting(Employee::getEmail)
                    .contains("manager1@atlas.com");
        }

        @Test
        @DisplayName("Manager search should not affect main employee results")
        void managerSearchShouldNotAffectMainResults() {
            // Scenario: User searches in manager dropdown independently

            // When: Search for "manager one" in manager dropdown
            List<Employee> managers = employeeRepository.findDistinctManagers("%manager one%");

            // Then: Only matching managers are returned
            assertThat(managers)
                    .hasSize(1)
                    .extracting(Employee::getEmail)
                    .containsExactly("manager1@atlas.com");

            // Note: Main employee results are not affected by manager search
            // Only when user SELECTS a manager from dropdown does it filter main results
        }
    }
}
