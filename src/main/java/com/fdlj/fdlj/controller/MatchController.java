package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.config.SwaggerConstants;
import com.fdlj.fdlj.dto.request.MatchRequest;
import com.fdlj.fdlj.dto.request.MatchResultRequest;
import com.fdlj.fdlj.dto.response.ApiResponse;
import com.fdlj.fdlj.dto.response.MatchResponse;
import com.fdlj.fdlj.dto.response.PagedResponse;
import com.fdlj.fdlj.entity.enums.MatchStatus;
import com.fdlj.fdlj.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class MatchController {

	private final MatchService matchService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CREATED, description = "partido creado exitosamente")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.BAD_REQUEST, description = "datos inválidos")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CONFLICT, description = "la fecha del partido debe ser futura")
	@Operation(summary = "Crear partido", description = "Crea una jornada con fecha, hora y lugar")
	public ResponseEntity<ApiResponse<MatchResponse>> createMatch(@Valid @RequestBody MatchRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.created(matchService.createMatch(request)));
	}

	@GetMapping
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "lista paginada de partidos")
	@Operation(summary = "Listar partidos", description = "Devuelve partidos con paginación y filtros")
	public ResponseEntity<ApiResponse<PagedResponse<MatchResponse>>> getAllMatches(
			@RequestParam(required = false) MatchStatus estado,
			@RequestParam(required = false) String lugar,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fechaDesde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fechaHasta,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "fechaHora:desc") String sort) {
		String[] parts = sort.split(":");
		String property = parts[0];
		Sort.Direction dir = parts.length > 1
				? Sort.Direction.fromString(parts[1])
				: Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(page, size, Sort.by(dir, property));
		PagedResponse<MatchResponse> response;
		if (estado != null || lugar != null || fechaDesde != null || fechaHasta != null) {
			response = matchService.searchMatches(estado, lugar, fechaDesde, fechaHasta, pageable);
		} else {
			response = matchService.getAllMatches(pageable);
		}
		return ResponseEntity.ok().body(ApiResponse.ok(response));
	}

	@GetMapping("/{id}")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "partido encontrado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NOT_FOUND, description = "partido no encontrado")
	@Operation(summary = "Obtener partido por ID", description = "Devuelve un partido por su id")
	public ResponseEntity<ApiResponse<MatchResponse>> getMatchById(@PathVariable Long id) {
		return ResponseEntity.ok().body(ApiResponse.ok(matchService.getMatchById(id)));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "partido actualizado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NOT_FOUND, description = "partido no encontrado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CONFLICT, description = "estado no permite la modificación o la nueva fecha debe ser futura")
	@Operation(summary = "Actualizar partido", description = "Actualiza fecha, hora y lugar de un partido programado o con convocatoria abierta")
	public ResponseEntity<ApiResponse<MatchResponse>> updateMatch(@PathVariable Long id, @Valid @RequestBody MatchRequest request) {
		return ResponseEntity.ok().body(ApiResponse.ok(matchService.updateMatch(id, request)));
	}

	@PostMapping("/{id}/convocatoria/abrir")
	@PreAuthorize("hasRole('ADMIN')")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "convocatoria abierta")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CONFLICT, description = "transición de estado inválida")
	@Operation(summary = "Abrir convocatoria", description = "Pasa el partido a CONVOCATORIA_ABIERTA")
	public ResponseEntity<ApiResponse<MatchResponse>> openConvocatoria(@PathVariable Long id) {
		return ResponseEntity.ok().body(ApiResponse.ok(matchService.openConvocatoria(id)));
	}

	@PostMapping("/{id}/convocatoria/cerrar")
	@PreAuthorize("hasRole('ADMIN')")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "convocatoria cerrada")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CONFLICT, description = "transición de estado inválida")
	@Operation(summary = "Cerrar convocatoria", description = "Pasa el partido a CONVOCATORIA_CERRADA")
	public ResponseEntity<ApiResponse<MatchResponse>> closeConvocatoria(@PathVariable Long id) {
		return ResponseEntity.ok().body(ApiResponse.ok(matchService.closeConvocatoria(id)));
	}

	@PostMapping("/{id}/convocatoria/reabrir")
	@PreAuthorize("hasRole('ADMIN')")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "convocatoria reabierta")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CONFLICT, description = "transición de estado inválida")
	@Operation(summary = "Reabrir convocatoria", description = "Pasa el partido de CONVOCATORIA_CERRADA a CONVOCATORIA_ABIERTA antes de generar equipos")
	public ResponseEntity<ApiResponse<MatchResponse>> reopenConvocatoria(@PathVariable Long id) {
		return ResponseEntity.ok().body(ApiResponse.ok(matchService.reopenConvocatoria(id)));
	}

	@PostMapping("/{id}/iniciar")
	@PreAuthorize("hasRole('ADMIN')")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "partido iniciado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CONFLICT, description = "transición de estado inválida o equipos sin generar")
	@Operation(summary = "Iniciar partido", description = "Pasa el partido a EN_CURSO. Requiere convocatoria cerrada y equipos generados")
	public ResponseEntity<ApiResponse<MatchResponse>> startMatch(@PathVariable Long id) {
		return ResponseEntity.ok().body(ApiResponse.ok(matchService.startMatch(id)));
	}

	@PostMapping("/{id}/finalizar")
	@PreAuthorize("hasRole('ADMIN')")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "partido finalizado con resultado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CONFLICT, description = "transición de estado inválida")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.BAD_REQUEST, description = "datos inválidos")
	@Operation(summary = "Finalizar partido", description = "Registra el resultado y pasa el partido a FINALIZADO")
	public ResponseEntity<ApiResponse<MatchResponse>> finishMatch(@PathVariable Long id, @Valid @RequestBody MatchResultRequest request) {
		return ResponseEntity.ok().body(ApiResponse.ok(matchService.finishMatch(id, request)));
	}

	@PostMapping("/{id}/cancelar")
	@PreAuthorize("hasRole('ADMIN')")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "partido cancelado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CONFLICT, description = "el partido ya está finalizado o cancelado")
	@Operation(summary = "Cancelar partido", description = "Cancela un partido desde cualquier estado no terminal")
	public ResponseEntity<ApiResponse<MatchResponse>> cancelMatch(@PathVariable Long id) {
		return ResponseEntity.ok().body(ApiResponse.ok(matchService.cancelMatch(id)));
	}
}
