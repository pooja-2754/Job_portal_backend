package com.pooja.jobportal.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey SECRET_KEY = Keys.hmacShaKeyFor("MY_SECRET_KEY_123456789012345678901234567890".getBytes());
    private final long EXPIRATION_TIME = 86400000; // 24 hours

    // Generate JWT token for user
    public String generateToken(String email) {
        return generateToken(email, "USER");
    }

    // Generate JWT token for company
    public String generateCompanyToken(String email) {
        return generateToken(email, "COMPANY");
    }

    // Generate JWT token with entity type
    public String generateToken(String email, String entityType) {
        return Jwts.builder()
                .subject(email)
                .claim("type", entityType) // "USER" or "COMPANY"
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    // Extract email from token
    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Validate token
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException |
                 SecurityException | IllegalArgumentException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // Get expiration date from token
    public Date getExpirationDateFromToken(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
    }

    // Check if token is expired
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    // Get remaining time in milliseconds
    public long getRemainingValidity(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            return 0;
        }
    }

    // Validate token with detailed response
    public TokenValidationResult validateTokenDetailed(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            return new TokenValidationResult(
                true,
                claims.getSubject(),
                claims.getExpiration(),
                claims.get("type", String.class), // Entity type: "USER" or "COMPANY"
                "Token is valid"
            );
        } catch (ExpiredJwtException e) {
            return new TokenValidationResult(
                false,
                null,
                null,
                null,
                "Token is expired"
            );
        } catch (UnsupportedJwtException e) {
            return new TokenValidationResult(
                false,
                null,
                null,
                null,
                "Token is unsupported"
            );
        } catch (MalformedJwtException e) {
            return new TokenValidationResult(
                false,
                null,
                null,
                null,
                "Token is malformed"
            );
        } catch (SecurityException e) {
            return new TokenValidationResult(
                false,
                null,
                null,
                null,
                "Token signature is invalid"
            );
        } catch (IllegalArgumentException e) {
            return new TokenValidationResult(
                false,
                null,
                null,
                null,
                "Token is illegal"
            );
        } catch (Exception e) {
            return new TokenValidationResult(
                false,
                null,
                null,
                null,
                "Token validation failed: " + e.getMessage()
            );
        }
    }

    // Get entity type from token
    public String getEntityTypeFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("type", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    // Inner class for detailed validation result
    public static class TokenValidationResult {
        private final boolean valid;
        private final String email;
        private final Date expiration;
        private final String entityType;
        private final String message;

        public TokenValidationResult(boolean valid, String email, Date expiration, String entityType, String message) {
            this.valid = valid;
            this.email = email;
            this.expiration = expiration;
            this.entityType = entityType;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getEmail() {
            return email;
        }

        public Date getExpiration() {
            return expiration;
        }

        public String getEntityType() {
            return entityType;
        }

        public String getMessage() {
            return message;
        }
    }
}
