package com.softdv.softdevoluciones.auth.controller;

import com.softdv.softdevoluciones.auth.dto.request.LoginRequest;
import com.softdv.softdevoluciones.auth.dto.request.RegistroRequest;
import com.softdv.softdevoluciones.auth.dto.response.LoginResponse;
import com.softdv.softdevoluciones.auth.dto.response.UsuarioResponse;
import com.softdv.softdevoluciones.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrar(
            @Valid @RequestBody RegistroRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.registrar(request));
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.iniciarSesion(request);
    }

    @GetMapping("/me")
    public UsuarioResponse me(Authentication authentication) {
        return authService.obtenerPerfil(authentication.getName());
    }
}
