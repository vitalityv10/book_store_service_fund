package com.epam.rd.autocode.spring.project.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {
    @Mock
    private MessageSource messageSource;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    private final String testEmail = "test@example.com";
    private final Integer testOtp = 123456;
    private final String mailFrom = "noreply@system.com";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "mailFrom", mailFrom);
    }

    @Test
    void sendMessage_ShouldSendCorrectEmail() {
        String subject = "OTP Code";
        String bodyTemplate = "Your code is: %s";
        String expectedBody = "Your code is: 123456";

        when(messageSource.getMessage(eq("mail.subject.otp"), any(), any(Locale.class)))
                .thenReturn(subject);
        when(messageSource.getMessage(eq("mail.body.otp"), any(), any(Locale.class)))
                .thenReturn(bodyTemplate);

        emailService.sendMessage(testEmail, testOtp);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertEquals(testEmail, capturedMessage.getTo()[0]);
        assertEquals(mailFrom, capturedMessage.getFrom());
        assertEquals(subject, capturedMessage.getSubject());
        assertEquals(expectedBody, capturedMessage.getText());
    }
}