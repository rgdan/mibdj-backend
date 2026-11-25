# Rutas de la API de MiBDJ

[README](../README.md) · [Configuración local](setup.md)

[Usuarios](#usuarios) · [Acceso](#acceso-por-email) · [Juegos](#juegos) · [Categorías](#categorías) · [Colección](#colección-personal) · [Wishlist](#lista-de-deseos) · [Valoraciones](#valoraciones) · [Lugares](#lugares) · [Partidas](#partidas) · [Estadísticas](#estadísticas-de-usuario)

## URL base

```text
http://localhost:8080
```

Las rutas consultan `juegos_mesadb` mediante repositorios con `JdbcTemplate`.
Los cuerpos de entrada y las respuestas con contenido usan JSON; las fechas
usan `YYYY-MM-DD`. Los ejemplos parten de los datos de `data.sql` y del
puerto predeterminado `8080`.

Las rutas no requieren una sesión iniciada. El pseudologin devuelve el ID
que el frontend incluye en las rutas; no autentica ni autoriza las solicitudes.
Los cuerpos de `POST` y `PUT` se envían con `Content-Type: application/json`.
Una respuesta `204 No Content` no tiene cuerpo.

## Usuarios

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

## Acceso por email

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

## Juegos

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

## Categorías

`GET /api/v1/categories` devuelve HTTP 200 con un arreglo de objetos
con `id` y `name`, ordenados por nombre. Si no hay categorías, devuelve `[]`.

```sh
curl -i http://localhost:8080/api/v1/categories
```

## Colección personal

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
Un estado sin coincidencias devuelve `[]`.

Cuerpo de `POST`:

| Campo | Tipo | Notas |
|---|---|---|
| `gameId` | integer | Obligatorio. |
| `status` | string | Obligatorio: `nuevo`, `usado` o `prestado`. Se ignoran mayúsculas y espacios externos. |
| `date` | string, `YYYY-MM-DD` | Opcional; fecha de adquisición. Si se omite, se usa la fecha de hoy. No puede ser futura. |

`PUT` recibe `status` (obligatorio) y `date` (opcional; si se omite, se
conserva la fecha guardada). Un valor inválido devuelve `400 Bad Request`.
La actualización usa `PUT`; no hay una ruta `PATCH`. Eliminar un elemento
quita la relación con el usuario y conserva el juego en el catálogo.

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

## Lista de deseos

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
Las prioridades son `1` baja, `2` media y `3` alta. El orden actual es
ascendente: primero la prioridad baja. Un filtro sin coincidencias devuelve `[]`.

Cuerpo de `POST`:

| Campo | Tipo | Notas |
|---|---|---|
| `gameId` | integer | Obligatorio. |
| `priority` | integer | Obligatorio: 1, 2 o 3. |
| `date` | string, `YYYY-MM-DD` | Opcional; si se omite, se usa la fecha de hoy. No puede ser futura. |

`PUT` recibe `priority` (obligatorio) y `date` (opcional; si se omite, se
conserva la fecha guardada). Un valor inválido devuelve `400 Bad Request`.
La actualización usa `PUT`; no hay una ruta `PATCH`. Eliminar un elemento
quita la relación con el usuario y conserva el juego en el catálogo.

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

## Valoraciones

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
`PUT` guarda la valoración completa: si se omite `comment`, elimina el
comentario anterior. Enviá números enteros para `rating` y `priority`;
la conversión JSON actual puede truncar valores decimales antes de validarlos.

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

## Lugares

Lugares del usuario para el selector al registrar una partida (tabla `Lugar`).
También se pueden crear por separado, antes de registrar la partida.

| Método | Ruta | Respuesta |
|---|---|---|
| `GET` | `/api/v1/users/{id}/places` | `200 OK` con los lugares del usuario, ordenados por nombre; `[]` si no tiene lugares; `404 Not Found` si el usuario no existe. |
| `POST` | `/api/v1/users/{userId}/places` | `201 Created` con el lugar creado; `400 Bad Request` para datos inválidos; `404 Not Found` si el usuario no existe. |

`POST` recibe `name` y `address`: textos obligatorios de máximo 255 caracteres
después de quitar los espacios externos. El ID se genera con `AUTO_INCREMENT`;
el lugar se guarda para el usuario de la ruta y aparece en su listado.

Campos de la respuesta:

| Campo JSON | Tipo | Columna en MySQL |
|---|---|---|
| `placeId` | integer | `id_lugar` |
| `name` | string | `nombre` |
| `address` | string | `direccion` |

```sh
curl -i http://localhost:8080/api/v1/users/1/places
curl -i http://localhost:8080/api/v1/users/1/places \
  -H 'Content-Type: application/json' \
  -d '{"name":"Casa de Ana","address":"Alajuela centro"}'
```

Respuesta para el usuario `1` del seed:

```json
[
  {
    "placeId": 1,
    "name": "Casa de Ana",
    "address": "Alajuela centro, 200 m norte de la catedral"
  }
]
```

## Partidas

Historial, detalle y registro transaccional de partidas jugadas (tablas `Partida`, `Participa`, `Juego`, `Lugar` y vista `vw_partidas_usuario`). Si el usuario no existe, estas rutas devuelven `404 Not Found`.

| Método | Ruta | Respuesta |
|---|---|---|
| `GET` | `/api/v1/users/{userId}/matches` | `200 OK` con las partidas que organizó o jugó el usuario, sin duplicados, consultando `vw_partidas_usuario`; `[]` si no tiene historial. |
| `GET` | `/api/v1/users/{userId}/matches/{matchId}` | `200 OK` con el detalle y los participantes; `404 Not Found` si la partida no existe o el usuario no es organizador ni participante. |
| `POST` | `/api/v1/users/{userId}/matches` | `201 Created` con la partida creada; `400` para las validaciones de entrada descritas abajo y `404` para referencias inexistentes. |

Cuerpo de `POST`:

| Campo | Tipo | Notas |
|---|---|---|
| `gameId` | integer | Obligatorio. Debe existir en `Juego`. |
| `placeId` | integer | Obligatorio. Debe existir en `Lugar` y pertenecer al organizador indicado en la ruta. |
| `fecha` | string, `YYYY-MM-DD` | Obligatorio. |
| `duracionMin` | integer | Obligatorio, mayor a 0. |
| `notas` | string | Opcional. Se quitan espacios externos; la columna admite hasta 255 caracteres. |
| `participantes` | arreglo de objetos | Obligatorio, con al menos un participante. No admite IDs de usuario duplicados ni inexistentes. |

Cada participante tiene estos campos obligatorios:

| Campo | Tipo | Validación |
|---|---|---|
| `userId` | integer | Debe existir en `Usuario`. |
| `puntos` | integer | Mayor o igual a 0. |
| `ganador` | boolean | `true` o `false`. Se permiten varios ganadores o ninguno. |

El organizador no se agrega automáticamente a `participantes`: incluí su ID
si también jugó. El ID de la partida se genera en el servidor. `Partida` y
sus filas de `Participa` se guardan en una transacción; si falla una escritura,
se revierte la operación completa.

Respuestas de partidas:

| Campo JSON | Tipo | Dónde aparece |
|---|---|---|
| `matchId` | integer | Listado, detalle y creación. |
| `userId` | integer | Listado, detalle y creación; identifica al organizador. |
| `gameId` | integer | Detalle y creación. |
| `gameTitle` | string | Listado y detalle. |
| `placeId` | integer | Detalle y creación. |
| `placeName` | string | Listado y detalle. |
| `fecha` | string, `YYYY-MM-DD` | Listado, detalle y creación. |
| `duracionMin` | integer | Listado, detalle y creación. |
| `notas` | string | Detalle y creación, cuando no es `null`. |
| `participantesCount` | integer | Listado. |
| `participantes` | arreglo de objetos | Detalle y creación; cada elemento contiene `userId`, `puntos` y `ganador`. |

Los campos `null` se omiten en las respuestas de partidas. El historial se
ordena por fecha e ID descendentes. En el detalle, los participantes se
ordenan por ganador, puntos descendentes e ID ascendente.

| Error | Casos |
|---|---|
| `400 Bad Request` | Campos obligatorios ausentes, fecha con formato inválido, duración no positiva, puntos negativos, participantes vacíos o repetidos, o lugar de otro organizador. |
| `404 Not Found` | Organizador, juego, lugar o usuario participante inexistentes. En el detalle, también una partida ajena o inexistente. |
| `500 Internal Server Error` | Actualmente, notas que exceden 255 caracteres o elementos `null` en participantes. La generación con `MAX(id_partida) + 1` también puede fallar con registros simultáneos. |

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


## Estadísticas de usuario

Resumen de las partidas donde el usuario figura en `Participa` (tablas
`Partida`, `Participa`, `Juego` y `Usuario`). Organizar una partida sin estar
incluido como participante no la incorpora a estas estadísticas.

| Método | Ruta | Respuesta |
|---|---|---|
| `GET` | `/api/v1/users/{userId}/stats` | `200 OK` con las estadísticas del usuario; `404 Not Found` si el usuario no existe. |

Si el usuario no tiene partidas registradas, los tres arreglos se devuelven vacíos (`[]`).

Campos de la respuesta:

| Campo JSON | Tipo | Descripción |
|---|---|---|
| `mostPlayedGames` | arreglo | Juegos ordenados de mayor a menor por cantidad de partidas jugadas (`gameId`, `gameTitle`, `matchesPlayed`). |
| `averageDurationByGame` | arreglo | Duración promedio en minutos por juego, redondeada a dos decimales y ordenada de mayor a menor (`gameId`, `gameTitle`, `averageDurationMin`). |
| `frequentCompanions` | arreglo | Otros usuarios con quienes ha compartido partidas, ordenados por frecuencia (`userId`, `userName`, `sharedMatches`). Excluye al propio usuario y distingue compañeros por ID. |

Cada partida se cuenta una sola vez. En caso de empate, las listas se
ordenan por título del juego o nombre del compañero, respectivamente.

```sh
curl -i http://localhost:8080/api/v1/users/1/stats
```


## Estado del servicio

`GET /actuator/health` comprueba el estado del backend, incluida la conexión a MySQL. Si está disponible, devuelve HTTP 200 y `"status":"UP"`.
