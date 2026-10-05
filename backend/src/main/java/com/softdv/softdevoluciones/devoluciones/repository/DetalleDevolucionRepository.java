package com.softdv.softdevoluciones.devoluciones.repository;

import com.softdv.softdevoluciones.devoluciones.entity.DetalleDevolucion;
import com.softdv.softdevoluciones.devoluciones.entity.EstadoDevolucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface DetalleDevolucionRepository
        extends JpaRepository<DetalleDevolucion, Long> {

    @Query("""
            SELECT COALESCE(SUM(d.cantidad), 0)
            FROM DetalleDevolucion d
            WHERE d.detalleCompra.id = :detalleCompraId
              AND d.solicitud.estado IN :estados
            """)
    Long sumarCantidadesPorEstados(
            @Param("detalleCompraId") Long detalleCompraId,
            @Param("estados") Collection<EstadoDevolucion> estados
    );
}