# Rutas de la API de MiBDJ

[Volver al README](../README.md)

## API actual

### URL base

```text
http://localhost:8080
```

Las respuestas usan JSON. Las rutas consultan la base de datos 'juegos_mesadb' mediante repositorios con 'JdbcTemplate' y no requieren una sesión iniciada.

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

### Acceso por email

`POST /api/v1/session` busca un usuario precargado por su email y devuelve
los mismos campos que el detalle de usuario.

```sh
curl -i http://localhost:8080/api/v1/session \
  -H 'Content-Type: application/json' \
  -d '{"email":"ana.mora@gmail.com"}'
```

Se quitan los espacios al principio y al final del email antes de validarlo.

| Respuesta | Cuándo |
|---|---|
| `200 OK` | El usuario existe; devuelve su información. |
| `400 Bad Request` | Falta el email, está vacío o su formato es inválido. |
| `404 Not Found` | No existe un usuario con ese email. |

Es un pseudologin: no pide contraseña, no genera tokens ni cookies de sesión
y no crea usuarios.

### Juegos

| Método | Ruta | Respuesta |
|---|---|---|
| `GET` | `/api/v1/games` | `200 OK` con un arreglo de juegos. |
| `GET` | `/api/v1/games/{id}` | `200 OK` con el juego; `404 Not Found` si no existe. |

El listado acepta estos filtros opcionales, que pueden combinarse:

| Parámetro | Uso |
|---|---|
| `title` | Coincidencia parcial del título. Se quitan espacios externos; vacío significa sin filtro. |
| `categoryId` | ID de categoría. Si no existe, devuelve `[]`. Un valor no numérico devuelve 400. |

Cada juego aparece una sola vez e incluye todas sus categorías,
incluso cuando se filtra por una de ellas.
Una búsqueda sin coincidencias devuelve `[]`.

| Campo JSON | Tipo | Columna en MySQL |
|---|---|---|
| `id` | integer | `id_juego` |
| `title` | string | `titulo` |
| `publisher` | string | `editorial` |
| `publicationYear` | integer | `anio_publicacion` |
| `minPlayers` | integer | `min_jugadores` |
| `maxPlayers` | integer | `max_jugadores` |
| `durationMin` | integer, minutos | `duracion_min` |
| `categories` | arreglo de objetos con `id` y `name` | `Juego_categoria` y `Categoria` |

Ejemplos:

```sh
curl -i http://localhost:8080/api/v1/games
curl -i 'http://localhost:8080/api/v1/games?title=Catan&categoryId=1'
curl -i http://localhost:8080/api/v1/games/1
```

### Categorías

`GET /api/v1/categories` devuelve HTTP 200 con un arreglo de objetos
con `id` y `name`, ordenados por nombre. Si no hay categorías, devuelve `[]`.

```sh
curl -i http://localhost:8080/api/v1/categories
```

### Colección personal

Juegos que posee un usuario (tabla `Posee`). Si el usuario no existe, todas
estas rutas devuelven `404 Not Found`.

| Método | Ruta | Respuesta |
|---|---|---|
| `GET` | `/api/v1/users/{userId}/collection` | `200 OK` con un arreglo, ordenado por título. Si la colección está vacía, devuelve `[]`. |
| `GET` | `/api/v1/users/{userId}/collection/{gameId}` | `200 OK` con el juego; `404 Not Found` si no está en la colección. |
| `POST` | `/api/v1/users/{userId}/collection` | `201 Created` con el juego agregado; `404` si el juego no existe; `409 Conflict` si ya estaba en la colección. |
| `PUT` | `/api/v1/users/{userId}/collection/{gameId}` | `200 OK` con el juego actualizado; `404` si no está en la colección. |
| `DELETE` | `/api/v1/users/{userId}/collection/{gameId}` | `204 No Content`; `404` si no está en la colección. |

El listado acepta el filtro opcional `status` (`nuevo`, `usado` o `prestado`).

Cuerpo de `POST`:

| Campo | Tipo | Notas |
|---|---|---|
| `gameId` | integer | Obligatorio. |
| `status` | string | Obligatorio: `nuevo`, `usado` o `prestado`. Se ignoran mayúsculas y espacios externos. |
| `date` | string, `YYYY-MM-DD` | Opcional; fecha de adquisición. Si se omite, se usa la fecha de hoy. No puede ser futura. |

`PUT` recibe `status` (obligatorio) y `date` (opcional; si se omite, se
conserva la fecha guardada). Un valor inválido devuelve `400 Bad Request`.

Campos de la respuesta:

| Campo JSON | Tipo | Columna en MySQL |
|---|---|---|
| `gameId` | integer | `Posee.id_juego` |
| `title` | string | `Juego.titulo` |
| `publisher` | string | `Juego.editorial` |
| `date` | string, `YYYY-MM-DD` | `Posee.fecha` |
| `status` | string | `Posee.estado` |

```sh
curl -i http://localhost:8080/api/v1/users/1/collection
curl -i 'http://localhost:8080/api/v1/users/1/collection?status=usado'
curl -i http://localhost:8080/api/v1/users/1/collection \
  -H 'Content-Type: application/json' \
  -d '{"gameId":8,"status":"nuevo","date":"2026-11-17"}'
curl -i -X PUT http://localhost:8080/api/v1/users/1/collection/8 \
  -H 'Content-Type: application/json' \
  -d '{"status":"prestado"}'
curl -i -X DELETE http://localhost:8080/api/v1/users/1/collection/8
```

Respuesta del `POST` anterior:

```json
{
  "gameId": 8,
  "title": "Pandemic",
  "publisher": "Z-Man Games",
  "date": "2026-11-17",
  "status": "nuevo"
}
```

### Lista de deseos

Juegos que un usuario quiere conseguir, con una prioridad de 1 a 3 (tabla
`Desea`). Si el usuario no existe, todas estas rutas devuelven `404 Not Found`.

| Método | Ruta | Respuesta |
|---|---|---|
| `GET` | `/api/v1/users/{userId}/wishlist` | `200 OK` con un arreglo ordenado por prioridad (1 primero) y después por fecha, de la más reciente a la más antigua. Si está vacía, devuelve `[]`. |
| `GET` | `/api/v1/users/{userId}/wishlist/{gameId}` | `200 OK` con el juego; `404 Not Found` si no está en la lista. |
| `POST` | `/api/v1/users/{userId}/wishlist` | `201 Created` con el juego agregado; `404` si el juego no existe; `409 Conflict` si ya estaba en la lista. |
| `PUT` | `/api/v1/users/{userId}/wishlist/{gameId}` | `200 OK` con el juego actualizado; `404` si no está en la lista. |
| `DELETE` | `/api/v1/users/{userId}/wishlist/{gameId}` | `204 No Content`; `404` si no está en la lista. |

El listado acepta el filtro opcional `priority` (1, 2 o 3).

Cuerpo de `POST`:

| Campo | Tipo | Notas |
|---|---|---|
| `gameId` | integer | Obligatorio. |
| `priority` | integer | Obligatorio: 1, 2 o 3. |
| `date` | string, `YYYY-MM-DD` | Opcional; si se omite, se usa la fecha de hoy. No puede ser futura. |

`PUT` recibe `priority` (obligatorio) y `date` (opcional; si se omite, se
conserva la fecha guardada). Un valor inválido devuelve `400 Bad Request`.

Campos de la respuesta:

| Campo JSON | Tipo | Columna en MySQL |
|---|---|---|
| `gameId` | integer | `Desea.id_juego` |
| `title` | string | `Juego.titulo` |
| `publisher` | string | `Juego.editorial` |
| `priority` | integer | `Desea.prioridad` |
| `date` | string, `YYYY-MM-DD` | `Desea.fecha` |

```sh
curl -i http://localhost:8080/api/v1/users/1/wishlist
curl -i 'http://localhost:8080/api/v1/users/1/wishlist?priority=2'
curl -i http://localhost:8080/api/v1/users/1/wishlist \
  -H 'Content-Type: application/json' \
  -d '{"gameId":5,"priority":1}'
curl -i -X PUT http://localhost:8080/api/v1/users/1/wishlist/5 \
  -H 'Content-Type: application/json' \
  -d '{"priority":3}'
curl -i -X DELETE http://localhost:8080/api/v1/users/1/wishlist/5
```

### Valoraciones

Calificaciones de 1 a 10 y comentarios opcionales de los usuarios sobre los
juegos (tabla `Valora`).

| Método | Ruta | Respuesta |
|---|---|---|
| `GET` | `/api/v1/games/{gameId}/ratings` | `200 OK` con todas las valoraciones del juego, su promedio y cantidad; `404 Not Found` si el juego no existe. |
| `GET` | `/api/v1/users/{userId}/ratings/{gameId}` | `200 OK` con la valoración del usuario; `404` si el usuario o el juego no existen o si el usuario todavía no lo ha valorado. |
| `PUT` | `/api/v1/users/{userId}/ratings/{gameId}` | `201 Created` si el usuario no había valorado el juego; `200 OK` si reemplaza su valoración anterior; `404` si el usuario o el juego no existen. |

Cuerpo de `PUT`:

| Campo | Tipo | Notas |
|---|---|---|
| `rating` | integer | Obligatorio, de 1 a 10. |
| `comment` | string | Opcional, máximo 255 caracteres. Si se omite o está vacío, se guarda sin comentario (`null`). |

Un valor inválido devuelve `400 Bad Request`.

Campos de cada valoración:

| Campo JSON | Tipo | Columna en MySQL |
|---|---|---|
| `userId` | integer | `Valora.id_usuario` |
| `userName` | string | `Usuario.nombre` |
| `gameId` | integer | `Valora.id_juego` |
| `gameTitle` | string | `Juego.titulo` |
| `rating` | integer | `Valora.calificacion` |
| `comment` | string o `null` | `Valora.comentario` |

`GET /api/v1/games/{gameId}/ratings` devuelve un objeto con `gameId`,
`gameTitle`, `average` (promedio con un decimal, `null` si no hay
valoraciones), `count` y `ratings` (arreglo ordenado de la calificación más
alta a la más baja).

```sh
curl -i http://localhost:8080/api/v1/games/1/ratings
curl -i http://localhost:8080/api/v1/users/1/ratings/1
curl -i -X PUT http://localhost:8080/api/v1/users/5/ratings/1 \
  -H 'Content-Type: application/json' \
  -d '{"rating":8,"comment":"Muy buenas partidas"}'
```

Respuesta de las valoraciones de Catan con los datos de ejemplo:

```json
{
  "gameId": 1,
  "gameTitle": "Catan",
  "average": 8.0,
  "count": 2,
  "ratings": [
    {
      "userId": 1,
      "userName": "Ana Mora",
      "gameId": 1,
      "gameTitle": "Catan",
      "rating": 9,
      "comment": "Excelente, siempre genera negociaciones divertidas y es muy estratégico"
    },
    {
      "userId": 3,
      "userName": "María Fernández",
      "gameId": 1,
      "gameTitle": "Catan",
      "rating": 7,
      "comment": null
    }
  ]
}
```

### Lugares

Catálogo de lugares registrados donde se juegan las partidas (tabla `Lugar`).

| Método | Ruta | Respuesta |
|---|---|---|
| `GET` | `/api/v1/places` | `200 OK` con un arreglo de lugares. Si no hay lugares registrados, devuelve `[]`. |

Campos de la respuesta:

| Campo JSON | Tipo | Columna en MySQL |
|---|---|---|
| `id` | integer | `id_lugar` |
| `name` | string | `nombre` |
| `type` | string | `tipo` |
| `address` | string | `direccion` |

```sh
curl -i http://localhost:8080/api/v1/places
```


### Partidas

Historial, detalle y registro transaccional de partidas jugadas (tablas `Partida`, `Participa`, `Juego`, `Lugar` y vista `vw_partidas_usuario`). Si el usuario no existe, estas rutas devuelven `404 Not Found`.

| Método | Ruta | Respuesta |
|---|---|---|
| `GET` | `/api/v1/users/{userId}/matches` | `200 OK` con el historial de partidas del usuario consultando `vw_partidas_usuario`. Si no ha jugado partidas, devuelve `[]`. |
| `GET` | `/api/v1/users/{userId}/matches/{matchId}` | `200 OK` con el detalle de la partida y sus participantes; `404 Not Found` si la partida no existe o el usuario no participa en ella. |
| `POST` | `/api/v1/users/{userId}/matches` | `201 Created` con la partida creada; `400` o `404` si algún dato o participante es inválido (revierte toda la transacción sin dejar datos parciales). |

Cuerpo de `POST`:

| Campo | Tipo | Notas |
|---|---|---|
| `gameId` | integer | Obligatorio. Debe existir en `Juego`. |
| `placeId` | integer | Obligatorio. Debe existir en `Lugar`. |
| `fecha` | string, `YYYY-MM-DD` | Obligatorio. |
| `duracionMin` | integer | Obligatorio, mayor a 0. |
| `notas` | string | Opcional. |
| `participantes` | arreglo de objetos | Obligatorio, al menos un participante con `userId` (integer), `puntos` (integer) y `ganador` (boolean). No admite usuarios duplicados ni inexistentes. |

```sh
curl -i http://localhost:8080/api/v1/users/1/matches
curl -i http://localhost:8080/api/v1/users/1/matches/1
curl -i http://localhost:8080/api/v1/users/1/matches \
  -H 'Content-Type: application/json' \
  -d '{
    "fecha": "2026-11-22",
    "duracionMin": 65,
    "notas": "Partida de prueba",
    "gameId": 1,
    "placeId": 1,
    "participantes": [
      {"userId": 1, "puntos": 10, "ganador": true},
      {"userId": 2, "puntos": 8, "ganador": false}
    ]
  }'
```


### Estadísticas de usuario

Resumen de actividad del usuario en sus partidas registradas (tablas `Partida`, `Participa`, `Juego` y `Usuario`).

| Método | Ruta | Respuesta |
|---|---|---|
| `GET` | `/api/v1/users/{userId}/stats` | `200 OK` con las estadísticas del usuario; `404 Not Found` si el usuario no existe. |

Si el usuario no tiene partidas registradas, los tres arreglos se devuelven vacíos (`[]`).

Campos de la respuesta:

| Campo JSON | Tipo | Descripción |
|---|---|---|
| `mostPlayedGames` | arreglo | Juegos ordenados de mayor a menor por cantidad de partidas jugadas (`gameId`, `gameTitle`, `matchesPlayed`). |
| `averageDurationByGame` | arreglo | Duración promedio en minutos por cada juego jugado (`gameId`, `gameTitle`, `averageDurationMin`). |
| `frequentCompanions` | arreglo | Otros usuarios con quienes ha compartido partidas, ordenados por frecuencia (`userId`, `userName`, `sharedMatches`). |

```sh
curl -i http://localhost:8080/api/v1/users/1/stats
```


### Estado del servicio

`GET /actuator/health` comprueba el estado del backend, incluida la conexión a MySQL. Si está disponible, devuelve HTTP 200 y `"status":"UP"`.

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
