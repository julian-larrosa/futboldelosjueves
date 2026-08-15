package com.fdlj.fdlj.service;

import com.fdlj.fdlj.dto.request.ParticipationRequest;
import com.fdlj.fdlj.dto.response.ParticipationResponse;

import java.util.List;

public interface ParticipationService {

	ParticipationResponse addPlayerToConvocatoria(Long matchId, ParticipationRequest request);

	void removePlayerFromConvocatoria(Long matchId, Long playerId);

	List<ParticipationResponse> getParticipations(Long matchId);

	ParticipationResponse getMyParticipation(Long matchId, Long playerId);
}
