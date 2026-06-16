package com.hotel.config;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // 🔥 VERY IMPORTANT (skip auth APIs)
        if (path.startsWith("/api/auth")) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {

            try {
                String token = header.substring(7);
                var claims = jwtUtil.extractClaims(token);

                String email = claims.getSubject();
                String role = (String) claims.get("role");

                var authorities = java.util.List.of(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role)
                );

                var authToken = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        email, null, authorities
                );

                org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authToken);

            } catch (Exception e) {
                // 🔥 avoid breaking request
            }
        }

        chain.doFilter(request, response);
    }
}