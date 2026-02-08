package com.atlas.config;

import com.atlas.entity.*;
import com.atlas.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import jakarta.transaction.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

        private final UserRepository userRepository;
        private final EmployeeRepository employeeRepository;
        private final ProjectRepository projectRepository;
        private final AllocationRepository allocationRepository;
        private final MonthlyAllocationRepository monthlyAllocationRepository;
        private final PasswordEncoder passwordEncoder;

        private final Random random = new Random(42);
        private int employeeCounter = 1000; // Sequential counter for unique oracle IDs

        private final String[] FIRST_NAMES = { "Ahmed", "Mohamed", "Omar", "Ali", "Hassan", "Mahmoud", "Youssef",
                        "Khaled",
                        "Sara", "Marwa", "Nour", "Fatma", "Aisha", "Layla", "Hana", "Dina", "Reem", "Mona" };
        private final String[] LAST_NAMES = { "Ibrahim", "Hassan", "Ali", "Mohamed", "Ahmed", "Mahmoud", "Abdel",
                        "El-Sayed", "Mostafa", "Salem", "Rashad", "Farouk", "Nasser", "Kamal" };
        private final String[] SKILLS = { "Java", "Python", "Angular", "React", "Cloud Services", "DevOps",
                        "Data Engineering",
                        "Machine Learning", "Cybersecurity", "Network Engineering", "Project Management", "Agile",
                        "Testing",
                        "Business Analysis" };
        private final String[] PARENT_TOWERS = { "EPIS", "Application", "Data&Agility", "OT" };
        private final String[][] TOWERS = {
                        { "Cloud & Core Infrastructure Services", "Network, Cybersecurity & Collaboration" },
                        { "Testing", "Development", "Quality Assurance" },
                        { "Agility", "Data Engineering", "Analytics" },
                        { "Automation and Control", "Industrial Systems" }
        };
        private final String[] LOCATIONS = { "Egypt", "KSA", "UAE" };
        private final String[] GRADES = { "3", "4", "5", "6", "7", "C" };

        @Override
        public void run(String... args) {
                if (userRepository.count() > 0) {
                        log.info("Data already initialized, skipping creation...");
                        fixData();
                        return;
                }

                log.info("Initializing sample data...");

                // Create managers first (10 total across N1-N4)
                List<Employee> managers = createManagers();

                // Create regular employees (190)
                List<Employee> employees = createEmployees(managers);

                // Create projects (15)
                List<Project> projects = createProjects(managers);

                // Create allocations
                createAllocations(employees, projects);

                // Create users for managers and admin
                createUsers(managers);

                // Run data fix to ensure consistency and add new statuses
                fixData();

                log.info("Sample data initialization complete!");
                log.info("Created {} managers, {} employees, {} projects", managers.size(), employees.size(),
                                projects.size());
        }

        @Transactional
        private void fixData() {
                log.info("Running data fix...");

                // 1. Fix allocations: ensure ACTIVE allocations have percentages, clear monthly
                // from PROSPECT
                List<Allocation> allocations = allocationRepository.findAll();
                Double[] validPercentages = { 0.25, 0.5, 0.75, 1.0 };
                int fixedCount = 0;
                int prospectCleanedCount = 0;
                int currentYear = LocalDate.now().getYear();

                for (int i = 0; i < allocations.size(); i++) {
                        Allocation allocation = allocations.get(i);
                        boolean changed = false;

                        if (allocation.getStatus() == Allocation.AllocationStatus.PROSPECT) {
                                // PROSPECT allocations should never have monthly allocations
                                if (!allocation.getMonthlyAllocations().isEmpty()) {
                                        monthlyAllocationRepository.deleteByAllocationId(allocation.getId());
                                        prospectCleanedCount++;
                                }
                        } else {
                                // For ACTIVE allocations, ensure they have monthly allocations
                                for (int month = 1; month <= 12; month++) {
                                        Double val = allocation.getAllocationForYearMonth(currentYear, month);
                                        if (val == null || val <= 0) {
                                                // Replace with a valid percentage
                                                allocation.setAllocationForYearMonth(currentYear, month,
                                                                validPercentages[random
                                                                                .nextInt(validPercentages.length)]);
                                                changed = true;
                                        }
                                }
                        }

                        if (changed) {
                                allocationRepository.save(allocation);
                                fixedCount++;
                        }
                }
                log.info("Fixed {} allocations, cleaned {} PROSPECT allocations", fixedCount, prospectCleanedCount);

                // 2. Fix projects: Set some to COMPLETED and ON_HOLD
                List<Project> projects = projectRepository.findAll();
                int completedCount = 0;
                int onHoldCount = 0;

                for (int i = 0; i < projects.size(); i++) {
                        Project project = projects.get(i);
                        if (i % 5 == 0) {
                                project.setStatus(Project.ProjectStatus.COMPLETED);
                                completedCount++;
                                projectRepository.save(project);
                        } else if (i % 5 == 1) {
                                project.setStatus(Project.ProjectStatus.ON_HOLD);
                                onHoldCount++;
                                projectRepository.save(project);
                        }
                }
                log.info("Set {} projects to COMPLETED, {} to ON_HOLD", completedCount, onHoldCount);

                // 3. Assign Maternity, Long Leave, and Resigned statuses to employees
                List<Employee> employees = employeeRepository.findAll();
                int maternityCount = 0;
                int longLeaveCount = 0;
                int resignedCount = 0;

                for (int i = 0; i < employees.size(); i++) {
                        Employee emp = employees.get(i);

                        // Reset to Active first
                        emp.setStatus(Employee.EmployeeStatus.ACTIVE);

                        if ("Female".equalsIgnoreCase(emp.getGender()) && i % 20 == 0) {
                                emp.setStatus(Employee.EmployeeStatus.MATERNITY);
                                maternityCount++;
                        } else if (i % 25 == 0) {
                                emp.setStatus(Employee.EmployeeStatus.LONG_LEAVE);
                                longLeaveCount++;
                        } else if (i % 30 == 0) {
                                emp.setStatus(Employee.EmployeeStatus.RESIGNED);
                                resignedCount++;
                        }
                        employeeRepository.save(emp);
                }
                log.info("Assigned {} to Maternity, {} to Long Leave, {} to Resigned",
                                maternityCount, longLeaveCount, resignedCount);
        }

        private List<Employee> createManagers() {
                List<Employee> managers = new ArrayList<>();

                // N1 - Executive (1) - Can oversee multiple towers
                // N1 doesn't have a specific tower constraint
                Employee n1 = createEmployee("Ahmed", "El-Sayed", "N1", "Executive", "Chief Technology Officer",
                                null, null, null); // N1 oversees all towers
                managers.add(n1);

                // N2 - Heads (4) - One per parent tower, each locked to their tower
                Employee n2_epis = createEmployee("Mohamed", "Hassan", "N2", "Head", "VP of Infrastructure",
                                PARENT_TOWERS[0], TOWERS[0][0], n1); // EPIS
                Employee n2_app = createEmployee("Sara", "Ibrahim", "N2", "Head", "VP of Applications",
                                PARENT_TOWERS[1], TOWERS[1][0], n1); // Application
                Employee n2_data = createEmployee("Khaled", "Nasser", "N2", "Head", "VP of Data & Agility",
                                PARENT_TOWERS[2], TOWERS[2][0], n1); // Data&Agility
                Employee n2_ot = createEmployee("Hassan", "Farouk", "N2", "Head", "VP of Operations Technology",
                                PARENT_TOWERS[3], TOWERS[3][0], n1); // OT
                managers.add(n2_epis);
                managers.add(n2_app);
                managers.add(n2_data);
                managers.add(n2_ot);

                // N3 - Department Managers (8) - Two per parent tower, inheriting N2's exact
                // tower
                // Each N3 is in the SAME tower as their N2 manager
                // EPIS N3s (under n2_epis)
                Employee n3_epis_1 = createEmployee("Omar", "Mohamed", "N3", "Department Manager",
                                "Cloud Services Manager",
                                n2_epis.getParentTower(), n2_epis.getTower(), n2_epis);
                Employee n3_epis_2 = createEmployee("Ali", "Mahmoud", "N3", "Department Manager",
                                "Infrastructure Manager",
                                n2_epis.getParentTower(), n2_epis.getTower(), n2_epis);

                // Application N3s (under n2_app)
                Employee n3_app_1 = createEmployee("Marwa", "Ali", "N3", "Department Manager", "Development Manager",
                                n2_app.getParentTower(), n2_app.getTower(), n2_app);
                Employee n3_app_2 = createEmployee("Dina", "Salem", "N3", "Department Manager", "QA Manager",
                                n2_app.getParentTower(), n2_app.getTower(), n2_app);

                // Data&Agility N3s (under n2_data)
                Employee n3_data_1 = createEmployee("Youssef", "Kamal", "N3", "Department Manager", "Agility Manager",
                                n2_data.getParentTower(), n2_data.getTower(), n2_data);
                Employee n3_data_2 = createEmployee("Reem", "Rashad", "N3", "Department Manager", "Scrum Manager",
                                n2_data.getParentTower(), n2_data.getTower(), n2_data);

                // OT N3s (under n2_ot)
                Employee n3_ot_1 = createEmployee("Mahmoud", "Ibrahim", "N3", "Department Manager",
                                "Automation Manager",
                                n2_ot.getParentTower(), n2_ot.getTower(), n2_ot);
                Employee n3_ot_2 = createEmployee("Layla", "Hassan", "N3", "Department Manager",
                                "Control Systems Manager",
                                n2_ot.getParentTower(), n2_ot.getTower(), n2_ot);

                managers.add(n3_epis_1);
                managers.add(n3_epis_2);
                managers.add(n3_app_1);
                managers.add(n3_app_2);
                managers.add(n3_data_1);
                managers.add(n3_data_2);
                managers.add(n3_ot_1);
                managers.add(n3_ot_2);

                // N4 - Team Leads (8) - One per N3, inheriting their manager's tower exactly
                Employee n4_1 = createEmployee("Nour", "Mostafa", "N4", "Team Lead", "Cloud Team Lead",
                                n3_epis_1.getParentTower(), n3_epis_1.getTower(), n3_epis_1);
                Employee n4_2 = createEmployee("Fatma", "Ahmed", "N4", "Team Lead", "Security Team Lead",
                                n3_epis_2.getParentTower(), n3_epis_2.getTower(), n3_epis_2);
                Employee n4_3 = createEmployee("Hana", "Mohamed", "N4", "Team Lead", "Frontend Team Lead",
                                n3_app_1.getParentTower(), n3_app_1.getTower(), n3_app_1);
                Employee n4_4 = createEmployee("Aisha", "Ali", "N4", "Team Lead", "Testing Team Lead",
                                n3_app_2.getParentTower(), n3_app_2.getTower(), n3_app_2);
                Employee n4_5 = createEmployee("Mona", "Hassan", "N4", "Team Lead", "Scrum Master",
                                n3_data_1.getParentTower(), n3_data_1.getTower(), n3_data_1);
                Employee n4_6 = createEmployee("Ahmed", "Abdel", "N4", "Team Lead", "Data Engineering Lead",
                                n3_data_2.getParentTower(), n3_data_2.getTower(), n3_data_2);
                Employee n4_7 = createEmployee("Mohamed", "Kamal", "N4", "Team Lead", "PLC Team Lead",
                                n3_ot_1.getParentTower(), n3_ot_1.getTower(), n3_ot_1);
                Employee n4_8 = createEmployee("Omar", "Salem", "N4", "Team Lead", "SCADA Team Lead",
                                n3_ot_2.getParentTower(), n3_ot_2.getTower(), n3_ot_2);

                managers.add(n4_1);
                managers.add(n4_2);
                managers.add(n4_3);
                managers.add(n4_4);
                managers.add(n4_5);
                managers.add(n4_6);
                managers.add(n4_7);
                managers.add(n4_8);

                return managers;
        }

        private List<Employee> createEmployees(List<Employee> managers) {
                List<Employee> employees = new ArrayList<>();

                // Get N3 and N4 managers (indices 5-12 for N3, 13-20 for N4)
                // Structure: [0]=N1, [1-4]=N2s, [5-12]=N3s, [13-20]=N4s
                List<Employee> teamManagers = new ArrayList<>();
                for (int i = 5; i < managers.size(); i++) {
                        teamManagers.add(managers.get(i)); // N3 and N4 managers
                }

                // Create 190 employees, distributed among N3 and N4 managers
                // Each employee inherits their manager's tower
                for (int i = 0; i < 190; i++) {
                        String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
                        String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
                        String grade = GRADES[random.nextInt(GRADES.length)];

                        // Select a manager from N3 or N4 level
                        Employee manager = teamManagers.get(i % teamManagers.size());

                        // Employee inherits manager's tower - this is the key fix
                        String parentTower = manager.getParentTower();
                        String tower = manager.getTower();

                        Employee emp = createEmployee(firstName, lastName, grade, "Specialist",
                                        SKILLS[random.nextInt(SKILLS.length)] + " Engineer",
                                        parentTower, tower, manager);
                        employees.add(emp);
                }

                return employees;
        }

        private Employee createEmployee(String firstName, String lastName, String grade, String jobLevel,
                        String title, String parentTower, String tower, Employee manager) {
                String name = firstName + " " + lastName;
                String email = (firstName.toLowerCase() + "." + lastName.toLowerCase() + random.nextInt(1000)
                                + "@company.com")
                                .replace(" ", "");

                Employee employee = Employee.builder()
                                .oracleId(String.valueOf(employeeCounter++))
                                .name(name)
                                .gender(Arrays
                                                .asList("Sara", "Marwa", "Nour", "Fatma", "Aisha", "Layla", "Hana",
                                                                "Dina", "Reem", "Mona")
                                                .contains(firstName) ? "Female" : "Male")
                                .grade(grade)
                                .jobLevel(jobLevel)
                                .title(title)
                                .primarySkill(SKILLS[random.nextInt(SKILLS.length)])
                                .secondarySkill(SKILLS[random.nextInt(SKILLS.length)])
                                .hiringType("On-Payroll")
                                .location(LOCATIONS[random.nextInt(LOCATIONS.length)])
                                .legalEntity("GS")
                                .nationality("Egyptian")
                                .hireDate(LocalDate.now().minusDays(random.nextInt(2000)))
                                .email(email)
                                .parentTower(parentTower)
                                .tower(tower)
                                .manager(manager)
                                .isActive(true)
                                .build();

                return employeeRepository.save(employee);
        }

        private List<Project> createProjects(List<Employee> managers) {
                List<Project> projects = new ArrayList<>();

                // Projects organized by parent tower
                String[][] projectsByTower = {
                                // EPIS projects
                                { "Cloud Migration", "Network Security", "Infrastructure Upgrade",
                                                "Cybersecurity Platform" },
                                // Application projects
                                { "Digital Transformation", "Mobile App", "Customer Portal", "ERP Integration" },
                                // Data&Agility projects
                                { "Data Lake", "AI Platform", "Analytics Dashboard", "ML Pipeline" },
                                // OT projects
                                { "Smart Meters", "IoT Platform", "SCADA Modernization", "Industrial Automation" }
                };

                int projectCounter = 1000;

                // Create projects for each parent tower, managed by managers from that tower
                for (int towerIdx = 0; towerIdx < PARENT_TOWERS.length; towerIdx++) {
                        String parentTower = PARENT_TOWERS[towerIdx];
                        String[] towerProjects = projectsByTower[towerIdx];

                        // Find N3 managers for this tower (indices 5-12, 2 per tower)
                        // N3 managers: [5,6]=EPIS, [7,8]=App, [9,10]=Data, [11,12]=OT
                        int n3StartIdx = 5 + (towerIdx * 2);
                        List<Employee> towerManagers = new ArrayList<>();
                        towerManagers.add(managers.get(n3StartIdx));
                        towerManagers.add(managers.get(n3StartIdx + 1));

                        for (int p = 0; p < towerProjects.length; p++) {
                                // Alternate between the two N3 managers for this tower
                                Employee projectManager = towerManagers.get(p % 2);

                                Project project = Project.builder()
                                                .projectId("PRJ-" + projectCounter++)
                                                .name(towerProjects[p])
                                                .description("Project for " + towerProjects[p])
                                                .parentTower(parentTower)
                                                .tower(projectManager.getTower()) // Project in manager's tower
                                                .startDate(LocalDate.now().minusMonths(random.nextInt(12)))
                                                .endDate(LocalDate.now().plusMonths(random.nextInt(12)))
                                                .status(Project.ProjectStatus.ACTIVE)
                                                .manager(projectManager)
                                                .build();

                                projects.add(projectRepository.save(project));
                        }
                }

                return projects;
        }

        private void createAllocations(List<Employee> employees, List<Project> projects) {
                // Only valid percentage values (no B or P - those are handled by status)
                Double[] allocValues = { 1.0, 0.75, 0.5, 0.25 };
                int currentYear = LocalDate.now().getYear();

                for (Employee employee : employees) {
                        // Each employee gets 1-2 allocations
                        int numAllocations = 1 + random.nextInt(2);
                        Set<Long> usedProjects = new HashSet<>();

                        for (int i = 0; i < numAllocations; i++) {
                                Project project = projects.get(random.nextInt(projects.size()));
                                if (usedProjects.contains(project.getId()))
                                        continue;
                                usedProjects.add(project.getId());

                                Allocation allocation = Allocation.builder()
                                                .employee(employee)
                                                .project(project)
                                                .startDate(LocalDate.now().minusMonths(random.nextInt(6)))
                                                .endDate(LocalDate.now().plusMonths(random.nextInt(6)))
                                                .status(Allocation.AllocationStatus.ACTIVE)
                                                .build();

                                // Save allocation first
                                allocation = allocationRepository.save(allocation);

                                // Set allocation for each month with valid percentage values
                                for (int month = 1; month <= 12; month++) {
                                        Double alloc = allocValues[random.nextInt(allocValues.length)];
                                        allocation.setAllocationForYearMonth(currentYear, month, alloc);
                                }

                                allocationRepository.save(allocation);
                        }
                }
        }

        private void createUsers(List<Employee> managers) {
                // Create admin user
                User admin = User.builder()
                                .username("admin")
                                .password(passwordEncoder.encode("admin123"))
                                .email("admin@company.com")
                                .role(User.Role.SYSTEM_ADMIN)
                                .managerLevel(0)
                                .isActive(true)
                                .build();
                userRepository.save(admin);

                // Create user for each manager based on their grade/level
                // Structure: [0]=N1, [1-4]=N2s, [5-12]=N3s, [13-20]=N4s
                for (int i = 0; i < managers.size(); i++) {
                        Employee manager = managers.get(i);

                        // Determine role based on position in the list
                        User.Role role;
                        if (i == 0) {
                                role = User.Role.EXECUTIVE; // N1
                        } else if (i <= 4) {
                                role = User.Role.HEAD; // N2s (indices 1-4)
                        } else if (i <= 12) {
                                role = User.Role.DEPARTMENT_MANAGER; // N3s (indices 5-12)
                        } else {
                                role = User.Role.TEAM_LEAD; // N4s (indices 13-20)
                        }

                        User user = User.builder()
                                        .username(manager.getName().toLowerCase().replace(" ", "."))
                                        .password(passwordEncoder.encode("password123"))
                                        .email(manager.getEmail())
                                        .role(role)
                                        .managerLevel(role.getLevel())
                                        .employee(manager)
                                        .isActive(true)
                                        .build();
                        userRepository.save(user);
                }

                log.info("Created admin user and {} manager users", managers.size());
        }
}
