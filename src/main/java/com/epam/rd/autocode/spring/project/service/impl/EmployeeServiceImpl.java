package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.aop.BusinessLoggingEvent;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientFilter;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.model.QClient;
import com.epam.rd.autocode.spring.project.model.QEmployee;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import com.querydsl.core.BooleanBuilder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(e -> modelMapper.map(e, EmployeeDTO.class))
                .toList();
    }

    @Override
    public EmployeeDTO getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .map(employee -> modelMapper.map(employee, EmployeeDTO.class))
                .orElseThrow(() -> new NotFoundException("Employee  not found"));
    }

    @Override
    @Transactional
    @BusinessLoggingEvent(message = "Employee updated")
    public EmployeeDTO updateEmployeeByEmail(String email, EmployeeDTO employee) {
       Employee employeeToUpdate = employeeRepository.findByEmail(email)
               .orElseThrow(() -> new NotFoundException("Employee  not found"));
       modelMapper.map(employee, employeeToUpdate);
       Employee updatedEmployee = employeeRepository.save(employeeToUpdate);
       return modelMapper.map(updatedEmployee, EmployeeDTO.class);
    }

    @Override
    public void deleteEmployeeByEmail(String email) {
        Employee employeeToDelete = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Employee  not found"));
        employeeRepository.delete(employeeToDelete);
    }

    @Override
    @Transactional
    @BusinessLoggingEvent(message = "Employee added")
    public EmployeeDTO addEmployee(EmployeeDTO employee) {
        Employee newEmployee = new Employee();
        newEmployee.setEmail(employee.getEmail());
        newEmployee.setName(employee.getName());
        newEmployee.setPassword(passwordEncoder.encode(employee.getPassword()));
        newEmployee.setRoles(Collections.singleton(Role.valueOf("ROLE_EMPLOYEE")));
        employeeRepository.save(newEmployee);
        return modelMapper.map(newEmployee, EmployeeDTO.class);
    }


    @Override
    public Page<EmployeeDTO> getEmployeesByFilter(EmployeeDTO employeeDTO, Pageable pageable) {
        QEmployee qEmployee = QEmployee.employee;

        BooleanBuilder where = new BooleanBuilder();

        if (employeeDTO.getName() != null && !employeeDTO.getName().isBlank()) {
            where.and(qEmployee.name.containsIgnoreCase(employeeDTO.getName()));
        }

        return employeeRepository.findAll(where, pageable)
                .map(employee -> modelMapper.map(employee, EmployeeDTO.class));
    }
}
