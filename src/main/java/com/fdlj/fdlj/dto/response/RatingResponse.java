package com.fdlj.fdlj.dto.response;

public record RatingResponse(
		Long id,
		Long matchId,
		Long calificadorId,
		String calificadorNombreCompleto,
		Long calificadoId,
		String calificadoNombreCompleto,
		Integer puntaje
) {
}
