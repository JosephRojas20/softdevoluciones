package com.softdv.softdevoluciones.auth.service;

import com.softdv.softdevoluciones.usuarios.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey clave;
    private final long duracionSegundos;

    public JwtService(
            @Value("${app.jwt.secret}") String secretoBase64,
            @Value("${app.jwt.expiration-seconds:3600}") long duracionSegundos
    ) {
        byte[] bytes = Decoders.BASE64.decode(secretoBase64);

        if (bytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT_SECRET debe contener al menos 32 bytes aleatorios codificados en Base64"
            );
        }

        this.clave = Keys.hmacShaKeyFor(bytes);
        this.duracionSegundos = duracionSegundos;
    }

    public String generarToken(Usuario usuario) {
        Instant ahora = Instant.now();

        return Jwts.builder()
                .subject(usuario.getId().toString())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plusSeconds(duracionSegundos)))
                .signWith(clave)
                .compact();
    }

    public Long obtenerIdUsuario(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.valueOf(claims.getSubject());
    }

    public long getDuracionSegundos() {
        return duracionSegundos;
    }
}
