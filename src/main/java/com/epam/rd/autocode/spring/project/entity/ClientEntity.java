package com.epam.rd.autocode.spring.project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name ="CLIENTS")
public class ClientEntity {
    @Id
    @Column(name = "EMAIL")
    private String email;

    @Column(name = "BALANCE")
    private BigDecimal balance;

    @Column(name = "NAME")
    private String name;

    @Column(name = "PASSWORD")
    private String password;

}
