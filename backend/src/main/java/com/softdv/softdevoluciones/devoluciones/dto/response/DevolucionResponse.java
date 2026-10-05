package com.softdv.softdevoluciones.devoluciones.dto.response;

import com.softdv.softdevoluciones.devoluciones.entity.EstadoDevolucion;
import com.softdv.softdevoluciones.devoluciones.entity.MotivoDevolucion;
import com.softdv.softdevoluciones.devoluciones.entity.SolicitudDevolucion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record DevolucionResponse(
        Long id,
        Long compraId,
        Long usuarioId,
        LocalDateTime fechaSolicitud,
        EstadoDevolucion estado,
        MotivoDevolucion motivo,
        String comentario,
        String observacionOperador,
        BigDecimal importe,
        List<Item> items
) {

    public record Item(
            Long detalleCompraId,
            Long productoId,
            String producto,
            int cantidad,
            BigDecimal precioUnitario,
            BigDecimal importe
    ) {
    }

    public static DevolucionResponse desde(
            SolicitudDevolucion solicitud
    ) {
        List<Item> items = solicitud.getDetalles().stream()
                .map(detalle -> new Item(
                        detalle.getDetalleCompra().getId(),
                        detalle.getDetalleCompra().getProducto().getId(),
                        detalle.getDetalleCompra().getProducto().getNombre(),
                        detalle.getCantidad(),
                        detalle.getDetalleCompra().getPrecioUnitario(),
                        detalle.getImporte()
                ))
                .toList();

        return new DevolucionResponse(
                solicitud.getId(),
                solicitud.getCompra().getId(),
                solicitud.getUsuario().getId(),
                solicitud.getFechaSolicitud(),
                solicitud.getEstado(),
                solicitud.getMotivo(),
                solicitud.getComentario(),
                solicitud.getObservacionOperador(),
                solicitud.getImporte(),
                items
        );
    }
}
