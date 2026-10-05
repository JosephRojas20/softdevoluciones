package com.softdv.softdevoluciones.auth.service;
import com.softdv.softdevoluciones.auth.dto.request.LoginRequest;
import com.softdv.softdevoluciones.auth.dto.request.RegistroRequest;
import com.softdv.softdevoluciones.auth.dto.response.LoginResponse;
import com.softdv.softdevoluciones.auth.dto.response.UsuarioResponse;
import com.softdv.softdevoluciones.usuarios.entity.Rol;
import com.softdv.softdevoluciones.usuarios.entity.Usuario;
import com.softdv.softdevoluciones.usuarios.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarios,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ){
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UsuarioResponse registrar(RegistroRequest request){
        String email = normalizarEmail(request.email());

        if (usuarios.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El correo ya está registrado"
            );
        }

        Usuario usuario = new Usuario(
                request.nombre().trim(),
                email,
                passwordEncoder.encode(request.password()),
                Rol.CLIENTE
        );

        return UsuarioResponse.desde(usuarios.save(usuario));
    }

    @Transactional(readOnly = true)
    public LoginResponse iniciarSesion(LoginRequest request) {
        String email = normalizarEmail(request.email());

        Usuario usuario = usuarios.findByEmail(email)
                .orElseThrow(this::credencialesInvalidas);

        if (!usuario.isActivo()
                || !passwordEncoder.matches(
                request.password(),
                usuario.getPasswordHash()
        )) {
            throw credencialesInvalidas();
        }

        return new LoginResponse(
                jwtService.generarToken(usuario),
                "Bearer",
                jwtService.getDuracionSegundos(),
                UsuarioResponse.desde(usuario)
        );
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPerfil(String email) {
        Usuario usuario = usuarios.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));

        return UsuarioResponse.desde(usuario);
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private ResponseStatusException credencialesInvalidas() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Correo o contraseña incorrectos"
        );
    }
}