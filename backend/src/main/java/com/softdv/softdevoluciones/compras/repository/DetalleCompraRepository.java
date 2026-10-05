package com.softdv.softdevoluciones.compras.repository;

import com.softdv.softdevoluciones.compras.entity.DetalleCompra;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DetalleCompraRepository extends JpaRepository<DetalleCompra, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM DetalleCompra d WHERE d.id = :id")
    Optional<DetalleCompra> bloquear(@Param("id") Long id);
}