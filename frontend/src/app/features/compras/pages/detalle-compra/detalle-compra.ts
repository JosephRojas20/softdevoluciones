import {
  Component,
  DestroyRef,
  inject,
  OnInit,
  signal
} from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, of, switchMap, TimeoutError } from 'rxjs';

import { CompraService } from '../../services/compra.service';
import { CompraResponse } from '../../models/compra.model';
import { AuthService } from '../../../auth/services/auth.service';

@Component({
  selector: 'app-detalle-compra',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './detalle-compra.html',
  styleUrl: './detalle-compra.css'
})
export class DetalleCompra implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  private readonly compraService = inject(CompraService);
  private readonly authService = inject(AuthService);

  readonly compra = signal<CompraResponse | null>(null);
  readonly cargando = signal(true);
  readonly error = signal('');

  readonly imagenesFallidas = signal<ReadonlySet<number>>(new Set());

  ngOnInit(): void {
    this.route.paramMap
      .pipe(
        switchMap(parametros => {
          this.compra.set(null);
          this.error.set('');
          this.cargando.set(true);
          this.imagenesFallidas.set(new Set());

          const valor = parametros.get('id');

          if (!valor || !/^[1-9]\d*$/.test(valor)) {
            this.error.set('El identificador de la compra no es válido.');
            return of(null);
          }

          const id = Number(valor);

          if (!Number.isSafeInteger(id)) {
            this.error.set('El identificador de la compra no es válido.');
            return of(null);
          }

          return this.compraService.obtenerCompra(id).pipe(
            catchError((fallo: unknown) => {
              this.procesarError(fallo);
              return of(null);
            })
          );
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(compra => {
        this.compra.set(compra);
        this.cargando.set(false);
      });
  }

  marcarImagenFallida(detalleCompraId: number): void {
    this.imagenesFallidas.update(actuales => {
      const nuevas = new Set(actuales);
      nuevas.add(detalleCompraId);
      return nuevas;
    });
  }

  private procesarError(fallo: unknown): void {
    if (fallo instanceof TimeoutError) {
      this.error.set('El servidor tardó demasiado en responder.');
      return;
    }

    if (fallo instanceof HttpErrorResponse) {
      switch (fallo.status) {
        case 401:
        case 403:
          this.authService.cerrarSesion();
          void this.router.navigateByUrl('/login');
          break;

        case 404:
          this.error.set(
            'La compra no existe o no pertenece a tu cuenta.'
          );
          break;

        case 0:
          this.error.set('No se pudo conectar con el servidor.');
          break;

        default:
          this.error.set(
            fallo.error?.detail ??
            'No se pudo consultar el detalle de la compra.'
          );
      }

      return;
    }

    this.error.set('No se pudo consultar el detalle de la compra.');
  }
}