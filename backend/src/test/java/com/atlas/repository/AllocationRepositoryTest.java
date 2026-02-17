package com.atlas.repository;

import com.atlas.entity.Allocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for AllocationRepository filtration and faceted search logic.
 */
@DisplayName("Allocation Repository Tests")
public class AllocationRepositoryTest extends RepositoryTestBase {

    @Nested
    @DisplayName("Basic Allocation Queries")
    class BasicQueryTests {

        @Test
        @DisplayName("Should find all allocations with employee and project details")
        void shouldFindAllWithDetails() {
            // When: Query all allocations with details
            List<Allocation> allocations = allocationRepository.findAllWithEmployeeAndProject();

            // Then: All allocations are returned with eager-loaded relationships
            assertThat(allocations).isNotEmpty();
            assertThat(allocations)
                    .allMatch(a -> a.getEmployee() != null);
        }

        @Test
        @DisplayName("Should find allocations by employee ID")
        void shouldFindByEmployeeId() {
            // When: Query allocations for employeeActive
            List<Allocation> allocations = allocationRepository.findByEmployeeIdWithDetails(
                    employeeActive.getId()
            );

            // Then: Only employeeActive's allocations are returned
            assertThat(allocations)
                    .isNotEmpty()
                    .allMatch(a -> a.getEmployee().getId().equals(employeeActive.getId()));
        }

        @Test
        @DisplayName("Should find allocations by project ID")
        void shouldFindByProjectId() {
            // When: Query allocations for project1
            List<Allocation> allocations = allocationRepository.findByProjectIdWithDetails(
                    project1.getId()
            );

            // Then: Only project1's allocations are returned
            assertThat(allocations)
                    .isNotEmpty()
                    .allMatch(a -> a.getProject() != null && a.getProject().getId().equals(project1.getId()));
        }

        @Test
        @DisplayName("Should find allocations by multiple employee IDs")
        void shouldFindByEmployeeIds() {
            // Given: Multiple employee IDs
            List<Long> employeeIds = List.of(employeeActive.getId(), employeeMultipleProjects.getId());

            // When: Query allocations for these employees
            List<Allocation> allocations = allocationRepository.findByEmployeeIdsWithDetails(employeeIds);

            // Then: Allocations for both employees are returned
            assertThat(allocations)
                    .isNotEmpty()
                    .allMatch(a -> employeeIds.contains(a.getEmployee().getId()));
        }
    }

    @Nested
    @DisplayName("Project-Specific Allocation Queries")
    class ProjectAllocationTests {

        @Test
        @DisplayName("Should find PROJECT allocations by project ID")
        void shouldFindProjectAllocations() {
            // When: Query PROJECT allocations for project1
            List<Allocation> allocations = allocationRepository.findProjectAllocationsByProjectId(
                    project1.getId()
            );

            // Then: Only PROJECT allocations for project1 are returned
            assertThat(allocations)
                    .isNotEmpty()
                    .allMatch(a -> a.getAllocationType() == Allocation.AllocationType.PROJECT)
                    .allMatch(a -> a.getProject().getId().equals(project1.getId()));
        }

        @Test
        @DisplayName("Should find PROJECT allocations by multiple project IDs")
        void shouldFindProjectAllocationsByIds() {
            // Given: Multiple project IDs
            List<Long> projectIds = List.of(project1.getId(), project2.getId());

            // When: Query PROJECT allocations for these projects
            List<Allocation> allocations = allocationRepository.findProjectAllocationsByProjectIds(projectIds);

            // Then: PROJECT allocations for both projects are returned
            assertThat(allocations)
                    .isNotEmpty()
                    .allMatch(a -> a.getAllocationType() == Allocation.AllocationType.PROJECT)
                    .allMatch(a -> projectIds.contains(a.getProject().getId()));
        }
    }

    @Nested
    @DisplayName("Faceted Search - Distinct Allocation Types")
    class DistinctAllocationTypeTests {

        @Test
        @DisplayName("Should find distinct allocation types without manager filter")
        void shouldFindDistinctTypes() {
            // When: Get all distinct allocation types
            List<Allocation.AllocationType> types = allocationRepository.findDistinctAllocationTypesByManager(null);

            // Then: All allocation types present in data are returned
            assertThat(types)
                    .isNotEmpty()
                    .contains(
                            Allocation.AllocationType.PROJECT,
                            Allocation.AllocationType.PROSPECT,
                            Allocation.AllocationType.MATERNITY,
                            Allocation.AllocationType.VACATION
                    );
        }

        @Test
        @DisplayName("Should filter distinct allocation types by manager")
        void shouldFilterTypesByManager() {
            // When: Get allocation types for manager1's employees
            List<Allocation.AllocationType> types = allocationRepository.findDistinctAllocationTypesByManager(
                    manager1.getId()
            );

            // Then: Only types from manager1's employees are returned
            assertThat(types)
                    .isNotEmpty()
                    .contains(Allocation.AllocationType.PROJECT, Allocation.AllocationType.PROSPECT);
        }
    }

    @Nested
    @DisplayName("Employee-Based Allocation Queries")
    class EmployeeAllocationTests {

        @Test
        @DisplayName("Should find allocations by employee objects")
        void shouldFindAllocationsByEmployees() {
            // Given: List of employee objects
            List<com.atlas.entity.Employee> employees = List.of(employeeActive, employeeMultipleProjects);

            // When: Query allocations for these employees
            List<Allocation> allocations = allocationRepository.findAllocationsByEmployees(employees);

            // Then: Allocations for both employees are returned
            assertThat(allocations)
                    .isNotEmpty()
                    .allMatch(a -> employees.contains(a.getEmployee()));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle allocations without projects (MATERNITY, VACATION)")
        void shouldHandleAllocationsWithoutProjects() {
            // When: Query allocations for employeeMaternity
            List<Allocation> allocations = allocationRepository.findByEmployeeIdWithDetails(
                    employeeMaternity.getId()
            );

            // Then: Allocation is returned even though project is null
            assertThat(allocations)
                    .isNotEmpty()
                    .allMatch(a -> a.getAllocationType() == Allocation.AllocationType.MATERNITY)
                    .allMatch(a -> a.getProject() == null);
        }

        @Test
        @DisplayName("Should find allocation by ID with details")
        void shouldFindByIdWithDetails() {
            // Given: An allocation ID
            List<Allocation> allAllocations = allocationRepository.findAll();
            assertThat(allAllocations).isNotEmpty();
            Long allocationId = allAllocations.get(0).getId();

            // When: Find by ID with details
            var result = allocationRepository.findByIdWithDetails(allocationId);

            // Then: Allocation is found with relationships loaded
            assertThat(result).isPresent();
            assertThat(result.get().getEmployee()).isNotNull();
        }
    }
}
