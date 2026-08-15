package com.fdlj.fdlj.service;

import com.fdlj.fdlj.dto.request.MatchRequest;
import com.fdlj.fdlj.dto.request.MatchResultRequest;
import com.fdlj.fdlj.dto.response.MatchResponse;

import java.util.List;

public interface MatchService {

	MatchResponse createMatch(MatchRequest request);

	MatchResponse getMatchById(Long id);

	List<MatchResponse> getAllMatches();

	MatchResponse updateMatch(Long id, MatchRequest request);

	MatchResponse openConvocatoria(Long id);

	MatchResponse closeConvocatoria(Long id);

	MatchResponse reopenConvocatoria(Long id);

	MatchResponse startMatch(Long id);

	MatchResponse finishMatch(Long id, MatchResultRequest request);

	MatchResponse cancelMatch(Long id);
}
