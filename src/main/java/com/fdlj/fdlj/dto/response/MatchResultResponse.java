package com.fdlj.fdlj.dto.response;

import com.fdlj.fdlj.entity.enums.ResultadoPartido;

public record MatchResultResponse(
		Long matchId,
		Integer golesEquipoA,
		Integer golesEquipoB,
		ResultadoPartido resultado
) {
}
