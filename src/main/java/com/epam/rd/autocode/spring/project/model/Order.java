package com.epam.rd.autocode.spring.project.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Order {
    private Long id;
    private Client client;
    private Employee employee;
    private LocalDateTime orderDate;
    private BigDecimal price;
    private List<BookItem> bookItems;
}
