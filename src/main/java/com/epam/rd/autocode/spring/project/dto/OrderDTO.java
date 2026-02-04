package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.model.Order;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDTO{
    private UUID id;
    @NotBlank(message = "{order.client_email.not_blank}")
    @Email(message = "{order.email.invalid}")
    private String clientEmail;

    private String employeeEmail;

    @NotNull(message = "{order.date.not_null}")
    private LocalDateTime orderDate;

    @NotNull(message = "{order.price.not_null}")
    @Positive(message = "{order.price.positive}")
    private BigDecimal price;

    @NotEmpty(message = "{order.items.not_empty}")
    @Valid
    private List<BookItemDTO> bookItems;

    private OrderStatus orderStatus;

    public static OrderDTO toOrderDTO(Order order){
        return new OrderDTO(
                order.getId(),
                Optional.ofNullable(order.getClient())
                        .map(Client::getEmail)
                        .orElse("Client delete acc"),
                Optional.ofNullable(order.getEmployee())
                        .map(Employee::getEmail)
                        .orElse("Not Assigned"),
                order.getOrderDate(),
                order.getPrice(),
                order.getBookItems().stream()
                        .map(BookItemDTO::toBookItemDTO).toList(),
                order.getOrderStatus()
        );
    }
}
