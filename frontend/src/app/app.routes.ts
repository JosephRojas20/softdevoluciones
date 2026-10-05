import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/pages/login/login')
        .then(modulo => modulo.Login)
  },
  {
    path: '',
    loadComponent: () =>
      import('./core/layouts/main-layout/main-layout')
        .then(modulo => modulo.MainLayout),
    children: [
      {
        path: 'compras',
        canActivate: [authGuard],
        data: {
          roles: ['CLIENTE']
        },
        loadComponent: () =>
          import('./features/compras/pages/mis-compras/mis-compras')
            .then(modulo => modulo.MisCompras)
      },
      {
        path: 'compras/:compraId/devolucion/:detalleCompraId',
        canActivate: [authGuard],
        data: {
          roles: ['CLIENTE']
        },
        loadComponent: () =>
          import('./features/devoluciones/pages/solicitar-devolucion/solicitar-devolucion')
            .then(modulo => modulo.SolicitarDevolucion)
      },
      {
        path: 'compras/:id',
        canActivate: [authGuard],
        data: {
          roles: ['CLIENTE']
        },
        loadComponent: () =>
          import('./features/compras/pages/detalle-compra/detalle-compra')
            .then(modulo => modulo.DetalleCompra)
      },
      {
        path: 'devoluciones',
        canActivate: [authGuard],
        data: {
          roles: ['CLIENTE']
        },
        loadComponent: () =>
          import('./features/devoluciones/pages/mis-devoluciones/mis-devoluciones')
            .then(modulo => modulo.MisDevoluciones)
      },
      {
        path: 'gestion/devoluciones',
        canActivate: [authGuard],
        data: {
          roles: ['OPERADOR', 'ADMIN']
        },
        loadComponent: () =>
          import('./features/gestion/pages/gestion-devoluciones/gestion-devoluciones')
            .then(modulo => modulo.GestionDevoluciones)
      },
      {
        path: '',
        redirectTo: 'compras',
        pathMatch: 'full'
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'compras'
  }
];