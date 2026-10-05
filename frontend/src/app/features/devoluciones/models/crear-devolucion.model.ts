export type MotivoDevolucion =
  | 'DEFECTUOSO'
  | 'PRODUCTO_INCORRECTO'
  | 'INCOMPLETO'
  | 'OTRO';

export interface CrearDevolucionRequest {
  compraId: number;
  motivo: MotivoDevolucion;
  comentario: string | null;
  items: {
    detalleCompraId: number;
    cantidad: number;
  }[];
}

// En esta página solo necesitamos el ID de la respuesta.
export interface DevolucionCreada {
  id: number;
}