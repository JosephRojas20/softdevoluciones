package com.softdv.softdevoluciones.devoluciones.dto.request;

import com.softdv.softdevoluciones.devoluciones.entity.EstadoDevolucion;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CambiarEstadoRequest(

        @NotNull(message = "El estado es obligatorio")
        EstadoDevolucion estado,

        @Size(
                max = 1000,
                message = "La observación admite hasta 1000 caracteres"
        )
        String observacion

) {
}
