import { HttpInterceptorFn } from '@angular/common/http';

export const jwtInterceptor: HttpInterceptorFn = (request, next) => {
  const token = sessionStorage.getItem('softdv_token');

  const esApi = request.url.startsWith('/api/');

  const esPublica =
    request.url === '/api/auth/login' ||
    request.url === '/api/auth/registro';

  if (esApi && !esPublica && token) {
    const solicitudAutenticada = request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });

    return next(solicitudAutenticada);
  }

  return next(request);
};