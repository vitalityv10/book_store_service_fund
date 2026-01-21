package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name ="EMPLOYEES")
public class Employee extends User{
    @Column(name = "PHONE")
    private String phone;
    @Column(name = "BIRTH_DATE")
    private LocalDate birthDate;
}
//-- INSERT INTO EMPLOYEES (BIRTH_DATE, EMAIL, NAME,
// PASSWORD, PHONE)
