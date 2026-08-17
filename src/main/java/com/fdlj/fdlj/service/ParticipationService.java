package com.fdlj.fdlj.service;

import com.fdlj.fdlj.dto.request.ParticipationRequest;
import com.fdlj.fdlj.dto.response.PagedResponse;
import com.fdlj.fdlj.dto.response.ParticipationResponse;
import org.springframework.data.domain.Pageable;

public interface ParticipationService {

	ParticipationResponse addPlayerToConvocatoria(Long matchId, ParticipationRequest request);

	void removePlayerFromConvocatoria(Long matchId, Long playerId);

	PagedResponse<ParticipationResponse> getParticipations(Long matchId, Pageable pageable);

	ParticipationResponse getMyParticipation(Long matchId, Long playerId);
}
