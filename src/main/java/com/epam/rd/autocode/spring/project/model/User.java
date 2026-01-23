package com.epam.rd.autocode.spring.project.model;

import com.epam.rd.autocode.spring.project.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "name")
    private String name;

    @Column(name = "password")
    private String password;

    @ElementCollection(fetch = FetchType.EAGER, targetClass =  Role.class)
    @Enumerated(EnumType.STRING)
    @JoinTable(name = "user_role", joinColumns = {@JoinColumn(name = "id"
            ,referencedColumnName = "id") })
    @Column(name = "role", nullable = false)
    private Set<Role> roles;
}