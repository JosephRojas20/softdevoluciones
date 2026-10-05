package com.softdv.softdevoluciones.devoluciones.repository;


import com.softdv.softdevoluciones.devoluciones.entity.SolicitudDevolucion;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SolicitudDevolucionRepository
        extends JpaRepository<SolicitudDevolucion, Long>,
        JpaSpecificationExecutor<SolicitudDevolucion> {

    Page<SolicitudDevolucion> findByUsuarioId(
            Long usuarioId,
            Pageable pageable
    );

    Optional<SolicitudDevolucion> findByIdAndUsuarioId(
            Long id,
            Long usuarioId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT s
            FROM SolicitudDevolucion s
            WHERE s.id = :id
            """)
    Optional<SolicitudDevolucion> bloquearPorId(
            @Param("id") Long id
    );
}