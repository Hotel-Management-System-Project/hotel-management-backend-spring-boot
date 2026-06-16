package com.hotel.config;

import java.io.IOException;

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

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            var claims = jwtUtil.extractClaims(token);

            String email = claims.getSubject();
            String role = (String) claims.get("role"); // Extract the role string

            if (email != null && org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() == null) {
                // Convert your string role into a GrantedAuthority Spring Security understands
                var authorities = java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(role));

                // Create the authentication object
                var authToken = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        email, null, authorities
                );

                // Tell Spring Security that this request is officially authenticated
                org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            // Keep these in case your controllers read them directly from request attributes
            request.setAttribute("email", email);
            request.setAttribute("role", role);
        }

        chain.doFilter(request, response);
    }


}
