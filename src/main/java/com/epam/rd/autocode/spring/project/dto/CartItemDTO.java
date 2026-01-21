package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.CartItem;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemDTO {
    private String bookName;
    private BigDecimal price;
    private int quantity;

    public static CartItemDTO toCartItemDTO(CartItem cartItem){
        return new  CartItemDTO(
                cartItem.getBook().getName(),
                cartItem.getBook().getPrice(),
                cartItem.getQuantity()
        );
    }
}
