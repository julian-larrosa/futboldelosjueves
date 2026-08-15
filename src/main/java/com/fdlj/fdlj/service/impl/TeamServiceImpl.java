package com.fdlj.fdlj.service.impl;

import com.fdlj.fdlj.dto.request.TeamAssignmentRequest;
import com.fdlj.fdlj.dto.response.TeamBalanceResponse;
import com.fdlj.fdlj.dto.response.TeamResponse;
import com.fdlj.fdlj.entity.Match;
import com.fdlj.fdlj.entity.MatchParticipation;
import com.fdlj.fdlj.entity.Team;
import com.fdlj.fdlj.entity.enums.MatchStatus;
import com.fdlj.fdlj.entity.enums.TeamSide;
import com.fdlj.fdlj.exception.InvalidMatchStateException;
import com.fdlj.fdlj.exception.ResourceAlreadyExistsException;
import com.fdlj.fdlj.exception.ResourceNotFoundException;
import com.fdlj.fdlj.mapper.TeamMapper;
import com.fdlj.fdlj.repository.MatchParticipationRepository;
import com.fdlj.fdlj.repository.MatchRepository;
import com.fdlj.fdlj.repository.RatingRepository;
import com.fdlj.fdlj.repository.TeamRepository;
import com.fdlj.fdlj.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

	private static final int MIN_PLAYERS = 10;
	private static final int MAX_PLAYERS_PER_TEAM = 5;

	private final MatchRepository matchRepository;
	private final MatchParticipationRepository participationRepository;
	private final TeamRepository teamRepository;
	private final RatingRepository ratingRepository;
	private final TeamMapper teamMapper;

	@Override
	@Transactional
	public List<TeamResponse> generateTeams(Long matchId) {
		Match match = findMatch(matchId);
		if (match.getEstado() != MatchStatus.CONVOCATORIA_CERRADA) {
			throw new InvalidMatchStateException("Los equipos solo pueden generarse con la convocatoria cerrada");
		}
		List<MatchParticipation> convocados = participationRepository.findByMatchIdOrderByIdAsc(matchId).stream()
				.filter(p -> p.getPlayer().isActivo())
				.toList();
		if (convocados.size() < MIN_PLAYERS) {
			throw new InvalidMatchStateException("Se necesitan al menos " + MIN_PLAYERS
					+ " jugadores convocados para generar equipos (actuales: " + convocados.size() + ")");
		}

		clearTeams(matchId);

		List<MatchParticipation> draft = new ArrayList<>(convocados);
		draft.sort(Comparator
				.comparing(this::averageRating, Comparator.reverseOrder())
				.thenComparing(p -> p.getId()));

		Team teamA = new Team();
		teamA.setMatch(match);
		teamA.setSide(TeamSide.EQUIPO_A);
		teamRepository.save(teamA);

		Team teamB = new Team();
		teamB.setMatch(match);
		teamB.setSide(TeamSide.EQUIPO_B);
		teamRepository.save(teamB);

		int selected = Math.min(draft.size(), MAX_PLAYERS_PER_TEAM * 2);
		List<MatchParticipation> toSave = new ArrayList<>();
		for (int i = 0; i < selected; i++) {
			MatchParticipation participation = draft.get(i);
			int round = i / MAX_PLAYERS_PER_TEAM;
			int index = i % MAX_PLAYERS_PER_TEAM;
			boolean teamAFirst = round % 2 == 0;
			boolean pickTeamA = teamAFirst ? (index % 2 == 0) : (index % 2 == 1);
			participation.setTeam(pickTeamA ? teamA : teamB);
			toSave.add(participation);
		}
		participationRepository.saveAll(toSave);

		return buildTeamResponses(matchId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<TeamResponse> getTeams(Long matchId) {
		findMatch(matchId);
		return buildTeamResponses(matchId);
	}

	@Override
	@Transactional
	public List<TeamResponse> assignPlayer(Long matchId, Long playerId, TeamAssignmentRequest request) {
		Match match = findMatch(matchId);
		if (match.getEstado() != MatchStatus.CONVOCATORIA_CERRADA) {
			throw new InvalidMatchStateException("La composición de equipos solo puede modificarse con la convocatoria cerrada");
		}
		MatchParticipation participation = participationRepository.findByMatchIdAndPlayerId(matchId, playerId)
				.orElseThrow(() -> new ResourceNotFoundException("El jugador no está convocado para este partido"));
		if (!participation.getPlayer().isActivo()) {
			throw new InvalidMatchStateException("El jugador está desactivado y no puede integrar un equipo");
		}
		if (participation.getTeam() != null && participation.getTeam().getSide() == request.teamSide()) {
			throw new ResourceAlreadyExistsException("El jugador ya pertenece al equipo " + request.teamSide());
		}
		Team target = teamRepository.findByMatchIdAndSide(matchId, request.teamSide())
				.orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado para el partido"));
		long targetCount = participationRepository.findByTeamIdOrderByIdAsc(target.getId()).stream()
				.filter(m -> m.getPlayer().isActivo())
				.count();
		if (targetCount >= MAX_PLAYERS_PER_TEAM) {
			throw new InvalidMatchStateException("El equipo " + request.teamSide()
					+ " ya tiene " + MAX_PLAYERS_PER_TEAM + " jugadores");
		}
		participation.setTeam(target);
		participationRepository.save(participation);
		return buildTeamResponses(matchId);
	}

	@Override
	@Transactional(readOnly = true)
	public TeamBalanceResponse getTeamBalance(Long matchId) {
		findMatch(matchId);
		List<Team> teams = teamRepository.findByMatchIdOrderBySideAsc(matchId);
		if (teams.size() < 2) {
			throw new InvalidMatchStateException("Los equipos todavía no fueron generados");
		}
		Team teamA = teams.stream().filter(t -> t.getSide() == TeamSide.EQUIPO_A).findFirst().orElseThrow();
		Team teamB = teams.stream().filter(t -> t.getSide() == TeamSide.EQUIPO_B).findFirst().orElseThrow();
		Double avgA = teamAverageRating(membersOf(teamA));
		Double avgB = teamAverageRating(membersOf(teamB));
		double a = avgA != null ? avgA : 0.0;
		double b = avgB != null ? avgB : 0.0;
		double diferencia = Math.round(Math.abs(a - b) * 100.0) / 100.0;
		return new TeamBalanceResponse(avgA, avgB, diferencia);
	}

	private List<TeamResponse> buildTeamResponses(Long matchId) {
		return teamRepository.findByMatchIdOrderBySideAsc(matchId).stream()
				.map(team -> {
					List<MatchParticipation> members = membersOf(team);
					return teamMapper.toResponse(team, members, teamAverageRating(members));
				})
				.toList();
	}

	private List<MatchParticipation> membersOf(Team team) {
		return participationRepository.findByTeamIdOrderByIdAsc(team.getId());
	}

	private Double teamAverageRating(List<MatchParticipation> members) {
		List<MatchParticipation> activos = members.stream()
				.filter(m -> m.getPlayer().isActivo())
				.toList();
		if (activos.isEmpty()) {
			return null;
		}
		double sum = activos.stream().mapToDouble(this::averageRating).sum();
		return Math.round((sum / activos.size()) * 100.0) / 100.0;
	}

	private double averageRating(MatchParticipation participation) {
		Double avg = ratingRepository.averageByCalificadoId(participation.getPlayer().getId());
		return avg != null ? avg : 0.0;
	}

	private void clearTeams(Long matchId) {
		List<MatchParticipation> participations = participationRepository.findByMatchIdOrderByIdAsc(matchId);
		participations.forEach(p -> p.setTeam(null));
		participationRepository.saveAll(participations);
		teamRepository.deleteByMatchId(matchId);
		teamRepository.flush();
	}

	private Match findMatch(Long id) {
		return matchRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + id));
	}
}
