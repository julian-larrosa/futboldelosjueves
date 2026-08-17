package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.config.SwaggerConstants;
import com.fdlj.fdlj.dto.request.MatchAttributeRatingsRequest;
import com.fdlj.fdlj.dto.response.ApiResponse;
import com.fdlj.fdlj.dto.response.AttributeRatingResponse;
import com.fdlj.fdlj.dto.response.PlayerAttributeHistoryResponse;
import com.fdlj.fdlj.dto.response.PlayerAttributesResponse;
import com.fdlj.fdlj.service.AttributeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AttributeController {

	private final AttributeService attributeService;

	@GetMapping("/players/{playerId}/attributes")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK,
			description = "valoraciones actuales del jugador")
	@Operation(summary = "Valoraciones actuales",
			description = "Devuelve las 5 valoraciones actuales del jugador (Técnica, Físico, Definición, Mentalidad, Pase)")
	public ResponseEntity<ApiResponse<PlayerAttributesResponse>> getPlayerAttributes(
			@PathVariable Long playerId) {
		return ResponseEntity.ok(ApiResponse.ok(attributeService.getPlayerAttributes(playerId)));
	}

	@GetMapping("/players/{playerId}/attributes/history")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK,
			description = "historial de valoraciones del jugador")
	@Operation(summary = "Historial de valoraciones",
			description = "Devuelve el historial completo de valoraciones recibidas por el jugador en cada partido")
	public ResponseEntity<ApiResponse<PlayerAttributeHistoryResponse>> getPlayerAttributeHistory(
			@PathVariable Long playerId) {
		return ResponseEntity.ok(ApiResponse.ok(attributeService.getPlayerAttributeHistory(playerId)));
	}

	@PostMapping("/matches/{matchId}/attribute-ratings")
	@PreAuthorize("hasRole('ADMIN')")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CREATED,
			description = "calificaciones de atributos registradas")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.BAD_REQUEST,
			description = "datos inválidos")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CONFLICT,
			description = "partido no finalizado, auto-calificación o ya calificado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NOT_FOUND,
			description = "partido o jugador no encontrado")
	@Operation(summary = "Calificar atributos de jugadores",
			description = "Califica a uno o más jugadores del partido en los 5 atributos (Técnica, Físico, Definición, Mentalidad, Pase)")
	public ResponseEntity<ApiResponse<List<AttributeRatingResponse>>> submitAttributeRatings(
			@PathVariable Long matchId,
			@Valid @RequestBody MatchAttributeRatingsRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.created(attributeService.submitAttributeRatings(matchId, request)));
	}
}
