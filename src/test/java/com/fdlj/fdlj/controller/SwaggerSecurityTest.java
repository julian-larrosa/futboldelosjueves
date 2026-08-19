package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = "app.security.swagger-enabled=false")
class SwaggerSecurityTest extends IntegrationTestBase {

	@Test
	void swaggerDocs_withoutToken_whenDisabled_returns401() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void swaggerUi_withoutToken_whenDisabled_returns401() throws Exception {
		mockMvc.perform(get("/swagger-ui.html"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void swaggerDocs_withValidToken_whenDisabled_returns200() throws Exception {
		String admin = adminToken();
		mockMvc.perform(get("/v3/api-docs")
						.header("Authorization", bearer(admin)))
				.andExpect(status().isOk());
	}
}