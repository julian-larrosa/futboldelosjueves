package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.IntegrationTestBase;
import com.fdlj.fdlj.dto.request.ForgotPasswordRequest;
import com.fdlj.fdlj.dto.request.LoginRequest;
import com.fdlj.fdlj.dto.request.ResetPasswordRequest;
import com.fdlj.fdlj.entity.Player;
import com.fdlj.fdlj.entity.User;
import com.fdlj.fdlj.entity.enums.PlayerPosition;
import com.fdlj.fdlj.entity.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ForgotPasswordTest extends IntegrationTestBase {

	@Test
	void forgotPassword_success_updatesPasswordAndAllowsLogin() throws Exception {
		String email = emailFromToken(registerPlayer("Recupera").token());

		mockMvc.perform(post("/api/auth/password/forgot")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new ForgotPasswordRequest(email, "nuevaClave123"))))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new LoginRequest(email, "nuevaClave123"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.mustChangePassword").value(false));
	}

	@Test
	void forgotPassword_oldPasswordNoLongerWorks() throws Exception {
		String email = emailFromToken(registerPlayer("RecuperaVieja").token());

		mockMvc.perform(post("/api/auth/password/forgot")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new ForgotPasswordRequest(email, "nuevaClave123"))))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new LoginRequest(email, "password123"))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void forgotPassword_unknownEmail_returns404() throws Exception {
		mockMvc.perform(post("/api/auth/password/forgot")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new ForgotPasswordRequest("noexiste" + UUID.randomUUID() + "@example.com", "nuevaClave123"))))
				.andExpect(status().isNotFound());
	}

	@Test
	void forgotPassword_inactiveUser_returns404() throws Exception {
		String email = "inactivo" + UUID.randomUUID() + "@example.com";
		User user = new User();
		user.setUsername("usuario" + UUID.randomUUID());
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode("password123"));
		user.setRole(Role.PLAYER);
		userRepository.save(user);

		Player player = new Player();
		player.setNombre("Inactivo");
		player.setApellido("Recupera");
		player.setEmail(email);
		player.setPosicion(PlayerPosition.DELANTERO);
		player.setActivo(false);
		player.setUser(user);
		playerRepository.save(player);

		mockMvc.perform(post("/api/auth/password/forgot")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new ForgotPasswordRequest(email, "nuevaClave123"))))
				.andExpect(status().isNotFound());
	}

	@Test
	void forgotPassword_invalidNewPassword_returns400() throws Exception {
		String email = emailFromToken(registerPlayer("RecuperaInvalida").token());

		mockMvc.perform(post("/api/auth/password/forgot")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new ForgotPasswordRequest(email, "123"))))
				.andExpect(status().isBadRequest());
	}

	@Test
	void forgotPassword_afterAdminReset_clearsMustChangeFlag() throws Exception {
		String adminToken = adminToken();
		String email = emailFromToken(registerPlayer("RecuperaFlag").token());

		mockMvc.perform(put("/api/users/password/reset")
						.header("Authorization", bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new ResetPasswordRequest(email, "temporal123"))))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/auth/password/forgot")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new ForgotPasswordRequest(email, "nuevaClave123"))))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new LoginRequest(email, "nuevaClave123"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.mustChangePassword").value(false));
	}

	private String emailFromToken(String token) {
		return jwtService.extractEmail(token);
	}
}
