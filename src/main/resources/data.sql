INSERT INTO _genre (name) SELECT 'Комедия' WHERE NOT EXISTS (SELECT 1 FROM _genre WHERE name = 'Комедия');
INSERT INTO _genre (name) SELECT 'Драма' WHERE NOT EXISTS (SELECT 1 FROM _genre WHERE name = 'Драма');
INSERT INTO _genre (name) SELECT 'Мультфильм' WHERE NOT EXISTS (SELECT 1 FROM _genre WHERE name = 'Мультфильм');
INSERT INTO _genre (name) SELECT 'Триллер' WHERE NOT EXISTS (SELECT 1 FROM _genre WHERE name = 'Триллер');
INSERT INTO _genre (name) SELECT 'Документальный' WHERE NOT EXISTS (SELECT 1 FROM _genre WHERE name = 'Документальный');
INSERT INTO _genre (name) SELECT 'Боевик' WHERE NOT EXISTS (SELECT 1 FROM _genre WHERE name = 'Боевик');

INSERT INTO _mpa (name) SELECT 'G' WHERE NOT EXISTS (SELECT 1 FROM _mpa WHERE name = 'G');
INSERT INTO _mpa (name) SELECT 'PG' WHERE NOT EXISTS (SELECT 1 FROM _mpa WHERE name = 'PG');
INSERT INTO _mpa (name) SELECT 'PG-13' WHERE NOT EXISTS (SELECT 1 FROM _mpa WHERE name = 'PG-13');
INSERT INTO _mpa (name) SELECT 'R' WHERE NOT EXISTS (SELECT 1 FROM _mpa WHERE name = 'R');
INSERT INTO _mpa (name) SELECT 'NC-17' WHERE NOT EXISTS (SELECT 1 FROM _mpa WHERE name = 'NC-17');

INSERT INTO _friend_status (name) SELECT 'Запрошен' WHERE NOT EXISTS (SELECT 1 FROM _friend_status WHERE name = 'Запрошен');
INSERT INTO _friend_status (name) SELECT 'Подтвержден' WHERE NOT EXISTS (SELECT 1 FROM _friend_status WHERE name = 'Подтвержден');