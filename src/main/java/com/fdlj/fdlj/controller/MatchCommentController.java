package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.config.SwaggerConstants;
import com.fdlj.fdlj.dto.request.MatchCommentRequest;
import com.fdlj.fdlj.dto.response.ApiResponse;
import com.fdlj.fdlj.dto.response.MatchCommentResponse;
import com.fdlj.fdlj.entity.User;
import com.fdlj.fdlj.security.CurrentUserService;
import com.fdlj.fdlj.service.MatchCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/matches/{matchId}/comments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class MatchCommentController {

	private final MatchCommentService matchCommentService;
	private final CurrentUserService currentUserService;

	@PostMapping
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CREATED, description = "comentario creado exitosamente")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.BAD_REQUEST, description = "datos inválidos")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NOT_FOUND, description = "partido no encontrado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CONFLICT, description = "estado del partido no permitido")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.FORBIDDEN, description = "la hinchada no puede comentar")
	@Operation(summary = "Crear comentario del partido", description = "El ADMIN puede comentar antes de que el partido finalice; los jugadores solo después de finalizar. Permite múltiples comentarios por partido.")
	public ResponseEntity<ApiResponse<MatchCommentResponse>> createComment(
			@PathVariable Long matchId,
			@Valid @RequestBody MatchCommentRequest request) {
		User author = currentUserService.getCurrentUser();
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.created(matchCommentService.createComment(matchId, request, author)));
	}

	@PutMapping("/{commentId}")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "comentario actualizado exitosamente")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.BAD_REQUEST, description = "datos inválidos")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NOT_FOUND, description = "partido o comentario no encontrado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CONFLICT, description = "estado del partido no permitido")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.FORBIDDEN, description = "solo el autor puede modificar su comentario")
	@Operation(summary = "Actualizar comentario del partido", description = "El ADMIN puede modificar cualquier comentario antes de finalizar; los jugadores solo su propio comentario en un partido finalizado.")
	public ResponseEntity<ApiResponse<MatchCommentResponse>> updateComment(
			@PathVariable Long matchId,
			@PathVariable Long commentId,
			@Valid @RequestBody MatchCommentRequest request) {
		User author = currentUserService.getCurrentUser();
		return ResponseEntity.ok().body(ApiResponse.ok(matchCommentService.updateComment(matchId, commentId, request, author)));
	}

	@GetMapping
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "lista de comentarios del partido")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NOT_FOUND, description = "partido no encontrado")
	@Operation(summary = "Obtener comentarios del partido", description = "Devuelve todos los comentarios (poco serio) de un partido, ordenados del más reciente al más antiguo")
	public ResponseEntity<ApiResponse<List<MatchCommentResponse>>> getComments(@PathVariable Long matchId) {
		return ResponseEntity.ok().body(ApiResponse.ok(matchCommentService.getComments(matchId)));
	}
}
