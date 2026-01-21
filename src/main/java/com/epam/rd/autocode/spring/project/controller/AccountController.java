package com.epam.rd.autocode.spring.project.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AccountController {

    @GetMapping("/accounts")
    public String account(Authentication authentication) {
        String email = authentication.getName();
        boolean isEmployee = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        if(isEmployee || isAdmin)
            return "redirect:/employees/account/" + email;
        else{
            return "redirect:/clients/account/" + email;
        }
    }

//    @GetMapping("/orders")
//    public String orders(Authentication authentication) {
//        String email = authentication.getName();
//        boolean isEmployee = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
//        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
//        if(isEmployee || isAdmin)
//            return "redirect:/orders/my/all/" + email;
//        else{
//            return "redirect:/orders/my/" + email;
//        }
//    }
}
