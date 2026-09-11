package com.fdlj.fdlj.service.impl;

import com.fdlj.fdlj.dto.request.MatchResultRequest;
import com.fdlj.fdlj.dto.request.MatchStatisticsBatchRequest;
import com.fdlj.fdlj.dto.request.MatchStatisticsUpdateRequest;
import com.fdlj.fdlj.dto.response.MatchResultResponse;
import com.fdlj.fdlj.dto.response.ParticipationResponse;
import com.fdlj.fdlj.entity.Match;
import com.fdlj.fdlj.entity.MatchParticipation;
import com.fdlj.fdlj.entity.enums.MatchStatus;
import com.fdlj.fdlj.exception.InvalidMatchStateException;
import com.fdlj.fdlj.exception.ResourceNotFoundException;
import com.fdlj.fdlj.mapper.MatchMapper;
import com.fdlj.fdlj.mapper.MatchParticipationMapper;
import com.fdlj.fdlj.repository.MatchParticipationRepository;
import com.fdlj.fdlj.repository.MatchRepository;
import com.fdlj.fdlj.service.ResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResultServiceImpl implements ResultService {

	private final MatchRepository matchRepository;
	private final MatchParticipationRepository participationRepository;
	private final MatchMapper matchMapper;
	private final MatchParticipationMapper participationMapper;
	private final GoalsConsistencyValidator goalsConsistencyValidator;

	@Override
	@Transactional(readOnly = true)
	public MatchResultResponse getResult(Long matchId) {
		Match match = findMatch(matchId);
		if (match.getEstado() != MatchStatus.FINALIZADO) {
			throw new InvalidMatchStateException("El partido todavía no finalizó");
		}
		return matchMapper.toResultResponse(match);
	}

	@Override
	@Transactional
	public MatchResultResponse updateResult(Long matchId, MatchResultRequest request) {
		Match match = findMatch(matchId);
		if (match.getEstado() != MatchStatus.FINALIZADO) {
			throw new InvalidMatchStateException("El resultado solo puede corregirse en un partido finalizado");
		}
		goalsConsistencyValidator.validateGoals(matchId, request.golesEquipoA(), request.golesEquipoB());
		match.setGolesEquipoA(request.golesEquipoA());
		match.setGolesEquipoB(request.golesEquipoB());
		log.info("Resultado corregido en partido id={}: {}-{}", matchId, request.golesEquipoA(), request.golesEquipoB());
		return matchMapper.toResultResponse(matchRepository.save(match));
	}

	@Override
	@Transactional
	public ParticipationResponse updateMatchStatistics(Long matchId, Long playerId, MatchStatisticsUpdateRequest request) {
		Match match = findMatch(matchId);
		if (match.getEstado() != MatchStatus.EN_CURSO && match.getEstado() != MatchStatus.FINALIZADO) {
			throw new InvalidMatchStateException(
					"Las estadísticas individuales solo pueden registrarse en un partido en curso o finalizado");
		}
		MatchParticipation participation = participationRepository.findByMatchIdAndPlayerId(matchId, playerId)
				.orElseThrow(() -> new ResourceNotFoundException("El jugador no está convocado para este partido"));
		participation.setGoles(request.goles());
		participation.setJugoEfectivamente(request.jugoEfectivamente());
		log.info("Stats actualizadas: jugador id={} en partido id={}, goles={}", playerId, matchId, request.goles());
		return participationMapper.toResponse(participationRepository.save(participation));
	}

	@Override
	@Transactional
	public List<ParticipationResponse> updateStatisticsBatch(Long matchId, MatchStatisticsBatchRequest request) {
		Match match = findMatch(matchId);
		if (match.getEstado() != MatchStatus.EN_CURSO && match.getEstado() != MatchStatus.FINALIZADO) {
			throw new InvalidMatchStateException(
					"Las estadísticas individuales solo pueden registrarse en un partido en curso o finalizado");
		}
		List<MatchParticipation> updatedList = new ArrayList<>();
		for (var playerStat : request.stats()) {
			MatchParticipation participation = participationRepository.findByMatchIdAndPlayerId(matchId, playerStat.playerId())
					.orElseThrow(() -> new ResourceNotFoundException("El jugador no está convocado para este partido: " + playerStat.playerId()));
			participation.setGoles(playerStat.goles());
			participation.setJugoEfectivamente(playerStat.jugoEfectivamente());
			updatedList.add(participation);
		}
		participationRepository.saveAll(updatedList);
		participationRepository.flush();

		if (match.getGolesEquipoA() != null && match.getGolesEquipoB() != null) {
			goalsConsistencyValidator.validateGoals(matchId, match.getGolesEquipoA(), match.getGolesEquipoB());
		}

		log.info("Stats en lote actualizadas para {} jugadores en partido id={}", updatedList.size(), matchId);
		return updatedList.stream().map(participationMapper::toResponse).toList();
	}

	private Match findMatch(Long id) {
		return matchRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + id));
	}
}
