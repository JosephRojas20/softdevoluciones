import { inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';

import { AuthService } from '../../features/auth/services/auth.service';
import { Rol } from '../../features/auth/models/auth.model';

export const authGuard: CanActivateFn = route => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.obtenerToken()) {
    return router.createUrlTree(['/login']);
  }

  return authService.recuperarPerfil().pipe(
    map(usuario => {
      const rolesPermitidos =
        route.data['roles'] as Rol[] | undefined;

      if (
        !rolesPermitidos ||
        rolesPermitidos.includes(usuario.rol)
      ) {
        return true;
      }

      const destino = usuario.rol === 'CLIENTE'
        ? '/compras'
        : '/gestion/devoluciones';

      return router.createUrlTree([destino]);
    }),
    catchError((error: unknown) => {
      if (
        error instanceof HttpErrorResponse &&
        (error.status === 401 || error.status === 403)
      ) {
        authService.cerrarSesion();
      }

      return of(router.createUrlTree(['/login']));
    })
  );
};