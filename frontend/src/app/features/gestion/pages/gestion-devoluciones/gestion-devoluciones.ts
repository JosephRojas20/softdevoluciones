import {
  Component,
  DestroyRef,
  inject,
  OnInit,
  signal
} from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
  FormControl,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize, TimeoutError } from 'rxjs';

import { AuthService } from '../../../auth/services/auth.service';

import { Pagina } from '../../../compras/models/compra.model';

import {
  DevolucionService, FiltrosDevoluciones
} from '../../../devoluciones/services/devolucion.service';

import {
  DevolucionResponse,
  EstadoDevolucion
} from '../../../devoluciones/models/devolucion.model';

import {
  MotivoDevolucion
} from '../../../devoluciones/models/crear-devolucion.model';

@Component({
  selector: 'app-gestion-devoluciones',
  standalone: true,
  imports: [
    CurrencyPipe,
    DatePipe,
    ReactiveFormsModule
  ],
  templateUrl: './gestion-devoluciones.html',
  styleUrls: [
  '../../../devoluciones/pages/mis-devoluciones/mis-devoluciones.css',
  './gestion-devoluciones.css'
  ]
})
export class GestionDevoluciones implements OnInit {
  private readonly service = inject(DevolucionService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly pagina = signal<Pagina<DevolucionResponse> | null>(null);
  readonly seleccionada = signal<DevolucionResponse | null>(null);

  readonly cargando = signal(false);
  readonly cargandoDetalle = signal(false);
  readonly guardando = signal(false);

  readonly error = signal('');
  readonly errorDetalle = signal('');
  readonly exito = signal('');

  readonly estadoFiltro = new FormControl<EstadoDevolucion | ''>(
    '',
    { nonNullable: true }
  );

  readonly motivoFiltro = new FormControl<MotivoDevolucion | ''>(
    '',
    { nonNullable: true }
  );

  readonly desdeFiltro = new FormControl('', {
    nonNullable: true
  });

  readonly hastaFiltro = new FormControl('', {
    nonNullable: true
  });

  readonly observacion = new FormControl('', {
    nonNullable: true,
    validators: [Validators.maxLength(1000)]
  });

  readonly estados: {
    valor: EstadoDevolucion;
    nombre: string;
  }[] = [
    { valor: 'SOLICITADA', nombre: 'Solicitada' },
    { valor: 'EN_REVISION', nombre: 'En revisión' },
    { valor: 'APROBADA', nombre: 'Aprobada' },
    { valor: 'RECHAZADA', nombre: 'Rechazada' },
    { valor: 'COMPLETADA', nombre: 'Completada' }
  ];

  readonly motivos: {
    valor: MotivoDevolucion;
    nombre: string;
  }[] = [
    { valor: 'DEFECTUOSO', nombre: 'Producto defectuoso' },
    { valor: 'PRODUCTO_INCORRECTO', nombre: 'Producto incorrecto' },
    { valor: 'INCOMPLETO', nombre: 'Producto incompleto' },
    { valor: 'OTRO', nombre: 'Otro motivo' }
  ];

  private filtrosAplicados: FiltrosDevoluciones = {};

  get ocupada(): boolean {
    return this.cargando() ||
      this.cargandoDetalle() ||
      this.guardando();
  }

  ngOnInit(): void {
    this.cargar();
  }

  buscar(): void {
    if (this.ocupada) {
      return;
    }

    const desde = this.desdeFiltro.value;
    const hasta = this.hastaFiltro.value;

    if (desde && hasta && desde > hasta) {
      this.error.set(
        'La fecha desde no puede ser posterior a la fecha hasta.'
      );
      return;
    }

    this.filtrosAplicados = {
      estado: this.estadoFiltro.value,
      motivo: this.motivoFiltro.value,
      desde,
      hasta
    };

    this.cerrarDetalle();
    this.cargar(0);
  }

  limpiarFiltros(): void {
    if (this.ocupada) {
      return;
    }

    this.estadoFiltro.setValue('');
    this.motivoFiltro.setValue('');
    this.desdeFiltro.setValue('');
    this.hastaFiltro.setValue('');

    this.buscar();
  }

  actualizar(): void {
    if (!this.ocupada) {
      this.cargar(this.pagina()?.number ?? 0);
    }
  }

  cargar(numeroPagina = 0): void {
    if (this.ocupada) {
      return;
    }

    this.cargando.set(true);
    this.error.set('');

    this.service.listarParaGestion(
      this.filtrosAplicados,
      numeroPagina
    )
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.cargando.set(false))
      )
      .subscribe({
        next: pagina => {
          // Si una acción vació la última página, volver a la anterior.
          if (
            numeroPagina > 0 &&
            pagina.content.length === 0
          ) {
            this.cargando.set(false);
            this.cargar(
              Math.max(0, Math.min(
                numeroPagina - 1,
                pagina.totalPages - 1
              ))
            );
            return;
          }

          this.pagina.set(pagina);
        },
        error: error => {
          this.error.set(this.mensajeError(error));
        }
      });
  }

  revisar(id: number): void {
    if (this.ocupada) {
      return;
    }

    this.seleccionada.set(null);
    this.errorDetalle.set('');
    this.exito.set('');
    this.observacion.reset('');
    this.cargandoDetalle.set(true);

    this.service.obtenerDevolucion(id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.cargandoDetalle.set(false))
      )
      .subscribe({
        next: solicitud => {
          this.seleccionada.set(solicitud);
          this.observacion.setValue(
            solicitud.observacionOperador ?? ''
          );
        },
        error: error => {
          this.errorDetalle.set(this.mensajeError(error));
        }
      });
  }

  cerrarDetalle(): void {
    if (this.guardando() || this.cargandoDetalle()) {
      return;
    }

    this.seleccionada.set(null);
    this.errorDetalle.set('');
    this.exito.set('');
    this.observacion.reset('');
  }

  accionesPermitidas(
    estado: EstadoDevolucion
  ): EstadoDevolucion[] {
    switch (estado) {
      case 'SOLICITADA':
        return ['EN_REVISION'];

      case 'EN_REVISION':
        return ['APROBADA', 'RECHAZADA'];

      case 'APROBADA':
        return ['COMPLETADA'];

      default:
        return [];
    }
  }

  cambiarEstado(nuevoEstado: EstadoDevolucion): void {
    const solicitud = this.seleccionada();

    if (!solicitud || this.ocupada) {
      return;
    }

    this.errorDetalle.set('');
    this.exito.set('');

    if (
      !this.accionesPermitidas(solicitud.estado)
        .includes(nuevoEstado)
    ) {
      this.errorDetalle.set(
        'Esa acción no corresponde al estado actual.'
      );
      return;
    }

    if (this.observacion.invalid) {
      this.errorDetalle.set(
        'La observación no puede superar los 1000 caracteres.'
      );
      return;
    }

    const observacion = this.observacion.value.trim();

    if (nuevoEstado === 'RECHAZADA' && !observacion) {
      this.errorDetalle.set(
        'Debes escribir una observación para rechazar la solicitud.'
      );
      return;
    }

    this.guardando.set(true);

    this.service.cambiarEstado(solicitud.id, {
      estado: nuevoEstado,
      observacion: observacion || null
    })
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.guardando.set(false))
      )
      .subscribe({
        next: respuesta => {
          this.seleccionada.set(respuesta);
          this.observacion.setValue(
            respuesta.observacionOperador ?? ''
          );

          this.exito.set(
            `Solicitud #D-${respuesta.id}: estado actualizado a ` +
            this.nombreEstado(respuesta.estado) + '.'
          );

          // Permite actualizar la tabla tras finalizar la operación.
          this.guardando.set(false);
          this.cargar(this.pagina()?.number ?? 0);
        },
        error: error => {
          this.errorDetalle.set(this.mensajeError(error));
        }
      });
  }

  nombreEstado(estado: EstadoDevolucion): string {
    return this.estados.find(
      opcion => opcion.valor === estado
    )?.nombre ?? estado;
  }

  nombreMotivo(motivo: MotivoDevolucion): string {
    return this.motivos.find(
      opcion => opcion.valor === motivo
    )?.nombre ?? motivo;
  }

  nombreAccion(estado: EstadoDevolucion): string {
    switch (estado) {
      case 'EN_REVISION':
        return 'Iniciar revisión';
      case 'APROBADA':
        return 'Aprobar devolución';
      case 'RECHAZADA':
        return 'Rechazar devolución';
      case 'COMPLETADA':
        return 'Marcar como completada';
      default:
        return this.nombreEstado(estado);
    }
  }

  private mensajeError(error: unknown): string {
    if (error instanceof TimeoutError) {
      return 'El servidor tardó demasiado. Actualiza la información antes de repetir una acción.';
    }

    if (error instanceof HttpErrorResponse) {
      if (error.status === 401) {
        this.authService.cerrarSesion();
        void this.router.navigate(['/login']);
        return 'Tu sesión ha vencido.';
      }

      if (error.status === 403) {
        return 'Esta operación requiere el rol OPERADOR o ADMIN.';
      }

      if (error.status === 0) {
        return 'No se pudo conectar con el backend.';
      }

      if (typeof error.error?.detail === 'string') {
        return error.error.detail;
      }

      if (error.status === 409) {
        return 'La solicitud presenta un conflicto. Vuelve a consultarla para conocer su estado actual.';
      }
    }

    return 'No se pudo completar la operación.';
  }
}