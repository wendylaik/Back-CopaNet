package com.copanet.api.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Crear un token con UsuarioId
    public String generarToken(Integer usuarioId, String email, Integer rolId) {
        return Jwts.builder()
                .setSubject(email)
                .claim("usuarioId", usuarioId)
                .claim("rolId", rolId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 día
                .signWith(key)
                .compact();
    }


    // Extraer UsuarioId del token
    public Integer obtenerUsuarioId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("usuarioId", Integer.class);
    }
}
