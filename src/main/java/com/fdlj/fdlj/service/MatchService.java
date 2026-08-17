package com.fdlj.fdlj.service;

import com.fdlj.fdlj.dto.request.MatchRequest;
import com.fdlj.fdlj.dto.request.MatchResultRequest;
import com.fdlj.fdlj.dto.response.MatchResponse;
import com.fdlj.fdlj.dto.response.PagedResponse;
import com.fdlj.fdlj.entity.enums.MatchStatus;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;

public interface MatchService {

	MatchResponse createMatch(MatchRequest request);

	MatchResponse getMatchById(Long id);

	PagedResponse<MatchResponse> getAllMatches(Pageable pageable);

	PagedResponse<MatchResponse> searchMatches(MatchStatus estado, String lugar, OffsetDateTime fechaDesde, OffsetDateTime fechaHasta, Pageable pageable);

	MatchResponse updateMatch(Long id, MatchRequest request);

	MatchResponse openConvocatoria(Long id);

	MatchResponse closeConvocatoria(Long id);

	MatchResponse reopenConvocatoria(Long id);

	MatchResponse startMatch(Long id);

	MatchResponse finishMatch(Long id, MatchResultRequest request);

	MatchResponse cancelMatch(Long id);
}
