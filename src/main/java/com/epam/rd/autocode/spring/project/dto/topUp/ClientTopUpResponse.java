package com.epam.rd.autocode.spring.project.dto.topUp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientTopUpResponse {
    private String email;
    private BigDecimal balance;
}
