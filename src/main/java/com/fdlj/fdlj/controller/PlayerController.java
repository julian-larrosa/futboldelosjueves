package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.config.SwaggerConstants;
import com.fdlj.fdlj.dto.request.PlayerRequest;
import com.fdlj.fdlj.dto.response.ApiResponse;
import com.fdlj.fdlj.dto.response.PagedResponse;
import com.fdlj.fdlj.dto.response.PlayerResponse;
import com.fdlj.fdlj.entity.enums.PlayerPosition;
import com.fdlj.fdlj.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PlayerController {

	private final PlayerService playerService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CREATED, description = "jugador creado exitosamente")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.BAD_REQUEST, description = "datos inválidos")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.FORBIDDEN, description = "solo ADMIN")
	@Operation(summary = "Crear jugador", description = "Crea un jugador con nombre, apellido, email y posición (solo ADMIN)")
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
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "lista paginada de jugadores activos")
	@Operation(summary = "Listar jugadores", description = "Devuelve todos los jugadores activos con paginación y búsqueda")
	public ResponseEntity<ApiResponse<PagedResponse<PlayerResponse>>> getAllPlayers(
			@RequestParam(required = false) String nombre,
			@RequestParam(required = false) String apellido,
			@RequestParam(required = false) String email,
			@RequestParam(required = false) PlayerPosition posicion,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "apellido:asc") String sort) {
		String[] parts = sort.split(":");
		String property = parts[0];
		Sort.Direction dir = parts.length > 1
				? Sort.Direction.fromString(parts[1])
				: Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(page, size, Sort.by(dir, property));
		PagedResponse<PlayerResponse> response;
		if (nombre != null || apellido != null || email != null || posicion != null) {
			response = playerService.searchPlayers(nombre, apellido, email, posicion, pageable);
		} else {
			response = playerService.getAllPlayers(pageable);
		}
		return ResponseEntity.ok().body(ApiResponse.ok(response));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "jugador actualizado exitosamente")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NOT_FOUND, description = "jugador no encontrado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CONFLICT, description = "email ya utilizado por otro jugador")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.BAD_REQUEST, description = "datos inválidos")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.FORBIDDEN, description = "solo ADMIN")
	@Operation(summary = "Actualizar jugador", description = "Actualiza los datos de un jugador activo (solo ADMIN)")
	public ResponseEntity<ApiResponse<PlayerResponse>> updatePlayer(@PathVariable Long id, @Valid @RequestBody PlayerRequest request) {
		return ResponseEntity.ok().body(ApiResponse.ok(playerService.updatePlayer(id, request)));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NO_CONTENT, description = "jugador desactivado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NOT_FOUND, description = "jugador no encontrado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.FORBIDDEN, description = "solo ADMIN")
	@Operation(summary = "Desactivar jugador", description = "Desactiva un jugador mediante eliminación lógica (solo ADMIN)")
	public ResponseEntity<ApiResponse<Void>> deactivatePlayer(@PathVariable Long id) {
		playerService.deactivatePlayer(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
