import {
  Component,
  DestroyRef,
  inject,
  OnInit,
  signal
} from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize, TimeoutError } from 'rxjs';

import { AuthService } from '../../../auth/services/auth.service';
import { Pagina } from '../../../compras/models/compra.model';
import { DevolucionService } from '../../services/devolucion.service';
import {
  DevolucionResponse,
  EstadoDevolucion
} from '../../models/devolucion.model';
import { MotivoDevolucion } from '../../models/crear-devolucion.model';

@Component({
  selector: 'app-mis-devoluciones',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './mis-devoluciones.html',
  styleUrl: './mis-devoluciones.css'
})
export class MisDevoluciones implements OnInit {
  private readonly devolucionService = inject(DevolucionService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly pagina = signal<Pagina<DevolucionResponse> | null>(null);
  readonly cargando = signal(false);
  readonly error = signal('');

  readonly detalle = signal<DevolucionResponse | null>(null);
  readonly cargandoDetalle = signal(false);
  readonly errorDetalle = signal('');

  private readonly estados: Record<EstadoDevolucion, string> = {
    SOLICITADA: 'Solicitada',
    EN_REVISION: 'En revisión',
    APROBADA: 'Aprobada',
    RECHAZADA: 'Rechazada',
    COMPLETADA: 'Completada'
  };

  private readonly motivos: Record<MotivoDevolucion, string> = {
    DEFECTUOSO: 'Producto defectuoso',
    PRODUCTO_INCORRECTO: 'Producto incorrecto',
    INCOMPLETO: 'Producto incompleto',
    OTRO: 'Otro motivo'
  };

  ngOnInit(): void {
    this.cargarDevoluciones();
  }

  cargarDevoluciones(numeroPagina = 0): void {
    if (this.cargando()) {
      return;
    }

    this.cargando.set(true);
    this.error.set('');
    this.detalle.set(null);
    this.errorDetalle.set('');

    this.devolucionService.listarMisDevoluciones(numeroPagina)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.cargando.set(false))
      )
      .subscribe({
        next: pagina => this.pagina.set(pagina),
        error: error => {
          this.error.set(this.mensajeError(error));
        }
      });
  }

  actualizar(): void {
    this.cargarDevoluciones(this.pagina()?.number ?? 0);
  }

  verDetalle(id: number): void {
    if (this.cargandoDetalle()) {
      return;
    }

    this.detalle.set(null);
    this.errorDetalle.set('');
    this.cargandoDetalle.set(true);

    this.devolucionService.obtenerDevolucion(id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.cargandoDetalle.set(false))
      )
      .subscribe({
        next: solicitud => this.detalle.set(solicitud),
        error: error => {
          this.errorDetalle.set(this.mensajeError(error));
        }
      });
  }

  cerrarDetalle(): void {
    this.detalle.set(null);
    this.errorDetalle.set('');
  }

  nombreEstado(estado: EstadoDevolucion): string {
    return this.estados[estado] ?? estado;
  }

  nombreMotivo(motivo: MotivoDevolucion): string {
    return this.motivos[motivo] ?? motivo;
  }

  private mensajeError(error: unknown): string {
    if (error instanceof TimeoutError) {
      return 'El servidor tardó demasiado en responder. Intenta nuevamente.';
    }

    if (error instanceof HttpErrorResponse) {
      if (error.status === 401) {
        this.authService.cerrarSesion();
        void this.router.navigate(['/login']);
        return 'Tu sesión ha vencido.';
      }

      if (error.status === 403) {
        return 'No tienes permiso para consultar estas devoluciones.';
      }

      if (error.status === 0) {
        return 'No se pudo conectar con el backend.';
      }

      if (typeof error.error?.detail === 'string') {
        return error.error.detail;
      }

      if (error.status === 404) {
        return 'No se encontró la solicitud de devolución.';
      }
    }

    return 'No se pudieron consultar las devoluciones.';
  }
}