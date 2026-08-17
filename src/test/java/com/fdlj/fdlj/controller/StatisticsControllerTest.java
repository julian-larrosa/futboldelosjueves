package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StatisticsControllerTest extends IntegrationTestBase {

	@Test
	void getMatchStatistics_returns200() throws Exception {
		String admin = adminToken();
		Long matchId = setupFinishedMatch10(admin);
		mockMvc.perform(get("/api/matches/" + matchId + "/statistics")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.length()").value(10))
				.andExpect(jsonPath("$.data[0].playerId").isNumber());
	}

	@Test
	void getMatchStatistics_withoutToken_returns401() throws Exception {
		mockMvc.perform(get("/api/matches/1/statistics"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void getPlayerStatistics_returns200() throws Exception {
		String admin = adminToken();
		Long matchId = setupFinishedMatch10(admin);
		List<Long> playerIds = convocadosIds(admin, matchId);
		mockMvc.perform(get("/api/players/" + playerIds.get(0) + "/statistics")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.partidosJugados").value(1))
				.andExpect(jsonPath("$.data.goles").value(0))
				.andExpect(jsonPath("$.data.asistencias").value(0))
				.andExpect(jsonPath("$.data.ratingPromedio").isNumber())
				.andExpect(jsonPath("$.data.rendimientoReciente.partidosJugados").value(1))
				.andExpect(jsonPath("$.data.rendimientoReciente.indiceForma").isNumber());
	}

	@Test
	void getRecentForm_returns200() throws Exception {
		String admin = adminToken();
		Long matchId = setupFinishedMatch10(admin);
		List<Long> playerIds = convocadosIds(admin, matchId);
		mockMvc.perform(get("/api/players/" + playerIds.get(0) + "/statistics/recent")
						.param("limit", "3")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.partidosJugados").value(1))
				.andExpect(jsonPath("$.data.goles").value(0))
				.andExpect(jsonPath("$.data.indiceForma").isNumber());
	}

	@Test
	void getMatchStandings_returns200() throws Exception {
		String admin = adminToken();
		Long matchId = setupFinishedMatch10(admin);
		mockMvc.perform(get("/api/matches/" + matchId + "/standings")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.length()").value(10))
				.andExpect(jsonPath("$.data[0].puntos").isNumber())
				.andExpect(jsonPath("$.data[0].diferenciaGoles").isNumber());
	}

	@Test
	void getTopScorers_returns200() throws Exception {
		String admin = adminToken();
		setupFinishedMatch10(admin);
		mockMvc.perform(get("/api/statistics/top-scorers")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data").isArray());
	}

	@Test
	void getPlayerStatistics_notFound_returns404() throws Exception {
		String admin = adminToken();
		mockMvc.perform(get("/api/players/999999/statistics")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isNotFound());
	}

	@Test
	void getRecentForm_notFound_returns404() throws Exception {
		String admin = adminToken();
		mockMvc.perform(get("/api/players/999999/statistics/recent")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isNotFound());
	}
}
