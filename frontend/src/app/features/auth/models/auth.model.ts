export type Rol = 'CLIENTE' | 'OPERADOR' | 'ADMIN';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface UsuarioResponse {
  id: number;
  nombre: string;
  email: string;
  rol: Rol;
}

export interface LoginResponse {
  token: string;
  tipo: string;
  expiresIn: number;
  usuario: UsuarioResponse;
}