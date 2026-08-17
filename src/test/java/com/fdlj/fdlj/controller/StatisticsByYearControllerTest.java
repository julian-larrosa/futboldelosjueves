package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.IntegrationTestBase;
import com.fdlj.fdlj.dto.request.RatingRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StatisticsByYearControllerTest extends IntegrationTestBase {

	private static OffsetDateTime dateInYear(int year) {
		return OffsetDateTime.of(year, 6, 15, 20, 0, 0, 0, ZoneOffset.UTC);
	}

	@Test
	void standings_byYear_returnsOnlyThatYear() throws Exception {
		String admin = adminToken();
		setupFinishedMatchInYear(admin, 2025);
		setupFinishedMatchInYear(admin, 2026);

		mockMvc.perform(get("/api/statistics/standings")
						.param("year", "2025")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.length()").value(10));
	}

	@Test
	void standings_historical_returnsAllYears() throws Exception {
		String admin = adminToken();
		setupFinishedMatchInYear(admin, 2025);
		setupFinishedMatchInYear(admin, 2026);

		mockMvc.perform(get("/api/statistics/standings")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.length()").value(20));
	}

	@Test
	void topScorers_byYear_returnsOnlyYearGoals() throws Exception {
		String admin = adminToken();
		Long playerId = createPlayer("GoleadorAnio");
		setupMatchWithScorer(admin, 2025, playerId, 2);
		setupMatchWithScorer(admin, 2026, playerId, 3);

		mockMvc.perform(get("/api/statistics/top-scorers")
						.param("year", "2025")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].playerId").value(playerId))
				.andExpect(jsonPath("$.data[0].goles").value(2));

		mockMvc.perform(get("/api/statistics/top-scorers")
						.param("year", "2026")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].playerId").value(playerId))
				.andExpect(jsonPath("$.data[0].goles").value(3));
	}

	@Test
	void topScorers_historical_returnsAllGoals() throws Exception {
		String admin = adminToken();
		Long playerId = createPlayer("GoleadorHist");
		setupMatchWithScorer(admin, 2025, playerId, 2);
		setupMatchWithScorer(admin, 2026, playerId, 3);

		mockMvc.perform(get("/api/statistics/top-scorers")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].playerId").value(playerId))
				.andExpect(jsonPath("$.data[0].goles").value(5));
	}

	@Test
	void ratingRanking_byYear_returnsYearAverage() throws Exception {
		String admin = adminToken();
		RatingSetup setup2025 = setupFinishedMatchForRatingInYear(admin, 2025);
		RatingSetup setup2026 = setupFinishedMatchForRatingInYear(admin, 2026);
		createRating(setup2025, 8);
		createRating(setup2026, 10);

		mockMvc.perform(get("/api/statistics/ratings")
						.param("year", "2025")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[?(@.playerId == " + setup2025.calificado() + ")].promedio").value(8.0));

		mockMvc.perform(get("/api/statistics/ratings")
						.param("year", "2026")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[?(@.playerId == " + setup2026.calificado() + ")].promedio").value(10.0));
	}

	@Test
	void ratingRanking_historical_returnsAllAverage() throws Exception {
		String admin = adminToken();
		PlayerInfo calificador = registerPlayer("CalifHist");
		Long calificado = createPlayer("CalificadoHist");
		Long match2025 = setupMatchWithParticipants(admin, 2025, calificador.playerId(), calificado);
		Long match2026 = setupMatchWithParticipants(admin, 2026, calificador.playerId(), calificado);
		createRating(calificador.token(), match2025, calificado, 8);
		createRating(calificador.token(), match2026, calificado, 10);

		mockMvc.perform(get("/api/statistics/ratings")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[?(@.playerId == " + calificado + ")].promedio").value(9.0))
				.andExpect(jsonPath("$.data[?(@.playerId == " + calificado + ")].cantidadCalificaciones").value(2));
	}

	@Test
	void playerStatistics_byYear_returnsYearStats() throws Exception {
		String admin = adminToken();
		Long playerId = createPlayer("JugadorAnio");
		setupMatchWithScorer(admin, 2025, playerId, 2);
		setupMatchWithScorer(admin, 2026, playerId, 3);

		mockMvc.perform(get("/api/players/" + playerId + "/statistics")
						.param("year", "2025")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.partidosJugados").value(1))
				.andExpect(jsonPath("$.data.goles").value(2));

		mockMvc.perform(get("/api/players/" + playerId + "/statistics")
						.param("year", "2026")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.partidosJugados").value(1))
				.andExpect(jsonPath("$.data.goles").value(3));
	}

	@Test
	void playerStatistics_historical_returnsAllStats() throws Exception {
		String admin = adminToken();
		Long playerId = createPlayer("JugadorHist");
		setupMatchWithScorer(admin, 2025, playerId, 2);
		setupMatchWithScorer(admin, 2026, playerId, 3);

		mockMvc.perform(get("/api/players/" + playerId + "/statistics")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.partidosJugados").value(2))
				.andExpect(jsonPath("$.data.goles").value(5));
	}

	@Test
	void recentForm_byYear_returnsLast3WithinYear() throws Exception {
		String admin = adminToken();
		Long playerId = createPlayer("FormaAnio");
		setupMatchWithScorer(admin, 2026, playerId, 1);
		setupMatchWithScorer(admin, 2026, playerId, 1);
		setupMatchWithScorer(admin, 2026, playerId, 1);

		mockMvc.perform(get("/api/players/" + playerId + "/statistics/recent")
						.param("year", "2026")
						.param("limit", "3")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.partidosJugados").value(3))
				.andExpect(jsonPath("$.data.goles").value(3));
	}

	@Test
	void recentForm_historical_returnsLast3Overall() throws Exception {
		String admin = adminToken();
		Long playerId = createPlayer("FormaHist");
		setupMatchWithScorer(admin, 2025, playerId, 0);
		setupMatchWithScorer(admin, 2026, playerId, 1);
		setupMatchWithScorer(admin, 2026, playerId, 1);
		setupMatchWithScorer(admin, 2026, playerId, 1);

		mockMvc.perform(get("/api/players/" + playerId + "/statistics/recent")
						.param("limit", "3")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.partidosJugados").value(3))
				.andExpect(jsonPath("$.data.goles").value(3));
	}

	@Test
	void standings_yearWithoutMatches_returnsEmptyList() throws Exception {
		String admin = adminToken();
		setupFinishedMatchInYear(admin, 2026);

		mockMvc.perform(get("/api/statistics/standings")
						.param("year", "2020")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.length()").value(0));

		mockMvc.perform(get("/api/statistics/top-scorers")
						.param("year", "2020")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.length()").value(0));

		mockMvc.perform(get("/api/statistics/ratings")
						.param("year", "2020")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.length()").value(0));
	}

	@Test
	void standings_mixedYears_aggregatesCorrectly() throws Exception {
		String admin = adminToken();
		Long playerId = createPlayer("Mixto");
		setupMatchWithScorer(admin, 2025, playerId, 1);
		setupMatchWithScorer(admin, 2026, playerId, 2);

		mockMvc.perform(get("/api/statistics/standings")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[?(@.playerId == " + playerId + ")].partidosJugados").value(2));

		mockMvc.perform(get("/api/statistics/standings")
						.param("year", "2025")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[?(@.playerId == " + playerId + ")].partidosJugados").value(1));

		mockMvc.perform(get("/api/statistics/standings")
						.param("year", "2026")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[?(@.playerId == " + playerId + ")].partidosJugados").value(1));
	}

	@Test
	void playerStatistics_yearWithoutMatches_returnsZeroed() throws Exception {
		String admin = adminToken();
		Long playerId = createPlayer("SinPartidos");
		setupMatchWithScorer(admin, 2026, playerId, 2);

		mockMvc.perform(get("/api/players/" + playerId + "/statistics")
						.param("year", "2020")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.partidosJugados").value(0))
				.andExpect(jsonPath("$.data.goles").value(0))
				.andExpect(jsonPath("$.data.rendimientoReciente.partidosJugados").value(0));
	}

	private Long setupMatchWithScorer(String adminToken, int year, Long scorerId, int scorerGoals) throws Exception {
		Long matchId = createMatch(adminToken, dateInYear(year));
		openConvocatoria(adminToken, matchId);
		convocar(adminToken, matchId, scorerId);
		for (int i = 0; i < 9; i++) {
			convocar(adminToken, matchId, createPlayer("Otro" + i));
		}
		closeConvocatoria(adminToken, matchId);
		generateTeams(adminToken, matchId);
		startMatch(adminToken, matchId);
		for (Long playerId : convocadosIds(adminToken, matchId)) {
			int goles = playerId.equals(scorerId) ? scorerGoals : 0;
			updateStats(adminToken, matchId, playerId, goles, true);
		}
		finishMatch(adminToken, matchId, scorerGoals, 1);
		return matchId;
	}

	private Long setupMatchWithParticipants(String adminToken, int year, Long participant1, Long participant2)
			throws Exception {
		Long matchId = createMatch(adminToken, dateInYear(year));
		openConvocatoria(adminToken, matchId);
		convocar(adminToken, matchId, participant1);
		convocar(adminToken, matchId, participant2);
		for (int i = 0; i < 8; i++) {
			convocar(adminToken, matchId, createPlayer("Otro" + i));
		}
		closeConvocatoria(adminToken, matchId);
		generateTeams(adminToken, matchId);
		startMatch(adminToken, matchId);
		for (Long playerId : convocadosIds(adminToken, matchId)) {
			updateStats(adminToken, matchId, playerId, 0, true);
		}
		finishMatch(adminToken, matchId, 2, 2);
		return matchId;
	}

	private void createRating(RatingSetup setup, int puntaje) throws Exception {
		createRating(setup.calificador().token(), setup.matchId(), setup.calificado(), puntaje);
	}

	private void createRating(String token, Long matchId, Long calificadoId, int puntaje) throws Exception {
		String body = objectMapper.writeValueAsString(new RatingRequest(calificadoId, puntaje));
		mockMvc.perform(post("/api/matches/" + matchId + "/ratings")
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated());
	}
}