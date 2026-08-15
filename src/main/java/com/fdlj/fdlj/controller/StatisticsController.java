package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.config.SwaggerConstants;
import com.fdlj.fdlj.dto.response.ApiResponse;
import com.fdlj.fdlj.dto.response.ParticipationResponse;
import com.fdlj.fdlj.dto.response.PlayerStatisticsResponse;
import com.fdlj.fdlj.dto.response.RecentFormResponse;
import com.fdlj.fdlj.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class StatisticsController {

	private final StatisticsService statisticsService;

	@GetMapping("/matches/{matchId}/statistics")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "estadísticas del partido")
	@Operation(summary = "Estadísticas del partido", description = "Devuelve goles, asistencias y participación efectiva de cada convocado")
	public ResponseEntity<ApiResponse<List<ParticipationResponse>>> getMatchStatistics(@PathVariable Long matchId) {
		return ResponseEntity.ok().body(ApiResponse.ok(statisticsService.getMatchStatistics(matchId)));
	}

	@GetMapping("/players/{playerId}/statistics")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "estadísticas históricas del jugador")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NOT_FOUND, description = "jugador no encontrado")
	@Operation(summary = "Estadísticas del jugador", description = "Devuelve estadísticas históricas derivadas de los partidos finalizados")
	public ResponseEntity<ApiResponse<PlayerStatisticsResponse>> getPlayerStatistics(@PathVariable Long playerId) {
		return ResponseEntity.ok().body(ApiResponse.ok(statisticsService.getPlayerStatistics(playerId)));
	}

	@GetMapping("/players/{playerId}/statistics/recent")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "rendimiento reciente del jugador")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NOT_FOUND, description = "jugador no encontrado")
	@Operation(summary = "Rendimiento reciente", description = "Devuelve el rendimiento de los últimos partidos finalizados del jugador")
	public ResponseEntity<ApiResponse<RecentFormResponse>> getRecentForm(
			@PathVariable Long playerId, @RequestParam(defaultValue = "5") int limit) {
		return ResponseEntity.ok().body(ApiResponse.ok(statisticsService.getRecentForm(playerId, limit)));
	}
}
