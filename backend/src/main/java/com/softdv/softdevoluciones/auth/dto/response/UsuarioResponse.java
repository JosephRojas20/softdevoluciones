package com.softdv.softdevoluciones.auth.dto.response;

import com.softdv.softdevoluciones.usuarios.entity.Usuario;

public record UsuarioResponse(
        Long id,
        String nombre,
        String email,
        String rol
) {
    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol().name()
        );
    }
}
