package com.fdlj.fdlj.dto.response;

public record AttendanceStatisticsResponse(
		long totalHinchas,
		long totalAsistencias,
		double promedioPorPartido
) {
}
