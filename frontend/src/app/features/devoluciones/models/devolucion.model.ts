import { MotivoDevolucion } from './crear-devolucion.model';

export type EstadoDevolucion =
  | 'SOLICITADA'
  | 'EN_REVISION'
  | 'APROBADA'
  | 'RECHAZADA'
  | 'COMPLETADA';

export interface DetalleDevolucionResponse {
  detalleCompraId: number;
  productoId: number;
  producto: string;
  cantidad: number;
  precioUnitario: number;
  importe: number;
}

export interface DevolucionResponse {
  id: number;
  compraId: number;
  usuarioId: number;
  fechaSolicitud: string;
  estado: EstadoDevolucion;
  motivo: MotivoDevolucion;
  comentario: string | null;
  observacionOperador: string | null;
  importe: number;
  detalles: DetalleDevolucionResponse[];
}