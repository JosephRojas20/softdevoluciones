package com.softdv.softdevoluciones.compras.repository;

import com.softdv.softdevoluciones.compras.entity.Compra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompraRepository extends JpaRepository<Compra, Long> {

    Page<Compra> findByUsuarioId(Long usuarioId, Pageable pageable);

    Optional<Compra> findByIdAndUsuarioId(Long id, Long usuarioId);
}