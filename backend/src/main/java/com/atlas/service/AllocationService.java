package com.atlas.service;

import com.atlas.dto.AllocationDTO;
import com.atlas.entity.Allocation;
import com.atlas.entity.Employee;
import com.atlas.entity.Project;
import com.atlas.entity.User;
import com.atlas.repository.AllocationRepository;
import com.atlas.repository.EmployeeRepository;
import com.atlas.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AllocationService {

    private final AllocationRepository allocationRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;

    public List<AllocationDTO> getAllAllocations(User currentUser) {
        List<Allocation> allocations = getFilteredAllocations(currentUser);
        return allocations.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public AllocationDTO getAllocationById(Long id) {
        Allocation allocation = allocationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Allocation not found: " + id));
        return toDTO(allocation);
    }

    public List<AllocationDTO> getAllocationsByEmployee(Long employeeId) {
        return allocationRepository.findByEmployeeId(employeeId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<AllocationDTO> getAllocationsByProject(Long projectId) {
        return allocationRepository.findByProjectId(projectId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AllocationDTO createAllocation(AllocationDTO dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found: " + dto.getEmployeeId()));

        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found: " + dto.getProjectId()));

        Allocation allocation = Allocation.builder()
                .employee(employee)
                .project(project)
                .confirmedAssignment(dto.getConfirmedAssignment())
                .prospectAssignment(dto.getProspectAssignment())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status(dto.getStatus() != null ? dto.getStatus() : Allocation.AllocationStatus.ACTIVE)
                .janUtilization(dto.getJanUtilization())
                .febUtilization(dto.getFebUtilization())
                .marUtilization(dto.getMarUtilization())
                .aprUtilization(dto.getAprUtilization())
                .mayUtilization(dto.getMayUtilization())
                .junUtilization(dto.getJunUtilization())
                .julUtilization(dto.getJulUtilization())
                .augUtilization(dto.getAugUtilization())
                .sepUtilization(dto.getSepUtilization())
                .octUtilization(dto.getOctUtilization())
                .novUtilization(dto.getNovUtilization())
                .decUtilization(dto.getDecUtilization())
                .build();

        allocation = allocationRepository.save(allocation);
        return toDTO(allocation);
    }

    @Transactional
    public AllocationDTO updateAllocation(Long id, AllocationDTO dto) {
        Allocation allocation = allocationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Allocation not found: " + id));

        allocation.setConfirmedAssignment(dto.getConfirmedAssignment());
        allocation.setProspectAssignment(dto.getProspectAssignment());
        allocation.setStartDate(dto.getStartDate());
        allocation.setEndDate(dto.getEndDate());
        allocation.setStatus(dto.getStatus());

        // Update monthly utilizations
        allocation.setJanUtilization(dto.getJanUtilization());
        allocation.setFebUtilization(dto.getFebUtilization());
        allocation.setMarUtilization(dto.getMarUtilization());
        allocation.setAprUtilization(dto.getAprUtilization());
        allocation.setMayUtilization(dto.getMayUtilization());
        allocation.setJunUtilization(dto.getJunUtilization());
        allocation.setJulUtilization(dto.getJulUtilization());
        allocation.setAugUtilization(dto.getAugUtilization());
        allocation.setSepUtilization(dto.getSepUtilization());
        allocation.setOctUtilization(dto.getOctUtilization());
        allocation.setNovUtilization(dto.getNovUtilization());
        allocation.setDecUtilization(dto.getDecUtilization());

        allocation = allocationRepository.save(allocation);
        return toDTO(allocation);
    }

    @Transactional
    public void deleteAllocation(Long id) {
        if (!allocationRepository.existsById(id)) {
            throw new RuntimeException("Allocation not found: " + id);
        }
        allocationRepository.deleteById(id);
    }

    private List<Allocation> getFilteredAllocations(User user) {
        if (user.getRole() == User.Role.SYSTEM_ADMIN || user.getRole() == User.Role.EXECUTIVE) {
            return allocationRepository.findAll();
        }

        var userEmployee = user.getEmployee();
        if (userEmployee == null) {
            return List.of();
        }

        String parentTower = userEmployee.getParentTower();
        String tower = userEmployee.getTower();

        return switch (user.getRole()) {
            case HEAD -> allocationRepository.findByParentTower(parentTower);
            case DEPARTMENT_MANAGER, TEAM_LEAD -> allocationRepository.findByTower(tower);
            default -> List.of();
        };
    }

    private AllocationDTO toDTO(Allocation allocation) {
        int currentMonth = LocalDate.now().getMonthValue();
        String currentUtil = allocation.getUtilizationForMonth(currentMonth);

        double percentage = 0.0;
        if (currentUtil != null && !currentUtil.equalsIgnoreCase("B") && !currentUtil.equalsIgnoreCase("P")) {
            try {
                percentage = Double.parseDouble(currentUtil) * 100;
            } catch (NumberFormatException ignored) {
            }
        }

        return AllocationDTO.builder()
                .id(allocation.getId())
                .employeeId(allocation.getEmployee().getId())
                .employeeName(allocation.getEmployee().getName())
                .projectId(allocation.getProject().getId())
                .projectName(allocation.getProject().getName())
                .confirmedAssignment(allocation.getConfirmedAssignment())
                .prospectAssignment(allocation.getProspectAssignment())
                .startDate(allocation.getStartDate())
                .endDate(allocation.getEndDate())
                .status(allocation.getStatus())
                .currentMonthUtilization(currentUtil)
                .utilizationPercentage(percentage)
                .janUtilization(allocation.getJanUtilization())
                .febUtilization(allocation.getFebUtilization())
                .marUtilization(allocation.getMarUtilization())
                .aprUtilization(allocation.getAprUtilization())
                .mayUtilization(allocation.getMayUtilization())
                .junUtilization(allocation.getJunUtilization())
                .julUtilization(allocation.getJulUtilization())
                .augUtilization(allocation.getAugUtilization())
                .sepUtilization(allocation.getSepUtilization())
                .octUtilization(allocation.getOctUtilization())
                .novUtilization(allocation.getNovUtilization())
                .decUtilization(allocation.getDecUtilization())
                .build();
    }
}
