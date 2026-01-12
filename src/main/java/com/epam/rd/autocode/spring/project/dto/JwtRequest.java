package com.epam.rd.autocode.spring.project.dto;

import lombok.Data;

@Data
public class JwtRequest {
    private String email;
    private String password;
}
