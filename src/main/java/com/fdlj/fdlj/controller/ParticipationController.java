package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.config.SwaggerConstants;
import com.fdlj.fdlj.dto.request.MatchStatisticsUpdateRequest;
import com.fdlj.fdlj.dto.request.ParticipationRequest;
import com.fdlj.fdlj.dto.response.ApiResponse;
import com.fdlj.fdlj.dto.response.PagedResponse;
import com.fdlj.fdlj.dto.response.ParticipationResponse;
import com.fdlj.fdlj.security.CurrentPlayerService;
import com.fdlj.fdlj.service.ParticipationService;
import com.fdlj.fdlj.service.ResultService;
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
@RequestMapping("/api/matches/{matchId}/participations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ParticipationController {

	private final ParticipationService participationService;
	private final ResultService resultService;
	private final CurrentPlayerService currentPlayerService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CREATED, description = "jugador convocado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CONFLICT, description = "estado inválido o jugador ya convocado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NOT_FOUND, description = "partido o jugador no encontrado")
	@Operation(summary = "Convocar jugador", description = "Agrega un jugador activo a la convocatoria del partido")
	public ResponseEntity<ApiResponse<ParticipationResponse>> addPlayer(@PathVariable Long matchId, @Valid @RequestBody ParticipationRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.created(participationService.addPlayerToConvocatoria(matchId, request)));
	}

	@DeleteMapping("/{playerId}")
	@PreAuthorize("hasRole('ADMIN')")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NO_CONTENT, description = "jugador desconvocado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CONFLICT, description = "estado inválido")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NOT_FOUND, description = "partido o convocado no encontrado")
	@Operation(summary = "Quitar convocado", description = "Quita un jugador de la convocatoria del partido")
	public ResponseEntity<ApiResponse<Void>> removePlayer(@PathVariable Long matchId, @PathVariable Long playerId) {
		participationService.removePlayerFromConvocatoria(matchId, playerId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@GetMapping
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "lista paginada de convocados")
	@Operation(summary = "Listar convocados", description = "Devuelve los jugadores convocados del partido con paginación")
	public ResponseEntity<ApiResponse<PagedResponse<ParticipationResponse>>> getParticipations(
			@PathVariable Long matchId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
		return ResponseEntity.ok().body(ApiResponse.ok(participationService.getParticipations(matchId, pageable)));
	}

	@GetMapping("/mine")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "participación propia")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NOT_FOUND, description = "el jugador no está convocado")
	@Operation(summary = "Consultar mi participación", description = "Devuelve la participación del jugador autenticado en el partido")
	public ResponseEntity<ApiResponse<ParticipationResponse>> getMyParticipation(@PathVariable Long matchId) {
		Long playerId = currentPlayerService.getCurrentPlayer().getId();
		return ResponseEntity.ok().body(ApiResponse.ok(participationService.getMyParticipation(matchId, playerId)));
	}

	@PutMapping("/{playerId}")
	@PreAuthorize("hasRole('ADMIN')")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "estadísticas individuales actualizadas")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CONFLICT, description = "estado inválido")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NOT_FOUND, description = "partido o convocado no encontrado")
	@Operation(summary = "Registrar estadísticas individuales", description = "Registra goles, asistencias y participación efectiva de un jugador en el partido")
	public ResponseEntity<ApiResponse<ParticipationResponse>> updateMatchStatistics(
			@PathVariable Long matchId, @PathVariable Long playerId,
			@Valid @RequestBody MatchStatisticsUpdateRequest request) {
		return ResponseEntity.ok().body(ApiResponse.ok(
				resultService.updateMatchStatistics(matchId, playerId, request)));
	}
}
