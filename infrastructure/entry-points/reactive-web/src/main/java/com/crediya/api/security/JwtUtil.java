package com.crediya.api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtUtil {

    private final Key key;

    public JwtUtil(Environment env) {
        String secret = env.getProperty("jwt.secret", "defaultSecretKey");
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public boolean isTokenInvalid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return false;
        } catch (JwtException | IllegalArgumentException ex) {
            return true;
        }
    }

    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }


    @SuppressWarnings("unchecked")
    public String getRole(String token) {
        return getClaims(token).get("role").toString();
    }


}
