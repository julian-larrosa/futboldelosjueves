package com.fdlj.fdlj.service.impl;

import com.fdlj.fdlj.dto.request.PlayerRequest;
import com.fdlj.fdlj.dto.response.PlayerResponse;
import com.fdlj.fdlj.entity.Player;
import com.fdlj.fdlj.exception.ResourceAlreadyExistsException;
import com.fdlj.fdlj.exception.ResourceNotFoundException;
import com.fdlj.fdlj.mapper.PlayerMapper;
import com.fdlj.fdlj.repository.PlayerRepository;
import com.fdlj.fdlj.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerServiceImpl implements PlayerService {

	private final PlayerRepository playerRepository;
	private final PlayerMapper playerMapper;

	@Override
	@Transactional
	public PlayerResponse createPlayer(PlayerRequest request) {
		String email = playerMapper.normalizeEmail(request.email());
		if (playerRepository.existsByEmailAndActivoTrue(email)) {
			throw new ResourceAlreadyExistsException("Ya existe un jugador con el email: " + email);
		}
		Player player = playerMapper.toEntity(request);
		return playerMapper.toResponse(playerRepository.save(player));
	}

	@Override
	@Transactional(readOnly = true)
	public PlayerResponse getPlayerById(Long id) {
		return playerMapper.toResponse(findActivePlayer(id));
	}

	@Override
	@Transactional(readOnly = true)
	public List<PlayerResponse> getAllPlayers() {
		return playerRepository.findByActivoTrue().stream()
				.map(playerMapper::toResponse)
				.toList();
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
		return playerMapper.toResponse(playerRepository.save(player));
	}

	@Override
	@Transactional
	public void deactivatePlayer(Long id) {
		Player player = findActivePlayer(id);
		player.setActivo(false);
		playerRepository.save(player);
	}

	private Player findActivePlayer(Long id) {
		return playerRepository.findByIdAndActivoTrue(id)
				.orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado con id: " + id));
	}
}
