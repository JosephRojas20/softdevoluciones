import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map, Observable, timeout } from 'rxjs';

import {
  CrearDevolucionRequest,
  DevolucionCreada,
  MotivoDevolucion
} from '../models/crear-devolucion.model';

import {
  DevolucionResponse,
  DetalleDevolucionResponse,
  EstadoDevolucion
} from '../models/devolucion.model';

import { Pagina } from '../../compras/models/compra.model';

type RespuestaApi = Omit<DevolucionResponse, 'detalles'> & {
  detalles?: DetalleDevolucionResponse[];
  items?: DetalleDevolucionResponse[];
};

export interface FiltrosDevoluciones {
  estado?: EstadoDevolucion | '';
  motivo?: MotivoDevolucion | '';
  desde?: string;
  hasta?: string;
}

export interface CambiarEstadoRequest {
  estado: EstadoDevolucion;
  observacion: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class DevolucionService {
  private readonly http = inject(HttpClient);

  crearSolicitud(
    solicitud: CrearDevolucionRequest
  ): Observable<DevolucionCreada> {
    return this.http
      .post<DevolucionCreada>('/api/devoluciones', solicitud)
      .pipe(timeout(15000));
  }

  listarMisDevoluciones(
    page = 0
  ): Observable<Pagina<DevolucionResponse>> {
    const params = this.parametrosPagina(page);

    return this.http
      .get<Pagina<RespuestaApi>>(
        '/api/devoluciones/mis-devoluciones',
        { params }
      )
      .pipe(
        timeout(15000),
        map(pagina => this.normalizarPagina(pagina))
      );
  }

  obtenerDevolucion(id: number): Observable<DevolucionResponse> {
    return this.http
      .get<RespuestaApi>(`/api/devoluciones/${id}`)
      .pipe(
        timeout(15000),
        map(solicitud => this.normalizar(solicitud))
      );
  }

  listarParaGestion(
    filtros: FiltrosDevoluciones,
    page = 0
  ): Observable<Pagina<DevolucionResponse>> {
    let params = this.parametrosPagina(page);

    if (filtros.estado) {
      params = params.set('estado', filtros.estado);
    }

    if (filtros.motivo) {
      params = params.set('motivo', filtros.motivo);
    }

    if (filtros.desde) {
      params = params.set('desde', filtros.desde);
    }

    if (filtros.hasta) {
      params = params.set('hasta', filtros.hasta);
    }

    return this.http
      .get<Pagina<RespuestaApi>>(
        '/api/admin/devoluciones',
        { params }
      )
      .pipe(
        timeout(15000),
        map(pagina => this.normalizarPagina(pagina))
      );
  }

  cambiarEstado(
    id: number,
    solicitud: CambiarEstadoRequest
  ): Observable<DevolucionResponse> {
    return this.http
      .patch<RespuestaApi>(
        `/api/admin/devoluciones/${id}/estado`,
        solicitud
      )
      .pipe(
        timeout(15000),
        map(respuesta => this.normalizar(respuesta))
      );
  }

  private parametrosPagina(page: number): HttpParams {
    return new HttpParams()
      .set('page', page)
      .set('size', 10)
      .set('sort', 'fechaSolicitud,desc');
  }

  private normalizarPagina(
    pagina: Pagina<RespuestaApi>
  ): Pagina<DevolucionResponse> {
    return {
      ...pagina,
      content: pagina.content.map(
        solicitud => this.normalizar(solicitud)
      )
    };
  }

  private normalizar(
    respuesta: RespuestaApi
  ): DevolucionResponse {
    return {
      ...respuesta,
      detalles: respuesta.detalles ?? respuesta.items ?? []
    };
  }
}