package com.fdlj.fdlj.service.impl;

import com.fdlj.fdlj.dto.request.PlayerRequest;
import com.fdlj.fdlj.dto.response.PagedResponse;
import com.fdlj.fdlj.dto.response.PlayerResponse;
import com.fdlj.fdlj.entity.Player;
import com.fdlj.fdlj.entity.PlayerAttribute;
import com.fdlj.fdlj.entity.enums.AttributeType;
import com.fdlj.fdlj.entity.enums.PlayerPosition;
import com.fdlj.fdlj.exception.ResourceAlreadyExistsException;
import com.fdlj.fdlj.exception.ResourceNotFoundException;
import com.fdlj.fdlj.mapper.PlayerMapper;
import com.fdlj.fdlj.repository.PlayerAttributeRepository;
import com.fdlj.fdlj.repository.PlayerRepository;
import com.fdlj.fdlj.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerServiceImpl implements PlayerService {

	private final PlayerRepository playerRepository;
	private final PlayerAttributeRepository attributeRepository;
	private final PlayerMapper playerMapper;

	@Override
	@Transactional
	public PlayerResponse createPlayer(PlayerRequest request) {
		String email = playerMapper.normalizeEmail(request.email());
		if (playerRepository.existsByEmailAndActivoTrue(email)) {
			throw new ResourceAlreadyExistsException("Ya existe un jugador con el email: " + email);
		}
		Player player = playerMapper.toEntity(request);
		Player savedPlayer = playerRepository.save(player);

		for (AttributeType type : AttributeType.values()) {
			PlayerAttribute attribute = new PlayerAttribute();
			attribute.setPlayer(savedPlayer);
			attribute.setAttributeType(type);
			attribute.setCurrentValue(5.0);
			attributeRepository.save(attribute);
		}

		PlayerResponse response = playerMapper.toResponse(savedPlayer);
		log.info("Jugador creado: {} {} (id={})", response.nombre(), response.apellido(), response.id());
		return response;
	}

	@Override
	@Transactional(readOnly = true)
	public PlayerResponse getPlayerById(Long id) {
		return playerMapper.toResponse(findActivePlayer(id));
	}

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<PlayerResponse> getAllPlayers(Pageable pageable) {
		Page<Player> page = playerRepository.findByActivoTrue(pageable);
		return PagedResponse.of(page.map(playerMapper::toResponse));
	}

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<PlayerResponse> searchPlayers(String nombre, String apellido, String email, PlayerPosition posicion, Pageable pageable) {
		Page<Player> page = playerRepository.searchPlayers(nombre, apellido, email, posicion, pageable);
		return PagedResponse.of(page.map(playerMapper::toResponse));
	}

	@Override
	@Transactional
	public PlayerResponse updatePlayer(Long id, PlayerRequest request) {
		Player player = findActivePlayer(id);
		String email = playerMapper.normalizeEmail(request.email());
		if (playerRepository.existsByEmailAndActivoTrueAndIdNot(email, id)) {
			throw new ResourceAlreadyExistsException("Ya existe otro jugador con el email: " + email);
		}
		player.setNombre(request.nombre().trim());
		player.setApellido(request.apellido().trim());
		player.setEmail(email);
		player.setPosicion(request.posicion());
		log.info("Jugador actualizado: id={}", id);
		return playerMapper.toResponse(playerRepository.save(player));
	}

	@Override
	@Transactional
	public void deactivatePlayer(Long id) {
		Player player = findActivePlayer(id);
		player.setActivo(false);
		playerRepository.save(player);
		log.info("Jugador desactivado: id={}, nombre={} {}", id, player.getNombre(), player.getApellido());
	}

	private Player findActivePlayer(Long id) {
		return playerRepository.findByIdAndActivoTrue(id)
				.orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado con id: " + id));
	}
}
