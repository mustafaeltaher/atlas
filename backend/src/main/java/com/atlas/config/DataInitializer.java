package com.atlas.config;

import com.atlas.entity.*;
import com.atlas.repository.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

        private final UserRepository userRepository;
        private final EmployeeRepository employeeRepository;
        private final ProjectRepository projectRepository;
        private final AllocationRepository allocationRepository;
        private final TechTowerRepository techTowerRepository;
        private final SkillRepository skillRepository;
        private final EmployeeSkillRepository employeeSkillRepository;
        private final PasswordEncoder passwordEncoder;

        private final ObjectMapper objectMapper = new ObjectMapper();
        private final Random random = new Random(42);

        private final String[] PARENT_TOWER_NAMES = { "EPIS", "Application", "Data&Agility", "OT" };
        private final String[][] TOWER_NAMES = {
                        { "Cloud & Core Infrastructure Services", "Network, Cybersecurity & Collaboration" },
                        { "Testing", "Development", "Quality Assurance" },
                        { "Agility", "Data Engineering", "Analytics" },
                        { "Automation and Control", "Industrial Systems" }
        };
        private final String[] GRADES = { "3", "4", "5", "6", "7", "C" };
        private final String[] REGIONS = { "MEA", "Europe", "Asia", "Americas" };
        private final String[] VERTICALS = { "Energy", "Telecom", "Banking", "Healthcare", "Government" };

        // Skills per child tower
        private final String[][] TOWER_SKILLS = {
                        { "AWS", "Azure", "GCP", "Docker", "Kubernetes", "Terraform", "Linux" },
                        { "Cisco Networking", "Firewall Management", "SIEM", "Penetration Testing", "IAM",
                                        "Endpoint Security" },
                        { "Selenium", "JUnit", "Cypress", "Performance Testing", "API Testing", "Test Automation" },
                        { "Java", "Spring Boot", "Angular", "React", "Python", "Node.js", "TypeScript" },
                        { "ISTQB", "Test Planning", "Regression Testing", "UAT", "BDD", "Defect Management" },
                        { "Scrum", "SAFe", "Kanban", "Jira", "Agile Coaching", "Product Ownership" },
                        { "Spark", "Kafka", "Airflow", "Snowflake", "dbt", "SQL", "ETL" },
                        { "Power BI", "Tableau", "Python Analytics", "R", "Machine Learning", "Statistics" },
                        { "PLC Programming", "SCADA", "DCS", "HMI Design", "Industrial IoT", "Modbus" },
                        { "MES", "Historian", "OPC UA", "Safety Systems", "Control Valves", "Instrumentation" }
        };

        private final Map<String, TechTower> towerMap = new HashMap<>();
        private final Map<String, List<Skill>> skillsByTower = new HashMap<>();

        @Override
        public void run(String... args) {
                if (employeeRepository.count() > 0) {
                        log.info("Data already initialized, skipping creation...");
                        return;
                }

                log.info("Initializing data from JSON...");

                createTechTowers();

                try {
                        importEmployeesFromJson();
                } catch (Exception e) {
                        log.error("Failed to import employees from JSON", e);
                        throw new RuntimeException(e);
                }

                List<Employee> allEmployees = employeeRepository.findAll();

                // Find managers for project creation (anyone who has reports)
                List<Employee> managers = allEmployees.stream()
                                .filter(e -> employeeRepository.countByManager(e) > 0)
                                .collect(Collectors.toList());

                if (managers.isEmpty()) {
                        log.warn("No managers found after import! Using random employees for project management.");
                        // Fallback: Pick random top 10%
                        Collections.shuffle(allEmployees);
                        managers = allEmployees.subList(0, Math.max(1, allEmployees.size() / 10));
                }

                assignTowersHierarchically(allEmployees);

                // Refresh list to get updated towers
                allEmployees = employeeRepository.findAllWithTower();

                List<Project> projects = createProjects(managers);
                createAllocations(allEmployees, projects);
                createEmployeeSkills(allEmployees);
                createUsersForManagers(managers);

                fixData();

                log.info("Data initialization complete!");
                log.info("Imported {} employees", allEmployees.size());
        }

        private void assignTowersHierarchically(List<Employee> allEmployees) {
                List<TechTower> availableTowers = new ArrayList<>(towerMap.values());
                if (availableTowers.isEmpty())
                        return;

                // 1. Identify Group CEO (Root) - approximation: manager is null
                // In imported data, top level might not have manager set.
                List<Employee> topLevelEmployees = allEmployees.stream()
                                .filter(e -> e.getManager() == null)
                                .collect(Collectors.toList());

                for (Employee top : topLevelEmployees) {
                        // 2. Direct reports to CEO (Level 1)
                        // CEO themselves might not need a tower, or can have one.
                        // Request says: "only the group CEO can see multiple all the towers"
                        // implication is CEO doesn't belong to one.
                        // But for simplicity/data consistency, we might leave CEO tower null or assign
                        // one.
                        // Let's propagate to direct reports.

                        List<Employee> directReports = employeeRepository.findByManager(top);
                        for (Employee directReport : directReports) {
                                // Assign Random Tower to Level 1 (Vertical Heads)
                                TechTower assignedTower = availableTowers.get(random.nextInt(availableTowers.size()));
                                directReport.setTower(assignedTower);
                                employeeRepository.save(directReport);

                                // 3. Propagate to their subordinates recursively
                                propagateTower(directReport, assignedTower);
                        }
                }
                log.info("Assigned towers hierarchically");
        }

        private void propagateTower(Employee manager, TechTower tower) {
                List<Employee> reports = employeeRepository.findByManager(manager);
                for (Employee report : reports) {
                        report.setTower(tower);
                        employeeRepository.save(report);
                        propagateTower(report, tower);
                }
        }

        private void importEmployeesFromJson() throws Exception {
                InputStream inputStream = getClass().getResourceAsStream("/company_structure_json.json");
                if (inputStream == null) {
                        // Try loading from file system if resource not found (for dev environment)
                        java.io.File file = new java.io.File("company_structure_json.json");
                        if (file.exists()) {
                                inputStream = new java.io.FileInputStream(file);
                        } else {
                                throw new RuntimeException("company_structure_json.json not found");
                        }
                }

                EmployeeJsonDTO[] dtos = objectMapper.readValue(inputStream, EmployeeJsonDTO[].class);
                log.info("Loaded {} records from JSON", dtos.length);

                // Import all employees without filtering
                List<EmployeeJsonDTO> allEmployeesList = Arrays.asList(dtos);

                log.info("Importing {} employees (no filtering)", allEmployeesList.size());

                Map<String, Employee> emailToEmployeeMap = new HashMap<>();
                Map<String, String> employeeEmailToManagerEmailMap = new HashMap<>();

                // Phase 1: Create Employee entities
                int oracleIdCounter = 1000;
                for (EmployeeJsonDTO dto : allEmployeesList) {
                        // Normalize email
                        String email = dto.getEmail() != null ? dto.getEmail().trim() : "";
                        if (email.isEmpty())
                                continue;

                        Employee employee = Employee.builder()
                                        .oracleId(oracleIdCounter++)
                                        .name(dto.displayName)
                                        .email(email)
                                        .title(dto.jobTitle)
                                        .grade(GRADES[random.nextInt(GRADES.length)]) // Assign random grade as it is
                                                                                      // missing in JSON
                                        .jobLevel(determineJobLevel(dto.jobTitle))
                                        .hiringType(Employee.HiringType.FULL_TIME)
                                        .location(dto.officeLocation != null ? dto.officeLocation : "Cairo")
                                        .legalEntity("GS")
                                        .nationality("Egyptian")
                                        .hireDate(LocalDate.now().minusDays(random.nextInt(2000))) // Random hire date
                                        .gender(guessGender(dto.givenName))
                                        .build();

                        Employee saved = employeeRepository.save(employee);
                        emailToEmployeeMap.put(email.toLowerCase(), saved);

                        if (dto.managerEmail != null && !dto.managerEmail.trim().isEmpty()
                                        && !"N/A".equalsIgnoreCase(dto.managerEmail)) {
                                employeeEmailToManagerEmailMap.put(email.toLowerCase(),
                                                dto.managerEmail.trim().toLowerCase());
                        }
                }

                // Phase 2: Link Managers
                for (Map.Entry<String, String> entry : employeeEmailToManagerEmailMap.entrySet()) {
                        String empEmail = entry.getKey();
                        String mgrEmail = entry.getValue();

                        Employee employee = emailToEmployeeMap.get(empEmail);
                        Employee manager = emailToEmployeeMap.get(mgrEmail);

                        if (employee != null && manager != null) {
                                employee.setManager(manager);
                                employeeRepository.save(employee);
                        }
                }
        }

        private Employee.JobLevel determineJobLevel(String title) {
                if (title == null)
                        return Employee.JobLevel.ENTRY_LEVEL;
                String lower = title.toLowerCase();
                if (lower.contains("chief") || lower.contains("head") || lower.contains("director"))
                        return Employee.JobLevel.EXECUTIVE_LEVEL;
                if (lower.contains("manager") || lower.contains("lead"))
                        return Employee.JobLevel.ADVANCED_MANAGER_LEVEL;
                if (lower.contains("senior"))
                        return Employee.JobLevel.MID_LEVEL;
                return Employee.JobLevel.ENTRY_LEVEL;
        }

        private Employee.Gender guessGender(String firstName) {
                // Simple heuristic list, fallback to MALE
                List<String> femaleNames = Arrays.asList("Sara", "Marwa", "Nour", "Fatma", "Aisha", "Layla", "Hana",
                                "Dina", "Reem", "Mona", "Niveen", "Esraa", "Dalia", "Rasha", "Violet");
                if (firstName != null && femaleNames.contains(firstName))
                        return Employee.Gender.FEMALE;
                return Employee.Gender.MALE;
        }

        private void createUsersForManagers(List<Employee> managers) {
                for (Employee manager : managers) {
                        // Check if user already exists
                        if (userRepository.findByUsername(manager.getEmail()).isPresent())
                                continue;

                        User user = User.builder()
                                        .username(manager.getEmail()) // Use email as username
                                        .password(passwordEncoder.encode("password123"))
                                        .email(manager.getEmail())
                                        .employee(manager)
                                        .build();
                        userRepository.save(user);
                }
                log.info("Created users for {} managers", managers.size());
        }

        // --- REUSED/ADAPTED METHODS FROM ORIGINAL ---

        private void createTechTowers() {
                if (techTowerRepository.count() > 0)
                        return;
                log.info("Creating TechTower seed data...");

                int skillIdx = 0;
                for (int i = 0; i < PARENT_TOWER_NAMES.length; i++) {
                        TechTower parentTower = TechTower.builder()
                                        .description(PARENT_TOWER_NAMES[i])
                                        .parentTower(null)
                                        .build();
                        parentTower = techTowerRepository.save(parentTower);
                        towerMap.put(PARENT_TOWER_NAMES[i], parentTower);

                        for (String childName : TOWER_NAMES[i]) {
                                TechTower childTower = TechTower.builder()
                                                .description(childName)
                                                .parentTower(parentTower)
                                                .build();
                                childTower = techTowerRepository.save(childTower);
                                towerMap.put(childName, childTower);

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
        }

        private List<Project> createProjects(List<Employee> managers) {
                List<Project> projects = new ArrayList<>();
                String[][] projectsByArea = {
                                { "Cloud Migration", "Network Security", "Infrastructure Upgrade",
                                                "Cybersecurity Platform" },
                                { "Digital Transformation", "Mobile App", "Customer Portal", "ERP Integration" },
                                { "Data Lake", "AI Platform", "Analytics Dashboard", "ML Pipeline" },
                                { "Smart Meters", "IoT Platform", "SCADA Modernization", "Industrial Automation" }
                };

                int projectCounter = 1000;
                Random rnd = new Random();

                // Ensure we have at least one manager to assign
                if (managers.isEmpty())
                        return projects;

                for (int areaIdx = 0; areaIdx < PARENT_TOWER_NAMES.length; areaIdx++) {
                        String[] areaProjects = projectsByArea[areaIdx];
                        for (String projectName : areaProjects) {
                                Employee projectManager = managers.get(rnd.nextInt(managers.size()));

                                Project project = Project.builder()
                                                .projectId("PRJ-" + projectCounter++)
                                                .description(projectName)
                                                .projectType(rnd.nextBoolean() ? Project.ProjectType.PROJECT
                                                                : Project.ProjectType.OPPORTUNITY)
                                                .region(REGIONS[areaIdx % REGIONS.length])
                                                .vertical(VERTICALS[areaIdx % VERTICALS.length])
                                                .startDate(LocalDate.now().minusMonths(rnd.nextInt(12)))
                                                .endDate(LocalDate.now().plusMonths(rnd.nextInt(12)))
                                                .status(Project.ProjectStatus.ACTIVE)
                                                .manager(projectManager)
                                                .build();

                                projects.add(projectRepository.save(project));
                        }
                }
                return projects;
        }

        private void createAllocations(List<Employee> employees, List<Project> projects) {
                if (projects.isEmpty())
                        return;
                Integer[] allocValues = { 100, 75, 50, 25 };
                int currentYear = LocalDate.now().getYear();

                for (Employee employee : employees) {
                        // 20% Bench (no allocations)
                        if (random.nextDouble() < 0.2) {
                                continue;
                        }

                        int numAllocations = 1 + random.nextInt(2);
                        Set<Long> usedProjects = new HashSet<>();

                        for (int i = 0; i < numAllocations; i++) {
                                Project project = projects.get(random.nextInt(projects.size()));
                                if (usedProjects.contains(project.getId()))
                                        continue;
                                usedProjects.add(project.getId());

                                // 10% Prospect
                                Allocation.AllocationType type = (random.nextDouble() < 0.1)
                                                ? Allocation.AllocationType.PROSPECT
                                                : Allocation.AllocationType.PROJECT;

                                Allocation allocation = Allocation.builder()
                                                .employee(employee)
                                                .project(project)
                                                .startDate(LocalDate.now().minusMonths(random.nextInt(6)))
                                                .endDate(LocalDate.now().plusMonths(random.nextInt(6)))
                                                .allocationType(type)
                                                .build();

                                allocation = allocationRepository.save(allocation);

                                if (type == Allocation.AllocationType.PROJECT) {
                                        for (int month = 1; month <= 12; month++) {
                                                Integer alloc = allocValues[random.nextInt(allocValues.length)];
                                                allocation.setAllocationForYearMonth(currentYear, month, alloc);
                                        }
                                }
                                allocationRepository.save(allocation);
                        }
                }
        }

        private void createEmployeeSkills(List<Employee> allEmployees) {
                EmployeeSkill.SkillGrade[] grades = EmployeeSkill.SkillGrade.values();

                for (Employee emp : allEmployees) {
                        TechTower tower = emp.getTower();
                        if (tower == null)
                                continue;

                        List<Skill> towerSkills = skillsByTower.get(tower.getDescription());
                        if (towerSkills == null || towerSkills.isEmpty())
                                continue;

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
                        }
                }
        }

        @Transactional
        public void fixData() {
                // Reuse existing fixData logic if strictly required, simplified here for
                // brevity as initial generation is better now
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class EmployeeJsonDTO {
                public String displayName;
                public String givenName;
                public String surname;
                public String mail;
                public String userPrincipalName; // fallback for email
                public String jobTitle; // maps to title
                public String department;
                public String officeLocation;
                public String managerEmail;

                // Helper to get best email
                public String getEmail() {
                        if (mail != null && !mail.isEmpty())
                                return mail;
                        return userPrincipalName;
                }

                // Helper specifically for Jackson if it prefers direct field access or standard
                // getters
                public String getTitle() {
                        return jobTitle;
                }
        }
}
