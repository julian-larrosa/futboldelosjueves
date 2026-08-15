package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.config.SwaggerConstants;
import com.fdlj.fdlj.dto.request.RatingRequest;
import com.fdlj.fdlj.dto.response.ApiResponse;
import com.fdlj.fdlj.dto.response.RatingResponse;
import com.fdlj.fdlj.security.CurrentPlayerService;
import com.fdlj.fdlj.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/matches/{matchId}/ratings")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class RatingController {

	private final RatingService ratingService;
	private final CurrentPlayerService currentPlayerService;

	@PostMapping
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CREATED, description = "calificación creada")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.BAD_REQUEST, description = "datos inválidos")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CONFLICT, description = "partido no finalizado, auto-calificación, no participó o ya calificó")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NOT_FOUND, description = "partido o jugador calificado no encontrado")
	@Operation(summary = "Calificar a un jugador", description = "Califica de 1 a 10 a un jugador que participó efectivamente del partido finalizado")
	public ResponseEntity<ApiResponse<RatingResponse>> createRating(
			@PathVariable Long matchId, @Valid @RequestBody RatingRequest request) {
		Long calificadorId = currentPlayerService.getCurrentPlayer().getId();
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.created(ratingService.createRating(matchId, request, calificadorId)));
	}

	@GetMapping
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "calificaciones del partido")
	@Operation(summary = "Listar calificaciones", description = "Devuelve las calificaciones registradas en el partido")
	public ResponseEntity<ApiResponse<List<RatingResponse>>> getRatings(@PathVariable Long matchId) {
		return ResponseEntity.ok().body(ApiResponse.ok(ratingService.getRatings(matchId)));
	}
}
