package com.mtp.config.center.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {

    private static final String BASE64_SECRET = "YXNkZmdoamtsO2FzZGZnaGprbDthc2RmZ2hqa2w7YXNkZg==";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(Base64.getDecoder().decode(BASE64_SECRET));
    private static final long EXPIRATION = 86400000;

    public static String generateToken(String username, Map<String, Object> claims) {
        Map<String, Object> allClaims = new HashMap<>(claims);
        return Jwts.builder()
                .setClaims(allClaims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(KEY)
                .compact();
    }

    public static String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public static Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}