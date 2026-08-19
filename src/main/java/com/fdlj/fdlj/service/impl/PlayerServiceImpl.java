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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

		List<PlayerAttribute> attributes = new ArrayList<>();
		for (AttributeType type : AttributeType.values()) {
			PlayerAttribute attribute = new PlayerAttribute();
			attribute.setPlayer(savedPlayer);
			attribute.setAttributeType(type);
			attribute.setCurrentValue(5.0);
			attributes.add(attribute);
		}
		List<PlayerAttribute> savedAttributes = attributeRepository.saveAll(attributes);

		PlayerResponse response = playerMapper.toResponse(savedPlayer, Map.of(savedPlayer.getId(), savedAttributes));
		log.info("Jugador creado: {} {} (id={})", response.nombre(), response.apellido(), response.id());
		return response;
	}

	@Override
	@Transactional(readOnly = true)
	public PlayerResponse getPlayerById(Long id) {
		Player player = findActivePlayer(id);
		return playerMapper.toResponse(player, attributesByPlayers(List.of(player)));
	}

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<PlayerResponse> getAllPlayers(Pageable pageable) {
		Page<Player> page = playerRepository.findByActivoTrue(pageable);
		Map<Long, List<PlayerAttribute>> attributesByPlayer = attributesByPlayers(page.getContent());
		return PagedResponse.of(page.map(p -> playerMapper.toResponse(p, attributesByPlayer)));
	}

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<PlayerResponse> searchPlayers(String nombre, String apellido, String email, PlayerPosition posicion, Pageable pageable) {
		Page<Player> page = playerRepository.searchPlayers(nombre, apellido, email, posicion, pageable);
		Map<Long, List<PlayerAttribute>> attributesByPlayer = attributesByPlayers(page.getContent());
		return PagedResponse.of(page.map(p -> playerMapper.toResponse(p, attributesByPlayer)));
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
		Player savedPlayer = playerRepository.save(player);
		return playerMapper.toResponse(savedPlayer, attributesByPlayers(List.of(savedPlayer)));
	}

	@Override
	@Transactional
	public void deactivatePlayer(Long id) {
		Player player = findActivePlayer(id);
		player.setActivo(false);
		playerRepository.save(player);
		log.info("Jugador desactivado: id={}, nombre={} {}", id, player.getNombre(), player.getApellido());
	}

	private Map<Long, List<PlayerAttribute>> attributesByPlayers(List<Player> players) {
		List<Long> ids = players.stream().map(Player::getId).toList();
		if (ids.isEmpty()) {
			return Map.of();
		}
		return attributeRepository.findByPlayerIdIn(ids).stream()
				.collect(Collectors.groupingBy(a -> a.getPlayer().getId()));
	}

	private Player findActivePlayer(Long id) {
		return playerRepository.findByIdAndActivoTrue(id)
				.orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado con id: " + id));
	}
}
