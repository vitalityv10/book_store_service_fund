package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.ForgotPassword;
import com.epam.rd.autocode.spring.project.repo.ForgotPasswordRepository;
import com.epam.rd.autocode.spring.project.service.ForgotPasswordService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ForgotPasswordServiceImplTest {
    @Autowired
    private ForgotPasswordRepository forgotPasswordRepository;

    @Autowired
    private ForgotPasswordService forgotPasswordService;

    private ForgotPassword forgotPassword;


    @BeforeEach
    void setUp() {
        forgotPassword = ForgotPassword.builder()
                .otp(123456)
                .email("new@email.com")
                .expiryDate(LocalDateTime.now().minusMinutes(1))
                .build();
    }

    @AfterEach
    void tearDown() {
        forgotPasswordRepository.deleteAll();
    }

    @Test
    void saveForgotPassword_Success() {
        String email= "new@email.com";
        forgotPasswordRepository.save(forgotPassword);

        ForgotPassword saved = forgotPasswordService.getForgotPassword(email, forgotPassword.getOtp());
        assertNotNull(saved);
        assertEquals(email, saved.getEmail());
    }


    @Test
    void getForgotPassword_ReturnNewObjectByMail() {
        String email= "new@email.com";

        assertNotNull(forgotPasswordService.getForgotPassword(email));
    }
    @Test
    void getForgotPassword_NotFound() {
        String email= "@email.com";

        assertThrows(NotFoundException.class,
                () -> forgotPasswordService.getForgotPassword(email, forgotPassword.getOtp()));
    }



    @Test
    void deleteByEmail_Success() {
        String email = "to-delete@email.com";
        forgotPassword.setEmail(email);
        forgotPasswordRepository.save(forgotPassword);
        assertTrue(forgotPasswordRepository.getByEmailAndOtp(email, forgotPassword.getOtp()).isPresent());

        forgotPasswordService.deleteByEmail(email);

        assertFalse(forgotPasswordRepository.getByEmailAndOtp(email, forgotPassword.getOtp()).isPresent());
    }

    @Test
    void deleteByEmail_EmailNotFound() {
        String nonExistentEmail = "notfound@email.com";

        assertThrows(NotFoundException.class,
                () -> forgotPasswordService.deleteByEmail(nonExistentEmail));
    }

    @Test
    void deleteById() {
        ForgotPassword saved = forgotPasswordRepository.save(forgotPassword);
        Long id = saved.getId();

        forgotPasswordService.deleteById(id);

        assertFalse(forgotPasswordRepository.findById(id.toString()).isPresent());
    }
}