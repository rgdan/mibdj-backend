-- ============================================================================
-- BASE DE DATOS: Catálogo y Registro de Juegos de Mesa
-- ============================================================================


CREATE DATABASE IF NOT EXISTS juegos_mesadb;
USE juegos_mesadb;

-- ----------------------------------------------------------------------------
-- 1. TABLAS PRINCIPALES / ENTIDADES
-- ----------------------------------------------------------------------------

-- Tabla: Usuario
-- Almacena la información de los usuarios registrados en el sistema.
CREATE TABLE Usuario (
    id_usuario      INT NOT NULL,
    nombre          VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    pais            VARCHAR(255) NOT NULL,
    fecha           DATE NOT NULL,

    PRIMARY KEY (id_usuario)
);

-- Tabla: Juego
-- Catálogo principal de juegos de mesa
CREATE TABLE Juego (
    id_juego          INT NOT NULL,
    titulo            VARCHAR(255) NOT NULL,
    editorial         VARCHAR(255) NOT NULL,
    anio_publicacion  INT NOT NULL,
    min_jugadores     INT NOT NULL,
    max_jugadores     INT NOT NULL,
    duracion_min      INT NOT NULL,

    PRIMARY KEY (id_juego),

    -- Restricciones de validación para valores mayores que cero y coherencia
    CONSTRAINT chk_juego_min_jugadores
        CHECK (min_jugadores > 0),
    CONSTRAINT chk_juego_max_jugadores
        CHECK (max_jugadores >= min_jugadores),
    CONSTRAINT chk_juego_duracion
        CHECK (duracion_min > 0)
);

-- Tabla: Categoria
-- Géneros o categorías de los juegos (Estrategia, Cooperativo, Cartas, etc.).
CREATE TABLE Categoria (
    id_categoria  INT NOT NULL,
    nombre        VARCHAR(255) NOT NULL,

    PRIMARY KEY (id_categoria)
);

-- Tabla: Lugar
-- Lugares  donde se juegan las partidas.
CREATE TABLE Lugar (
    id_lugar   INT NOT NULL AUTO_INCREMENT,
    id_usuario INT NOT NULL,      -- Enlazamos el jugar un usuario específico
    nombre     VARCHAR(255) NOT NULL,
    direccion  VARCHAR(255) NOT NULL,

    PRIMARY KEY (id_lugar),

    CONSTRAINT fk_lugar_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES Usuario (id_usuario)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);


-- Tabla: Partida
-- Registro individual de cada evento de juego jugado.
CREATE TABLE Partida (
    id_partida    INT NOT NULL,
    fecha         DATE NOT NULL,
    duracion_min  INT NOT NULL,
    notas         VARCHAR(255), -- Opcional: permite espacio vacio si no hay notas de la partida
    id_usuario    INT NOT NULL,
    id_juego      INT NOT NULL,
    id_lugar      INT NOT NULL,

    PRIMARY KEY (id_partida),

    -- Restricción para asegurar una duración válida
    CONSTRAINT chk_partida_duracion
        CHECK (duracion_min > 0),

    -- Claves foráneas hacia usuario organizador, el juego y el lugar
    CONSTRAINT fk_partida_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES Usuario (id_usuario)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_partida_juego
        FOREIGN KEY (id_juego)
        REFERENCES Juego (id_juego)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_partida_lugar
        FOREIGN KEY (id_lugar)
        REFERENCES Lugar (id_lugar)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);


-- ----------------------------------------------------------------------------
-- 2. TABLAS INTERMEDIAS / RELACIONES MUCHOS A MUCHOS (N:M)
-- ----------------------------------------------------------------------------

-- Relación: Juego_categoria (Juego N ---- N Categoria)
-- Asocia un juego con una o más categorías.
CREATE TABLE Juego_categoria (
    id_juego      INT NOT NULL,
    id_categoria  INT NOT NULL,

    PRIMARY KEY (id_juego, id_categoria),

	CONSTRAINT fk_juego_categoria_juego
        FOREIGN KEY (id_juego)
        REFERENCES Juego (id_juego)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_juego_categoria_categoria
        FOREIGN KEY (id_categoria)
        REFERENCES Categoria (id_categoria)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);


-- Relación: Posee (Usuario N ---- N Juego)
-- Colección personal de juegos adquiridos por cada usuario.
CREATE TABLE Posee (
    id_usuario  INT NOT NULL,
    id_juego    INT NOT NULL,
    fecha       DATE NOT NULL,
    estado      VARCHAR(255) NOT NULL,

    PRIMARY KEY (id_usuario, id_juego),

    -- Valida que la condición del juego sea únicamente 'nuevo', 'usado' o 'prestado'
    CONSTRAINT chk_estado_juego
        CHECK (estado IN ('nuevo', 'usado', 'prestado')),

    CONSTRAINT fk_posee_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES Usuario (id_usuario)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_posee_juego
        FOREIGN KEY (id_juego)
        REFERENCES Juego (id_juego)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- Relación: Desea (Usuario N ---- N Juego)
-- Lista de deseos de juegos que le gustaría adquirir a un usuario.
CREATE TABLE Desea (
    id_usuario  INT NOT NULL,
    id_juego    INT NOT NULL,
    prioridad   INT NOT NULL,
    fecha  DATE NOT NULL,

    PRIMARY KEY (id_usuario, id_juego),

    -- Restringe la prioridad a los niveles 1, 2 o 3
    CONSTRAINT chk_prioridad_juego
        CHECK (prioridad IN (1, 2, 3)),

    CONSTRAINT fk_desea_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES Usuario (id_usuario)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_desea_juego
        FOREIGN KEY (id_juego)
        REFERENCES Juego (id_juego)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- Relación: Valora (Usuario N ---- N Juego)
-- Reseñas y calificaciones otorgadas por los usuarios a los juegos.
CREATE TABLE Valora (
    id_usuario    INT NOT NULL,
    id_juego      INT NOT NULL,
    calificacion  INT NOT NULL,
    comentario    VARCHAR(255) , -- Opcional: permite espacio vacio si el usuario solo califica sin comentar

    PRIMARY KEY (id_usuario, id_juego),

    -- Limita la calificación a un rango del 1 al 10 inclusive
    CONSTRAINT chk_calificacion_juego
        CHECK (calificacion BETWEEN 1 AND 10),

    CONSTRAINT fk_valora_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES Usuario (id_usuario)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_valora_juego
        FOREIGN KEY (id_juego)
        REFERENCES Juego (id_juego)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- Relación: Participa (Usuario N ---- N Partida)
-- Participantes de cada partida jugada, sus puntos e indicación de si ganaron.
CREATE TABLE Participa (
    id_usuario  INT NOT NULL,
    id_partida  INT NOT NULL,
    puntos      INT NOT NULL,
    ganador     BOOl,

    PRIMARY KEY (id_partida, id_usuario),

    -- Validaciones para puntuación no negativa
    CONSTRAINT chk_puntos_posi_participa
        CHECK (puntos >= 0),


    CONSTRAINT fk_participa_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES Usuario (id_usuario)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_participa_partida
        FOREIGN KEY (id_partida)
        REFERENCES Partida (id_partida)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);


-- ----------------------------------------------------------------------------
-- 3. Vista propuesta
-- ----------------------------------------------------------------------------


CREATE VIEW vw_partidas_usuario AS
SELECT
    u.nombre AS usuario,
    p.id_partida AS partida,
    j.titulo AS juego,
    (
        SELECT COUNT(*)
        FROM Participa par
        WHERE par.id_partida = p.id_partida
    ) AS participantes,
    l.nombre AS lugar
FROM Partida p
JOIN Usuario u ON p.id_usuario = u.id_usuario
JOIN Juego j   ON p.id_juego = j.id_juego
JOIN Lugar l   ON p.id_lugar = l.id_lugar;
