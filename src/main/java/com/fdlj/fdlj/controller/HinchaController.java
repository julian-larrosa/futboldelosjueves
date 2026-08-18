package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.config.SwaggerConstants;
import com.fdlj.fdlj.dto.response.ApiResponse;
import com.fdlj.fdlj.dto.response.HinchaResponse;
import com.fdlj.fdlj.dto.response.PagedResponse;
import com.fdlj.fdlj.service.HinchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hinchas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class HinchaController {

	private final HinchaService hinchaService;

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "lista paginada de hinchas activos")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.FORBIDDEN, description = "solo ADMIN")
	@Operation(summary = "Listar hinchas", description = "Devuelve todos los hinchas activos con paginación (solo ADMIN)")
	public ResponseEntity<ApiResponse<PagedResponse<HinchaResponse>>> getAllHinchas(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "apellido:asc") String sort) {
		String[] parts = sort.split(":");
		String property = parts[0];
		Sort.Direction dir = parts.length > 1
				? Sort.Direction.fromString(parts[1])
				: Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(page, size, Sort.by(dir, property));
		return ResponseEntity.ok().body(ApiResponse.ok(hinchaService.getAllHinchas(pageable)));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "hincha encontrado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NOT_FOUND, description = "hincha no encontrado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.FORBIDDEN, description = "solo ADMIN")
	@Operation(summary = "Obtener hincha por ID", description = "Devuelve un hincha activo por su id (solo ADMIN)")
	public ResponseEntity<ApiResponse<HinchaResponse>> getHinchaById(@PathVariable Long id) {
		return ResponseEntity.ok().body(ApiResponse.ok(hinchaService.getHinchaById(id)));
	}
}
