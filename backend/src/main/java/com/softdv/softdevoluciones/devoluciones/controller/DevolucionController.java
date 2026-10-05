package com.softdv.softdevoluciones.devoluciones.controller;

import com.softdv.softdevoluciones.devoluciones.dto.request.CrearDevolucionRequest;
import com.softdv.softdevoluciones.devoluciones.dto.response.DevolucionResponse;
import com.softdv.softdevoluciones.devoluciones.service.DevolucionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/devoluciones")
public class DevolucionController {

    private final DevolucionService devolucionService;

    public DevolucionController(DevolucionService devolucionService) {
        this.devolucionService = devolucionService;
    }

    @PostMapping
    public ResponseEntity<DevolucionResponse> crear(
            @Valid @RequestBody CrearDevolucionRequest request,
            Authentication authentication
    ) {
        DevolucionResponse respuesta = devolucionService.crear(
                request,
                authentication.getName()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }

    @GetMapping("/mis-devoluciones")
    public Page<DevolucionResponse> misDevoluciones(
            Authentication authentication,
            @PageableDefault(
                    size = 10,
                    sort = "fechaSolicitud",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return devolucionService.misDevoluciones(
                authentication.getName(),
                pageable
        );
    }

    @GetMapping("/{id}")
    public DevolucionResponse obtenerDetalle(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return devolucionService.obtenerDetalle(
                id,
                authentication.getName()
        );
    }
}
