package com.fdlj.fdlj.service.impl;

import com.fdlj.fdlj.dto.request.RatingRequest;
import com.fdlj.fdlj.dto.response.RatingResponse;
import com.fdlj.fdlj.entity.Match;
import com.fdlj.fdlj.entity.MatchParticipation;
import com.fdlj.fdlj.entity.Player;
import com.fdlj.fdlj.entity.Rating;
import com.fdlj.fdlj.entity.enums.MatchStatus;
import com.fdlj.fdlj.exception.InvalidMatchStateException;
import com.fdlj.fdlj.exception.ResourceAlreadyExistsException;
import com.fdlj.fdlj.exception.ResourceNotFoundException;
import com.fdlj.fdlj.mapper.RatingMapper;
import com.fdlj.fdlj.repository.MatchParticipationRepository;
import com.fdlj.fdlj.repository.MatchRepository;
import com.fdlj.fdlj.repository.RatingRepository;
import com.fdlj.fdlj.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

	private final MatchRepository matchRepository;
	private final MatchParticipationRepository participationRepository;
	private final RatingRepository ratingRepository;
	private final RatingMapper ratingMapper;

	@Override
	@Transactional
	public RatingResponse createRating(Long matchId, RatingRequest request, Long calificadorId) {
		Match match = findMatch(matchId);
		if (match.getEstado() != MatchStatus.FINALIZADO) {
			throw new InvalidMatchStateException("Solo se puede calificar un partido finalizado");
		}
		Player calificador = findEffectiveParticipant(matchId, calificadorId);
		Player calificado = findEffectiveParticipant(matchId, request.calificadoId());
		if (calificador.getId().equals(calificado.getId())) {
			throw new InvalidMatchStateException("Un jugador no puede calificarse a sí mismo");
		}
		if (ratingRepository.existsByMatchIdAndCalificadorIdAndCalificadoId(
				matchId, calificadorId, request.calificadoId())) {
			throw new ResourceAlreadyExistsException("Ya calificaste a este jugador en este partido");
		}
		Rating rating = new Rating();
		rating.setMatch(match);
		rating.setCalificador(calificador);
		rating.setCalificado(calificado);
		rating.setPuntaje(request.puntaje());
		return ratingMapper.toResponse(ratingRepository.save(rating));
	}

	@Override
	@Transactional(readOnly = true)
	public List<RatingResponse> getRatings(Long matchId) {
		findMatch(matchId);
		return ratingRepository.findByMatchIdOrderByIdAsc(matchId).stream()
				.map(ratingMapper::toResponse)
				.toList();
	}

	private Player findEffectiveParticipant(Long matchId, Long playerId) {
		MatchParticipation participation = participationRepository.findByMatchIdAndPlayerId(matchId, playerId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"El jugador con id " + playerId + " no participó de este partido"));
		if (!Boolean.TRUE.equals(participation.getJugoEfectivamente())) {
			throw new InvalidMatchStateException(
					"El jugador con id " + playerId + " no jugó efectivamente en este partido");
		}
		return participation.getPlayer();
	}

	private Match findMatch(Long id) {
		return matchRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + id));
	}
}
