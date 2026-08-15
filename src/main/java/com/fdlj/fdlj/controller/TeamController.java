package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.config.SwaggerConstants;
import com.fdlj.fdlj.dto.request.TeamAssignmentRequest;
import com.fdlj.fdlj.dto.response.ApiResponse;
import com.fdlj.fdlj.dto.response.TeamBalanceResponse;
import com.fdlj.fdlj.dto.response.TeamResponse;
import com.fdlj.fdlj.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/matches/{matchId}/teams")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class TeamController {

	private final TeamService teamService;

	@PostMapping("/generate")
	@PreAuthorize("hasRole('ADMIN')")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "equipos generados")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CONFLICT, description = "convocatoria no cerrada o jugadores insuficientes")
	@Operation(summary = "Generar equipos", description = "Genera equipos balanceados (5 vs 5) a partir de los convocados. Regenera si ya existían")
	public ResponseEntity<ApiResponse<List<TeamResponse>>> generateTeams(@PathVariable Long matchId) {
		return ResponseEntity.ok().body(ApiResponse.ok(teamService.generateTeams(matchId)));
	}

	@GetMapping
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "equipos del partido")
	@Operation(summary = "Consultar equipos", description = "Devuelve la composición y el rating promedio de cada equipo")
	public ResponseEntity<ApiResponse<List<TeamResponse>>> getTeams(@PathVariable Long matchId) {
		return ResponseEntity.ok().body(ApiResponse.ok(teamService.getTeams(matchId)));
	}

	@PutMapping("/{playerId}")
	@PreAuthorize("hasRole('ADMIN')")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "jugador asignado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CONFLICT, description = "estado inválido o equipo completo")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NOT_FOUND, description = "partido, convocado o equipo no encontrado")
	@Operation(summary = "Asignar jugador a un equipo", description = "Asigna manualmente un jugador convocado a un equipo")
	public ResponseEntity<ApiResponse<List<TeamResponse>>> assignPlayer(
			@PathVariable Long matchId, @PathVariable Long playerId,
			@Valid @RequestBody TeamAssignmentRequest request) {
		return ResponseEntity.ok().body(ApiResponse.ok(teamService.assignPlayer(matchId, playerId, request)));
	}

	@GetMapping("/balance")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "balance de equipos")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CONFLICT, description = "equipos no generados")
	@Operation(summary = "Balance de equipos", description = "Devuelve el rating promedio de cada equipo y la diferencia de nivel")
	public ResponseEntity<ApiResponse<TeamBalanceResponse>> getTeamBalance(@PathVariable Long matchId) {
		return ResponseEntity.ok().body(ApiResponse.ok(teamService.getTeamBalance(matchId)));
	}
}
