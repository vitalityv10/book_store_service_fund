package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.aop.BusinessLoggingEvent;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.ForgotPassword;
import com.epam.rd.autocode.spring.project.repo.ForgotPasswordRepository;
import com.epam.rd.autocode.spring.project.service.ForgotPasswordService;
import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ForgotPasswordServiceImpl implements ForgotPasswordService {
    private final ForgotPasswordRepository forgotPasswordRepository;

    @Override
    @Transactional
    @BusinessLoggingEvent(message = "OTP added", value = Level.INFO)
    public void saveForgotPassword(ForgotPassword forgotPassword) {
        forgotPasswordRepository.save(forgotPassword);
    }

    @Override
    public ForgotPassword getForgotPassword(String email) {
        return ForgotPassword.builder()
                .otp(otpGenerator())
                .expiryDate(LocalDateTime.now().plusMinutes(2))
                .email(email)
                .build();

    }

    @Override
    public ForgotPassword getForgotPassword(String email, Integer otp) {
        return forgotPasswordRepository.getByEmailAndOtp(email, otp)
                .orElseThrow(() -> new NotFoundException("Otp not found"));
    }

    @Override
    @Transactional
    @BusinessLoggingEvent(message = "OTP deleted", value = Level.INFO)
    public void deleteByEmail(String email) {
        if (!forgotPasswordRepository.existsByEmail(email)) {
            throw new NotFoundException("Email not found");
        }
        forgotPasswordRepository.deleteByEmail(email);
    }

    @Override
    @Transactional
    @BusinessLoggingEvent(message = "OTP deleted", value = Level.INFO)
    public void deleteById(Long id) {
        forgotPasswordRepository.deleteById(id);
    }

    private Integer otpGenerator() {
        SecureRandom random = new SecureRandom();
        return random.nextInt(900_000) + 100_000;
    }
}
