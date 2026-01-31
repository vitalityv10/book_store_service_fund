package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.model.*;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;


import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceImpTest {

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Employee employee;
    private Client client;

    @BeforeEach
    void setUp() {
        employee = new Employee();
                employee.setPassword("$2a$10$8kbSX.9C602LS9nIqYf6IOkF1u9Vw4PfWQ4.rOTby9LlsfL6OiGke");
                employee.setEmail("john.wick@email.com");
                employee.setRoles(new HashSet<>(Set.of(Role.ROLE_EMPLOYEE)));
                employeeRepository.save(employee);

        client = new Client();
                client.setEmail("walterw@example.com");
                client.setPassword("$2a$10$8kbSX.9C602LS9nIqYf6IOkF1u9Vw4PfWQ4.rOTby9LlsfL6OiGke");
                client.setRoles(new HashSet<>(Set.of(Role.ROLE_CLIENT)));
        clientRepository.save(client);
    }

    @AfterEach
    void tearDown() {
        if (client.getId() != null) clientRepository.deleteById(client.getId());
        if (employee.getId() != null) employeeRepository.deleteById(employee.getId());
    }


    @Test
    void loadUserByUsername_UserNotFound() {
       UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
               () -> userService.loadUserByUsername("john"));
       assertEquals("User not found: john", exception.getMessage());
    }

    @Test
    void loadUserByUsername_Client() {
        UserDetails userClient = userService.loadUserByUsername(client.getEmail());

        assertEquals(client.getEmail(), userClient.getUsername());
        assertTrue(userClient.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority()
                        .equals("ROLE_CLIENT")));    }

    @Test
    void loadUserByUsername_Employee() {
        UserDetails userClient = userService.loadUserByUsername(employee.getEmail());

        assertEquals(employee.getEmail(), userClient.getUsername());
        assertTrue(userClient.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority()
                        .equals("ROLE_EMPLOYEE")));
    }

    @Test
    @Transactional
    void loadUserByUsername_UserBlocked() {
        client.getRoles().add(Role.ROLE_BLOCKED);
        clientRepository.save(client);

        UserDetails userClient = userService.loadUserByUsername(client.getEmail());
        assertFalse( userClient.isEnabled());
    }

    @Test
    void loadUserByUsername_AccountFlags() {
        UserDetails userDetails = userService.loadUserByUsername(client.getEmail());

        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
    }

    @Test
    void loadUserByUsername_EmailCaseInsensitive() {
        String upperEmail = employee.getEmail().toUpperCase();

        UserDetails userDetails = userService.loadUserByUsername(upperEmail);
        assertEquals(employee.getEmail(), userDetails.getUsername());
    }
    @Test
    void loadUserByUsername_PriorityTest() {
        Client duplicateClient = new Client();
        duplicateClient.setEmail(employee.getEmail());
        duplicateClient.setPassword("$2a$10$8kbSX.9C602LS9nIqYf6IOkF1u9Vw4PfWQ4.rOTby9LlsfL6OiGke");
        duplicateClient.setRoles(Set.of(Role.ROLE_CLIENT));
        clientRepository.save(duplicateClient);

        UserDetails result = userService.loadUserByUsername(employee.getEmail());

        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT")));
    }

    @Test
    void existsByEmail_ReturnsTrue() {
        assertTrue(userService.existsByEmail(employee.getEmail()));
    }
    @Test
    void existsByEmail_NotExistsEmail() {
        assertFalse(userService.existsByEmail("newemail@gmail.com"));
        assertFalse(userService.existsByEmail(" "));
    }

    @Test
    void existsByEmail_CaseSensitiveEmail(){
        assertTrue(userService.existsByEmail(employee.getEmail().toUpperCase()));
    }

    @Test
    @Transactional
    void updatePassword_ClientSuccess() {
        String newPassword = "newClientPassword123";

        userService.updatePassword(client.getEmail(), newPassword);
        Client updatedClient = clientRepository.findByEmail(client.getEmail()).get();

        assertTrue(passwordEncoder.matches(newPassword, updatedClient.getPassword()));
    }

    @Test
    @Transactional
    void updatePassword_EmployeeSuccess() {
        String newPassword = "newEmployeePassword456";

        userService.updatePassword(employee.getEmail(), newPassword);
        Employee updatedEmployee = employeeRepository.findByEmail(employee.getEmail()).get();

        assertTrue(passwordEncoder.matches(newPassword, updatedEmployee.getPassword()));
    }

    @Test
    @Transactional
    void updatePassword_CaseInsensitivity() {
        String upperEmail = client.getEmail().toUpperCase();
        String newPassword = "passwordCaseTest";

        userService.updatePassword(upperEmail, newPassword);
        Client updatedClient = clientRepository.findByEmail(client.getEmail()).get();

        assertTrue(passwordEncoder.matches(newPassword, updatedClient.getPassword()));
    }

    @Test
    @Transactional
    void updatePassword_UserNotFound() {
        assertThrows(java.util.NoSuchElementException.class, () -> {
            userService.updatePassword("nonexistent@mail.com", "anyPassword");
        });
    }

}