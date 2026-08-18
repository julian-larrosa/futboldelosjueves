package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AttendanceControllerTest extends IntegrationTestBase {

	@Test
	void registerAttendance_admin_returns201() throws Exception {
		String adminToken = adminToken();
		Long matchId = setupFinishedMatch10(adminToken);
		HinchaInfo hincha = registerHincha("María");

		mockMvc.perform(post("/api/matches/" + matchId + "/attendance")
						.header("Authorization", bearer(adminToken))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(
								new com.fdlj.fdlj.dto.request.AttendanceRegisterRequest(java.util.List.of(hincha.hinchaId())))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data[0].matchId").value(matchId))
				.andExpect(jsonPath("$.data[0].hinchaId").value(hincha.hinchaId()));
	}

	@Test
	void registerAttendance_duplicate_returns409() throws Exception {
		String adminToken = adminToken();
		Long matchId = setupFinishedMatch10(adminToken);
		HinchaInfo hincha = registerHincha("María");
		registerAttendance(adminToken, matchId, hincha.hinchaId());

		mockMvc.perform(post("/api/matches/" + matchId + "/attendance")
						.header("Authorization", bearer(adminToken))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(
								new com.fdlj.fdlj.dto.request.AttendanceRegisterRequest(java.util.List.of(hincha.hinchaId())))))
				.andExpect(status().isConflict());
	}

	@Test
	void registerAttendance_duplicateInSameRequest_returns201() throws Exception {
		String adminToken = adminToken();
		Long matchId = setupFinishedMatch10(adminToken);
		HinchaInfo hincha = registerHincha("María");

		mockMvc.perform(post("/api/matches/" + matchId + "/attendance")
						.header("Authorization", bearer(adminToken))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(
								new com.fdlj.fdlj.dto.request.AttendanceRegisterRequest(java.util.List.of(hincha.hinchaId(), hincha.hinchaId())))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.length()").value(1));
	}

	@Test
	void registerAttendance_matchNotFound_returns404() throws Exception {
		String adminToken = adminToken();
		HinchaInfo hincha = registerHincha("María");

		mockMvc.perform(post("/api/matches/999999/attendance")
						.header("Authorization", bearer(adminToken))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(
								new com.fdlj.fdlj.dto.request.AttendanceRegisterRequest(java.util.List.of(hincha.hinchaId())))))
				.andExpect(status().isNotFound());
	}

	@Test
	void registerAttendance_hinchaNotFound_returns404() throws Exception {
		String adminToken = adminToken();
		Long matchId = setupFinishedMatch10(adminToken);

		mockMvc.perform(post("/api/matches/" + matchId + "/attendance")
						.header("Authorization", bearer(adminToken))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(
								new com.fdlj.fdlj.dto.request.AttendanceRegisterRequest(java.util.List.of(999999L)))))
				.andExpect(status().isNotFound());
	}

	@Test
	void registerAttendance_canceledMatch_returns409() throws Exception {
		String adminToken = adminToken();
		Long matchId = createMatch(adminToken);
		mockMvc.perform(post("/api/matches/" + matchId + "/cancelar")
						.header("Authorization", bearer(adminToken)))
				.andExpect(status().isOk());
		HinchaInfo hincha = registerHincha("María");

		mockMvc.perform(post("/api/matches/" + matchId + "/attendance")
						.header("Authorization", bearer(adminToken))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(
								new com.fdlj.fdlj.dto.request.AttendanceRegisterRequest(java.util.List.of(hincha.hinchaId())))))
				.andExpect(status().isConflict());
	}

	@Test
	void registerAttendance_hinchaRole_returns403() throws Exception {
		String adminToken = adminToken();
		Long matchId = setupFinishedMatch10(adminToken);
		HinchaInfo hincha = registerHincha("María");

		mockMvc.perform(post("/api/matches/" + matchId + "/attendance")
						.header("Authorization", bearer(hincha.token()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(
								new com.fdlj.fdlj.dto.request.AttendanceRegisterRequest(java.util.List.of(hincha.hinchaId())))))
				.andExpect(status().isForbidden());
	}

	@Test
	void removeAttendance_admin_returns204() throws Exception {
		String adminToken = adminToken();
		Long matchId = setupFinishedMatch10(adminToken);
		HinchaInfo hincha = registerHincha("María");
		registerAttendance(adminToken, matchId, hincha.hinchaId());

		mockMvc.perform(delete("/api/matches/" + matchId + "/attendance/" + hincha.hinchaId())
						.header("Authorization", bearer(adminToken)))
				.andExpect(status().isNoContent());
	}

	@Test
	void removeAttendance_notRegistered_returns404() throws Exception {
		String adminToken = adminToken();
		Long matchId = setupFinishedMatch10(adminToken);
		HinchaInfo hincha = registerHincha("María");

		mockMvc.perform(delete("/api/matches/" + matchId + "/attendance/" + hincha.hinchaId())
						.header("Authorization", bearer(adminToken)))
				.andExpect(status().isNotFound());
	}

	@Test
	void getMatchAttendance_admin_returns200() throws Exception {
		String adminToken = adminToken();
		Long matchId = setupFinishedMatch10(adminToken);
		HinchaInfo hincha1 = registerHincha("María");
		HinchaInfo hincha2 = registerHincha("Juan");
		registerAttendance(adminToken, matchId, hincha1.hinchaId(), hincha2.hinchaId());

		mockMvc.perform(get("/api/matches/" + matchId + "/attendance")
						.header("Authorization", bearer(adminToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.length()").value(2));
	}

	@Test
	void getMatchAttendance_hincha_returns403() throws Exception {
		String adminToken = adminToken();
		Long matchId = setupFinishedMatch10(adminToken);
		HinchaInfo hincha = registerHincha("María");

		mockMvc.perform(get("/api/matches/" + matchId + "/attendance")
						.header("Authorization", bearer(hincha.token())))
				.andExpect(status().isForbidden());
	}

	@Test
	void getHinchaAttendance_ownHincha_returns200() throws Exception {
		String adminToken = adminToken();
		Long matchId = setupFinishedMatch10(adminToken);
		HinchaInfo hincha = registerHincha("María");
		registerAttendance(adminToken, matchId, hincha.hinchaId());

		mockMvc.perform(get("/api/hinchas/" + hincha.hinchaId() + "/attendance")
						.header("Authorization", bearer(hincha.token())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.length()").value(1))
				.andExpect(jsonPath("$.data[0].matchId").value(matchId));
	}

	@Test
	void getHinchaAttendance_otherHincha_returns403() throws Exception {
		String adminToken = adminToken();
		Long matchId = setupFinishedMatch10(adminToken);
		HinchaInfo hincha1 = registerHincha("María");
		HinchaInfo hincha2 = registerHincha("Juan");
		registerAttendance(adminToken, matchId, hincha1.hinchaId(), hincha2.hinchaId());

		mockMvc.perform(get("/api/hinchas/" + hincha1.hinchaId() + "/attendance")
						.header("Authorization", bearer(hincha2.token())))
				.andExpect(status().isForbidden());
	}

	@Test
	void getHinchaAttendance_player_returns403() throws Exception {
		String adminToken = adminToken();
		Long matchId = setupFinishedMatch10(adminToken);
		HinchaInfo hincha = registerHincha("María");
		registerAttendance(adminToken, matchId, hincha.hinchaId());
		PlayerInfo player = registerPlayer("Pedro");

		mockMvc.perform(get("/api/hinchas/" + hincha.hinchaId() + "/attendance")
						.header("Authorization", bearer(player.token())))
				.andExpect(status().isForbidden());
	}

	@Test
	void getHinchaAttendance_admin_returns200() throws Exception {
		String adminToken = adminToken();
		Long matchId = setupFinishedMatch10(adminToken);
		HinchaInfo hincha = registerHincha("María");
		registerAttendance(adminToken, matchId, hincha.hinchaId());

		mockMvc.perform(get("/api/hinchas/" + hincha.hinchaId() + "/attendance")
						.header("Authorization", bearer(adminToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.length()").value(1));
	}

	@Test
	void getHinchaAttendance_byYear_returnsFiltered() throws Exception {
		String adminToken = adminToken();
		Long match2025 = setupFinishedMatchInYear(adminToken, 2025);
		Long match2026 = setupFinishedMatchInYear(adminToken, 2026);
		HinchaInfo hincha = registerHincha("María");
		registerAttendance(adminToken, match2025, hincha.hinchaId());
		registerAttendance(adminToken, match2026, hincha.hinchaId());

		mockMvc.perform(get("/api/hinchas/" + hincha.hinchaId() + "/attendance?year=2025")
						.header("Authorization", bearer(adminToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.length()").value(1))
				.andExpect(jsonPath("$.data[0].matchId").value(match2025));
	}

	@Test
	void getAttendanceRanking_authenticated_returnsRanking() throws Exception {
		String adminToken = adminToken();
		Long match1 = setupFinishedMatch10(adminToken);
		Long match2 = setupFinishedMatch10(adminToken);
		HinchaInfo hincha1 = registerHincha("María");
		HinchaInfo hincha2 = registerHincha("Juan");
		registerAttendance(adminToken, match1, hincha1.hinchaId(), hincha2.hinchaId());
		registerAttendance(adminToken, match2, hincha1.hinchaId());
		PlayerInfo player = registerPlayer("Pedro");

		mockMvc.perform(get("/api/attendance/ranking")
						.header("Authorization", bearer(player.token())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].hinchaId").value(hincha1.hinchaId()))
				.andExpect(jsonPath("$.data[0].totalPartidos").value(2))
				.andExpect(jsonPath("$.data[0].asistenciasPorAnio[0].partidos").value(2))
				.andExpect(jsonPath("$.data[1].hinchaId").value(hincha2.hinchaId()))
				.andExpect(jsonPath("$.data[1].totalPartidos").value(1));
	}

	@Test
	void getAttendanceRanking_byYear_returnsFiltered() throws Exception {
		String adminToken = adminToken();
		Long match2025 = setupFinishedMatchInYear(adminToken, 2025);
		Long match2026 = setupFinishedMatchInYear(adminToken, 2026);
		HinchaInfo hincha = registerHincha("María");
		registerAttendance(adminToken, match2025, hincha.hinchaId());
		registerAttendance(adminToken, match2026, hincha.hinchaId());

		mockMvc.perform(get("/api/attendance/ranking?year=2025")
						.header("Authorization", bearer(adminToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].totalPartidos").value(1))
				.andExpect(jsonPath("$.data[0].asistenciasPorAnio[0].anio").value(2025));
	}

	@Test
	void getAttendanceStatistics_returnsStats() throws Exception {
		String adminToken = adminToken();
		Long matchId = setupFinishedMatch10(adminToken);
		HinchaInfo hincha1 = registerHincha("María");
		HinchaInfo hincha2 = registerHincha("Juan");
		registerAttendance(adminToken, matchId, hincha1.hinchaId(), hincha2.hinchaId());

		mockMvc.perform(get("/api/attendance/statistics")
						.header("Authorization", bearer(adminToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.totalHinchas").value(2))
				.andExpect(jsonPath("$.data.totalAsistencias").value(2))
				.andExpect(jsonPath("$.data.promedioPorPartido").value(2.0));
	}
}