package com.epam.rd.autocode.spring.project.conf;

import com.epam.rd.autocode.spring.project.service.UserService;
import com.epam.rd.autocode.spring.project.util.JwtTokenUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final UserService userService;
    private final JwtTokenUtils jwtUtils;

    @Value("${jwt.lifetime}")
    private Duration lifeTime;


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        Optional<String> accessToken = getTokenFromCookie(request, "JWT");

        if (accessToken.isEmpty()) {
            log.debug("No JWT cookie found for request: {}", request.getRequestURI());
        }

        if (accessToken.isPresent() && jwtUtils.validateToken(accessToken.get())) {
            authenticateWithToken(accessToken.get());
        } else {
            Optional<String> refreshToken = getTokenFromCookie(request, "RefreshJWT");
            refreshToken.ifPresent(token -> tryToRefreshAuthentication(token, response));
        }

        filterChain.doFilter(request, response);
    }

    private void tryToRefreshAuthentication(String refreshToken, HttpServletResponse response) {
        if (!jwtUtils.validateToken(refreshToken)) {
            log.info("Refresh token is invalid or expired");
            return;
        }
        String username = jwtUtils.getSubject(refreshToken);

        if (username == null || username.isBlank()) {
            log.warn("Username is null in refresh token");
            return;
        }
        Set<String> roles = jwtUtils.getRoles(refreshToken);

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password("")
                .authorities(authorities)
                .build();
        String newAccessToken = jwtUtils.generateAccessToken(userDetails);

        Cookie newAccessTokenCookie = new Cookie("JWT", newAccessToken);
        newAccessTokenCookie.setHttpOnly(true);
        newAccessTokenCookie.setPath("/");
        newAccessTokenCookie.setMaxAge((int) lifeTime.toSeconds());
        response.addCookie(newAccessTokenCookie);

        authenticateWithToken(newAccessToken);
        log.info("Access token refreshed successfully for user: {}", username);
    }

    private void authenticateWithToken(String token) {
        try {
            String subject = jwtUtils.getSubject(token);

            UserDetails userDetails = userService.loadUserByUsername(subject);

            if (userDetails.isEnabled()) {
                log.debug("Authenticating user: {} with roles: {}", subject, userDetails.getAuthorities());
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                log.warn("User {} tried to login, but he disabled", subject);
                SecurityContextHolder.clearContext();
            }
        } catch (Exception e) {
            log.error("Auth error: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
    }

    private Optional<String> getTokenFromCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}