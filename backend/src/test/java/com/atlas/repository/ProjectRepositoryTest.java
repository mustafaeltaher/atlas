package com.atlas.repository;

import com.atlas.entity.Project;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for ProjectRepository filtration and faceted search logic.
 */
@DisplayName("Project Repository Tests")
public class ProjectRepositoryTest extends RepositoryTestBase {

    @Nested
    @DisplayName("Basic Project Queries")
    class BasicQueryTests {

        @Test
        @DisplayName("Should find project by project ID string")
        void shouldFindByProjectId() {
            // When: Find by project ID string
            var result = projectRepository.findByProjectId("PRJ-1000");

            // Then: Project is found
            assertThat(result).isPresent();
            assertThat(result.get().getDescription()).isEqualTo("Cloud Migration Project");
        }

        @Test
        @DisplayName("Should find active projects")
        void shouldFindActiveProjects() {
            // When: Find all active projects
            List<Project> projects = projectRepository.findActiveProjects();

            // Then: Only active projects are returned
            assertThat(projects)
                    .isNotEmpty()
                    .allMatch(p -> p.getStatus() == Project.ProjectStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should count active projects")
        void shouldCountActiveProjects() {
            // When: Count active projects
            long count = projectRepository.countActiveProjects();

            // Then: Count matches active projects (3)
            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("Should check if project ID exists")
        void shouldCheckProjectIdExists() {
            // When: Check existing project ID
            boolean exists = projectRepository.existsByProjectId("PRJ-1000");

            // Then: Returns true
            assertThat(exists).isTrue();

            // When: Check non-existing project ID
            exists = projectRepository.existsByProjectId("PRJ-NONEXISTENT");

            // Then: Returns false
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("Employee-Based Project Queries")
    class EmployeeProjectTests {

        @Test
        @DisplayName("Should find active projects by employees")
        void shouldFindActiveProjectsByEmployees() {
            // Given: List of employees
            List<com.atlas.entity.Employee> employees = List.of(employeeActive, employeeMultipleProjects);

            // When: Find active projects for these employees
            List<Project> projects = projectRepository.findActiveProjectsByEmployees(employees);

            // Then: Projects are returned
            assertThat(projects).isNotEmpty();
        }

        @Test
        @DisplayName("Should find all projects by employees (including non-active)")
        void shouldFindProjectsByEmployees() {
            // Given: List of employees
            List<com.atlas.entity.Employee> employees = List.of(employeeActive);

            // When: Find all projects for these employees
            List<Project> projects = projectRepository.findProjectsByEmployees(employees);

            // Then: Projects are returned
            assertThat(projects).isNotEmpty();
        }

        @Test
        @DisplayName("Should count active projects by employees")
        void shouldCountActiveProjectsByEmployees() {
            // Given: List of employees
            List<com.atlas.entity.Employee> employees = List.of(employeeActive, employeeMultipleProjects);

            // When: Count active projects for these employees
            long count = projectRepository.countActiveProjectsByEmployees(employees);

            // Then: Count > 0
            assertThat(count).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should count active projects by employee IDs")
        void shouldCountActiveProjectsByEmployeeIds() {
            // Given: List of employee IDs
            List<Long> employeeIds = List.of(employeeActive.getId(), employeeMultipleProjects.getId());

            // When: Count active projects for these employee IDs
            long count = projectRepository.countActiveProjectsByEmployeeIds(employeeIds);

            // Then: Count > 0
            assertThat(count).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Faceted Search - Distinct Statuses")
    class DistinctStatusTests {

        @Test
        @DisplayName("Should find distinct statuses without filters")
        void shouldFindDistinctStatuses() {
            // When: Get all distinct statuses
            List<Project.ProjectStatus> statuses = projectRepository.findDistinctStatuses(null, null);

            // Then: All statuses present in data are returned
            assertThat(statuses)
                    .isNotEmpty()
                    .contains(Project.ProjectStatus.ACTIVE, Project.ProjectStatus.COMPLETED);
        }

        @Test
        @DisplayName("Should filter distinct statuses by region")
        void shouldFilterStatusesByRegion() {
            // When: Get statuses for MEA region
            List<Project.ProjectStatus> statuses = projectRepository.findDistinctStatuses("MEA", null);

            // Then: Only statuses from MEA projects are returned
            assertThat(statuses).isNotEmpty();
        }

        @Test
        @DisplayName("Should filter distinct statuses by search term")
        void shouldFilterStatusesBySearch() {
            // When: Search for "cloud"
            List<Project.ProjectStatus> statuses = projectRepository.findDistinctStatuses(null, "%cloud%");

            // Then: Status from Cloud Migration Project is returned
            assertThat(statuses)
                    .contains(Project.ProjectStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should filter distinct statuses by project IDs")
        void shouldFilterStatusesByIds() {
            // Given: List of project IDs
            List<Long> projectIds = List.of(project1.getId(), project2.getId());

            // When: Get statuses for these projects
            List<Project.ProjectStatus> statuses = projectRepository.findDistinctStatusesByIds(
                    projectIds, null, null
            );

            // Then: Only statuses from specified projects are returned
            assertThat(statuses)
                    .isNotEmpty()
                    .contains(Project.ProjectStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle NULL region filter")
        void shouldHandleNullRegion() {
            // When: Query with NULL region
            List<Project.ProjectStatus> statuses = projectRepository.findDistinctStatuses(null, null);

            // Then: All statuses are returned
            assertThat(statuses).isNotEmpty();
        }

        @Test
        @DisplayName("Should handle NULL search filter")
        void shouldHandleNullSearch() {
            // When: Query with NULL search
            List<Project.ProjectStatus> statuses = projectRepository.findDistinctStatuses("MEA", null);

            // Then: All MEA projects' statuses are returned
            assertThat(statuses).isNotEmpty();
        }

        @Test
        @DisplayName("Should handle combined filters")
        void shouldHandleCombinedFilters() {
            // When: Query with both region and search filters
            List<Project.ProjectStatus> statuses = projectRepository.findDistinctStatuses("MEA", "%cloud%");

            // Then: Filtered statuses are returned
            assertThat(statuses)
                    .contains(Project.ProjectStatus.ACTIVE);
        }
    }
}
