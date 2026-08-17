package com.fdlj.fdlj.dto.response;

public record TopScorerResponse(
		Long playerId,
		String nombre,
		String apellido,
		int goles,
		int partidosJugados
) {
}
