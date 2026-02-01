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

    // Paginated version with search and filters
    public org.springframework.data.domain.Page<AllocationDTO> getAllAllocations(User currentUser,
            org.springframework.data.domain.Pageable pageable, String search, String status) {

        Allocation.AllocationStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = Allocation.AllocationStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore filter
            }
        }

        String searchParam = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        if (currentUser.getRole() == User.Role.SYSTEM_ADMIN || currentUser.getRole() == User.Role.EXECUTIVE) {
            // Direct database pagination for admins
            org.springframework.data.domain.Page<Allocation> allocationPage = allocationRepository.searchAllocations(
                    searchParam, statusEnum, pageable);
            return allocationPage.map(this::toDTO);
        }

        // For managers: fetch all accessible allocations, then filter and paginate
        List<Allocation> allAccessible = getFilteredAllocations(currentUser);

        // Apply search and filters
        final String searchLower = searchParam != null ? searchParam.toLowerCase() : null;
        final Allocation.AllocationStatus statusFinal = statusEnum;

        allAccessible = allAccessible.stream()
                .filter(a -> searchLower == null ||
                        (a.getEmployee() != null && a.getEmployee().getName() != null
                                && a.getEmployee().getName().toLowerCase().contains(searchLower))
                        ||
                        (a.getProject() != null && a.getProject().getName() != null
                                && a.getProject().getName().toLowerCase().contains(searchLower)))
                .filter(a -> statusFinal == null || a.getStatus() == statusFinal)
                .collect(Collectors.toList());

        List<AllocationDTO> dtoList = allAccessible.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        // Manual pagination from the filtered list
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtoList.size());

        List<AllocationDTO> pageContent = start < dtoList.size() ? dtoList.subList(start, end) : List.of();

        return new org.springframework.data.domain.PageImpl<>(
                pageContent,
                pageable,
                dtoList.size());
    }

    public AllocationDTO getAllocationById(Long id, User currentUser) {
        Allocation allocation = allocationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Allocation not found: " + id));
        if (!hasAccessToAllocation(currentUser, allocation)) {
            throw new RuntimeException("Access denied to allocation: " + id);
        }
        return toDTO(allocation);
    }

    public List<AllocationDTO> getAllocationsByEmployee(Long employeeId, User currentUser) {
        return allocationRepository.findByEmployeeIdWithDetails(employeeId).stream()
                .filter(a -> hasAccessToAllocation(currentUser, a))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<AllocationDTO> getAllocationsByProject(Long projectId, User currentUser) {
        return allocationRepository.findByProjectIdWithDetails(projectId).stream()
                .filter(a -> hasAccessToAllocation(currentUser, a))
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
                .janAllocation(dto.getJanAllocation())
                .febAllocation(dto.getFebAllocation())
                .marAllocation(dto.getMarAllocation())
                .aprAllocation(dto.getAprAllocation())
                .mayAllocation(dto.getMayAllocation())
                .junAllocation(dto.getJunAllocation())
                .julAllocation(dto.getJulAllocation())
                .augAllocation(dto.getAugAllocation())
                .sepAllocation(dto.getSepAllocation())
                .octAllocation(dto.getOctAllocation())
                .novAllocation(dto.getNovAllocation())
                .decAllocation(dto.getDecAllocation())
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

        // Update monthly allocations
        allocation.setJanAllocation(dto.getJanAllocation());
        allocation.setFebAllocation(dto.getFebAllocation());
        allocation.setMarAllocation(dto.getMarAllocation());
        allocation.setAprAllocation(dto.getAprAllocation());
        allocation.setMayAllocation(dto.getMayAllocation());
        allocation.setJunAllocation(dto.getJunAllocation());
        allocation.setJulAllocation(dto.getJulAllocation());
        allocation.setAugAllocation(dto.getAugAllocation());
        allocation.setSepAllocation(dto.getSepAllocation());
        allocation.setOctAllocation(dto.getOctAllocation());
        allocation.setNovAllocation(dto.getNovAllocation());
        allocation.setDecAllocation(dto.getDecAllocation());

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
            return allocationRepository.findAllWithEmployeeAndProject();
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

    private boolean hasAccessToAllocation(User user, Allocation allocation) {
        if (user.getRole() == User.Role.SYSTEM_ADMIN || user.getRole() == User.Role.EXECUTIVE) {
            return true;
        }

        var userEmployee = user.getEmployee();
        if (userEmployee == null) {
            return false;
        }

        String userTower = userEmployee.getTower();
        String userParentTower = userEmployee.getParentTower();
        Employee allocationEmployee = allocation.getEmployee();

        return switch (user.getRole()) {
            case HEAD -> userParentTower != null && userParentTower.equals(allocationEmployee.getParentTower());
            case DEPARTMENT_MANAGER, TEAM_LEAD -> userTower != null && userTower.equals(allocationEmployee.getTower());
            default -> false;
        };
    }

    private AllocationDTO toDTO(Allocation allocation) {
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
                .currentMonthAllocation(calculateCurrentMonthAllocation(allocation))
                .allocationPercentage(calculateAllocationPercentage(allocation))
                .janAllocation(allocation.getJanAllocation())
                .febAllocation(allocation.getFebAllocation())
                .marAllocation(allocation.getMarAllocation())
                .aprAllocation(allocation.getAprAllocation())
                .mayAllocation(allocation.getMayAllocation())
                .junAllocation(allocation.getJunAllocation())
                .julAllocation(allocation.getJulAllocation())
                .augAllocation(allocation.getAugAllocation())
                .sepAllocation(allocation.getSepAllocation())
                .octAllocation(allocation.getOctAllocation())
                .novAllocation(allocation.getNovAllocation())
                .decAllocation(allocation.getDecAllocation())
                .build();
    }

    private String calculateCurrentMonthAllocation(Allocation allocation) {
        int currentMonth = LocalDate.now().getMonthValue();
        return allocation.getAllocationForMonth(currentMonth);
    }

    private double calculateAllocationPercentage(Allocation allocation) {
        String currentAllocation = calculateCurrentMonthAllocation(allocation);
        double percentage = 0.0;
        if (currentAllocation != null && !currentAllocation.equalsIgnoreCase("B")
                && !currentAllocation.equalsIgnoreCase("P")) {
            try {
                percentage = Double.parseDouble(currentAllocation) * 100;
            } catch (NumberFormatException ignored) {
                // Log or handle the exception if necessary
            }
        }
        return percentage;
    }
}
