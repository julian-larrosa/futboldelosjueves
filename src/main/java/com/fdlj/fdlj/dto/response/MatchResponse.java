package com.fdlj.fdlj.dto.response;

import com.fdlj.fdlj.entity.enums.MatchStatus;

import java.time.OffsetDateTime;

public record MatchResponse(
		Long id,
		OffsetDateTime fechaHora,
		String lugar,
		MatchStatus estado,
		Integer golesEquipoA,
		Integer golesEquipoB,
		Integer cantidadConvocados
) {
}
