package com.softdv.softdevoluciones.auth.dto.response;

public record LoginResponse(
        String token,
        String tipo,
        long expiresIn,
        UsuarioResponse usuario
) {
}