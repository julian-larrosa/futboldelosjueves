package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.IntegrationTestBase;
import com.fdlj.fdlj.dto.request.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = {
		"app.security.auth-rate-limit.enabled=true",
		"app.security.auth-rate-limit.max=3",
		"app.security.auth-rate-limit.window-seconds=60"
})
class AuthRateLimitTest extends IntegrationTestBase {

	@Test
	void login_moreThanLimit_returns429() throws Exception {
		String body = objectMapper.writeValueAsString(new LoginRequest("nadie@example.com", "mal"));
		for (int i = 0; i < 3; i++) {
			mockMvc.perform(post("/api/auth/login")
							.contentType(MediaType.APPLICATION_JSON)
							.content(body))
					.andExpect(status().isUnauthorized());
		}
		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isTooManyRequests());
	}

	@Test
	void nonAuthEndpoint_notRateLimited_returns401() throws Exception {
		mockMvc.perform(get("/api/players"))
				.andExpect(status().isUnauthorized());
	}
}