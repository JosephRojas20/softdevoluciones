import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { timeout } from 'rxjs';

import {
  CompraResponse,
  Pagina
} from '../models/compra.model';

@Injectable({
  providedIn: 'root'
})
export class CompraService {
  private readonly http = inject(HttpClient);

  listarMisCompras(pagina = 0) {
    return this.http
      .get<Pagina<CompraResponse>>(
        '/api/compras/mis-compras',
        {
          params: {
            page: pagina,
            size: 10,
            sort: 'fecha,desc'
          }
        }
      )
      .pipe(timeout(15000));
  }

  obtenerCompra(id: number) {
    return this.http
      .get<CompraResponse>(`/api/compras/${id}`)
      .pipe(timeout(15000));
  }
}