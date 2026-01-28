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

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

        private final UserRepository userRepository;
        private final EmployeeRepository employeeRepository;
        private final ProjectRepository projectRepository;
        private final AllocationRepository allocationRepository;
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
                        log.info("Data already initialized, skipping...");
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

                log.info("Sample data initialization complete!");
                log.info("Created {} managers, {} employees, {} projects", managers.size(), employees.size(),
                                projects.size());
        }

        private List<Employee> createManagers() {
                List<Employee> managers = new ArrayList<>();

                // N1 - Executive (1)
                Employee n1 = createEmployee("Ahmed", "El-Sayed", "N1", "Executive", "Chief Technology Officer",
                                PARENT_TOWERS[0], TOWERS[0][0], null);
                managers.add(n1);

                // N2 - Heads (2)
                Employee n2_1 = createEmployee("Mohamed", "Hassan", "N2", "Head", "VP of Engineering",
                                PARENT_TOWERS[0], TOWERS[0][0], n1);
                Employee n2_2 = createEmployee("Sara", "Ibrahim", "N2", "Head", "VP of Applications",
                                PARENT_TOWERS[1], TOWERS[1][0], n1);
                managers.add(n2_1);
                managers.add(n2_2);

                // N3 - Department Managers (3)
                Employee n3_1 = createEmployee("Omar", "Mohamed", "N3", "Department Manager", "Engineering Manager",
                                PARENT_TOWERS[0], TOWERS[0][0], n2_1);
                Employee n3_2 = createEmployee("Marwa", "Ali", "N3", "Department Manager", "Development Manager",
                                PARENT_TOWERS[1], TOWERS[1][0], n2_2);
                Employee n3_3 = createEmployee("Khaled", "Mahmoud", "N3", "Department Manager", "Data Manager",
                                PARENT_TOWERS[2], TOWERS[2][0], n2_1);
                managers.add(n3_1);
                managers.add(n3_2);
                managers.add(n3_3);

                // N4 - Team Leads (4)
                Employee n4_1 = createEmployee("Youssef", "Rashad", "N4", "Team Lead", "Cloud Team Lead",
                                PARENT_TOWERS[0], TOWERS[0][0], n3_1);
                Employee n4_2 = createEmployee("Nour", "Salem", "N4", "Team Lead", "Testing Team Lead",
                                PARENT_TOWERS[1], TOWERS[1][0], n3_2);
                Employee n4_3 = createEmployee("Ali", "Hassan", "N4", "Team Lead", "DevOps Team Lead",
                                PARENT_TOWERS[0], TOWERS[0][1], n3_1);
                Employee n4_4 = createEmployee("Fatma", "Kamal", "N4", "Team Lead", "Analytics Team Lead",
                                PARENT_TOWERS[2], TOWERS[2][1], n3_3);
                managers.add(n4_1);
                managers.add(n4_2);
                managers.add(n4_3);
                managers.add(n4_4);

                return managers;
        }

        private List<Employee> createEmployees(List<Employee> managers) {
                List<Employee> employees = new ArrayList<>();

                for (int i = 0; i < 190; i++) {
                        String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
                        String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
                        String grade = GRADES[random.nextInt(GRADES.length)];
                        int parentTowerIdx = random.nextInt(PARENT_TOWERS.length);
                        String parentTower = PARENT_TOWERS[parentTowerIdx];
                        String tower = TOWERS[parentTowerIdx][random.nextInt(TOWERS[parentTowerIdx].length)];

                        Employee manager = managers.get(3 + random.nextInt(managers.size() - 3)); // Skip N1

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
                String[] projectNames = {
                                "Cloud Migration", "Digital Transformation", "Smart Meters", "MOFA Portal",
                                "Ethrai Platform", "MEP System", "Network Security", "Data Lake",
                                "Mobile App", "DevOps Pipeline", "AI Platform", "ERP Integration",
                                "Customer Portal", "IoT Platform", "Analytics Dashboard"
                };

                for (int i = 0; i < 15; i++) {
                        int parentTowerIdx = i % PARENT_TOWERS.length;
                        Project project = Project.builder()
                                        .projectId("PRJ-" + (1000 + i))
                                        .name(projectNames[i])
                                        .description("Project for " + projectNames[i])
                                        .parentTower(PARENT_TOWERS[parentTowerIdx])
                                        .tower(TOWERS[parentTowerIdx][i % TOWERS[parentTowerIdx].length])
                                        .startDate(LocalDate.now().minusMonths(random.nextInt(12)))
                                        .endDate(LocalDate.now().plusMonths(random.nextInt(12)))
                                        .status(Project.ProjectStatus.ACTIVE)
                                        .manager(managers.get(3 + random.nextInt(managers.size() - 3)))
                                        .build();

                        projects.add(projectRepository.save(project));
                }

                return projects;
        }

        private void createAllocations(List<Employee> employees, List<Project> projects) {
                String[] allocValues = { "1", "0.5", "0.25", "B", "P" };

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
                                                .confirmedAssignment(project.getName())
                                                .startDate(LocalDate.now().minusMonths(random.nextInt(6)))
                                                .endDate(LocalDate.now().plusMonths(random.nextInt(6)))
                                                .status(Allocation.AllocationStatus.ACTIVE)
                                                .build();

                                // Set allocation for each month
                                for (int month = 1; month <= 12; month++) {
                                        String alloc = allocValues[random.nextInt(allocValues.length)];
                                        allocation.setAllocationForMonth(month, alloc);
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

                // Create user for each manager
                User.Role[] roles = { User.Role.EXECUTIVE, User.Role.HEAD, User.Role.HEAD,
                                User.Role.DEPARTMENT_MANAGER, User.Role.DEPARTMENT_MANAGER,
                                User.Role.DEPARTMENT_MANAGER,
                                User.Role.TEAM_LEAD, User.Role.TEAM_LEAD, User.Role.TEAM_LEAD, User.Role.TEAM_LEAD };

                for (int i = 0; i < managers.size(); i++) {
                        Employee manager = managers.get(i);
                        User user = User.builder()
                                        .username(manager.getName().toLowerCase().replace(" ", "."))
                                        .password(passwordEncoder.encode("password123"))
                                        .email(manager.getEmail())
                                        .role(roles[i])
                                        .managerLevel(roles[i].getLevel())
                                        .employee(manager)
                                        .isActive(true)
                                        .build();
                        userRepository.save(user);
                }

                log.info("Created admin user and {} manager users", managers.size());
                log.info("Login credentials:");
                log.info("  Admin: admin / admin123");
                log.info("  N1 Manager: ahmed.el-sayed / password123");
                log.info("  N2 Manager: mohamed.hassan / password123");
        }
}
