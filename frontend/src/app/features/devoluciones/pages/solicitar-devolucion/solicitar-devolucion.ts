import {
  Component,
  DestroyRef,
  inject,
  OnInit,
  signal
} from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize, TimeoutError } from 'rxjs';

import { AuthService } from '../../../auth/services/auth.service';
import { CompraService } from '../../../compras/services/compra.service';
import {
  CompraResponse,
  LineaCompra
} from '../../../compras/models/compra.model';

import { DevolucionService } from '../../services/devolucion.service';
import {
  CrearDevolucionRequest,
  MotivoDevolucion
} from '../../models/crear-devolucion.model';

@Component({
  selector: 'app-solicitar-devolucion',
  standalone: true,
  imports: [FormsModule, RouterLink, CurrencyPipe],
  templateUrl: './solicitar-devolucion.html',
  styleUrl: './solicitar-devolucion.css'
})
export class SolicitarDevolucion implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  private readonly compraService = inject(CompraService);
  private readonly devolucionService = inject(DevolucionService);
  private readonly authService = inject(AuthService);

  readonly compra = signal<CompraResponse | null>(null);
  readonly producto = signal<LineaCompra | null>(null);

  readonly cargando = signal(true);
  readonly enviando = signal(false);
  readonly errorCarga = signal('');
  readonly errorEnvio = signal('');
  readonly solicitudId = signal<number | null>(null);
  readonly imagenFallida = signal(false);

  cantidad: number | null = 1;
  motivo: MotivoDevolucion | '' = '';
  comentario = '';

  readonly motivos: {
    valor: MotivoDevolucion;
    etiqueta: string;
  }[] = [
    { valor: 'DEFECTUOSO', etiqueta: 'Producto defectuoso' },
    { valor: 'PRODUCTO_INCORRECTO', etiqueta: 'Producto incorrecto' },
    { valor: 'INCOMPLETO', etiqueta: 'Producto incompleto' },
    { valor: 'OTRO', etiqueta: 'Otro motivo' }
  ];

  ngOnInit(): void {
    const compraId = this.leerId(
      this.route.snapshot.paramMap.get('compraId')
    );

    const detalleCompraId = this.leerId(
      this.route.snapshot.paramMap.get('detalleCompraId')
    );

    if (compraId === null || detalleCompraId === null) {
      this.errorCarga.set('La dirección de la solicitud no es válida.');
      this.cargando.set(false);
      return;
    }

    this.compraService.obtenerCompra(compraId)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.cargando.set(false))
      )
      .subscribe({
        next: compra => {
          const producto = compra.detalles.find(
            detalle => detalle.detalleCompraId === detalleCompraId
          );

          if (!producto) {
            this.errorCarga.set(
              'El producto seleccionado no pertenece a esta compra.'
            );
            return;
          }

          this.compra.set(compra);
          this.producto.set(producto);
        },
        error: error => {
          this.errorCarga.set(
            this.obtenerMensajeError(
              error,
              'No se pudo cargar la compra.'
            )
          );
        }
      });
  }

  get importeEstimado(): number {
    const producto = this.producto();
    const cantidad = this.cantidad;

    if (
      !producto ||
      cantidad === null ||
      !Number.isInteger(cantidad) ||
      cantidad < 1 ||
      cantidad > producto.cantidad
    ) {
      return 0;
    }

    return producto.precioUnitario * cantidad;
  }

  enviarSolicitud(): void {
    if (this.enviando() || this.solicitudId() !== null) {
      return;
    }

    this.errorEnvio.set('');

    const compra = this.compra();
    const producto = this.producto();
    const cantidad = this.cantidad;

    if (!compra || !producto) {
      this.errorEnvio.set('Primero debe cargarse el producto.');
      return;
    }

    if (
      cantidad === null ||
      !Number.isInteger(cantidad) ||
      cantidad < 1 ||
      cantidad > producto.cantidad
    ) {
      this.errorEnvio.set(
        `Ingresa una cantidad entera entre 1 y ${producto.cantidad}.`
      );
      return;
    }

    const motivo = this.motivos.find(
      opcion => opcion.valor === this.motivo
    )?.valor;

    if (!motivo) {
      this.errorEnvio.set('Selecciona un motivo de devolución.');
      return;
    }

    const comentario = this.comentario.trim();

    if (comentario.length > 1000) {
      this.errorEnvio.set(
        'El comentario no puede superar los 1000 caracteres.'
      );
      return;
    }

    const solicitud: CrearDevolucionRequest = {
      compraId: compra.id,
      motivo,
      comentario: comentario || null,
      items: [
        {
          detalleCompraId: producto.detalleCompraId,
          cantidad
        }
      ]
    };

    this.enviando.set(true);

    this.devolucionService.crearSolicitud(solicitud)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.enviando.set(false))
      )
      .subscribe({
        next: respuesta => {
          this.solicitudId.set(respuesta.id);
        },
        error: error => {
          this.errorEnvio.set(
            this.obtenerMensajeError(
              error,
              'No se pudo registrar la solicitud.'
            )
          );
        }
      });
  }

  private leerId(valor: string | null): number | null {
    if (!valor || !/^[1-9]\d*$/.test(valor)) {
      return null;
    }

    const id = Number(valor);
    return Number.isSafeInteger(id) ? id : null;
  }

  private obtenerMensajeError(
    error: unknown,
    mensajePredeterminado: string
  ): string {
    if (error instanceof TimeoutError) {
      return 'La operación tardó demasiado. Revisa Mis devoluciones antes de volver a enviar la solicitud.';
    }

    if (error instanceof HttpErrorResponse) {
      if (error.status === 401) {
        this.authService.cerrarSesion();
        void this.router.navigate(['/login']);
        return 'Tu sesión ha vencido. Inicia sesión nuevamente.';
      }

      if (error.status === 403) {
        return 'No tienes permiso para realizar esta operación.';
      }

      if (error.status === 0) {
        return 'No se pudo conectar con el servidor.';
      }

      if (
        typeof error.error?.detail === 'string' &&
        error.error.detail.trim()
      ) {
        return error.error.detail;
      }

      if (error.status === 404) {
        return 'No se encontró la compra o el producto solicitado.';
      }

      if (error.status === 409) {
        return 'La solicitud presenta un conflicto. Revisa tus devoluciones y la cantidad seleccionada.';
      }
    }

    return mensajePredeterminado;
  }
}