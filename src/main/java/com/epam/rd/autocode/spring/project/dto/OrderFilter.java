package com.epam.rd.autocode.spring.project.dto;


import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderFilter(
        String clientEmail,
        String employeeEmail,
        LocalDateTime orderDate,
        BigDecimal price,
        OrderStatus orderStatus) {
}
