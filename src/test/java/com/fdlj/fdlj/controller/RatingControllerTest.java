package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.IntegrationTestBase;
import com.fdlj.fdlj.dto.request.RatingRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RatingControllerTest extends IntegrationTestBase {

	@Test
	void createRating_success_returns201() throws Exception {
		String admin = adminToken();
		RatingSetup setup = setupFinishedMatchForRating(admin);
		String body = objectMapper.writeValueAsString(new RatingRequest(setup.calificado(), 8));
		mockMvc.perform(post("/api/matches/" + setup.matchId() + "/ratings")
						.header("Authorization", bearer(setup.calificador().token()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.puntaje").value(8))
				.andExpect(jsonPath("$.data.calificadorId").value(setup.calificador().playerId()))
				.andExpect(jsonPath("$.data.calificadoId").value(setup.calificado()));
	}

	@Test
	void createRating_matchNotFinished_returns409() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		PlayerInfo player = registerPlayer("AntesDeJugar");
		String body = objectMapper.writeValueAsString(new RatingRequest(1L, 8));
		mockMvc.perform(post("/api/matches/" + matchId + "/ratings")
						.header("Authorization", bearer(player.token()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isConflict());
	}

	@Test
	void createRating_selfRating_returns409() throws Exception {
		String admin = adminToken();
		RatingSetup setup = setupFinishedMatchForRating(admin);
		String body = objectMapper.writeValueAsString(new RatingRequest(setup.calificador().playerId(), 8));
		mockMvc.perform(post("/api/matches/" + setup.matchId() + "/ratings")
						.header("Authorization", bearer(setup.calificador().token()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isConflict());
	}

	@Test
	void createRating_calificadorNotEffective_returns409() throws Exception {
		String admin = adminToken();
		RatingSetup setup = setupWithEffectiveFlags(admin, false, true);
		String body = objectMapper.writeValueAsString(new RatingRequest(setup.calificado(), 8));
		mockMvc.perform(post("/api/matches/" + setup.matchId() + "/ratings")
						.header("Authorization", bearer(setup.calificador().token()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isConflict());
	}

	@Test
	void createRating_calificadoNotEffective_returns409() throws Exception {
		String admin = adminToken();
		RatingSetup setup = setupWithEffectiveFlags(admin, true, false);
		String body = objectMapper.writeValueAsString(new RatingRequest(setup.calificado(), 8));
		mockMvc.perform(post("/api/matches/" + setup.matchId() + "/ratings")
						.header("Authorization", bearer(setup.calificador().token()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isConflict());
	}

	@Test
	void createRating_duplicate_returns409() throws Exception {
		String admin = adminToken();
		RatingSetup setup = setupFinishedMatchForRating(admin);
		String body = objectMapper.writeValueAsString(new RatingRequest(setup.calificado(), 8));
		mockMvc.perform(post("/api/matches/" + setup.matchId() + "/ratings")
						.header("Authorization", bearer(setup.calificador().token()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated());
		mockMvc.perform(post("/api/matches/" + setup.matchId() + "/ratings")
						.header("Authorization", bearer(setup.calificador().token()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isConflict());
	}

	@Test
	void createRating_puntajeOutOfRange_returns400() throws Exception {
		String admin = adminToken();
		RatingSetup setup = setupFinishedMatchForRating(admin);
		mockMvc.perform(post("/api/matches/" + setup.matchId() + "/ratings")
						.header("Authorization", bearer(setup.calificador().token()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new RatingRequest(setup.calificado(), 0))))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post("/api/matches/" + setup.matchId() + "/ratings")
						.header("Authorization", bearer(setup.calificador().token()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new RatingRequest(setup.calificado(), 11))))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createRating_withoutToken_returns401() throws Exception {
		String admin = adminToken();
		RatingSetup setup = setupFinishedMatchForRating(admin);
		String body = objectMapper.writeValueAsString(new RatingRequest(setup.calificado(), 8));
		mockMvc.perform(post("/api/matches/" + setup.matchId() + "/ratings")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void createRating_withHinchaToken_returns403() throws Exception {
		String admin = adminToken();
		RatingSetup setup = setupFinishedMatchForRating(admin);
		HinchaInfo hincha = registerHincha("HinchaSinPermiso");
		String body = objectMapper.writeValueAsString(new RatingRequest(setup.calificado(), 8));
		mockMvc.perform(post("/api/matches/" + setup.matchId() + "/ratings")
						.header("Authorization", bearer(hincha.token()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isForbidden());
	}

	@Test
	void createRating_withAdminToken_returns403() throws Exception {
		String admin = adminToken();
		RatingSetup setup = setupFinishedMatchForRating(admin);
		String body = objectMapper.writeValueAsString(new RatingRequest(setup.calificado(), 8));
		mockMvc.perform(post("/api/matches/" + setup.matchId() + "/ratings")
						.header("Authorization", bearer(admin))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isForbidden());
	}

	private RatingSetup setupWithEffectiveFlags(String admin, boolean calificadorEfectivo, boolean calificadoEfectivo)
			throws Exception {
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);
		PlayerInfo calificador = registerPlayer("Calificador");
		convocar(admin, matchId, calificador.playerId());
		Long calificado = createPlayer("Calificado");
		convocar(admin, matchId, calificado);
		for (int i = 0; i < 8; i++) {
			convocar(admin, matchId, createPlayer("Otro" + i));
		}
		closeConvocatoria(admin, matchId);
		generateTeams(admin, matchId);
		startMatch(admin, matchId);
		for (Long playerId : convocadosIds(admin, matchId)) {
			boolean efectivo = playerId.equals(calificador.playerId()) ? calificadorEfectivo
					: playerId.equals(calificado) ? calificadoEfectivo : true;
			updateStats(admin, matchId, playerId, 0, efectivo);
		}
		finishMatch(admin, matchId, 2, 2);
		return new RatingSetup(matchId, calificador, calificado);
	}
}
