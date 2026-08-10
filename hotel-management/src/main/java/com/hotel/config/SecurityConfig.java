package com.hotel.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final String frontendOrigin;

    public SecurityConfig(
            JwtFilter jwtFilter,
            @Value("${app.cors.frontend-origin:}") String frontendOrigin) {
        this.jwtFilter = jwtFilter;
        this.frontendOrigin = frontendOrigin;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .cors(cors ->
                cors.configurationSource(corsConfigurationSource())
            )

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(auth -> auth

                // Permit browser CORS preflight requests.
                .requestMatchers(HttpMethod.OPTIONS, "/**")
                .permitAll()

                // Only unauthenticated account-creation and recovery routes are public.
                // Password changes and account deletion must continue through JWT auth.
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/auth/login",
                    "/api/auth/signup",
                    "/api/auth/send-signup-otp",
                    "/api/auth/verify-signup-otp",
                    "/api/auth/forgot-password/send-otp",
                    "/api/auth/forgot-password/reset"
                )
                .permitAll()

                // Only administrators can access admin APIs.
                .requestMatchers("/api/admin/**")
                .hasRole("ADMIN")

                // Existing browsers may hold a short-lived JWT issued before
                // HOTEL_OWNER replaced OWNER. Keep that legacy role valid only
                // for the room-edit route while those tokens expire.
                .requestMatchers(HttpMethod.PUT, "/updateById/**")
                .hasAnyRole("ADMIN", "HOTEL_OWNER", "OWNER")

                // Admin and hotel owner can create hotels and rooms.
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/hotels",
                    "/api/hotel-images",
                    "/addRoom",
                    "/addRoomsBulk",
                    "/api/room-images"
                )
                .hasAnyRole("ADMIN", "HOTEL_OWNER")

                // Admin and hotel owner can update hotels and rooms.
                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/hotels/*",
                    "/api/hotels/*/submit",
                    "/updateByRoomNumber/**",
                    "/api/room-images/**"
                )
                .hasAnyRole("ADMIN", "HOTEL_OWNER")

                // Admin and hotel owner can delete hotels, rooms and images.
                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/hotels/**",
                    "/deleteRoom/**",
                    "/deleteRoomById/**",
                    "/api/hotel-images/**",
                    "/api/room-images/**"
                )
                .hasAnyRole("ADMIN", "HOTEL_OWNER")

                // All other APIs require a valid JWT.
                .anyRequest()
                .authenticated()
            )

            .addFilterBefore(
                jwtFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        List<String> allowedOrigins = new ArrayList<>(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));

        if (frontendOrigin != null && !frontendOrigin.isBlank()) {
            allowedOrigins.add(frontendOrigin.trim());
        }

        configuration.setAllowedOriginPatterns(allowedOrigins);

        configuration.setAllowedMethods(
            List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
            )
        );

        configuration.setAllowedHeaders(
            List.of(
                "Authorization",
                "Content-Type",
                "Accept"
            )
        );

        configuration.setExposedHeaders(
            List.of("Authorization")
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
            "/**",
            configuration
        );

        return source;
    }
}
