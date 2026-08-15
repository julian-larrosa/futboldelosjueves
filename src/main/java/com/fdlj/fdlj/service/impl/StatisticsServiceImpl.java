package com.fdlj.fdlj.service.impl;

import com.fdlj.fdlj.dto.response.ParticipationResponse;
import com.fdlj.fdlj.dto.response.PlayerStatisticsResponse;
import com.fdlj.fdlj.dto.response.RecentFormResponse;
import com.fdlj.fdlj.entity.MatchParticipation;
import com.fdlj.fdlj.entity.Player;
import com.fdlj.fdlj.entity.Team;
import com.fdlj.fdlj.entity.enums.MatchStatus;
import com.fdlj.fdlj.entity.enums.ResultadoPartido;
import com.fdlj.fdlj.entity.enums.TeamSide;
import com.fdlj.fdlj.exception.ResourceNotFoundException;
import com.fdlj.fdlj.mapper.MatchParticipationMapper;
import com.fdlj.fdlj.repository.MatchParticipationRepository;
import com.fdlj.fdlj.repository.MatchRepository;
import com.fdlj.fdlj.repository.PlayerRepository;
import com.fdlj.fdlj.repository.RatingRepository;
import com.fdlj.fdlj.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

	private static final int DEFAULT_RECENT_LIMIT = 5;

	private final MatchRepository matchRepository;
	private final PlayerRepository playerRepository;
	private final MatchParticipationRepository participationRepository;
	private final RatingRepository ratingRepository;
	private final MatchParticipationMapper participationMapper;

	@Override
	@Transactional(readOnly = true)
	public List<ParticipationResponse> getMatchStatistics(Long matchId) {
		matchRepository.findById(matchId)
				.orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + matchId));
		return participationRepository.findByMatchIdOrderByIdAsc(matchId).stream()
				.map(participationMapper::toResponse)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public PlayerStatisticsResponse getPlayerStatistics(Long playerId) {
		findActivePlayer(playerId);
		List<MatchParticipation> played = participationsPlayed(playerId);
		Aggregated aggregated = aggregate(played);
		double porcentaje = aggregated.partidos() > 0
				? Math.round(aggregated.victorias() * 100.0 / aggregated.partidos())
				: 0.0;
		return new PlayerStatisticsResponse(
				playerId,
				aggregated.partidos(),
				aggregated.victorias(),
				aggregated.derrotas(),
				aggregated.empates(),
				aggregated.goles(),
				aggregated.asistencias(),
				ratingRepository.averageByCalificadoId(playerId),
				porcentaje,
				getRecentForm(playerId, DEFAULT_RECENT_LIMIT)
		);
	}

	@Override
	@Transactional(readOnly = true)
	public RecentFormResponse getRecentForm(Long playerId, int limit) {
		findActivePlayer(playerId);
		List<MatchParticipation> played = participationsPlayed(playerId);
		int size = Math.min(Math.max(limit, 1), played.size());
		Aggregated aggregated = aggregate(played.subList(0, size));
		return new RecentFormResponse(
				aggregated.partidos(),
				aggregated.victorias(),
				aggregated.derrotas(),
				aggregated.empates(),
				aggregated.goles(),
				aggregated.asistencias(),
				ratingRepository.averageByCalificadoId(playerId)
		);
	}

	private List<MatchParticipation> participationsPlayed(Long playerId) {
		return participationRepository
				.findByPlayerIdAndMatchEstadoAndJugoEfectivamenteTrueOrderByMatchFechaHoraDesc(
						playerId, MatchStatus.FINALIZADO);
	}

	private Aggregated aggregate(List<MatchParticipation> played) {
		int victorias = 0;
		int derrotas = 0;
		int empates = 0;
		int goles = 0;
		int asistencias = 0;
		for (MatchParticipation participation : played) {
			goles += participation.getGoles();
			asistencias += participation.getAsistencias();
			ResultadoPartido resultado = resultFor(participation);
			TeamSide side = teamSideOf(participation);
			if (side == null) {
				continue;
			}
			if (resultado == ResultadoPartido.EMPATE) {
				empates++;
			} else if ((side == TeamSide.EQUIPO_A && resultado == ResultadoPartido.GANA_EQUIPO_A)
					|| (side == TeamSide.EQUIPO_B && resultado == ResultadoPartido.GANA_EQUIPO_B)) {
				victorias++;
			} else {
				derrotas++;
			}
		}
		return new Aggregated(played.size(), victorias, derrotas, empates, goles, asistencias);
	}

	private ResultadoPartido resultFor(MatchParticipation participation) {
		int golesA = participation.getMatch().getGolesEquipoA();
		int golesB = participation.getMatch().getGolesEquipoB();
		if (golesA > golesB) {
			return ResultadoPartido.GANA_EQUIPO_A;
		}
		if (golesB > golesA) {
			return ResultadoPartido.GANA_EQUIPO_B;
		}
		return ResultadoPartido.EMPATE;
	}

	private TeamSide teamSideOf(MatchParticipation participation) {
		Team team = participation.getTeam();
		return team != null ? team.getSide() : null;
	}

	private Player findActivePlayer(Long id) {
		return playerRepository.findByIdAndActivoTrue(id)
				.orElseThrow(() -> new ResourceNotFoundException("Jugador activo no encontrado con id: " + id));
	}

	private record Aggregated(
			int partidos,
			int victorias,
			int derrotas,
			int empates,
			int goles,
			int asistencias
	) {
	}
}
