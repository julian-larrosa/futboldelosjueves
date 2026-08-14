package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.dto.request.PlayerRequest;
import com.fdlj.fdlj.dto.response.PlayerResponse;
import com.fdlj.fdlj.service.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {

	private final PlayerService playerService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PlayerResponse createPlayer(@Valid @RequestBody PlayerRequest request) {
		return playerService.createPlayer(request);
	}

	@GetMapping("/{id}")
	public PlayerResponse getPlayerById(@PathVariable Long id) {
		return playerService.getPlayerById(id);
	}

	@GetMapping
	public List<PlayerResponse> getAllPlayers() {
		return playerService.getAllPlayers();
	}

	@PutMapping("/{id}")
	public PlayerResponse updatePlayer(@PathVariable Long id, @Valid @RequestBody PlayerRequest request) {
		return playerService.updatePlayer(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deactivatePlayer(@PathVariable Long id) {
		playerService.deactivatePlayer(id);
	}
}
