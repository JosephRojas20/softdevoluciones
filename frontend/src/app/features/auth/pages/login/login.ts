import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { finalize, TimeoutError } from 'rxjs';

import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  email = '';
  password = '';

  readonly cargando = signal(false);
  readonly error = signal('');
  readonly mostrarPassword = signal(false);

  ingresar(): void {
    if (this.cargando()) {
      return;
    }

    this.error.set('');

    if (!this.email.trim() || !this.password) {
      this.error.set('Ingresa tu correo y contraseña.');
      return;
    }

    this.cargando.set(true);

    this.authService
      .iniciarSesion({
        email: this.email.trim(),
        password: this.password
      })
      .pipe(
        finalize(() => this.cargando.set(false))
      )
      .subscribe({
        next: respuesta => {
          this.password = '';
          this.mostrarPassword.set(false);

          const destino = respuesta.usuario.rol === 'CLIENTE'
            ? '/compras'
            : '/gestion/devoluciones';

          void this.router.navigateByUrl(destino);
        },
        error: (fallo: unknown) => {
          if (fallo instanceof TimeoutError) {
            this.error.set(
              'El servidor tardó demasiado en responder.'
            );
            return;
          }

          if (fallo instanceof HttpErrorResponse) {
            switch (fallo.status) {
              case 0:
                this.error.set(
                  'No se pudo conectar con el backend. Verifica que esté ejecutándose.'
                );
                break;

              case 401:
                this.error.set(
                  'Correo o contraseña incorrectos.'
                );
                break;

              case 403:
                this.error.set(
                  'El servidor rechazó el acceso.'
                );
                break;

              default:
                this.error.set(
                  fallo.error?.detail ??
                  'No se pudo iniciar sesión.'
                );
            }

            return;
          }

          this.error.set(
            'Ocurrió un error al iniciar sesión.'
          );
        }
      });
  }

  salir(): void {
    this.authService.cerrarSesion();
    this.password = '';
    this.error.set('');
    this.mostrarPassword.set(false);
  }
}