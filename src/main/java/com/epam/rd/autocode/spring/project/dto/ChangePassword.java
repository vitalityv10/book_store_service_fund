package com.epam.rd.autocode.spring.project.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChangePassword {
    @NotBlank(message = "{validation.user.password.not_blank}")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z]).{8,}$",
            message = "{validation.user.password.pattern}"
    )
    private String password;
    @NotBlank(message = "{validation.user.password.not_blank}")
    private String confirmPassword;
}
