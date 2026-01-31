package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.aop.SecurityLoggingEvent;
import com.epam.rd.autocode.spring.project.model.*;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.*;
import com.epam.rd.autocode.spring.project.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImp implements UserService {
    private final PasswordEncoder passwordEncoder;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)
   // @SecurityLoggingEvent(message = "User logged in", value = Level.INFO)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    String emailLowCase = email.toLowerCase();

        return clientRepository.findByEmail(emailLowCase)
                .map(client -> (com.epam.rd.autocode.spring.project.model.User) client)
                .or(() -> employeeRepository.findByEmail(emailLowCase)
                        .map(emp -> (com.epam.rd.autocode.spring.project.model.User) emp))
                .map(UserServiceImp::getUser)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + emailLowCase));
    }
    private static org.springframework.security.core.userdetails.User getUser(User user) {
        Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toSet());

        boolean isBlocked = user.getRoles().contains(Role.ROLE_BLOCKED);

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                !isBlocked,
                true,
                true,
                true,
                authorities
        );
    }

    @Override
    public boolean existsByEmail(String email) {
        return clientRepository.findByEmail(email.toLowerCase()).isPresent()
                || employeeRepository.findByEmail(email.toLowerCase()).isPresent();
    }

    @Override
    @Transactional
    @SecurityLoggingEvent(message = "User changed his password",  value = Level.INFO)
    public void updatePassword(String email, String password){
        if(clientRepository.findByEmail(email.toLowerCase()).isPresent()){
           Client client = clientRepository.findByEmail(email.toLowerCase()).get();
           client.setPassword(passwordEncoder.encode(password));
           clientRepository.save(client);
        } else {
            Employee e  = employeeRepository.findByEmail(email.toLowerCase()).get();
            e.setPassword(passwordEncoder.encode(password));
            employeeRepository.save(e);
        }
    }
}
