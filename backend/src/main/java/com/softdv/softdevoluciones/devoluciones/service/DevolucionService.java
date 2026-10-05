package com.softdv.softdevoluciones.devoluciones.service;

import com.softdv.softdevoluciones.compras.entity.Compra;
import com.softdv.softdevoluciones.compras.entity.DetalleCompra;
import com.softdv.softdevoluciones.compras.repository.CompraRepository;
import com.softdv.softdevoluciones.compras.repository.DetalleCompraRepository;
import com.softdv.softdevoluciones.devoluciones.dto.request.CambiarEstadoRequest;
import com.softdv.softdevoluciones.devoluciones.dto.request.CrearDevolucionRequest;
import com.softdv.softdevoluciones.devoluciones.dto.response.DevolucionResponse;
import com.softdv.softdevoluciones.devoluciones.entity.DetalleDevolucion;
import com.softdv.softdevoluciones.devoluciones.entity.EstadoDevolucion;
import com.softdv.softdevoluciones.devoluciones.entity.MotivoDevolucion;
import com.softdv.softdevoluciones.devoluciones.entity.SolicitudDevolucion;
import com.softdv.softdevoluciones.devoluciones.repository.DetalleDevolucionRepository;
import com.softdv.softdevoluciones.devoluciones.repository.SolicitudDevolucionRepository;
import com.softdv.softdevoluciones.usuarios.entity.Rol;
import com.softdv.softdevoluciones.usuarios.entity.Usuario;
import com.softdv.softdevoluciones.usuarios.repository.UsuarioRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DevolucionService {

    private static final List<EstadoDevolucion> ESTADOS_QUE_CONSUMEN_UNIDADES =
            List.of(
                    EstadoDevolucion.APROBADA,
                    EstadoDevolucion.COMPLETADA
            );

    private final UsuarioRepository usuarioRepository;
    private final CompraRepository compraRepository;
    private final DetalleCompraRepository detalleCompraRepository;
    private final SolicitudDevolucionRepository solicitudRepository;
    private final DetalleDevolucionRepository detalleDevolucionRepository;

    public DevolucionService(
            UsuarioRepository usuarioRepository,
            CompraRepository compraRepository,
            DetalleCompraRepository detalleCompraRepository,
            SolicitudDevolucionRepository solicitudRepository,
            DetalleDevolucionRepository detalleDevolucionRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.compraRepository = compraRepository;
        this.detalleCompraRepository = detalleCompraRepository;
        this.solicitudRepository = solicitudRepository;
        this.detalleDevolucionRepository = detalleDevolucionRepository;
    }

    @Transactional
    public DevolucionResponse crear(
            CrearDevolucionRequest request,
            String email
    ) {
        Usuario usuario = obtenerUsuario(email);

        Compra compra = compraRepository
                .findByIdAndUsuarioId(request.compraId(), usuario.getId())
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND,
                        "Compra no encontrada"
                ));

        SolicitudDevolucion solicitud = new SolicitudDevolucion(
                compra,
                usuario,
                request.motivo(),
                request.comentario()
        );

        Set<Long> detallesIncluidos = new HashSet<>();

        // Bloquear siempre en el mismo orden reduce el riesgo de interbloqueos.
        List<CrearDevolucionRequest.Item> itemsOrdenados =
                request.items().stream()
                        .sorted(Comparator.comparing(
                                CrearDevolucionRequest.Item::detalleCompraId
                        ))
                        .toList();

        for (CrearDevolucionRequest.Item item : itemsOrdenados) {

            if (!detallesIncluidos.add(item.detalleCompraId())) {
                throw error(
                        HttpStatus.BAD_REQUEST,
                        "No puedes repetir el mismo detalle de compra"
                );
            }

            DetalleCompra detalleCompra = detalleCompraRepository
                    .bloquear(item.detalleCompraId())
                    .orElseThrow(() -> error(
                            HttpStatus.BAD_REQUEST,
                            "El detalle de compra no existe"
                    ));

            if (!detalleCompra.getCompra().getId().equals(compra.getId())) {
                throw error(
                        HttpStatus.BAD_REQUEST,
                        "El producto no pertenece a la compra seleccionada"
                );
            }

            validarCantidadDisponible(detalleCompra, item.cantidad());

            DetalleDevolucion detalle = new DetalleDevolucion(
                    solicitud,
                    detalleCompra,
                    item.cantidad()
            );

            // La entidad suma el importe calculado con el precio de la compra.
            solicitud.agregarDetalle(detalle);
        }

        SolicitudDevolucion guardada =
                solicitudRepository.saveAndFlush(solicitud);

        return DevolucionResponse.desde(guardada);
    }

    @Transactional(readOnly = true)
    public Page<DevolucionResponse> misDevoluciones(
            String email,
            Pageable pageable
    ) {
        Usuario usuario = obtenerUsuario(email);

        return solicitudRepository
                .findByUsuarioId(usuario.getId(), pageable)
                .map(DevolucionResponse::desde);
    }

    @Transactional(readOnly = true)
    public DevolucionResponse obtenerDetalle(
            Long solicitudId,
            String email
    ) {
        Usuario usuario = obtenerUsuario(email);

        SolicitudDevolucion solicitud;

        if (puedeGestionar(usuario)) {
            solicitud = solicitudRepository.findById(solicitudId)
                    .orElseThrow(() -> error(
                            HttpStatus.NOT_FOUND,
                            "Solicitud no encontrada"
                    ));
        } else {
            solicitud = solicitudRepository
                    .findByIdAndUsuarioId(solicitudId, usuario.getId())
                    .orElseThrow(() -> error(
                            HttpStatus.NOT_FOUND,
                            "Solicitud no encontrada"
                    ));
        }

        return DevolucionResponse.desde(solicitud);
    }

    @Transactional(readOnly = true)
    public Page<DevolucionResponse> listarParaGestion(
            EstadoDevolucion estado,
            MotivoDevolucion motivo,
            LocalDate desde,
            LocalDate hasta,
            Pageable pageable,
            String email
    ) {
        exigirPermisoGestion(obtenerUsuario(email));

        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "La fecha inicial no puede ser posterior a la fecha final"
            );
        }

        Specification<SolicitudDevolucion> filtros =
                (root, query, builder) -> {

                    List<Predicate> condiciones = new ArrayList<>();

                    if (estado != null) {
                        condiciones.add(
                                builder.equal(root.get("estado"), estado)
                        );
                    }

                    if (motivo != null) {
                        condiciones.add(
                                builder.equal(root.get("motivo"), motivo)
                        );
                    }

                    if (desde != null) {
                        condiciones.add(
                                builder.greaterThanOrEqualTo(
                                        root.<LocalDateTime>get("fechaSolicitud"),
                                        desde.atStartOfDay()
                                )
                        );
                    }

                    if (hasta != null) {
                        // Incluye todas las horas del último día.
                        condiciones.add(
                                builder.lessThan(
                                        root.<LocalDateTime>get("fechaSolicitud"),
                                        hasta.plusDays(1).atStartOfDay()
                                )
                        );
                    }

                    return builder.and(
                            condiciones.toArray(new Predicate[0])
                    );
                };

        return solicitudRepository.findAll(filtros, pageable)
                .map(DevolucionResponse::desde);
    }

    @Transactional
    public DevolucionResponse cambiarEstado(
            Long solicitudId,
            CambiarEstadoRequest request,
            String email
    ) {
        exigirPermisoGestion(obtenerUsuario(email));

        SolicitudDevolucion solicitud = solicitudRepository
                .bloquearPorId(solicitudId)
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND,
                        "Solicitud no encontrada"
                ));

        EstadoDevolucion actual = solicitud.getEstado();
        EstadoDevolucion nuevo = request.estado();

        validarTransicion(actual, nuevo);

        if (nuevo == EstadoDevolucion.RECHAZADA
                && (request.observacion() == null
                || request.observacion().isBlank())) {

            throw error(
                    HttpStatus.BAD_REQUEST,
                    "Debes indicar una observación para rechazar la solicitud"
            );
        }

        if (nuevo == EstadoDevolucion.APROBADA) {
            validarDisponibilidadParaAprobar(solicitud);
        }

        solicitud.cambiarEstado(nuevo, request.observacion());

        // JPA guarda el cambio al finalizar la transacción.
        return DevolucionResponse.desde(solicitud);
    }

    private void validarDisponibilidadParaAprobar(
            SolicitudDevolucion solicitud
    ) {
        List<DetalleDevolucion> detallesOrdenados =
                solicitud.getDetalles().stream()
                        .sorted(Comparator.comparing(
                                detalle -> detalle.getDetalleCompra().getId()
                        ))
                        .toList();

        for (DetalleDevolucion detalle : detallesOrdenados) {
            Long detalleCompraId = detalle.getDetalleCompra().getId();

            DetalleCompra detalleCompra = detalleCompraRepository
                    .bloquear(detalleCompraId)
                    .orElseThrow(() -> error(
                            HttpStatus.CONFLICT,
                            "El detalle de compra ya no está disponible"
                    ));

            validarCantidadDisponible(
                    detalleCompra,
                    detalle.getCantidad()
            );
        }
    }

    private void validarCantidadDisponible(
            DetalleCompra detalleCompra,
            int cantidadSolicitada
    ) {
        if (cantidadSolicitada <= 0) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "La cantidad debe ser mayor que cero"
            );
        }

        long unidadesComprometidas =
                detalleDevolucionRepository.sumarCantidadesPorEstados(
                        detalleCompra.getId(),
                        ESTADOS_QUE_CONSUMEN_UNIDADES
                );

        long disponibles =
                detalleCompra.getCantidad() - unidadesComprometidas;

        if (cantidadSolicitada > disponibles) {
            throw error(
                    HttpStatus.CONFLICT,
                    "Cantidad no disponible para "
                            + detalleCompra.getProducto().getNombre()
                            + ". Unidades disponibles: "
                            + disponibles
            );
        }
    }

    private void validarTransicion(
            EstadoDevolucion actual,
            EstadoDevolucion nuevo
    ) {
        boolean permitida = switch (actual) {
            case SOLICITADA ->
                    nuevo == EstadoDevolucion.EN_REVISION;

            case EN_REVISION ->
                    nuevo == EstadoDevolucion.APROBADA
                            || nuevo == EstadoDevolucion.RECHAZADA;

            case APROBADA ->
                    nuevo == EstadoDevolucion.COMPLETADA;

            case RECHAZADA, COMPLETADA -> false;
        };

        if (!permitida) {
            throw error(
                    HttpStatus.CONFLICT,
                    "No se permite cambiar de " + actual + " a " + nuevo
            );
        }
    }

    private Usuario obtenerUsuario(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> error(
                        HttpStatus.UNAUTHORIZED,
                        "Usuario no encontrado"
                ));

        if (!usuario.isActivo()) {
            throw error(
                    HttpStatus.UNAUTHORIZED,
                    "El usuario está desactivado"
            );
        }

        return usuario;
    }

    private boolean puedeGestionar(Usuario usuario) {
        return usuario.getRol() == Rol.OPERADOR
                || usuario.getRol() == Rol.ADMIN;
    }

    private void exigirPermisoGestion(Usuario usuario) {
        if (!puedeGestionar(usuario)) {
            throw error(
                    HttpStatus.FORBIDDEN,
                    "No tienes permiso para gestionar devoluciones"
            );
        }
    }

    private ResponseStatusException error(
            HttpStatus status,
            String mensaje
    ) {
        return new ResponseStatusException(status, mensaje);
    }
}
