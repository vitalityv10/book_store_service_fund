package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImp implements UserService {
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        return clientRepository.findByEmail(email)
                .map(client -> (com.epam.rd.autocode.spring.project.model.User) client)
                .or(() -> employeeRepository.findByEmail(email)
                        .map(emp -> (com.epam.rd.autocode.spring.project.model.User) emp))
                .map(UserServiceImp::getUser)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
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
        return clientRepository.findByEmail(email).isPresent() || employeeRepository.findByEmail(email).isPresent();
    }
}
