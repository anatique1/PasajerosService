# Portal de pasajeros RutaMacht

Interfaz React + Vite para consultar viajes publicados y autenticarse con Google. La autenticación valida el ID token en el microservicio `pasajero` y guarda el perfil en la colección MongoDB `cuentas`.

## Configurar Google

1. En Google Cloud Console, crea un cliente OAuth de tipo **Aplicación web**.
2. En **Orígenes autorizados de JavaScript**, agrega `http://localhost:5173`.
3. Copia el ID de cliente (no se necesita un secreto OAuth en el navegador).
4. Crea `frontend/.env.local` copiando `.env.example` y reemplaza `VITE_GOOGLE_CLIENT_ID` por ese ID.
5. Configura `GOOGLE_CLIENT_ID` con ese mismo valor en las ejecuciones de IntelliJ de `pasajero` y `APIRutaMacht`.

El backend comprueba la firma, audiencia y vigencia de la credencial con la biblioteca oficial de Google antes de crear o actualizar la cuenta. La clave de Atlas/local se configura por separado como `PASAJEROS_MONGODB_URI`.

## Ejecutar

1. Inicia MongoDB y el microservicio `pasajero` (puerto `8082`).
2. Inicia `APIRutaMacht` (puerto `8081`) para consultar viajes. Configura `RUTAMACHT_MONGODB_URI` en ese servicio si usas Atlas.
3. En esta carpeta ejecuta `npm install` y luego `npm run dev`.
4. Abre `http://localhost:5173`.

La búsqueda consulta `GET http://localhost:8081/api/viajes`. El acceso Google usa `POST http://localhost:8082/api/auth/google` con `{ "credential": "..." }`.

El pasajero puede reservar un cupo indicando el punto de recogida, consultar sus reservas y cancelarlas desde **Mi espacio**. El API principal verifica el token de Google antes de cada operación; conserva la credencial solo en memoria del navegador, así que al recargar la página deberá iniciar sesión de nuevo. Los cupos se descuentan al confirmar una reserva y se devuelven al cancelarla.

Rutas protegidas del API principal:

- `POST /api/viajes/{viajeId}/reservas` con `{ "puntoRecogida": "..." }`.
- `GET /api/reservas/mis` para las reservas de la cuenta autenticada.
- `DELETE /api/reservas/{reservaId}` para cancelar una reserva propia.

Las tres rutas usan `Authorization: Bearer <Google ID token>`. Configura también `GOOGLE_CLIENT_ID` en la ejecución IntelliJ de `APIRutaMacht`; debe ser el mismo cliente web que usa el frontend y el microservicio `pasajero`.
