# Rutas de la API de MiBDJ

[Volver al README](../README.md)

Las rutas usan el prefijo `/api/v1` y no requieren una sesión iniciada.

## Estado actual

El controlador de usuarios todavía pertenece al ejemplo anterior y consulta `users`. Los scripts de MiBDJ crean `Usuario` y las tablas de juegos de mesa. Estas rutas están pendientes de adaptar y no funcionan con el esquema actual; se conservan aquí como referencia del código existente.

Para comprobar el arranque y la conexión a MySQL, usar `GET /actuator/health`. Con ambos disponibles responde HTTP 200 y `status: "UP"`.

## URL base

Al ejecutar la aplicacion localmente, use:

```text
http://localhost:8080
```

Los cuerpos de las solicitudes y respuestas usan JSON. Al enviar un cuerpo JSON, incluya el encabezado `Content-Type: application/json`.

## Usuarios

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

## Ejemplo con curl

```sh
curl http://localhost:8080/api/v1/users
```

## Notas

- Reemplace los IDs de las rutas por los valores numericos que devuelve la API.
- La aplicacion utiliza MySQL. Configure la conexion a la base de datos para su entorno antes de iniciar el servicio.
