package com.fdlj.fdlj.dto.response;

import java.util.List;

public record AttendanceRankingResponse(
		Long hinchaId,
		String nombre,
		String apellido,
		long totalPartidos,
		List<AnioAttendance> asistenciasPorAnio
) {

	public record AnioAttendance(int anio, long partidos) {
	}
}
