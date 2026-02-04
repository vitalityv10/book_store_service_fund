package com.epam.rd.autocode.spring.project.controller.auth;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.model.enums.*;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import com.epam.rd.autocode.spring.project.service.UserService;
import jakarta.validation.constraints.Min;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
class AuthRegisterControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;
    @MockBean
    private EmployeeService employeeService;
    @MockBean
    private UserService userService;

    private EmployeeDTO employee;
    private ClientDTO client;

    @BeforeEach
    void setUp() {
        employee = EmployeeDTO.builder()
                .password("password123")
                .email("john.doe@email.com")
                .name("John Doe")
                .birthDate(LocalDate.now().minusYears(1))
                .phone("+380991323484")
                .build();

        client = ClientDTO.builder()
                .password("password123")
                .email("client2@example.com")
                .name("Client 2")
                .build();
    }

    @Test
    void showEmployeeRegistrationForm_404() throws Exception {
        mockMvc.perform(get("/auth/employee/registration"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser (roles = "ADMIN")
    void showEmployeeRegistrationForm() throws Exception {
        mockMvc.perform(get("/auth/employee/registration"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/add_empl"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @WithMockUser (roles = "ADMIN")
    void employeeRegistration_Successful() throws Exception {
        when(userService.existsByEmail(anyString())).thenReturn(false);
        when(employeeService.addEmployee(any(EmployeeDTO.class)))
                .thenReturn(employee);

        mockMvc.perform(post("/auth/employee/registration")
                .param("email", employee.getEmail())
                .param("password", employee.getPassword())
                .param("name", employee.getName())
                                .param("phone", employee.getPhone())
                                .param("birthDate", employee.getBirthDate().toString())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"));

        verify(employeeService, times(1)).addEmployee(any(EmployeeDTO.class));
    }
    @Test
    @WithMockUser (roles = "ADMIN")
    void employeeRegistration_InvalidPhone_ShouldReturnErrors() throws Exception {
        mockMvc.perform(post("/auth/employee/registration")
                        .param("email", employee.getEmail())
                        .param("password", employee.getPassword())
                        .param("name", employee.getName())
                        .param("phone", "1324940829018409382")
                        .param("birthDate", employee.getBirthDate().toString())
                )
                .andExpect(status().isOk())
                .andExpect(view().name("auth/add_empl"))
                .andExpect(model().attributeHasFieldErrors("user", "phone"));
        verify(employeeService, never()).addEmployee(any(EmployeeDTO.class));
    }
    @Test
    @WithMockUser (roles = "ADMIN")
    void employeeRegistration_FutureDate_ShouldReturnErrors() throws Exception {
        mockMvc.perform(post("/auth/employee/registration")
                        .param("email", employee.getEmail())
                        .param("password", employee.getPassword())
                        .param("name", employee.getName())
                        .param("phone", employee.getPhone())
                        .param("birthDate", "2026-06-18")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("auth/add_empl"))
                .andExpect(model().attributeHasFieldErrors("user", "birthDate"));
        verify(employeeService, never()).addEmployee(any(EmployeeDTO.class));
    }


    @Test
    void showRegistrationForm() throws Exception {
        mockMvc.perform(get("/auth/registration"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registration"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void clientRegistration_Successful() throws Exception {
        UserDetails mockUserDetails = User.builder()
                .username(client.getEmail())
                .password(client.getPassword())
                .authorities("ROLE_CLIENT")
                .build();

        when(userService.existsByEmail(client.getEmail())).thenReturn(false);
        when(clientService.addClient(any(ClientDTO.class))).thenReturn(client);

        when(userService.loadUserByUsername(client.getEmail())).thenReturn(mockUserDetails);

        mockMvc.perform(post("/auth/registration")
                        .param("email", client.getEmail())
                        .param("password", client.getPassword())
                        .param("name", client.getName()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(clientService, times(1)).addClient(any(ClientDTO.class));
    }
    @Test
    void clientRegistration_InvalidEmail_ShouldReturnErrors() throws Exception {
        mockMvc.perform(post("/auth/registration")
                        .with(csrf())
                        .param("email", "newmail")
                        .param("password", client.getPassword())
                        .param("name", client.getName()))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registration"))
                .andExpect(model().attributeHasFieldErrors("user", "email"));

        verify(clientService, never()).addClient(any(ClientDTO.class));
    }
    @Test
    void clientRegistration_BlankName_ShouldReturnErrors() throws Exception {
        mockMvc.perform(post("/auth/registration")
                        .with(csrf())
                        .param("email", client.getEmail())
                        .param("password", client.getPassword())
                        .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registration"))
                .andExpect(model().attributeHasFieldErrors("user", "name"));

        verify(clientService, never()).addClient(any(ClientDTO.class));
    }
    @Test
    void clientRegistration_PasswordPatternMismatch_ShouldReturnErrors() throws Exception {
        mockMvc.perform(post("/auth/registration")
                        .with(csrf())
                        .param("email", client.getEmail())
                        .param("password", "short") //without numbers
                        .param("name", client.getName()))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registration"))
                .andExpect(model().attributeHasFieldErrors("user", "password"));

        verify(clientService, never()).addClient(any(ClientDTO.class));
    }



}