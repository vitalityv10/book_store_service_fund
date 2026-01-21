package com.epam.rd.autocode.spring.project.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientDTO{
    @NotBlank(message = "{validation.user.email.not_blank}")
    @Email(message = "{validation.user.email.invalid}")
    private String email;

    @NotBlank(message = "{validation.user.password.not_blank}")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z]).{8,}$",
            message = "{validation.user.password.pattern}"
    )
    private String password;

    @NotBlank(message = "{validation.user.name.not_blank}")
    @Size(min = 2, max = 50, message = "{validation.user.name.size}")
    private String name;

    @Min(value = 0)
    private BigDecimal balance;

}
