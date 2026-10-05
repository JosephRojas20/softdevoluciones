import { Component, computed, inject, signal } from '@angular/core';
import {
  Router,
  RouterLink,
  RouterLinkActive,
  RouterOutlet
} from '@angular/router';

import { AuthService } from '../../../features/auth/services/auth.service';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './main-layout.html',
  styleUrl: './main-layout.css'
})
export class MainLayout {
  readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly menuAbierto = signal(false);

  readonly esCliente = computed(
    () => this.authService.usuario()?.rol === 'CLIENTE'
  );

  readonly iniciales = computed(() => {
    const nombre = this.authService.usuario()?.nombre ?? '';

    return nombre
      .trim()
      .split(/\s+/)
      .slice(0, 2)
      .map(parte => parte.charAt(0))
      .join('')
      .toUpperCase();
  });

  cerrarMenu(): void {
    this.menuAbierto.set(false);
  }

  cerrarSesion(): void {
    this.authService.cerrarSesion();
    void this.router.navigateByUrl('/login');
  }
}