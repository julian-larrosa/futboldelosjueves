package com.fdlj.fdlj.dto.response;

import com.fdlj.fdlj.entity.enums.TeamSide;

import java.util.List;

public record TeamResponse(
		Long id,
		TeamSide side,
		List<PlayerTeamMemberResponse> jugadores,
		Double ratingPromedio
) {
}
