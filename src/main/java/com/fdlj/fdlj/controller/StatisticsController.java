package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.config.SwaggerConstants;
import com.fdlj.fdlj.dto.response.ApiResponse;
import com.fdlj.fdlj.dto.response.ParticipationResponse;
import com.fdlj.fdlj.dto.response.PlayerStatisticsResponse;
import com.fdlj.fdlj.dto.response.RatingAverageResponse;
import com.fdlj.fdlj.dto.response.RecentFormResponse;
import com.fdlj.fdlj.dto.response.TeamStandingResponse;
import com.fdlj.fdlj.dto.response.TopScorerResponse;
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
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CONFLICT, description = "el partido todavía no finalizó")
	@Operation(summary = "Estadísticas del partido", description = "Devuelve goles y participación efectiva de cada convocado de un partido finalizado")
	public ResponseEntity<ApiResponse<List<ParticipationResponse>>> getMatchStatistics(@PathVariable Long matchId) {
		return ResponseEntity.ok().body(ApiResponse.ok(statisticsService.getMatchStatistics(matchId)));
	}

	@GetMapping("/players/{playerId}/statistics")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "estadísticas del jugador")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NOT_FOUND, description = "jugador no encontrado")
	@Operation(summary = "Estadísticas del jugador", description = "Devuelve estadísticas derivadas de los partidos finalizados. Sin el parámetro year devuelve el histórico acumulado; con year (ej. 2026) filtra por temporada. El campo ratingPromedio es el promedio de los 5 atributos del jugador")
	public ResponseEntity<ApiResponse<PlayerStatisticsResponse>> getPlayerStatistics(
			@PathVariable Long playerId, @RequestParam(required = false) Integer year) {
		return ResponseEntity.ok().body(ApiResponse.ok(statisticsService.getPlayerStatistics(playerId, year)));
	}

	@GetMapping("/players/{playerId}/statistics/recent")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "rendimiento reciente del jugador")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NOT_FOUND, description = "jugador no encontrado")
	@Operation(summary = "Rendimiento reciente", description = "Devuelve el rendimiento de los últimos partidos finalizados del jugador. Sin year son los últimos partidos históricos; con year (ej. 2026) los últimos de esa temporada. El campo ratingPromedio es el promedio de los 5 atributos del jugador")
	public ResponseEntity<ApiResponse<RecentFormResponse>> getRecentForm(
			@PathVariable Long playerId,
			@RequestParam(defaultValue = "3") int limit,
			@RequestParam(required = false) Integer year) {
		return ResponseEntity.ok().body(ApiResponse.ok(statisticsService.getRecentForm(playerId, limit, year)));
	}

	@GetMapping("/matches/{matchId}/standings")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "tabla de puntos del partido")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CONFLICT, description = "el partido todavía no finalizó")
	@Operation(summary = "Tabla de puntos del partido", description = "Devuelve la tabla de posiciones de los jugadores en el partido finalizado")
	public ResponseEntity<ApiResponse<List<TeamStandingResponse>>> getMatchStandings(@PathVariable Long matchId) {
		return ResponseEntity.ok().body(ApiResponse.ok(statisticsService.getMatchStandings(matchId)));
	}

	@GetMapping("/statistics/standings")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "tabla de puntos por temporada o histórica")
	@Operation(summary = "Tabla de puntos acumulada", description = "Devuelve la tabla de posiciones acumulada sobre partidos finalizados. Sin year devuelve el histórico de todos los años; con year (ej. 2026) filtra por temporada")
	public ResponseEntity<ApiResponse<List<TeamStandingResponse>>> getStandings(
			@RequestParam(required = false) Integer year) {
		return ResponseEntity.ok().body(ApiResponse.ok(statisticsService.getStandings(year)));
	}

	@GetMapping("/statistics/top-scorers")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "máximos goleadores")
	@Operation(summary = "Máximos goleadores", description = "Devuelve el ranking de goleadores de los partidos finalizados. Sin year devuelve el histórico; con year (ej. 2026) filtra por temporada")
	public ResponseEntity<ApiResponse<List<TopScorerResponse>>> getTopScorers(
			@RequestParam(required = false) Integer year) {
		return ResponseEntity.ok().body(ApiResponse.ok(statisticsService.getTopScorers(year)));
	}

	@GetMapping("/statistics/ratings")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "ranking de calificación de pares")
	@Operation(summary = "Calificación de pares", description = "Devuelve el ranking del promedio de calificaciones recibidas por cada jugador. Sin year devuelve el histórico; con year (ej. 2026) filtra por temporada")
	public ResponseEntity<ApiResponse<List<RatingAverageResponse>>> getRatingRanking(
			@RequestParam(required = false) Integer year) {
		return ResponseEntity.ok().body(ApiResponse.ok(statisticsService.getRatingRanking(year)));
	}
}