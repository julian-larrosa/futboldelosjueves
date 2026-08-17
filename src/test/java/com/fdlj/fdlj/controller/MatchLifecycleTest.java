package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.IntegrationTestBase;
import com.fdlj.fdlj.dto.request.AttributeRatingRequest;
import com.fdlj.fdlj.dto.request.MatchAttributeRatingsRequest;
import com.fdlj.fdlj.dto.request.RatingRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MatchLifecycleTest extends IntegrationTestBase {

	@Test
	void fullMatchLifecycle() throws Exception {
		String admin = adminToken();

		// 1. Crear partido
		Long matchId = createMatch(admin);
		mockMvc.perform(get("/api/matches/" + matchId)
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.estado").value("PROGRAMADO"));

		// 2. Abrir convocatoria
		openConvocatoria(admin, matchId);
		mockMvc.perform(get("/api/matches/" + matchId)
						.header("Authorization", bearer(admin)))
				.andExpect(jsonPath("$.data.estado").value("CONVOCATORIA_ABIERTA"));

		// 3. Convocar 10 jugadores
		PlayerInfo scorer = registerPlayer("Goleador");
		convocar(admin, matchId, scorer.playerId());
		PlayerInfo assister = registerPlayer("Asistidor");
		convocar(admin, matchId, assister.playerId());
		List<Long> otherIds = new ArrayList<>();
		for (int i = 0; i < 8; i++) {
			Long pid = createPlayer("Jugador" + i);
			otherIds.add(pid);
			convocar(admin, matchId, pid);
		}

		// 4. Cerrar convocatoria
		closeConvocatoria(admin, matchId);
		mockMvc.perform(get("/api/matches/" + matchId)
						.header("Authorization", bearer(admin)))
				.andExpect(jsonPath("$.data.estado").value("CONVOCATORIA_CERRADA"));

		// 5. Generar equipos
		generateTeams(admin, matchId);
		mockMvc.perform(get("/api/matches/" + matchId + "/teams")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.length()").value(2))
				.andExpect(jsonPath("$.data[0].jugadores.length()").value(5))
				.andExpect(jsonPath("$.data[1].jugadores.length()").value(5));

		// 6. Iniciar partido
		startMatch(admin, matchId);
		mockMvc.perform(get("/api/matches/" + matchId)
						.header("Authorization", bearer(admin)))
				.andExpect(jsonPath("$.data.estado").value("EN_CURSO"));

		// 7. Registrar stats individuales (goleador hace 2 goles + 1 asistencia, asistidor 1 asistencia)
		updateStats(admin, matchId, scorer.playerId(), 2, true);
		updateStats(admin, matchId, assister.playerId(), 0, true);
		for (Long pid : otherIds) {
			updateStats(admin, matchId, pid, 0, true);
		}

		// 8. Finalizar partido 3-1
		finishMatch(admin, matchId, 3, 1);
		mockMvc.perform(get("/api/matches/" + matchId)
						.header("Authorization", bearer(admin)))
				.andExpect(jsonPath("$.data.estado").value("FINALIZADO"))
				.andExpect(jsonPath("$.data.golesEquipoA").value(3))
				.andExpect(jsonPath("$.data.golesEquipoB").value(1));

		// 9. Verificar resultado
		mockMvc.perform(get("/api/matches/" + matchId + "/result")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.resultado").value("GANA_EQUIPO_A"));

		// 10. Verificar estadísticas del partido
		mockMvc.perform(get("/api/matches/" + matchId + "/statistics")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.length()").value(10));

		// 11. Verificar tabla de posiciones
		mockMvc.perform(get("/api/matches/" + matchId + "/standings")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.length()").value(10))
				.andExpect(jsonPath("$.data[0].puntos").isNumber());

		// 12. ADMIN califica atributos de todos los efectivos
		List<Long> allPlayers = new ArrayList<>();
		allPlayers.add(scorer.playerId());
		allPlayers.add(assister.playerId());
		allPlayers.addAll(otherIds);
		List<AttributeRatingRequest> attrRatings = new ArrayList<>();
		for (Long pid : allPlayers) {
			attrRatings.add(buildAttributeRating(pid, 7, 8, 6, 9, 7));
		}
		submitAttributeRatings(admin, matchId, attrRatings);

		// 13. Verificar atributos actualizados del goleador
		mockMvc.perform(get("/api/players/" + scorer.playerId() + "/attributes")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.attributes.length()").value(5))
				.andExpect(jsonPath("$.data.attributes[?(@.attributeType == 'TECNICA')].currentValue").value(7.0));

		// 14. Verificar historial de atributos
		mockMvc.perform(get("/api/players/" + scorer.playerId() + "/attributes/history")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.history.length()").value(5));

		// 15. Jugador califica a un par (peer rating)
		String body = objectMapper.writeValueAsString(new RatingRequest(assister.playerId(), 8));
		mockMvc.perform(post("/api/matches/" + matchId + "/ratings")
						.header("Authorization", bearer(scorer.token()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.puntaje").value(8));

		// 16. Verificar calificaciones del partido
		mockMvc.perform(get("/api/matches/" + matchId + "/ratings")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content.length()").value(1));

		// 17. Verificar estadísticas globales del goleador
		mockMvc.perform(get("/api/players/" + scorer.playerId() + "/statistics")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.partidosJugados").value(1))
				.andExpect(jsonPath("$.data.goles").value(2))
				.andExpect(jsonPath("$.data.victorias").value(1))
				.andExpect(jsonPath("$.data.ratingPromedio").isNumber());

		// 18. Verificar rendimiento reciente (últimos 3)
		mockMvc.perform(get("/api/players/" + scorer.playerId() + "/statistics/recent")
						.param("limit", "3")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.partidosJugados").value(1))
				.andExpect(jsonPath("$.data.indiceForma").value(1.0));

		// 19. Verificar máximos goleadores
		mockMvc.perform(get("/api/statistics/top-scorers")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.length()").value(10))
				.andExpect(jsonPath("$.data[0].goles").value(2))
				.andExpect(jsonPath("$.data[0].playerId").value(scorer.playerId()));
	}

	@Test
	void finishMatch_goalsExceedIndividual_returns409() throws Exception {
		String admin = adminToken();
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);

		Long scorer = createPlayer("GoleadorExceso");
		convocar(admin, matchId, scorer);
		for (int i = 0; i < 9; i++) {
			convocar(admin, matchId, createPlayer("Otro" + i));
		}
		closeConvocatoria(admin, matchId);
		generateTeams(admin, matchId);
		startMatch(admin, matchId);

		// El goleador anota 3 goles
		updateStats(admin, matchId, scorer, 3, true);
		for (Long pid : convocadosIds(admin, matchId)) {
			if (!pid.equals(scorer)) {
				updateStats(admin, matchId, pid, 0, true);
			}
		}

		// Finalizar con solo 2 goles para el equipo → 409
		String body = objectMapper.writeValueAsString(
				new com.fdlj.fdlj.dto.request.MatchResultRequest(2, 0));
		mockMvc.perform(post("/api/matches/" + matchId + "/finalizar")
						.header("Authorization", bearer(admin))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isConflict());
	}
}
