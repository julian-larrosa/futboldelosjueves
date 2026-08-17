package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.IntegrationTestBase;
import com.fdlj.fdlj.dto.request.AttributeRatingRequest;
import com.fdlj.fdlj.dto.request.MatchAttributeRatingsRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AttributeControllerTest extends IntegrationTestBase {

	@Test
	void getPlayerAttributes_newPlayer_returns5DefaultAttributes() throws Exception {
		PlayerInfo player = registerPlayer("NuevoJugador");
		mockMvc.perform(get("/api/players/" + player.playerId() + "/attributes")
						.header("Authorization", bearer(player.token())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.attributes").isArray())
				.andExpect(jsonPath("$.data.attributes.length()").value(5));
	}

	@Test
	void submitAttributeRatings_adminSuccess_returns201() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);

		Long calificado = createPlayer("CalificadoAttr");
		convocar(admin, matchId, calificado);

		for (int i = 0; i < 9; i++) {
			convocar(admin, matchId, createPlayer("OtroAttr" + i));
		}

		closeConvocatoria(admin, matchId);
		generateTeams(admin, matchId);
		startMatch(admin, matchId);
		marcarEfectivos(admin, matchId);
		finishMatch(admin, matchId, 3, 1);

		AttributeRatingRequest rating = buildAttributeRating(calificado, 8, 7, 9, 6, 8);
		mockMvc.perform(post("/api/matches/" + matchId + "/attribute-ratings")
						.header("Authorization", bearer(admin))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new MatchAttributeRatingsRequest(List.of(rating)))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.length()").value(1));
	}

	@Test
	void submitAttributeRatings_updatesCurrentValues() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);

		Long calificado = createPlayer("CalificadoUpdate");
		convocar(admin, matchId, calificado);

		for (int i = 0; i < 9; i++) {
			convocar(admin, matchId, createPlayer("OtroUpdate" + i));
		}

		closeConvocatoria(admin, matchId);
		generateTeams(admin, matchId);
		startMatch(admin, matchId);
		marcarEfectivos(admin, matchId);
		finishMatch(admin, matchId, 2, 2);

		AttributeRatingRequest rating = buildAttributeRating(calificado, 10, 8, 7, 9, 6);
		submitAttributeRatings(admin, matchId, List.of(rating));

		mockMvc.perform(get("/api/players/" + calificado + "/attributes")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.attributes").isArray())
				.andExpect(jsonPath("$.data.attributes[?(@.attributeType == 'TECNICA')].currentValue").value(10.0));
	}

	@Test
	void submitAttributeRatings_playerRole_returns403() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);

		PlayerInfo player = registerPlayer("PlayerNoAdmin");
		convocar(admin, matchId, player.playerId());

		for (int i = 0; i < 9; i++) {
			convocar(admin, matchId, createPlayer("OtroNoAdmin" + i));
		}

		closeConvocatoria(admin, matchId);
		generateTeams(admin, matchId);
		startMatch(admin, matchId);
		marcarEfectivos(admin, matchId);
		finishMatch(admin, matchId, 1, 1);

		AttributeRatingRequest rating = buildAttributeRating(player.playerId(), 8, 7, 9, 6, 8);
		mockMvc.perform(post("/api/matches/" + matchId + "/attribute-ratings")
						.header("Authorization", bearer(player.token()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new MatchAttributeRatingsRequest(List.of(rating)))))
				.andExpect(status().isForbidden());
	}

	@Test
	void submitAttributeRatings_matchNotFinished_returns409() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);

		Long calificado = createPlayer("CalificadoNoFin");
		convocar(admin, matchId, calificado);

		for (int i = 0; i < 9; i++) {
			convocar(admin, matchId, createPlayer("OtroNoFin" + i));
		}

		closeConvocatoria(admin, matchId);
		generateTeams(admin, matchId);
		startMatch(admin, matchId);
		marcarEfectivos(admin, matchId);

		AttributeRatingRequest rating = buildAttributeRating(calificado, 8, 7, 9, 6, 8);
		mockMvc.perform(post("/api/matches/" + matchId + "/attribute-ratings")
						.header("Authorization", bearer(admin))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new MatchAttributeRatingsRequest(List.of(rating)))))
				.andExpect(status().isConflict());
	}

	@Test
	void submitAttributeRatings_invalidRange_returns400() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);

		Long calificado = createPlayer("CalificadoRange");
		convocar(admin, matchId, calificado);

		for (int i = 0; i < 9; i++) {
			convocar(admin, matchId, createPlayer("OtroRange" + i));
		}

		closeConvocatoria(admin, matchId);
		generateTeams(admin, matchId);
		startMatch(admin, matchId);
		marcarEfectivos(admin, matchId);
		finishMatch(admin, matchId, 2, 1);

		AttributeRatingRequest rating = buildAttributeRating(calificado, 0, 7, 9, 6, 8);
		mockMvc.perform(post("/api/matches/" + matchId + "/attribute-ratings")
						.header("Authorization", bearer(admin))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new MatchAttributeRatingsRequest(List.of(rating)))))
				.andExpect(status().isBadRequest());
	}

	@Test
	void submitAttributeRatings_duplicatePlayer_returns409() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);

		Long calificado = createPlayer("CalificadoDup");
		convocar(admin, matchId, calificado);

		for (int i = 0; i < 9; i++) {
			convocar(admin, matchId, createPlayer("OtroDup" + i));
		}

		closeConvocatoria(admin, matchId);
		generateTeams(admin, matchId);
		startMatch(admin, matchId);
		marcarEfectivos(admin, matchId);
		finishMatch(admin, matchId, 2, 2);

		AttributeRatingRequest rating1 = buildAttributeRating(calificado, 8, 7, 9, 6, 8);
		submitAttributeRatings(admin, matchId, List.of(rating1));

		AttributeRatingRequest rating2 = buildAttributeRating(calificado, 7, 8, 8, 7, 7);
		mockMvc.perform(post("/api/matches/" + matchId + "/attribute-ratings")
						.header("Authorization", bearer(admin))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new MatchAttributeRatingsRequest(List.of(rating2)))))
				.andExpect(status().isConflict());
	}

	@Test
	void submitAttributeRatings_withoutToken_returns401() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);

		Long calificado = createPlayer("CalificadoNoToken");
		convocar(admin, matchId, calificado);

		for (int i = 0; i < 9; i++) {
			convocar(admin, matchId, createPlayer("OtroNoToken" + i));
		}

		closeConvocatoria(admin, matchId);
		generateTeams(admin, matchId);
		startMatch(admin, matchId);
		marcarEfectivos(admin, matchId);
		finishMatch(admin, matchId, 1, 1);

		AttributeRatingRequest rating = buildAttributeRating(calificado, 8, 7, 9, 6, 8);
		mockMvc.perform(post("/api/matches/" + matchId + "/attribute-ratings")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new MatchAttributeRatingsRequest(List.of(rating)))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void getAttributeHistory_afterRating_returnsHistory() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);

		Long calificado = createPlayer("CalificadoHist");
		convocar(admin, matchId, calificado);

		for (int i = 0; i < 9; i++) {
			convocar(admin, matchId, createPlayer("OtroHist" + i));
		}

		closeConvocatoria(admin, matchId);
		generateTeams(admin, matchId);
		startMatch(admin, matchId);
		marcarEfectivos(admin, matchId);
		finishMatch(admin, matchId, 2, 1);

		AttributeRatingRequest rating = buildAttributeRating(calificado, 8, 7, 9, 6, 8);
		submitAttributeRatings(admin, matchId, List.of(rating));

		mockMvc.perform(get("/api/players/" + calificado + "/attributes/history")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.history").isArray())
				.andExpect(jsonPath("$.data.history.length()").value(5));
	}

	private void marcarEfectivos(String adminToken, Long matchId) throws Exception {
		for (Long playerId : convocadosIds(adminToken, matchId)) {
			updateStats(adminToken, matchId, playerId, 0, true);
		}
	}
}
