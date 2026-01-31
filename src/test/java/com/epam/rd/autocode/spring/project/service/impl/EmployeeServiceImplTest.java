package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EmployeeServiceImplTest {
    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setName("Walter");
        employee.setEmail("walterw@example.com");
        employee.setPassword("$2a$10$8kbSX.9C602LS9nIqYf6IOkF1u9Vw4PfWQ4.rOTby9LlsfL6OiGke");
        employee.setRoles(new HashSet<>(Set.of(Role.ROLE_EMPLOYEE)));
        employeeRepository.save(employee);
    }

    @Test
    void getAllEmployees_SuccessReturnNumOfEmployees() {
        Integer numOfEmpl = 11;

        List<EmployeeDTO> employees = employeeService.getAllEmployees();

        assertNotNull(employees);
        assertEquals(numOfEmpl, employeeService.getAllEmployees().size());
    }

    @Test
    void getEmployeeByEmail_Success() {
        EmployeeDTO employeeDTO = employeeService.getEmployeeByEmail(employee.getEmail());

        assertNotNull(employeeDTO);
        assertEquals(employee.getName(), employeeDTO.getName());
    }
    @Test
    void getEmployeeByEmail_EmailNotFound() {
        String email = "jassie@gmail.com";
       NotFoundException exception = assertThrows(NotFoundException.class,
               () -> employeeService.getEmployeeByEmail(email));

        assertEquals("Employee not found", exception.getMessage());
    }

    @Test
    void updateEmployeeByEmail_Success() {
        String oldEmail = employee.getEmail();
        EmployeeDTO updateDto = EmployeeDTO.builder()
                .name("Updated Name")
                .email("new.email@example.com")
                .password("securepass")
                .phone("+380992336388")
                .birthDate(LocalDate.now().minusYears(10))
                .build();

        EmployeeDTO result = employeeService.updateEmployeeByEmail(oldEmail, updateDto);

        assertEquals("Updated Name", result.getName());
        assertEquals("new.email@example.com", result.getEmail());

        assertThrows(NotFoundException.class, () -> employeeService.getEmployeeByEmail(oldEmail));
    }

    @Test
    void updateEmployeeByEmail_EmployeeNotFound() {
        String oldEmail = employee.getEmail();
        EmployeeDTO updateDto = EmployeeDTO.builder()
                .name("Updated Name")
                .email("new.email@example.com")
                .password("new.password")
                .phone("+380992336388")
                .birthDate(LocalDate.now().minusYears(10))
                .build();

        EmployeeDTO employeeToUpdate = employeeService.getEmployeeByEmail(oldEmail);
        employeeToUpdate.setName(updateDto.getName());
        employeeToUpdate.setEmail(updateDto.getEmail());

        assertThrows(NotFoundException.class, () -> employeeService
                .updateEmployeeByEmail(employeeToUpdate.getEmail(), updateDto));
    }

    @Test
    void deleteEmployeeByEmail_Success() {
        employeeService.deleteEmployeeByEmail(employee.getEmail());

        assertThrows(NotFoundException.class, () ->
                employeeService.getEmployeeByEmail(employee.getEmail()));
    }

    @Test
    void deleteEmployeeByEmail_EmployeeNotFound() {
        String email = "walterw@g.com";
        assertThrows(NotFoundException.class, () ->
                employeeService.deleteEmployeeByEmail(email));
    }

    @Test
    void addEmployee() {
        EmployeeDTO updateDto = EmployeeDTO.builder()
                .name("Updated Name")
                .email("new.email@example.com")
                .password("new.password")
                .phone("+380992336388")
                .birthDate(LocalDate.now().minusYears(10))
                .build();

        EmployeeDTO savedEmployee = employeeService.addEmployee(updateDto);

        assertNotNull(savedEmployee);
        assertEquals(savedEmployee.getName(), updateDto.getName());

        assertTrue(employeeRepository.findByEmail(employee.getEmail()).isPresent());

    }

    @Test
    void getEmployeeByFilter_Success() {
        Pageable pageable = PageRequest.of(0, 5);

        Page<EmployeeDTO> result = employeeService.getEmployeesByFilter(new EmployeeDTO(), pageable);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(11, result.getTotalElements());
    }

    @Test
    void getEmployeeByFilter_NameSuccess() {
        EmployeeDTO employeeDTO = EmployeeDTO.builder()
                .name("Walter")
                .build();
        Pageable pageable = PageRequest.of(0, 5);

        Page<EmployeeDTO> result = employeeService.getEmployeesByFilter(employeeDTO, pageable);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.getContent().stream()
                .anyMatch(c -> c.getName().contains("Walter")));
    }
}