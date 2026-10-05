package com.softdv.softdevoluciones.devoluciones.entity;

import com.softdv.softdevoluciones.compras.entity.Compra;
import com.softdv.softdevoluciones.usuarios.entity.Usuario;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "solicitudes_devolucion")
public class SolicitudDevolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "compra_id", nullable = false)
    private Compra compra;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoDevolucion estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MotivoDevolucion motivo;

    @Column(length = 1000)
    private String comentario;

    @Column(name = "observacion_operador", length = 1000)
    private String observacionOperador;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;

    @OneToMany(
            mappedBy = "solicitud",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("id ASC")
    private List<DetalleDevolucion> detalles = new ArrayList<>();

    protected SolicitudDevolucion() {
    }

    public SolicitudDevolucion(
            Compra compra,
            Usuario usuario,
            MotivoDevolucion motivo,
            String comentario
    ) {
        this.compra = compra;
        this.usuario = usuario;
        this.motivo = motivo;
        this.comentario = comentario;
        this.fechaSolicitud = LocalDateTime.now();
        this.estado = EstadoDevolucion.SOLICITADA;
        this.importe = BigDecimal.ZERO;
    }

    public void agregarDetalle(DetalleDevolucion detalle) {
        if (detalle.getSolicitud() != this) {
            throw new IllegalArgumentException(
                    "El detalle debe pertenecer a esta solicitud"
            );
        }

        detalles.add(detalle);
        importe = importe.add(detalle.getImporte());
    }

    public void cambiarEstado(
            EstadoDevolucion nuevoEstado,
            String observacion
    ) {
        this.estado = nuevoEstado;

        if (observacion != null && !observacion.isBlank()) {
            this.observacionOperador = observacion.trim();
        }
    }

    public Long getId() {
        return id;
    }

    public Compra getCompra() {
        return compra;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public EstadoDevolucion getEstado() {
        return estado;
    }

    public MotivoDevolucion getMotivo() {
        return motivo;
    }

    public String getComentario() {
        return comentario;
    }

    public String getObservacionOperador() {
        return observacionOperador;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public List<DetalleDevolucion> getDetalles() {
        return List.copyOf(detalles);
    }
}
