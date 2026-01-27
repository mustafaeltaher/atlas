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

                String oracleId = getStringValue(row, columnIndex.get("Oracle ID"));
                if (oracleId == null || oracleId.isEmpty())
                    continue;

                // Skip if employee already exists
                if (employeeRepository.existsByOracleId(oracleId)) {
                    log.info("Employee with Oracle ID {} already exists, skipping", oracleId);
                    continue;
                }

                Employee employee = Employee.builder()
                        .oracleId(oracleId)
                        .name(getStringValue(row, columnIndex.get("Name")))
                        .gender(getStringValue(row, columnIndex.get("Gender")))
                        .grade(getStringValue(row, columnIndex.get("Grade")))
                        .jobLevel(getStringValue(row, columnIndex.get("Job Level")))
                        .title(getStringValue(row, columnIndex.get("Title")))
                        .primarySkill(getStringValue(row, columnIndex.get("Primary Skill")))
                        .secondarySkill(getStringValue(row, columnIndex.get("Secondary Skill")))
                        .hiringType(getStringValue(row, columnIndex.get("Hiring Type")))
                        .location(getStringValue(row, columnIndex.get("Location")))
                        .legalEntity(getStringValue(row, columnIndex.get("Legal Entity")))
                        .costCenter(getStringValue(row, columnIndex.get("Cost Center")))
                        .nationality(getStringValue(row, columnIndex.get("Nationality")))
                        .hireDate(getDateValue(row, columnIndex.get("Hire date")))
                        .resignationDate(getDateValue(row, columnIndex.get("Resignation date")))
                        .reasonOfLeave(getStringValue(row, columnIndex.get("Reason of Leave")))
                        .email(getStringValue(row, columnIndex.get("Emails")))
                        .parentTower(getStringValue(row, columnIndex.get("Parent Tower")))
                        .tower(getStringValue(row, columnIndex.get("Tower")))
                        .futureManager(getStringValue(row, columnIndex.get("Future Manager")))
                        .isActive(true)
                        .build();

                final Employee savedEmployee = employeeRepository.save(employee);

                // Create allocation if project ID exists
                String projectId = getStringValue(row, columnIndex.get("Project ID"));
                if (projectId != null && !projectId.isEmpty()) {
                    Project project = projectRepository.findByProjectId(projectId)
                            .orElseGet(() -> {
                                Project newProject = Project.builder()
                                        .projectId(projectId)
                                        .name(getStringValue(row, columnIndex.get("Confirmed Assignment")))
                                        .parentTower(savedEmployee.getParentTower())
                                        .tower(savedEmployee.getTower())
                                        .status(Project.ProjectStatus.ACTIVE)
                                        .build();
                                return projectRepository.save(newProject);
                            });

                    Allocation allocation = Allocation.builder()
                            .employee(savedEmployee)
                            .project(project)
                            .confirmedAssignment(getStringValue(row, columnIndex.get("Confirmed Assignment")))
                            .prospectAssignment(getStringValue(row, columnIndex.get("Prospect Assignment")))
                            .endDate(getDateValue(row, columnIndex.get("Current Assignment End Date")))
                            .janUtilization(getStringValue(row, columnIndex.get("Jan 26")))
                            .febUtilization(getStringValue(row, columnIndex.get("Feb 26")))
                            .marUtilization(getStringValue(row, columnIndex.get("Mar 26")))
                            .aprUtilization(getStringValue(row, columnIndex.get("Apr 26")))
                            .mayUtilization(getStringValue(row, columnIndex.get("May 26")))
                            .junUtilization(getStringValue(row, columnIndex.get("Jun 26")))
                            .julUtilization(getStringValue(row, columnIndex.get("Jul 26")))
                            .augUtilization(getStringValue(row, columnIndex.get("Aug 26")))
                            .sepUtilization(getStringValue(row, columnIndex.get("Sep 26")))
                            .octUtilization(getStringValue(row, columnIndex.get("Oct 26")))
                            .novUtilization(getStringValue(row, columnIndex.get("Nov 26")))
                            .decUtilization(getStringValue(row, columnIndex.get("Dec 26")))
                            .status(Allocation.AllocationStatus.ACTIVE)
                            .build();

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
}
