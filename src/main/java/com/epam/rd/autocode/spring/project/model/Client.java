package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name ="clients")
public class Client extends User{
    @Column(name = "balance")
    private BigDecimal balance = BigDecimal.ZERO;

    @OneToOne(mappedBy = "client", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Cart cart;
}
