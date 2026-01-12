package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegisterResponseDto {
    @NotBlank(message = "You must enter the name")
    private String name;
    @NotBlank(message = "You must enter your password")
    private String password;
    @Email
    @NotBlank(message = "You must enter you email")
    private String email;

    private Set<Role> roles;
}
