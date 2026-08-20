package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.config.SwaggerConstants;
import com.fdlj.fdlj.dto.request.ChangePasswordRequest;
import com.fdlj.fdlj.dto.request.ResetPasswordRequest;
import com.fdlj.fdlj.security.CurrentUserService;
import com.fdlj.fdlj.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

	private final AuthService authService;
	private final CurrentUserService currentUserService;

	@PutMapping("/me/password")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NO_CONTENT, description = "contraseña actualizada exitosamente")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.BAD_REQUEST, description = "datos inválidos o contraseña actual incorrecta")
	@Operation(summary = "Cambiar mi contraseña", description = "Cambia la contraseña del usuario autenticado validando la contraseña actual")
	public ResponseEntity<Void> changeMyPassword(@Valid @RequestBody ChangePasswordRequest request) {
		String email = currentUserService.getCurrentUser().getEmail();
		authService.changePassword(email, request);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@PutMapping("/password/reset")
	@PreAuthorize("hasRole('ADMIN')")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NO_CONTENT, description = "contraseña restablecida exitosamente")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NOT_FOUND, description = "usuario no encontrado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.BAD_REQUEST, description = "datos inválidos")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.FORBIDDEN, description = "solo ADMIN")
	@Operation(summary = "Restablecer contraseña por email", description = "Restablece la contraseña de un usuario por email y fuerza su cambio en el próximo login (solo ADMIN)")
	public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		authService.resetPassword(request);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}