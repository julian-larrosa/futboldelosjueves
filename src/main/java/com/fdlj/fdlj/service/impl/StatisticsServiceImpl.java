package com.fdlj.fdlj.service.impl;

import com.fdlj.fdlj.dto.response.ParticipationResponse;
import com.fdlj.fdlj.dto.response.PlayerStatisticsResponse;
import com.fdlj.fdlj.dto.response.RatingAverageResponse;
import com.fdlj.fdlj.dto.response.RecentFormResponse;
import com.fdlj.fdlj.dto.response.TeamStandingResponse;
import com.fdlj.fdlj.dto.response.TopScorerResponse;
import com.fdlj.fdlj.entity.Match;
import com.fdlj.fdlj.entity.MatchParticipation;
import com.fdlj.fdlj.entity.Player;
import com.fdlj.fdlj.entity.PlayerAttribute;
import com.fdlj.fdlj.entity.Rating;
import com.fdlj.fdlj.entity.Team;
import com.fdlj.fdlj.entity.enums.MatchStatus;
import com.fdlj.fdlj.entity.enums.ResultadoPartido;
import com.fdlj.fdlj.entity.enums.TeamSide;
import com.fdlj.fdlj.exception.InvalidMatchStateException;
import com.fdlj.fdlj.exception.ResourceNotFoundException;
import com.fdlj.fdlj.mapper.MatchParticipationMapper;
import com.fdlj.fdlj.repository.MatchParticipationRepository;
import com.fdlj.fdlj.repository.MatchRepository;
import com.fdlj.fdlj.repository.PlayerAttributeRepository;
import com.fdlj.fdlj.repository.PlayerRepository;
import com.fdlj.fdlj.repository.RatingRepository;
import com.fdlj.fdlj.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

	private static final int DEFAULT_RECENT_LIMIT = 3;

	private final MatchRepository matchRepository;
	private final PlayerRepository playerRepository;
	private final MatchParticipationRepository participationRepository;
	private final PlayerAttributeRepository attributeRepository;
	private final RatingRepository ratingRepository;
	private final MatchParticipationMapper participationMapper;

	@Override
	@Transactional(readOnly = true)
	public List<ParticipationResponse> getMatchStatistics(Long matchId) {
		requireFinalizado(matchId);
		return participationRepository.findByMatchIdOrderByIdAsc(matchId).stream()
				.map(participationMapper::toResponse)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public PlayerStatisticsResponse getPlayerStatistics(Long playerId, Integer year) {
		findActivePlayer(playerId);
		List<MatchParticipation> played = participationsPlayed(playerId, year);
		Aggregated aggregated = aggregate(played);
		double porcentaje = aggregated.partidos() > 0
				? Math.round(aggregated.victorias() * 100.0 / aggregated.partidos())
				: 0.0;
		Double ratingPromedio = calculateAttributeAverage(playerId);
		return new PlayerStatisticsResponse(
				playerId,
				aggregated.partidos(),
				aggregated.victorias(),
				aggregated.derrotas(),
				aggregated.empates(),
				aggregated.goles(),
				ratingPromedio,
				porcentaje,
				getRecentForm(playerId, DEFAULT_RECENT_LIMIT, year)
		);
	}

	@Override
	@Transactional(readOnly = true)
	public RecentFormResponse getRecentForm(Long playerId, int limit, Integer year) {
		findActivePlayer(playerId);
		List<MatchParticipation> played = participationsPlayed(playerId, year);
		int size = Math.min(Math.max(limit, 1), played.size());
		Aggregated aggregated = aggregate(played.subList(0, size));
		double indiceForma = aggregated.partidos() > 0
				? Math.round((aggregated.victorias() * 1.0 + aggregated.empates() * 0.5) * 100.0
						/ aggregated.partidos()) / 100.0
				: 0.0;
		Double ratingPromedio = calculateAttributeAverage(playerId);
		return new RecentFormResponse(
				aggregated.partidos(),
				aggregated.victorias(),
				aggregated.derrotas(),
				aggregated.empates(),
				aggregated.goles(),
				ratingPromedio,
				indiceForma
		);
	}

	@Override
	@Transactional(readOnly = true)
	public List<TeamStandingResponse> getMatchStandings(Long matchId) {
		requireFinalizado(matchId);
		return buildStandings(participationRepository.findByMatchIdAndJugoEfectivamenteTrue(matchId));
	}

	@Override
	@Transactional(readOnly = true)
	public List<TeamStandingResponse> getStandings(Integer year) {
		return buildStandings(fetchFinishedParticipations(year));
	}

	@Override
	@Transactional(readOnly = true)
	public List<TopScorerResponse> getTopScorers(Integer year) {
		Map<Long, ScorerAccumulator> accumulators = new LinkedHashMap<>();

		List<MatchParticipation> allPlayed = fetchFinishedParticipations(year);

		for (MatchParticipation p : allPlayed) {
			Long playerId = p.getPlayer().getId();
			ScorerAccumulator acc = accumulators.computeIfAbsent(playerId,
					id -> new ScorerAccumulator(p.getPlayer()));
			acc.goles += p.getGoles();
			acc.partidosJugados++;
		}

		return accumulators.values().stream()
				.map(acc -> new TopScorerResponse(
						acc.player.getId(),
						acc.player.getNombre(),
						acc.player.getApellido(),
						acc.goles,
						acc.partidosJugados))
				.sorted(Comparator
						.comparingInt((TopScorerResponse t) -> -t.goles())
						.thenComparingInt(TopScorerResponse::partidosJugados))
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<RatingAverageResponse> getRatingRanking(Integer year) {
		Map<Long, RatingAccumulator> accumulators = new LinkedHashMap<>();

		for (Rating rating : ratingRepository.findByYear(year)) {
			Player calificado = rating.getCalificado();
			Long playerId = calificado.getId();
			RatingAccumulator acc = accumulators.computeIfAbsent(playerId,
					id -> new RatingAccumulator(calificado));
			acc.suma += rating.getPuntaje();
			acc.cantidad++;
		}

		return accumulators.values().stream()
				.map(acc -> new RatingAverageResponse(
						acc.player.getId(),
						acc.player.getNombre(),
						acc.player.getApellido(),
						Math.round(acc.suma * 100.0 / acc.cantidad) / 100.0,
						acc.cantidad))
				.sorted(Comparator
						.comparingDouble((RatingAverageResponse r) -> -r.promedio())
						.thenComparing(Comparator.comparingLong(RatingAverageResponse::cantidadCalificaciones).reversed()))
				.toList();
	}

	private List<TeamStandingResponse> buildStandings(List<MatchParticipation> played) {
		Map<Long, StandingAccumulator> accumulators = new LinkedHashMap<>();
		for (MatchParticipation participation : played) {
			Long playerId = participation.getPlayer().getId();
			StandingAccumulator acc = accumulators.computeIfAbsent(playerId,
					id -> new StandingAccumulator(participation.getPlayer()));

			acc.partidosJugados++;

			TeamSide side = teamSideOf(participation);
			if (side == null) {
				continue;
			}

			Integer matchGolesA = participation.getMatch().getGolesEquipoA();
			Integer matchGolesB = participation.getMatch().getGolesEquipoB();
			if (matchGolesA == null || matchGolesB == null) {
				throw new InvalidMatchStateException("El partido no tiene resultado cargado");
			}
			int golesA = matchGolesA;
			int golesB = matchGolesB;

			if (side == TeamSide.EQUIPO_A) {
				acc.golesAFavor += golesA;
				acc.golesEnContra += golesB;
			} else {
				acc.golesAFavor += golesB;
				acc.golesEnContra += golesA;
			}

			ResultadoPartido resultado = resultFor(participation);
			if (resultado == ResultadoPartido.EMPATE) {
				acc.empates++;
				acc.puntos += 1;
			} else if ((side == TeamSide.EQUIPO_A && resultado == ResultadoPartido.GANA_EQUIPO_A)
					|| (side == TeamSide.EQUIPO_B && resultado == ResultadoPartido.GANA_EQUIPO_B)) {
				acc.victorias++;
				acc.puntos += 3;
			} else {
				acc.derrotas++;
			}
		}

		return accumulators.values().stream()
				.map(acc -> new TeamStandingResponse(
						acc.player.getId(),
						acc.player.getNombre(),
						acc.player.getApellido(),
						acc.partidosJugados,
						acc.victorias,
						acc.empates,
						acc.derrotas,
						acc.golesAFavor,
						acc.golesEnContra,
						acc.golesAFavor - acc.golesEnContra,
						acc.puntos))
				.sorted(Comparator
						.comparingInt(TeamStandingResponse::puntos).reversed()
						.thenComparingInt(TeamStandingResponse::diferenciaGoles).reversed()
						.thenComparingInt(TeamStandingResponse::golesAFavor).reversed())
				.toList();
	}

	private List<MatchParticipation> fetchFinishedParticipations(Integer year) {
		if (year == null) {
			return participationRepository.findByMatchEstadoAndJugoEfectivamenteTrueOrderByMatchFechaHoraDesc(
					MatchStatus.FINALIZADO);
		}
		return participationRepository.findByMatchEstadoAndJugoEfectivamenteTrueByYearOrderByMatchFechaHoraDesc(
				MatchStatus.FINALIZADO, year);
	}

	private Double calculateAttributeAverage(Long playerId) {
		List<PlayerAttribute> attributes = attributeRepository.findByPlayerId(playerId);
		if (attributes.isEmpty()) {
			return 5.0;
		}
		double sum = attributes.stream()
				.mapToDouble(PlayerAttribute::getCurrentValue)
				.sum();
		return Math.round(sum / attributes.size() * 100.0) / 100.0;
	}

	private List<MatchParticipation> participationsPlayed(Long playerId, Integer year) {
		if (year == null) {
			return participationRepository
					.findByPlayerIdAndMatchEstadoAndJugoEfectivamenteTrueOrderByMatchFechaHoraDesc(
							playerId, MatchStatus.FINALIZADO);
		}
		return participationRepository
				.findByPlayerIdAndMatchEstadoAndJugoEfectivamenteTrueByYearOrderByMatchFechaHoraDesc(
						playerId, MatchStatus.FINALIZADO, year);
	}

	private Aggregated aggregate(List<MatchParticipation> played) {
		int victorias = 0;
		int derrotas = 0;
		int empates = 0;
		int goles = 0;
		for (MatchParticipation participation : played) {
			goles += participation.getGoles();
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
		return new Aggregated(played.size(), victorias, derrotas, empates, goles);
	}

	private ResultadoPartido resultFor(MatchParticipation participation) {
		Integer golesA = participation.getMatch().getGolesEquipoA();
		Integer golesB = participation.getMatch().getGolesEquipoB();
		if (golesA == null || golesB == null) {
			throw new InvalidMatchStateException("El partido no tiene resultado cargado");
		}
		if (golesA > golesB) {
			return ResultadoPartido.GANA_EQUIPO_A;
		}
		if (golesB > golesA) {
			return ResultadoPartido.GANA_EQUIPO_B;
		}
		return ResultadoPartido.EMPATE;
	}

	private Match requireFinalizado(Long matchId) {
		Match match = matchRepository.findById(matchId)
				.orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + matchId));
		if (match.getEstado() != MatchStatus.FINALIZADO) {
			throw new InvalidMatchStateException("El partido todavía no finalizó");
		}
		return match;
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
			int goles
	) {
	}

	private static class StandingAccumulator {
		final Player player;
		int partidosJugados = 0;
		int victorias = 0;
		int empates = 0;
		int derrotas = 0;
		int golesAFavor = 0;
		int golesEnContra = 0;
		int puntos = 0;

		StandingAccumulator(Player player) {
			this.player = player;
		}
	}

	private static class ScorerAccumulator {
		final Player player;
		int goles = 0;
		int partidosJugados = 0;

		ScorerAccumulator(Player player) {
			this.player = player;
		}
	}

	private static class RatingAccumulator {
		final Player player;
		double suma = 0;
		long cantidad = 0;

		RatingAccumulator(Player player) {
			this.player = player;
		}
	}
}
