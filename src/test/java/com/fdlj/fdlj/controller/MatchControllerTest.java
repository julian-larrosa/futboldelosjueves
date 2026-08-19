package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.IntegrationTestBase;
import com.fdlj.fdlj.dto.request.MatchRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MatchControllerTest extends IntegrationTestBase {

	@Test
	void createMatch_admin_returns201WithProgramado() throws Exception {
		String admin = adminToken();
		String body = objectMapper.writeValueAsString(
				new MatchRequest(OffsetDateTime.now().plusDays(1), "Cancha"));
		mockMvc.perform(post("/api/matches")
						.header("Authorization", bearer(admin))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.estado").value("PROGRAMADO"))
				.andExpect(jsonPath("$.data.lugar").value("Cancha"));
	}

	@Test
	void createMatch_withoutToken_returns401() throws Exception {
		mockMvc.perform(post("/api/matches")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new MatchRequest(OffsetDateTime.now().plusDays(1), "Cancha"))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void createMatch_fechaPasada_returns409() throws Exception {
		String admin = adminToken();
		String body = objectMapper.writeValueAsString(
				new MatchRequest(OffsetDateTime.now().minusDays(1), "Cancha"));
		mockMvc.perform(post("/api/matches")
						.header("Authorization", bearer(admin))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isConflict());
	}

	@Test
	void createMatch_asPlayer_returns403() throws Exception {
		PlayerInfo player = registerPlayer("Jugador");
		mockMvc.perform(post("/api/matches")
						.header("Authorization", bearer(player.token()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new MatchRequest(OffsetDateTime.now().plusDays(1), "Cancha"))))
				.andExpect(status().isForbidden());
	}

	@Test
	void openConvocatoria_fromProgramado_returns200AndAbierta() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		mockMvc.perform(post("/api/matches/" + matchId + "/convocatoria/abrir")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.estado").value("CONVOCATORIA_ABIERTA"));
	}

	@Test
	void openConvocatoria_twice_returns409() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);
		mockMvc.perform(post("/api/matches/" + matchId + "/convocatoria/abrir")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isConflict());
	}

	@Test
	void closeConvocatoria_returns200AndCerrada() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);
		mockMvc.perform(post("/api/matches/" + matchId + "/convocatoria/cerrar")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.estado").value("CONVOCATORIA_CERRADA"));
	}

	@Test
	void updateMatch_invalidState_returns409() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);
		closeConvocatoria(admin, matchId);
		mockMvc.perform(put("/api/matches/" + matchId)
						.header("Authorization", bearer(admin))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new MatchRequest(OffsetDateTime.now().plusDays(2), "Otra Cancha"))))
				.andExpect(status().isConflict());
	}

	@Test
	void updateMatch_noPermiteMoverPartidoProximoAlPasado_returns409() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);
		String body = objectMapper.writeValueAsString(
				new MatchRequest(OffsetDateTime.now().minusDays(1), "Otra Cancha"));
		mockMvc.perform(put("/api/matches/" + matchId)
						.header("Authorization", bearer(admin))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isConflict());
	}

	@Test
	void updateMatch_partidoConFechaPasadaSePuedeEditar_returns200() throws Exception {
		String admin = adminToken();
		Long matchId = createMatchOrSeed(admin, OffsetDateTime.now().minusDays(5));
		openConvocatoria(admin, matchId);
		String body = objectMapper.writeValueAsString(
				new MatchRequest(OffsetDateTime.now().minusDays(3), "Cancha Editada"));
		mockMvc.perform(put("/api/matches/" + matchId)
						.header("Authorization", bearer(admin))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.lugar").value("Cancha Editada"));
	}

	@Test
	void startMatch_withoutTeams_returns409() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);
		closeConvocatoria(admin, matchId);
		mockMvc.perform(post("/api/matches/" + matchId + "/iniciar")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isConflict());
	}

	@Test
	void cancelMatch_fromCerrada_returns200AndCancelado() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);
		closeConvocatoria(admin, matchId);
		mockMvc.perform(post("/api/matches/" + matchId + "/cancelar")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.estado").value("CANCELADO"));
	}

	@Test
	void getMatchById_returns200() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		mockMvc.perform(get("/api/matches/" + matchId)
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(matchId))
				.andExpect(jsonPath("$.data.cantidadConvocados").value(0));
	}
}
