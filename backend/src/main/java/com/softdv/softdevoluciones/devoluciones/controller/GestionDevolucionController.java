package com.softdv.softdevoluciones.devoluciones.controller;

import com.softdv.softdevoluciones.devoluciones.dto.request.CambiarEstadoRequest;
import com.softdv.softdevoluciones.devoluciones.dto.response.DevolucionResponse;
import com.softdv.softdevoluciones.devoluciones.entity.EstadoDevolucion;
import com.softdv.softdevoluciones.devoluciones.entity.MotivoDevolucion;
import com.softdv.softdevoluciones.devoluciones.service.DevolucionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/devoluciones")
public class GestionDevolucionController {

    private final DevolucionService devolucionService;

    public GestionDevolucionController(
            DevolucionService devolucionService
    ) {
        this.devolucionService = devolucionService;
    }

    @GetMapping
    public Page<DevolucionResponse> listar(
            @RequestParam(required = false)
            EstadoDevolucion estado,

            @RequestParam(required = false)
            MotivoDevolucion motivo,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate desde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate hasta,

            @PageableDefault(
                    size = 10,
                    sort = "fechaSolicitud",
                    direction = Sort.Direction.DESC
            ) Pageable pageable,

            Authentication authentication
    ) {
        return devolucionService.listarParaGestion(
                estado,
                motivo,
                desde,
                hasta,
                pageable,
                authentication.getName()
        );
    }

    @PatchMapping("/{id}/estado")
    public DevolucionResponse cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoRequest request,
            Authentication authentication
    ) {
        return devolucionService.cambiarEstado(
                id,
                request,
                authentication.getName()
        );
    }
}
