export interface Pagina<T> {
  content: T[];
  number: number;
  totalPages: number;
  totalElements: number;
  first: boolean;
  last: boolean;
}

export interface LineaCompra {
  detalleCompraId: number;
  productoId: number;
  nombre: string;
  descripcion: string | null;
  imagenUrl: string | null;
  cantidad: number;
  precioUnitario: number;
}

export interface CompraResponse {
  id: number;
  fecha: string;
  total: number;
  estado: string;
  detalles: LineaCompra[];
}