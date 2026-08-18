package com.fdlj.fdlj.service.impl;

import com.fdlj.fdlj.entity.MatchParticipation;
import com.fdlj.fdlj.entity.enums.TeamSide;
import com.fdlj.fdlj.exception.InvalidMatchStateException;
import com.fdlj.fdlj.repository.MatchParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoalsConsistencyValidator {

	private final MatchParticipationRepository participationRepository;

	public void validateGoals(Long matchId, int golesEquipoA, int golesEquipoB) {
		var participations = participationRepository.findByMatchIdAndJugoEfectivamenteTrueWithDetails(matchId);
		int individualGoalsA = participations.stream()
				.filter(p -> p.getTeam() != null && p.getTeam().getSide() == TeamSide.EQUIPO_A)
				.mapToInt(MatchParticipation::getGoles).sum();
		int individualGoalsB = participations.stream()
				.filter(p -> p.getTeam() != null && p.getTeam().getSide() == TeamSide.EQUIPO_B)
				.mapToInt(MatchParticipation::getGoles).sum();
		if (individualGoalsA > golesEquipoA || individualGoalsB > golesEquipoB) {
			throw new InvalidMatchStateException(
					"Los goles individuales registrados (" + individualGoalsA + "-" + individualGoalsB
							+ ") exceden los goles del partido (" + golesEquipoA + "-" + golesEquipoB + ")");
		}
	}
}
