package com.softdv.softdevoluciones.compras.controller;

import com.softdv.softdevoluciones.compras.dto.response.CompraResponse;
import com.softdv.softdevoluciones.compras.service.CompraService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compras")
public class CompraController {

    private final CompraService compraService;

    public CompraController(CompraService compraService) {
        this.compraService = compraService;
    }

    @GetMapping("/mis-compras")
    public Page<CompraResponse> misCompras(
            Authentication authentication,
            Pageable pageable
    ) {
        return compraService.misCompras(
                authentication.getName(),
                pageable
        );
    }

    @GetMapping("/{id}")
    public CompraResponse detalle(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return compraService.obtenerDetalle(
                id,
                authentication.getName()
        );
    }
}