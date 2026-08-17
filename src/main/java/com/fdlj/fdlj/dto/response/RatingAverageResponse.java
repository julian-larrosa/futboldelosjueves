package com.fdlj.fdlj.dto.response;

public record RatingAverageResponse(
		Long playerId,
		String nombre,
		String apellido,
		Double promedio,
		long cantidadCalificaciones
) {
}
