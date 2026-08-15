package com.fdlj.fdlj.mapper;

import com.fdlj.fdlj.dto.response.PlayerTeamMemberResponse;
import com.fdlj.fdlj.dto.response.TeamResponse;
import com.fdlj.fdlj.entity.MatchParticipation;
import com.fdlj.fdlj.entity.Team;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class TeamMapper {

	public TeamResponse toResponse(Team team, List<MatchParticipation> members, Double ratingPromedio) {
		return new TeamResponse(
				team.getId(),
				team.getSide(),
				members.stream()
						.sorted(Comparator.comparing(m -> m.getId()))
						.map(m -> new PlayerTeamMemberResponse(
								m.getPlayer().getId(),
								m.getPlayer().getNombre(),
								m.getPlayer().getApellido()))
						.toList(),
				ratingPromedio
		);
	}
}
