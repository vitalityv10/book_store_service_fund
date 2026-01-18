package com.epam.rd.autocode.spring.project.controller.auth;

import com.epam.rd.autocode.spring.project.aop.SecurityLoggingEvent;
import com.epam.rd.autocode.spring.project.util.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import static com.epam.rd.autocode.spring.project.util.CookieUtils.clearCookie;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthLoginController {

    private final AuthenticationManager authenticationManager;
    private final CookieUtils cookieUtils;

    @RequestMapping(value = "/login", method =  RequestMethod.GET)
    public String showLoginForm() {
        return "login";
    }

    @RequestMapping(value = "/login", method =  RequestMethod.POST)
    @SecurityLoggingEvent(message = "Login attempt to the service")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        @RequestParam(defaultValue = "") String redirect,
                        HttpServletResponse response) {

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));

            cookieUtils.cookiesSetUp(response, authentication);
          //  log.info("[SECURITY EVENT] Успішний вхід: {}", email);

            if (redirect == null || redirect.isBlank() || redirect.equals("null")) {
                return "redirect:/books";
            }
            return "redirect:" + redirect;
    }

    @RequestMapping(value = "/logout", method =   RequestMethod.GET)
    @SecurityLoggingEvent(message = "Logout attempt to the service")
    public String logout(HttpServletResponse response) {
        clearCookie(response, "JWT");
        clearCookie(response, "RefreshJWT");
        return "redirect:/books";
    }
}
