# Arrancar el backend

Necesitás Java 21 y Docker en ejecución. Ejecutá los comandos desde la raíz del backend.

## 1. Compilar

```sh
./mvnw clean compile
```

## 2. Levantar MySQL

La primera vez, creá el contenedor con los datos de ejemplo:

```sh
docker run -d --name mibdj-mysql \
  -p 127.0.0.1:3306:3306 \
  -e MYSQL_ROOT_PASSWORD=mibdj_root_local \
  -e MYSQL_DATABASE=juegos_mesadb \
  -e MYSQL_USER=mibdj \
  -e MYSQL_PASSWORD=mibdj_local \
  -v mibdj-mysql-data:/var/lib/mysql \
  -v "$PWD/src/main/resources/schema.sql:/docker-entrypoint-initdb.d/01-schema.sql:ro" \
  -v "$PWD/src/main/resources/data.sql:/docker-entrypoint-initdb.d/02-data.sql:ro" \
  mysql:8.4
```

En los siguientes arranques, usá solamente:

```sh
docker start mibdj-mysql
```

Esperá a que MySQL esté listo. Podés verlo con `docker logs -f mibdj-mysql`:
la línea final de `ready for connections` debe indicar `port: 3306`.
Salí de los logs con `Ctrl+C`; el contenedor sigue funcionando.
Los datos se conservan en el volumen y los scripts se importan solo cuando está vacío.

## 3. Arrancar el backend

La primera vez, creá `.env` en la raíz con estos valores para el contenedor anterior:

```properties
DB_URL=jdbc:mysql://127.0.0.1:3306/juegos_mesadb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USR_NAME=mibdj
DB_PWD=mibdj_local
```

Son credenciales de desarrollo local. Si ya tenés un contenedor configurado,
usá sus valores de conexión.

```sh
./mvnw spring-boot:run
```

API: `http://localhost:8080`. Comprobación: `curl http://localhost:8080/actuator/health`.

Para parar: `Ctrl+C` en el backend y `docker stop mibdj-mysql` para MySQL.

[README](../README.md) · [Rutas de la API](api.md)
