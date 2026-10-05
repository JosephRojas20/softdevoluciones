import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, timeout } from 'rxjs';

import {
  LoginRequest,
  LoginResponse,
  UsuarioResponse
} from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly usuarioActual =
    signal<UsuarioResponse | null>(null);

  readonly usuario = this.usuarioActual.asReadonly();

  iniciarSesion(datos: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>('/api/auth/login', datos)
      .pipe(
        timeout(15000),
        tap(respuesta => {
          sessionStorage.setItem(
            'softdv_token',
            respuesta.token
          );

          this.usuarioActual.set(respuesta.usuario);
        })
      );
  }

  recuperarPerfil(): Observable<UsuarioResponse> {
    return this.http
      .get<UsuarioResponse>('/api/auth/me')
      .pipe(
        timeout(15000),
        tap(usuario => {
          this.usuarioActual.set(usuario);
        })
      );
  }

  obtenerToken(): string | null {
    return sessionStorage.getItem('softdv_token');
  }

  cerrarSesion(): void {
    sessionStorage.removeItem('softdv_token');
    this.usuarioActual.set(null);
  }
}