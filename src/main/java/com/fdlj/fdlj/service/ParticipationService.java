package com.fdlj.fdlj.service;

import com.fdlj.fdlj.dto.request.MatchStatisticsBatchRequest;
import com.fdlj.fdlj.dto.request.ParticipationRequest;
import com.fdlj.fdlj.dto.response.PagedResponse;
import com.fdlj.fdlj.dto.response.ParticipationResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ParticipationService {

	ParticipationResponse addPlayerToConvocatoria(Long matchId, ParticipationRequest request);

	void removePlayerFromConvocatoria(Long matchId, Long playerId);

	PagedResponse<ParticipationResponse> getParticipations(Long matchId, Pageable pageable);

	ParticipationResponse getMyParticipation(Long matchId, Long playerId);

	List<ParticipationResponse> updateStatisticsBatch(Long matchId, MatchStatisticsBatchRequest request);
}
