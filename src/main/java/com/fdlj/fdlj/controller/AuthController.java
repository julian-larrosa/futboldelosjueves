package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.config.SwaggerConstants;
import com.fdlj.fdlj.dto.request.LoginRequest;
import com.fdlj.fdlj.dto.request.RegisterHinchaRequest;
import com.fdlj.fdlj.dto.request.RegisterRequest;
import com.fdlj.fdlj.dto.response.ApiResponse;
import com.fdlj.fdlj.dto.response.AuthResponse;
import com.fdlj.fdlj.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CREATED, description = "usuario creado exitosamente")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.BAD_REQUEST, description = "datos inválidos")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CONFLICT, description = "email o username ya utilizado")
	@Operation(summary = "Registrar nuevo usuario", description = "Crea una cuenta de usuario y su jugador asociado, y devuelve un token JWT")
	public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
		AuthResponse response = authService.register(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
	}

	@PostMapping("/register-hincha")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CREATED, description = "hincha creado exitosamente")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.BAD_REQUEST, description = "datos inválidos")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CONFLICT, description = "email ya utilizado")
	@Operation(summary = "Registrar hincha", description = "Crea una cuenta de hincha con el rol HINCHADA y devuelve un token JWT")
	public ResponseEntity<ApiResponse<AuthResponse>> registerHincha(@Valid @RequestBody RegisterHinchaRequest request) {
		AuthResponse response = authService.registerHincha(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
	}

	@PostMapping("/login")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "login exitoso")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.BAD_REQUEST, description = "datos inválidos")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.UNAUTHORIZED, description = "email o contraseña incorrectos")
	@Operation(summary = "Iniciar sesión", description = "Autentica un usuario con email y contraseña y devuelve un token JWT")
	public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok().body(ApiResponse.ok(authService.login(request)));
	}
}
