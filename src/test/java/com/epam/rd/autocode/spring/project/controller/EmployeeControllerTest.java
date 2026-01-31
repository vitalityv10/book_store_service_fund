package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.dto.topUp.ClientTopUpRequest;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableArgumentResolver;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeDTO employee;
    private List<EmployeeDTO> employees;
    private Page<EmployeeDTO> employeesPage;

    @BeforeEach
    void setUp() {
        employee = EmployeeDTO.builder()
                .password("$2a$10$8kbSX.9C602LS9nIqYf6IOkF1u9Vw4PfWQ4.rOTby9LlsfL6OiGke")
                .email("john.doe@email.com")
                .name("John Doe")
                .birthDate(LocalDate.now().minusYears(1))
                .phone("123456789")
                .build();

        employees = Collections.singletonList(employee);
        employeesPage = new PageImpl<>(employees, PageRequest.of(0, 5), 1);

    }

    @Test
    @WithMockUser(roles= "CLIENT")
    void employeePage_Client_404() throws Exception {
        mockMvc.perform(get("/employees"))
                .andExpect(status().isNotFound());
    }

    @Test
    void employeePage_Anonymous_302() throws Exception {
        mockMvc.perform(get("/employees"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeePage_ReturnViewWithEmployee() throws Exception {
       when(employeeService.getEmployeesByFilter(any(EmployeeDTO.class), any(Pageable.class)))
               .thenReturn(employeesPage);

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/employees"))
                .andExpect(model().attributeExists("employees"))
                .andExpect(model().attributeExists("employeeFilter"));

    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeePage_ReturnViewWithEmployeeByFilter() throws Exception {
        when(employeeService.getEmployeesByFilter(any(EmployeeDTO.class), any(Pageable.class)))
                .thenReturn(employeesPage);

        mockMvc.perform(get("/employees")
                        .param("name", employee.getName()))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/employees"))
                .andExpect(model().attribute("employees", hasProperty("content", is(employeesPage.getContent()))))
                .andExpect(model().attribute("employeeFilter", hasProperty("name",is(employee.getName()))));

        verify(employeeService, times(1)).getEmployeesByFilter(any(EmployeeDTO.class), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeePage_ReturnViewWithEmployeeInASC() throws Exception {
        when(employeeService.getEmployeesByFilter(any(EmployeeDTO.class), any(Pageable.class)))
                .thenReturn(employeesPage);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        mockMvc.perform(get("/employees")
                        .param("name", employee.getName())
                        .param("sort", "name,asc"))
                .andExpect(status().isOk());

        verify(employeeService).getEmployeesByFilter(any(EmployeeDTO.class), captor.capture());
        Pageable pageable = captor.getValue();

        assertNotNull(pageable.getSort().getOrderFor("name"));
        assertEquals(Sort.Direction.ASC, pageable.getSort().getOrderFor("name").getDirection());
    }


    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void showMyAccount_Success() throws Exception {
        when(employeeService.getEmployeeByEmail(anyString()))
                .thenReturn(employee);

        mockMvc.perform(get("/employees/account/{email}", employee.getEmail()))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/employee_account_info"))
                .andExpect(model().attributeExists("employee"));

        verify(employeeService, times(1)).getEmployeeByEmail(anyString());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void showMyAccount_EmailNotExist() throws Exception {
        String nonExistEmail ="sa@gmail.com";

        when(employeeService.getEmployeeByEmail(nonExistEmail))
                .thenThrow(new NotFoundException("Client not found"));

        mockMvc.perform(get("/employees/account/{email}", nonExistEmail))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).getEmployeeByEmail(anyString());
    }

    @Test
    void showMyAccount_Anonymous_404() throws Exception {
        mockMvc.perform(get("/employees/account/{email}", employee.getEmail()))
                .andExpect(status().is3xxRedirection());

        verify(employeeService, never()).getEmployeeByEmail(anyString());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void showEditForm_Success() throws Exception {
        when(employeeService.getEmployeeByEmail(anyString()))
                .thenReturn(employee);

        mockMvc.perform(get("/employees/account/edit/{}", employee.getEmail()))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/employee_account_edit"))
                .andExpect(model().attributeExists("employee"));

        verify(employeeService, times(1)).getEmployeeByEmail(anyString());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void showEditForm_Client404() throws Exception {
        mockMvc.perform(get("/employees/account/edit/{email}", employee.getEmail()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(employeeService, never()).getEmployeeByEmail(anyString());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void editMyAccount_Success() throws Exception {
        when(employeeService.updateEmployeeByEmail(eq(employee.getEmail()), any(EmployeeDTO.class)))
                .thenReturn(employee);

        mockMvc.perform(patch("/employees/account/edit/{email}", employee.getEmail())
                .param("name", "Adolf")
                .param("email", employee.getEmail())
                .param("password", employee.getPassword())
                .param("phone", "+380991233684")
                .param("birthDate", employee.getBirthDate().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees/account/"+employee.getEmail()));
        verify(employeeService, times(1)).updateEmployeeByEmail(eq(employee.getEmail()), any(EmployeeDTO.class));
    }


    @Test
    @WithMockUser(roles = "EMPLOYEE", username = "john.doe@email.com")
    void editMyAccount_InvalidAttributes() throws Exception {
        mockMvc.perform(patch("/employees/account/edit/{}", employee.getEmail())
                        .param("name", "Adolf")
                        .param("email", employee.getEmail())
                        .param("password", "hfaf")
                        .param("phone", "0991233684412130")
                        .param("birthDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/employee_account_edit"))
                .andExpect(model().attributeExists("employee"))
                .andExpect(model().attributeHasFieldErrors("employee", "password",  "birthDate", "phone"));

        verify(employeeService, never())
                .updateEmployeeByEmail(anyString(), any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE", username = "john.doe@email.com")
    void deleteMyAccount() throws Exception {
        mockMvc.perform(delete("/employees/account/delete/{email}", employee.getEmail()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(employeeService, times(1))
                .deleteEmployeeByEmail(employee.getEmail());

    }
}