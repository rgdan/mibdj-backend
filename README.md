<div align="center">

<h1>MiBDJ</h1>
<p>Mi base de datos de juegos de mesa</p>
<p><strong>Backend · Java 21 · Spring Boot · MySQL</strong></p>

[Configurar el proyecto](docs/setup.md) · [Consultar la API](docs/api.md) · [Ver el frontend](https://github.com/rgdan/mibdj-frontend)

</div>

---

Un catálogo de juegos de mesa con colección personal, lista de deseos y
valoraciones. Registrá tus partidas, los participantes, sus puntos y ganadores;
consultá el historial y las estadísticas de juego de cada usuario.

[Inicio local](#inicio-local) · [Estructura](#estructura) · [Base de datos](#base-de-datos)

## Inicio local

[Compilar, levantar MySQL y arrancar el backend](docs/setup.md).

## Estructura

Las solicitudes pasan por controladores REST, servicios y repositorios.
Los repositorios ejecutan SQL parametrizado con `JdbcTemplate` contra MySQL.

```text
src/main/
├── java/teccr/mibdj/
│   ├── api/          Controladores y cuerpos de entrada
│   ├── service/      Validaciones y transacciones
│   ├── repository/   Interfaces de acceso a datos
│   │   └── jdbc/     Implementaciones SQL con JdbcTemplate
│   └── data/         Objetos de respuesta
└── resources/
    ├── application.properties
    ├── schema.sql    Tablas, restricciones y vista
    └── data.sql      Datos iniciales
```

## Base de datos

`juegos_mesadb` contiene 10 tablas y la vista `vw_partidas_usuario`, usada en
el historial. El seed incluye 5 usuarios y 8 juegos de mesa.

[Esquema SQL](src/main/resources/schema.sql) · [Datos iniciales](src/main/resources/data.sql) · [Diagrama editable](docs/diagrama-bd.drawio)

<details>
<summary>Ver el diagrama de la base de datos</summary>

![Diagrama lógico de MiBDJ con tablas, claves y relaciones](docs/diagrama-bd.png)

</details>

Los contratos, ejemplos y límites actuales están en la [referencia de la API](docs/api.md).
