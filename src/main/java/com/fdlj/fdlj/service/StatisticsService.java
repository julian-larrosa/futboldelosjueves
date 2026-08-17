package com.fdlj.fdlj.service;

import com.fdlj.fdlj.dto.response.ParticipationResponse;
import com.fdlj.fdlj.dto.response.PlayerStatisticsResponse;
import com.fdlj.fdlj.dto.response.RatingAverageResponse;
import com.fdlj.fdlj.dto.response.RecentFormResponse;
import com.fdlj.fdlj.dto.response.TeamStandingResponse;
import com.fdlj.fdlj.dto.response.TopScorerResponse;

import java.util.List;

public interface StatisticsService {

	List<ParticipationResponse> getMatchStatistics(Long matchId);

	PlayerStatisticsResponse getPlayerStatistics(Long playerId, Integer year);

	RecentFormResponse getRecentForm(Long playerId, int limit, Integer year);

	List<TeamStandingResponse> getMatchStandings(Long matchId);

	List<TeamStandingResponse> getStandings(Integer year);

	List<TopScorerResponse> getTopScorers(Integer year);

	List<RatingAverageResponse> getRatingRanking(Integer year);
}
