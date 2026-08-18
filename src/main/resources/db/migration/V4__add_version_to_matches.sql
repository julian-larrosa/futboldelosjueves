-- V4: Control de concurrencia optimista sobre matches.
-- Agrega la columna version que usa @Version para detectar ediciones concurrentes
-- del ciclo de vida de un partido y responder 409.

ALTER TABLE matches ADD COLUMN version bigint NOT NULL DEFAULT 0;