package com.fdlj.fdlj.dto.response;

public record PlayerTeamMemberResponse(
		Long playerId,
		String nombre,
		String apellido
) {
}
