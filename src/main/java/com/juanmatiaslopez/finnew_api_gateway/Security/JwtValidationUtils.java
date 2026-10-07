package com.juanmatiaslopez.finnew_api_gateway.Security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class JwtValidationUtils {

    @Value("${jwt.secret}") private String SECRET;

    public void validateToken(final String token) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

        Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    }
}
