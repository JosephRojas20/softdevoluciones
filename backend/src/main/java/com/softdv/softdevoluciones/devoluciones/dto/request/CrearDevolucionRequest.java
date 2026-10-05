package com.softdv.softdevoluciones.devoluciones.dto.request;

import com.softdv.softdevoluciones.devoluciones.entity.MotivoDevolucion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record CrearDevolucionRequest(

        @NotNull(message = "La compra es obligatoria")
        @Positive(message = "El identificador de compra debe ser positivo")
        Long compraId,

        @NotNull(message = "El motivo es obligatorio")
        MotivoDevolucion motivo,

        @Size(
                max = 1000,
                message = "El comentario admite hasta 1000 caracteres"
        )
        String comentario,

        @NotEmpty(message = "Selecciona al menos un producto")
        List<@NotNull @Valid Item> items

) {

    public record Item(

            @NotNull(message = "El detalle de compra es obligatorio")
            @Positive(message = "El identificador debe ser positivo")
            Long detalleCompraId,

            @NotNull(message = "La cantidad es obligatoria")
            @Positive(message = "La cantidad debe ser mayor que cero")
            Integer cantidad

    ) {
    }
}