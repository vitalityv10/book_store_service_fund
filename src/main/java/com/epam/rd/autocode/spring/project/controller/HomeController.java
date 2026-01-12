package com.epam.rd.autocode.spring.project.controller;

import jakarta.annotation.security.RolesAllowed;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HomeController {

    @GetMapping
    @RolesAllowed({"ADMIN", "CLIENT", "EMPLOYEE"})
    public String index() {
        return "home";
    }
}
