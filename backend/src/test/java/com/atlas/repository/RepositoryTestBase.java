package com.atlas.repository;

import com.atlas.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for repository tests with common test data setup.
 * Sets up a realistic employee hierarchy with allocations for testing
 * filtration logic.
 */
@DataJpaTest
@ActiveProfiles("test")
public abstract class RepositoryTestBase {

        @Autowired
        protected EmployeeRepository employeeRepository;

        @Autowired
        protected AllocationRepository allocationRepository;

        @Autowired
        protected ProjectRepository projectRepository;

        @Autowired
        protected TechTowerRepository techTowerRepository;

        @Autowired
        protected MonthlyAllocationRepository monthlyAllocationRepository;

        // Test data holders
        protected Employee ceo;
        protected Employee manager1;
        protected Employee manager2;
        protected Employee employeeBench;
        protected Employee employeeActive;
        protected Employee employeeProspect;
        protected Employee employeeMaternity;
        protected Employee employeeVacation;
        protected Employee employeeMultipleProjects;
        protected Employee employeeResigned;
        protected Employee employeePastProspect;

        protected Project project1;
        protected Project project2;
        protected Project project3;
        protected Project projectInactive;

        protected TechTower towerEPIS;
        protected TechTower towerApplication;

        protected int currentYear;
        protected int currentMonth;

        @BeforeEach
        public void setUp() {
                currentYear = LocalDate.now().getYear();
                currentMonth = LocalDate.now().getMonthValue();

                // Create tech towers
                towerEPIS = TechTower.builder()
                                .description("EPIS")
                                .build();
                towerEPIS = techTowerRepository.save(towerEPIS);

                towerApplication = TechTower.builder()
                                .description("Application")
                                .parentTower(towerEPIS)
                                .build();
                towerApplication = techTowerRepository.save(towerApplication);

                // Create employee hierarchy
                createEmployees();

                // Create projects
                createProjects();

                // Create allocations
                createAllocations();
        }

        private void createEmployees() {
                // CEO (no manager, no tower - sees all)
                ceo = Employee.builder()
                                .oracleId(1000)
                                .name("CEO User")
                                .email("ceo@atlas.com")
                                .title("Chief Executive Officer")
                                .grade("C")
                                .jobLevel(Employee.JobLevel.EXECUTIVE_LEVEL)
                                .hiringType(Employee.HiringType.FULL_TIME)
                                .location("Cairo")
                                .legalEntity("GS")
                                .nationality("Egyptian")
                                .hireDate(LocalDate.now().minusYears(10))
                                .gender(Employee.Gender.MALE)
                                .build();
                ceo = employeeRepository.save(ceo);

                // Manager 1 (reports to CEO)
                manager1 = Employee.builder()
                                .oracleId(1001)
                                .name("Manager One")
                                .email("manager1@atlas.com")
                                .title("Engineering Manager")
                                .grade("7")
                                .jobLevel(Employee.JobLevel.ADVANCED_MANAGER_LEVEL)
                                .hiringType(Employee.HiringType.FULL_TIME)
                                .location("Cairo")
                                .legalEntity("GS")
                                .nationality("Egyptian")
                                .hireDate(LocalDate.now().minusYears(5))
                                .gender(Employee.Gender.MALE)
                                .manager(ceo)
                                .tower(towerEPIS)
                                .build();
                manager1 = employeeRepository.save(manager1);

                // Manager 2 (reports to CEO, different tower)
                manager2 = Employee.builder()
                                .oracleId(1002)
                                .name("Manager Two")
                                .email("manager2@atlas.com")
                                .title("Product Manager")
                                .grade("7")
                                .jobLevel(Employee.JobLevel.ADVANCED_MANAGER_LEVEL)
                                .hiringType(Employee.HiringType.FULL_TIME)
                                .location("Cairo")
                                .legalEntity("GS")
                                .nationality("Egyptian")
                                .hireDate(LocalDate.now().minusYears(5))
                                .gender(Employee.Gender.FEMALE)
                                .manager(ceo)
                                .tower(towerApplication)
                                .build();
                manager2 = employeeRepository.save(manager2);

                // BENCH employee (no allocations)
                employeeBench = Employee.builder()
                                .oracleId(2000)
                                .name("Bench Employee")
                                .email("bench@atlas.com")
                                .title("Software Engineer")
                                .grade("5")
                                .jobLevel(Employee.JobLevel.MID_LEVEL)
                                .hiringType(Employee.HiringType.FULL_TIME)
                                .location("Cairo")
                                .legalEntity("GS")
                                .nationality("Egyptian")
                                .hireDate(LocalDate.now().minusYears(2))
                                .gender(Employee.Gender.MALE)
                                .manager(manager1)
                                .tower(towerEPIS)
                                .build();
                employeeBench = employeeRepository.save(employeeBench);

                // ACTIVE employee (has PROJECT allocation with % > 0 this month)
                employeeActive = Employee.builder()
                                .oracleId(2001)
                                .name("Active Employee")
                                .email("active@atlas.com")
                                .title("Senior Software Engineer")
                                .grade("6")
                                .jobLevel(Employee.JobLevel.MID_LEVEL)
                                .hiringType(Employee.HiringType.FULL_TIME)
                                .location("Cairo")
                                .legalEntity("GS")
                                .nationality("Egyptian")
                                .hireDate(LocalDate.now().minusYears(3))
                                .gender(Employee.Gender.MALE)
                                .manager(manager1)
                                .tower(towerEPIS)
                                .build();
                employeeActive = employeeRepository.save(employeeActive);

                // PROSPECT employee (has PROSPECT allocation, no active PROJECT)
                employeeProspect = Employee.builder()
                                .oracleId(2002)
                                .name("Prospect Employee")
                                .email("prospect@atlas.com")
                                .title("Software Engineer")
                                .grade("5")
                                .jobLevel(Employee.JobLevel.MID_LEVEL)
                                .hiringType(Employee.HiringType.FULL_TIME)
                                .location("Cairo")
                                .legalEntity("GS")
                                .nationality("Egyptian")
                                .hireDate(LocalDate.now().minusYears(1))
                                .gender(Employee.Gender.FEMALE)
                                .manager(manager1)
                                .tower(towerEPIS)
                                .build();
                employeeProspect = employeeRepository.save(employeeProspect);

                // PAST PROSPECT employee (has a PROSPECT allocation that ended in the past)
                employeePastProspect = Employee.builder()
                                .oracleId(2007)
                                .name("Past Prospect Employee")
                                .email("pastprospect@atlas.com")
                                .title("Software Engineer")
                                .grade("5")
                                .jobLevel(Employee.JobLevel.MID_LEVEL)
                                .hiringType(Employee.HiringType.FULL_TIME)
                                .location("Cairo")
                                .legalEntity("GS")
                                .nationality("Egyptian")
                                .hireDate(LocalDate.now().minusYears(2))
                                .gender(Employee.Gender.FEMALE)
                                .manager(manager1)
                                .tower(towerEPIS)
                                .build();
                employeePastProspect = employeeRepository.save(employeePastProspect);

                // MATERNITY employee (has MATERNITY allocation)
                employeeMaternity = Employee.builder()
                                .oracleId(2003)
                                .name("Maternity Employee")
                                .email("maternity@atlas.com")
                                .title("Software Engineer")
                                .grade("5")
                                .jobLevel(Employee.JobLevel.MID_LEVEL)
                                .hiringType(Employee.HiringType.FULL_TIME)
                                .location("Cairo")
                                .legalEntity("GS")
                                .nationality("Egyptian")
                                .hireDate(LocalDate.now().minusYears(2))
                                .gender(Employee.Gender.FEMALE)
                                .manager(manager2)
                                .tower(towerApplication)
                                .build();
                employeeMaternity = employeeRepository.save(employeeMaternity);

                // VACATION employee (has VACATION allocation)
                employeeVacation = Employee.builder()
                                .oracleId(2004)
                                .name("Vacation Employee")
                                .email("vacation@atlas.com")
                                .title("Software Engineer")
                                .grade("5")
                                .jobLevel(Employee.JobLevel.MID_LEVEL)
                                .hiringType(Employee.HiringType.FULL_TIME)
                                .location("Cairo")
                                .legalEntity("GS")
                                .nationality("Egyptian")
                                .hireDate(LocalDate.now().minusYears(2))
                                .gender(Employee.Gender.MALE)
                                .manager(manager2)
                                .tower(towerApplication)
                                .build();
                employeeVacation = employeeRepository.save(employeeVacation);

                // Employee with multiple projects
                employeeMultipleProjects = Employee.builder()
                                .oracleId(2005)
                                .name("Multi Project Employee")
                                .email("multi@atlas.com")
                                .title("Lead Software Engineer")
                                .grade("6")
                                .jobLevel(Employee.JobLevel.MID_LEVEL)
                                .hiringType(Employee.HiringType.FULL_TIME)
                                .location("Cairo")
                                .legalEntity("GS")
                                .nationality("Egyptian")
                                .hireDate(LocalDate.now().minusYears(4))
                                .gender(Employee.Gender.MALE)
                                .manager(manager1)
                                .tower(towerEPIS)
                                .build();
                employeeMultipleProjects = employeeRepository.save(employeeMultipleProjects);

                // RESIGNED employee
                employeeResigned = Employee.builder()
                                .oracleId(2006)
                                .name("Resigned Employee")
                                .email("resigned@atlas.com")
                                .title("Software Engineer")
                                .grade("5")
                                .jobLevel(Employee.JobLevel.MID_LEVEL)
                                .hiringType(Employee.HiringType.FULL_TIME)
                                .location("Cairo")
                                .legalEntity("GS")
                                .nationality("Egyptian")
                                .hireDate(LocalDate.now().minusYears(3))
                                .resignationDate(LocalDate.now().minusMonths(1))
                                .gender(Employee.Gender.MALE)
                                .manager(manager1)
                                .tower(towerEPIS)
                                .build();
                employeeResigned = employeeRepository.save(employeeResigned);
        }

        private void createProjects() {
                project1 = Project.builder()
                                .projectId("PRJ-1000")
                                .description("Cloud Migration Project")
                                .projectType(Project.ProjectType.PROJECT)
                                .region("MEA")
                                .vertical("Energy")
                                .startDate(LocalDate.now().minusMonths(6))
                                .endDate(LocalDate.now().plusMonths(6))
                                .status(Project.ProjectStatus.ACTIVE)
                                .manager(manager1)
                                .build();
                project1 = projectRepository.save(project1);

                project2 = Project.builder()
                                .projectId("PRJ-1001")
                                .description("Mobile App Development")
                                .projectType(Project.ProjectType.PROJECT)
                                .region("Europe")
                                .vertical("Telecom")
                                .startDate(LocalDate.now().minusMonths(3))
                                .endDate(LocalDate.now().plusMonths(9))
                                .status(Project.ProjectStatus.ACTIVE)
                                .manager(manager2)
                                .build();
                project2 = projectRepository.save(project2);

                project3 = Project.builder()
                                .projectId("PRJ-1002")
                                .description("Data Analytics Platform")
                                .projectType(Project.ProjectType.OPPORTUNITY)
                                .region("MEA")
                                .vertical("Banking")
                                .startDate(LocalDate.now().minusMonths(1))
                                .endDate(LocalDate.now().plusMonths(12))
                                .status(Project.ProjectStatus.ACTIVE)
                                .manager(manager1)
                                .build();
                project3 = projectRepository.save(project3);

                projectInactive = Project.builder()
                                .projectId("PRJ-9999")
                                .description("Completed Legacy Project")
                                .projectType(Project.ProjectType.PROJECT)
                                .region("Asia")
                                .vertical("Government")
                                .startDate(LocalDate.now().minusYears(2))
                                .endDate(LocalDate.now().minusYears(1))
                                .status(Project.ProjectStatus.COMPLETED)
                                .manager(manager1)
                                .build();
                projectInactive = projectRepository.save(projectInactive);
        }

        private void createAllocations() {
                // ACTIVE employee - PROJECT allocation with 100% this month
                Allocation activeAllocation = Allocation.builder()
                                .employee(employeeActive)
                                .project(project1)
                                .allocationType(Allocation.AllocationType.PROJECT)
                                .startDate(LocalDate.now().minusMonths(3))
                                .endDate(LocalDate.now().plusMonths(3))
                                .build();
                activeAllocation = allocationRepository.save(activeAllocation);

                MonthlyAllocation monthlyActive = MonthlyAllocation.builder()
                                .allocation(activeAllocation)
                                .year(currentYear)
                                .month(currentMonth)
                                .percentage(100)
                                .build();
                monthlyAllocationRepository.save(monthlyActive);

                // PROSPECT employee - PROSPECT allocation only
                Allocation prospectAllocation = Allocation.builder()
                                .employee(employeeProspect)
                                .project(project2)
                                .allocationType(Allocation.AllocationType.PROSPECT)
                                .startDate(LocalDate.now().minusMonths(1))
                                .endDate(LocalDate.now().plusMonths(6))
                                .build();
                allocationRepository.save(prospectAllocation);

                // PAST PROSPECT employee - PROSPECT allocation that ended 3 months ago
                Allocation pastProspectAllocation = Allocation.builder()
                                .employee(employeePastProspect)
                                .project(project2)
                                .allocationType(Allocation.AllocationType.PROSPECT)
                                .startDate(LocalDate.now().minusMonths(6))
                                .endDate(LocalDate.now().minusMonths(3))
                                .build();
                allocationRepository.save(pastProspectAllocation);

                // MATERNITY employee - MATERNITY allocation
                Allocation maternityAllocation = Allocation.builder()
                                .employee(employeeMaternity)
                                .project(null)
                                .allocationType(Allocation.AllocationType.MATERNITY)
                                .startDate(LocalDate.now().minusMonths(2))
                                .endDate(LocalDate.now().plusMonths(4))
                                .build();
                allocationRepository.save(maternityAllocation);

                // VACATION employee - VACATION allocation
                Allocation vacationAllocation = Allocation.builder()
                                .employee(employeeVacation)
                                .project(null)
                                .allocationType(Allocation.AllocationType.VACATION)
                                .startDate(LocalDate.now().minusWeeks(1))
                                .endDate(LocalDate.now().plusWeeks(1))
                                .build();
                allocationRepository.save(vacationAllocation);

                // Multi-project employee - 2 active PROJECT allocations (50% + 50% = 100%)
                Allocation multiAllocation1 = Allocation.builder()
                                .employee(employeeMultipleProjects)
                                .project(project1)
                                .allocationType(Allocation.AllocationType.PROJECT)
                                .startDate(LocalDate.now().minusMonths(2))
                                .endDate(LocalDate.now().plusMonths(4))
                                .build();
                multiAllocation1 = allocationRepository.save(multiAllocation1);

                MonthlyAllocation monthlyMulti1 = MonthlyAllocation.builder()
                                .allocation(multiAllocation1)
                                .year(currentYear)
                                .month(currentMonth)
                                .percentage(50)
                                .build();
                monthlyAllocationRepository.save(monthlyMulti1);

                Allocation multiAllocation2 = Allocation.builder()
                                .employee(employeeMultipleProjects)
                                .project(project2)
                                .allocationType(Allocation.AllocationType.PROJECT)
                                .startDate(LocalDate.now().minusMonths(1))
                                .endDate(LocalDate.now().plusMonths(5))
                                .build();
                multiAllocation2 = allocationRepository.save(multiAllocation2);

                MonthlyAllocation monthlyMulti2 = MonthlyAllocation.builder()
                                .allocation(multiAllocation2)
                                .year(currentYear)
                                .month(currentMonth)
                                .percentage(50)
                                .build();
                monthlyAllocationRepository.save(monthlyMulti2);
        }

        /**
         * Helper to get all subordinate IDs for a manager (including indirect reports)
         */
        protected List<Long> getAccessibleEmployeeIds(Employee manager) {
                return employeeRepository.findAllSubordinateIds(manager.getId());
        }
}
