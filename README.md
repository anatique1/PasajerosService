# PasajerosService

Microservicio de **pasajeros** del proyecto **RutaMacht** (API secundaria). Expone un CRUD de pasajeros persistido en **MongoDB**, el inicio de sesión con Google y un portal web en React para buscar viajes y reservar cupos.

APIRutaMacht (API principal, puerto 8081) consume este servicio mediante `RestClient`.

## Arquitectura

```
Portal React (5173) ──► APIRutaMacht (8081) ──► PasajerosService (8082) ──► MongoDB
        │                        │                                          (pasajeros, cuentas)
        └── login Google ────────┼──────────────────► /api/auth/google
                                 └── MongoDB (conductores, vehículos, viajes, reservas)
Panel Angular (4200) ──► APIRutaMacht (8081)
```

| Componente | Tecnología | Puerto |
|---|---|---|
| API de pasajeros | Spring Boot 4, Java, Spring Data MongoDB | 8082 |
| Portal de pasajeros (segunda interfaz) | React + Vite + TypeScript | 5173 |
| Base de datos | MongoDB (`pasajeros`) | 27017 |

## Requisitos

- JDK 17 o superior (el de tu `pom.xml`)
- Node.js 18 o superior (solo para el portal React)
- MongoDB en ejecución en `localhost:27017`

## Configuración

Variables de entorno (todas opcionales salvo el login con Google):

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `PASAJEROS_MONGODB_URI` | Cadena de conexión a MongoDB | `mongodb://localhost:27017/pasajeros` |
| `GOOGLE_CLIENT_ID` | Client ID de OAuth (aplicación web) para validar el token de Google | vacío (el login responde 503) |

Frontend (`frontend/.env`, copia `frontend/.env.example`):

```
VITE_GOOGLE_CLIENT_ID=tu-client-id.apps.googleusercontent.com
VITE_RUTAMACHT_API_URL=http://localhost:8081/api
VITE_PASAJEROS_API_URL=http://localhost:8082/api
```

## Cómo ejecutarlo

Orden recomendado: MongoDB → PasajerosService → APIRutaMacht → portal.

```bash
# 1. Backend de pasajeros
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run

# 2. Portal React
cd frontend
npm install
npm run dev                     # http://localhost:5173
```

## Documentación con Swagger

- Swagger UI: http://localhost:8082/swagger-ui.html
- OpenAPI JSON: http://localhost:8082/v3/api-docs

## Endpoints

### Pasajeros (`/api/pasajeros`)

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/pasajeros/conexion` | Verifica que el servicio está activo | 200 |
| POST | `/api/pasajeros` | Crea un pasajero (el `id` es opcional; si falta, MongoDB lo genera) | 201, 400 |
| GET | `/api/pasajeros` | Lista todos los pasajeros | 200 |
| GET | `/api/pasajeros/{id}` | Consulta un pasajero | 200, 404 |
| PUT | `/api/pasajeros/{id}` | Actualiza un pasajero | 200, 400, 404 |
| DELETE | `/api/pasajeros/{id}` | Elimina un pasajero | 200, 404 |

Ejemplo de cuerpo para `POST` y `PUT`:

```json
{
  "nombre": "Laura",
  "apellido": "Gómez",
  "documento": "1234567890",
  "tipoDocumento": "Cédula de ciudadanía",
  "telefono": "3001234567",
  "correo": "laura@correo.com",
  "fechaRegistro": "2026-11-18"
}
```

Todos los campos son obligatorios, salvo `id` al crear.

### Autenticación (`/api/auth`)

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| POST | `/api/auth/google` | Valida el ID token de Google y guarda o actualiza la cuenta en la colección `cuentas` | 200, 401, 503 |

Cuerpo: `{ "credential": "<id token de Google>" }`

## Comunicación entre microservicios

APIRutaMacht llama a este servicio mediante `PasajeroClient` (URL configurable con `API_PASAJEROS_URL`, por defecto `http://localhost:8082`):

| APIRutaMacht (8081) | PasajerosService (8082) |
|---|---|
| `GET /api/conexion/pasajero` | `GET /api/pasajeros/conexion` |
| `POST/GET/PUT/DELETE /api/pasajero` | `POST/GET/PUT/DELETE /api/pasajeros` |

Si PasajerosService está apagado, APIRutaMacht responde 503 en esas rutas.

## Persistencia

Los datos viven en MongoDB, base `pasajeros`, colecciones `pasajeros` y `cuentas`. Sobreviven al reinicio de la aplicación.

## Estructura del proyecto

```
src/main/java/com/example/pasajero/
├── config/       CorsConfig, OpenApiConfig
├── controller/   PasajeroController, AuthController
├── dto/          GoogleLoginRequest, PerfilUsuarioResponse
├── model/        Persona, Pasajero, CuentaGoogle
├── repository/   PasajeroRepository, CuentaGoogleRepository
└── services/     PasajeroService, GoogleAuthService
frontend/         Portal React (login con Google, búsqueda de viajes, reservas)
```

## Notas para el repositorio

Agrega al `.gitignore` para no subir carpetas pesadas:

```
frontend/node_modules/
frontend/dist/
frontend/.env
```
