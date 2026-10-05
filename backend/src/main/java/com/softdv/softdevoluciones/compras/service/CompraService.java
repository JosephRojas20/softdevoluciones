package com.softdv.softdevoluciones.compras.service;

import com.softdv.softdevoluciones.compras.dto.response.CompraResponse;
import com.softdv.softdevoluciones.compras.repository.CompraRepository;
import com.softdv.softdevoluciones.usuarios.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CompraService {

    private final CompraRepository compraRepository;
    private final UsuarioRepository usuarioRepository;

    public CompraService(
            CompraRepository compraRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.compraRepository = compraRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public Page<CompraResponse> misCompras(String email, Pageable pageable) {
        Long usuarioId = obtenerUsuarioId(email);

        return compraRepository.findByUsuarioId(usuarioId, pageable)
                .map(CompraResponse::desde);
    }

    @Transactional(readOnly = true)
    public CompraResponse obtenerDetalle(Long compraId, String email) {
        Long usuarioId = obtenerUsuarioId(email);

        return compraRepository
                .findByIdAndUsuarioId(compraId, usuarioId)
                .map(CompraResponse::desde)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Compra no encontrada"
                ));
    }

    private Long obtenerUsuarioId(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuario no encontrado"
                ))
                .getId();
    }
}