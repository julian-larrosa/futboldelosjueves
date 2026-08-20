package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.IntegrationTestBase;
import com.fdlj.fdlj.dto.request.ChangePasswordRequest;
import com.fdlj.fdlj.dto.request.LoginRequest;
import com.fdlj.fdlj.dto.request.RegisterRequest;
import com.fdlj.fdlj.dto.request.ResetPasswordRequest;
import com.fdlj.fdlj.entity.enums.PlayerPosition;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends IntegrationTestBase {

	@Test
	void changeMyPassword_success_returns204AndAllowsLoginWithNewPassword() throws Exception {
		String token = registerPlayer("Cambio").token();

		mockMvc.perform(put("/api/users/me/password")
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new ChangePasswordRequest("password123", "nuevaContraseña123"))))
				.andExpect(status().isNoContent());

		String response = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new LoginRequest(
								userEmailFromToken(token), "nuevaContraseña123"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.mustChangePassword").value(false))
				.andReturn().getResponse().getContentAsString();
	}

	@Test
	void changeMyPassword_wrongCurrentPassword_returns400() throws Exception {
		String token = registerPlayer("CambioMal").token();

		mockMvc.perform(put("/api/users/me/password")
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new ChangePasswordRequest("contraseñaIncorrecta", "nuevaContraseña123"))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("La contraseña actual es incorrecta."));
	}

	@Test
	void changeMyPassword_invalidNewPassword_returns400() throws Exception {
		String token = registerPlayer("CambioInvalido").token();

		mockMvc.perform(put("/api/users/me/password")
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new ChangePasswordRequest("password123", "123"))))
				.andExpect(status().isBadRequest());
	}

	@Test
	void changeMyPassword_withoutToken_returns401() throws Exception {
		mockMvc.perform(put("/api/users/me/password")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new ChangePasswordRequest("password123", "nuevaContraseña123"))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void resetPassword_admin_setsFlagAndAllowsLogin() throws Exception {
		String adminToken = adminToken();
		String email = registerUserAndGetEmail();

		mockMvc.perform(put("/api/users/password/reset")
						.header("Authorization", bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new ResetPasswordRequest(email, "temporal123"))))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new LoginRequest(email, "temporal123"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.mustChangePassword").value(true));
	}

	@Test
	void resetPassword_admin_unknownEmail_returns404() throws Exception {
		String adminToken = adminToken();

		mockMvc.perform(put("/api/users/password/reset")
						.header("Authorization", bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new ResetPasswordRequest("noexiste@example.com", "temporal123"))))
				.andExpect(status().isNotFound());
	}

	@Test
	void resetPassword_invalidNewPassword_returns400() throws Exception {
		String adminToken = adminToken();
		String email = registerUserAndGetEmail();

		mockMvc.perform(put("/api/users/password/reset")
						.header("Authorization", bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new ResetPasswordRequest(email, "123"))))
				.andExpect(status().isBadRequest());
	}

	@Test
	void resetPassword_notAdmin_returns403() throws Exception {
		String playerToken = registerPlayer("NoAdmin").token();
		String email = registerUserAndGetEmail();

		mockMvc.perform(put("/api/users/password/reset")
						.header("Authorization", bearer(playerToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new ResetPasswordRequest(email, "temporal123"))))
				.andExpect(status().isForbidden());
	}

	@Test
	void changeMyPassword_afterAdminReset_clearsFlag() throws Exception {
		String adminToken = adminToken();
		String email = registerUserAndGetEmail();

		mockMvc.perform(put("/api/users/password/reset")
						.header("Authorization", bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new ResetPasswordRequest(email, "temporal123"))))
				.andExpect(status().isNoContent());

		String response = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new LoginRequest(email, "temporal123"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.mustChangePassword").value(true))
				.andReturn().getResponse().getContentAsString();

		String token = objectMapper.readTree(response).at("/data/token").asText();

		mockMvc.perform(put("/api/users/me/password")
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new ChangePasswordRequest("temporal123", "definitiva123"))))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new LoginRequest(email, "definitiva123"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.mustChangePassword").value(false));
	}

	private String registerUserAndGetEmail() throws Exception {
		String email = "player" + UUID.randomUUID() + "@example.com";
		String response = mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new RegisterRequest("user" + UUID.randomUUID(), email, "password123", "Juan", "Perez",
										PlayerPosition.DELANTERO))))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(response).at("/data/user/email").asText();
	}

	private String userEmailFromToken(String token) {
		return jwtService.extractEmail(token);
	}
}