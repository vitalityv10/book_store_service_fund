package com.epam.rd.autocode.spring.project.dto.topUp;

import java.math.BigDecimal;


public record ClientTopUpRequest(
        String email,
        BigDecimal balance) {
}
