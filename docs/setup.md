# Configuración local

[Volver al README](../README.md)

## Requisitos

- **JDK 21**, con `java` y `javac` disponibles.
- **Docker**, con el motor en ejecución.

El proyecto incluye Maven Wrapper (`mvnw`), así que no hace falta instalar Maven por separado. La primera ejecución necesita internet para descargar Maven y las dependencias.

Comprobá las versiones desde la raíz del backend:

```sh
java -version
javac -version
./mvnw -version
```

Los tres deben indicar Java 21. Si Maven usa otra versión, revisá `JAVA_HOME`.

## Dependencias

Maven las descarga a partir de `pom.xml`: Spring Boot 4.0.2, Spring Web MVC, JDBC (`JdbcTemplate`), MySQL Connector/J, Validation, Actuator y Lombok. También incluye DevTools para desarrollo y Spring Web MVC Test para pruebas.

Lombok genera getters, setters y otros métodos durante la compilación. No hace falta instalar estas bibliotecas a mano.

## MySQL con Docker

Ejecutá los comandos desde la raíz del backend. Las contraseñas de este ejemplo son solo para desarrollo local.

### 1. Crear el contenedor

```sh
docker run -d --name mibdj-mysql \
  -p 127.0.0.1:3306:3306 \
  -e MYSQL_ROOT_PASSWORD=mibdj_root_local \
  -e MYSQL_DATABASE=juegos_mesadb \
  -e MYSQL_USER=mibdj \
  -e MYSQL_PASSWORD=mibdj_local \
  --mount type=volume,source=mibdj-mysql-data,target=/var/lib/mysql \
  --mount "type=bind,source=$(pwd)/src/main/resources/schema.sql,target=/docker-entrypoint-initdb.d/01-schema.sql,readonly" \
  --mount "type=bind,source=$(pwd)/src/main/resources/data.sql,target=/docker-entrypoint-initdb.d/02-data.sql,readonly" \
  mysql:8.4
```

MySQL importa primero `schema.sql` y después `data.sql`, únicamente cuando el volumen está vacío. Reiniciar el contenedor no vuelve a importar los scripts. Cambiar los archivos SQL o las variables del comando tampoco actualiza una base ya inicializada.

Si el puerto 3306 ya está ocupado, usá `127.0.0.1:3307:3306` y poné `3307` en `DB_URL`.

### 2. Esperar a que termine la carga

```sh
docker exec mibdj-mysql sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" mysql -h 127.0.0.1 -u "$MYSQL_USER" "$MYSQL_DATABASE" -e "SELECT COUNT(*) AS usuarios FROM Usuario;"'
```

La primera carga puede tardar alrededor de un minuto. Si todavía no conecta, esperá unos segundos y repetí el comando. Debe mostrar **5 usuarios**. Podés revisar el arranque con `docker logs mibdj-mysql`.

### 3. Configurar la conexión

```sh
cp .env.example .env
```

Editá `.env` para que coincida con el contenedor:

```properties
DB_URL=jdbc:mysql://127.0.0.1:3306/juegos_mesadb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USR_NAME=mibdj
DB_PWD=mibdj_local
```

Spring Boot lee `.env` desde el directorio de trabajo. Usá formato Java properties, sin comillas ni `export`; una barra invertida literal se escribe `\\`. Las variables de entorno con esos nombres tienen prioridad sobre `.env`. Los tres valores son obligatorios y no hay una URL de respaldo. Reiniciá el backend después de cambiarlos.

`.env` está excluido de Git. El backend no importa los scripts al arrancar: la carga inicial la hace el contenedor.

### Parar y volver a arrancar

```sh
docker stop mibdj-mysql
docker start mibdj-mysql
```

El volumen `mibdj-mysql-data` conserva los datos. El comando `docker run` se usa para crear el contenedor; para las siguientes ejecuciones usá `docker start`.

## Compilar y ejecutar

Compilar desde la raíz del backend, sin necesitar MySQL:

```sh
./mvnw clean compile
```

Con MySQL listo y `.env` configurado, arrancar la aplicación:

```sh
./mvnw spring-boot:run
```

La API queda en `http://localhost:8080`. Comprobá el arranque y la conexión a la base:

```sh
curl http://localhost:8080/actuator/health
```

Debe responder HTTP 200 y mostrar `"status":"UP"`. `Ctrl+C` detiene el backend.

Las rutas, los cuerpos JSON y los ejemplos de uso están en la
[documentación de la API](api.md).
