package com.epam.rd.autocode.spring.project.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtTokenUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.lifetime}")
    private Duration lifeTime;


    private String generateToken(UserDetails userDetails, Duration expiration) {

        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration.toMillis());
//        String role = userDetails.getAuthorities().stream()
//                .findFirst()
//                .map(grantedAuthority -> grantedAuthority.getAuthority())
//                .orElse("ROLE_CLIENT");

        Set <String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(userDetails, this.lifeTime);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(userDetails, this.lifeTime.multipliedBy(10));
    }
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getSubject(String token) {
        return getAllClaims(token).getSubject();
    }

//    public String getRole(String token) {
//        return getAllClaims(token).get("role", String.class);
//    }

    public Set<String> getRoles(String token) {
        //return getAllClaims(token).get("roles", Set.class);
        Claims claims = getAllClaims(token);
        Object roles = claims.get("roles");

        if (roles instanceof Collection<?>) {
            return ((Collection<?>) roles).stream()
                    .map(Object::toString)
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    public String getEmail(String token) {
        return getAllClaims(token).get("email", String.class);
    }

    public Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}


