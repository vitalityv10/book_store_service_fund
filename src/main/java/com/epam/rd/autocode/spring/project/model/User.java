package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    private Long id;
    private String email;
    private String password;
    private String name;

//    @ElementCollection(fetch = FetchType.EAGER, targetClass = Role.class)
//    @Enumerated(EnumType.STRING)
//    @JoinTable(name = "client_id", joinColumns = {@JoinColumn(name = "client_id") })
//    @Column(name = "role", nullable = false)
//    private Role role;
}
