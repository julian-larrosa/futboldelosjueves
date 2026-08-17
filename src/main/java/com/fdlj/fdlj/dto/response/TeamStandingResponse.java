package com.fdlj.fdlj.dto.response;

public record TeamStandingResponse(
		Long playerId,
		String nombre,
		String apellido,
		int partidosJugados,
		int victorias,
		int empates,
		int derrotas,
		int golesAFavor,
		int golesEnContra,
		int diferenciaGoles,
		int puntos
) {
}
