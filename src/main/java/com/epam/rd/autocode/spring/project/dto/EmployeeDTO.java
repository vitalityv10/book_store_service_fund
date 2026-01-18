package com.epam.rd.autocode.spring.project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeDTO{
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

    private String phone;
    private LocalDate birthDate;
}
