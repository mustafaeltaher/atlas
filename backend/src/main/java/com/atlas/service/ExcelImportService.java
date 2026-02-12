package com.atlas.service;

import com.atlas.entity.Allocation;
import com.atlas.entity.Employee;
import com.atlas.entity.Project;
import com.atlas.repository.AllocationRepository;
import com.atlas.repository.EmployeeRepository;
import com.atlas.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelImportService {

    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final AllocationRepository allocationRepository;

    @Transactional
    public int importEmployees(MultipartFile file) throws Exception {
        int imported = 0;

        try (InputStream is = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            Map<String, Integer> columnIndex = new HashMap<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    columnIndex.put(cell.getStringCellValue().trim(), i);
                }
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                String oracleIdStr = getStringValue(row, columnIndex.get("Oracle ID"));
                if (oracleIdStr == null || oracleIdStr.isEmpty())
                    continue;

                Integer oracleId;
                try {
                    oracleId = Integer.parseInt(oracleIdStr);
                } catch (NumberFormatException e) {
                    log.warn("Invalid Oracle ID: {}, skipping row", oracleIdStr);
                    continue;
                }

                // Skip if employee already exists
                if (employeeRepository.existsByOracleId(oracleId)) {
                    log.info("Employee with Oracle ID {} already exists, skipping", oracleId);
                    continue;
                }

                // Parse enums safely
                Employee.Gender gender = parseGender(getStringValue(row, columnIndex.get("Gender")));
                Employee.JobLevel jobLevel = parseJobLevel(getStringValue(row, columnIndex.get("Job Level")));
                Employee.HiringType hiringType = parseHiringType(getStringValue(row, columnIndex.get("Hiring Type")));

                Employee employee = Employee.builder()
                        .oracleId(oracleId)
                        .name(getStringValue(row, columnIndex.get("Name")))
                        .gender(gender)
                        .grade(getStringValue(row, columnIndex.get("Grade")))
                        .jobLevel(jobLevel)
                        .title(getStringValue(row, columnIndex.get("Title")))
                        .hiringType(hiringType)
                        .location(getStringValue(row, columnIndex.get("Location")))
                        .legalEntity(getStringValue(row, columnIndex.get("Legal Entity")))
                        .costCenter(getStringValue(row, columnIndex.get("Cost Center")))
                        .nationality(getStringValue(row, columnIndex.get("Nationality")))
                        .hireDate(getDateValue(row, columnIndex.get("Hire date")))
                        .resignationDate(getDateValue(row, columnIndex.get("Resignation date")))
                        .reasonOfLeave(getStringValue(row, columnIndex.get("Reason of Leave")))
                        .email(getStringValue(row, columnIndex.get("Emails")))
                        .build();

                final Employee savedEmployee = employeeRepository.save(employee);

                // Create allocation if project ID exists
                String projectId = getStringValue(row, columnIndex.get("Project ID"));
                if (projectId != null && !projectId.isEmpty()) {
                    Project project = projectRepository.findByProjectId(projectId)
                            .orElseGet(() -> {
                                Project newProject = Project.builder()
                                        .projectId(projectId)
                                        .description(getStringValue(row, columnIndex.get("Confirmed Assignment")))
                                        .projectType(Project.ProjectType.PROJECT)
                                        .region(getStringValue(row, columnIndex.get("Region")))
                                        .vertical(getStringValue(row, columnIndex.get("Vertical")))
                                        .status(Project.ProjectStatus.ACTIVE)
                                        .build();
                                return projectRepository.save(newProject);
                            });

                    Allocation allocation = Allocation.builder()
                            .employee(savedEmployee)
                            .project(project)
                            .endDate(getDateValue(row, columnIndex.get("Current Assignment End Date")))
                            .allocationType(Allocation.AllocationType.PROJECT)
                            .build();

                    allocation = allocationRepository.save(allocation);

                    // Parse monthly allocations from Excel and create MonthlyAllocation entries
                    int year = LocalDate.now().getYear();
                    String[] months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov",
                            "Dec" };
                    for (int m = 0; m < months.length; m++) {
                        String columnName = months[m] + " 26"; // e.g., "Jan 26"
                        String allocValue = getStringValue(row, columnIndex.get(columnName));
                        if (allocValue != null && !allocValue.isEmpty()) {
                            try {
                                int percentage = (int) Double.parseDouble(allocValue);
                                if (percentage > 0) {
                                    allocation.setAllocationForYearMonth(year, m + 1, percentage);
                                }
                            } catch (NumberFormatException ignored) {
                                // Skip non-numeric values like "P" or "B"
                            }
                        }
                    }
                    allocationRepository.save(allocation);
                }

                imported++;
            }
        }

        return imported;
    }

    private String getStringValue(Row row, Integer colIndex) {
        if (colIndex == null)
            return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null)
            return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private LocalDate getDateValue(Row row, Integer colIndex) {
        if (colIndex == null)
            return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null)
            return null;

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            Date date = cell.getDateCellValue();
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return null;
    }

    private Employee.Gender parseGender(String value) {
        if (value == null || value.isEmpty()) return null;
        return switch (value.trim().toUpperCase()) {
            case "MALE", "M" -> Employee.Gender.MALE;
            case "FEMALE", "F" -> Employee.Gender.FEMALE;
            default -> {
                log.warn("Unknown gender value: {}", value);
                yield null;
            }
        };
    }

    private Employee.JobLevel parseJobLevel(String value) {
        if (value == null || value.isEmpty()) return null;
        return switch (value.trim().toUpperCase().replace(" ", "_").replace("/", "_")) {
            case "ENTRY_LEVEL", "ENTRY-LEVEL", "ENTRY LEVEL" -> Employee.JobLevel.ENTRY_LEVEL;
            case "MID_LEVEL", "MID-LEVEL", "MID LEVEL" -> Employee.JobLevel.MID_LEVEL;
            case "ADVANCED_MANAGER_LEVEL", "ADVANCED/MANAGER-LEVEL", "ADVANCED/MANAGER LEVEL" -> Employee.JobLevel.ADVANCED_MANAGER_LEVEL;
            case "EXECUTIVE_LEVEL", "EXECUTIVE-LEVEL", "EXECUTIVE LEVEL" -> Employee.JobLevel.EXECUTIVE_LEVEL;
            default -> {
                log.warn("Unknown job level value: {}", value);
                yield null;
            }
        };
    }

    private Employee.HiringType parseHiringType(String value) {
        if (value == null || value.isEmpty()) return null;
        return switch (value.trim().toUpperCase().replace("-", "_").replace(" ", "_")) {
            case "FULL_TIME", "FULLTIME", "FULL TIME" -> Employee.HiringType.FULL_TIME;
            case "PART_TIME", "PARTTIME", "PART TIME" -> Employee.HiringType.PART_TIME;
            default -> {
                log.warn("Unknown hiring type value: {}", value);
                yield null;
            }
        };
    }
}
