package com.epam.rd.autocode.spring.project.controller.auth;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.model.ForgotPassword;
import com.epam.rd.autocode.spring.project.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ForgotPasswordControllerTest {
   @Autowired
   private MockMvc mockMvc;

   @MockBean
   private ForgotPasswordService forgotPasswordService;
   @MockBean
   private UserService userService;
   @MockBean
   private EmailService emailService;

   private EmployeeDTO employee;
   private ForgotPassword forgotPassword;

    @BeforeEach
    void setUp() {
        forgotPassword = ForgotPassword.builder().otp(123414).build();
        employee = EmployeeDTO.builder()
                .password("password123")
                .email("john.doe@email.com")
                .name("John Doe")
                .birthDate(LocalDate.now().minusYears(1))
                .phone("+380991323484")
                .build();

    }

    @Test
    void showForm() throws Exception {
        mockMvc.perform(get("/auth/forgot-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/forgot_password"));
    }

    @Test
    void verifyMail_EmailExists() throws Exception {
        when(userService.existsByEmail(employee.getEmail())).thenReturn(true);
        when(forgotPasswordService.getForgotPassword(employee.getEmail())).thenReturn(forgotPassword);
       doNothing().when(emailService).sendMessage(eq(employee.getEmail()), any(Integer.class));

        mockMvc.perform(post("/auth/forgot-password/verify-mail")
                        .param("email", employee.getEmail()))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/verify_otp"))
                .andExpect(model().attribute("email", employee.getEmail()));

        verify(emailService, times(1)).sendMessage(eq(employee.getEmail()), any(Integer.class));
        verify(forgotPasswordService, times(1)).saveForgotPassword(any());

    }
    @Test
    void verifyMail_EmailNotExists() throws Exception {
        when(userService.existsByEmail(employee.getEmail())).thenReturn(false);

        mockMvc.perform(post("/auth/forgot-password/verify-mail")
                        .param("email", employee.getEmail()))
                .andExpect(status().is3xxRedirection());

        verify(emailService, never()).sendMessage(anyString(), any(Integer.class));
        verify(forgotPasswordService, never()).saveForgotPassword(any());

    }


    @Test
    void showOtpPage() throws Exception {
        mockMvc.perform(get("/auth/forgot-password/verify-otp-page/{email}", employee.getEmail())
                .param("email", employee.getEmail()))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/verify_otp"));
    }

    @Test
    void verifyOTP_ExpiredOTP() throws Exception {
        when(forgotPasswordService.getForgotPassword(eq(employee.getEmail()), eq(forgotPassword.getOtp())))
                .thenReturn(forgotPassword);
        forgotPassword.setExpiryDate(LocalDateTime.now().minusMinutes(1));

        ForgotPassword newForgotPassword = new ForgotPassword();
        newForgotPassword.setOtp(999999);

        when(forgotPasswordService.getForgotPassword(eq(employee.getEmail())))
                .thenReturn(newForgotPassword);

        doNothing().when(forgotPasswordService).deleteById(eq(forgotPassword.getId()));

        mockMvc.perform(post("/auth/forgot-password/verify-otp")
                        .param("email", employee.getEmail())
                        .param("otp", String.valueOf(forgotPassword.getOtp())))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/verify_otp"))
                .andExpect(model().attribute("error", "OTP has expired"));

        verify(forgotPasswordService, times(1)).deleteById(eq(forgotPassword.getId()));
        verify(forgotPasswordService, times(1)).getForgotPassword(eq(employee.getEmail()));
    }

    @Test
    void verifyOTP_Success() throws Exception{
        when(forgotPasswordService.getForgotPassword(eq(employee.getEmail()), eq(forgotPassword.getOtp())))
                .thenReturn(forgotPassword);

        forgotPassword.setExpiryDate(LocalDateTime.now().plusMinutes(5));

            mockMvc.perform(post("/auth/forgot-password/verify-otp")
                        .param("email", employee.getEmail())
                        .param("otp", String.valueOf(forgotPassword.getOtp())))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/new_password"))
                .andExpect(model().attribute("email", employee.getEmail()))
                .andExpect(model().attributeExists("changePassword"));

        verify(forgotPasswordService, atLeastOnce()).getForgotPassword(eq(employee.getEmail()), eq(forgotPassword.getOtp()));
    }

    @Test
    void changePassword_Success() throws Exception {
        ChangePassword changePassword = new ChangePassword();
        changePassword.setPassword("NewPass123");
        changePassword.setConfirmPassword("NewPass123");

        doNothing().when(userService).updatePassword(eq(employee.getEmail()), eq("NewPass123"));

        mockMvc.perform(patch("/auth/forgot-password/change-password/{email}", employee.getEmail())
                        .param("password", changePassword.getPassword())
                        .param("confirmPassword", changePassword.getConfirmPassword()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
        verify(userService, times(1)).updatePassword(eq(employee.getEmail()), eq(changePassword.getPassword()));
    }
    @Test
    void changePassword_PasswordsDoNotMatch() throws Exception {
        mockMvc.perform(patch("/auth/forgot-password/change-password/{email}", employee.getEmail())
                        .param("password", "Password123")
                        .param("confirmPassword", "DifferentPass456"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/new_password"))
                .andExpect(model().attributeHasFieldErrors("changePassword", "confirmPassword"))
                .andExpect(model().attribute("email", employee.getEmail()));

        verify(userService, never()).updatePassword(anyString(), anyString());
    }

    @Test
    void changePassword_EmptyPassword_ShouldReturnError() throws Exception {
        mockMvc.perform(patch("/auth/forgot-password/change-password/{email}", employee.getEmail() )
                        .param("password", "")
                        .param("confirmPassword", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/new_password"));

        verify(userService, never()).updatePassword(anyString(), anyString());
    }

}