package com.epam.rd.autocode.spring.project.model;

import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
public class Employee extends User{
    private String phone;
    private LocalDate birthDate;

    public Employee(Long id, String email, String password,
                    String name, String phone, LocalDate birthDate) {
        super(id, email, password, name);
        this.phone = phone;
        this.birthDate = birthDate;
    }
}
