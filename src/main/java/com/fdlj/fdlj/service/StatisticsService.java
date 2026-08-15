package com.fdlj.fdlj.service;

import com.fdlj.fdlj.dto.response.ParticipationResponse;
import com.fdlj.fdlj.dto.response.PlayerStatisticsResponse;
import com.fdlj.fdlj.dto.response.RecentFormResponse;

import java.util.List;

public interface StatisticsService {

	List<ParticipationResponse> getMatchStatistics(Long matchId);

	PlayerStatisticsResponse getPlayerStatistics(Long playerId);

	RecentFormResponse getRecentForm(Long playerId, int limit);
}
