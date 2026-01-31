package com.epam.rd.autocode.spring.project.controller.auth;

import com.epam.rd.autocode.spring.project.aop.SecurityLoggingEvent;
import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.service.*;
import com.epam.rd.autocode.spring.project.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthRegisterController {
    private final ClientService clientService;
    private final UserService userService;
    private final EmployeeService employeeService;
    private final CookieUtils cookieUtils;

    @GetMapping(value = "/registration")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new ClientDTO());
        return "auth/registration";
    }

    @PostMapping(value = "/registration")
    @SecurityLoggingEvent(message = "Client registration requested")
    public String registration(@ModelAttribute("user") @Valid ClientDTO clientDTO,
                               BindingResult result, Model model,
                               HttpServletResponse response) {
        if (result.hasErrors()) {
            return "auth/registration";
        }
        if (userService.existsByEmail(clientDTO.getEmail())) {
            model.addAttribute("emailError", "User already exist");
            return "auth/registration";
        }
        clientService.addClient(clientDTO);

        autoLogin(clientDTO, response);

        return "redirect:/books";
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/employee/registration")
    public String showRegistrationFormForEmpl(Model model) {
        model.addAttribute("user", new EmployeeDTO());
        return "auth/add_empl";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/employee/registration")
    @SecurityLoggingEvent(message = "Employee registration requested", value = Level.WARN)
    public String registrationEmployee(@ModelAttribute("user") @Valid EmployeeDTO employeeDTO, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "auth/add_empl";
        }
        if (userService.existsByEmail(employeeDTO.getEmail())) {
            model.addAttribute("emailError", "User already exist");
            return "auth/add_empl";
        }
        employeeService.addEmployee(employeeDTO);

        return "redirect:/employees";
    }

    private void autoLogin(ClientDTO clientDTO, HttpServletResponse response) {
        UserDetails userDetails = userService.loadUserByUsername(clientDTO.getEmail());

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        cookieUtils.cookiesSetUp(response, authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}
