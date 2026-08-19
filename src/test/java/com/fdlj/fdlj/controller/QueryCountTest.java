package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.IntegrationTestBase;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class QueryCountTest extends IntegrationTestBase {

	@Autowired
	private SessionFactory sessionFactory;

	@BeforeEach
	void resetQueryStatistics() {
		sessionFactory.getStatistics().clear();
		sessionFactory.getStatistics().setStatisticsEnabled(true);
	}

	private long queries() {
		return sessionFactory.getStatistics().getPrepareStatementCount();
	}

	@Test
	void getMatchStatistics_queryCountStaysBounded() throws Exception {
		String admin = adminToken();
		Long matchId = setupFinishedMatch10(admin);
		sessionFactory.getStatistics().clear();
		mockMvc.perform(get("/api/matches/" + matchId + "/statistics")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk());
		assertThat(queries())
				.as("consultas SQL de getMatchStatistics con 10 participaciones")
				.isLessThanOrEqualTo(8);
	}

	@Test
	void getStandings_queryCountStaysBounded() throws Exception {
		String admin = adminToken();
		setupFinishedMatch10(admin);
		sessionFactory.getStatistics().clear();
		mockMvc.perform(get("/api/statistics/standings")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk());
		assertThat(queries())
				.as("consultas SQL de getStandings sin año")
				.isLessThanOrEqualTo(8);
	}

	@Test
	void getTopScorers_queryCountStaysBounded() throws Exception {
		String admin = adminToken();
		setupFinishedMatch10(admin);
		sessionFactory.getStatistics().clear();
		mockMvc.perform(get("/api/statistics/top-scorers")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk());
		assertThat(queries())
				.as("consultas SQL de getTopScorers sin año")
				.isLessThanOrEqualTo(10);
	}

	@Test
	void getRatingRanking_queryCountStaysBounded() throws Exception {
		String admin = adminToken();
		setupFinishedMatchForRating(admin);
		sessionFactory.getStatistics().clear();
		mockMvc.perform(get("/api/statistics/ratings")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk());
		assertThat(queries())
				.as("consultas SQL de getRatingRanking sin año")
				.isLessThanOrEqualTo(6);
	}

	@Test
	void getStandings_queryCountDoesNotScaleWithNumberOfMatches() throws Exception {
		String admin1 = adminToken();
		setupFinishedMatch10(admin1);
		sessionFactory.getStatistics().clear();
		mockMvc.perform(get("/api/statistics/standings")
						.header("Authorization", bearer(admin1)))
				.andExpect(status().isOk());
		long queriesWithOneMatch = queries();

		String admin2 = adminToken();
		setupFinishedMatch10(admin2);
		setupFinishedMatch10(admin2);
		sessionFactory.getStatistics().clear();
		mockMvc.perform(get("/api/statistics/standings")
						.header("Authorization", bearer(admin2)))
				.andExpect(status().isOk());
		long queriesWithThreeMatches = queries();

		assertThat(queriesWithThreeMatches)
				.as("el count de consultas no debe crecer linealmente con la cantidad de partidos")
				.isLessThanOrEqualTo(queriesWithOneMatch + 1);
	}

	@Test
	void getPlayers_queryCountDoesNotScaleWithNumberOfPlayers() throws Exception {
		String admin = adminToken();
		createPlayer("Jugador1");
		sessionFactory.getStatistics().clear();
		mockMvc.perform(get("/api/players?size=10")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk());
		long queriesWithOnePlayer = queries();

		for (int i = 2; i <= 10; i++) {
			createPlayer("Jugador" + i);
		}
		sessionFactory.getStatistics().clear();
		mockMvc.perform(get("/api/players?size=10")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk());
		long queriesWithTenPlayers = queries();

		assertThat(queriesWithTenPlayers)
				.as("el count de consultas no debe crecer linealmente con la cantidad de jugadores")
				.isLessThanOrEqualTo(queriesWithOnePlayer + 1);
	}

	@Test
	void getMatches_queryCountDoesNotScaleWithNumberOfMatches() throws Exception {
		String admin = adminToken();
		createMatch(admin);
		sessionFactory.getStatistics().clear();
		mockMvc.perform(get("/api/matches?size=10")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk());
		long queriesWithOneMatch = queries();

		for (int i = 2; i <= 10; i++) {
			createMatch(admin);
		}
		sessionFactory.getStatistics().clear();
		mockMvc.perform(get("/api/matches?size=10")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk());
		long queriesWithTenMatches = queries();

		assertThat(queriesWithTenMatches)
				.as("el count de consultas no debe crecer linealmente con la cantidad de partidos")
				.isLessThanOrEqualTo(queriesWithOneMatch + 1);
	}
}
