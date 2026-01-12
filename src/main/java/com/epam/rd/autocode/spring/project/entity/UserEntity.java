package com.epam.rd.autocode.spring.project.entity;

import com.epam.rd.autocode.spring.project.model.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "BALANCE")
    private BigDecimal balance;

    @Column(name = "NAME")
    private String name;

    @Column(name = "PASSWORD")
    private String password;

    @ElementCollection(fetch = FetchType.EAGER, targetClass =  Role.class)
    @Enumerated(EnumType.STRING)
    @JoinTable(name = "user_role", joinColumns = {@JoinColumn(name = "user_id") })
    @Column(name = "role", nullable = false)
    private Set<Role> roles;
}
