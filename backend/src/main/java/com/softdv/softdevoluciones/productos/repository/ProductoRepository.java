package com.softdv.softdevoluciones.productos.repository;

import com.softdv.softdevoluciones.productos.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
