package com.fdlj.fdlj.service;

import com.fdlj.fdlj.dto.request.PlayerRequest;
import com.fdlj.fdlj.dto.response.PlayerResponse;

import java.util.List;

public interface PlayerService {

	PlayerResponse createPlayer(PlayerRequest request);

	PlayerResponse getPlayerById(Long id);

	List<PlayerResponse> getAllPlayers();

	PlayerResponse updatePlayer(Long id, PlayerRequest request);

	void deactivatePlayer(Long id);
}
