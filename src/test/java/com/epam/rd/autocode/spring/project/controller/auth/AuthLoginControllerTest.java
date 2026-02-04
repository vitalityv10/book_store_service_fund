package com.epam.rd.autocode.spring.project.controller.auth;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.model.enums.*;
import com.epam.rd.autocode.spring.project.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthLoginControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private EmployeeDTO employee;
    private ClientDTO client;

    @BeforeEach
    void setUp() {
        employee = EmployeeDTO.builder()
                .password("$2a$10$8kbSX.9C602LS9nIqYf6IOkF1u9Vw4PfWQ4.rOTby9LlsfL6OiGke")
                .email("john.doe@email.com")
                .name("John Doe")
                .birthDate(LocalDate.now().minusYears(1))
                .phone("123456789")
                .build();

        client = ClientDTO.builder()
                .email("client2@example.com")
                .password("$2a$10$8kbSX.9C602LS9nIqYf6IOkF1u9Vw4PfWQ4.rOTby9LlsfL6OiGke")
                        .build();
    }

    @Test
    void showLoginForm_Success() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk());
    }

    @Test
    void login_Client_Success() throws Exception {
        String password = "securepass";
       when(userService.loadUserByUsername(client.getEmail()))
               .thenReturn(new User(
                       client.getEmail(),
                       client.getPassword(),
                       Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT"))
               ));
       mockMvc.perform(post("/auth/login")
               .param("email", client.getEmail())
               .param("password", password))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/books"));

       verify(userService, times(1)).loadUserByUsername(client.getEmail());

    }

    
    @Test
    void login_Employee_Success() throws Exception {
        String password = "securepass";
        getMockUser();

        mockMvc.perform(post("/auth/login")
                        .param("email", employee.getEmail())
                        .param("password", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(userService, times(1)).loadUserByUsername(employee.getEmail());

    }

    @Test
    void login_Wrong_Credentials_EmailNotFound() throws Exception {
        String wrongEmail = "sa@gmial.com";

        when(userService.loadUserByUsername(wrongEmail))
                .thenReturn(new User(
                        employee.getEmail(),
                        employee.getPassword(),
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))
                ));

        mockMvc.perform(post("/auth/login")
                        .param("email", wrongEmail)
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login?error=true"));

        verify(userService, atLeastOnce()).loadUserByUsername(wrongEmail);

    }
    @Test
    void login_Wrong_Credentials_InvalidPassword() throws Exception {
        getMockUser();

        mockMvc.perform(post("/auth/login")
                        .param("email", employee.getEmail())
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login?error=true"));

        verify(userService, atLeastOnce()).loadUserByUsername(employee.getEmail());

    }
    @Test
    void login_Jwt_Success() throws Exception {
        String password = "securepass";
        getMockUser();

        mockMvc.perform(post("/auth/login")
                        .param("email", employee.getEmail())
                        .param("password", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"))
                .andExpect(cookie().exists("JWT"))
                .andExpect(cookie().httpOnly("JWT", true));

        verify(userService, times(1))
                .loadUserByUsername(employee.getEmail());

    }

    @Test
    void logout() throws Exception {
    mockMvc.perform(get("/auth/logout"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/books"))
            .andExpect(cookie().maxAge("JWT", 0))
            .andExpect(cookie().maxAge("RefreshJWT", 0));

    }

    private void getMockUser() {
        when(userService.loadUserByUsername(employee.getEmail()))
                .thenReturn(new User(
                        employee.getEmail(),
                        employee.getPassword(),
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))
                ));
    }

}