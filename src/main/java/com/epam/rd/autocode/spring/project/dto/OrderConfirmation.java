package com.epam.rd.autocode.spring.project.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@AllArgsConstructor
@Builder
public class OrderConfirmation {
     private CartDTO cartDTO;
     private BigDecimal balance;
     private Boolean canAfford;
}
