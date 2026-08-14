package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.config.SwaggerConstants;
import com.fdlj.fdlj.dto.request.PlayerRequest;
import com.fdlj.fdlj.dto.response.ApiResponse;
import com.fdlj.fdlj.dto.response.PlayerResponse;
import com.fdlj.fdlj.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PlayerController {

	private final PlayerService playerService;

	@PostMapping
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CREATED, description = "jugador creado exitosamente")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.BAD_REQUEST, description = "datos inválidos")
	@Operation(summary = "Crear jugador", description = "Crea un jugador con nombre, apellido, email y posición")
	public ResponseEntity<ApiResponse<PlayerResponse>> createPlayer(@Valid @RequestBody PlayerRequest request) {
		PlayerResponse response = playerService.createPlayer(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
	}

	@GetMapping("/{id}")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "jugador encontrado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NOT_FOUND, description = "jugador no encontrado")
	@Operation(summary = "Obtener jugador por ID", description = "Devuelve un jugador activo por su id")
	public ResponseEntity<ApiResponse<PlayerResponse>> getPlayerById(@PathVariable Long id) {
		return ResponseEntity.ok().body(ApiResponse.ok(playerService.getPlayerById(id)));
	}

	@GetMapping
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "lista de jugadores activos")
	@Operation(summary = "Listar jugadores", description = "Devuelve todos los jugadores activos")
	public ResponseEntity<ApiResponse<List<PlayerResponse>>> getAllPlayers() {
		return ResponseEntity.ok().body(ApiResponse.ok(playerService.getAllPlayers()));
	}

	@PutMapping("/{id}")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "jugador actualizado exitosamente")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NOT_FOUND, description = "jugador no encontrado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CONFLICT, description = "email ya utilizado por otro jugador")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.BAD_REQUEST, description = "datos inválidos")
	@Operation(summary = "Actualizar jugador", description = "Actualiza los datos de un jugador activo")
	public ResponseEntity<ApiResponse<PlayerResponse>> updatePlayer(@PathVariable Long id, @Valid @RequestBody PlayerRequest request) {
		return ResponseEntity.ok().body(ApiResponse.ok(playerService.updatePlayer(id, request)));
	}

	@DeleteMapping("/{id}")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NO_CONTENT, description = "jugador desactivado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NOT_FOUND, description = "jugador no encontrado")
	@Operation(summary = "Desactivar jugador", description = "Desactiva un jugador mediante eliminación lógica")
	public ResponseEntity<ApiResponse<Void>> deactivatePlayer(@PathVariable Long id) {
		playerService.deactivatePlayer(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
