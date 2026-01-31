package com.epam.rd.autocode.spring.project.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.thymeleaf.exceptions.TemplateProcessingException;

import java.util.Locale;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final MessageSource messageSource;

    @ExceptionHandler({NotFoundException.class, NoResourceFoundException.class, AccessDeniedException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNotFound(Exception ex, Locale locale) {
        log.warn("[SECURITY] Access Denied for client - hiding as 404");
        return createModelAndView("error/404", new Exception("error.not_found"), locale);
    }
    @ExceptionHandler(NoHandlerFoundException.class)
    public ModelAndView handleNotFound(NoHandlerFoundException ex, Locale locale) {
        log.error("Page not found: {}", ex.getRequestURL());
        return createModelAndView("error/404", new Exception("error.not_found"), locale);
    }

    @ExceptionHandler({DisabledException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleForbidden(Exception ex, Locale locale) {
        log.warn("[SECURITY] Access denied, returning 404: {}", ex.getMessage());
        return createModelAndView("error/404", ex, locale);
    }


    @ExceptionHandler(AuthenticationException.class)
    public String handleAuthError(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return "redirect:/auth/login?error=true";
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleAll(Exception ex, Locale locale) {
        log.error("Unhandled exception caught: ", ex);
        if (ex instanceof DataIntegrityViolationException) {
            return createModelAndView("error/404", ex, locale);
        }
        return createModelAndView("error/404", ex, locale);
    }

    @ExceptionHandler({IllegalStateException.class, DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ModelAndView handleConflict(Exception ex, Locale locale) {
        return createModelAndView("error/409", ex, locale);
    }

    @ExceptionHandler({AlreadyExistException.class})
    public ModelAndView handleAlreadyExist(Exception ex, Locale locale) {
        log.error("AlreadyExist exception caught: ", ex);
        return createModelAndView("error/404", ex, locale);
    }

    private ModelAndView createModelAndView(String viewName, Exception ex, Locale locale) {
        ModelAndView modelAndView = new ModelAndView(viewName);

        String errorMessage;
        try {
            errorMessage = messageSource.getMessage(ex.getMessage(), null, locale);
        } catch (NoSuchMessageException | NullPointerException e) {
            errorMessage = ex.getMessage();
        }

        modelAndView.addObject("message", errorMessage);
        modelAndView.addObject("error", errorMessage);

        log.warn("[EXCEPTION] Path: {}, Message: {}", viewName, errorMessage);
        return modelAndView;
    }
}