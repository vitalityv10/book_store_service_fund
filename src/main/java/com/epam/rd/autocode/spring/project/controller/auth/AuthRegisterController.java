package com.epam.rd.autocode.spring.project.controller.auth;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthRegisterController {
    private final ClientService clientService;
    private final UserService userService;
    private final EmployeeService employeeService;

    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new ClientDTO());
        model.addAttribute("registerAction", "/auth/registration");
        return "auth/registration";
    }

    @RequestMapping(value = "/registration", method =  RequestMethod.POST)
    public String registration(@ModelAttribute("user") @Valid ClientDTO clientDTO, BindingResult result, Model model) {
        return handleRegistration(clientDTO, result, model, clientDTO.getEmail(),
                () -> clientService.addClient(clientDTO));
    }

    @RequestMapping(value = "employee/registration", method = RequestMethod.GET)
    public String showRegistrationFormForEmpl(Model model) {
        model.addAttribute("user", new EmployeeDTO());
        model.addAttribute("registerAction", "/auth/employee/registration");
        return "auth/registration";
    }


    @RequestMapping(value = "/employee/registration", method = RequestMethod.POST)
    public String registrationEmployee(@ModelAttribute("user") @Valid EmployeeDTO employeeDTO, BindingResult result, Model model) {
        return handleRegistration(employeeDTO, result, model,  employeeDTO.getEmail(),
                () -> employeeService.addEmployee(employeeDTO));
    }


    private String handleRegistration(Object dto, BindingResult result, Model model,
                                       String email, Runnable saveTask) {
        if (result.hasErrors()) {
            return "auth/registration";
        }
        if (userService.existsByEmail(email)) {
            model.addAttribute("emailError", "Користувач з таким email вже існує");
            return "auth/registration";
        }
            saveTask.run();
            log.info("Successfully registered: {}", email);

        return "redirect:/auth/login";
    }
}
