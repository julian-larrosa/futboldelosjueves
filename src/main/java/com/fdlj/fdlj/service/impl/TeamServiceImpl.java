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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamServiceImpl implements TeamService {

	private static final int MIN_PLAYERS = 2;

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

		Map<Long, Double> averages = loadRatingAverages();

		List<MatchParticipation> draft = new ArrayList<>(convocados);
		draft.sort(Comparator
				.comparing((MatchParticipation p) -> averages.getOrDefault(p.getPlayer().getId(), 0.0),
						Comparator.reverseOrder())
				.thenComparing(p -> p.getId()));

		Team teamA = new Team();
		teamA.setMatch(match);
		teamA.setSide(TeamSide.EQUIPO_A);
		teamRepository.save(teamA);

		Team teamB = new Team();
		teamB.setMatch(match);
		teamB.setSide(TeamSide.EQUIPO_B);
		teamRepository.save(teamB);

		List<MatchParticipation> toSave = new ArrayList<>();
		for (int i = 0; i < draft.size(); i++) {
			MatchParticipation participation = draft.get(i);
			int roundOfTwo = i / 2;
			boolean reverse = (roundOfTwo % 2 == 1);
			boolean firstPickOfRound = (i % 2 == 0);
			boolean pickA = reverse ? !firstPickOfRound : firstPickOfRound;
			participation.setTeam(pickA ? teamA : teamB);
			toSave.add(participation);
		}
		participationRepository.saveAll(toSave);

		int teamASize = draft.size() / 2;
		int teamBSize = draft.size() - teamASize;
		log.info("Equipos generados para partido id={}: {} jugadores en Equipo A, {} en Equipo B",
				matchId, teamASize, teamBSize);
		return buildTeamResponses(matchId, averages);
	}

	@Override
	@Transactional(readOnly = true)
	public List<TeamResponse> getTeams(Long matchId) {
		findMatch(matchId);
		return buildTeamResponses(matchId, loadRatingAverages());
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
		TeamSide otherSide = request.teamSide() == TeamSide.EQUIPO_A ? TeamSide.EQUIPO_B : TeamSide.EQUIPO_A;
		long otherCount = teamRepository.findByMatchIdAndSide(matchId, otherSide)
				.map(other -> participationRepository.findByTeamIdOrderByIdAsc(other.getId()).stream()
						.filter(m -> m.getPlayer().isActivo())
						.count())
				.orElse(0L);
		if (targetCount >= otherCount + 1) {
			throw new InvalidMatchStateException("El equipo " + request.teamSide()
					+ " ya tiene " + targetCount + " jugadores y no puede tener más de 1 de diferencia con el otro equipo");
		}
		participation.setTeam(target);
		participationRepository.save(participation);
		log.info("Jugador id={} asignado manualmente a {} en partido id={}", playerId, request.teamSide(), matchId);
		return buildTeamResponses(matchId, loadRatingAverages());
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
		Map<Long, Double> averages = loadRatingAverages();
		Double avgA = teamAverageRating(membersOf(teamA), averages);
		Double avgB = teamAverageRating(membersOf(teamB), averages);
		double a = avgA != null ? avgA : 0.0;
		double b = avgB != null ? avgB : 0.0;
		double diferencia = Math.round(Math.abs(a - b) * 100.0) / 100.0;
		return new TeamBalanceResponse(avgA, avgB, diferencia);
	}

	private List<TeamResponse> buildTeamResponses(Long matchId, Map<Long, Double> averages) {
		return teamRepository.findByMatchIdOrderBySideAsc(matchId).stream()
				.map(team -> {
					List<MatchParticipation> members = membersOf(team);
					return teamMapper.toResponse(team, members, teamAverageRating(members, averages));
				})
				.toList();
	}

	private List<MatchParticipation> membersOf(Team team) {
		return participationRepository.findByTeamIdOrderByIdAsc(team.getId());
	}

	private Double teamAverageRating(List<MatchParticipation> members, Map<Long, Double> averages) {
		List<MatchParticipation> activos = members.stream()
				.filter(m -> m.getPlayer().isActivo())
				.toList();
		if (activos.isEmpty()) {
			return null;
		}
		double sum = activos.stream()
				.mapToDouble(m -> averages.getOrDefault(m.getPlayer().getId(), 0.0))
				.sum();
		return Math.round((sum / activos.size()) * 100.0) / 100.0;
	}

	private Map<Long, Double> loadRatingAverages() {
		return ratingRepository.averageGroupedByCalificado().stream()
				.collect(Collectors.toMap(
						row -> ((Number) row[0]).longValue(),
						row -> ((Number) row[1]).doubleValue()));
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
