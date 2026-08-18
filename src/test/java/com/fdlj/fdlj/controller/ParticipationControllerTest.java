package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ParticipationControllerTest extends IntegrationTestBase {

	@Test
	void convocarPlayer_returns201() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);
		Long playerId = createPlayer("Convocado");
		mockMvc.perform(post("/api/matches/" + matchId + "/participations")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"playerId\":" + playerId + "}")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.playerId").value(playerId))
				.andExpect(jsonPath("$.data.goles").value(0));
	}

	@Test
	void convocarPlayer_duplicate_returns409() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);
		Long playerId = createPlayer("Duplicado");
		convocar(admin, matchId, playerId);
		mockMvc.perform(post("/api/matches/" + matchId + "/participations")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"playerId\":" + playerId + "}")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isConflict());
	}

	@Test
	void addPlayer_convocatoriaLlena_returns409() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);
		for (int i = 0; i < 20; i++) {
			convocar(admin, matchId, createPlayer("Lleno" + i));
		}
		Long playerId = createPlayer("Excedente");
		mockMvc.perform(post("/api/matches/" + matchId + "/participations")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"playerId\":" + playerId + "}")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isConflict());
	}

	@Test
	void convocarPlayer_afterCerrada_returns409() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);
		closeConvocatoria(admin, matchId);
		Long playerId = createPlayer("Tarde");
		mockMvc.perform(post("/api/matches/" + matchId + "/participations")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"playerId\":" + playerId + "}")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isConflict());
	}

	@Test
	void convocarDeactivatedPlayer_returns404() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);
		Long playerId = createPlayer("Inactivo");
		mockMvc.perform(delete("/api/players/" + playerId)
						.header("Authorization", bearer(admin)))
				.andExpect(status().isNoContent());
		mockMvc.perform(post("/api/matches/" + matchId + "/participations")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"playerId\":" + playerId + "}")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isNotFound());
	}

	@Test
	void getParticipations_byAdmin_returns200() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);
		Long playerId = createPlayer("Listado");
		convocar(admin, matchId, playerId);
		mockMvc.perform(get("/api/matches/" + matchId + "/participations")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.content.length()").value(1))
				.andExpect(jsonPath("$.data.content[0].playerId").value(playerId));
	}

	@Test
	void getParticipations_asPlayer_returns200() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);
		PlayerInfo player = registerPlayer("ListaPlayer");
		convocar(admin, matchId, player.playerId());
		mockMvc.perform(get("/api/matches/" + matchId + "/participations")
						.header("Authorization", bearer(player.token())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.content.length()").value(1));
	}

	@Test
	void getMyParticipations_returns200() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);
		PlayerInfo player = registerPlayer("MiPartido");
		convocar(admin, matchId, player.playerId());
		mockMvc.perform(get("/api/matches/" + matchId + "/participations/mine")
						.header("Authorization", bearer(player.token())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.playerId").value(player.playerId()));
	}

	@Test
	void getMyParticipations_notConvocado_returns404() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);
		String playerToken = registerPlayer("SinConv").token();
		mockMvc.perform(get("/api/matches/" + matchId + "/participations/mine")
						.header("Authorization", bearer(playerToken)))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteParticipation_returns204() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);
		Long playerId = createPlayer("Removido");
		convocar(admin, matchId, playerId);
		mockMvc.perform(delete("/api/matches/" + matchId + "/participations/" + playerId)
						.header("Authorization", bearer(admin)))
				.andExpect(status().isNoContent());
	}

	@Test
	void updateParticipationStats_whenProgramado_returns409() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);
		Long playerId = createPlayer("StatsProhibidas");
		convocar(admin, matchId, playerId);
		mockMvc.perform(put("/api/matches/" + matchId + "/participations/" + playerId)
						.header("Authorization", bearer(admin))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"goles\":1,\"jugoEfectivamente\":true}"))
				.andExpect(status().isConflict());
	}
}
