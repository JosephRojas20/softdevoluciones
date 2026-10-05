# SoftDevoluciones

Aplicación web para registrar y gestionar solicitudes de devolución
de productos asociados a compras.

El cliente consulta sus compras, selecciona productos y solicita una
devolución. El operador revisa las solicitudes y actualiza sus estados
según el flujo establecido.

## Tecnologías

- Java 21.
- Spring Boot.
- Spring Security y JWT.
- Spring Data JPA.
- PostgreSQL.
- Flyway.
- Angular con componentes standalone.
- TypeScript, HTML y CSS.
- Maven y npm.

## Organización del proyecto

- `backend/`: API REST desarrollada con Spring Boot.
- `frontend/`: aplicación web desarrollada con Angular.
- `README.md`: instrucciones de instalación y ejecución.

El backend utiliza una arquitectura de monolito modular, con módulos
como autenticación, usuarios, compras y devoluciones.

El frontend organiza sus funcionalidades en:

- `auth`: inicio de sesión.
- `compras`: listado y detalle de compras.
- `devoluciones`: solicitud y consulta de devoluciones del cliente.
- `gestion`: gestión de devoluciones del operador.
- `core`: guards, interceptor HTTP y estructura visual compartida.

## Roles

| Rol | Funcionalidades |
|---|---|
| CLIENTE | Consultar sus compras, solicitar devoluciones y consultar sus solicitudes. |
| OPERADOR | Consultar solicitudes, aplicar filtros y gestionar sus estados. |
| ADMIN | Acceder a la gestión administrativa de devoluciones. |

El backend valida las credenciales y devuelve el rol del usuario.
Angular redirige al cliente a `/compras` y al operador o administrador
a `/gestion/devoluciones`.

Las rutas del frontend están protegidas mediante guards.
El backend aplica la autorización de los endpoints.

## Requisitos

- JDK 21.
- PostgreSQL en ejecución.
- Node.js y npm compatibles con la versión de Angular del proyecto.
- Git.
- IntelliJ IDEA y Visual Studio Code, opcionalmente.
- Postman, opcionalmente, para probar la API.

La versión de Angular y las dependencias del frontend pueden
consultarse en `frontend/package.json`.

## Configuración de la base de datos

Crear una base de datos llamada `softdv` en PostgreSQL:

```sql
CREATE DATABASE softdv;
```

También puede crearse desde pgAdmin.

No es necesario crear manualmente las tablas.
Flyway aplica las migraciones de:

```text
backend/src/main/resources/db/migration
```

Flyway registra las migraciones ejecutadas y no vuelve a ejecutar
las mismas versiones en cada inicio.

Hibernate utiliza `ddl-auto=validate` para verificar que las tablas
coincidan con las entidades.

No modificar migraciones ya aplicadas; agregar una nueva migración
para introducir cambios en la estructura.

## Variables de entorno del backend

Configurar:

| Variable | Descripción |
|---|---|
| DB_URL | URL de PostgreSQL. Por defecto: `jdbc:postgresql://localhost:5432/softdv`. |
| DB_USERNAME | Usuario de PostgreSQL. Por defecto: `postgres`. |
| DB_PASSWORD | Contraseña del usuario de PostgreSQL. |
| JWT_SECRET | Clave aleatoria codificada en Base64, con al menos 32 bytes decodificados. |

Las credenciales reales y la clave JWT no se incluyen en el repositorio.

En IntelliJ IDEA, configurar las variables en:

`Run → Edit Configurations → Environment variables`.

## Ejecutar el backend

Abrir la carpeta `backend` en IntelliJ IDEA, cargar Maven y ejecutar
la clase principal de Spring Boot.

También se puede ejecutar desde una terminal de Windows con las
variables de entorno configuradas:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

El backend estará disponible en:

```text
http://localhost:8080
```

### Consideración sobre los datos de prueba

Si se conserva la migración de datos de prueba que busca al usuario
`cliente@softdv.com`, ese usuario debe existir antes de ejecutarla.

Para una base nueva:

1. Mantener inicialmente las migraciones de estructura V1 y V2.
2. Ejecutar el backend y registrar `cliente@softdv.com`.
3. Detener el backend y reincorporar la migración V3 de datos de prueba.
4. Ejecutar nuevamente el backend.

Este procedimiento es únicamente para una instalación nueva.
En una base donde V3 ya fue aplicada, no retirarla ni modificarla.

Los productos y compras de demostración se cargan mediante SQL;
la aplicación no implementa un módulo de inventario.

## Ejecutar el frontend

Desde otra terminal:

```powershell
cd frontend
npm ci
npx ng serve --proxy-config proxy.conf.json
```

Abrir:

```text
http://localhost:4200
```

El archivo `proxy.conf.json` redirige las solicitudes `/api`
al backend en `http://localhost:8080`.

El backend debe estar en ejecución para iniciar sesión y consultar datos.

## Crear usuarios de prueba

Registrar una cuenta mediante:

```http
POST http://localhost:8080/api/auth/registro
Content-Type: application/json
```

Ejemplo:

```json
{
  "nombre": "Cliente de prueba",
  "email": "cliente@softdv.com",
  "password": "Cliente1234"
}
```

El registro público asigna el rol CLIENTE.

Para probar el operador, registrar otra cuenta y cambiar su rol
desde PostgreSQL, únicamente en el entorno de desarrollo:

```sql
UPDATE usuarios
SET rol = 'OPERADOR'
WHERE email = 'operador@softdv.com';
```

Después iniciar sesión con esa cuenta desde Angular.
Las contraseñas se almacenan cifradas mediante PasswordEncoder.

## Endpoints principales

| Método | Ruta | Función |
|---|---|---|
| POST | `/api/auth/registro` | Registrar cliente. |
| POST | `/api/auth/login` | Iniciar sesión. |
| GET | `/api/auth/me` | Consultar el usuario autenticado. |
| GET | `/api/compras/mis-compras` | Listar compras del cliente. |
| GET | `/api/compras/{id}` | Consultar el detalle de una compra. |
| POST | `/api/devoluciones` | Registrar una solicitud. |
| GET | `/api/devoluciones/mis-devoluciones` | Listar solicitudes del cliente. |
| GET | `/api/devoluciones/{id}` | Consultar una solicitud. |
| GET | `/api/admin/devoluciones` | Listar solicitudes con filtros y paginación. |
| PATCH | `/api/admin/devoluciones/{id}/estado` | Actualizar el estado de una solicitud. |

Los endpoints protegidos requieren:

```http
Authorization: Bearer <token>
```

## Flujo de estados

| Estado actual | Siguientes estados permitidos |
|---|---|
| SOLICITADA | EN_REVISION |
| EN_REVISION | APROBADA o RECHAZADA |
| APROBADA | COMPLETADA |
| RECHAZADA | Sin cambios adicionales |
| COMPLETADA | Sin cambios adicionales |

El rechazo requiere una observación del operador.

El backend valida la pertenencia de los productos a la compra,
las cantidades disponibles y las transiciones de estado.

El importe de devolución se calcula utilizando el precio original
de compra y la cantidad solicitada.

## Comprobación funcional

1. Iniciar sesión como cliente.
2. Consultar una compra y su detalle.
3. Solicitar una devolución.
4. Verificar que aparezca en Mis devoluciones.
5. Cerrar sesión e iniciar como operador.
6. Revisar la solicitud y cambiar su estado.
7. Volver a entrar como cliente y comprobar el estado actualizado.

## Estado y alcance

Se han implementado las pantallas de autenticación, compras,
solicitud de devolución, consulta del cliente y gestión del operador.

La comprobación completa de los requisitos debe realizarse antes
de considerar finalizada la entrega.

Pendiente identificado: convertir el formulario de solicitud del
cliente a formulario reactivo para cumplir exactamente RF-FE-06.

Las pantallas adicionales de gestión de usuarios e inventario
no forman parte de la implementación actual.