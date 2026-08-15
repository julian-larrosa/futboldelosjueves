package com.fdlj.fdlj.service;

import com.fdlj.fdlj.dto.request.RatingRequest;
import com.fdlj.fdlj.dto.response.RatingResponse;

import java.util.List;

public interface RatingService {

	RatingResponse createRating(Long matchId, RatingRequest request, Long calificadorId);

	List<RatingResponse> getRatings(Long matchId);
}
