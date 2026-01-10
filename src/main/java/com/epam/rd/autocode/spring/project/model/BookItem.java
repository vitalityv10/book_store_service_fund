package com.epam.rd.autocode.spring.project.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookItem {
    private Long id;
    private Order order;
    private Book book;
    private Integer quantity;
}
