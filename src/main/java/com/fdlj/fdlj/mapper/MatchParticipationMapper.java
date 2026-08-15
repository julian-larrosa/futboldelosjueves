package com.fdlj.fdlj.mapper;

import com.fdlj.fdlj.dto.response.ParticipationResponse;
import com.fdlj.fdlj.entity.MatchParticipation;
import org.springframework.stereotype.Component;

@Component
public class MatchParticipationMapper {

	public ParticipationResponse toResponse(MatchParticipation participation) {
		var team = participation.getTeam();
		var player = participation.getPlayer();
		return new ParticipationResponse(
				participation.getId(),
				player.getId(),
				player.getNombre() + " " + player.getApellido(),
				team != null ? team.getId() : null,
				team != null ? team.getSide() : null,
				participation.getGoles(),
				participation.getAsistencias(),
				participation.getJugoEfectivamente()
		);
	}
}
