package com.epam.rd.autocode.spring.project.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeDTO{
    private UUID employeeId;

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

    @NotBlank(message = "{validation.user.phone.not_blank}")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "{validation.user.phone.invalid}")
    private String phone;

    @NotNull(message = "{validation.user.birthDate.not_null}")
    @Past(message = "{validation.user.birthDate.past}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
}
