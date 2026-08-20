package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.IntegrationTestBase;
import com.fdlj.fdlj.dto.request.PlayerRequest;
import com.fdlj.fdlj.entity.enums.PlayerPosition;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PlayerControllerTest extends IntegrationTestBase {

	@Test
	void updatePlayer_asAdmin_returns200() throws Exception {
		String admin = adminToken();
		Long playerId = createPlayer("ParaActualizar");
		String body = objectMapper.writeValueAsString(
				new PlayerRequest("NuevoNombre", "NuevoApellido", "actualizado" + UUID.randomUUID() + "@example.com",
						PlayerPosition.ARQUERO));
		mockMvc.perform(put("/api/players/" + playerId)
						.header("Authorization", bearer(admin))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.nombre").value("NuevoNombre"))
				.andExpect(jsonPath("$.data.posicion").value("ARQUERO"));
	}

	@Test
	void updatePlayer_withPlayerToken_returns403() throws Exception {
		PlayerInfo player = registerPlayer("SinPermiso");
		Long playerId = createPlayer("Objetivo");
		String body = objectMapper.writeValueAsString(
				new PlayerRequest("OtroNombre", "OtroApellido", "otro" + UUID.randomUUID() + "@example.com",
						PlayerPosition.DELANTERO));
		mockMvc.perform(put("/api/players/" + playerId)
						.header("Authorization", bearer(player.token()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isForbidden());
	}

	@Test
	void updatePlayer_withHinchaToken_returns403() throws Exception {
		HinchaInfo hincha = registerHincha("HinchaSinPermiso");
		Long playerId = createPlayer("Objetivo");
		String body = objectMapper.writeValueAsString(
				new PlayerRequest("OtroNombre", "OtroApellido", "otro" + UUID.randomUUID() + "@example.com",
						PlayerPosition.DELANTERO));
		mockMvc.perform(put("/api/players/" + playerId)
						.header("Authorization", bearer(hincha.token()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isForbidden());
	}

	@Test
	void deactivatePlayer_asAdmin_returns204() throws Exception {
		String admin = adminToken();
		Long playerId = createPlayer("ParaDesactivar");
		mockMvc.perform(delete("/api/players/" + playerId)
						.header("Authorization", bearer(admin)))
				.andExpect(status().isNoContent());
	}

	@Test
	void deactivatePlayer_withPlayerToken_returns403() throws Exception {
		PlayerInfo player = registerPlayer("SinPermiso");
		Long playerId = createPlayer("Objetivo");
		mockMvc.perform(delete("/api/players/" + playerId)
						.header("Authorization", bearer(player.token())))
				.andExpect(status().isForbidden());
	}

	@Test
	void deactivatePlayer_withHinchaToken_returns403() throws Exception {
		HinchaInfo hincha = registerHincha("HinchaSinPermiso");
		Long playerId = createPlayer("Objetivo");
		mockMvc.perform(delete("/api/players/" + playerId)
						.header("Authorization", bearer(hincha.token())))
				.andExpect(status().isForbidden());
	}

	@Test
	void getAllPlayers_conFiltroNombre_returns200AndFiltra() throws Exception {
		String admin = adminToken();
		createPlayer("BuscadoPorNombre");
		mockMvc.perform(get("/api/players")
						.header("Authorization", bearer(admin))
						.param("nombre", "buscado"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.content").isArray())
				.andExpect(jsonPath("$.data.totalElements").value(1));
	}
}