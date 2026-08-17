package com.fdlj.fdlj.controller;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import com.fdlj.fdlj.dto.request.LoginRequest;
import com.fdlj.fdlj.dto.request.RegisterRequest;
import com.fdlj.fdlj.entity.enums.PlayerPosition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JsonMapper objectMapper;

	private String uniqueEmail() {
		return "user" + UUID.randomUUID() + "@example.com";
	}

	private RegisterRequest buildRegisterRequest(String username, String email) {
		return new RegisterRequest(username, email, "password123", "Juan", "Perez", PlayerPosition.DELANTERO);
	}

	@Test
	void register_success_returnsTokenAndCreatesUserAndPlayer() throws Exception {
		String email = uniqueEmail();
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(buildRegisterRequest("juan", email))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.token").isNotEmpty())
				.andExpect(jsonPath("$.data.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.data.user.email").value(email))
				.andExpect(jsonPath("$.data.user.role").value("PLAYER"))
				.andExpect(jsonPath("$.data.player.nombre").value("Juan"))
				.andExpect(jsonPath("$.data.player.activo").value(true));
	}

	@Test
	void register_duplicateEmail_returns409() throws Exception {
		String email = uniqueEmail();
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(buildRegisterRequest("user1" + UUID.randomUUID(), email))))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(buildRegisterRequest("user2" + UUID.randomUUID(), email))))
				.andExpect(status().isConflict());
	}

	@Test
	void register_duplicateUsername_returns409() throws Exception {
		String username = "dupuser" + UUID.randomUUID();
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(buildRegisterRequest(username, uniqueEmail()))))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(buildRegisterRequest(username, uniqueEmail()))))
				.andExpect(status().isConflict());
	}

	@Test
	void register_invalidPayload_returns400() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"\",\"email\":\"mal\",\"password\":\"123\",\"nombre\":\"\",\"apellido\":\"\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void login_success_returnsToken() throws Exception {
		String email = uniqueEmail();
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(buildRegisterRequest("loginuser" + UUID.randomUUID(), email))))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new LoginRequest(email, "password123"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.token").isNotEmpty())
				.andExpect(jsonPath("$.data.user.email").value(email));
	}

	@Test
	void login_wrongPassword_returns401() throws Exception {
		String email = uniqueEmail();
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(buildRegisterRequest("wrongpwd" + UUID.randomUUID(), email))))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new LoginRequest(email, "contraseñaIncorrecta"))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void login_unknownEmail_returns401() throws Exception {
		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new LoginRequest("noexiste@example.com", "password123"))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void players_withoutToken_returns401() throws Exception {
		mockMvc.perform(get("/api/players"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void players_withValidJwt_returns200() throws Exception {
		String email = uniqueEmail();
		String response = mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(buildRegisterRequest("tokenuser" + UUID.randomUUID(), email))))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		JsonNode json = objectMapper.readTree(response);
		String token = json.at("/data/token").asText();

		mockMvc.perform(get("/api/players")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());
	}
}
