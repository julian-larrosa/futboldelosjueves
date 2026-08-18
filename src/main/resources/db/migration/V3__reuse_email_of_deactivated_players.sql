-- V3: Reutilización de email de jugadores soft-deleted.
-- Se reemplaza la UNIQUE total de players.email por una UNIQUE parcial que solo
-- aplica a jugadores activos (activo = true). Esto permite reutilizar el email
-- de jugadores desactivados y evita colisiones con usuarios de la hinchada.

ALTER TABLE players DROP CONSTRAINT ukpnrwm9bkjel7qss1ekm05j953;

CREATE UNIQUE INDEX uk_players_email_active ON players (email) WHERE activo = true;