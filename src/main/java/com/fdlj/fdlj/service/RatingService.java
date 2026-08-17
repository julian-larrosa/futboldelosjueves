package com.fdlj.fdlj.service;

import com.fdlj.fdlj.dto.request.RatingRequest;
import com.fdlj.fdlj.dto.response.PagedResponse;
import com.fdlj.fdlj.dto.response.RatingResponse;
import org.springframework.data.domain.Pageable;

public interface RatingService {

	RatingResponse createRating(Long matchId, RatingRequest request, Long calificadorId);

	PagedResponse<RatingResponse> getRatings(Long matchId, Pageable pageable);
}
