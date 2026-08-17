package com.fdlj.fdlj.service;

import com.fdlj.fdlj.dto.request.PlayerRequest;
import com.fdlj.fdlj.dto.response.PagedResponse;
import com.fdlj.fdlj.dto.response.PlayerResponse;
import com.fdlj.fdlj.entity.enums.PlayerPosition;
import org.springframework.data.domain.Pageable;

public interface PlayerService {

	PlayerResponse createPlayer(PlayerRequest request);

	PlayerResponse getPlayerById(Long id);

	PagedResponse<PlayerResponse> getAllPlayers(Pageable pageable);

	PagedResponse<PlayerResponse> searchPlayers(String nombre, String apellido, String email, PlayerPosition posicion, Pageable pageable);

	PlayerResponse updatePlayer(Long id, PlayerRequest request);

	void deactivatePlayer(Long id);
}
