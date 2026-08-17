package com.fdlj.fdlj.service.impl;

import com.fdlj.fdlj.dto.request.ParticipationRequest;
import com.fdlj.fdlj.dto.response.PagedResponse;
import com.fdlj.fdlj.dto.response.ParticipationResponse;
import com.fdlj.fdlj.entity.Match;
import com.fdlj.fdlj.entity.MatchParticipation;
import com.fdlj.fdlj.entity.Player;
import com.fdlj.fdlj.entity.enums.MatchStatus;
import com.fdlj.fdlj.exception.InvalidMatchStateException;
import com.fdlj.fdlj.exception.ResourceAlreadyExistsException;
import com.fdlj.fdlj.exception.ResourceNotFoundException;
import com.fdlj.fdlj.mapper.MatchParticipationMapper;
import com.fdlj.fdlj.repository.MatchParticipationRepository;
import com.fdlj.fdlj.repository.MatchRepository;
import com.fdlj.fdlj.repository.PlayerRepository;
import com.fdlj.fdlj.service.ParticipationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParticipationServiceImpl implements ParticipationService {

	private final MatchRepository matchRepository;
	private final MatchParticipationRepository participationRepository;
	private final PlayerRepository playerRepository;
	private final MatchParticipationMapper participationMapper;

	@Override
	@Transactional
	public ParticipationResponse addPlayerToConvocatoria(Long matchId, ParticipationRequest request) {
		Match match = findMatch(matchId);
		ensureState(match, MatchStatus.PROGRAMADO, MatchStatus.CONVOCATORIA_ABIERTA);
		Player player = findActivePlayer(request.playerId());
		if (participationRepository.existsByMatchIdAndPlayerId(matchId, player.getId())) {
			throw new ResourceAlreadyExistsException("El jugador ya está convocado para este partido");
		}
		MatchParticipation participation = new MatchParticipation();
		participation.setMatch(match);
		participation.setPlayer(player);
		participation.setGoles(0);
		log.info("Jugador {} {} convocado al partido id={}", player.getNombre(), player.getApellido(), matchId);
		return participationMapper.toResponse(participationRepository.save(participation));
	}

	@Override
	@Transactional
	public void removePlayerFromConvocatoria(Long matchId, Long playerId) {
		Match match = findMatch(matchId);
		ensureState(match, MatchStatus.PROGRAMADO, MatchStatus.CONVOCATORIA_ABIERTA);
		MatchParticipation participation = findParticipation(matchId, playerId);
		log.info("Jugador id={} removido de convocatoria del partido id={}", playerId, matchId);
		participationRepository.delete(participation);
	}

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<ParticipationResponse> getParticipations(Long matchId, Pageable pageable) {
		findMatch(matchId);
		Page<MatchParticipation> page = participationRepository.findByMatchIdOrderByIdAsc(matchId, pageable);
		return PagedResponse.of(page.map(participationMapper::toResponse));
	}

	@Override
	@Transactional(readOnly = true)
	public ParticipationResponse getMyParticipation(Long matchId, Long playerId) {
		findMatch(matchId);
		return participationMapper.toResponse(findParticipation(matchId, playerId));
	}

	private Match findMatch(Long id) {
		return matchRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + id));
	}

	private Player findActivePlayer(Long id) {
		return playerRepository.findByIdAndActivoTrue(id)
				.orElseThrow(() -> new ResourceNotFoundException("Jugador activo no encontrado con id: " + id));
	}

	private MatchParticipation findParticipation(Long matchId, Long playerId) {
		return participationRepository.findByMatchIdAndPlayerId(matchId, playerId)
				.orElseThrow(() -> new ResourceNotFoundException("El jugador no está convocado para este partido"));
	}

	private void ensureState(Match match, MatchStatus... expected) {
		for (MatchStatus status : expected) {
			if (match.getEstado() == status) {
				return;
			}
		}
		throw new InvalidMatchStateException("Operación no permitida en estado " + match.getEstado());
	}
}
