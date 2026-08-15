package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import tools.jackson.databind.JsonNode;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TeamControllerTest extends IntegrationTestBase {

	@Test
	void generateTeams_with10Players_returns2TeamsOf5() throws Exception {
		String admin = adminToken();
		Long matchId = setupFinishedMatch10(admin);
		mockMvc.perform(get("/api/matches/" + matchId + "/teams")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.length()").value(2))
				.andExpect(jsonPath("$.data[0].side").value("EQUIPO_A"))
				.andExpect(jsonPath("$.data[0].jugadores.length()").value(5))
				.andExpect(jsonPath("$.data[1].side").value("EQUIPO_B"))
				.andExpect(jsonPath("$.data[1].jugadores.length()").value(5));
	}

	@Test
	void generateTeams_with8Players_returns409() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);
		for (int i = 0; i < 8; i++) {
			convocar(admin, matchId, createPlayer("Jugador" + i));
		}
		closeConvocatoria(admin, matchId);
		mockMvc.perform(post("/api/matches/" + matchId + "/teams/generate")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isConflict());
	}

	@Test
	void generateTeams_convocatoriaAbierta_returns409() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);
		mockMvc.perform(post("/api/matches/" + matchId + "/teams/generate")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isConflict());
	}

	@Test
	void generateTeams_with10Players_returns200() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);
		for (int i = 0; i < 10; i++) {
			convocar(admin, matchId, createPlayer("Jugador" + i));
		}
		closeConvocatoria(admin, matchId);
		mockMvc.perform(post("/api/matches/" + matchId + "/teams/generate")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.length()").value(2));
	}

	@Test
	void assignPlayer_teamsFull_returns409() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);
		Long extra = createPlayer("Extra");
		for (int i = 0; i < 10; i++) {
			convocar(admin, matchId, createPlayer("Jugador" + i));
		}
		convocar(admin, matchId, extra);
		closeConvocatoria(admin, matchId);
		generateTeams(admin, matchId);

		mockMvc.perform(put("/api/matches/" + matchId + "/teams/" + extra)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"teamSide\":\"EQUIPO_A\"}")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isConflict());
	}

	@Test
	void assignPlayer_openSlot_returns200() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);
		Long firstPlayerId = createPlayer("Jugador0");
		for (int i = 1; i < 10; i++) {
			convocar(admin, matchId, createPlayer("Jugador" + i));
		}
		convocar(admin, matchId, firstPlayerId);
		closeConvocatoria(admin, matchId);
		generateTeams(admin, matchId);

		reopenConvocatoria(admin, matchId);
		mockMvc.perform(delete("/api/matches/" + matchId + "/participations/" + firstPlayerId)
						.header("Authorization", bearer(admin)))
				.andExpect(status().isNoContent());
		Long replacement = createPlayer("Reemplazo");
		convocar(admin, matchId, replacement);
		closeConvocatoria(admin, matchId);

		String teamsJson = mockMvc.perform(get("/api/matches/" + matchId + "/teams")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		JsonNode teams = objectMapper.readTree(teamsJson).at("/data");
		String openSide = null;
		for (JsonNode team : teams) {
			if (team.get("jugadores").size() == 4) {
				openSide = team.get("side").asText();
				break;
			}
		}

		mockMvc.perform(put("/api/matches/" + matchId + "/teams/" + replacement)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"teamSide\":\"" + openSide + "\"}")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.length()").value(2))
				.andExpect(jsonPath("$.data[0].jugadores.length()").value(5))
				.andExpect(jsonPath("$.data[1].jugadores.length()").value(5));
	}

	@Test
	void getTeams_withoutTeams_returns200Empty() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);
		mockMvc.perform(get("/api/matches/" + matchId + "/teams")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.length()").value(0));
	}
}
