package com.fdlj.fdlj.service.impl;

import com.fdlj.fdlj.dto.request.AttributeRatingRequest;
import com.fdlj.fdlj.dto.request.MatchAttributeRatingsRequest;
import com.fdlj.fdlj.dto.response.AttributeRatingResponse;
import com.fdlj.fdlj.dto.response.PlayerAttributeHistoryResponse;
import com.fdlj.fdlj.dto.response.PlayerAttributesResponse;
import com.fdlj.fdlj.entity.Match;
import com.fdlj.fdlj.entity.MatchParticipation;
import com.fdlj.fdlj.entity.Player;
import com.fdlj.fdlj.entity.PlayerAttribute;
import com.fdlj.fdlj.entity.PlayerAttributeHistory;
import com.fdlj.fdlj.entity.enums.AttributeType;
import com.fdlj.fdlj.entity.enums.MatchStatus;
import com.fdlj.fdlj.exception.InvalidMatchStateException;
import com.fdlj.fdlj.exception.ResourceNotFoundException;
import com.fdlj.fdlj.mapper.PlayerAttributeMapper;
import com.fdlj.fdlj.repository.MatchParticipationRepository;
import com.fdlj.fdlj.repository.MatchRepository;
import com.fdlj.fdlj.repository.PlayerAttributeHistoryRepository;
import com.fdlj.fdlj.repository.PlayerAttributeRepository;
import com.fdlj.fdlj.repository.PlayerRepository;
import com.fdlj.fdlj.service.AttributeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttributeServiceImpl implements AttributeService {

	private final PlayerRepository playerRepository;
	private final MatchRepository matchRepository;
	private final MatchParticipationRepository participationRepository;
	private final PlayerAttributeRepository attributeRepository;
	private final PlayerAttributeHistoryRepository historyRepository;
	private final PlayerAttributeMapper attributeMapper;

	@Override
	@Transactional(readOnly = true)
	public PlayerAttributesResponse getPlayerAttributes(Long playerId) {
		Player player = findActivePlayer(playerId);
		List<PlayerAttribute> attributes = attributeRepository.findByPlayerId(playerId);
		Map<AttributeType, Double> historicalAverages = findHistoricalAverages(playerId);
		return attributeMapper.toAttributesResponse(player, attributes, historicalAverages);
	}

	private Map<AttributeType, Double> findHistoricalAverages(Long playerId) {
		Map<AttributeType, Double> averages = new LinkedHashMap<>();
		for (Object[] row : historyRepository.findAverageRatingByPlayerIdGroupByAttributeType(playerId)) {
			AttributeType type = (AttributeType) row[0];
			Double average = ((Number) row[1]).doubleValue();
			averages.put(type, Math.round(average * 100.0) / 100.0);
		}
		return averages;
	}

	@Override
	@Transactional(readOnly = true)
	public PlayerAttributeHistoryResponse getPlayerAttributeHistory(Long playerId) {
		findActivePlayer(playerId);
		List<PlayerAttributeHistory> history = historyRepository.findByPlayerIdOrderByMatchIdDesc(playerId);
		return attributeMapper.toHistoryResponse(playerId, history);
	}

	@Override
	@Transactional
	public List<AttributeRatingResponse> submitAttributeRatings(Long matchId, MatchAttributeRatingsRequest request) {
		Match match = findMatch(matchId);
		if (match.getEstado() != MatchStatus.FINALIZADO) {
			throw new InvalidMatchStateException("Solo se pueden calificar atributos de un partido finalizado");
		}

		List<AttributeRatingResponse> responses = new ArrayList<>();

		for (AttributeRatingRequest rating : request.ratings()) {
			Player calificado = findEffectiveParticipant(matchId, rating.playerId());

			if (historyRepository.findByPlayerIdAndAttributeTypeAndMatchId(
					calificado.getId(), AttributeType.TECNICA, matchId).isPresent()) {
				throw new InvalidMatchStateException(
						"El jugador con id " + rating.playerId() + " ya fue calificado en este partido");
			}

			for (AttributeType type : AttributeType.values()) {
				Integer value = extractAttributeValue(rating, type);
				saveHistoryAndUpdateAttribute(calificado, match, type, value);
			}

			responses.add(new AttributeRatingResponse(
					calificado.getId(),
					calificado.getNombre(),
					calificado.getApellido()));

			log.info("Atributos oficiales calificados por ADMIN: jugador {} en partido id={}",
					rating.playerId(), matchId);
		}

		return responses;
	}

	private void saveHistoryAndUpdateAttribute(Player player, Match match, AttributeType type, Integer ratingValue) {
		PlayerAttributeHistory history = new PlayerAttributeHistory();
		history.setPlayer(player);
		history.setAttributeType(type);
		history.setMatch(match);
		history.setRatingValue(ratingValue);
		historyRepository.save(history);

		PlayerAttribute attribute = attributeRepository.findByPlayerIdAndAttributeType(player.getId(), type)
				.orElseGet(() -> createDefaultAttribute(player, type));

		long count = historyRepository.countByPlayerIdAndAttributeType(player.getId(), type);
		double newAverage = ((attribute.getCurrentValue() * (count - 1)) + ratingValue) / (double) count;
		attribute.setCurrentValue(Math.round(newAverage * 100.0) / 100.0);
		attributeRepository.save(attribute);
	}

	private PlayerAttribute createDefaultAttribute(Player player, AttributeType type) {
		PlayerAttribute attribute = new PlayerAttribute();
		attribute.setPlayer(player);
		attribute.setAttributeType(type);
		attribute.setCurrentValue(5.0);
		return attributeRepository.save(attribute);
	}

	private Integer extractAttributeValue(AttributeRatingRequest request, AttributeType type) {
		return switch (type) {
			case TECNICA -> request.tecnica();
			case FISICO -> request.fisico();
			case DEFINICION -> request.definicion();
			case MENTALIDAD -> request.mentalidad();
			case PASE -> request.pase();
		};
	}

	private Player findActivePlayer(Long id) {
		return playerRepository.findByIdAndActivoTrue(id)
				.orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado con id: " + id));
	}

	private Match findMatch(Long id) {
		return matchRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + id));
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
}
