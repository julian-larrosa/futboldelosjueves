package com.fdlj.fdlj.mapper;

import com.fdlj.fdlj.dto.request.PlayerRequest;
import com.fdlj.fdlj.dto.response.PlayerAttributesResponse;
import com.fdlj.fdlj.dto.response.PlayerResponse;
import com.fdlj.fdlj.entity.Player;
import com.fdlj.fdlj.entity.PlayerAttribute;
import com.fdlj.fdlj.repository.PlayerAttributeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PlayerMapper {

	private final PlayerAttributeRepository playerAttributeRepository;

	public Player toEntity(PlayerRequest request) {
		Player player = new Player();
		player.setNombre(request.nombre().trim());
		player.setApellido(request.apellido().trim());
		player.setEmail(normalizeEmail(request.email()));
		player.setPosicion(request.posicion());
		return player;
	}

	public PlayerResponse toResponse(Player player) {
		PlayerAttributesResponse attributesResponse = null;
		if (player.getId() != null) {
			List<PlayerAttribute> attrs = playerAttributeRepository.findByPlayerId(player.getId());
			if (!attrs.isEmpty()) {
				List<com.fdlj.fdlj.dto.response.PlayerAttributeResponse> attrList = attrs.stream()
						.map(a -> new com.fdlj.fdlj.dto.response.PlayerAttributeResponse(
								a.getAttributeType(), a.getCurrentValue()))
						.sorted(Comparator.comparing(com.fdlj.fdlj.dto.response.PlayerAttributeResponse::attributeType))
						.toList();
				attributesResponse = new PlayerAttributesResponse(player.getId(), attrList);
			}
		}
		return new PlayerResponse(
				player.getId(),
				player.getNombre(),
				player.getApellido(),
				player.getEmail(),
				player.getPosicion(),
				player.isActivo(),
				attributesResponse
		);
	}

	public String normalizeEmail(String email) {
		return email.trim().toLowerCase();
	}
}
