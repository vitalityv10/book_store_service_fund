package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class OrderConfirmation {
     private CartDTO cartDTO;
     private BigDecimal balance;
     private Boolean canAfford;
}
