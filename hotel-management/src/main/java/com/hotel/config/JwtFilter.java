package com.hotel.config;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication
        .UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority
        .SimpleGrantedAuthority;
import org.springframework.security.core.context
        .SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        if (!HttpMethod.POST.matches(request.getMethod())) {
            return false;
        }

        String path = request.getServletPath();
        return path.equals("/api/auth/login")
                || path.equals("/api/auth/signup")
                || path.equals("/api/auth/send-signup-otp")
                || path.equals("/api/auth/verify-signup-otp")
                || path.equals("/api/auth/forgot-password/send-otp")
                || path.equals("/api/auth/forgot-password/reset");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            System.out.println(
                    "JWT MISSING: "
                    + request.getMethod()
                    + " "
                    + request.getRequestURI()
            );

            sendUnauthorized(
                    response,
                    "Authorization token is missing"
            );
            return;
        }

        String token = authorizationHeader
                .substring(7)
                .trim();

        if (token.isBlank()) {
            sendUnauthorized(
                    response,
                    "Authorization token is empty"
            );
            return;
        }

        try {
            Claims claims = jwtUtil.extractClaims(token);

            String email = claims.getSubject();
            String role = claims.get("role", String.class);

            if (email == null || email.isBlank()) {
                throw new RuntimeException(
                        "JWT email claim is missing"
                );
            }

            if (role == null || role.isBlank()) {
                throw new RuntimeException(
                        "JWT role claim is missing"
                );
            }

            String authority = role.startsWith("ROLE_")
                    ? role
                    : "ROLE_" + role;

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(
                                    new SimpleGrantedAuthority(authority)
                            )
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

            System.out.println(
                    "JWT authenticated: "
                    + email
                    + " - "
                    + authority
                    + " - "
                    + request.getMethod()
                    + " "
                    + request.getRequestURI()
            );

            filterChain.doFilter(request, response);

        } catch (Exception exception) {
            SecurityContextHolder.clearContext();

            System.out.println(
                    "JWT ERROR: "
                    + exception.getMessage()
                    + " - "
                    + request.getMethod()
                    + " "
                    + request.getRequestURI()
            );

            sendUnauthorized(
                    response,
                    "Invalid or expired authentication token"
            );
        }
    }

    private void sendUnauthorized(
            HttpServletResponse response,
            String message
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        response.getWriter().write(
                "{\"status\":\"error\",\"message\":\""
                + message
                + "\"}"
        );
    }
}
