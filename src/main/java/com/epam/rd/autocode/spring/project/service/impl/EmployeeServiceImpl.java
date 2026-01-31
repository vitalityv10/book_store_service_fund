package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.aop.BusinessLoggingEvent;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.*;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final OrderRepository orderRepository;

    @Override
    @BusinessLoggingEvent(message = "Employees review starting ")
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
                .orElseThrow(() -> new NotFoundException("Employee not found"));
    }

    @Override
    @BusinessLoggingEvent(message = "Employee updating")
    @Transactional
    public EmployeeDTO updateEmployeeByEmail(String email, EmployeeDTO employee) {
       Employee employeeToUpdate = employeeRepository.findByEmail(email)
               .orElseThrow(() -> new NotFoundException("Employee not found"));
        if(passwordEncoder.matches(employee.getPassword(), employeeToUpdate.getPassword()))
        {
            employeeToUpdate.setName(employee.getName());
            employeeToUpdate.setEmail(employee.getEmail());
            employeeToUpdate.setBirthDate(employee.getBirthDate());
            employeeToUpdate.setPhone(employee.getPhone());

            Employee updatedEmployee = employeeRepository.save(employeeToUpdate);
            return modelMapper.map(updatedEmployee, EmployeeDTO.class);
        }else{
            throw new IllegalArgumentException("password.mismatch");
        }
    }

    @Override
    @BusinessLoggingEvent(message = "Employee deleting")
    @Transactional
    public void deleteEmployeeByEmail(String email) {
        if (!canEmployeeBeDeleted(email)) {
            throw new IllegalStateException("error.client.has.active.orders");
        }
        Employee employeeToDelete = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Employee not found"));
        employeeRepository.delete(employeeToDelete);
    }

    @Override
    @Transactional
    @BusinessLoggingEvent(message = "Employee registration")
    public EmployeeDTO addEmployee(EmployeeDTO employee) {
        Employee newEmployee = modelMapper.map(employee, Employee.class);
        newEmployee.setPassword(passwordEncoder.encode(employee.getPassword()));
        newEmployee.setRoles(Collections.singleton(Role.valueOf("ROLE_EMPLOYEE")));
        employeeRepository.save(newEmployee);
        return modelMapper.map(newEmployee, EmployeeDTO.class);
    }

    @Override
    @BusinessLoggingEvent(message = "Employees by filter review starting ")
    public Page<EmployeeDTO> getEmployeesByFilter(EmployeeDTO employeeDTO, Pageable pageable) {
        QEmployee qEmployee = QEmployee.employee;

        BooleanBuilder where = new BooleanBuilder();

        if (employeeDTO.getName() != null && !employeeDTO.getName().isBlank()) {
            where.and(qEmployee.name.containsIgnoreCase(employeeDTO.getName()));
        }

        return employeeRepository.findAll(where, pageable)
                .map(employee -> modelMapper.map(employee, EmployeeDTO.class));
    }

    private boolean canEmployeeBeDeleted(String email) {
        EmployeeDTO employee = getEmployeeByEmail(email);
        return !orderRepository.existsByEmployeeId(employee.getEmployeeId());
    }
}
