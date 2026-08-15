package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.IntegrationTestBase;
import com.fdlj.fdlj.dto.request.MatchResultRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ResultControllerTest extends IntegrationTestBase {

	@Test
	void getResult_matchNotFound_returns404() throws Exception {
		String admin = adminToken();
		mockMvc.perform(get("/api/matches/999999/result")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isNotFound());
	}

	@Test
	void getResult_notFinished_returns409() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		mockMvc.perform(get("/api/matches/" + matchId + "/result")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isConflict());
	}

	@Test
	void getResult_finished_returns200() throws Exception {
		String admin = adminToken();
		Long matchId = setupFinishedMatch10(admin);
		mockMvc.perform(get("/api/matches/" + matchId + "/result")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.golesEquipoA").value(3))
				.andExpect(jsonPath("$.data.golesEquipoB").value(1))
				.andExpect(jsonPath("$.data.resultado").value("GANA_EQUIPO_A"));
	}

	@Test
	void updateResult_asAdmin_returns200() throws Exception {
		String admin = adminToken();
		Long matchId = setupFinishedMatch10(admin);
		String body = objectMapper.writeValueAsString(new MatchResultRequest(2, 2));
		mockMvc.perform(put("/api/matches/" + matchId + "/result")
						.header("Authorization", bearer(admin))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.golesEquipoA").value(2))
				.andExpect(jsonPath("$.data.golesEquipoB").value(2))
				.andExpect(jsonPath("$.data.resultado").value("EMPATE"));
	}

	@Test
	void updateResult_asPlayer_returns403() throws Exception {
		String admin = adminToken();
		Long matchId = setupFinishedMatch10(admin);
		PlayerInfo player = registerPlayer("SinPermiso");
		String body = objectMapper.writeValueAsString(new MatchResultRequest(1, 1));
		mockMvc.perform(put("/api/matches/" + matchId + "/result")
						.header("Authorization", bearer(player.token()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isForbidden());
	}

	@Test
	void updateResult_notFinished_returns409() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		String body = objectMapper.writeValueAsString(new MatchResultRequest(1, 1));
		mockMvc.perform(put("/api/matches/" + matchId + "/result")
						.header("Authorization", bearer(admin))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isConflict());
	}

	@Test
	void getResult_withoutToken_returns401() throws Exception {
		mockMvc.perform(get("/api/matches/1/result"))
				.andExpect(status().isUnauthorized());
	}
}
