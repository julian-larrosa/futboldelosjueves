package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.config.SwaggerConstants;
import com.fdlj.fdlj.dto.request.AttendanceRegisterRequest;
import com.fdlj.fdlj.dto.response.ApiResponse;
import com.fdlj.fdlj.dto.response.AttendanceRankingResponse;
import com.fdlj.fdlj.dto.response.AttendanceStatisticsResponse;
import com.fdlj.fdlj.dto.response.MatchAttendanceResponse;
import com.fdlj.fdlj.entity.User;
import com.fdlj.fdlj.security.CurrentUserService;
import com.fdlj.fdlj.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AttendanceController {

	private final AttendanceService attendanceService;
	private final CurrentUserService currentUserService;

	@PostMapping("/matches/{matchId}/attendance")
	@PreAuthorize("hasRole('ADMIN')")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CREATED, description = "asistencias registradas exitosamente")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.BAD_REQUEST, description = "datos inválidos")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NOT_FOUND, description = "partido o hincha no encontrado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.CONFLICT, description = "hincha ya registrado o partido cancelado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.FORBIDDEN, description = "solo ADMIN")
	@Operation(summary = "Registrar asistencia de hinchas", description = "Registra qué hinchas asistieron a un partido (solo ADMIN)")
	public ResponseEntity<ApiResponse<List<MatchAttendanceResponse>>> registerAttendance(
			@PathVariable Long matchId,
			@Valid @RequestBody AttendanceRegisterRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.created(attendanceService.registerAttendance(matchId, request)));
	}

	@DeleteMapping("/matches/{matchId}/attendance/{hinchaId}")
	@PreAuthorize("hasRole('ADMIN')")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NO_CONTENT, description = "asistencia removida")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NOT_FOUND, description = "partido o asistencia no encontrado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.FORBIDDEN, description = "solo ADMIN")
	@Operation(summary = "Remover asistencia de hincha", description = "Elimina la asistencia de un hincha de un partido (solo ADMIN)")
	public ResponseEntity<ApiResponse<Void>> removeAttendance(
			@PathVariable Long matchId,
			@PathVariable Long hinchaId) {
		attendanceService.removeAttendance(matchId, hinchaId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@GetMapping("/matches/{matchId}/attendance")
	@PreAuthorize("hasRole('ADMIN')")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "lista de asistencias del partido")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NOT_FOUND, description = "partido no encontrado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.FORBIDDEN, description = "solo ADMIN")
	@Operation(summary = "Obtener asistencias de un partido", description = "Devuelve qué hinchas asistieron a un partido (solo ADMIN)")
	public ResponseEntity<ApiResponse<List<MatchAttendanceResponse>>> getMatchAttendance(@PathVariable Long matchId) {
		return ResponseEntity.ok().body(ApiResponse.ok(attendanceService.getMatchAttendance(matchId)));
	}

	@GetMapping("/hinchas/{hinchaId}/attendance")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "lista de asistencias del hincha")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.NOT_FOUND, description = "hincha no encontrado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.FORBIDDEN, description = "solo el propio hincha o ADMIN")
	@Operation(summary = "Obtener asistencias de un hincha", description = "Devuelve el historial de asistencia de un hincha. El hincha solo puede consultar su propia asistencia; el ADMIN puede consultar cualquiera. Filtro opcional por año.")
	public ResponseEntity<ApiResponse<List<MatchAttendanceResponse>>> getHinchaAttendance(
			@PathVariable Long hinchaId,
			@RequestParam(required = false) Integer year) {
		User requester = currentUserService.getCurrentUser();
		return ResponseEntity.ok().body(ApiResponse.ok(attendanceService.getHinchaAttendance(hinchaId, year, requester)));
	}

	@GetMapping("/attendance/ranking")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "ranking de hinchas por asistencias")
	@Operation(summary = "Ranking de hinchas", description = "Devuelve el ranking de hinchas por partidos asistidos (solo partidos finalizados). Filtro opcional por año.")
	public ResponseEntity<ApiResponse<List<AttendanceRankingResponse>>> getAttendanceRanking(
			@RequestParam(required = false) Integer year) {
		return ResponseEntity.ok().body(ApiResponse.ok(attendanceService.getAttendanceRanking(year)));
	}

	@GetMapping("/attendance/statistics")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.OK, description = "estadísticas de asistencia")
	@Operation(summary = "Estadísticas de asistencia", description = "Devuelve estadísticas globales de asistencia (solo partidos finalizados). Filtro opcional por año.")
	public ResponseEntity<ApiResponse<AttendanceStatisticsResponse>> getAttendanceStatistics(
			@RequestParam(required = false) Integer year) {
		return ResponseEntity.ok().body(ApiResponse.ok(attendanceService.getAttendanceStatistics(year)));
	}
}
