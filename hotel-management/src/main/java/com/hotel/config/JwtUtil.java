package com.hotel.config;

import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtil {
	private final String SECRET = "hotel-secret-key";

	public String generateToken(String email, String role) {
		
		return Jwts.builder()
				.setSubject(email)
				.claim("role", role)
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + 86400000))
				.signWith(SignatureAlgorithm.HS256, SECRET)
				.compact();
		
	}
	
	    public Claims extractClaims(String token) {
	        return Jwts.parser().setSigningKey(SECRET)
	                .parseClaimsJws(token).getBody();
	    }

	
}
