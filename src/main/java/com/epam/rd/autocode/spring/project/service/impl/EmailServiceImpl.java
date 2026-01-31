package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.aop.BusinessLoggingEvent;
import com.epam.rd.autocode.spring.project.aop.SecurityLoggingEvent;
import com.epam.rd.autocode.spring.project.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final MessageSource messageSource;
    private final JavaMailSender mailSender;

    @Value("${MAIL_USERNAME:default_user}")
    private String mailFrom;

    @Override
    @BusinessLoggingEvent(message = "OTP was sent", value = Level.INFO)
    public void sendMessage(String email, Integer otp) {
        Locale locale = LocaleContextHolder.getLocale();

        String subject = messageSource.getMessage("mail.subject.otp", null, locale);
        String bodyTemplate = messageSource.getMessage("mail.body.otp", null, locale);
        String body = String.format(bodyTemplate, otp);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom(mailFrom);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

}
