package com.fdlj.fdlj.service;

import com.fdlj.fdlj.dto.request.TeamAssignmentRequest;
import com.fdlj.fdlj.dto.response.TeamBalanceResponse;
import com.fdlj.fdlj.dto.response.TeamResponse;

import java.util.List;

public interface TeamService {

	List<TeamResponse> generateTeams(Long matchId);

	List<TeamResponse> getTeams(Long matchId);

	List<TeamResponse> assignPlayer(Long matchId, Long playerId, TeamAssignmentRequest request);

	TeamBalanceResponse getTeamBalance(Long matchId);
}
