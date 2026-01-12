package com.epam.rd.autocode.spring.project.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookItemDTO {
    private String bookName;
    private Integer quantity;
}
