-- V6: Seed de 10 jugadores de prueba con atributos aleatorios.

WITH seed_players AS (
    INSERT INTO players (nombre, apellido, email, posicion, activo, user_id)
    VALUES
    ('Mateo',       'González',   'mateo.gonzalez@test.com',    'ARQUERO',       true, NULL),
    ('Santiago',    'López',      'santiago.lopez@test.com',    'ARQUERO',       true, NULL),
    ('Valentín',    'Martínez',   'valentin.martinez@test.com', 'DEFENSOR',      true, NULL),
    ('Tomás',       'Rodríguez',  'tomas.rodriguez@test.com',   'DEFENSOR',      true, NULL),
    ('Lucas',       'Fernández',  'lucas.fernandez@test.com',   'DEFENSOR',      true, NULL),
    ('Mateo',       'García',     'mateo.garcia@test.com',      'MEDIOCAMPISTA', true, NULL),
    ('Diego',       'Pérez',      'diego.perez@test.com',       'MEDIOCAMPISTA', true, NULL),
    ('Benjamín',    'Sánchez',    'benjamin.sanchez@test.com',  'MEDIOCAMPISTA', true, NULL),
    ('Nicolás',     'Romero',     'nicolas.romero@test.com',    'DELANTERO',     true, NULL),
    ('Andrés',      'Morales',    'andres.morales@test.com',    'DELANTERO',     true, NULL)
    RETURNING id, email
)
INSERT INTO player_attributes (player_id, attribute_type, current_value)
SELECT sp.id, a.attribute_type, ROUND((RANDOM() * 6 + 3)::numeric, 1)
FROM seed_players sp
CROSS JOIN (VALUES ('TECNICA'), ('FISICO'), ('DEFINICION'), ('MENTALIDAD'), ('PASE')) AS a(attribute_type);