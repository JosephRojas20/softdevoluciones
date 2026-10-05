package com.softdv.softdevoluciones.devoluciones.entity;


import com.softdv.softdevoluciones.compras.entity.DetalleCompra;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "detalles_devolucion",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_solicitud_detalle_compra",
                columnNames = {"solicitud_id", "detalle_compra_id"}
        )
)
public class DetalleDevolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudDevolucion solicitud;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "detalle_compra_id", nullable = false)
    private DetalleCompra detalleCompra;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;

    protected DetalleDevolucion() {
    }

    public DetalleDevolucion(
            SolicitudDevolucion solicitud,
            DetalleCompra detalleCompra,
            int cantidad
    ) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero"
            );
        }

        this.solicitud = solicitud;
        this.detalleCompra = detalleCompra;
        this.cantidad = cantidad;

        this.importe = detalleCompra.getPrecioUnitario()
                .multiply(BigDecimal.valueOf(cantidad));
    }

    public Long getId() {
        return id;
    }

    public SolicitudDevolucion getSolicitud() {
        return solicitud;
    }

    public DetalleCompra getDetalleCompra() {
        return detalleCompra;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getImporte() {
        return importe;
    }
}