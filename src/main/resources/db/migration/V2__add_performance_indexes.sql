-- V2: Índices de performance para las consultas más frecuentes de la fase 7-8.
-- Cubren los accesos por FK y por estado+fecha (reemplazo de YEAR() por rangos).

CREATE INDEX idx_mp_player ON match_participations (player_id);
CREATE INDEX idx_mp_team ON match_participations (team_id);
CREATE INDEX idx_rating_calificado ON ratings (calificado_id);
CREATE INDEX idx_attendance_hincha ON match_attendances (hincha_id);
CREATE INDEX idx_comments_match_created ON match_comments (match_id, created_at);
CREATE INDEX idx_matches_estado_fecha ON matches (estado, fecha_hora);