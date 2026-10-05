import { Component, inject, OnInit, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { finalize, TimeoutError } from 'rxjs';

import { CompraService } from '../../services/compra.service';
import {
  CompraResponse,
  Pagina
} from '../../models/compra.model';
import { AuthService } from '../../../auth/services/auth.service';

@Component({
  selector: 'app-mis-compras',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './mis-compras.html',
  styleUrl: './mis-compras.css'
})
export class MisCompras implements OnInit {
  private readonly compraService = inject(CompraService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly pagina = signal<Pagina<CompraResponse> | null>(null);
  readonly cargando = signal(false);
  readonly error = signal('');

  ngOnInit(): void {
    this.cargarCompras();
  }

  cargarCompras(numero = 0): void {
    if (this.cargando()) {
      return;
    }

    this.cargando.set(true);
    this.error.set('');

    this.compraService
      .listarMisCompras(numero)
      .pipe(
        finalize(() => this.cargando.set(false))
      )
      .subscribe({
        next: datos => this.pagina.set(datos),
        error: (fallo: unknown) => {
          if (fallo instanceof TimeoutError) {
            this.error.set('El servidor tardó demasiado en responder.');
            return;
          }

          if (fallo instanceof HttpErrorResponse) {
            if (fallo.status === 401 || fallo.status === 403) {
              this.authService.cerrarSesion();
              void this.router.navigateByUrl('/login');
              return;
            }

            this.error.set(
              fallo.status === 0
                ? 'No se pudo conectar con el servidor.'
                : fallo.error?.detail ?? 'No se pudieron cargar las compras.'
            );

            return;
          }

          this.error.set('No se pudieron cargar las compras.');
        }
      });
  }
}