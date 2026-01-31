package com.epam.rd.autocode.spring.project.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class CookieUtils {
    private final JwtTokenUtils jwtUtils;

    @Value("${jwt.lifetime}")
    private Duration lifetime;

    public void setCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        addCookie(response, "JWT", accessToken, (int) lifetime.toSeconds());
        addCookie(response, "RefreshJWT", refreshToken, (int) lifetime.multipliedBy(10).toSeconds());
    }

    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        log.debug("Setting cookie '{}' with maxAge: {}s", name, maxAge);
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    public static void clearCookie(HttpServletResponse response, String name) {
        log.debug("Clearing cookie: {}", name);
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }


    public void cookiesSetUp(HttpServletResponse response, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String accessToken = jwtUtils.generateAccessToken(userDetails);
        String refreshToken = jwtUtils.generateRefreshToken(userDetails);

        log.info("JWT tokens generated for user: {}", userDetails.getUsername());
        setCookies(response, accessToken, refreshToken);
    }
}
