package com.fdlj.fdlj.service;

import com.fdlj.fdlj.dto.request.MatchAttributeRatingsRequest;
import com.fdlj.fdlj.dto.response.AttributeRatingResponse;
import com.fdlj.fdlj.dto.response.PlayerAttributeHistoryResponse;
import com.fdlj.fdlj.dto.response.PlayerAttributesResponse;

import java.util.List;

public interface AttributeService {

	PlayerAttributesResponse getPlayerAttributes(Long playerId);

	PlayerAttributeHistoryResponse getPlayerAttributeHistory(Long playerId);

	List<AttributeRatingResponse> submitAttributeRatings(Long matchId, MatchAttributeRatingsRequest request);
}
