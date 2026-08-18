package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.IntegrationTestBase;
import com.fdlj.fdlj.dto.request.PlayerRequest;
import com.fdlj.fdlj.entity.enums.PlayerPosition;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PlayerEmailReuseTest extends IntegrationTestBase {

	private String createPlayerViaApi(String adminToken, String email) throws Exception {
		String body = objectMapper.writeValueAsString(
				new PlayerRequest("Juan", "Perez", email, PlayerPosition.DELANTERO));
		String response = mockMvc.perform(post("/api/players")
						.header("Authorization", bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(response).at("/data/id").asText();
	}

	@Test
	void createPlayer_reusingEmailOfDeactivatedPlayer_returns201() throws Exception {
		String admin = adminToken();
		String email = "reutilizado" + UUID.randomUUID() + "@example.com";
		String id = createPlayerViaApi(admin, email);

		mockMvc.perform(delete("/api/players/" + id)
						.header("Authorization", bearer(admin)))
				.andExpect(status().isNoContent());

		String body = objectMapper.writeValueAsString(
				new PlayerRequest("Maria", "Gomez", email, PlayerPosition.DEFENSOR));
		mockMvc.perform(post("/api/players")
						.header("Authorization", bearer(admin))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.email").value(email));
	}

	@Test
	void createPlayer_duplicateEmailWhileActive_returns409() throws Exception {
		String admin = adminToken();
		String email = "duplicado" + UUID.randomUUID() + "@example.com";
		createPlayerViaApi(admin, email);

		String body = objectMapper.writeValueAsString(
				new PlayerRequest("Maria", "Gomez", email, PlayerPosition.DEFENSOR));
		mockMvc.perform(post("/api/players")
						.header("Authorization", bearer(admin))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isConflict());
	}
}
