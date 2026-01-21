package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.Cart;
import com.epam.rd.autocode.spring.project.model.CartItem;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartDTO {
     private String clientEmail;
     private BigDecimal totalPrice;
     private List<CartItemDTO> items;

     public static CartDTO toCartDTO(Cart cart){
         return new CartDTO(
                 cart.getClient().getEmail(),
                 cart.getTotalPrice(),
                 cart.getItems().stream()
                         .map(CartItemDTO::toCartItemDTO).toList()
         );
     }

}
