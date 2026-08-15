package com.fdlj.fdlj.dto.response;

import com.fdlj.fdlj.entity.enums.TeamSide;

public record ParticipationResponse(
		Long id,
		Long playerId,
		String playerNombreCompleto,
		Long teamId,
		TeamSide teamSide,
		Integer goles,
		Integer asistencias,
		Boolean jugoEfectivamente
) {
}
