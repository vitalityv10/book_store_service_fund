package com.epam.rd.autocode.spring.project.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.event.Level;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("within(com.epam.rd.autocode.spring.project.service.impl..*) || " +
            "within(com.epam.rd.autocode.spring.project.controller..*)")
    public void applicationPackagePointcut() {}

    @AfterThrowing(pointcut = "applicationPackagePointcut()", throwing = "e")
    public void logError(JoinPoint joinPoint, Throwable e) {
        String methodName = joinPoint.getSignature().toShortString();
        log.error("[EXCEPTION] Метод: {}. Помилка: {}", methodName, e.getMessage());
    }

    @Around("applicationPackagePointcut() && @annotation(businessEvent)")
    public Object logBusinessEvent(ProceedingJoinPoint joinPoint, BusinessLoggingEvent businessEvent) throws Throwable {
        log.info("[START] {}", businessEvent.message());
        try {
            Object result = joinPoint.proceed();
            log.info("[SUCCESS] {}", businessEvent.message());
            return result;
        } catch (Throwable t) {
            log.error("[FAILED] {}", businessEvent.message());
            throw t;
        }
    }

    @Around("applicationPackagePointcut() && @annotation(securityEvent)")
    public Object logSecurityEvent(ProceedingJoinPoint joinPoint, SecurityLoggingEvent securityEvent) throws Throwable {
        String userContext = getCurrentUserContext();
        String methodName = joinPoint.getSignature().toShortString();

        try {
            Object result = joinPoint.proceed();
            String msg = String.format("[SECURITY SUCCESS] %s. Користувач: %s. Метод: %s",
                    securityEvent.message(), userContext, methodName);
            logWithLevel(securityEvent.value(), msg);
            return result;
        } catch (Throwable t) {
            String msg = String.format("[SECURITY ALERT] %s. Користувач: %s. Спроба невдала: %s",
                    securityEvent.message(), userContext, t.getMessage());
            log.error(msg);
            throw t;
        }
    }

    private String getCurrentUserContext() {
        String username = "анонім";
        String ipAddress = "невідомий IP";

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            username = auth.getName();
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            ipAddress = request.getRemoteAddr();
        }

        return String.format("[%s | IP: %s]", username, ipAddress);
    }

    private void logWithLevel(Level level, String message) {
        switch (level) {
            case ERROR -> log.error(message);
            case WARN  -> log.warn(message);
            case DEBUG -> log.debug(message);
            case TRACE -> log.trace(message);
            default    -> log.info(message);
        }
    }

}
