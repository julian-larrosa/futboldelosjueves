package com.fdlj.fdlj.service;

import com.fdlj.fdlj.dto.request.MatchResultRequest;
import com.fdlj.fdlj.dto.request.MatchStatisticsUpdateRequest;
import com.fdlj.fdlj.dto.response.MatchResultResponse;
import com.fdlj.fdlj.dto.response.ParticipationResponse;

public interface ResultService {

	MatchResultResponse getResult(Long matchId);

	MatchResultResponse updateResult(Long matchId, MatchResultRequest request);

	ParticipationResponse updateMatchStatistics(Long matchId, Long playerId, MatchStatisticsUpdateRequest request);
}
