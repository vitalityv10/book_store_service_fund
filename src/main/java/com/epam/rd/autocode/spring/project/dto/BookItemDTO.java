package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.BookItem;
import com.epam.rd.autocode.spring.project.model.Cart;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookItemDTO {
    @NotBlank(message = "{book.name.not_blank}")
    private String bookName;

    @NotNull(message = "{book.quantity.not_null}")
    @Positive(message = "{book.quantity.positive}")
    private Integer quantity;


    public static BookItemDTO toBookItemDTO(BookItem bookItem){
        return new BookItemDTO(
                bookItem.getBook().getName(),
                bookItem.getQuantity()
        );
    }
}
