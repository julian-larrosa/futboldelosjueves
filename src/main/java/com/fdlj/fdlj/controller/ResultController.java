package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.config.SwaggerConstants;
import com.fdlj.fdlj.dto.request.MatchResultRequest;
import com.fdlj.fdlj.dto.response.ApiResponse;
import com.fdlj.fdlj.dto.response.MatchResultResponse;
import com.fdlj.fdlj.service.ResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matches/{matchId}/result")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ResultController {

	private final ResultService resultService;

	@GetMapping
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "resultado del partido")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CONFLICT, description = "el partido todavía no finalizó")
	@Operation(summary = "Consultar resultado", description = "Devuelve los goles y el ganador del partido")
	public ResponseEntity<ApiResponse<MatchResultResponse>> getResult(@PathVariable Long matchId) {
		return ResponseEntity.ok().body(ApiResponse.ok(resultService.getResult(matchId)));
	}

	@PutMapping
	@PreAuthorize("hasRole('ADMIN')")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "resultado corregido")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CONFLICT, description = "el partido no está finalizado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.BAD_REQUEST, description = "datos inválidos")
	@Operation(summary = "Corregir resultado", description = "Corrige los goles de un partido finalizado")
	public ResponseEntity<ApiResponse<MatchResultResponse>> updateResult(
			@PathVariable Long matchId, @Valid @RequestBody MatchResultRequest request) {
		return ResponseEntity.ok().body(ApiResponse.ok(resultService.updateResult(matchId, request)));
	}
}
