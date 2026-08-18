package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HinchaControllerTest extends IntegrationTestBase {

	@Test
	void getAllHinchas_admin_returns200() throws Exception {
		String adminToken = adminToken();
		registerHincha("María");
		registerHincha("Juan");

		mockMvc.perform(get("/api/hinchas")
						.header("Authorization", bearer(adminToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content").isArray())
				.andExpect(jsonPath("$.data.totalElements").value(2));
	}

	@Test
	void getAllHinchas_hincha_returns403() throws Exception {
		HinchaInfo hincha = registerHincha("María");

		mockMvc.perform(get("/api/hinchas")
						.header("Authorization", bearer(hincha.token())))
				.andExpect(status().isForbidden());
	}

	@Test
	void getAllHinchas_withoutToken_returns401() throws Exception {
		mockMvc.perform(get("/api/hinchas"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void getHinchaById_admin_returns200() throws Exception {
		String adminToken = adminToken();
		HinchaInfo hincha = registerHincha("María");

		mockMvc.perform(get("/api/hinchas/" + hincha.hinchaId())
						.header("Authorization", bearer(adminToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(hincha.hinchaId()))
				.andExpect(jsonPath("$.data.nombre").value("María"))
				.andExpect(jsonPath("$.data.activo").value(true))
				.andExpect(jsonPath("$.data.email").isNotEmpty());
	}

	@Test
	void getHinchaById_notFound_returns404() throws Exception {
		mockMvc.perform(get("/api/hinchas/999999")
						.header("Authorization", bearer(adminToken())))
				.andExpect(status().isNotFound());
	}

	@Test
	void getHinchaById_hincha_returns403() throws Exception {
		HinchaInfo hincha = registerHincha("María");

		mockMvc.perform(get("/api/hinchas/" + hincha.hinchaId())
						.header("Authorization", bearer(hincha.token())))
				.andExpect(status().isForbidden());
	}
}