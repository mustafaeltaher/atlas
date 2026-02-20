package com.atlas.service;

import com.atlas.dto.AddSkillRequest;
import com.atlas.dto.EmployeeSkillDTO;
import com.atlas.dto.SkillDTO;
import com.atlas.entity.Employee;
import com.atlas.entity.EmployeeSkill;
import com.atlas.entity.Skill;
import com.atlas.entity.TechTower;
import com.atlas.entity.User;
import com.atlas.repository.EmployeeRepository;
import com.atlas.repository.EmployeeSkillRepository;
import com.atlas.repository.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmployeeSkillService.
 * Tests ABAC access control, skill assignment/removal, and DTO conversions.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Employee Skill Service Tests")
public class EmployeeSkillServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private EmployeeSkillRepository employeeSkillRepository;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeSkillService employeeSkillService;

    private Employee employee;
    private User currentUser;
    private Skill skill;
    private TechTower tower;
    private EmployeeSkill employeeSkill;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .id(1L)
                .oracleId(1000)
                .name("Test Employee")
                .email("test@atlas.com")
                .build();

        currentUser = new User();
        currentUser.setEmployee(employee);

        tower = TechTower.builder()
                .id(1)
                .description("Backend Development")
                .build();

        skill = Skill.builder()
                .id(42)
                .description("Java Programming")
                .tower(tower)
                .build();

        employeeSkill = EmployeeSkill.builder()
                .id(123)
                .employee(employee)
                .skill(skill)
                .skillLevel(EmployeeSkill.SkillLevel.PRIMARY)
                .skillGrade(EmployeeSkill.SkillGrade.ADVANCED)
                .build();
    }

    // ========== getEmployeeSkills Tests ==========

    @Test
    @DisplayName("getEmployeeSkills - should return list of skills when access granted")
    void getEmployeeSkills_withAccess_returnsSkills() {
        // Arrange
        when(employeeService.getAccessibleEmployeeIds(currentUser))
                .thenReturn(List.of(1L));
        when(employeeSkillRepository.findByEmployeeId(1L))
                .thenReturn(List.of(employeeSkill));

        // Act
        List<EmployeeSkillDTO> result = employeeSkillService.getEmployeeSkills(1L, currentUser);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(123);
        assertThat(result.get(0).getSkillId()).isEqualTo(42);
        assertThat(result.get(0).getSkillDescription()).isEqualTo("Java Programming");
        assertThat(result.get(0).getSkillLevel()).isEqualTo("PRIMARY");
        assertThat(result.get(0).getSkillGrade()).isEqualTo("ADVANCED");
    }

    @Test
    @DisplayName("getEmployeeSkills - should return empty list when employee has no skills")
    void getEmployeeSkills_noSkills_returnsEmptyList() {
        // Arrange
        when(employeeService.getAccessibleEmployeeIds(currentUser))
                .thenReturn(List.of(1L));
        when(employeeSkillRepository.findByEmployeeId(1L))
                .thenReturn(Collections.emptyList());

        // Act
        List<EmployeeSkillDTO> result = employeeSkillService.getEmployeeSkills(1L, currentUser);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getEmployeeSkills - should throw exception when access denied")
    void getEmployeeSkills_noAccess_throwsException() {
        // Arrange
        when(employeeService.getAccessibleEmployeeIds(currentUser))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        assertThatThrownBy(() -> employeeSkillService.getEmployeeSkills(1L, currentUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied to employee: 1");
    }

    @Test
    @DisplayName("getEmployeeSkills - should handle skills with null level and grade")
    void getEmployeeSkills_nullLevelAndGrade_handlesGracefully() {
        // Arrange
        EmployeeSkill skillWithNulls = EmployeeSkill.builder()
                .id(124)
                .employee(employee)
                .skill(skill)
                .skillLevel(null)
                .skillGrade(null)
                .build();

        when(employeeService.getAccessibleEmployeeIds(currentUser))
                .thenReturn(List.of(1L));
        when(employeeSkillRepository.findByEmployeeId(1L))
                .thenReturn(List.of(skillWithNulls));

        // Act
        List<EmployeeSkillDTO> result = employeeSkillService.getEmployeeSkills(1L, currentUser);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSkillLevel()).isNull();
        assertThat(result.get(0).getSkillGrade()).isNull();
    }

    // ========== getAvailableSkills Tests ==========

    @Test
    @DisplayName("getAvailableSkills - should return skills not assigned to employee")
    void getAvailableSkills_withAccess_returnsAvailableSkills() {
        // Arrange
        when(employeeService.getAccessibleEmployeeIds(currentUser))
                .thenReturn(List.of(1L));
        when(skillRepository.findAvailableSkillsForEmployee(1L))
                .thenReturn(List.of(skill));

        // Act
        List<SkillDTO> result = employeeSkillService.getAvailableSkills(1L, currentUser);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(42);
        assertThat(result.get(0).getDescription()).isEqualTo("Java Programming");
        assertThat(result.get(0).getTowerDescription()).isEqualTo("Backend Development");
    }

    @Test
    @DisplayName("getAvailableSkills - should throw exception when access denied")
    void getAvailableSkills_noAccess_throwsException() {
        // Arrange
        when(employeeService.getAccessibleEmployeeIds(currentUser))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        assertThatThrownBy(() -> employeeSkillService.getAvailableSkills(1L, currentUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied to employee: 1");
    }

    @Test
    @DisplayName("getAvailableSkills - should handle skills without tower")
    void getAvailableSkills_skillWithoutTower_handlesGracefully() {
        // Arrange
        Skill skillWithoutTower = Skill.builder()
                .id(43)
                .description("Generic Skill")
                .tower(null)
                .build();

        when(employeeService.getAccessibleEmployeeIds(currentUser))
                .thenReturn(List.of(1L));
        when(skillRepository.findAvailableSkillsForEmployee(1L))
                .thenReturn(List.of(skillWithoutTower));

        // Act
        List<SkillDTO> result = employeeSkillService.getAvailableSkills(1L, currentUser);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTowerDescription()).isNull();
    }

    // ========== addSkillToEmployee Tests ==========

    @Test
    @DisplayName("addSkillToEmployee - should successfully add skill with access")
    void addSkillToEmployee_withAccess_addsSkill() {
        // Arrange
        AddSkillRequest request = AddSkillRequest.builder()
                .skillId(42)
                .skillLevel(EmployeeSkill.SkillLevel.SECONDARY)
                .skillGrade(EmployeeSkill.SkillGrade.INTERMEDIATE)
                .build();

        when(employeeService.getAccessibleEmployeeIds(currentUser))
                .thenReturn(List.of(1L));
        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));
        when(skillRepository.findById(42))
                .thenReturn(Optional.of(skill));
        when(employeeSkillRepository.existsByEmployeeIdAndSkillId(1L, 42))
                .thenReturn(false);
        when(employeeSkillRepository.save(any(EmployeeSkill.class)))
                .thenReturn(employeeSkill);

        // Act
        EmployeeSkillDTO result = employeeSkillService.addSkillToEmployee(1L, request, currentUser);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getSkillId()).isEqualTo(42);
        verify(employeeSkillRepository).save(any(EmployeeSkill.class));
    }

    @Test
    @DisplayName("addSkillToEmployee - should throw exception when access denied")
    void addSkillToEmployee_noAccess_throwsException() {
        // Arrange
        AddSkillRequest request = AddSkillRequest.builder()
                .skillId(42)
                .skillLevel(EmployeeSkill.SkillLevel.PRIMARY)
                .skillGrade(EmployeeSkill.SkillGrade.ADVANCED)
                .build();

        when(employeeService.getAccessibleEmployeeIds(currentUser))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        assertThatThrownBy(() -> employeeSkillService.addSkillToEmployee(1L, request, currentUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied to employee: 1");
    }

    @Test
    @DisplayName("addSkillToEmployee - should throw exception when employee not found")
    void addSkillToEmployee_employeeNotFound_throwsException() {
        // Arrange
        AddSkillRequest request = AddSkillRequest.builder()
                .skillId(42)
                .skillLevel(EmployeeSkill.SkillLevel.PRIMARY)
                .skillGrade(EmployeeSkill.SkillGrade.ADVANCED)
                .build();

        when(employeeService.getAccessibleEmployeeIds(currentUser))
                .thenReturn(List.of(1L));
        when(employeeRepository.findById(1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> employeeSkillService.addSkillToEmployee(1L, request, currentUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Employee not found: 1");
    }

    @Test
    @DisplayName("addSkillToEmployee - should throw exception when skill not found")
    void addSkillToEmployee_skillNotFound_throwsException() {
        // Arrange
        AddSkillRequest request = AddSkillRequest.builder()
                .skillId(99)
                .skillLevel(EmployeeSkill.SkillLevel.PRIMARY)
                .skillGrade(EmployeeSkill.SkillGrade.ADVANCED)
                .build();

        when(employeeService.getAccessibleEmployeeIds(currentUser))
                .thenReturn(List.of(1L));
        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));
        when(skillRepository.findById(99))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> employeeSkillService.addSkillToEmployee(1L, request, currentUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Skill not found: 99");
    }

    @Test
    @DisplayName("addSkillToEmployee - should throw exception when skill already assigned")
    void addSkillToEmployee_duplicateSkill_throwsException() {
        // Arrange
        AddSkillRequest request = AddSkillRequest.builder()
                .skillId(42)
                .skillLevel(EmployeeSkill.SkillLevel.PRIMARY)
                .skillGrade(EmployeeSkill.SkillGrade.ADVANCED)
                .build();

        when(employeeService.getAccessibleEmployeeIds(currentUser))
                .thenReturn(List.of(1L));
        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));
        when(skillRepository.findById(42))
                .thenReturn(Optional.of(skill));
        when(employeeSkillRepository.existsByEmployeeIdAndSkillId(1L, 42))
                .thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> employeeSkillService.addSkillToEmployee(1L, request, currentUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Skill 42 is already assigned to employee 1");
    }

    // ========== removeSkillFromEmployee Tests ==========

    @Test
    @DisplayName("removeSkillFromEmployee - should successfully remove skill with access")
    void removeSkillFromEmployee_withAccess_removesSkill() {
        // Arrange
        when(employeeService.getAccessibleEmployeeIds(currentUser))
                .thenReturn(List.of(1L));
        when(employeeRepository.existsById(1L))
                .thenReturn(true);
        when(employeeSkillRepository.findByEmployeeIdAndSkillId(1L, 42))
                .thenReturn(Optional.of(employeeSkill));

        // Act
        employeeSkillService.removeSkillFromEmployee(1L, 42, currentUser);

        // Assert
        verify(employeeSkillRepository).delete(employeeSkill);
    }

    @Test
    @DisplayName("removeSkillFromEmployee - should throw exception when access denied")
    void removeSkillFromEmployee_noAccess_throwsException() {
        // Arrange
        when(employeeService.getAccessibleEmployeeIds(currentUser))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        assertThatThrownBy(() -> employeeSkillService.removeSkillFromEmployee(1L, 42, currentUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied to employee: 1");
    }

    @Test
    @DisplayName("removeSkillFromEmployee - should throw exception when employee not found")
    void removeSkillFromEmployee_employeeNotFound_throwsException() {
        // Arrange
        when(employeeService.getAccessibleEmployeeIds(currentUser))
                .thenReturn(List.of(1L));
        when(employeeRepository.existsById(1L))
                .thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> employeeSkillService.removeSkillFromEmployee(1L, 42, currentUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Employee not found: 1");
    }

    @Test
    @DisplayName("removeSkillFromEmployee - should throw exception when skill not assigned")
    void removeSkillFromEmployee_skillNotAssigned_throwsException() {
        // Arrange
        when(employeeService.getAccessibleEmployeeIds(currentUser))
                .thenReturn(List.of(1L));
        when(employeeRepository.existsById(1L))
                .thenReturn(true);
        when(employeeSkillRepository.findByEmployeeIdAndSkillId(1L, 99))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> employeeSkillService.removeSkillFromEmployee(1L, 99, currentUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Skill 99 is not assigned to employee 1");
    }
}
