# Rutas de la API de MiBDJ

[Volver al README](../README.md)

## API actual

### URL base

```text
http://localhost:8080
```

Las respuestas usan JSON. Las rutas de usuarios consultan la tabla `Usuario` con `JdbcTemplate` y no requieren una sesión iniciada.

### Usuarios

| Método | Ruta | Respuesta |
|---|---|---|
| `GET` | `/api/v1/users` | `200 OK` con un arreglo de usuarios. Si no hay usuarios, devuelve `[]`. |
| `GET` | `/api/v1/users/{id}` | `200 OK` con el usuario; `404 Not Found` si el ID no existe. |

Campos de la respuesta:

| Campo JSON | Tipo | Columna en MySQL |
|---|---|---|
| `id` | integer | `id_usuario` |
| `name` | string | `nombre` |
| `email` | string | `email` |
| `country` | string | `pais` |
| `date` | string, `YYYY-MM-DD` | `fecha` |

Se utilizan los usuarios precargados. Estas rutas no crean, editan ni eliminan usuarios.

```sh
curl -i http://localhost:8080/api/v1/users
curl -i http://localhost:8080/api/v1/users/1
curl -i http://localhost:8080/api/v1/users/999999
```

Respuesta del detalle del usuario `1` del seed:

```json
{
  "id": 1,
  "name": "Ana Mora",
  "email": "ana.mora@gmail.com",
  "country": "Costa Rica",
  "date": "2025-11-03"
}
```

### Estado del servicio

`GET /actuator/health` comprueba el estado del backend, incluida la conexión a MySQL. Si está disponible, devuelve HTTP 200 y `"status":"UP"`.

El pseudologin por email todavía está pendiente en la [issue #25](https://github.com/rgdan/mibdj-backend/issues/25).

## API anterior

Referencia histórica del ejemplo de usuarios. Consultaba la tabla `users`; este contrato no corresponde al esquema actual de MiBDJ. Las rutas `POST`, `PATCH` y `DELETE` de esta sección fueron eliminadas, al igual que los campos `userName`, `type` y `createdAt`. Las dos rutas GET conservan su URL, pero ahora devuelven los campos de la API actual.

### Usuarios


| Metodo | Endpoint | Descripcion | Respuesta exitosa |
|---|---|---|---|
| `GET` | `/api/v1/users` | Obtener todos los usuarios. | `200 OK`, arreglo JSON |
| `GET` | `/api/v1/users/{id}` | Obtener un usuario por su ID. | `200 OK`, objeto de usuario; `404 Not Found` si no existe |
| `POST` | `/api/v1/users` | Crear un usuario. | `201 Created`, objeto del usuario creado |
| `PATCH` | `/api/v1/users/{id}` | Actualizar los campos enviados que sean editables. | `200 OK`, objeto del usuario actualizado |
| `DELETE` | `/api/v1/users/{id}` | Eliminar un usuario. | `204 No Content` |

Campos JSON de usuario:

| Campo | Tipo | Notas |
|---|---|---|
| `id` | integer | Lo genera el servidor. |
| `userName` | string | Obligatorio al crear un usuario; debe ser unico. |
| `name` | string | Opcional. |
| `email` | string | Opcional. |
| `type` | string | `ADMIN` o `REGULAR`. |
| `createdAt` | string | Fecha y hora administradas por el servidor, en formato ISO-8601. |

`PATCH` permite actualizar `name`, `email` y `type`. Los campos omitidos no cambian. Este endpoint no modifica `userName`.

Crear un usuario:

```http
POST /api/v1/users
Content-Type: application/json

{
  "userName": "ana_soto",
  "name": "Ana Soto",
  "email": "asoto@example.com",
  "type": "REGULAR"
}
```

Actualizar el correo de un usuario:

```http
PATCH /api/v1/users/1
Content-Type: application/json

{
  "email": "ana.new@example.com"
}
```
