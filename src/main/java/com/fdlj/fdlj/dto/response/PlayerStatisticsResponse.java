package com.fdlj.fdlj.dto.response;

public record PlayerStatisticsResponse(
		Long playerId,
		int partidosJugados,
		int victorias,
		int derrotas,
		int empates,
		int goles,
		Double ratingPromedio,
		Double porcentajeVictorias,
		RecentFormResponse rendimientoReciente
) {
}
