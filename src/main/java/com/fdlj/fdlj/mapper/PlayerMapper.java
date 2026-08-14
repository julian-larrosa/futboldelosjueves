package com.fdlj.fdlj.mapper;

import com.fdlj.fdlj.dto.request.PlayerRequest;
import com.fdlj.fdlj.dto.response.PlayerResponse;
import com.fdlj.fdlj.entity.Player;
import org.springframework.stereotype.Component;

@Component
public class PlayerMapper {

	public Player toEntity(PlayerRequest request) {
		Player player = new Player();
		player.setNombre(request.nombre().trim());
		player.setApellido(request.apellido().trim());
		player.setEmail(normalizeEmail(request.email()));
		player.setPosicion(request.posicion());
		return player;
	}

	public PlayerResponse toResponse(Player player) {
		return new PlayerResponse(
				player.getId(),
				player.getNombre(),
				player.getApellido(),
				player.getEmail(),
				player.getPosicion(),
				player.isActivo()
		);
	}

	public String normalizeEmail(String email) {
		return email.trim().toLowerCase();
	}
}
