package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.dto.PageResponse;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@PreAuthorize("hasAnyRole('EMPLOYEE')")
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    @GetMapping
    public String employeePage(EmployeeDTO employeeDTO, @PageableDefault(size = 5) Pageable pageable, Model model) {
        Page<EmployeeDTO> employeePage = employeeService.getEmployeesByFilter(employeeDTO, pageable);
        model.addAttribute("employees", PageResponse.of(employeePage));
        model.addAttribute("employeeFilter", employeeDTO);
        return "employee/employees";
    }


    @GetMapping("/account/{email}")
    public String showMyAccount(@PathVariable("email") String email, Model model) {
        EmployeeDTO employeeDTO = employeeService.getEmployeeByEmail(email);
        model.addAttribute("employee", employeeDTO);
        return "employee/employee_account_info";
    }

    @GetMapping("/account/edit/{email}")
    public String showEditForm(@PathVariable("email") String email, Model model) {
        EmployeeDTO employeeDTO = employeeService.getEmployeeByEmail(email);
        model.addAttribute("employee", employeeDTO);
        return "employee/employee_account_edit";
    }

    @PatchMapping("/account/edit/{email}")
    public String editMyAccount(@ModelAttribute("employee")@Valid EmployeeDTO employeeDTO,
                                BindingResult bindingResult,
                                @PathVariable("email") String email){
        if (bindingResult.hasErrors()) {
            return "employee/employee_account_edit";
        }
        employeeService.updateEmployeeByEmail(email, employeeDTO);
        return "redirect:/employees/account/" + employeeDTO.getEmail();
    }

    @DeleteMapping("/account/delete/{email}")
    public String deleteMyAccount(@PathVariable("email") String email){
        employeeService.deleteEmployeeByEmail(email);
        return "redirect:/books";
    }


}
