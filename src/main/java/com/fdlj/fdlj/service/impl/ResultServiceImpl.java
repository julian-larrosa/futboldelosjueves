package com.fdlj.fdlj.service.impl;

import com.fdlj.fdlj.dto.request.MatchResultRequest;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResultServiceImpl implements ResultService {

	private final MatchRepository matchRepository;
	private final MatchParticipationRepository participationRepository;
	private final MatchMapper matchMapper;
	private final MatchParticipationMapper participationMapper;

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
		match.setGolesEquipoA(request.golesEquipoA());
		match.setGolesEquipoB(request.golesEquipoB());
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
		participation.setAsistencias(request.asistencias());
		participation.setJugoEfectivamente(request.jugoEfectivamente());
		return participationMapper.toResponse(participationRepository.save(participation));
	}

	private Match findMatch(Long id) {
		return matchRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + id));
	}
}
