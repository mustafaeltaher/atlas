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
        private final TechTowerRepository techTowerRepository;
        private final SkillRepository skillRepository;
        private final EmployeeSkillRepository employeeSkillRepository;
        private final PasswordEncoder passwordEncoder;

        private final Random random = new Random(42);
        private int employeeCounter = 1000; // Sequential counter for unique oracle IDs

        private final String[] FIRST_NAMES = { "Ahmed", "Mohamed", "Omar", "Ali", "Hassan", "Mahmoud", "Youssef",
                        "Khaled",
                        "Sara", "Marwa", "Nour", "Fatma", "Aisha", "Layla", "Hana", "Dina", "Reem", "Mona" };
        private final String[] LAST_NAMES = { "Ibrahim", "Hassan", "Ali", "Mohamed", "Ahmed", "Mahmoud", "Abdel",
                        "El-Sayed", "Mostafa", "Salem", "Rashad", "Farouk", "Nasser", "Kamal" };
        private final String[] PARENT_TOWER_NAMES = { "EPIS", "Application", "Data&Agility", "OT" };
        private final String[][] TOWER_NAMES = {
                        { "Cloud & Core Infrastructure Services", "Network, Cybersecurity & Collaboration" },
                        { "Testing", "Development", "Quality Assurance" },
                        { "Agility", "Data Engineering", "Analytics" },
                        { "Automation and Control", "Industrial Systems" }
        };
        private final String[] LOCATIONS = { "Egypt", "KSA", "UAE" };
        private final String[] GRADES = { "3", "4", "5", "6", "7", "C" };
        private final String[] REGIONS = { "MEA", "Europe", "Asia", "Americas" };
        private final String[] VERTICALS = { "Energy", "Telecom", "Banking", "Healthcare", "Government" };

        // Skills per child tower (indexed same as TOWER_NAMES)
        private final String[][] TOWER_SKILLS = {
                        // EPIS - Cloud & Core Infrastructure Services
                        { "AWS", "Azure", "GCP", "Docker", "Kubernetes", "Terraform", "Linux" },
                        // EPIS - Network, Cybersecurity & Collaboration
                        { "Cisco Networking", "Firewall Management", "SIEM", "Penetration Testing", "IAM",
                                        "Endpoint Security" },
                        // Application - Testing
                        { "Selenium", "JUnit", "Cypress", "Performance Testing", "API Testing", "Test Automation" },
                        // Application - Development
                        { "Java", "Spring Boot", "Angular", "React", "Python", "Node.js", "TypeScript" },
                        // Application - Quality Assurance
                        { "ISTQB", "Test Planning", "Regression Testing", "UAT", "BDD", "Defect Management" },
                        // Data&Agility - Agility
                        { "Scrum", "SAFe", "Kanban", "Jira", "Agile Coaching", "Product Ownership" },
                        // Data&Agility - Data Engineering
                        { "Spark", "Kafka", "Airflow", "Snowflake", "dbt", "SQL", "ETL" },
                        // Data&Agility - Analytics
                        { "Power BI", "Tableau", "Python Analytics", "R", "Machine Learning", "Statistics" },
                        // OT - Automation and Control
                        { "PLC Programming", "SCADA", "DCS", "HMI Design", "Industrial IoT", "Modbus" },
                        // OT - Industrial Systems
                        { "MES", "Historian", "OPC UA", "Safety Systems", "Control Valves", "Instrumentation" }
        };

        // Store created TechTower entities for reuse
        private final Map<String, TechTower> towerMap = new HashMap<>();
        // Store created Skill entities per tower for reuse
        private final Map<String, List<Skill>> skillsByTower = new HashMap<>();

        @Override
        public void run(String... args) {
                if (userRepository.count() > 0) {
                        log.info("Data already initialized, skipping creation...");
                        fixData();
                        return;
                }

                log.info("Initializing sample data...");

                // Create TechTower seed data first
                createTechTowers();

                // Create managers first (10 total across N1-N4)
                List<Employee> managers = createManagers();

                // Create regular employees (190)
                List<Employee> employees = createEmployees(managers);

                // Create projects (15)
                List<Project> projects = createProjects(managers);

                // Create allocations
                createAllocations(employees, projects);

                // Create skills and assign to employees
                List<Employee> allEmployees = new ArrayList<>(managers);
                allEmployees.addAll(employees);
                createEmployeeSkills(allEmployees);

                // Create users for managers (N1 = top-level, gets full access via hierarchy)
                createUsers(managers);

                // Run data fix to ensure consistency
                fixData();

                log.info("Sample data initialization complete!");
                log.info("Created {} managers, {} employees, {} projects, {} skills", managers.size(), employees.size(),
                                projects.size(), skillRepository.count());
        }

        private void createTechTowers() {
                log.info("Creating TechTower seed data...");

                int skillIdx = 0;
                for (int i = 0; i < PARENT_TOWER_NAMES.length; i++) {
                        // Create parent tower
                        TechTower parentTower = TechTower.builder()
                                        .description(PARENT_TOWER_NAMES[i])
                                        .parentTower(null)
                                        .build();
                        parentTower = techTowerRepository.save(parentTower);
                        towerMap.put(PARENT_TOWER_NAMES[i], parentTower);

                        // Create child towers under this parent, with skills
                        for (String childName : TOWER_NAMES[i]) {
                                TechTower childTower = TechTower.builder()
                                                .description(childName)
                                                .parentTower(parentTower)
                                                .build();
                                childTower = techTowerRepository.save(childTower);
                                towerMap.put(childName, childTower);

                                // Create skills for this child tower
                                List<Skill> towerSkills = new ArrayList<>();
                                if (skillIdx < TOWER_SKILLS.length) {
                                        for (String skillName : TOWER_SKILLS[skillIdx]) {
                                                Skill skill = Skill.builder()
                                                                .description(skillName)
                                                                .tower(childTower)
                                                                .build();
                                                towerSkills.add(skillRepository.save(skill));
                                        }
                                }
                                skillsByTower.put(childName, towerSkills);
                                skillIdx++;
                        }
                }

                log.info("Created {} TechTowers, {} Skills", towerMap.size(), skillRepository.count());
        }

        @Transactional
        private void fixData() {
                log.info("Running data fix...");

                // 1. Fix allocations: ensure PROJECT allocations have percentages, clear monthly
                // from PROSPECT
                List<Allocation> allocations = allocationRepository.findAll();
                Integer[] validPercentages = { 25, 50, 75, 100 };
                int fixedCount = 0;
                int prospectCleanedCount = 0;
                int currentYear = LocalDate.now().getYear();

                for (int i = 0; i < allocations.size(); i++) {
                        Allocation allocation = allocations.get(i);
                        boolean changed = false;

                        if (allocation.getAllocationType() == Allocation.AllocationType.PROSPECT) {
                                // PROSPECT allocations should never have monthly allocations
                                if (!allocation.getMonthlyAllocations().isEmpty()) {
                                        monthlyAllocationRepository.deleteByAllocationId(allocation.getId());
                                        prospectCleanedCount++;
                                }
                        } else if (allocation.getAllocationType() == Allocation.AllocationType.PROJECT) {
                                // For PROJECT allocations, ensure they have monthly allocations
                                for (int month = 1; month <= 12; month++) {
                                        Integer val = allocation.getAllocationForYearMonth(currentYear, month);
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

                // 3. Set resignation dates for some employees (simulating resigned/maternity/leave)
                List<Employee> employees = employeeRepository.findAll();
                int resignedCount = 0;

                for (int i = 0; i < employees.size(); i++) {
                        Employee emp = employees.get(i);

                        if (i % 30 == 0 && emp.getManager() != null) {
                                // Simulate resigned employees by setting a resignation date
                                emp.setResignationDate(LocalDate.now().minusDays(random.nextInt(90)));
                                emp.setReasonOfLeave("Voluntary resignation");
                                resignedCount++;
                                employeeRepository.save(emp);
                        }
                }
                log.info("Set {} employees as resigned (with resignation date)", resignedCount);
        }

        private List<Employee> createManagers() {
                List<Employee> managers = new ArrayList<>();

                // N1 - Executive (1) - Top of hierarchy, no manager, no specific tower
                Employee n1 = createEmployee("Ahmed", "El-Sayed", "N1", Employee.JobLevel.EXECUTIVE_LEVEL,
                                "Chief Technology Officer", null, null);
                managers.add(n1);

                // N2 - Heads (4) - One per parent tower
                TechTower episTower = towerMap.get(TOWER_NAMES[0][0]);
                TechTower appTower = towerMap.get(TOWER_NAMES[1][0]);
                TechTower dataTower = towerMap.get(TOWER_NAMES[2][0]);
                TechTower otTower = towerMap.get(TOWER_NAMES[3][0]);

                Employee n2_epis = createEmployee("Mohamed", "Hassan", "N2", Employee.JobLevel.ADVANCED_MANAGER_LEVEL,
                                "VP of Infrastructure", episTower, n1);
                Employee n2_app = createEmployee("Sara", "Ibrahim", "N2", Employee.JobLevel.ADVANCED_MANAGER_LEVEL,
                                "VP of Applications", appTower, n1);
                Employee n2_data = createEmployee("Khaled", "Nasser", "N2", Employee.JobLevel.ADVANCED_MANAGER_LEVEL,
                                "VP of Data & Agility", dataTower, n1);
                Employee n2_ot = createEmployee("Hassan", "Farouk", "N2", Employee.JobLevel.ADVANCED_MANAGER_LEVEL,
                                "VP of Operations Technology", otTower, n1);
                managers.add(n2_epis);
                managers.add(n2_app);
                managers.add(n2_data);
                managers.add(n2_ot);

                // N3 - Department Managers (8) - Two per parent tower
                Employee n3_epis_1 = createEmployee("Omar", "Mohamed", "N3", Employee.JobLevel.ADVANCED_MANAGER_LEVEL,
                                "Cloud Services Manager", n2_epis.getTower(), n2_epis);
                Employee n3_epis_2 = createEmployee("Ali", "Mahmoud", "N3", Employee.JobLevel.ADVANCED_MANAGER_LEVEL,
                                "Infrastructure Manager", n2_epis.getTower(), n2_epis);

                Employee n3_app_1 = createEmployee("Marwa", "Ali", "N3", Employee.JobLevel.ADVANCED_MANAGER_LEVEL,
                                "Development Manager", n2_app.getTower(), n2_app);
                Employee n3_app_2 = createEmployee("Dina", "Salem", "N3", Employee.JobLevel.ADVANCED_MANAGER_LEVEL,
                                "QA Manager", n2_app.getTower(), n2_app);

                Employee n3_data_1 = createEmployee("Youssef", "Kamal", "N3", Employee.JobLevel.ADVANCED_MANAGER_LEVEL,
                                "Agility Manager", n2_data.getTower(), n2_data);
                Employee n3_data_2 = createEmployee("Reem", "Rashad", "N3", Employee.JobLevel.ADVANCED_MANAGER_LEVEL,
                                "Scrum Manager", n2_data.getTower(), n2_data);

                Employee n3_ot_1 = createEmployee("Mahmoud", "Ibrahim", "N3", Employee.JobLevel.ADVANCED_MANAGER_LEVEL,
                                "Automation Manager", n2_ot.getTower(), n2_ot);
                Employee n3_ot_2 = createEmployee("Layla", "Hassan", "N3", Employee.JobLevel.ADVANCED_MANAGER_LEVEL,
                                "Control Systems Manager", n2_ot.getTower(), n2_ot);

                managers.add(n3_epis_1);
                managers.add(n3_epis_2);
                managers.add(n3_app_1);
                managers.add(n3_app_2);
                managers.add(n3_data_1);
                managers.add(n3_data_2);
                managers.add(n3_ot_1);
                managers.add(n3_ot_2);

                // N4 - Team Leads (8) - One per N3
                Employee n4_1 = createEmployee("Nour", "Mostafa", "N4", Employee.JobLevel.MID_LEVEL,
                                "Cloud Team Lead", n3_epis_1.getTower(), n3_epis_1);
                Employee n4_2 = createEmployee("Fatma", "Ahmed", "N4", Employee.JobLevel.MID_LEVEL,
                                "Security Team Lead", n3_epis_2.getTower(), n3_epis_2);
                Employee n4_3 = createEmployee("Hana", "Mohamed", "N4", Employee.JobLevel.MID_LEVEL,
                                "Frontend Team Lead", n3_app_1.getTower(), n3_app_1);
                Employee n4_4 = createEmployee("Aisha", "Ali", "N4", Employee.JobLevel.MID_LEVEL,
                                "Testing Team Lead", n3_app_2.getTower(), n3_app_2);
                Employee n4_5 = createEmployee("Mona", "Hassan", "N4", Employee.JobLevel.MID_LEVEL,
                                "Scrum Master", n3_data_1.getTower(), n3_data_1);
                Employee n4_6 = createEmployee("Ahmed", "Abdel", "N4", Employee.JobLevel.MID_LEVEL,
                                "Data Engineering Lead", n3_data_2.getTower(), n3_data_2);
                Employee n4_7 = createEmployee("Mohamed", "Kamal", "N4", Employee.JobLevel.MID_LEVEL,
                                "PLC Team Lead", n3_ot_1.getTower(), n3_ot_1);
                Employee n4_8 = createEmployee("Omar", "Salem", "N4", Employee.JobLevel.MID_LEVEL,
                                "SCADA Team Lead", n3_ot_2.getTower(), n3_ot_2);

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

                        // Employee inherits manager's tower
                        TechTower tower = manager.getTower();

                        Employee emp = createEmployee(firstName, lastName, grade, Employee.JobLevel.ENTRY_LEVEL,
                                        "Engineer", tower, manager);
                        employees.add(emp);
                }

                return employees;
        }

        private Employee createEmployee(String firstName, String lastName, String grade, Employee.JobLevel jobLevel,
                        String title, TechTower tower, Employee manager) {
                String name = firstName + " " + lastName;
                String email = (firstName.toLowerCase() + "." + lastName.toLowerCase() + random.nextInt(1000)
                                + "@company.com")
                                .replace(" ", "");

                Employee.Gender gender = Arrays
                                .asList("Sara", "Marwa", "Nour", "Fatma", "Aisha", "Layla", "Hana",
                                                "Dina", "Reem", "Mona")
                                .contains(firstName) ? Employee.Gender.FEMALE : Employee.Gender.MALE;

                Employee employee = Employee.builder()
                                .oracleId(employeeCounter++)
                                .name(name)
                                .gender(gender)
                                .grade(grade)
                                .jobLevel(jobLevel)
                                .title(title)
                                .hiringType(Employee.HiringType.FULL_TIME)
                                .location(LOCATIONS[random.nextInt(LOCATIONS.length)])
                                .legalEntity("GS")
                                .nationality("Egyptian")
                                .hireDate(LocalDate.now().minusDays(random.nextInt(2000)))
                                .email(email)
                                .tower(tower)
                                .manager(manager)
                                .build();

                return employeeRepository.save(employee);
        }

        private List<Project> createProjects(List<Employee> managers) {
                List<Project> projects = new ArrayList<>();

                // Projects organized by area
                String[][] projectsByArea = {
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

                // Create projects for each area, managed by managers from that area
                for (int areaIdx = 0; areaIdx < PARENT_TOWER_NAMES.length; areaIdx++) {
                        String[] areaProjects = projectsByArea[areaIdx];

                        // Find N3 managers for this area (indices 5-12, 2 per area)
                        int n3StartIdx = 5 + (areaIdx * 2);
                        List<Employee> areaManagers = new ArrayList<>();
                        areaManagers.add(managers.get(n3StartIdx));
                        areaManagers.add(managers.get(n3StartIdx + 1));

                        for (int p = 0; p < areaProjects.length; p++) {
                                // Alternate between the two N3 managers for this area
                                Employee projectManager = areaManagers.get(p % 2);

                                Project project = Project.builder()
                                                .projectId("PRJ-" + projectCounter++)
                                                .description(areaProjects[p])
                                                .projectType(p % 3 == 0 ? Project.ProjectType.OPPORTUNITY
                                                                : Project.ProjectType.PROJECT)
                                                .region(REGIONS[areaIdx % REGIONS.length])
                                                .vertical(VERTICALS[areaIdx % VERTICALS.length])
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
                // Valid percentage values as integers
                Integer[] allocValues = { 100, 75, 50, 25 };
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
                                                .allocationType(Allocation.AllocationType.PROJECT)
                                                .build();

                                // Save allocation first
                                allocation = allocationRepository.save(allocation);

                                // Set allocation for each month with valid percentage values
                                for (int month = 1; month <= 12; month++) {
                                        Integer alloc = allocValues[random.nextInt(allocValues.length)];
                                        allocation.setAllocationForYearMonth(currentYear, month, alloc);
                                }

                                allocationRepository.save(allocation);
                        }
                }
        }

        private void createEmployeeSkills(List<Employee> allEmployees) {
                EmployeeSkill.SkillGrade[] grades = EmployeeSkill.SkillGrade.values();
                int assignedCount = 0;

                for (Employee emp : allEmployees) {
                        TechTower tower = emp.getTower();
                        if (tower == null)
                                continue;

                        List<Skill> towerSkills = skillsByTower.get(tower.getDescription());
                        if (towerSkills == null || towerSkills.isEmpty())
                                continue;

                        // Assign 1-3 skills per employee
                        int numSkills = 1 + random.nextInt(Math.min(3, towerSkills.size()));
                        List<Skill> shuffled = new ArrayList<>(towerSkills);
                        Collections.shuffle(shuffled, random);

                        for (int i = 0; i < numSkills; i++) {
                                EmployeeSkill.SkillLevel level = (i == 0) ? EmployeeSkill.SkillLevel.PRIMARY
                                                : EmployeeSkill.SkillLevel.SECONDARY;
                                EmployeeSkill es = EmployeeSkill.builder()
                                                .employee(emp)
                                                .skill(shuffled.get(i))
                                                .skillLevel(level)
                                                .skillGrade(grades[random.nextInt(grades.length)])
                                                .build();
                                employeeSkillRepository.save(es);
                                assignedCount++;
                        }
                }

                log.info("Assigned {} skills to {} employees", assignedCount, allEmployees.size());
        }

        private void createUsers(List<Employee> managers) {
                // Create user for each manager
                // N1 (index 0) is the top-level employee (no manager) and gets full access via hierarchy
                // No separate SYSTEM_ADMIN user needed
                for (int i = 0; i < managers.size(); i++) {
                        Employee manager = managers.get(i);

                        User user = User.builder()
                                        .username(manager.getName().toLowerCase().replace(" ", "."))
                                        .password(passwordEncoder.encode("password123"))
                                        .email(manager.getEmail())
                                        .employee(manager)
                                        .build();
                        userRepository.save(user);
                }

                log.info("Created {} manager users (N1 = top-level with full access)", managers.size());
        }
}
