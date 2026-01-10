package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.entity.ClientEntity;
import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientDTO{
    private String email;
    private String password;
    private String name;
    private BigDecimal balance;

    public static ClientDTO toClientDTO(ClientEntity client) {
        return builder()
                .email(client.getEmail())
                .password(client.getPassword())
                .name(client.getName())
                .balance(client.getBalance())
                .build();
    }
}
