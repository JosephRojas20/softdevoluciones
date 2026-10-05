package com.softdv.softdevoluciones.compras.dto.response;

import com.softdv.softdevoluciones.compras.entity.Compra;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CompraResponse(
        Long id,
        LocalDateTime fecha,
        BigDecimal total,
        String estado,
        List<Linea> detalles
) {

    public record Linea(
            Long detalleCompraId,
            Long productoId,
            String nombre,
            String descripcion,
            String imagenUrl,
            int cantidad,
            BigDecimal precioUnitario
    ) {
    }

    public static CompraResponse desde(Compra compra) {
        List<Linea> lineas = compra.getDetalles()
                .stream()
                .map(detalle -> new Linea(
                        detalle.getId(),
                        detalle.getProducto().getId(),
                        detalle.getProducto().getNombre(),
                        detalle.getProducto().getDescripcion(),
                        detalle.getProducto().getImagenUrl(),
                        detalle.getCantidad(),
                        detalle.getPrecioUnitario()
                ))
                .toList();

        return new CompraResponse(
                compra.getId(),
                compra.getFecha(),
                compra.getTotal(),
                compra.getEstado(),
                lineas
        );
    }
}