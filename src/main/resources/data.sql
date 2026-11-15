-- ----------------------------------------------------------------------------
-- Usuario
-- ----------------------------------------------------------------------------
INSERT INTO Usuario (id_usuario, nombre, email, pais, fecha) VALUES
(1, 'Ana Mora',        'ana.mora@gmail.com',        'Costa Rica', '2025-11-03'),
(2, 'Luis Vargas',     'luis.vargas@gmail.com',     'Costa Rica', '2025-11-20'),
(3, 'María Fernández', 'maria.fernandez@gmail.com', 'México',     '2026-01-15'),
(4, 'Carlos Jiménez',  'carlos.jimenez@gmail.com',  'Costa Rica', '2026-02-02'),
(5, 'Sofía Ramírez',   'sofia.ramirez@gmail.com',   'Colombia',   '2026-02-18');

-- ----------------------------------------------------------------------------
-- Juego
-- ----------------------------------------------------------------------------
INSERT INTO Juego (id_juego, titulo, editorial, anio_publicacion, min_jugadores, max_jugadores, duracion_min) VALUES
(1, 'Catan',        'Kosmos',        1995, 3, 4, 90),
(2, 'Organ Attack', 'Awkward Yeti',  2016, 2, 6, 30),
(3, 'UNO',          'Mattel',        1971, 2, 10, 30),
(4, 'Clue',         'Hasbro',        1949, 3, 6, 45),
(5, 'Sushi Go',     'Gamewright',    2013, 2, 5, 15),
(6, 'Carcassonne',  'Hans im Glück', 2000, 2, 5, 45),
(7, 'Azul',         'Plan B Games',  2017, 2, 4, 40),
(8, 'Pandemic',     'Z-Man Games',   2008, 2, 4, 45);

-- ----------------------------------------------------------------------------
-- Categoria
-- ----------------------------------------------------------------------------
INSERT INTO Categoria (id_categoria, nombre) VALUES
(1, 'Estrategia'),
(2, 'Cooperativo'),
(3, 'Cartas'),
(4, 'Familiar'),
(5, 'Fiesta'),
(6, 'Deducción');

-- ----------------------------------------------------------------------------
-- Lugar
-- ----------------------------------------------------------------------------
INSERT INTO Lugar (id_lugar, id_usuario, nombre, direccion) VALUES
(1,1, 'Casa de Ana',      'Alajuela centro, 200 m norte de la catedral'),
(2,4, 'Café Dados y Fichas',  'Heredia, 100 m sur del parque central'),
(3, 5,  'Laimi',            'Sede Interuniversitaria de Alajuela'),
(4, 2, 'Casa de Luis',      'San José, Barrio Escalante'),
(5, 3, 'Casa de Pedro',       'Cartago, 50 m este de las Ruinas');

-- ----------------------------------------------------------------------------
-- Juego_categoria
-- ----------------------------------------------------------------------------
INSERT INTO Juego_categoria (id_juego, id_categoria) VALUES
(1, 1), (1, 4),
(2, 3), (2, 5),
(3, 3), (3, 4),
(4, 6), (4, 4),
(5, 3), (5, 4),
(6, 1), (6, 4),
(7, 1), (7, 4),
(8, 2), (8, 1);

-- ----------------------------------------------------------------------------
-- Partida (id_lugar NULL = sin lugar; NULL en notas = sin notas)
-- ----------------------------------------------------------------------------
INSERT INTO Partida (id_partida, fecha, duracion_min, notas, id_usuario, id_juego, id_lugar) VALUES
(1,  '2026-03-07', 100, 'Partida muy reñida, se definió en el último turno', 1, 1, 1),  -- Catan
(2,  '2026-03-14',  50, NULL,                                               1, 6, 1),  -- Carcassonne
(3,  '2026-03-21',  55, 'Ganamos por un turno de diferencia',                2, 8, 4),  -- Pandemic
(4,  '2026-04-04',  65, 'Se resolvió con la última acusación',               1, 4, 1),  -- Clue
(5,  '2026-04-18',  45, NULL,                                               3, 7, 5),  -- Azul
(6,  '2026-05-02',  35, 'Hubo muchos +4 al final',                           2, 3, 4),  -- UNO
(7,  '2026-05-16',  95, NULL,                                               1, 1, 1),  -- Catan
(8,  '2026-06-06',  20, 'Partida rápida de tres rondas',                     4, 5, 2),  -- Sushi Go
(9,  '2026-06-20',  40, 'Primera vez que se juega Organ Attack',             5, 2, 3),  -- Organ Attack
(10, '2026-07-11',  50, NULL,                                               1, 6, 1),  -- Carcassonne
(11, '2026-08-01',  60, 'Perdimos, se salió de control un brote',            2, 8, 4),  -- Pandemic
(12, '2026-09-05',  40, NULL,                                               3, 7, 5);  -- Azul

-- ----------------------------------------------------------------------------
-- Participa (usuario, partida, puntos, ganador)
-- ----------------------------------------------------------------------------
INSERT INTO Participa (id_usuario, id_partida, puntos, ganador) VALUES
-- P1 Catan
(1, 1, 10, TRUE), (2, 1, 8, FALSE), (3, 1, 7, FALSE), (4, 1, 6, FALSE),
-- P2 Carcassonne
(1, 2, 87, TRUE), (2, 2, 74, FALSE),
-- P3 Pandemic (cooperativo: puntos = enfermedades curadas)
(2, 3, 4, TRUE), (1, 3, 4, TRUE), (4, 3, 4, TRUE),
-- P4 Clue (puntos: 1 = resolvió el caso, 0 = no)
(1, 4, 0, FALSE), (2, 4, 1, TRUE), (5, 4, 0, FALSE),
-- P5 Azul
(3, 5, 78, TRUE), (4, 5, 65, FALSE),
-- P6 UNO
(2, 6, 320, FALSE), (1, 6, 510, TRUE), (5, 6, 280, FALSE), (3, 6, 190, FALSE),
-- P7 Catan
(1, 7, 9, FALSE), (3, 7, 10, TRUE), (4, 7, 7, FALSE),
-- P8 Sushi Go
(4, 8, 41, TRUE), (1, 8, 38, FALSE), (2, 8, 30, FALSE), (3, 8, 27, FALSE),
-- P9 Organ Attack (puntos = órganos que le quedaron)
(5, 9, 3, TRUE), (2, 9, 0, FALSE), (1, 9, 0, FALSE),
-- P10 Carcassonne
(1, 10, 65, FALSE), (3, 10, 71, TRUE),
-- P11 Pandemic (derrota)
(2, 11, 2, FALSE), (1, 11, 2, FALSE), (3, 11, 2, FALSE),
-- P12 Azul
(3, 12, 81, FALSE), (1, 12, 90, TRUE);

-- ----------------------------------------------------------------------------
-- Posee (colección de juegos)
-- ----------------------------------------------------------------------------
INSERT INTO Posee (id_usuario, id_juego, fecha, estado) VALUES
(1, 1, '2025-12-25', 'nuevo'),
(1, 6, '2025-12-10', 'usado'),
(1, 4, '2026-01-12', 'nuevo'),
(1, 7, '2026-02-14', 'nuevo'),
(2, 8, '2025-12-05', 'nuevo'),
(2, 3, '2026-01-08', 'usado'),
(2, 2, '2026-03-30', 'nuevo'),
(3, 7, '2026-02-20', 'nuevo'),
(3, 1, '2026-03-01', 'prestado'),
(4, 5, '2026-03-15', 'nuevo'),
(4, 1, '2026-04-02', 'usado'),
(5, 2, '2026-05-22', 'usado');

-- ----------------------------------------------------------------------------
-- Desea (prioridad: 1 baja, 2 media, 3 alta)
-- ----------------------------------------------------------------------------
INSERT INTO Desea (id_usuario, id_juego, prioridad, fecha) VALUES
(1, 2, 3, '2026-06-22'),
(1, 8, 2, '2026-04-01'),
(2, 7, 2, '2026-04-20'),
(2, 5, 1, '2026-06-08'),
(3, 8, 3, '2026-08-02'),
(4, 6, 1, '2026-07-12'),
(5, 4, 2, '2026-04-06');

-- ----------------------------------------------------------------------------
-- Valora (calificación 1-10; NULL es que no tiene comentario)
-- ----------------------------------------------------------------------------
INSERT INTO Valora (id_usuario, id_juego, calificacion, comentario) VALUES
(1, 1, 9,  'Excelente, siempre genera negociaciones divertidas y es muy estratégico'),
(1, 6, 8,  NULL),
(1, 4, 7,  'Buenas partidas de deducción'),
(1, 7, 9,  'Piezas bastante bonitas'),
(2, 8, 9,  'El mejor cooperativo que he probado'),
(2, 3, 8,  NULL),
(2, 2, 10, 'Muy divertido con 4 o más jugadores'),
(3, 7, 8,  'Fácil de enseñar'),
(3, 1, 7,  NULL),
(4, 5, 6,  'Rápido y fácil de enseñar'),
(5, 2, 9,  NULL);
