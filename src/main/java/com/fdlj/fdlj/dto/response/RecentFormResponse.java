package com.fdlj.fdlj.dto.response;

public record RecentFormResponse(
		int partidosJugados,
		int victorias,
		int derrotas,
		int empates,
		int goles,
		int asistencias,
		Double ratingPromedio
) {
}
