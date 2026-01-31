package com.epam.rd.autocode.spring.project.exception;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class ErrorController{
    @GetMapping("/403")
    public void handle403() {
        throw new AccessDeniedException("not found");
    }
}