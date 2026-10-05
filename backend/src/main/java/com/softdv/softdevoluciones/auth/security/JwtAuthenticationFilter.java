package com.softdv.softdevoluciones.auth.security;

import com.softdv.softdevoluciones.auth.service.JwtService;
import com.softdv.softdevoluciones.usuarios.entity.Usuario;
import com.softdv.softdevoluciones.usuarios.repository.UsuarioRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarios;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UsuarioRepository usuarios
    ) {
        this.jwtService = jwtService;
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        if (authorization != null
                && authorization.startsWith("Bearer ")) {

            String token = authorization.substring(7);

            try {
                Long usuarioId = jwtService.obtenerIdUsuario(token);
                Optional<Usuario> encontrado = usuarios.findById(usuarioId);

                if (encontrado.isPresent() && encontrado.get().isActivo()) {
                    Usuario usuario = encontrado.get();

                    var authorities = List.of(
                            new SimpleGrantedAuthority(
                                    "ROLE_" + usuario.getRol().name()
                            )
                    );

                    var authentication =
                            new UsernamePasswordAuthenticationToken(
                                    usuario.getEmail(),
                                    null,
                                    authorities
                            );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }
            } catch (JwtException | IllegalArgumentException ex) {
                // Token inválido o vencido: continúa sin autenticar.
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
